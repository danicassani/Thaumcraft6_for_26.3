"""Translate generator-authored loot to the native 26.3 condition/modifier codec.

Only loot_table resources are transformed; already modern resources are stable.
"""
from pathlib import Path


def modern_loot(value):
    if isinstance(value, list):
        return [modern_loot(child) for child in value]
    if not isinstance(value, dict):
        return value
    out = {}
    for key, child in value.items():
        if key == 'conditions':
            if 'condition' in value:
                raise ValueError('Mixed old and new loot conditions')
            terms = [modern_condition(term) for term in child]
            if terms:
                out['condition'] = terms[0] if len(terms) == 1 else {'type': 'minecraft:all_of', 'terms': terms}
        elif key == 'functions' and value.get('type', value.get('function')) != 'minecraft:sequence':
            if 'modifier' in value:
                raise ValueError('Mixed old and new loot modifiers')
            if child:
                out['modifier'] = [modern_loot(term) for term in child]
        elif key == 'function':
            out['type'] = child
        elif key == 'condition' and isinstance(child, dict):
            out[key] = modern_condition(child)
        else:
            out[key] = modern_loot(child)
    return out


def modern_condition(value):
    if isinstance(value, str):
        return value
    kind = value.get('condition', value.get('type'))
    if kind == 'minecraft:match_tool' and value.get('predicate') == {
        'predicates': {'minecraft:enchantments': [{'enchantments': 'minecraft:silk_touch', 'levels': {'min': 1}}]}
    }:
        return 'minecraft:tool/can_silk_touch'
    if kind == 'minecraft:block_state_property':
        return {'type': 'minecraft:match_block', 'blocks': value['block'], 'state': value.get('properties', {})}
    return {'type' if key == 'condition' else key:
            [modern_condition(term) for term in child] if key == 'terms' else
            modern_condition(child) if key == 'term' else modern_loot(child)
            for key, child in value.items()}


def for_resource(path, value):
    return modern_loot(value) if 'loot_table' in Path(path).parts else value
