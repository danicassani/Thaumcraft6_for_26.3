#!/usr/bin/env python3
"""Original transfuser meshes, orientations, costs and research recipe aliases."""
import json
from pathlib import Path
from loot_schema import for_resource
R=Path(__file__).resolve().parents[1];A=R/'src/modern/resources/assets/thaumcraft';D=R/'src/modern/resources/data/thaumcraft';O=R/'src/main/resources/assets/thaumcraft'
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(for_resource(p,v),ensure_ascii=False,indent=2)+'\n')
for name,filling in [('essentia_input',True),('essentia_output',False)]:
 m=json.loads((O/f'models/block/{name}.json').read_text());m.pop('__createdwith',None);save(A/f'models/block/{name}.json',m)
 old=json.loads((O/f'blockstates/{name}.json').read_text())['variants'];variants={k:{**{a:b for a,b in v.items() if a!='model'},'model':'thaumcraft:block/'+name} for k,v in old.items() if k!='inventory'};save(A/f'blockstates/{name}.json',{'variants':variants})
 save(A/f'models/item/{name}.json',{'parent':'thaumcraft:block/'+name,'gui_light':'front','display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.8,.8,.8]}}})
 save(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'thaumcraft:item/'+name}})
 save(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'thaumcraft:'+name}]}]})
 save(D/f'recipe/arcane/{name}.json',{'type':'thaumcraft:arcane','research':'ESSENTIATRANSPORT','vis':100,'crystals':{'aer':1,'aqua':1},'recipe':{'type':'minecraft:crafting_shaped','pattern':['   ','BQB','IGI'],'key':{'I':'thaumcraft:plate_iron','B':'thaumcraft:plate_brass','Q':'minecraft:dispenser' if filling else 'minecraft:hopper','G':'thaumcraft:metal_alchemical'},'result':{'id':'thaumcraft:'+name}}})
# The book generator keeps the exact legacy aliases for these two devices.
import subprocess,sys
subprocess.run([sys.executable,str(R/'tools/generate_book_completion.py')],check=True)
labels={
 'block.thaumcraft.essentia_input':('Filling Essentia Transfuser','Transfusor de llenado de esencia'),
 'block.thaumcraft.essentia_output':('Emptying Essentia Transfuser','Transfusor de vaciado de esencia'),
 'essentia.thaumcraft.transfuser.status':('Transferred: %s · Reserved: %s','Transferido: %s · Reservado: %s'),
}
for locale,index in [('en_us',0),('es_es',1)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text());v.update({k:pair[index] for k,pair in labels.items()})
 v['research.ESSENTIATRANSPORT.stage.2']=("I have learned to keep essentia stable during transport through the air.<BR>The Filling Essentia Transfuser draws one unit every five ticks from its rear connection and sends it to jars in front. It prefers jars already containing that aspect.<BR>The Emptying Essentia Transfuser draws from jars in front and feeds its rear connection. The receiver must request a specific aspect with positive suction; a labelled jar or a filtered tube provides this demand.<PAGE>Both devices scan sixteen blocks to either side and forward depths zero through fifteen. They ignore closed jars and respect aspect filters. They can use linked essentia mirrors without loading remote chunks.<BR>Place the nozzle pointing into the target area. Sneak-use a resonator or casting gauntlet to rotate it. Neither machine spends vis while operating; use valves to control the tube network.<BR>If a receiver changes during transfer, a single reserved unit waits inside. Picking up that device preserves the reserved unit, including across saves." if index==0 else "He aprendido a mantener estable la esencia durante su transporte por el aire.<BR>El transfusor de llenado toma una unidad cada cinco ticks de su conexión trasera y la envía a los tarros de delante. Prefiere los que ya contienen ese aspecto.<BR>El transfusor de vaciado extrae de los tarros de delante y alimenta su conexión trasera. El receptor debe solicitar un aspecto concreto con succión positiva: un tarro etiquetado o un tubo filtrado permiten hacerlo.<PAGE>Ambos exploran dieciséis bloques a los lados y profundidades de cero a quince hacia delante. Respetan las etiquetas y las abrazaderas. También pueden usar espejos de esencia enlazados sin cargar chunks remotos.<BR>Colocaré la boquilla apuntando a la zona de trabajo. Usarla agachado con un resonador o guantelete la rota. No consume vis al funcionar; puedo controlar los tubos mediante válvulas.<BR>Si el receptor cambia durante una transferencia, una unidad reservada espera dentro. Recoger el dispositivo conserva esa unidad, también al guardar.")
 save(p,v)
print('Two original transfusers: models, six directions, recipes and real book aliases')

subprocess.run([sys.executable,str(R/"tools/generate_research_visuals.py")],check=True)
