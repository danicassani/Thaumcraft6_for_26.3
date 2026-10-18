# Mecánicas y dependencias del port

Orden de trabajo. Estas entradas describen trabajo pendiente salvo que se indique una implementación verificada. No equivalen a funcionalidad por registrar un bloque. Las dependencias cruzadas (p. ej. hojas de silverwood → aura) se conservan en la implementación final.

## M01: Crecimiento de árboles y ciclo de hojas

**Estado:** pendiente.

Contenido: `saplingGreatwood`, `saplingSilverwood`, `leafGreatwood`, `leafSilverwood`.

## M02: Iluminación, soporte y tintes de velas/nitor

**Estado:** iluminación, soporte, drops y recoloreado implementados y probados; el consumidor de estabilidad de infusión se implementará en M09.

Contenido: `candles [16 colores]`, `nitor [16 colores]`.

## M03: Inventarios persistentes, menús y automatización

**Estado:** pendiente.

Contenido: `tableWood`, `tableStone`, `pedestalArcane`, `pedestalAncient`, `pedestalEldritch`, `hungryChest`, `patternCrafter`, `focusPouch`.

## M04: Aspectos, investigación, escaneo y progreso

**Estado:** port.8 implementa conocimiento persistente, observaciones y escaneos únicos, catálogo original (148 investigaciones/271 etapas), diario, Thaumonomicon, requisitos y bloqueos de recetas; mesa/cartas de teorías, cielo y consumidores avanzados pendientes (RESEARCH-SCANNING.md).

Contenido: `researchTable`, `thaumonomicon`, `curio`, `primordialPearl`, `celestialNotes`, `salisMundus`, `crystalEssence`, `scribingTools`, `thaumometer`.

## M05: Aura, vis y flux por chunk y sincronización

**Estado:** núcleo, cristales y consumidores de port.6 implementados; port.7 añade cargador sobre mesa arcana y sifón con grietas funcionales; conexión al manipulador de focos pendiente de M10 (ARCANE-RIFTS.md).

Contenido: `crystalAir`, `crystalFire`, `crystalWater`, `crystalEarth`, `crystalOrder`, `crystalEntropy`, `crystalTaint`, `grassAmbient`, `visBattery`, `arcaneWorkbenchCharger`, `dioptra`, `rechargePedestal`, `visGenerator`, `voidSiphon`, `creativeFluxSponge`.

## M06: Crafteo arcano y descuentos de vis

**Estado:** mesa y cargador implementados; port.8 añade requisitos de investigación, recetas del taumómetro y dioptra y consulta en el libro; descuentos de equipo pendientes (RESEARCH-SCANNING.md).

Contenido: `arcaneWorkbench`.

## M07: Alquimia, disolución de aspectos y fluidos

**Estado:** pendiente.

Contenido: `crucible`, `centrifuge`, `bellows`, `smelterBasic`, `smelterThaumium`, `smelterVoid`, `smelterAux`, `smelterVent`, `alembic`, `infernalFurnace`, `thaumatorium`, `thaumatoriumTop`, `brainBox`, `condenser`, `condenserlattice`, `condenserlatticeDirty`.

## M08: Transporte y almacenamiento de esencia

**Estado:** pendiente.

Contenido: `bannerCrimsonCult`, `tube`, `tubeValve`, `tubeRestrict`, `tubeOneway`, `tubeFilter`, `tubeBuffer`, `jarNormal`, `jarVoid`, `jarBrain`, `essentiaTransportInput`, `essentiaTransportOutput`, `label`, `phial`, `resonator`, `banners [16 colores]`.

## M09: Infusión, simetría, estabilidad y mejoras

**Estado:** pendiente.

Contenido: `inlay`, `infusionMatrix`, `stabilizer`.

## M10: Focos, hechizos, guanteletes y proyectiles

**Estado:** pendiente.

Contenido: `wandWorkbench`, `pechWand`, `alumentum`, `turretPlacer`, `causalityCollapser`, `grappleGun`, `casterBasic`, `focus1`, `focus2`, `focus3`.

## M11: Gólems, sellos, tareas e IA

**Estado:** pendiente.

Contenido: `golemBuilder`, `golemBell`, `golemPlacer`, `seals`.

## M12: Warp, autocuración, efectos de equipo y accesorios

**Estado:** pendiente.

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
