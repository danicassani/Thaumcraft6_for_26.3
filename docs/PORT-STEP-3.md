# Paso 3: minerales, materiales y bloques básicos

Objetivo: primer contenido utilizable sobre Forge 26.3-66.0.13 / Java 25.
Versión del mod: 6.1.BETA26-port.3-forge66.0.13.

## Contenido

Pestaña creativa «Thaumcraft», también localizable por búsqueda y /give:

- Bloques: ore_amber, ore_cinnabar, ore_quartz, metal_thaumium, metal_void,
  metal_brass, amber_block y amber_brick (todos bajo thaumcraft:).
- Materiales: amber, quicksilver, ingot_thaumium, ingot_void, ingot_brass,
  nugget_thaumium, nugget_void, nugget_brass y nugget_quicksilver.

Se mantienen los IDs de bloques originales. Los materiales que antes usaban
metadata se separan en IDs propios; no hay conversión de inventarios 1.12.2.
Texturas originales, modelos y blockstates modernos, definiciones items/ de 26.3,
nombres en inglés y español. Los bloques de ámbar son translúcidos y se recogen
a mano. Los cinco bloques de almacenamiento pueden formar bases de baliza.

Ámbar: 1–2 unidades; cuarzo: 1 unidad. Ambos admiten Fortuna, Toque de Seda y
1–4 XP sin Toque de Seda. Cinabrio se recoge como bloque y se funde en mercurio
(200 ticks, 1 XP). Se exigen herramientas mediante tags modernos. El hallazgo
raro de curiosidades del ámbar queda pendiente del sistema de investigación.

19 recetas con desbloqueos por inventario: nueve pepitas por lingote (y viceversa),
nueve lingotes por bloque de metal (y viceversa), nueve gotas por mercurio,
cuatro piezas de ámbar por bloque, conversión reversible de cuatro bloques de
ámbar/ladrillos y fundición del cinabrio. Se conservan esas proporciones del
ConfigRecipes original. No se añaden recetas ordinarias para fabricar taumio,
metal del vacío o latón a partir de materias vanilla: requieren sistemas de
alquimia que aún no están portados; por ahora están disponibles en creativo.

## Generación

Se añaden los tres minerales a los biomas del Overworld mediante forge:add_features,
con sustitución de piedra y pizarra profunda. Solo afecta a chunks nuevos.
Distribución inicial del port, no una promesa de balance idéntico al original:

| Mineral | Tamaño máximo de veta | Intentos/chunk al 100% | Alturas inclusivas |
|---|---:|---:|---|
| Ámbar | 6 | 8 | 0 a 96 |
| Cinabrio | 4 | 4 | -48 a 32 |
| Cuarzo | 6 | 6 | -32 a 64 |

OreCountPlacement aplica generateAmber, generateCinnabar, generateQuartz y
oreDensity de la configuración SERVER existente. Las fracciones de intentos se
redondean de forma probabilística, sin perder la generación a densidades bajas.
No hay regeneración retroactiva de terreno, aura, cristales vis, árboles mágicos,
Thaumonomicon, investigación, crisol ni máquinas en este lote.

## Empaquetado y pruebas

src/main se conserva intacto. processResources reutiliza únicamente las texturas
del árbol antiguo: los modelos, blockstates y datos de 1.12 no se distribuyen
como recursos modernos. BasicContentProbe es una prueba optativa de integración
y se excluye del build normal.

Verificado el 18 de octubre de 2026 (Europe/Madrid):

- clean build: correcto; WarpCodecTest ejecutado, 6 tests, cero fallos/errores.
- `-PbasicContentProbe=basic-content-verified runServer`: servidor dedicado
  listo y BASIC_CONTENT_PROBE_OK. Comprueba colocación/loot de los 8 bloques,
  Toque de Seda, las 18 recetas de crafteo y fundición. Busca y encuentra los
  tres minerales generados naturalmente en 25 chunks. Comprueba también la
  desactivación del ámbar y el escalado de densidad al 500%, restaurando los
  valores de configuración al terminar. Cierre por stop y guardado correctos.
- Cliente normal, sin fixture: entrada en una copia del mundo de prueba,
  pestaña Thaumcraft (página 2 del inventario creativo) con 17 entradas y sus
  modelos/texturas visibles. Mena de ámbar seleccionada y colocada en el mundo.
- Atlas blocks/items ampliados para las rutas originales textures/blocks y
  textures/items; Minecraft 26.3 los separa. Se comprobó la recarga de recursos
  final en el cliente. Sin avisos de modelos o texturas ausentes del contenido.
- JAR normal: 19 recetas y 17 definiciones modernas de item; sin clases Probe.
  JSON y referencias a modelos/texturas comprobados. src/main intacto y
  git diff --check correcto.

Los logs y capturas permanecen locales en work/. Se publica el código y esta
nota en la rama del port; la rama de artefactos recibe solo JAR y SHA-256.
Persisten los avisos de entorno ya conocidos (servicios online, audio y backend
OpenGL no disponible, con Vulkan por software operativo).
