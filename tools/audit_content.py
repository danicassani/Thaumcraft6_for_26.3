"""Audit original registration sites; do not infer completion from a registered ID."""
from pathlib import Path
import re,json
ROOT=Path(__file__).resolve().parents[1]
SIMPLE={'BlockStoneTC','BlockStairsTC','BlockSlabTC','BlockLogsTC','BlockPlanksTC','BlockMetalTC','BlockPillar','BlockFlesh','BlockRailPowered','ItemTCBase','ItemThaumiumAxe','ItemThaumiumSword','ItemThaumiumShovel','ItemThaumiumPickaxe','ItemThaumiumHoe','ItemThaumiumArmor','ItemChunksEdible','ItemTripleMeatTreat','BlockPlantShimmerleaf','BlockPlantCinderpearl','BlockPlantVishroom'}
# Every nontrivial registration is mapped explicitly. No decorative placeholders.
GROUPS={
'M01':('Crecimiento de árboles y ciclo de hojas', 'BlockSaplingTC BlockLeavesTC'),
'M02':('Iluminación, soporte y tintes de velas/nitor', 'BlockCandle BlockNitor'),
'M03':('Inventarios persistentes, menús y automatización', 'BlockTable BlockPedestal BlockHungryChest BlockPatternCrafter ItemFocusPouch'),
'M04':('Aspectos, investigación, escaneo y progreso', 'ItemThaumonomicon ItemCurio ItemCelestialNotes ItemScribingTools ItemThaumometer ItemMagicDust ItemCrystalEssence BlockResearchTable ItemPrimordialPearl'),
'M05':('Aura, vis y flux por chunk y sincronización', 'BlockCrystal BlockGrassAmbient BlockVisBattery BlockDioptra BlockArcaneWorkbenchCharger BlockRechargePedestal BlockVisGenerator BlockVoidSiphon ItemCreativeFluxSponge'),
'M06':('Crafteo arcano y descuentos de vis', 'BlockArcaneWorkbench'),
'M07':('Alquimia, disolución de aspectos y fluidos', 'BlockCrucible BlockThaumatorium BlockSmelter BlockSmelterAux BlockSmelterVent BlockBellows BlockInfernalFurnace BlockAlembic BlockCentrifuge BlockCondenser BlockCondenserLattice BlockBrainBox'),
'M08':('Transporte y almacenamiento de esencia', 'BlockTube BlockJar BlockEssentiaTransport ItemPhial ItemLabel BlockBannerTC ItemResonator'),
'M09':('Infusión, simetría, estabilidad y mejoras', 'BlockInfusionMatrix BlockStabilizer BlockInlay'),
'M10':('Focos, hechizos, guanteletes y proyectiles', 'BlockFocalManipulator ItemCaster ItemFocus ItemPechWand ItemAlumentum ItemCausalityCollapser ItemTurretPlacer ItemGrappleGun'),
'M11':('Gólems, sellos, tareas e IA', 'BlockGolemBuilder ItemGolemBell ItemGolemPlacer ItemSealPlacer'),
'M12':('Warp, autocuración, efectos de equipo y accesorios', 'ItemVoidAxe ItemVoidSword ItemVoidShovel ItemVoidPickaxe ItemVoidHoe ItemElementalAxe ItemElementalSword ItemElementalShovel ItemElementalPickaxe ItemElementalHoe ItemPrimalCrusher ItemCrimsonBlade ItemGoggles ItemRobeArmor ItemBootsTraveller ItemFortressArmor ItemVoidArmor ItemVoidRobeArmor ItemCultistPlateArmor ItemCultistBoots ItemCultistRobeArmor ItemCultistLeaderArmor ItemBaubles ItemAmuletVis ItemVerdantCharm ItemCuriosityBand ItemVoidseerCharm ItemCloudRing ItemCharmUndying ItemSanitySoap ItemBathSalts ItemSanityChecker ItemZombieBrain'),
'M13':('Corrupción, contagio, rifts y fluidos especiales', 'BlockTaintFibre BlockTaint BlockTaintFeature BlockTaintLog BlockFluxGoo BlockFluidDeath BlockFluidPure ItemBottleTaint'),
'M14':('Redstone, movimiento y máquinas auxiliares', 'BlockArcaneEar BlockArcaneEarToggle BlockRedstoneRelay BlockPavingStone BlockLevitator BlockLamp BlockWaterJug BlockSpa BlockPotionSprayer'),
'M15':('Espejos y enlaces entre ubicaciones', 'BlockMirror ItemHandMirror'),
'M16':('Botín, estructuras, dimensión y contenido interno', 'BlockLoot ItemLootBag BlockStonePorous BlockHole BlockEffect BlockPlaceholder BlockBarrier ItemEnchantmentPlaceholder'),
}
byclass={c:k for k,(_,classes) in GROUPS.items() for c in classes.split()}
rows=[]
for kind in ['Blocks','Items']:
 text=(ROOT/f'src/main/java/thaumcraft/common/config/Config{kind}.java').read_text();start=text.index(f'public static void init{kind}(');text=text[start:];end=text.find('\n    public static void ',100);text=text[:end] if end!=-1 else text
 for field,cls in re.findall(r'(?:BlocksTC|ItemsTC)\.(\w+)\s*=\s*(?:\(BlockSlab\))?\s*(?:registerBlock\()?new\s+(\w+)',text):
  group='S' if cls in SIMPLE else byclass.get(cls)
  if cls=='BlockOreTC' or field in ['amberBlock','amberBrick']:group='P3'
  if field=='empty':group='M16'
  if field.startswith('doubleSlab'):group='S-double'
  if group is None:raise ValueError((field,cls))
  rows.append({'kind':kind,'field':field,'class':cls,'group':group})
