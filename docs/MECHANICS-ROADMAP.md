# Mecánicas y dependencias del port

Orden de trabajo. Estas entradas describen trabajo pendiente salvo que se indique una implementación verificada. No equivalen a funcionalidad por registrar un bloque. Las dependencias cruzadas (p. ej. hojas de silverwood → aura) se conservan en la implementación final.

## M01: Crecimiento de árboles y ciclo de hojas

**Estado:** port.49 integra magical_forest en el clima estándar del Overworld, árboles greatwood de dos copas, silverwood con raíces/tronco en cruz y roble mágico, follaje nativo y flora. Solo terreno nuevo; estructuras, árboles infestados, decoración/variantes visuales y criaturas aún pendientes (WORLDGEN-FOREST.md).

Contenido: `saplingGreatwood`, `saplingSilverwood`, `leafGreatwood`, `leafSilverwood`.

## M02: Iluminación, soporte y tintes de velas/nitor

**Estado:** luces, soporte, drops y recoloreado implementados; port.12 incorpora las velas como estabilizadores por pares con rendimiento decreciente; nitor no estabiliza (INFUSION-CARDS.md).

Contenido: `candles [16 colores]`, `nitor [16 colores]`.

## M03: Inventarios persistentes, menús y automatización

**Estado:** pedestales e inventarios persistentes implementados; port.19 añade bolsa de 18 focos, port.20 prensa/carga de gólems y port.29 fabricador de diez patrones con inventarios físicos y reserva de vis; port.41 completa el cofre hambriento nativo con recogida física, 27 ranuras, automatización y receta original (HUNGRY-CHEST.md).

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

**Estado:** port.30 añade hornos y mejoras; port.31 añade centrifugadora; port.32 añade horno infernal multibloque reversible, Salis, cocción nativa, aura, fuelles, bonificaciones y cola persistente. port.33 añade Thaumatorium, matrices mnemónicas, selector autorizado, transporte y trabajo pagado persistente. port.34 añade condensador, celosías, filtrado de flux, atascos y mantenimiento, transporte y progreso persistente; equipos de M07 portados, compatibilidad y equivalencia final por auditar (ADVANCED-SMELTERS.md, CENTRIFUGE.md, INFERNAL-FURNACE.md, THAUMATORIUM.md, FLUX-CONDENSER.md).

Contenido: `crucible`, `centrifuge`, `bellows`, `smelterBasic`, `smelterThaumium`, `smelterVoid`, `smelterAux`, `smelterVent`, `alembic`, `infernalFurnace`, `thaumatorium`, `thaumatoriumTop`, `brainBox`, `condenser`, `condenserlattice`, `condenserlatticeDirty`.

## M08: Transporte y almacenamiento de esencia

**Estado:** port.11 añade seis tubos, succión local, válvulas/redstone, filtros, depósitos mixtos, etiquetas y resonador; port.12 añade cerebro con 2000 XP y absorción remota de tarros para la matriz; port.30 conecta fuelles a depósitos con prioridades y redstone. port.42 incorpora espejos de esencia y fuentes remotas para la matriz (MIRRORS.md) (ESSENTIA-TRANSPORT.md, INFUSION-CARDS.md, ADVANCED-SMELTERS.md).

Contenido: `bannerCrimsonCult`, `tube`, `tubeValve`, `tubeRestrict`, `tubeOneway`, `tubeFilter`, `tubeBuffer`, `jarNormal`, `jarVoid`, `jarBrain`, `essentiaTransportInput`, `essentiaTransportOutput`, `label`, `phial`, `resonator`, `banners [16 colores]`.

## M09: Infusión, simetría, estabilidad y mejoras

**Estado:** port.13 añade estabilizadores con carga/red horizontal de incrustaciones y pedestales, mitigación pagada, mejoras de velocidad/coste y ocho encantamientos funcionales de herramientas; port.14 añade equipo, escudos rúnicos y guantelete básico; variantes avanzadas y sucesos de corrupción siguen pendientes (INFUSION-ADVANCED.md).

