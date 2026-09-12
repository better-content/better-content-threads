#!/usr/bin/env python3
"""Export player-facing definitions without authoring-only scenes and evidence notes."""
import json
from pathlib import Path
repo = Path(__file__).resolve().parent.parent
cards = json.loads((repo / 'authoring/discoveries.json').read_text())['cards']
assert len(cards) == 52 and sorted(c['order'] for c in cards) == list(range(1, 53))
assert len({c['id'] for c in cards}) == 52
output = {'schema': 'bc.threads.v4', 'threads': [{k: v for k, v in c.items() if k not in {'scene', 'evidence'}} for c in cards]}
(repo / 'src/main/resources/data/better_content_threads/threads/catalogue.json').write_text(json.dumps(output, indent=2) + '\n')
