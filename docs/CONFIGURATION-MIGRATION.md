> Actualización port.73: `misc.allowCheatSheet` ya está implementada; véase [CHEAT-SHEET.md](CHEAT-SHEET.md). Las referencias siguientes a su falta de consumidor son históricas.

# Correspondencia de configuración

Fuente de las filas antiguas: `src/main/java/thaumcraft/common/config/ModConfig.java`.
«Migrada (esquema)» significa definición TOML registrada, con valor por defecto conservado; sus mecánicas todavía no están conectadas. No significa que la opción ya altere el juego ni que sus mecánicas estén portadas. La generación y carga de ambos TOML sí se verificó en cliente/servidor.
Los archivos .cfg antiguos no se importan automáticamente.
Desde el [paso 3](PORT-STEP-3.md), las opciones de los tres minerales y su densidad están conectadas a la generación de chunks nuevos. Desde [port.6](M05-CRYSTALS-DEVICES.md), `world.generateCrystals` y `world.oreDensity` controlan los intentos de generación de cristales en chunks nuevos.
Desde [port.5](AURA-CORE.md), `world.generateAura` controla la inicialización de nuevos registros de aura. Las demás filas marcadas como esquema conservan el estado anterior.

| Clave antigua | Por defecto | Significado/comentario original | Destino | Estado |
|---|---|---|---|---|
| CONFIG_GRAPHICS.largeTagText | `false` | Setting this to true will make the amount text in aspect tags twice as large. Useful for certain resolutions and custom fonts. | CLIENT graphics.largeTagText | migrada (esquema) |
| CONFIG_GRAPHICS.disableShaders | `false` | This setting will disable certain thaumcraft shaders for those who experience FPS drops. | CLIENT graphics.disableShaders | migrada (esquema) |
| CONFIG_GRAPHICS.nostress | `false` | Set to true to disable anxiety triggers like the heartbeat sound. | Pendiente: efecto mixto cliente/servidor | pendiente |
| CONFIG_GRAPHICS.crooked | `true` | Hate crooked labels, kittens, puppies and all things awesome? If yes, set this to false. | CLIENT graphics.crooked | migrada (esquema) |
| CONFIG_GRAPHICS.dialBottom | `false` | Set to true to have the wand dial display in the bottom left instead of the top left. | CLIENT graphics.dialBottom | migrada (esquema) |
| CONFIG_GRAPHICS.showTags | `false` | Item aspects are hidden by default and pressing shift reveals them. Changing this setting to 'true' will reverse this behaviour and always display aspects unless shift is pressed. | CLIENT graphics.showTags | migrada (esquema) |
| CONFIG_GRAPHICS.blueBiome | `false` | Set this to true to get the old blue magical forest back. | CLIENT graphics.blueBiome | migrada (esquema) |
| CONFIG_GRAPHICS.showGolemEmotes | `true` | Will golems display emote particles if they recieve orders or encounter problems | SERVER graphics.showGolemEmotes | migrada (esquema) |
| CONFIG_WORLD.overworldDim | `0` | The dimension considered to be your 'overworld'. Certain TC structures will only spawn in this dim. | Pendiente: dimensión numérica → clave de dimensión | pendiente |
| CONFIG_WORLD.dimensionOuterId | `-42` | Outer lands dimension id | Pendiente: dimensión numérica → clave de dimensión | pendiente |
| CONFIG_WORLD.oreDensity | `100` | The % of normal ore amounts that will be spawned. For example 50 will spawn half the ores while 200 will spawn double. Default 100 | SERVER world.oreDensity | activa en chunks nuevos (paso 3) |
| CONFIG_WORLD.generateMagicForest | `true` | Generar bosque mágico | SERVER world.generateMagicForest | migrada (esquema) |
| CONFIG_WORLD.biomeMagicalForestWeight | `5` | Higher values increases number of magical forest biomes. If you are using biome addon mods you probably want to increase this weight quite a bit | SERVER world.biomeMagicalForestWeight | migrada (esquema) |
| CONFIG_WORLD.taintSpreadRate | `100.0f` | The % chance of taint fibres spreading on a block tick. Setting this to 0 will effectively stop taint fibre spread. | SERVER world.taintSpreadRate | activa en la propagación (TAINT-FLUIDS.md) |
| CONFIG_WORLD.taintSpreadArea | `32` | The range at which taint can spread from a taint seed. This value is only a base and will be modified by flux levels. | SERVER world.taintSpreadArea | activa en semillas y degradación (TAINT-FLUIDS.md) |
| CONFIG_WORLD.generateAura | `true` | Generar aura | SERVER world.generateAura | conectada: nuevos registros de aura |
| CONFIG_WORLD.generateStructure | `true` | Habilitar generación de estructuras Thaumcraft | SERVER world.generateStructure | migrada (esquema) |
| CONFIG_WORLD.generateCinnabar | `true` | Habilitar generación de mena de cinabrio | SERVER world.generateCinnabar | activa en chunks nuevos (paso 3) |
| CONFIG_WORLD.generateAmber | `true` | Habilitar generación de mena de ámbar | SERVER world.generateAmber | activa en chunks nuevos (paso 3) |
| CONFIG_WORLD.generateQuartz | `true` | Habilitar generación de mena de cuarzo | SERVER world.generateQuartz | activa en chunks nuevos (paso 3) |
| CONFIG_WORLD.generateCrystals | `true` | Habilitar generación de cristales | SERVER world.generateCrystals | conectada: cristales en chunks nuevos |
| CONFIG_WORLD.generateTrees | `true` | Habilitar generación de árboles Thaumcraft | SERVER world.generateTrees | migrada (esquema) |
| CONFIG_WORLD.regenKey | `"DEFAULT"` | This key is used to keep track of which chunk have been generated/regenerated. Changing it will cause the regeneration code to run again, so only change it if you want it to happen. Useful to regen only one world feature at a time. | SERVER world.regenKey | migrada (esquema) |
| CONFIG_WORLD.regenAura | `false` | Regenerar aura de chunks marcados para regeneración | SERVER world.regenAura | migrada (esquema) |
| CONFIG_WORLD.regenStructure | `false` | Regenerar estructuras de chunks marcados | SERVER world.regenStructure | migrada (esquema) |
| CONFIG_WORLD.regenCinnabar | `false` | Regenerar cinabrio de chunks marcados | SERVER world.regenCinnabar | migrada (esquema) |
| CONFIG_WORLD.regenAmber | `false` | Regenerar ámbar de chunks marcados | SERVER world.regenAmber | migrada (esquema) |
| CONFIG_WORLD.regenQuartz | `false` | Regenerar cuarzo de chunks marcados | SERVER world.regenQuartz | migrada (esquema) |
| CONFIG_WORLD.regenCrystals | `false` | Regenerar cristales de chunks marcados | SERVER world.regenCrystals | migrada (esquema) |
| CONFIG_WORLD.regenTrees | `false` | Regenerar árboles de chunks marcados | SERVER world.regenTrees | migrada (esquema) |
| CONFIG_WORLD.allowSpawnAngryZombie | `true` | Permitir spawn de zombis furiosos | SERVER world.allowSpawnAngryZombie | migrada (esquema) |
| CONFIG_WORLD.allowSpawnFireBat | `true` | Permitir spawn de murciélagos de fuego | SERVER world.allowSpawnFireBat | migrada (esquema) |
| CONFIG_WORLD.allowSpawnWisp | `true` | Permitir spawn de wisps | SERVER world.allowSpawnWisp | activa en wisps de grieta; aparición natural pendiente (RIFT-EVENTS.md) |
| CONFIG_WORLD.allowSpawnTaintacle | `true` | Permitir spawn de tentáculos de corrupción | SERVER world.allowSpawnTaintacle | migrada (esquema) |
| CONFIG_WORLD.allowSpawnPech | `true` | Permitir spawn de pechs | SERVER world.allowSpawnPech | migrada (esquema) |
| CONFIG_WORLD.allowSpawnElder | `true` | Permitir spawn de ancianos eldritch | SERVER world.allowSpawnElder | migrada (esquema) |
| CONFIG_WORLD.allowChampionMobs | `true` | Setting this to false will disable spawning champion mobs. Even when false they will still have a greatly reduced chance of spawning in certain dangerous places. | SERVER world.allowChampionMobs | migrada y aplicada por M16.1: suma 2 a la tirada y anula los bonus de dificultad, bioma y whitelist (CHAMPIONS.md) |
| CONFIG_MISC.noSleep | `false` | Setting this to true will make you get the recipe book for salis mundus without having to sleep first. | SERVER misc.noSleep | migrada (esquema) |
| CONFIG_MISC.wussMode | `false` | Setting this to true disables Warp, Taint spread and similar mechanics. You wuss. | SERVER misc.wussMode | activa parcialmente en investigación, corrupción y sucesos de grietas (TAINT-FLUIDS.md, RIFT-EVENTS.md) |
| CONFIG_MISC.allowCheatSheet | `false` | Enables a version of the Thauminomicon in creative mode that grants you all the research when you first use it. | SERVER misc.allowCheatSheet | migrada (esquema) |
| CONFIG_MISC.shieldRecharge | `2000` | How many milliseconds passes between runic shielding recharge ticks. Lower values equals faster recharge. Minimum of 500. | SERVER misc.shieldRecharge | migrada (esquema) |
| CONFIG_MISC.shieldWait | `4000` | How many milliseconds passes after a shield has been reduced to zero before it can start recharging again. Minimum of 0. | SERVER misc.shieldWait | migrada (esquema) |
| CONFIG_MISC.shieldCost | `1` | How much vis it costs to reacharge a single unit of shielding. Minimum of 0. | SERVER misc.shieldCost | migrada (esquema) |

