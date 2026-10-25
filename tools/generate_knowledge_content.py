#!/usr/bin/env python3
"""Extract original research, category formulae and explicitly assigned aspects."""
import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OLD = ROOT / 'src/main'
OUT = ROOT / 'src/modern/resources/assets/thaumcraft'


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n')


aspect_source = (OLD / 'java/thaumcraft/api/aspects/Aspect.java').read_text()
aspect_source = re.sub(r'/\*.*?\*/', '', aspect_source, flags=re.S)
aspect_source = re.sub(r'//[^\n]*', '', aspect_source)
aspect_names = dict(re.findall(r'public static Aspect (\w+) = new Aspect\("([^"]+)"', aspect_source))
components = {tag: [] for tag in aspect_names.values()}
for name, parents in re.findall(r'public static Aspect (\w+) = new Aspect\([^;]+?new Aspect\[\] \{([^}]+)\}', aspect_source):
    components[aspect_names[name]] = [aspect_names[x.strip()] for x in parents.split(',')]


def aspects(text):
    return {aspect_names[name]: int(count) for name, count in re.findall(r'\.add\(Aspect\.(\w+), (\d+)\)', text)}


simple = json.loads((ROOT / 'tools/simple_content.json').read_text())
fields = {v['legacy_field']: v['id'] for group in simple.values() if isinstance(group, list) for v in group if isinstance(v, dict) and v.get('legacy_field')}
fields.update({'infusionMatrix':'infusion_matrix','pedestalArcane':'pedestal_arcane','pedestalAncient':'pedestal_ancient','pedestalEldritch':'pedestal_eldritch','jarBrain':'jar_brain','brain':'brain','curio':'curio','tube':'tube','tubeValve':'tube_valve','tubeFilter':'tube_filter','tubeRestrict':'tube_restrict','tubeOneway':'tube_oneway','tubeBuffer':'tube_buffer','label':'label','resonator':'resonator','saplingGreatwood':'sapling_greatwood','saplingSilverwood':'sapling_silverwood','leafGreatwood':'leaves_greatwood','leafSilverwood':'leaves_silverwood','crucible':'crucible','smelterBasic':'smelter_basic','alembic':'alembic','jarNormal':'jar_normal','jarVoid':'jar_void','phial':'phial','alumentum':'alumentum','scribingTools':'scribing_tools','researchTable':'research_table','tableWood':'table_wood','celestialNotes':'celestial_notes','thaumonomicon': 'thaumonomicon', 'thaumometer': 'thaumometer', 'salisMundus': 'salis_mundus',
               'arcaneWorkbench': 'arcane_workbench', 'arcaneWorkbenchCharger': 'arcane_workbench_charger',
               'visGenerator': 'vis_generator', 'rechargePedestal': 'recharge_pedestal', 'dioptra': 'dioptra',
               'voidSiphon': 'void_siphon', 'oreAmber': 'ore_amber', 'oreCinnabar': 'ore_cinnabar', 'oreQuartz': 'ore_quartz',
               'amber': 'amber', 'quicksilver': 'quicksilver', 'amberBlock': 'amber_block', 'amberBrick': 'amber_brick',
               'ingotThaumium': 'ingot_thaumium', 'ingotVoid': 'ingot_void', 'ingotBrass': 'ingot_brass'})
fields.update({'wandWorkbench':'wand_workbench','casterBasic':'caster_basic','travellerBoots':'traveller_boots','primordialPearl':'primordial_pearl'})
for family in ['void','elemental','cloth','fortress','voidRobe']:
    for suffix in ['Sword','Pick','Axe','Shovel','Hoe','Helm','Chest','Legs','Boots']:
        fields[family+suffix]=('void_robe' if family=='voidRobe' else family)+'_'+suffix.lower()
for name, tag in [('Air','aer'),('Fire','ignis'),('Water','aqua'),('Earth','terra'),('Order','ordo'),('Entropy','perditio'),('Taint','vitium')]:
    fields['crystal'+name] = 'crystal_'+tag

