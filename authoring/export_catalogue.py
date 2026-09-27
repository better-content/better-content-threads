#!/usr/bin/env python3
"""Export the fixed bundled definitions with reviewed card artwork."""
import json
from pathlib import Path
repo = Path(__file__).resolve().parent.parent
cards = json.loads((repo / 'authoring/discoveries.json').read_text())['cards']
assert len(cards) == 52 and sorted(c['order'] for c in cards) == list(range(1, 53))
assert len({c['id'] for c in cards}) == 52
textures = repo / 'src/main/resources/assets/learning_surfaces/textures/gui/threads'
threads = []
for card in cards:
    row = {k: v for k, v in card.items() if k not in {'scene', 'evidence'}}
    if not (textures / f"{card['id']}.png").is_file():
        raise FileNotFoundError(f"Missing reviewed card art: {card['id']}")
    threads.append(row)
output = {'schema': 'bc.learning_surfaces.cards.v1', 'threads': threads}
(repo / 'src/main/resources/data/learning_surfaces/threads/catalogue.json').write_text(json.dumps(output, indent=2) + '\n')
