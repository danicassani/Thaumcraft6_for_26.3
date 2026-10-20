#!/usr/bin/env python3
"""Smart golem progression; run after golem_darts."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'src/modern/resources/assets/thaumcraft/lang'
for i,locale in enumerate(['en_us','es_es']):
    p=root/(locale+'.json');v=json.loads(p.read_text())
    v['golem.thaumcraft.rank']=['Rank %s (%s/%s XP)','Rango %s (%s/%s XP)'][i]
    v['golem.thaumcraft.rank_max']=['Rank %s (maximum)','Rango %s (máximo)'][i]
    v['research.MINDBIOTHAUMIC.stage.2']=[
        'Smart golems learn from successful work: one XP per transferred stack, harvested or broken block, successful Use action or damaging attack, plus eight for a kill. Failed, blocked, canceled and idle actions give no XP. Clockwork minds, including ordinary Scout heads, do not learn. Ranks run from 0 to 10; reaching the next rank costs (rank + 1) squared times 1000 XP, retaining the remainder. Each rank grants one maximum health, 2.5% of the design movement speed and, for Fighters, 0.25 attack damage. Ranking up does not heal. Bell commands show rank and progress; the packed golem tooltip also displays them. Bell packing, deployment and world saves preserve rank, XP and health fraction. Old golems start at rank zero. Construction costs, research, channels, seal requirements and work radius stay the same.',
        'Los gólems Inteligentes aprenden del trabajo efectivo: un XP por pila transferida, bloque cosechado o roto, acción de Usar realizada o ataque que cause daño, y ocho adicionales por una baja. Fallos, bloqueos, acciones canceladas e inactividad no dan XP. Las mentes mecánicas, incluido Explorador sin Inteligente, no aprenden. Hay rangos de 0 a 10; el siguiente cuesta (rango + 1) al cuadrado por 1000 XP y conserva el sobrante. Cada rango aporta un punto de salud máxima, un 2,5 % de la velocidad del diseño y, para Luchadores, 0,25 de daño de ataque. Subir no cura. La campana muestra rango y progreso; también aparecen en la descripción del gólem recogido. Recoger, desplegar y guardar conserva rango, XP y fracción de salud. Los gólems anteriores comienzan en rango cero. Costes, investigaciones, canales, requisitos de sellos y radio de trabajo se mantienen.'
    ][i]
    p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
print('Smart golem ranks and bilingual progression guide generated.')