modern_ids = {'thaumcraft:'+p.stem for p in (OUT / 'items').glob('*.json')} | {'thaumcraft:thaumometer','thaumcraft:thaumonomicon'}
client = next((Path('/workspace/.gradle/caches/minecraftforge/forgegradle/mavenizer/caches/minecraft_tasks/26.3')).glob('client.jar'), None)
if client is None:
    raise RuntimeError('Minecraft 26.3 client.jar is required to validate modern item names')
with zipfile.ZipFile(client) as z:
    modern_ids |= {'minecraft:'+Path(n).stem for n in z.namelist() if n.startswith('assets/minecraft/items/') and n.endswith('.json')}

renames = {'grass':'grass_block','grass_path':'dirt_path','web':'cobweb','lit_redstone_ore':'redstone_ore',
           'hardened_clay':'terracotta','stonebrick':'stone_bricks','reeds':'sugar_cane','slime':'slime_block',
           'lit_pumpkin':'jack_o_lantern','portal':'nether_portal','noteblock':'note_block','waterlily':'lily_pad',
           'melon_block':'melon','melon':'melon_slice','fish':'cod','cooked_fish':'cooked_cod','speckled_melon':'glistering_melon_slice',
           'writable_book':'writable_book','record_13':'music_disc_13','dye':'ink_sac','golden_rail':'powered_rail'}


def modern_item(namespace, name, meta=0):
    if namespace == 'thaumcraft':
        if name == 'leather': return 'minecraft:leather'
        if name == 'nitor': name='nitor_'+['white','orange','magenta','lightblue','yellow','lime','pink','gray','silver','cyan','purple','blue','brown','green','red','black'][meta] if meta<16 else name
        if name in ['ingot','ingots']: name = ['ingot_thaumium','ingot_void','ingot_brass'][meta] if meta < 3 else name
        else: name = fields.get(name, re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower())
        if name == 'arcane_stone': name = 'stone_arcane'
    else:
        if name == 'stone' and meta in range(1,7): name = ['stone','granite','polished_granite','diorite','polished_diorite','andesite','polished_andesite'][meta]
        elif name == 'dirt' and meta in [1,2]: name = ['dirt','coarse_dirt','podzol'][meta]
        elif name == 'coal' and meta == 1: name = 'charcoal'
        elif name == 'dye' and meta in range(16): name = ['ink_sac','red_dye','green_dye','cocoa_beans','lapis_lazuli','purple_dye','cyan_dye','light_gray_dye','gray_dye','pink_dye','lime_dye','yellow_dye','light_blue_dye','magenta_dye','orange_dye','bone_meal'][meta]
        elif name == 'wool': name = 'white_wool'
        else: name = renames.get(name, name)
    return namespace+':'+name


def spec(raw):
    parts = raw.split(';', 3)
    item = parts[0];count = int(parts[1]) if len(parts)>1 else 1;meta = int(parts[2]) if len(parts)>2 else 0
    aspect = re.search(r"key:['\"]([^'\"]+)", parts[3]) if len(parts)>3 else None
    if ':' not in item: return {'id':'unavailable:'+item,'count':count,'legacy':raw}
    namespace, name = item.split(':',1)
    if namespace=='thaumcraft' and name=='curio' and meta in range(7):
        return {'id':'thaumcraft:curio','count':count,'aspect':['arcane','preserved','ancient','eldritch','knowledge','twisted','rites'][meta],'legacy':raw}
    item = modern_item(namespace,name,meta)
    unavailable = item not in modern_ids or (len(parts)>3 and aspect is None)
    return {'id': item if not unavailable else 'unavailable:'+item, 'count':count, **({'aspect':aspect[1]} if aspect else {}), 'legacy':raw}


entries = []
for path in sorted((OLD / 'resources/assets/thaumcraft/research').glob('*.json')):
    for entry in json.loads(path.read_text())['entries']:
        # Firebats are not ported yet: require the native blaze discovery alongside the bat.
        if entry['key']=='FOCUSSPELLBAT': entry['parents']=['!minecraft:blaze' if key=='!Firebat' else key for key in entry['parents']]
        for stage in entry['stages']:
            if 'required_knowledgeauram' in stage:
                stage['required_knowledge'] = stage.pop('required_knowledgeauram')
            for key in ['required_item','required_craft']:
                stage[key] = [spec(x) for x in stage.get(key, [])]
        entries.append(entry)
