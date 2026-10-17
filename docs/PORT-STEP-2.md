# Paso 2: infraestructura Forge 26.3

Estado: infraestructura implementada, compilación, cliente, servidor y persistencia
verificados en esta instancia. No es un port jugable ni implica compatibilidad con partidas antiguas.

## Checkout y fuentes oficiales

Inspección inicial: rama `work`, HEAD `954022bb777b7546281fb36df8522f0ba6b43f81`,
sin cambios locales. Se revisaron build.gradle, propiedades, wrapper, Thaumcraft,
Registrar, todos los proxies, ConfigBlocks/Items/Entities/Recipes/Aspects/Research,
ModConfig, mcmod.info, pack.mcmeta y META-INF/tc_at.cfg.
Los fuentes y recursos de `src/main` no se han modificado ni movido.

Fuentes contrastadas:

- [Forge, rama 26.3](https://github.com/MinecraftForge/MinecraftForge/tree/26.3),
  observada en `55f03a2d86fec8f44722ee3d2d5f2d60cb93c5e6`.
- [Tag oficial 66.0](https://github.com/MinecraftForge/MinecraftForge/tree/66.0),
  commit `2bc9649bd66c25598552f748da11f73fb6e6547a`: minecraft.versions.toml,
  mdk/build.gradle, wrapper, mods.toml, ejemplo en src/test/java/com/example,
  DeferredRegister, ForgeRegistries y PlayerEvent.Clone.
- [Documentación oficial](https://docs.minecraftforge.net/en/26.3/gettingstarted/):
  acceso HTTP 403. El repositorio oficial MinecraftForge/Documentation no anuncia
  rama 26.3. No se han usado ejemplos 1.21 como autoridad de API 26.3.

Se fija Forge `26.3-66.0.0`, ForgeGradle `7.0.17`, Java 25 y el wrapper Gradle
9.5.0 del tag 66.0. La rama 26.3 actual lleva Gradle 9.7.1, pero se conserva el
del MDK del tag seleccionado. El MDK ofrece un rango para ForgeGradle: aquí se
fija su límite inferior exacto. eventbus-validator 7.0.6 coincide con el catálogo
actual de la misma rama. El catálogo Maven confirmó la publicación de 26.3-66.0.0 y la compilación
resolvió correctamente ese artefacto.

## Correspondencia de mecanismos

| Mecanismo 1.12.2 | Ubicación antigua | Equivalente previsto/implementado 26.3 | En este paso |
|---|---|---|---|
| @Mod + @Mod.EventHandler | Thaumcraft.java | @Mod + constructor FMLJavaModLoadingContext y FMLCommonSetupEvent.getBus | Sí |
| @SidedProxy, IProxy | proxies/* | Suscriptor físico Dist.CLIENT separado; sin referencia desde código común | Sí |
| RegistryEvent.Register | Registrar.java | DeferredRegister tipado, register(BusGroup) | Sí, infraestructura vacía |
| ConfigBlocks/Items/Entities | common/config | Puntos de extensión BLOCKS/ITEMS/ENTITIES | Contenido individual pendiente |
| Potion | Registrar y lib/potions | MOB_EFFECTS | Solo infraestructura |
| IRecipe, registros por código | ConfigRecipes | RECIPE_TYPES y RECIPE_SERIALIZERS estáticos; instancias mediante datos | Solo infraestructura |
| GameRegistry TileEntity | ConfigBlocks | BlockEntityType y serializadores propios | Paso posterior |
| @Config, ConfigManager, ConfigChangedEvent | ModConfig, CommonProxy | ForgeConfigSpec, CLIENT/SERVER registrados en contexto | Sí, esquema |
| capabilities serializables | PlayerWarp/PlayerKnowledge | Almacenamiento versionado aislado para warp; conocimiento pendiente | Parcial |
| ChunkDataEvent | ChunkEvents | Persistencia de aura/sellos junto al port de esos sistemas | Inventario |
| Fluids universal bucket, networking, GUI, IMC, comandos | Thaumcraft y CommonProxy | APIs modernas respectivas | Fuera de este paso |
| Biomas/worldgen | Registrar, ConfigBlocks | Registros dinámicos/data packs | Paso posterior |

`sourceSets.main` selecciona solo `src/modern/java`. Recursos modernos en
`src/modern/resources`; processResources reutiliza únicamente `assets/**` del
árbol antiguo. No entran mcmod.info, el access transformer 1.12 ni pack_format 3.
El pack moderno usa formato 121, según el generado oficial del tag 66.0.
Revertir esta selección restaura el árbol antiguo sin movimientos masivos.
No existen bloques/items/entidades de demostración ni instancias antiguas activas.
La configuración declara el source set del mod mediante runs.mods (API de
ForgeGradle 7.0.17). Además compileJava y processResources escriben en staging
separado, y mergeMainMod (Sync) crea una única raíz build/mod/main para el loader.
Se evita merge-source-sets=true porque solapaba salidas de tareas en clean build. El servidor usa run/server y el cliente
run, evitando conflictos de archivos y logs. src/runtimeTest/java se incluye
únicamente con -PwarpRestartProbe; el build normal lo excluye.

Mod bus: registros y ciclo de vida. Bus global: PlayerEvent.Clone.BUS. El código
común no importa ni carga paquetes del cliente; ClientLifecycle queda detrás de
Dist.CLIENT. Se comprobó en fuentes y en dos arranques de servidor dedicado: el contexto
informa Env=SERVER, no carga ImmediateWindowProvider ni emite el listener de cliente.

## Configuración

Véase [tabla completa de las 43 opciones](CONFIGURATION-MIGRATION.md).
40 definiciones migradas (6 CLIENT y 34 SERVER), 3 pendientes: overworldDim,
dimensionOuterId y nostress. Los valores no activan mecánicas que aún no existen.
No se carga simultáneamente el sistema @Config antiguo. No se convierte .cfg.
Forge generó y cargó run/config/thaumcraft-client.toml (6 valores) y
run/server/port-step2-smoke/serverconfig/thaumcraft-server.toml (34 valores).
Los 40 defaults se compararon con ModConfig original; el segundo servidor volvió
a cargar su archivo sin corregir valores.

## Inventario de persistencia

| Propietario | Lectura/escritura antigua | Formato/ID | Estrategia moderna y estado |
|---|---|---|---|
| Jugador: warp | PlayerWarp.DefaultImpl.serializeNBT/deserializeNBT; Provider; PlayerEvents.attachCapabilitiesPlayer/playerClone | ForgeCaps `thaumcraft:warp`: int[] warp (PERMANENT,NORMAL,TEMPORARY), int counter | Implementado almacenamiento servidor con Codec y formato v1 bajo ForgeData `thaumcraft:warp`; no conversión automática de ForgeCaps |
| Jugador: investigación/conocimiento | PlayerKnowledge.DefaultImpl.serializeNBT/deserializeNBT y Provider; PlayerEvents clone | `thaumcraft:knowledge`; listas research (key,stage,flags CSV) y knowledge (key,amount) | Pendiente con ResearchManager, categorías, flags y sincronización; no descartar claves desconocidas |
| Chunk: aura | ChunkEvents.chunkSave/chunkLoad | compuesto `Thaumcraft`: base short, flux/vis float, boolean regenKey | Pendiente con AuraHandler; no convertir cada chunk a un SavedData global ficticio |
| Chunk: sellos | ChunkEvents y SealEntity.writeNBT/readNBT | `Thaumcraft.seals`, lista de compuestos | Pendiente con golems/sellos y sus coordenadas/configuraciones |
| Bloques | TileThaumcraft y subclases readFromNBT/writeToNBT/writeSyncNBT | NBT de TileEntity e inventarios | Port individual a BlockEntity y API de serialización de 26.3 |
| Entidades | common/entities, métodos readEntityFromNBT/writeEntityToNBT | NBT específico de constructos, criaturas y proyectiles | Port individual de cada entidad |
| Items | ItemStack tags, InfusionEnchantment y utils | NBT de aspectos, foco, cargas y mejoras | Componentes de datos y codecs cuando se porten los items |
| Entidades de item | TileCrucible, PlayerEvents y AIPechItemEntityGoto, getEntityData | Marcas auxiliares de entidad de item en NBT | Pendiente junto a recetas, pechs y entidades de item |
| Mundo/dimensión | WorldEvents load/unload; AuraWorld/AuraHandler | Cachés de memoria; worldSave vacío | No se encontró WorldSavedData/MapStorage propio. El estado persistente real está por chunk; no se fabrica dato global |

El almacenamiento nuevo de warp conserva los tres tipos y rango 0–500. Counter
conserva int sin inventar límites. Formato v1 explicita campos; ausencia devuelve
estado vacío sin escribirlo. Datos inválidos/versiones futuras producen DataResult
con error y bloquean sobrescritura. Clone conserva copia del tag existente,
incluidos datos futuros, en muerte o recreación. La persistencia en disco usa
getPersistentData y ForgeData, verificados contra el parche oficial de Entity.
No hay red, mecánicas warp ni importación 1.12.2; no usar partidas originales.

Se añadió la suite JUnit 5.12.2 WarpCodecTest (6 tests de round trip y rechazo
de entradas); checkWarpCodec depende de test. JUnit y su launcher 1.12.2 son
dependencias exclusivamente de test, verificadas con el toolchain de este build;
no se distribuyen ni se cargan como mods. La suite pasó con 6 tests y cero fallos.
Además se ejecutó WarpRestartProbe, descrito más abajo, sobre un mundo real de prueba.

## Dependencias

Baubles `curse.maven:baubles-227083:2518667` es el artefacto antiguo. Se retira del
build moderno junto con sus consumidores excluidos, sin afirmar que exista versión
26.3 ni elegir una sustitución no verificada. Al portar equipamiento hará falta
una API de accesorios compatible con Forge 26.3 o una implementación propia
autorizada. Ninguna dependencia de accesorios se ha añadido. También quedan fuera los fuentes vendorizados
Botania IPetalApothecary y com.sasmaster.glelwjgl, ligados a API/renderizado antiguo.

## Comandos y resultados reales

Directorio: `/workspace/Thaumcraft6_for_26.3`.
El helper local `python3 /workspace/.cloud-setup/gradle-modern` ejecuta
`bash gradlew --no-daemon` con Java 25, proxy explícito para procesos hijos,
Java 8 auxiliar, cachés bajo /workspace y, para cliente, el driver Vulkan Mesa
por software. Es una configuración de máquina, no una dependencia del mod.
El install_script guardado reproduce el helper, JDKs, certificados y herramientas.

| Comando/comprobación | Resultado real |
|---|---|
| JAVA_HOME=.../java25 GRADLE_USER_HOME=/workspace/.gradle bash gradlew --version | Gradle 9.5.0, Temurin 25.0.3+9 |
| python3 /workspace/.cloud-setup/gradle-modern clean build | Compilación limpia y build correctos; suite JUnit de 6 tests, 0 fallos, 0 errores, 0 omitidos |
| bash /workspace/.cloud-setup/install-modern.sh | Instalación repetida y build correctos; 5.147 assets verificados |
| DISPLAY=:99 python3 /workspace/.cloud-setup/gradle-modern runClient | Pantalla principal observada: Minecraft 26.3, Forge Beta 66.0.0, 3 mods cargados. Salida normal por Quit Game, BUILD SUCCESSFUL |
| TC_WARP_RESTART_PROBE=write python3 /workspace/.cloud-setup/gradle-modern -PwarpRestartProbe runServer --console=plain | Servidor dedicado listo, WARP_RESTART_PROBE_WRITE_OK, cierre con stop y guardado correcto |
| TC_WARP_RESTART_PROBE=read python3 /workspace/.cloud-setup/gradle-modern -PwarpRestartProbe runServer --console=plain | Mismo mundo reabierto, WARP_RESTART_PROBE_READ_OK, cierre con stop y guardado correcto |
| python3 /workspace/.cloud-setup/ping-minecraft.py, en ambos servidores | Respuesta JSON válida del protocolo 777, nombre 26.3, 0/20 jugadores |
| Comparación de TOML generado contra defaults originales | 6 opciones CLIENT y 34 SERVER coinciden |
| git diff --check; git diff --exit-code -- src/main | Correctos; originales intactos |
| Búsqueda de APIs antiguas en src/modern | Sin @SidedProxy, RegistryEvent, ConfigManager, ConfigChangedEvent, FML* antiguos, NBTTagCompound ni referencias a proxies/Baubles |
| Inspección del JAR normal | Solo 6 clases modernas, bytecode major 69 (Java 25), un pack.mcmeta moderno, sin mcmod.info ni tc_at.cfg |

Prueba de persistencia: `src/runtimeTest/java/thaumcraft/persistence/WarpRestartProbe.java`
se compila solo con la propiedad explícita de integración. Utiliza un ServerPlayer
real de prueba, UUID fijo, en el mundo nuevo port-step2-smoke; no registra contenido.
La primera fase comprueba ausencia sin escritura, guarda State(1,21,34,55,89) con
PlayerDataStorage.save y confirma el .dat. Se cerró completamente el servidor con
stop. La segunda fase carga el .dat mediante PlayerDataStorage y TagValueInput,
comprueba los cinco campos, publica PlayerEvent.Clone y confirma la copia; además
comprueba que read/write rechacen un tag inválido y lo conserven intacto.
No se simuló login ni muerte física de un jugador conectado: se probó el evento
de recreación real y el almacenamiento real. No se probó ninguna conversión 1.12.
Para repetir la fase write se necesita otro mundo de prueba sin ese UUID guardado;
no borrar ni sobrescribir un mundo existente para hacer pasar la prueba.

La EULA fue aceptada expresamente por el usuario para este entorno de prueba.
El servidor está ligado a 127.0.0.1:25565. No se probó login autenticado remoto.

Las tareas repetidas reutilizaron salidas válidas FROM-CACHE cuando correspondía;
la suite también se ejecutó realmente, con resultados XML de 6/0/0/0.

## Fallos diagnosticados durante la preparación

- Inicialmente Maven Forge devolvía HTTP 403 descargando
  minecraft-mavenizer-0.4.54.jar. Después de la actualización de red, se resolvió
  el artefacto oficial 26.3-66.0.0. Los procesos hijos de Java también necesitan
  el proxy; se propaga con JAVA_TOOL_OPTIONS.
- Mavenizer solicitó Java 8 para una herramienta auxiliar. Se exponen JAVA_HOME_8,
  JDK8 y org.gradle.java.installations.paths. La compilación y el runtime usan Java
  25; se comprobó el bytecode y los mensajes de ModLauncher.
- Slime Launcher intentaba escribir assets en /home/agent. Se usa user.home local
  bajo /workspace sin modificar HOME. Un timeout de descarga se resolvió con
  reintentos y verificación SHA-1 de los 5.147 objetos contra el índice oficial.
- La primera prueba gráfica falló por GLX y ausencia de VK_KHR_surface. Se preparó
  el paquete Debian firmado mesa-vulkan-drivers 25.0.7-2+deb13u1 y su ICD lavapipe;
  Vulkan llvmpipe llegó a la pantalla principal. Xvfb se inicia con
  `/workspace/.cloud-setup/xvfb/usr/bin/Xvfb :99 -screen 0 1280x720x24 -nolisten tcp`.
- El primer intento de carga detectó clases/metadatos separados. El ajuste
  merge-source-sets=true del MDK permitió cargar el mod, pero clean build reveló
  que processResources eliminaba las clases restauradas al compartir su salida.
  La declaración explícita mods/ModConfig.source(SourceSet), por sí sola, tampoco
  unía las raíces en este launcher. La solución final usa staging separado para
  cada tarea y un Sync mergeMainMod con dependencias explícitas para producir una
  raíz común, sin propietarios de salida solapados. Las salidas se declaran antes
  de configurar runs, porque ForgeGradle captura snapshots de SourceSet; se
  comprobó explícitamente la presencia de Thaumcraft y la marca del probe en servidor.
  Un intento intermedio arrancó solo Forge y mostró mod:thaumcraft ausente: no
  se contó como prueba superada. No se desactivó la caché
  ni se omitieron compilación, recursos o tests.
- El primer runner de checks Java no era descubrible por la tarea test de Gradle 9;
  se sustituyó por una suite JUnit real. No se desactivó failOnNoDiscoveredTests.
- El runtime registra avisos de servicios online no accesibles
  (discovery.minecraftservices.com y pc.realms.minecraft.net) y ausencia de audio
  físico. No impidieron menú, servidor ni persistencia; no se verifican Realms,
  autenticación online ni reproducción de sonido.
- Netty comprueba kqueue en Linux y Log4j DebugFile registra un error no fatal al
  formatear esa excepción de plataforma. Ambos servidores continuaron con su
  transporte disponible, respondieron al protocolo y guardaron/cerraron bien.
  No se ha alterado Netty ni ocultado el log mediante catches del mod.

Se mantuvieron TLS y verificaciones de paquetes/artefactos: JDKs con SHA-256,
paquetes Debian contra índices firmados y assets con los hashes oficiales.
Logs de esta instancia: /tmp/thaumcraft-client-staged.log,
/tmp/thaumcraft-server-write.log, /tmp/thaumcraft-server-read.log,
/tmp/thaumcraft-server-final-verified.log, /tmp/thaumcraft-build-accepted.log y
/tmp/thaumcraft-install-accepted.log. Captura final: /tmp/thaumcraft-main-menu-final.png.
Guardar el borrador del entorno no equivale a publicarlo ni demuestra restauración
en una tarea nueva; esas operaciones siguen a cargo del producto/usuario.

## Paso 3 y límites

Seleccionar contenido real y portar sus IDs, propiedades, block entities y recursos
de datos. Diseñar la integración de accesorios compatible con Forge 26.3 y el
protocolo de investigación/conocimiento. Resolver nostress por jugador y las
claves modernas de dimensiones cuando se porten sus sistemas. No activar en bloque
el árbol 1.12.2. Aura, mundo, golems, GUI, renderizadores, recetas individuales y
compatibilidad de partidas antiguas permanecen fuera de este paso.
