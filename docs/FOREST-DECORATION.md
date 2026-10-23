# Decoración del bosque mágico — port.53

## Terreno nuevo

El bioma `thaumcraft:magical_forest` incorpora dos features colocadas nativas:

- `magical_boulders`: `minecraft:forest_rock`, de adoquín musgoso. Cada chunk sortea entre cero y dos intentos, como el `nextInt(3)` original. Se coloca en modificaciones locales, antes de la vegetación.
- `magical_mushrooms`: `minecraft:mushroom_island_vegetation`, que elige con igual probabilidad el hongo gigante rojo o marrón. Se realizan dieciséis sorteos independientes, cada uno con probabilidad de 1/40, antes de los árboles del bosque mágico.

Los modificadores de altura usan `WORLD_SURFACE_WG` y el filtro nativo de bioma. Las formas, sustratos, obstrucciones y límites de altura los gestionan los generadores vanilla de 26.3. La distribución dentro del chunk se adapta a `in_square`: conserva los intentos y probabilidades originales, pero no la cuadrícula antigua de 4×4 ni sus desplazamientos hacia chunks vecinos. Las semillas no reproducen las posiciones de 1.12.2.

Los bloques son los originales vanilla; no se añaden IDs, datos de bloque ni formatos de guardado. No hay recorridos de chunks cargados ni regeneración retroactiva. Las partidas existentes conservan su terreno; esta decoración aparece al explorar chunks nuevos de bosque mágico. `generateTrees` sigue afectando a los árboles, no a estas rocas y hongos. Las opciones que desactivan nuevas áreas del bosque mantienen su comportamiento anterior.

## Paleta azul del cliente

`graphics.blueBiome` pasa a controlar la paleta alternativa original: césped 6728396 y follaje 7851246. Desactivada, se delega al resolver previo del juego y se conservan los colores del bioma/datapack, incluidos los valores originales de césped 5635969 y follaje 6750149. Los otros biomas y el agua conservan sus colores.

Dos access transformers de campos de `BiomeColors` permiten envolver los resolvers nativos al registrar los colores del cliente, antes de crear las cachés de `ClientLevel`. Se conserva el mezclado nativo de colores en las fronteras y los proveedores de tintes existentes, incluidas las hojas de greatwood; las hojas de silverwood mantienen su tinte fijo. No se mutan los registros de biomas del servidor ni del cliente.

El cambio de opción se detecta en el hilo del cliente: vacía las cachés de tintes y recompila la geometría visible. Funciona también sobre bosques ya guardados y no requiere regenerar bloques. La identidad del bioma se actualiza al cambiar de nivel o desconectarse.

## Verificación

Los fixtures opcionales `decoration-server`, `decoration-read` y `decoration-client` quedan excluidos del JAR normal. Localizan un interior de bosque mágico a partir del clima y exigen que sus archivos de región no existan antes de generar el área. La prueba inspecciona chunks generados normalmente, sin colocar rocas u hongos a mano para fabricar la evidencia.

La fauna pendiente (pechs y otras criaturas), el bioma eerie, los portales mayores, jefes y contenido/dimensión eldritch siguen fuera de esta entrega. No se declara cerrado M16.

Servidor dedicado: la región de prueba no existía antes de generarse. En la ejecución final, el área de 7×7 chunks del bosque próximo a `(101856, 102304)` contiene 13 bloques superficiales de roca musgosa, 205 bloques de copa roja, 216 de copa marrón y 47 de tallo. Las muestras guardadas son hongo `(101876, 73, 102307)` y roca `(101877, 70, 102308)`. Se comprobaron también las referencias nativas y las etapas de decoración cargadas.

El primer buscador del fixture exigía un interior homogéneo de 128×128 bloques y no encontró ninguno en su radio. La búsqueda exige ahora un interior de 32×32 y conserva la comprobación de archivos de región ausentes para toda el área muestreada. Se mantienen los bordes reales de los biomas y los filtros nativos; no se fuerza el bioma del terreno.

Un segundo proceso de servidor dedicado cargó las dos muestras desde disco y confirmó la conservación de los hongos, la roca y el bioma. El cliente usa una copia aislada del mundo dedicado ya cerrado; no reutiliza el gran escenario de pruebas anteriores.

La primera prueba gráfica detectó secciones ausentes al invalidar directamente `LevelRenderer`. La actualización de 26.3 requiere `LevelExtractor.allChanged()`, que renueva también `SectionUpdateTracker` y difiere la recompilación al momento correcto del frame. Se usa esta ruta nativa y el fixture exige que ambas secciones de muestra estén compiladas y visibles antes de anunciar la captura.

También se corrigió la comprobación de regiones del fixture: 26.3 guarda el Overworld en `dimensions/minecraft/overworld/region`. Se resuelve la ruta mediante `DimensionType.getStorageFolder`, se exige que la carpeta nativa exista y se repiten las pruebas sobre archivos de región ausentes. La primera ejecución del cliente había inspeccionado las muestras copiadas del servidor, no una región nueva del cliente.

Cliente final: genera de forma nativa la misma región nueva en su copia independiente, confirma rocas y ambos tipos de copa, alterna la paleta normal → azul → normal con actualización de cachés y conserva los colores de otros biomas y los registros. Las dos secciones de muestra están compiladas antes de la captura. Se restaura la opción original del cliente después de capturar.

Captura compartida antes del JAR final y de los pushes. SHA-256 de la imagen: `a37db90dbbbe9e3daecc42acfb1afce86ec202bc19f871abdb9a11a41e438ad3`.

Entrega final: `clean build test` completado en 49 s; 187 pruebas, sin fallos, errores ni omisiones. Segunda pasada de generadores sin cambios. Recursos comprobados: 637 referencias de modelos, 336 recetas, 325 definiciones de objetos y 205 tablas de botín. El JAR normal excluye los fixtures y conserva todos los recursos gráficos de port.52.

Artefacto: `thaumcraft-6.1.BETA26-port.53-forge66.0.13.jar`, 8994471 bytes. SHA-256: `cf8a7c65a14645a3764e8e9d860a40ebc7d119271f3dace690ce93155ed038db`.

Actualización port.54: los pechs ya están implementados; véase [PECHS.md](PECHS.md). Las apariciones naturales de wisps están disponibles desde port.52.
