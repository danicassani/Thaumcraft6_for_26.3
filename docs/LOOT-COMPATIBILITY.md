# Botín nativo de 26.3: port.47

La revisión inicial de M16 detectó cinco avisos de alternativas inalcanzables. El problema era que 56 tablas conservaban campos de otra versión: `conditions` y `functions` podían ignorarse al leerlas. La primera alternativa quedaba incondicional y los modificadores de cantidades o componentes no se aplicaban.

## Cambio

Las tablas usan ahora `condition` y `modifier`. Los predicados y modificadores inline usan el discriminador `type`. Varias condiciones se combinan mediante `minecraft:all_of`; los modificadores conservan su orden. Toque de Seda utiliza el predicado vanilla `minecraft:tool/can_silk_touch`, y el estado de la prensa se comprueba mediante `minecraft:match_block`.

Esto restablece el comportamiento ya definido por el port:

- Piedra porosa intacta con Seda; sin ella, grava o una selección del botín ponderado. Fortuna aumenta la probabilidad de la selección.
- Tierra, costra y roca corruptas intactas con Seda; sin ella, posibilidad de cristal de Vitium con su componente real.
- Prensa compacta como objeto de prensa y prensa ensamblada como pistón, sin mezclar ambas alternativas.
- Cantidades variables de drops de criaturas, componentes de cristales y variantes de curiosidades; la curiosidad de ritos vuelve a requerir una muerte atribuida a jugador y su tirada de probabilidad.
- Condiciones de supervivencia a explosiones y los modificadores de las demás tablas migradas vuelven a formar parte de la evaluación nativa.

No cambian IDs ni formatos de guardado. La corrección afecta a futuras extracciones y drops; no reescribe objetos existentes que hubieran recibido componentes incorrectos con una versión anterior.

## Generación y comprobación

`tools/loot_schema.py` convierte la representación escrita por los generadores al formato de 26.3 al guardar un recurso `loot_table`; no transforma recetas ni otros JSON. Conserva sin cambios las tablas que ya son modernas, rechaza mezclas ambiguas de campos antiguos/nuevos y preserva la secuencia de modificadores.

`checkLootResources` forma parte de `check`/`build`, después de `processResources`. Revisa las 185 tablas y rechaza campos obsoletos y alternativas intermedias incondicionales. Comparte la selección de Python 3 de `checkModelResources`, incluida la opción `-PpythonExecutable=...`. La carga y evaluación en servidor siguen siendo necesarias para verificar codecs y comportamiento.

Las 56 tablas anteriores se usan como regresión de la conversión: todas producen exactamente el recurso nuevo y el verificador rechaza su formato anterior. Se comprueban idempotencia de las 185 tablas, rechazo de mezcla de esquemas y aislamiento de recursos ajenos al botín. La segunda pasada completa de generadores no cambia archivos.

## Alcance

Es una corrección del botín ya existente. Las bolsas de tesoros, cajas, urnas y su integración con estructuras aún pertenecen a los siguientes avances de M16.

## Resultados nativos

El servidor comprueba 64 extracciones con Seda por bloque y 1024 extracciones normales por cada bloque corrupto. En 4096 semillas deterministas, la piedra porosa produce 275 recompensas normales, 1092 con Fortuna III y 40 supervivientes con radio de explosión 100. Las alternativas compacta/ensamblada de la prensa producen exclusivamente prensa/pistón, respectivamente.

En 2048 tiradas se observan las cantidades 1..3 del cristal de semilla primordial, siempre con Vitium. Sin atribución a jugador no aparece ninguna curiosidad; con atribución aparecen 23 curiosidades, todas con la variante de ritos. Las pilas vacías por cantidad cero se distinguen de drops efectivos. La prueba conserva muestras reales en un cofre vanilla para comprobar su guardado y sincronización.

Una segunda JVM dedicada carga el cofre guardado y verifica el bloque obtenido con Seda, los cristales de Vitium (incluida la pila de tres) y la curiosidad de ritos. La salida final de ambas ejecuciones de servidor es correcta, sin los cinco avisos de alternativas inalcanzables ni errores de parseo de botín.

El cliente repite las pruebas y abre el cofre mediante clic real. Se verifican los componentes de Vitium/ritos, la pila de tres, grava y pistón mediante los slots sincronizados del contenedor nativo; después guarda el mundo desde el menú y cierra correctamente. La captura se tomó antes de compilar el JAR y pushear.

Las fixtures se activan con `-PalchemyProbe=loot-write`, `loot-read` y `loot-client`; la última espera a que el jugador entre en un mundo de pruebas. Quedan excluidas del JAR normal.

Compilación limpia `clean build test`: 180 pruebas, cero fallos, errores u omisiones; pasan `checkLootResources` (185 tablas) y `checkModelResources` (626 referencias). El guard de botín pasa también sobre las tablas extraídas del JAR. Se conservan 336 recetas y 316 definiciones de objetos.

Artefacto: `thaumcraft-6.1.BETA26-port.47-forge66.0.13.jar`, 8 900 155 bytes; SHA-256: `bc5363d85ba79c59e1934d93e846463e7f2250f7431cb1a2888ab8e3db1317a2`.

Actualización port.48: las bolsas y seis contenedores ya tienen mecánicas y recursos; su integración con generación de mundo queda para la siguiente etapa. Véase [TREASURES.md](TREASURES.md).
