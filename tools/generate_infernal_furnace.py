#!/usr/bin/env python3
"""Original infernal multiblock model, catalog and bounded smelting bonus definitions. Run last."""
import json
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
m=json.loads((R/'src/main/resources/assets/thaumcraft/models/block/infernal_furnace.json').read_text());m.pop('__createdwith',None);m['render_type']='minecraft:cutout';m['textures'].update(core='minecraft:block/lava_still',fire='minecraft:block/fire_0',particle='thaumcraft:blocks/infernal_furnace');m['display']={'gui':{'rotation':[30,225,0],'scale':[.275,.275,.275]}};save(A/'models/block/infernal_furnace.json',m)
save(A/'blockstates/infernal_furnace.json',{'variants':{f'facing={d}':{'model':'thaumcraft:block/infernal_furnace','y':y} for d,y in [('north',180),('east',270),('south',0),('west',90)]}})
save(A/'items/infernal_furnace.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/infernal_furnace'}})
for name,texture,drop in [('infernal_nether_bricks','nether_bricks','nether_bricks'),('infernal_obsidian','obsidian','obsidian')]:
 save(A/f'blockstates/{name}.json',{'variants':{'':{'model':'minecraft:block/'+texture}}})
 save(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:'+drop}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
save(D/'loot_table/blocks/infernal_furnace.json',{'type':'minecraft:block','pools':[]})
def append_tag(path,values):
 v=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
 for value in values:
  if value not in v['values']:v['values'].append(value)
 save(path,v)
append_tag(R/'src/modern/resources/data/minecraft/tags/block/mineable/pickaxe.json',['thaumcraft:infernal_nether_bricks','thaumcraft:infernal_obsidian','thaumcraft:infernal_furnace'])
append_tag(R/'src/modern/resources/data/minecraft/tags/block/needs_diamond_tool.json',['thaumcraft:infernal_obsidian'])
bonuses=[]
def bonus(inputs,output,chance=.33):bonuses.append({'inputs':inputs,'output':output,'chance':chance})
for metal in ['iron','gold','copper','tin','silver','lead','cinnabar','quartz']:
 ids=['thaumcraft:cluster_'+metal]
 if metal in ['iron','gold','copper']:ids+=['#minecraft:'+metal+'_ores','minecraft:raw_'+metal]
 elif metal=='quartz':ids+=['minecraft:nether_quartz_ore']
 else:ids+=['#c:ores/'+metal,'thaumcraft:ore_'+metal]
 out={'iron':'minecraft:iron_nugget','gold':'minecraft:gold_nugget','cinnabar':'thaumcraft:nugget_quicksilver'}.get(metal,'thaumcraft:nugget_'+metal)
 bonus(ids,out)
 chance={'gold':.02,'silver':.02,'cinnabar':.025}.get(metal,.01)
 bonus([i for i in ids if not i.startswith('thaumcraft:cluster_')],'thaumcraft:nugget_rareearth',chance)
bonus(['thaumcraft:cluster_'+m for m in ['iron','gold','copper','tin','silver','lead','cinnabar','quartz']],'thaumcraft:nugget_rareearth',.02)
for mineral in ['diamond','emerald','redstone','lapis']:bonus(['#minecraft:'+mineral+'_ores'],'thaumcraft:nugget_rareearth',.025 if mineral in ['diamond','emerald'] else .01)
for vanilla,chunk in [('beef','beef'),('chicken','chicken'),('porkchop','pork'),('rabbit','rabbit'),('mutton','mutton')]:bonus(['minecraft:'+vanilla],'thaumcraft:chunk_'+chunk)
bonus(['minecraft:cod','minecraft:salmon'],'thaumcraft:chunk_fish')
save(A/'knowledge/infernal_bonuses.json',bonuses)
p=A/'knowledge/recipes.json';v=json.loads(p.read_text());v['thaumcraft:infernalfurnace']=['3 x 3 x 3 multiblock; Salis Mundus; INFERNALFURNACE','12 minecraft:nether_bricks; 12 minecraft:obsidian; iron bars; lava source','Corners: nether bricks on all three layers. Remaining frame: obsidian.','Bottom center: obsidian. Middle center: lava. Top center: open.','Front middle: iron bars. Apply Salis to any frame block or bars.','No fuel. Input through open top or top item capability. Output through front.'];save(p,v)
for loc,es in [('en_us',False),('es_es',True)]:
 p=A/f'lang/{loc}.json';v=json.loads(p.read_text());v['block.thaumcraft.infernal_furnace']='Horno infernal' if es else 'Infernal Furnace';v['block.thaumcraft.infernal_nether_bricks']='Ladrillos del horno infernal' if es else 'Infernal Furnace Bricks';v['block.thaumcraft.infernal_obsidian']='Obsidiana del horno infernal' if es else 'Infernal Furnace Obsidian';v['infernal.thaumcraft.status']='Entradas %s · Cocción %s/%s · Carga %s · Fuelles %s · Salidas %s' if es else 'Inputs %s · Cooking %s/%s · Credits %s · Bellows %s · Outputs %s'
 guide=(' En este port: forma una estructura 3×3×3 con 12 ladrillos del Nether y 12 obsidianas, lava central, abertura superior y barrotes en el centro frontal. Actívala con Salis Mundus tras investigar. Cocina en 140 ticks sin aura o 80 con carga; hasta cuatro fuelles a dos bloques del centro, orientados hacia él y sin redstone, aceleran y añaden tiradas de bonificación. Cada vis paga una operación acelerada, cargando hasta 20 al trabajar. Admite entrada superior automatizada de 32 pilas; los objetos arrojados no cocinables se destruyen. Una salida bloqueada conserva productos y experiencia ya ganados, sin volver a sortearlos. Romper el marco restaura materiales y devuelve inventarios; no suelta un horno transportable.' if es else ' In this port: build a 3×3×3 structure with 12 nether bricks and 12 obsidian, central lava, an open top and front-center iron bars. Activate with Salis Mundus after research. Cooking takes 140 ticks without aura or 80 with credits; up to four unpowered bellows two blocks from the center, facing inward, accelerate it and add bonus rolls. Each vis pays for one boosted operation, charging up to 20 when work starts. Top automation accepts 32 input stacks; unsmeltable thrown items are destroyed. A blocked output preserves earned items and XP without rerolling. Breaking the frame restores its materials and returns inventories; it never drops a portable furnace.')
 k='research.INFERNALFURNACE.stage.2'
 if guide not in v.get(k,''):v[k]=v.get(k,'')+guide
 save(p,v)
print('Infernal furnace: original multiblock model, 27 bonus rules, frame drops and bilingual guide.')
