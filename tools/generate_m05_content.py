"""Port original crystal meshes and device resources, with 26.3 vanilla prototypes."""
from pathlib import Path
import json, re, random, zipfile
ROOT=Path(__file__).resolve().parents[1]
a=ROOT/'src/modern/resources/assets/thaumcraft';d=ROOT/'src/modern/resources/data';old=ROOT/'src/main/resources/assets/thaumcraft'
mc=zipfile.ZipFile('/workspace/.gradle/caches/minecraftforge/forgegradle/mavenizer/caches/minecraft_tasks/26.3/client.jar')
def save(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n')
def argb(rgb):return (rgb|0xff000000)-(1<<32)
source=re.sub(r'//[^\n]*','',(ROOT/'src/main/java/thaumcraft/api/aspects/Aspect.java').read_text())
aspects={name:int(color,16) for name,color in re.findall(r'new Aspect\("([a-z]+)",\s*(0x[0-9a-fA-F]+)',source)}
save(ROOT/'tools/crystal_aspects.json',aspects)
# Preserve the original declaration order in code and creative variants.
java=['package thaumcraft.crystals;','','import java.util.Map;','','/** Original essence identifiers and colors; research/combinations are ported separately. */','public final class CrystalAspects {','    private CrystalAspects() {}','    public static final Map<String, Integer> COLORS;','    static {','        var colors = new java.util.LinkedHashMap<String, Integer>();']
java+=['        colors.put("'+name+'", '+hex(color)+');' for name,color in aspects.items()]
java+=['        COLORS = java.util.Collections.unmodifiableMap(colors);','    }','}']
(ROOT/'src/modern/java/thaumcraft/crystals/CrystalAspects.java').write_text('\n'.join(java)+'\n')
# OBJ has eight individual shards; keep vertices, UVs and all faces, triangulating fans.
vertices=[];uvs=[];parts=[];faces=[]
for line in (old/'models/obj/crystal.obj').read_text().splitlines():
 fields=line.split()
 if not fields:continue
 if fields[0] in ['o','g']:
  if faces:parts.append(faces);faces=[]
 elif fields[0]=='v':vertices.append(list(map(float,fields[1:4])))
 elif fields[0]=='vt':uvs.append(list(map(float,fields[1:3])))
 elif fields[0]=='f':
  face=[]
  for value in fields[1:]:
   v,t,*_=map(int,value.split('/'));face.append(vertices[v-1]+uvs[t-1])
  for i in range(1,len(face)-1):faces.append([face[0],face[i],face[i+1]])
if faces:parts.append(faces)
assert len(parts)==8
for size in range(4):
 for variant in range(8):
  order=list(range(8));random.Random(variant).shuffle(order)
  triangles=[triangle for p in order[:size+1] for triangle in parts[p]]
  save(a/f'models/block/crystal_s{size}_v{variant}.json',{'parent':'minecraft:block/block','loader':'thaumcraft:triangles','tint_index':0,'textures':{'texture':'thaumcraft:blocks/crystal','particle':'thaumcraft:blocks/crystal'},'triangles':triangles})
rotations={'down':{},'up':{'x':180},'north':{'x':270},'south':{'x':90},'west':{'x':90,'y':90},'east':{'x':90,'y':270}}
for aspect in ['aer','ignis','aqua','terra','ordo','perditio','vitium']:
 name='crystal_'+aspect;multipart=[]
 for direction,rotation in rotations.items():
  for size in range(4):
   multipart.append({'when':{direction:'true','size':str(size)},'apply':[{'model':f'thaumcraft:block/crystal_s{size}_v{v}',**rotation} for v in range(8)]})
 save(a/f'blockstates/{name}.json',{'multipart':multipart})
 save(a/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/crystal_planter'}})
 save(a/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'thaumcraft:item/{name}','tints':[{'type':'minecraft:constant','value':argb(aspects[aspect])}]}})
