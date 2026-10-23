#!/usr/bin/env python3
"""Check reachable local models, texture files and block/item atlas membership.

Usage: python3 tools/verify_model_resources.py [processed-resource-directory]
Run processResources first: it includes the preserved legacy textures.
Vanilla model/texture contents are checked by the native client, not this tool.
"""
import json
import sys
from pathlib import Path


def verify(root):
    assets = root / 'assets'
    models = {}
    for path in assets.glob('*/models/**/*.json'):
        namespace = path.relative_to(assets).parts[0]
        models[f'{namespace}:{path.relative_to(assets / namespace / "models").as_posix()[:-5]}'] = json.loads(path.read_text())
    atlas = [('directory', 'block', 'block/'), ('directory', 'item', 'item/')]
    for atlas_path in [assets / 'minecraft/atlases/blocks.json', assets / 'minecraft/atlases/items.json']:
        if not atlas_path.exists():
            continue
        for source in json.loads(atlas_path.read_text())['sources']:
            kind = source['type'].removeprefix('minecraft:')
            if kind == 'directory':
                atlas.append((kind, source['source'], source['prefix']))
            elif kind == 'single':
                atlas.append((kind, source.get('sprite', source['resource']), source['resource']))
            else:
                raise ValueError(f'Unsupported model atlas source: {source}')
    errors, cache = [], {}
    roots = set()

    def references(value):
        if isinstance(value, dict):
            if isinstance(value.get('model'), str):
                roots.add(value['model'])
            for child in value.values():
                references(child)
        elif isinstance(value, list):
            for child in value:
                references(child)

    for directory in ['blockstates', 'items']:
        for path in assets.glob(f'*/{directory}/**/*.json'):
            data = json.loads(path.read_text())
            if directory == 'blockstates' and path.parent.parent.name == 'thaumcraft' and (path.stem.startswith('crystal_') or path.stem == 'taint_fibre'):
                if not any('when' not in part for part in data.get('multipart', [])):
                    errors.append(f'{path.name}: multipart needs an unconditional particle fallback for unsupported states')
            references(data)

    def inherit(name, chain=()):
        if ':' not in name:
            name = 'minecraft:' + name
        if name in chain:
            raise ValueError(f'Model inheritance cycle: {chain + (name,)}')
        if name in cache:
            return cache[name]
        if name not in models:
            if name.startswith('minecraft:'):
                return {}, True
            raise ValueError(f'Missing model: {name}')
        data = models[name]
        textures, external = inherit(data['parent'], chain + (name,)) if 'parent' in data else ({}, False)
        textures = {**textures, **data.get('textures', {})}
        cache[name] = textures, external
        return textures, external

    def texture_file(ref):
        namespace, path = ref.split(':', 1)
        return assets / namespace / 'textures' / (path + '.png')

    def in_atlas(ref):
        namespace, path = ref.split(':', 1)
        for kind, source, prefix in atlas:
            if kind == 'single' and source == ref and texture_file(prefix).is_file():
                return True
            if kind == 'directory' and path.startswith(prefix):
                actual = assets / namespace / 'textures' / source / (path[len(prefix):] + '.png')
                if actual.is_file():
                    return True
        return False

    for name in sorted(roots):
        try:
            if ':' not in name:
                name = 'minecraft:' + name
            if name.startswith('minecraft:'):
                continue
            if name not in models:
                raise ValueError(f'Missing model: {name}')
            model = models[name]
            textures, external = inherit(name)

            def resolve(ref, seen=()):
                if not ref.startswith('#'):
                    return ref if ':' in ref else 'minecraft:' + ref
                key = ref[1:]
                if key in seen:
                    raise ValueError(f'Texture alias cycle: {seen + (key,)}')
                if key not in textures:
                    if external:
                        return None
                    raise ValueError(f'Missing texture reference: {key}')
                return resolve(textures[key], seen + (key,))

            refs = list(textures.values())
            for element in model.get('elements', []):
                refs.extend(face['texture'] for face in element.get('faces', {}).values())
            if name.split(':', 1)[1].startswith('block/') and not external:
                refs.append('#particle')
            for ref in refs:
                target = resolve(ref)
                if target is None or target.startswith('minecraft:'):
                    continue
                if not texture_file(target).is_file():
                    raise ValueError(f'Missing texture file: {target}')
                if not in_atlas(target):
                    raise ValueError(f'Texture absent from model atlases: {target}')
        except ValueError as ex:
            errors.append(f'{name}: {ex}')
    if errors:
        raise ValueError('\n'.join(errors))
    if not roots:
        raise ValueError('No model references found; run processResources first')
    return len(roots)


if __name__ == '__main__':
    root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / 'build/staging/main/resources'
    try:
        print(f'Model resources OK: {verify(root)} model references; vanilla resources require native validation')
    except ValueError as ex:
        sys.exit(str(ex))