Los límites se conservan: oreDensity 1–500, biomeMagicalForestWeight 0–100,
taintSpreadRate 0–500 (float antiguo, DoubleValue moderno), taintSpreadArea 8–256,
shieldRecharge 500–500000 ms, shieldWait 0–50000 ms, shieldCost 0–500.
oreDensity y biomeMagicalForestWeight requieren reinicio del mundo.

GRAPHICS no equivale automáticamente a CLIENT: `showGolemEmotes` controla
`world.setEntityState` desde AIGoto/AIGotoBlock/AIGotoEntity; se define en SERVER.
`nostress` se consume tanto en PacketMiscEvent cliente como en WarpEvents servidor
para sonidos: pendiente de diseñar la política por jugador y la sincronización;
no se crean dos valores contradictorios.
WORLD afecta generación, regeneración y spawns. MISC afecta PlayerEvents,
WarpEvents, TaintHelper, ResearchManager e ItemThaumonomicon: autoridad SERVER.
No hay opción COMMON identificada que justifique otro archivo.
SERVER se carga al abrir un mundo (serverconfig); CLIENT en el cliente (config).

`auraSize`, `aspectOrder`, `found*`, `isHalloween` son estado interno/cachés,
no opciones declaradas con @Config: no se convierten en ajustes de usuario.
ConfigBlocks/Items/Entities/Recipes/Aspects/Research son inicializadores de
contenido, no archivos de configuración adicionales. Las integraciones por
OreDictionary y reflexión de MFR quedan pendientes con sus mecánicas.