save(a/'models/item/crystal_essence.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/crystal_essence'}})
def crystal_model(color):return {'type':'minecraft:model','model':'thaumcraft:item/crystal_essence','tints':[{'type':'minecraft:constant','value':argb(color)}]}
save(a/'items/crystal_essence.json',{'model':{'type':'minecraft:select','property':'minecraft:component','component':'thaumcraft:crystal_aspect','cases':[{'when':aspect,'model':crystal_model(color)} for aspect,color in aspects.items()],'fallback':crystal_model(aspects['aer'])}})
# Original cuboid device meshes are already representable by vanilla 26.3 JSON.
for name in ['vis_generator','dioptra','recharge_pedestal']:
 model=json.loads((old/f'models/block/{name}.json').read_text());model.pop('__createdwith',None);model.setdefault('parent','minecraft:block/block');save(a/f'models/block/{name}.json',model)
 save(a/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'thaumcraft:block/{name}'}})
 save(d/f'thaumcraft/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+name}]}]})
legacy=json.loads((old/'blockstates/vis_generator.json').read_text())['variants'];variants={}
for powered in [False,True]:
 for facing in ['down','up','north','south','east','west']:
  v=legacy[f'enabled={str(not powered).lower()},facing={facing}'].copy();v['model']='thaumcraft:block/vis_generator';variants[f'facing={facing},powered={str(powered).lower()}']=v
save(a/'blockstates/vis_generator.json',{'variants':variants})
save(a/'blockstates/recharge_pedestal.json',{'variants':{'':{'model':'thaumcraft:block/recharge_pedestal'}}})
off=json.loads((a/'models/block/dioptra.json').read_text());off['textures'].update({'side':'thaumcraft:blocks/dioptra_side_off','particle':'thaumcraft:blocks/dioptra_side_off'});save(a/'models/block/dioptra_off.json',off)
save(a/'blockstates/dioptra.json',{'variants':{'powered=false':{'model':'thaumcraft:block/dioptra'},'powered=true':{'model':'thaumcraft:block/dioptra_off'}}})
# Ambient grass keeps vanilla snow/lifecycle/loot and a biome tint.
save(a/'blockstates/grass_ambient.json',json.loads(mc.read('assets/minecraft/blockstates/grass_block.json')))
save(a/'items/grass_ambient.json',json.loads(mc.read('assets/minecraft/items/grass_block.json')))
loot=json.loads(mc.read('data/minecraft/loot_table/blocks/grass_block.json'))
loot=json.loads(json.dumps(loot).replace('"minecraft:grass_block"','"thaumcraft:grass_ambient"'))
save(d/'thaumcraft/loot_table/blocks/grass_ambient.json',loot)
for typ in ['block','item']:save(d/f'minecraft/tags/{typ}/dirt.json',{'replace':False,'values':['thaumcraft:grass_ambient']})
# New chunks only, gated by generateCrystals and oreDensity through the existing placement.
save(d/'thaumcraft/worldgen/feature/crystals.json',{'type':'thaumcraft:crystals'})
for dimension,count,biome in [('overworld',8,'#minecraft:is_overworld'),('nether',1,'#minecraft:is_nether')]:
 name='crystals_'+dimension
 save(d/f'thaumcraft/worldgen/placed_feature/{name}.json',{'feature':'thaumcraft:crystals','placement':[{'type':'thaumcraft:ore_count','ore':'crystals','count':count},{'type':'minecraft:in_square'},{'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':-48 if dimension=='overworld' else 5},'max_inclusive':{'absolute':96 if dimension=='overworld' else 120}}},{'type':'minecraft:biome'}]})
 save(d/f'thaumcraft/forge/biome_modifier/{name}.json',{'type':'forge:add_features','biomes':biome,'features':['thaumcraft:'+name],'step':'underground_decoration'})
# Add pickaxe mining for machines, keeping all previous entries.
p=d/'minecraft/tags/block/mineable/pickaxe.json';tag=json.loads(p.read_text());tag['values']=list(dict.fromkeys(tag['values']+['thaumcraft:dioptra','thaumcraft:recharge_pedestal']));save(p,tag)
translations={
'en_us':{'block.thaumcraft.vis_generator':'Vis Generator','block.thaumcraft.dioptra':'Dioptra','block.thaumcraft.recharge_pedestal':'Recharge Pedestal','block.thaumcraft.grass_ambient':'Ambient Grass','item.thaumcraft.crystal_essence':'Vis Crystal','device.thaumcraft.enabled':'enabled','device.thaumcraft.disabled':'disabled','block.thaumcraft.vis_generator.read':'%s / 1000 FE — %s','block.thaumcraft.dioptra.read':'13×13 chunks (%s). Local base: %s, vis: %s, flux: %s'},
'es_es':{'block.thaumcraft.vis_generator':'Generador de vis','block.thaumcraft.dioptra':'Dioptra','block.thaumcraft.recharge_pedestal':'Pedestal de recarga','block.thaumcraft.grass_ambient':'Césped ambiental','item.thaumcraft.crystal_essence':'Cristal de vis','device.thaumcraft.enabled':'activo','device.thaumcraft.disabled':'desactivado','block.thaumcraft.vis_generator.read':'%s / 1000 FE — %s','block.thaumcraft.dioptra.read':'13×13 chunks (%s). Base local: %s, vis: %s, flux: %s'}}
for locale,extra in translations.items():
 legacy_lang={}
 for line in (old/'lang/en_us.lang').read_text().splitlines():
  if '=' in line and not line.startswith('#'):
   k,v=line.split('=',1);legacy_lang[k]=v
 for aspect in aspects:
  label=legacy_lang.get('tc.aspect.'+aspect,aspect)
  if locale=='es_es':label={'aer':'Aire','terra':'Tierra','ignis':'Fuego','aqua':'Agua','ordo':'Orden','perditio':'Entropía','vacuos':'Vacío','lux':'Luz','motus':'Movimiento','gelum':'Frío','vitreus':'Cristal','metallum':'Metal','victus':'Vida','mortuus':'Muerte','potentia':'Energía','permutatio':'Intercambio','auram':'Aura','alkimia':'Alquimia','vitium':'Corrupción','tenebrae':'Oscuridad','alienis':'Extrañeza','volatus':'Vuelo','herba':'Planta','instrumentum':'Herramienta','fabrico':'Fabricación','machina':'Mecanismo','vinculum':'Prisión','spiritus':'Espíritu','cognitio':'Mente','sensus':'Sentidos','aversio':'Aversión','praemunio':'Protección','desiderium':'Deseo','exanimis':'No muerto','bestia':'Bestia','humanus':'Humano','praecantatio':'Magia'}[aspect]
  extra['item.thaumcraft.crystal_essence.'+aspect]=('Cristal de vis de '+label.lower() if locale=='es_es' else legacy_lang.get('item.crystal_essence.name','%s Vis Crystal').replace('%s',label))
  if aspect in ['aer','ignis','aqua','terra','ordo','perditio','vitium']:extra['block.thaumcraft.crystal_'+aspect]=(label+' Crystal Cluster' if locale=='en_us' else 'Cristales de '+label.lower())
 p=a/f'lang/{locale}.json';strings=json.loads(p.read_text());strings.update(extra);save(p,strings)
print('7 crystal blocks, 37 essence aspects, 32 original-mesh growth variants, ambient grass and 3 devices generated.')
