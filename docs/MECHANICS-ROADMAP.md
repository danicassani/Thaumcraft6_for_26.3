# Mecánicas y dependencias del port

Orden de trabajo. Estas entradas describen trabajo pendiente salvo que se indique una implementación verificada. No equivalen a funcionalidad por registrar un bloque. Las dependencias cruzadas (p. ej. hojas de silverwood → aura) se conservan en la implementación final.

## M01: Crecimiento de árboles y ciclo de hojas

**Estado:** port.10 añade crecimiento por tiempo/bonemeal, hojas nativas con drops/decay, recuperación de vis de silverwood y generación básica en bosques nuevos; forma de árboles y biomas mágicos originales aún parciales (ALCHEMY-ESSENTIA.md).

Contenido: `saplingGreatwood`, `saplingSilverwood`, `leafGreatwood`, `leafSilverwood`.

## M02: Iluminación, soporte y tintes de velas/nitor

**Estado:** luces, soporte, drops y recoloreado implementados; port.12 incorpora las velas como estabilizadores por pares con rendimiento decreciente; nitor no estabiliza (INFUSION-CARDS.md).

Contenido: `candles [16 colores]`, `nitor [16 colores]`.

## M03: Inventarios persistentes, menús y automatización

**Estado:** port.12 añade inventarios de una unidad, sincronización y capacidad Forge de los tres pedestales; contenedores y autómatas restantes pendientes.

Contenido: `tableWood`, `tableStone`, `pedestalArcane`, `pedestalAncient`, `pedestalEldritch`, `hungryChest`, `patternCrafter`, `focusPouch`.

## M04: Aspectos, investigación, escaneo y progreso

**Estado:** port.12 amplía a 32 cartas activas, ayudas de crisol/matriz/cerebro y nueve ayudas con paginación y protocolo nativo; curiosidades con estudio y fuente preservada, frascos exactos en requisitos; Study/Notation sin proveedor original activo se mantienen fuera (INFUSION-CARDS.md).

Contenido: `researchTable`, `thaumonomicon`, `curio`, `primordialPearl`, `celestialNotes`, `salisMundus`, `crystalEssence`, `scribingTools`, `thaumometer`.

## M05: Aura, vis y flux por chunk y sincronización

**Estado:** núcleo, cristales y consumidores de port.6 implementados; port.7 añade cargador sobre mesa arcana y sifón con grietas funcionales; port.14 conecta el manipulador de focos y su cargador (ARCANE-RIFTS.md).

Contenido: `crystalAir`, `crystalFire`, `crystalWater`, `crystalEarth`, `crystalOrder`, `crystalEntropy`, `crystalTaint`, `grassAmbient`, `visBattery`, `arcaneWorkbenchCharger`, `dioptra`, `rechargePedestal`, `visGenerator`, `voidSiphon`, `creativeFluxSponge`.

## M06: Crafteo arcano y descuentos de vis

**Estado:** mesa y cargador implementados; port.8 añade requisitos de investigación, recetas del taumómetro y dioptra y consulta en el libro; port.14 aplica descuentos reales de equipo (RESEARCH-SCANNING.md).

Contenido: `arcaneWorkbench`.

## M07: Alquimia, disolución de aspectos y fluidos

**Estado:** port.10 implementa crisol, agua/calor, disolución, 55 recetas, resolución de aspectos desde recetas y horno básico con alambiques; máquinas y mejoras posteriores pendientes (ALCHEMY-ESSENTIA.md).

Contenido: `crucible`, `centrifuge`, `bellows`, `smelterBasic`, `smelterThaumium`, `smelterVoid`, `smelterAux`, `smelterVent`, `alembic`, `infernalFurnace`, `thaumatorium`, `thaumatoriumTop`, `brainBox`, `condenser`, `condenserlattice`, `condenserlatticeDirty`.

## M08: Transporte y almacenamiento de esencia

**Estado:** port.11 añade seis tubos, succión local, válvulas/redstone, filtros, depósitos mixtos, etiquetas y resonador; port.12 añade cerebro con 2000 XP y absorción remota de tarros para la matriz; fuelles y espejos de esencia siguen pendientes (ESSENTIA-TRANSPORT.md, INFUSION-CARDS.md).

Contenido: `bannerCrimsonCult`, `tube`, `tubeValve`, `tubeRestrict`, `tubeOneway`, `tubeFilter`, `tubeBuffer`, `jarNormal`, `jarVoid`, `jarBrain`, `essentiaTransportInput`, `essentiaTransportOutput`, `label`, `phial`, `resonator`, `banners [16 colores]`.

## M09: Infusión, simetría, estabilidad y mejoras

**Estado:** port.13 añade estabilizadores con carga/red horizontal de incrustaciones y pedestales, mitigación pagada, mejoras de velocidad/coste y ocho encantamientos funcionales de herramientas; port.14 añade equipo, escudos rúnicos y guantelete básico; variantes avanzadas y sucesos de corrupción siguen pendientes (INFUSION-ADVANCED.md).

Contenido: `inlay`, `infusionMatrix`, `stabilizer`.

