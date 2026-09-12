"""Provenance failures must stop CI before an API artifact enters its provider directory."""
import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import zipfile

spec = importlib.util.spec_from_file_location('provider_bootstrap', Path(__file__).with_name('prepare-source-providers.py'))
bootstrap = importlib.util.module_from_spec(spec)
spec.loader.exec_module(bootstrap)


class ProviderBootstrapTest(unittest.TestCase):
    def setUp(self):
        temporary_root = Path.home() / '.tmp'
        temporary_root.mkdir(exist_ok=True)
        self.temp = tempfile.TemporaryDirectory(dir=temporary_root)
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.provider = {'repository': 'example', 'artifact': 'example-1.0.jar', 'modId': 'example', 'dependsOn': []}

    def jar(self, mod_id='example'):
        target = self.root / 'mods' / self.provider['artifact']
        target.parent.mkdir(exist_ok=True)
        with zipfile.ZipFile(target, 'w') as jar:
            jar.writestr('META-INF/mods.toml', '[[mods]]\nmodId="' + mod_id + '"\n')
            jar.writestr('META-INF/better-content-source.properties',
                         'schema=bc.custom_mod_source.v1\nrepository=example\nmod_id=example\ncommit=' + 'a' * 40 + '\n')
        return target

    def test_modified_baseline_jar_is_rejected_even_with_matching_checkout_head(self):
        artifact = self.jar()
        original_hash = hashlib.sha256(artifact.read_bytes()).hexdigest()
        (self.root / 'gradle').mkdir()
        (self.root / 'gradle/active-custom-mods.json').write_text(json.dumps({
            'schema': 'bc.active_custom_mods.v1', 'mods': [self.provider]}))
        (self.root / 'index.toml').write_text('hash-format="sha256"\n[[files]]\nfile="mods/example-1.0.jar"\nhash="' + original_hash + '"\n')
        # A still-valid ZIP can change independently of its checkout and expected index hash.
        with zipfile.ZipFile(artifact, 'a') as jar:
            jar.writestr('unexpected.txt', 'modified payload')
        with patch.object(bootstrap, 'git', side_effect=['b' * 40, '']):
            with self.assertRaisesRegex(ValueError, 'hash mismatch'):
                bootstrap.baseline_payloads(self.root, {'baselineCommit': 'b' * 40}, [self.provider])

    def test_wrong_mod_identity_is_rejected(self):
        with self.assertRaisesRegex(ValueError, 'identity mismatch'):
            bootstrap.validate_jar(self.jar('unrelated_mod'), self.provider)

    def test_wrong_source_revision_is_rejected(self):
        with patch.object(bootstrap, 'git', return_value='b' * 40):
            with self.assertRaisesRegex(ValueError, 'does not match pin'):
                bootstrap.validate_checkout(self.root, {**self.provider, 'sourceCommit': 'a' * 40})

    def test_dependency_cycle_is_rejected(self):
        path = self.root / 'pins.json'
        path.write_text(json.dumps({'schema': 'bc.threads_ci_providers.v1', 'baselineCommit': 'a' * 40,
                                    'providers': [{**self.provider, 'dependsOn': ['example']}]}))
        with self.assertRaisesRegex(ValueError, 'dependency cycle'):
            bootstrap.plan(path)


if __name__ == '__main__':
    unittest.main()
