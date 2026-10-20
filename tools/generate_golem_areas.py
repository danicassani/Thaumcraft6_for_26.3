#!/usr/bin/env python3
"""Independent oriented seal dimensions; run after golem_ranks."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'src/modern/resources/assets/thaumcraft/lang'
labels={'area_mode':['Area','Zona'],'area_oriented':['Face oriented','Según cara'],'area_legacy':['Existing radius','Radio anterior'],'area_x_minus':['X −','X −'],'area_x_plus':['X +','X +'],'area_y_minus':['Y −','Y −'],'area_y_plus':['Y +','Y +'],'area_z_minus':['Z −','Z −'],'area_z_plus':['Z +','Z +']}
for i,locale in enumerate(['en_us','es_es']):
 p=root/(locale+'.json');v=json.loads(p.read_text())
 for key,value in labels.items():v['golem.thaumcraft.'+key]=value[i]
 v['research.CONTROLSEALS.stage.1']=v['research.CONTROLSEALS.stage.1'].split([' The third configuration page',' La tercera página de configuración'][i])[0] + [
  ' The third configuration page offers a face-oriented area for Pickup, Guard, Breaker, Harvest, Lumber and Butcher, including their advanced variants. X, Y and Z range independently from 1 to 8. Along the mounting face the area extends forward; perpendicular axes extend equally in both directions by dimension minus one. Existing-radius mode is the default and preserves previous zones. Switching modes or editing dimensions invalidates current jobs and flying darts. The home work radius and inventory faces stay separate. No chunks are loaded by an area search. Oriented Breaker drops physical loot for Pickup workers, so it can continue breaking blocks without carrying the drops.',
  ' La tercera página de configuración ofrece zona según cara para Recoger, Vigilar, Romper, Cosechar, Talar y Carnicero, incluidas sus variantes avanzadas. X, Y y Z van de 1 a 8 independientemente. En el eje de la cara de colocación la zona avanza hacia fuera; en los otros ejes se extiende por igual a ambos lados, dimensión menos uno. El modo Radio anterior es el predeterminado y conserva las zonas existentes. Cambiar modo o dimensiones invalida tareas y dardos en vuelo. El radio del hogar y las caras de inventario son independientes. Las búsquedas de área no cargan chunks. Romper según cara suelta el botín físico para otros gólems de Recoger y puede continuar sin acumularlo en su carga.'
 ][i]
 p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
print('Oriented seal area controls and bilingual guide generated.')