## M10: Focos, hechizos, guanteletes y proyectiles

**Estado:** port.18 añade velocidad/rebote/búsqueda de proyectiles, Fortuna IV/Seda en Rotura e Intercambio y G/Ctrl+G para Plan con ajustes sincronizados y persistentes; port.15–17 aportan Scatter, Split, Cloud/Mine, Plan, Spellbat y Rift. Grafos profundos, secuencias de medios y dispositivos restantes pendientes (FOCUS-OPTIONS.md).

Contenido: `wandWorkbench`, `pechWand`, `alumentum`, `turretPlacer`, `causalityCollapser`, `grappleGun`, `casterBasic`, `focus1`, `focus2`, `focus3`.

## M11: Gólems, sellos, tareas e IA

**Estado:** pendiente.

Contenido: `golemBuilder`, `golemBell`, `golemPlacer`, `seals`.

## M12: Warp, autocuración, efectos de equipo y accesorios

**Estado:** port.14 añade herramientas/armaduras del vacío, elementales, fortaleza y túnicas; reparación, descuentos, escudo rúnico, botas cargables, accesorios adaptados a mano secundaria y comprobador de cordura. Equivalencia completa, siete ranuras, máscaras, culto y limpieza de distorsión siguen pendientes (THAUMATURGY-EQUIPMENT.md).

Contenido: `brain`, `sanitySoap`, `bathSalts`, `sanityChecker`, `voidAxe`, `voidSword`, `voidShovel`, `voidPick`, `voidHoe`, `elementalAxe`, `elementalSword`, `elementalShovel`, `elementalPick`, `elementalHoe`, `primalCrusher`, `crimsonBlade`, `goggles`, `clothChest`, `clothLegs`, `clothBoots`, `travellerBoots`, `fortressHelm`, `fortressChest`, `fortressLegs`, `voidHelm`, `voidChest`, `voidLegs`, `voidBoots`, `voidRobeHelm`, `voidRobeChest`, `voidRobeLegs`, `crimsonPlateHelm`, `crimsonPlateChest`, `crimsonPlateLegs`, `crimsonBoots`, `crimsonRobeHelm`, `crimsonRobeChest`, `crimsonRobeLegs`, `crimsonPraetorHelm`, `crimsonPraetorChest`, `crimsonPraetorLegs`, `baubles`, `amuletVis`, `charmVerdant`, `bandCuriosity`, `charmVoidseer`, `ringCloud`, `charmUndying`.

## M13: Corrupción, contagio, rifts y fluidos especiales

**Estado:** núcleo de grietas implementado para el sifón: generación por flux, crecimiento, estabilidad, colisiones, sincronización, guardado y colapso con semillas; eventos avanzados, corrupción y fluidos pendientes (ARCANE-RIFTS.md).

Contenido: `taintFibre`, `taintCrust`, `taintSoil`, `taintRock`, `taintGeyser`, `taintFeature`, `taintLog`, `fluxGoo`, `liquidDeath`, `purifyingFluid`, `bottleTaint`.

## M14: Redstone, movimiento y máquinas auxiliares

**Estado:** pendiente.

Contenido: `pavingStoneTravel`, `pavingStoneBarrier`, `arcaneEar`, `arcaneEarToggle`, `lampArcane`, `lampFertility`, `lampGrowth`, `levitator`, `everfullUrn`, `spa`, `redstoneRelay`, `potionSprayer`.

## M15: Espejos y enlaces entre ubicaciones

**Estado:** pendiente.

Contenido: `mirror`, `mirrorEssentia`, `handMirror`.

## M16: Botín, estructuras, dimensión y contenido interno

**Estado:** pendiente.

Contenido: `stonePorous`, `lootCrateCommon`, `lootCrateUncommon`, `lootCrateRare`, `lootUrnCommon`, `lootUrnUncommon`, `lootUrnRare`, `hole`, `effectShock`, `effectSap`, `effectGlimmer`, `placeholderNetherbrick`, `placeholderObsidian`, `placeholderBars`, `placeholderAnvil`, `placeholderCauldron`, `placeholderTable`, `empty`, `barrier`, `lootBag`, `enchantedPlaceholder`.

## Dependencias adicionales fuera del registro BlocksTC/ItemsTC

También quedan pendientes los cubos de muerte líquida y fluido purificador (M13), las criaturas/IA y huevos de aparición de ConfigEntities (M10–M13/M16), los encantamientos normales y de infusión (M09/M12), los efectos de poción propios (M12/M13) y la generación de biomas/estructuras/dimensiones (M01/M16). No son referencias independientes de ItemsTC.

## Orden de implementación por dependencias

S → iluminación M02 → núcleo de aura M05 → árboles M01 → conocimiento/aspectos M04 → mesa arcana M06 → inventarios M03 → alquimia M07 → esencia M08 → infusión M09 → hechizos M10 → gólems M11 → equipo/warp M12 → corrupción M13 → dispositivos M14 → espejos M15 → mundo/botín M16. Las dependencias de consumo cruzadas se activan al implementar su sistema, sin sustituirlo por una receta gratuita.