## Opción nueva de port.7

`SERVER world.generateFluxRifts` (por defecto `true`) controla la aparición de
nuevas grietas por exceso de flux. Desactivarla conserva las entidades existentes.
No es una clave importada del `.cfg` original. El resto de M13 y `wussMode`
continúan pendientes; véase [ARCANE-RIFTS.md](ARCANE-RIFTS.md).

## Consumidores de port.8

`SERVER misc.noSleep` ya entrega el diario al recoger cristales sin exigir dormir.
`wussMode` evita el warp concedido por etapas de investigación; los demás efectos
de warp/taint siguen pendientes. `allowCheatSheet` continúa sin consumidor.
Véase [RESEARCH-SCANNING.md](RESEARCH-SCANNING.md).

## Consumidores de port.10

`SERVER world.generateTrees` controla la nueva generación básica de greatwood y
silverwood en bosques. No bloquea el crecimiento de saplings plantados. La
recuperación de vis de sus hojas usa el aura existente; véase
[ALCHEMY-ESSENTIA.md](ALCHEMY-ESSENTIA.md).

## Consumidores de port.35–36

`world.allowSpawnWisp` controla las apariciones de wisps desde las grietas.
`misc.wussMode` impide nuevas apariciones y elimina las nubes ambientales;
los wisps activos con IA también desaparecen. La corrupción y los líquidos de
port.35 tienen sus consumidores documentados en [TAINT-FLUIDS.md](TAINT-FLUIDS.md).
Las referencias a mecánicas pendientes en los apartados históricos de port.7/8
se actualizan en [RIFT-EVENTS.md](RIFT-EVENTS.md) para estos sistemas; el resto
de sucesos de distorsión sigue pendiente.