for field,cls in [('candles','BlockCandle'),('banners','BlockBannerTC'),('nitor','BlockNitor')]:rows.append({'kind':'Blocks','field':field+' [16 colores]','class':cls,'group':byclass[cls]})
assert len({(r['kind'],r['field']) for r in rows})==len(rows)
(ROOT/'docs/CONTENT-INVENTORY.json').write_text(json.dumps(rows,indent=2)+'\n')
lines=['# Inventario completo del registro original','', 'Fuente: ConfigBlocks.initBlocks y ConfigItems.initItems, más los tres mapas de 16 colores. Cada fila representa una referencia original; los materiales por metadata y los sellos contienen variantes. No es un recuento de IDs modernos.','','S = sin un sistema nuevo de Thaumcraft; P3 = lote previo; Mxx = dependencia propia. Las losas dobles se representan mediante type=double del mismo bloque moderno, sin duplicar items. Un ingrediente puede registrarse antes de que se porte la máquina que lo consume.','','| Tipo | Referencia original | Clase | Grupo |','|---|---|---|---|']
for r in rows:lines.append(f"| {r['kind']} | {r['field']} | {r['class']} | {r['group']} |")
(ROOT/'docs/CONTENT-INVENTORY.md').write_text('\n'.join(lines)+'\n')
lines=['# Mecánicas y dependencias del port','', 'Orden de trabajo. Estas entradas describen trabajo pendiente salvo que se indique una implementación verificada. No equivalen a funcionalidad por registrar un bloque. Las dependencias cruzadas (p. ej. hojas de silverwood → aura) se conservan en la implementación final.','']
for code,(name,classes) in GROUPS.items():
 fields=[r['field'] for r in rows if r['group']==code]
 status=json.loads((ROOT/'docs/MECHANICS-STATUS.json').read_text()).get(code,'pendiente') if (ROOT/'docs/MECHANICS-STATUS.json').exists() else 'pendiente'
 lines += [f'## {code}: {name}', '', '**Estado:** '+status+'.', '', 'Contenido: '+', '.join('`'+s+'`' for s in fields)+'.','']
lines += ['## Dependencias adicionales fuera del registro BlocksTC/ItemsTC', '', 'También quedan pendientes los cubos de muerte líquida y fluido purificador (M13), las criaturas/IA y huevos de aparición de ConfigEntities (M10–M13/M16), los encantamientos normales y de infusión (M09/M12), los efectos de poción propios (M12/M13) y la generación de biomas/estructuras/dimensiones (M01/M16). No son referencias independientes de ItemsTC.', '', '## Orden de implementación por dependencias', '', 'S → iluminación M02 → núcleo de aura M05 → árboles M01 → conocimiento/aspectos M04 → mesa arcana M06 → inventarios M03 → alquimia M07 → esencia M08 → infusión M09 → hechizos M10 → gólems M11 → equipo/warp M12 → corrupción M13 → dispositivos M14 → espejos M15 → mundo/botín M16. Las dependencias de consumo cruzadas se activan al implementar su sistema, sin sustituirlo por una receta gratuita.', '']
(ROOT/'docs/MECHANICS-ROADMAP.md').write_text('\n'.join(lines).rstrip()+'\n')
print(len(rows),'referencias originales auditadas;',sum(r['group'].startswith('S') for r in rows),'referencias sin sistema nuevo, incluidas losas dobles.')