entries.sort(key=lambda e:e['key'])
save(OUT / 'knowledge/research.json', entries)
save(OUT / 'knowledge/theory_meta.json',{e['key']:(.5 if 'SPIKY' in e.get('meta',[]) else 0)+(.1 if 'HIDDEN' in e.get('meta',[]) else 0) for e in entries if any(m in e.get('meta',[]) for m in ['SPIKY','HIDDEN'])})
config = (OLD / 'java/thaumcraft/common/config/ConfigResearch.java').read_text()
formulae = {category:aspects(formula) for category, formula in re.findall(r'registerCategory\("(\w+)"[^\n]+?(new AspectList\(\).*?), new ResourceLocation', config)}

# Explicit OreDictionary families mapped to modern item selectors. No substring guessing.
ore = {'gunpowder':['minecraft:gunpowder'],'cropWheat':['minecraft:wheat'],'egg':['minecraft:egg'],'gemDiamond':['minecraft:diamond'], 'gemEmerald':['minecraft:emerald'], 'gemQuartz':['minecraft:quartz'],
       'dustRedstone':['minecraft:redstone'], 'dustGlowstone':['minecraft:glowstone_dust'],
       'ingotIron':['minecraft:iron_ingot'], 'ingotGold':['minecraft:gold_ingot'], 'ingotCopper':['minecraft:copper_ingot'],
       'stone':['minecraft:stone','minecraft:granite','minecraft:diorite','minecraft:andesite'],
       'stoneGranite':['minecraft:granite'], 'stoneDiorite':['minecraft:diorite'], 'stoneAndesite':['minecraft:andesite'],
       'cobblestone':['minecraft:cobblestone'], 'dirt':['minecraft:dirt','minecraft:coarse_dirt'],
       'sand':['#minecraft:sand'], 'grass':['minecraft:grass_block'], 'endstone':['minecraft:end_stone'],
       'gravel':['minecraft:gravel'], 'netherrack':['minecraft:netherrack'], 'obsidian':['minecraft:obsidian'],
       'blockGlass':['minecraft:glass'], 'logWood':['#minecraft:logs'], 'plankWood':['#minecraft:planks'],
       'stickWood':['minecraft:stick'], 'treeLeaves':['#minecraft:leaves'], 'treeSapling':['#minecraft:saplings'],
       'paper':['minecraft:paper'], 'string':['minecraft:string'], 'leather':['minecraft:leather'],
       'feather':['minecraft:feather'], 'bone':['minecraft:bone'], 'slimeball':['minecraft:slime_ball'],
       'blockWool':['#minecraft:wool'], 'foodMeatRaw':['minecraft:beef','minecraft:porkchop','minecraft:chicken','minecraft:mutton','minecraft:rabbit']}
for metal in ['Iron','Gold','Copper','Diamond','Emerald','Redstone','Lapis','Quartz','Coal']:
    stem={'Lapis':'lapis','Quartz':'nether_quartz'}.get(metal,metal.lower())
    ore['ore'+metal] = ['minecraft:'+stem+'_ore'] + ([] if metal=='Quartz' else ['minecraft:deepslate_'+stem+'_ore'])
for metal in ['Thaumium','Void','Brass','Quicksilver']:
    ore['ingot'+metal] = ['thaumcraft:'+('quicksilver' if metal=='Quicksilver' else 'ingot_'+metal.lower())]
    ore['nugget'+metal] = ['thaumcraft:nugget_'+metal.lower()]
    ore['plate'+metal] = ['thaumcraft:plate_'+metal.lower()]
    ore['block'+metal] = ['thaumcraft:metal_'+metal.lower()]
