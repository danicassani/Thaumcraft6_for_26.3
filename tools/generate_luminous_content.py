from loot_schema import for_resource
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1];a=root/'src/modern/resources/assets/thaumcraft';d=root/'src/modern/resources/data/thaumcraft';legacy=root/'src/main/resources/assets/thaumcraft'
def save(p,o):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,o),indent=2,ensure_ascii=False)+'\n')
colors=[('white',16383998,'blanco'),('orange',16351261,'naranja'),('magenta',13061821,'magenta'),('lightblue',3847130,'azul claro'),('yellow',16701501,'amarillo'),('lime',8439583,'lima'),('pink',15961002,'rosa'),('gray',4673362,'gris'),('silver',10329495,'gris claro'),('cyan',1481884,'cian'),('purple',8991416,'morado'),('blue',3949738,'azul'),('brown',8606770,'marrón'),('green',6192150,'verde'),('red',11546150,'rojo'),('black',1908001,'negro')]
model=json.loads((legacy/'models/block/candle.json').read_text());save(a/'models/block/tallow_candle.json',model)
save(a/'models/block/nitor.json',{'elements':[],'textures':{'particle':'thaumcraft:blocks/nitor'}})
save(a/'models/item/nitor.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:blocks/nitor'}})
langs={lang:json.loads((a/f'lang/{lang}.json').read_text()) for lang in ['en_us','es_es']}
for family in ['candle','nitor']:
 ids=['thaumcraft:'+family+'_'+name for name,_,_ in colors]
 save(d/f'tags/item/{family}.json',{'replace':False,'values':ids})
 if family=='candle':save(d/'tags/block/infusion_stabilizers/candles.json',{'replace':False,'values':ids})
 else:save(d/'tags/block/crucible_heat_sources.json',{'replace':False,'values':ids})
 for name,rgb,es in colors:
  id=family+'_'+name;model='block/tallow_candle' if family=='candle' else 'item/nitor';value=rgb-16777216
  save(a/f'blockstates/{id}.json',{'variants':{'':{'model':'thaumcraft:block/'+('tallow_candle' if family=='candle' else 'nitor')}}})
  save(a/f'items/{id}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:'+model,'tints':[{'type':'minecraft:constant','value':value}]}})
  save(d/f'loot_table/blocks/{id}.json',{'type':'minecraft:block','pools':[{'rolls':1,'condition':{'type':'minecraft:survives_explosion'},'entries':[{'type':'minecraft:item','name':'thaumcraft:'+id}]}]})
  dye={'silver':'light_gray','lightblue':'light_blue'}.get(name,name)
  save(d/f'recipe/{family}_dye_{name}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['#thaumcraft:'+family,'minecraft:'+dye+'_dye'],'result':{'id':'thaumcraft:'+id}})
  langs['en_us']['block.thaumcraft.'+id]=(name.replace('lightblue','light blue').replace('silver','light gray').title()+' '+('Tallow Candle' if family=='candle' else 'Nitor'))
  langs['es_es']['block.thaumcraft.'+id]=('Vela de sebo '+es if family=='candle' else 'Nitor '+es)
# Keep the original three candles from a string and two tallow; nitor still requires alchemy.
save(d/'recipe/tallow_candles.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':[' S ',' T ',' T '],'key':{'S':'minecraft:string','T':'thaumcraft:tallow'},'result':{'id':'thaumcraft:candle_white','count':3}})
for lang,o in langs.items():save(a/f'lang/{lang}.json',o)
print('32 colored light blocks, recoloring and candle recipe generated.')
