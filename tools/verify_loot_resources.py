#!/usr/bin/env python3
"""Reject legacy loot fields which 26.3 can silently ignore. Native tests check semantics."""
import json
import sys
from pathlib import Path


def verify(root):
    tables = list(root.glob('data/*/loot_table/**/*.json'))
    if not tables:
        raise ValueError('No loot tables found; run processResources first')
    errors = []

    def walk(value, location):
        if isinstance(value, list):
            for index, child in enumerate(value):
                walk(child, f'{location}[{index}]')
        elif isinstance(value, dict):
            for key, child in value.items():
                if key in ('conditions', 'function') or key == 'functions' and value.get('type') != 'minecraft:sequence':
                    errors.append(f'{location}.{key}: legacy loot field; use native condition/modifier/type')
                if key == 'condition' and isinstance(child, dict) and 'type' not in child:
                    errors.append(f'{location}.condition: predicate lacks native type discriminator')
                if value.get('type') == 'minecraft:alternatives' and key == 'children':
                    for index, entry in enumerate(child[:-1]):
                        if 'condition' not in entry:
                            errors.append(f'{location}.children[{index}]: unconditional alternative hides later drops')
                walk(child, f'{location}.{key}')

    for table in tables:
        walk(json.loads(table.read_text()), str(table.relative_to(root)))
    if errors:
        raise ValueError('\n'.join(errors))
    return len(tables)


if __name__ == '__main__':
    root = Path(sys.argv[1]) if len(sys.argv)>1 else Path(__file__).resolve().parents[1] / 'build/staging/main/resources'
    try:
        print(f'Loot schema OK: {verify(root)} tables; native tests verify drops and probabilities')
    except ValueError as ex:
        sys.exit(str(ex))