assignments = {}
source = (OLD / 'java/thaumcraft/common/config/ConfigAspects.java').read_text()
for target, formula in re.findall(r'registerObjectTag\((.*?), (new AspectList\(\)(?:\.add\(Aspect\.\w+, \d+\))+)[)]', source):
    value = aspects(formula);selectors=[]
    if target.startswith('"'): selectors=ore.get(target.strip('"'),[])
    else:
        m=re.fullmatch(r'new ItemStack\((Items|Blocks|ItemsTC|BlocksTC)\.(\w+)(?:, 1, (\d+))?\)', target)
        if m:
            owner,name,meta=m.groups();selector=modern_item('thaumcraft' if owner.endswith('TC') else 'minecraft',name if owner.endswith('TC') else name.lower(),int(meta or 0))
            if selector in modern_ids: selectors=[selector]
            if owner=="Items" and name=="COAL" and meta=="32767":selectors=["minecraft:coal","minecraft:charcoal"]
    for selector in selectors: assignments[selector]=value

entities = {}
entity_renames = {'MushroomCow':'mooshroom','SnowMan':'snow_golem','Ozelot':'ocelot','PigZombie':'zombified_piglin','LavaSlime':'magma_cube',
                  'WitherBoss':'wither','VillagerGolem':'iron_golem','EnderCrystal':'end_crystal','EvocationIllager':'evoker','VindicationIllager':'vindicator',
                  'IllusionIllager':'illusioner','XPOrb':'experience_orb','WitherSkeleton':'wither_skeleton'}
for name, formula in re.findall(r'registerEntityTag\("([^"]+)", (new AspectList\(\)(?:\.add\(Aspect\.\w+, \d+\))+)[)]',source):
    if name.startswith('Thaumcraft.'):
        if name!='Thaumcraft.FluxRift': continue
        key='thaumcraft:flux_rift'
    else: key='minecraft:'+entity_renames.get(name,re.sub(r'(?<!^)(?=[A-Z])','_',name).lower())
    entities[key]=aspects(formula)

triggers = {'items':{},'entities':{}}
for flag, targets in re.findall(r'new ScanBlock\("([^"]+)", ([^;]+)\)\);',config):
    for owner,name in re.findall(r'(BlocksTC|Blocks)\.(\w+)',targets):
        key=modern_item('thaumcraft' if owner.endswith('TC') else 'minecraft',name if owner.endswith('TC') else name.lower())
        triggers['items'].setdefault(key,[]).append(flag)
for flag, targets in re.findall(r'new ScanItem\("([^"]+)", ([^;]+)\)\);',config):
    m=re.search(r'new ItemStack\((ItemsTC|Items|Blocks)\.(\w+)',targets)
    if m:
        owner,name=m.groups();key=modern_item('thaumcraft' if owner.endswith('TC') else 'minecraft',name if owner.endswith('TC') else name.lower());triggers['items'].setdefault(key,[]).append(flag)
for flag, name in re.findall(r'new ScanEntity\("([^"]+)", Entity(\w+)\.class',config):
    if name=='FluxRift':key='thaumcraft:flux_rift'
    else:key='minecraft:'+re.sub(r'(?<!^)(?=[A-Z])','_',name).lower()
    triggers['entities'].setdefault(key,[]).append(flag)
save(OUT / 'knowledge/aspects.json', {'components':components,'formulae':formulae,'items':assignments,'entities':entities,'triggers':triggers})

old_lang={}
for line in (OLD / 'resources/assets/thaumcraft/lang/en_us.lang').read_text().splitlines():
    if '=' in line and not line.startswith('#'):
        k,v=line.split('=',1)
        if k.startswith(('research.','tc.aspect.','tc.knowledge.')):old_lang[k]=v
path=OUT/'lang/en_us.json';lang=json.loads(path.read_text());lang.update(old_lang);save(path,lang)
(OUT/'models/item/thaumometer.json').unlink(missing_ok=True)
save(OUT/'models/item/thaumonomicon.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/thaumonomicon'}})
save(OUT/'items/thaumonomicon.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/thaumonomicon'}})
vertices,uvs=[],[]
meshes={'body':[],'pane':[]};material='body'
for line in (OLD/'resources/assets/thaumcraft/models/item/scanner.obj').read_text().splitlines():
    fields_obj=line.split()
    if not fields_obj:continue
    if fields_obj[0]=='v':
        x,y,z=map(float,fields_obj[1:4]);vertices.append([x*.32+.5,-z*.32+.5,y*.32+.5])
    elif fields_obj[0]=='vt':
        u,v=map(float,fields_obj[1:3]);uvs.append([u,1-v])
    elif fields_obj[0]=='usemtl':material=fields_obj[1]
    elif fields_obj[0]=='f':
        face=[]
        for reference in fields_obj[1:]:
            v,t,*_=map(int,reference.split('/'));face.append(vertices[v-1]+uvs[t-1])
        for i in range(1,len(face)-1):meshes[material].append([face[0],face[i],face[i+1]])
