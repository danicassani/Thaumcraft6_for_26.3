#!/usr/bin/env python3
"""Native marked Thaumonomicon variant with the original creative-only icon."""
import json
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
save(A/'items/thaumonomicon.json',{'model':{'type':'minecraft:condition','property':'minecraft:has_component','component':'thaumcraft:cheat_sheet','on_true':{'type':'minecraft:model','model':'thaumcraft:item/thaumonomicon_cheat'},'on_false':{'type':'minecraft:model','model':'thaumcraft:item/thaumonomicon'}}})
save(A/'models/item/thaumonomicon_cheat.json',{'parent':'minecraft:item/generated','textures':{'layer0':'thaumcraft:items/thaumonomicon_cheat'}})
labels={'item.thaumcraft.thaumonomicon_cheat':("Cheater’s Thaumonomicon","Thaumonomicon de trucos"),'research.thaumcraft.cheat_hint':('Use to unlock all research.','Úsalo para desbloquear todas las investigaciones.'),'research.thaumcraft.cheat_disabled':('Research unlocking is disabled by the server.','El servidor ha desactivado el desbloqueo de investigaciones.'),'research.thaumcraft.cheat_granted':('All research unlocked.','Todas las investigaciones desbloqueadas.'),'research.thaumcraft.cheat_failed':('Research could not be unlocked; saved data was preserved.','No se pudo desbloquear la investigación; se han conservado los datos guardados.')}
for locale,i in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v.update({k:value[i] for k,value in labels.items()});save(p,v)
print('Cheat-sheet component condition, original icon and English/Spanish instructions')
