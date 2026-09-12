#!/usr/bin/env python3
"""Prepare Threads CI APIs from a hash-checked pack baseline and pinned source builds.

Python 3.11+, git and Java 17 are required. This stages compile providers only;
it never deploys, refreshes, packages, or tests the modpack.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tomllib
import zipfile


def git(checkout, *args):
    return subprocess.check_output(['git', '-C', str(checkout), *args], text=True).strip()


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def plan(path):
    pins = json.loads(path.read_text())
    if pins.get('schema') != 'bc.threads_ci_providers.v1':
        raise ValueError('unsupported provider pins schema')
    if not re.fullmatch('[0-9a-f]{40}', pins.get('baselineCommit', '')):
        raise ValueError('baseline must be an exact source commit')
    mods = {p['repository']: p for p in pins['providers']}
    if len(mods) != len(pins['providers']):
        raise ValueError('duplicate provider repository')
    artifacts = set()
    for name, p in mods.items():
        if not re.fullmatch('[a-z0-9-]+', name):
            raise ValueError(f'unsafe repository: {name}')
        if not re.fullmatch(r'[A-Za-z0-9._+-]+\.jar', p['artifact']):
            raise ValueError(f'unsafe artifact: {name}')
        if p['artifact'] in artifacts:
            raise ValueError('duplicate provider artifact')
        artifacts.add(p['artifact'])
        if 'sourceCommit' in p and not re.fullmatch('[0-9a-f]{40}', p['sourceCommit']):
            raise ValueError(f'provider must use an exact source commit: {name}')
    ordered, active, done = [], set(), set()

    def visit(name):
        if name in active:
            raise ValueError(f'provider dependency cycle: {name}')
        if name in done:
            return
        if name not in mods:
            raise ValueError(f'missing provider dependency: {name}')
        active.add(name)
        for dependency in mods[name].get('dependsOn', []):
            visit(dependency)
        active.remove(name)
        done.add(name)
        ordered.append(mods[name])

    for name in mods:
        visit(name)
    return pins, ordered


def validate_jar(path, provider):
    if path.is_symlink():
        raise ValueError(f'symlink JAR: {path}')
    with zipfile.ZipFile(path) as jar:
        if jar.testzip() is not None:
            raise ValueError(f'corrupt JAR: {path}')
        metadata = tomllib.loads(jar.read('META-INF/mods.toml').decode())
        if provider['modId'] not in {mod['modId'] for mod in metadata['mods']}:
            raise ValueError(f'JAR mod identity mismatch: {path}')


def baseline_payloads(pack, pins, ordered):
    if git(pack, 'rev-parse', 'HEAD') != pins['baselineCommit']:
        raise ValueError('pack baseline checkout does not match pinned revision')
    # Check relevant tracked inputs too; HEAD alone does not establish file provenance.
    if git(pack, 'status', '--porcelain', '--untracked-files=no', '--', 'index.toml', 'mods', 'gradle/active-custom-mods.json'):
        raise ValueError('pack baseline provider inputs are dirty')
    inventory = json.loads((pack / 'gradle/active-custom-mods.json').read_text())
    if inventory.get('schema') != 'bc.active_custom_mods.v1':
        raise ValueError('unsupported baseline inventory')
    mods = {m['repository']: m for m in inventory['mods']}
    index = tomllib.loads((pack / 'index.toml').read_text())
    if index.get('hash-format') != 'sha256':
        raise ValueError('baseline index must use sha256')
    hashes = {p['file']: p['hash'] for p in index['files']}
    payloads = {}
    for p in ordered:
        if 'sourceCommit' in p:
            continue
        name = p['repository']
        if any(mods[name][key] != p[key] for key in ('artifact', 'modId')):
            raise ValueError(f'baseline inventory mismatch: {name}')
        source = pack / 'mods' / p['artifact']
        validate_jar(source, p)
        sha = digest(source)
        if hashes.get('mods/' + p['artifact']) != sha:
            raise ValueError(f'baseline JAR hash mismatch: {name}')
        with zipfile.ZipFile(source) as jar:
            identity = dict(line.split('=', 1) for line in jar.read(
                'META-INF/better-content-source.properties').decode().splitlines()
                if line and not line.startswith('#') and '=' in line)
        if (identity.get('schema') != 'bc.custom_mod_source.v1'
                or identity.get('repository') != name or identity.get('mod_id') != p['modId']
                or not re.fullmatch('[0-9a-f]{40}', identity.get('commit', ''))):
            raise ValueError(f'baseline source identity mismatch: {name}')
        payloads[name] = {**p, 'sourceCommit': identity['commit'], 'sha256': sha, 'origin': 'baseline'}
    return payloads


def validate_checkout(checkout, p):
    if git(checkout, 'rev-parse', 'HEAD') != p['sourceCommit']:
        raise ValueError(f'source checkout does not match pin: {p["repository"]}')
    if git(checkout, 'status', '--porcelain', '--untracked-files=normal'):
        raise ValueError(f'source checkout is dirty: {p["repository"]}')


def prepare(args):
    pins, ordered = plan(args.pins)
    payloads = baseline_payloads(args.pack_root.resolve(), pins, ordered)
    if args.validate_local is not None:
        for p in ordered:
            if 'sourceCommit' in p:
                checkout = args.validate_local / p['repository']
                validate_checkout(checkout, p)
                validate_jar(checkout / 'build/libs' / p['artifact'], p)
        print(f'Validated {len(ordered)} provider pins, baseline hashes, local source revisions and JAR identities; no build or staging performed.')
        return
    output, sources = args.output.resolve(), args.sources.resolve()
    if args.output.is_symlink() or args.sources.is_symlink():
        raise ValueError('CI output/source directories must not be symlinks')
    # A new isolated destination prevents stale or unrelated JARs contaminating the API set.
    output.mkdir(parents=True, exist_ok=False)
    sources.mkdir(parents=True, exist_ok=False)
    env = {**os.environ, 'BC_CUSTOM_MOD_JAR_DIR': str(output)}

    def manifest():
        (output / 'providers.json').write_text(json.dumps({
            'repository': 'better-content-threads', 'baselineCommit': pins['baselineCommit'],
            'providers': list(payloads.values())}, indent=2) + '\n')

    for p in ordered:
        name = p['repository']
        if 'sourceCommit' not in p:
            shutil.copyfile(args.pack_root / 'mods' / p['artifact'], output / p['artifact'])
            continue
        checkout = sources / name
        subprocess.run(['git', 'init', str(checkout)], check=True)
        subprocess.run(['git', '-C', str(checkout), 'remote', 'add', 'origin',
                        f'https://github.com/better-content/{name}.git'], check=True)
        subprocess.run(['git', '-C', str(checkout), 'fetch', '--depth=1', 'origin', p['sourceCommit']], check=True)
        subprocess.run(['git', '-C', str(checkout), 'checkout', '--detach', 'FETCH_HEAD'], check=True)
        validate_checkout(checkout, p)
        subprocess.run(['./gradlew', '--no-daemon', 'stageRuntimeJar'], cwd=checkout, env=env, check=True)
        validate_checkout(checkout, p)
        source = checkout / 'build/libs' / p['artifact']
        validate_jar(source, p)
        shutil.copyfile(source, output / p['artifact'])
        payloads[name] = {**p, 'sha256': digest(source), 'origin': 'source'}
        manifest()
    manifest()
    print(f'Staged {len(ordered)} verified-identity provider JARs to {output}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--pins', type=Path, default=Path(__file__).with_name('provider-pins.json'))
    parser.add_argument('--pack-root', type=Path, required=True)
    parser.add_argument('--output', type=Path, default=Path('build/ci-provider-jars'))
    parser.add_argument('--sources', type=Path, default=Path('build/ci-provider-sources'))
    parser.add_argument('--validate-local', type=Path, help='Read-only validation against existing local source checkouts and staged JARs')
    prepare(parser.parse_args())