for material,triangles in meshes.items():
    texture='scanner' if material=='body' else 'scanscreen'
    save(OUT/f'models/item/scanner_{material}.json',{'parent':'minecraft:block/block','loader':'thaumcraft:triangles','textures':{'texture':'thaumcraft:items/'+texture,'particle':'thaumcraft:items/scanner'},'triangles':triangles,
        'display':{'gui':{'rotation':[0,0,0],'scale':[1,1,1]},'firstperson_righthand':{'rotation':[0,-45,15],'translation':[1,3,1],'scale':[.7,.7,.7]},'firstperson_lefthand':{'rotation':[0,45,-15],'translation':[1,3,1],'scale':[.7,.7,.7]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.4,.4,.4]}}})
save(OUT/'items/thaumometer.json',{'model':{'type':'minecraft:composite','models':[{'type':'minecraft:model','model':'thaumcraft:item/scanner_'+part} for part in meshes]}})

print(len(entries),'research entries;',sum(len(e['stages']) for e in entries),'stages;',len(assignments),'aspect selectors;',len(entities),'entity types;',len(components),'aspects')

recipe_pages = {}
aliases = {'enchanted_fabric':'EnchantedFabric','vis_generator':'VisGenerator','workbench_charger':'workbenchcharger','recharge_pedestal':'rechargepedestal'}
for path in sorted((ROOT/'src/modern/resources/data/thaumcraft/recipe/arcane').glob('*.json')):
    definition=json.loads(path.read_text());recipe=definition['recipe'];lines=[]
    if 'pattern' in recipe:
        lines.extend('['+row+']' for row in recipe['pattern'])
        lines.extend(key+' = '+str(value) for key,value in recipe['key'].items())
    else:
        lines.extend(recipe.get('ingredients', ['One warded jar; preserves stored essentia.']))
    lines.append('Vis: '+str(definition['vis']))
    lines.extend(aspect+': '+str(count) for aspect,count in definition['crystals'].items())
    recipe_pages['thaumcraft:'+aliases.get(path.stem,path.stem)]=lines
