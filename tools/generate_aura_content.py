"""Modern resources for the functional aura buffer and creative flux sponge."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
a=ROOT/'src/modern/resources/assets/thaumcraft'
d=ROOT/'src/modern/resources/data/thaumcraft'
def save(p,v):
 p.parent.mkdir(parents=True,exist_ok=True)
 p.write_text(json.dumps(v,indent=2,ensure_ascii=False)+'\n')
variants={}
for charge in range(11):
 stage=0 if charge==0 else 4 if charge==10 else (charge-1)//3+1
 variants[f'charge={charge}']={'model':f'thaumcraft:block/vis_battery_{stage}'}
for stage in range(5):
 save(a/f'models/block/vis_battery_{stage}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'thaumcraft:blocks/vis_battery_{stage}'}})
save(a/'blockstates/vis_battery.json',{'variants':variants})
save(a/'items/vis_battery.json',{'model':{'type':'minecraft:model','model':'thaumcraft:block/vis_battery_0'}})
save(a/'models/item/creative_flux_sponge.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/flux_sponge'}})
save(a/'items/creative_flux_sponge.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/creative_flux_sponge'}})
save(d/'loot_table/blocks/vis_battery.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:vis_battery'}]}]})
translations={
'en_us':{'block.thaumcraft.vis_battery':'Vis Battery','item.thaumcraft.creative_flux_sponge':'Creative Flux Sponge','item.thaumcraft.creative_flux_sponge.help':'Creative only: clears flux from loaded chunks in a 9×9 chunk area.','item.thaumcraft.creative_flux_sponge.drained':'%s flux drained from loaded chunks in the 9×9 area.','command.thaumcraft.aura.unloaded':'This chunk is not loaded.','command.thaumcraft.aura.read':'Chunk %s, %s — base: %s, vis: %s, flux: %s','command.thaumcraft.aura.add':'Added %s %s.'},
'es_es':{'block.thaumcraft.vis_battery':'Batería de vis','item.thaumcraft.creative_flux_sponge':'Esponja creativa de flux','item.thaumcraft.creative_flux_sponge.help':'Solo creativo: limpia flux de los chunks cargados en un área de 9×9 chunks.','item.thaumcraft.creative_flux_sponge.drained':'%s de flux eliminado de los chunks cargados en el área de 9×9.','command.thaumcraft.aura.unloaded':'Este chunk no está cargado.','command.thaumcraft.aura.read':'Chunk %s, %s — base: %s, vis: %s, flux: %s','command.thaumcraft.aura.add':'Añadido %s de %s.'}}
for locale,added in translations.items():
 p=a/f'lang/{locale}.json';strings=json.loads(p.read_text());strings.update(added);save(p,strings)
print('Aura battery, sponge and operator diagnostics resources generated.')