Contenido: `inlay`, `infusionMatrix`, `stabilizer`.

## M10: Focos, hechizos, guanteletes y proyectiles

**Estado:** port.19 añade continuaciones de hasta cuatro niveles con modificadores propios, validación/costes globales y editor navegable; bolsa de 18 focos, estudio pech, colapsador causal, gancho recargable y ballestas automáticas básica/avanzada. Compatibilidad visual original, lienzo libre y variantes detalladas pendientes (FOCUS-GRAPHS-DEVICES.md).

Contenido: `wandWorkbench`, `pechWand`, `alumentum`, `turretPlacer`, `causalityCollapser`, `grappleGun`, `casterBasic`, `focus1`, `focus2`, `focus3`.

## M11: Gólems, sellos, tareas e IA

**Estado:** port.29 cierra la golemancia funcional: 2400 diseños, 16 sellos y sus opciones, logística/semillas, combate y defensa, minería progresiva y contenedores, prensa multibloque reversible y modelos OBJ originales con carga/animaciones. Fabricador de patrones integrado con proveedores y reposición. Adaptaciones modernas de placas, interfaz y compatibilidad con prensas compactas documentadas (GOLEMS-COMPLETE.md).

Contenido: `golemBuilder`, `golemBell`, `golemPlacer`, `seals`.

## M12: Warp, autocuración, efectos de equipo y accesorios

**Estado:** port.40 completa el pendiente de port.39: absorción especial de fortaleza/túnicas del vacío, viento de espada, atracción de hacha, azada 3x3/crecimiento, pala de tres planos con previsualización y pago protegido, efectos cuerpo a cuerpo de pico/vacío, reserva/movimiento/caídas del viajero y oscilación original de telas. Conserva accesorios, máscaras, culto y sucesos de port.38–39. Adaptaciones al daño y movimiento modernos documentadas; criaturas/variantes de culto siguen en M16 (EQUIPMENT-ABILITIES.md, ACCESSORIES-MASKS-CULT.md, WARP-EVENTS.md).

Contenido: `brain`, `sanitySoap`, `bathSalts`, `sanityChecker`, `voidAxe`, `voidSword`, `voidShovel`, `voidPick`, `voidHoe`, `elementalAxe`, `elementalSword`, `elementalShovel`, `elementalPick`, `elementalHoe`, `primalCrusher`, `crimsonBlade`, `goggles`, `clothChest`, `clothLegs`, `clothBoots`, `travellerBoots`, `fortressHelm`, `fortressChest`, `fortressLegs`, `voidHelm`, `voidChest`, `voidLegs`, `voidBoots`, `voidRobeHelm`, `voidRobeChest`, `voidRobeLegs`, `crimsonPlateHelm`, `crimsonPlateChest`, `crimsonPlateLegs`, `crimsonBoots`, `crimsonRobeHelm`, `crimsonRobeChest`, `crimsonRobeLegs`, `crimsonPraetorHelm`, `crimsonPraetorChest`, `crimsonPraetorLegs`, `baubles`, `amuletVis`, `charmVerdant`, `bandCuriosity`, `charmVoidseer`, `ringCloud`, `charmUndying`.

## M13: Corrupción, contagio, rifts y fluidos especiales

**Estado:** port.35–36 implementan corrupción, fluidos y cinco sucesos de grieta. port.37 incorpora perla primordial de ocho usos, restos reales de fabricación/infusión, botín original y efectos finales de colapso con guardado y protección frente a duplicación. Equivalencia funcional de este bloque revisada; shader y partículas adaptados a la API moderna (PRIMORDIAL-RIFTS.md, RIFT-EVENTS.md, TAINT-FLUIDS.md).

Contenido: `taintFibre`, `taintCrust`, `taintSoil`, `taintRock`, `taintGeyser`, `taintFeature`, `taintLog`, `fluxGoo`, `liquidDeath`, `purifyingFluid`, `bottleTaint`.

