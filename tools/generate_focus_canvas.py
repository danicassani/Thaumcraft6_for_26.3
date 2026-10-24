#!/usr/bin/env python3
from pathlib import Path
import json
A=Path(__file__).resolve().parents[1]/'src/modern/resources/assets/thaumcraft/lang'
translations={
'canvas_issue_1':('Insert a blank focus','Introduce un foco'),
'canvas_issue_2':('Focus capacity exceeded','Capacidad insuficiente'),
'canvas_issue_3':('Research incomplete','Investigación incompleta'),
'canvas_issue_4':('Not enough XP levels','Faltan niveles de XP'),
'canvas_issue_5':('Missing vis crystals','Faltan cristales de vis'),
'canvas_issue_6':('Crafting in progress','Fabricación en curso'),
'canvas_title':('Focus graph','Árbol del foco'),
'canvas_center':('Center graph','Centrar árbol'),
'canvas_help':('Select a node to edit it. Drag the background to pan; scroll to zoom.','Selecciona un nodo para editarlo. Arrastra el fondo para desplazarte; usa la rueda para ampliar.'),
'canvas_impact':('Same impact','Mismo impacto'),
'canvas_power':('Power %s','Pot. %s'),
'canvas_depth':('Depth %s','Prof. %s'),
'canvas_duration':('Time %s','Dur. %s'),
'canvas_levels':('%s XP levels','%s niveles XP'),
}
for locale,index in [('en_us',0),('es_es',1)]:
 p=A/(locale+'.json');v=json.loads(p.read_text());v.update({'focal.thaumcraft.'+k:labels[index] for k,labels in translations.items()});p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