recipe_pages['thaumcraft:salismundusfake']=['3 different aspect crystals + redstone + bowl + flint','Bowl and flint are returned.']
save(OUT/'knowledge/recipes.json',recipe_pages)
ui={
'en_us':{
'item.thaumcraft.thaumonomicon':'Thaumonomicon','item.thaumcraft.thaumometer':'Thaumometer',
'research.thaumcraft.advance':'Continue','research.thaumcraft.progress':'Research advanced: %s','research.thaumcraft.complete':'Research completed: %s',
'research.thaumcraft.scanned':'Discovered:','research.thaumcraft.nothing':'Nothing new to learn here.','research.thaumcraft.dream':'A strange dream reveals how to use Salis Mundus on a bookshelf.',
'research.thaumcraft.need_dream':'Collect a vis crystal and sleep, then use Salis Mundus on a bookshelf.','research.thaumcraft.need_book':'First obtain and open a Thaumonomicon.',
'research.thaumcraft.invalid':'Your knowledge data could not be read. It has been preserved; progress is disabled.',
'research.thaumcraft.parents':'Prerequisites:','research.thaumcraft.deliver':'Deliver:','research.thaumcraft.craft':'Craft:','research.thaumcraft.discover':'Discover:',
'research.thaumcraft.cost':'Knowledge:','research.thaumcraft.recipe':'Recipe:','research.thaumcraft.future_recipe':'This recipe needs a mechanic that is still pending in this port.',
'research.thaumcraft.stage':'Stage %s / %s','research.thaumcraft.scans':'Scans: %s','research.thaumcraft.aura':'Base %s  Vis %s  Flux %s',
'menu.thaumcraft.research_locked':'Research locked'},
'es_es':{
'item.thaumcraft.thaumonomicon':'Thaumonomicon','item.thaumcraft.thaumometer':'Taumómetro',
'research.thaumcraft.advance':'Continuar','research.thaumcraft.progress':'Investigación avanzada: %s','research.thaumcraft.complete':'Investigación completada: %s',
'research.thaumcraft.scanned':'Descubierto:','research.thaumcraft.nothing':'No hay nada nuevo que aprender aquí.','research.thaumcraft.dream':'Un sueño extraño revela cómo usar Salis Mundus en una librería.',
'research.thaumcraft.need_dream':'Consigue un cristal de vis y duerme; después aplica Salis Mundus a una librería.','research.thaumcraft.need_book':'Consigue y abre primero un Thaumonomicon.',
'research.thaumcraft.invalid':'No se pudo leer tu conocimiento. Se conserva el guardado y se desactiva el progreso.',
'research.thaumcraft.parents':'Requisitos:','research.thaumcraft.deliver':'Entregar:','research.thaumcraft.craft':'Fabricar:','research.thaumcraft.discover':'Descubrir:',
'research.thaumcraft.cost':'Conocimiento:','research.thaumcraft.recipe':'Receta:','research.thaumcraft.future_recipe':'Esta receta requiere una mecánica aún pendiente en el port.',
'research.thaumcraft.stage':'Etapa %s / %s','research.thaumcraft.scans':'Escaneos: %s','research.thaumcraft.aura':'Base %s  Vis %s  Flux %s',
'menu.thaumcraft.research_locked':'Investigación'} }
ui['en_us'].update({'research.thaumcraft.journal.1':'A strange dream\n\nGrind three crystals of different aspects with redstone using a bowl and flint. This creates Salis Mundus; the bowl and flint are returned.','research.thaumcraft.journal.2':'Use Salis Mundus on a bookshelf. Open the Thaumonomicon and start First Steps. Then transform a crafting table with Salis Mundus. Follow the book to craft a thaumometer and scan for knowledge.'})
ui['es_es'].update({'research.thaumcraft.journal.1':'Un sueño extraño\n\nMuele tres cristales de aspectos distintos con redstone, un cuenco y pedernal. Obtendrás Salis Mundus; se devuelven el cuenco y el pedernal.','research.thaumcraft.journal.2':'Aplica Salis Mundus a una librería. Abre el Thaumonomicon y comienza Primeros pasos. Transforma una mesa de crafteo con Salis Mundus. El libro explica el taumómetro y los escaneos.'})
ui['en_us'].update({'research.FIRSTSTEPS.stage.2': "Progress! I now have an Arcane Workbench at my disposal. It works much like a mundane crafting table and can even be used as such, however there are a few key differences.<BR>Firstly, many arcane recipes will require the use of those vis crystals I've been finding of late. They act as a focus for the magical energy I need to channel into the items I craft. The book calls this magical energy Vis.<BR>Supposedly this energy is all around me, but as yet I cannot perceive or measure it.<BR>It is time I craft the Thaumometer and use it to study the world around me. It should clearly mark those things I still need to examine.<BR>I have placed a bookmark to the Thaumometer recipe on the right.<BR>By holding the lens steady before my subject for a moment, I can discern its secrets. Crouching lets me turn my attention to an object held in my other hand instead.", 'research.FIRSTSTEPS.stage.3': 'Such strange wonders are being revealed to me. Peering through the lens of the Thaumometer shows me a world coursing with magic, Vis as the books calls it.<BR>Normally this energy is a pure ocean of magic that ebbs and flows, but it also permeates all things, both animate and inanimate. The book calls Vis in this state Essentia. Where Vis is formless and pure, Essentia is more rigid in its nature and falls into a multitude of clearly defined types, each mixing together with others to form greater and more complex types of essentia.<BR>I am not quite sure if the matter it is bound to gives Essentia its nature, or that the very nature of matter depends on the Essentia it contains. This bears further study.<BR>I am keeping a log of all the types of essentia I discover. A bookmark on the left will show my current knowledge.<BR>For now I need to focus my energies elsewhere. Studying essentia might be a good place to start.<BR>By holding the lens steady before my subject for a moment, I can discern its secrets. Crouching lets me turn my attention to an object held in my other hand instead.', 'research.thaumcraft.scan_other_empty': 'I need something to study in my other hand.'})
ui['es_es'].update({'research.FIRSTSTEPS.stage.2': '¡Progreso! Ya dispongo de una mesa de trabajo arcana. Funciona de forma parecida a una mesa de fabricación corriente, aunque tiene algunas diferencias.<BR>Muchas recetas arcanas requieren los cristales de vis que he ido encontrando. Estos concentran la energía mágica que debo canalizar hacia los objetos que fabrico. El libro llama Vis a esta energía.<BR>Al parecer, me rodea por todas partes, pero todavía no puedo percibirla ni medirla.<BR>Ha llegado el momento de fabricar el taumómetro y estudiar el mundo que me rodea.<BR>He marcado su receta a la derecha.<BR>Al mantener la lente frente a lo que deseo estudiar durante un instante, sus secretos empiezan a revelarse. Si me agacho, puedo concentrar el estudio en el objeto que sostengo en la otra mano.', 'research.FIRSTSTEPS.stage.3': 'Qué extrañas maravillas se revelan ante mí. A través de la lente del taumómetro veo un mundo recorrido por la magia que el libro llama Vis.<BR>Esta energía suele formar un océano puro que fluye y refluye, pero también impregna todas las cosas, animadas e inanimadas. El libro llama Essentia a la magia en este estado. Mientras que el Vis es puro y carece de forma, la Essentia adopta numerosos tipos definidos que se combinan para dar lugar a otros más complejos.<BR>No sé si la materia determina la naturaleza de la Essentia que contiene, o si ocurre al contrario. Merece un estudio más profundo.<BR>Llevo un registro de los tipos de Essentia que descubro. El marcador de la izquierda muestra mis conocimientos actuales.<BR>Por ahora debo dirigir mis esfuerzos hacia otros asuntos. Estudiar la Essentia parece un buen comienzo.<BR>Al mantener la lente frente a lo que deseo estudiar durante un instante, sus secretos empiezan a revelarse. Si me agacho, puedo concentrar el estudio en el objeto que sostengo en la otra mano.', 'research.thaumcraft.scan_other_empty': 'Necesito algo que estudiar en la otra mano.'})
ui['en_us'].update({'research.thaumcraft.unstudied': 'Unstudied', 'research.thaumcraft.studied': 'Studied'})
ui['es_es'].update({'research.thaumcraft.unstudied': 'Sin estudiar', 'research.thaumcraft.studied': 'Estudiado'})
for locale,note in {'en_us': 'A faint glimmer draws my attention to things I have yet to understand. Once studied, their aspects become recognisable through the lens, even when the object lies on the ground.', 'es_es': 'Un tenue resplandor atrae mi atención hacia lo que aún no comprendo. Una vez estudiado, puedo reconocer sus aspectos a través de la lente, incluso cuando el objeto descansa en el suelo.'}.items():
    for stage in [2,3]:ui[locale][f"research.FIRSTSTEPS.stage.{stage}"]+="<BR>"+note
for locale,values in ui.items():
    for category,label in zip(['BASICS','AUROMANCY','ALCHEMY','ARTIFICE','INFUSION','GOLEMANCY','ELDRITCH'],['Basics','Aura','Alchemy','Artifice','Infusion','Golems','Eldritch'] if locale=='en_us' else ['Básicos','Aura','Alquimia','Artificio','Infusión','Gólems','Oculto']):
        values['research.thaumcraft.category.'+category]=label
    path=OUT/f'lang/{locale}.json';lang=json.loads(path.read_text());lang.update(values)
    for key in ["scan_hint","scan_other_hint","scan_other_empty_hint"]:lang.pop("research.thaumcraft."+key,None)
    save(path,lang)

# Preserve the original marked creative variant when regenerating the normal book.
import subprocess,sys
subprocess.run([sys.executable,str(ROOT/"tools/generate_cheat_sheet.py")],check=True)