## M14: Redstone, movimiento y máquinas auxiliares

**Estado:** port.23 incorpora el levitador; port.43 añade piedras de viaje/barrera, dos orejas y relé nativo. port.44 completa lámparas arcana/crecimiento/fertilidad, urna inagotable, spa y pulverizador: luz con propiedad compatible con infusión, crecimiento y cría pagados, vis/agua, baños y pociones con essentia, menús, automatización y estados versionados persistentes. Recetas y recursos originales; adaptaciones documentadas (UTILITY-MACHINES.md, AUXILIARY-REDSTONE.md, GOLEMS-MOBILITY.md).

Contenido: `pavingStoneTravel`, `pavingStoneBarrier`, `arcaneEar`, `arcaneEarToggle`, `lampArcane`, `lampFertility`, `lampGrowth`, `levitator`, `everfullUrn`, `spa`, `redstoneRelay`, `potionSprayer`.

## M15: Espejos y enlaces entre ubicaciones

**Estado:** port.42 implementa espejos de objetos y esencia y espejo de mano: seis caras, enlace con dimensión/identidad, colas acotadas, automatización, envío físico y por menú, tarros remotos y matrices de infusión, inestabilidad/flux, guardado y recuperación sin cargar chunks a la fuerza. Recetas y texturas originales; shader y capacidad de cola adaptados (MIRRORS.md).

Contenido: `mirror`, `mirrorEssentia`, `handMirror`.

## M16: Botín, estructuras, dimensión y contenido interno

**Estado:** port.47 corrige 56 tablas existentes que usaban campos de condiciones/modificadores ignorados por 26.3. Se recuperan alternativas de Seda, probabilidades/Fortuna, explosiones y cantidades/componentes; el build revisa las 185 tablas (LOOT-COMPATIBILITY.md). Port.48 incorpora las tres bolsas y las seis cajas/urnas, con tablas nativas, recompensas y recursos originales (TREASURES.md). Port.49 incorpora el bosque mágico y árboles naturales (WORLDGEN-FOREST.md). Port.50 añade túmulos localizables con geometría original, tesoros, cofre/trampa y spawners, y greatwood infestado (WORLDGEN-STRUCTURES.md). Portales/criaturas restantes y dimensión siguen pendientes.

Contenido: `stonePorous`, `lootCrateCommon`, `lootCrateUncommon`, `lootCrateRare`, `lootUrnCommon`, `lootUrnUncommon`, `lootUrnRare`, `hole`, `effectShock`, `effectSap`, `effectGlimmer`, `placeholderNetherbrick`, `placeholderObsidian`, `placeholderBars`, `placeholderAnvil`, `placeholderCauldron`, `placeholderTable`, `empty`, `barrier`, `lootBag`, `enchantedPlaceholder`.

La revisión visual de port.46 corrige el atlas del cofre hambriento y las partículas de los cuatro crecimientos de corrupción; la compilación verifica los recursos de modelos (MODEL-RESOURCES.md).

## Dependencias adicionales fuera del registro BlocksTC/ItemsTC

Se han portado los cubos de muerte líquida y fluido purificador y las criaturas directamente asociadas a la corrupción (TAINT-FLUIDS.md). Quedan las demás criaturas/IA y huevos de aparición de ConfigEntities (M10–M13/M16), los encantamientos normales y de infusión (M09/M12), los efectos de poción propios (M12/M13) y la generación de biomas/estructuras/dimensiones (M01/M16). No son referencias independientes de ItemsTC.

## Orden de implementación por dependencias

S → iluminación M02 → núcleo de aura M05 → árboles M01 → conocimiento/aspectos M04 → mesa arcana M06 → inventarios M03 → alquimia M07 → esencia M08 → infusión M09 → hechizos M10 → gólems M11 → equipo/warp M12 → corrupción M13 → dispositivos M14 → espejos M15 → mundo/botín M16. Las dependencias de consumo cruzadas se activan al implementar su sistema, sin sustituirlo por una receta gratuita.
