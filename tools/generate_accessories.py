#!/usr/bin/env python3
"""Final equipment layer, run after warp: typed accessories, masks and cult gear."""
import json,shutil
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
items=['crimson_'+f+'_'+s for f in ['plate','robe','praetor'] for s in ['helm','chest','legs']]+['crimson_boots','crimson_blade']
for n in items:
 save(A/f'models/item/{n}.json',json.loads((O/f'models/item/{n}.json').read_text()));save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+n}})
for name,texture in [('crimson_plate','cultist_plate_armor'),('crimson_robe','cultist_robe_armor'),('crimson_praetor','cultist_leader_armor'),('crimson_boots','cultistboots')]:
 save(A/f'equipment/{name}.json',{'layers':{'humanoid':[{'texture':'thaumcraft:'+name}],'humanoid_leggings':[{'texture':'thaumcraft:'+name}]}})
 for layer in ['humanoid','humanoid_leggings']:
  p=A/f'textures/entity/equipment/{layer}/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(O/f'textures/entity/armor/{texture}.png',p)
save(D/'tags/item/repairs_iron.json',{'replace':False,'values':['minecraft:iron_ingot']})
def ing(n):return {'item':n if ':' in n else 'thaumcraft:'+n}
recipes=[('fortress_goggles','HelmGoggles',['minecraft:slime_ball','goggles'],{'sensus':40,'auram':20,'praemunio':20},5),('mask_0','MaskGrinningDevil',['minecraft:ink_sac','plate_iron','minecraft:leather','shimmerleaf','brain','plate_iron'],{'cognitio':80,'victus':80,'praemunio':20},8),('mask_1','MaskAngryGhost',['minecraft:bone_meal','plate_iron','minecraft:leather','minecraft:poisonous_potato','minecraft:wither_skeleton_skull','plate_iron'],{'perditio':80,'mortuus':80,'praemunio':20},8),('mask_2','MaskSippingFiend',['minecraft:red_dye','plate_iron','minecraft:leather','minecraft:ghast_tear','minecraft:milk_bucket','plate_iron'],{'exanimis':80,'victus':80,'praemunio':20},8)]
p=A/'knowledge/recipes.json';pages=json.loads(p.read_text())
for key,alias,parts,aspects,inst in recipes:
 save(D/f'recipe/infusion/{key}.json',{'type':'thaumcraft:infusion','research':'FORTRESSMASK','input':ing('fortress_helm'),'components':list(map(ing,parts)),'aspects':aspects,'instability':inst,'result':{'id':'thaumcraft:fortress_helm'},'infusion_enchantment':key});pages['thaumcraft:'+alias]=['Center: fortress helmet (retains components and damage)','Offerings: '+', '.join(parts),'Essentia: '+str(aspects)]
save(p,pages)
for locale,idx in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());labels={'equipment.thaumcraft.accessories':('Accessories','Accesorios'),'key.thaumcraft.accessories':('Open accessories','Abrir accesorios'),'equipment.thaumcraft.accessory':('Right click: equip. Sneak use or B: seven slots.','Clic derecho: equipar. Uso agachado o B: siete ranuras.'),'equipment.thaumcraft.slot_help':('Hover for slot types','Tipos al pasar el cursor'),'equipment.thaumcraft.invalid_accessories':('Accessory data is unreadable; preserved without changes.','No se pueden leer los accesorios; sus datos se han conservado.'),'equipment.thaumcraft.mask_0':('Mask of the Grinning Devil','Máscara del diablo sonriente'),'equipment.thaumcraft.mask_1':('Mask of the Angry Ghost','Máscara del fantasma furioso'),'equipment.thaumcraft.mask_2':('Mask of the Sipping Fiend','Máscara del demonio bebedor')}
 for i,key in enumerate(['amulet','ring','belt','head','body','charm']):labels['equipment.thaumcraft.slot_'+key]=(['Amulet','Ring','Belt','Head','Body','Charm'][i],['Amuleto','Anillo','Cinturón','Cabeza','Cuerpo','Talismán'][i])
 for n in items:labels['item.thaumcraft.'+n]=(n.replace('_',' ').title(),n.replace('_',' ').title())
 v.update({k:t[idx] for k,t in labels.items()});save(p,v)
