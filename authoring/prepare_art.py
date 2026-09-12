#!/usr/bin/env python3
"""Build packaged derivatives from reviewed imagegen masters. No image generation here."""
import argparse
import json
import subprocess
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument('review_bundle', type=Path)
args = parser.parse_args()
repo = Path(__file__).resolve().parent.parent
assets = repo / 'src/main/resources/assets/better_content_threads'
cards = json.loads((repo / 'authoring/discoveries.json').read_text())['cards']
briefs = json.loads((assets / 'loading_briefs/catalogue.json').read_text())['briefs']

def convert(source, target, *options):
    target.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(['gm', 'convert', str(source), *options, '-strip', str(target)], check=True)

for card in cards:
    name = card['id']
    source = args.review_bundle / 'masters' / (name + '.png')
    target = assets / 'textures/gui/threads' / (name + '.png')
    convert(source, target, '-resize', '256x384!')
    convert(source, target.with_name(name + '_thumb.png'), '-resize', '256x384!', '-colorspace', 'GRAY')
    convert(source, assets / 'textures/item/thread_cards' / (name + '.png'), '-resize', '256x384!')
    model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'better_content_threads:item/thread_cards/' + name}}
    (assets / 'models/item/thread_cards' / (name + '.json')).write_text(json.dumps(model, indent=2) + '\n')
model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'minecraft:item/paper', 'particle': 'minecraft:item/paper'},
         'overrides': [{'predicate': {'better_content_threads:thread_index': c['order']},
                        'model': 'better_content_threads:item/thread_cards/' + c['id']} for c in cards]}
(assets / 'models/item/thread_facsimile.json').write_text(json.dumps(model, indent=2) + '\n')
for brief in briefs:
    source = args.review_bundle / 'lessons' / (brief['id'] + '.png')
    if source.is_file():
        convert(source, assets / 'textures/gui/loading_briefs' / source.name, '-resize', '512x256!')
print(f'Prepared {len(cards)} cards, thumbnails and facsimiles; available lesson masters converted.')