p=R/'src/modern/resources/data/minecraft/tags/item/swords.json';v=json.loads(p.read_text());v['values']=list(dict.fromkeys(v['values']+['thaumcraft:crimson_blade']));save(p,v)
print('Accessories: seven slots; four preserving fortress infusions; eleven cult equipment items with original textures')

jewels=['amulet_mundane','ring_mundane','girdle_mundane','ring_apprentice','amulet_fancy','ring_fancy','girdle_fancy']
for n in jewels:
 save(A/f'models/item/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/'+n}});save(A/f'items/{n}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+n}})
for n,pattern,key in [('amulet_mundane',[' S ','S S',' I '],{'S':'minecraft:string','I':'thaumcraft:ingot_brass'}),('ring_mundane',['NNN','N N','NNN'],{'N':'thaumcraft:nugget_brass'}),('girdle_mundane',[' L ','L L',' I '],{'L':'minecraft:leather','I':'thaumcraft:ingot_brass'}),('amulet_fancy',[' S ','SGS',' I '],{'S':'minecraft:string','G':'minecraft:diamond','I':'minecraft:gold_ingot'}),('ring_fancy',['NGN','N N','NNN'],{'N':'minecraft:gold_nugget','G':'minecraft:diamond'}),('girdle_fancy',[' L ','LGL',' I '],{'L':'minecraft:leather','G':'minecraft:diamond','I':'minecraft:gold_ingot'})]:save(D/f'recipe/{n}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'thaumcraft:'+n}})
# Restore proper jewelry centers while preserving the previous port's gold center for old worlds/datapacks.
for n,center in {'amulet_vis':'amulet_mundane','ring_cloud':'ring_mundane','charm_verdant':'amulet_fancy','charm_voidseer':'amulet_fancy','band_curiosity':'girdle_fancy'}.items():
 p=D/f'recipe/infusion/{n}.json';v=json.loads(p.read_text());v['input']={'item':['thaumcraft:'+center,'minecraft:gold_ingot']};save(p,v)
for locale in ['en_us','es_es']:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text())
 for n in jewels:v['item.thaumcraft.'+n]=n.replace('_',' ').title()
 save(p,v)
print('Jewelry: seven variants, six original mundane/fancy recipes, original centers with legacy compatibility')

p=A/'knowledge/recipes.json';v=json.loads(p.read_text())
for recipe,alias in {'amulet_vis':'VisAmulet','ring_cloud':'CLOUDRING','charm_verdant':'VerdantHeart','charm_voidseer':'VoidseerPearl','band_curiosity':'CuriosityBand'}.items():
 data=json.loads((D/f'recipe/infusion/{recipe}.json').read_text());v['thaumcraft:'+alias]=['Center: '+str(data['input']['item']),'Offerings: '+', '.join(str(c['item']) for c in data['components']),'Essentia: '+str(data['aspects'])]
save(p,v)
p=A/'lang/es_es.json';v=json.loads(p.read_text());v.update({'item.thaumcraft.'+n:label for n,label in {'amulet_mundane':'Amuleto simple','amulet_fancy':'Amuleto refinado','ring_mundane':'Anillo simple','ring_fancy':'Anillo refinado','ring_apprentice':'Anillo del aprendiz','girdle_mundane':'Faja simple','girdle_fancy':'Faja refinada','crimson_blade':'Espada carmesí','crimson_boots':'Botas carmesí'}.items()})
for family,label in [('plate','de placas carmesí'),('robe','de túnica carmesí'),('praetor','del pretor carmesí')]:
 for suffix,part in [('helm','Yelmo'),('chest','Peto'),('legs','Grebas')]:v['item.thaumcraft.crimson_'+family+'_'+suffix]=part+' '+label
save(p,v)

p=D/'recipe/arcane/focus_pouch.json';v=json.loads(p.read_text());v['recipe']['key']['B']=['thaumcraft:girdle_mundane','minecraft:string'];save(p,v)

p=A/'knowledge/aspects.json';v=json.loads(p.read_text());v['items']['thaumcraft:ring_apprentice']={'auram':5,'metallum':5,'praecantatio':20};save(p,v)
