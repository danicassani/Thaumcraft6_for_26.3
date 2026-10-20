# Centrifugadora de esencia — port.31

Añade la centrifugadora a Minecraft 26.3 / Forge 66.0.13 / Java 25. Es el segundo lote de alquimia avanzada, tras los hornos y mejoras de port.30. No cambia la generación del mundo. El horno infernal, el Thaumatorium y el condensador siguen pendientes.

El horno infernal se añade en [port.32](INFERNAL-FURNACE.md).

## Funcionamiento

La máquina conserva las reglas de `TileCentrifuge` original: una unidad de un aspecto compuesto produce **una unidad de uno de sus dos componentes**, elegido al azar. No produce ambos ni dos unidades. Utiliza las 31 composiciones del catálogo original de 37 aspectos; rechaza los seis primarios y los identificadores desconocidos.

- Entrada inferior y salida superior; no hay conexión lateral. La succión inferior es 128 sin entrada y 64 mientras trabaja; arriba es cero. Solo consulta vecinos de chunks cargados.
- El trabajo necesita 39 ticks de proceso. La aspiración automática se intenta cada cinco ticks cuando ambos depósitos están vacíos. Tubos externos pueden entregar una entrada adicional mientras queda una salida; la capacidad máxima es una entrada y una salida.
- Redstone pausa la entrada y el reloj, con succión cero; permite extraer la salida. Al retirar la señal continúa desde el mismo tick.
- Una salida ocupada nunca se sobrescribe. La entrada puede llegar al final del reloj, pero espera a que se retire la salida anterior.
- Las simulaciones no modifican reservas ni progreso. Las transferencias usan el protocolo existente de comprobación de ambas partes antes de consumir una unidad.
- El resonador muestra la salida y succión; con la mano vacía también se consulta entrada y tiempo restante. No hay nueva interfaz de inventario. El comparador da 15 cuando hay una salida lista.

El modelo y la textura de bloques originales se reutilizan. El rotor se representa mediante ocho fases de estados de bloque, con geometrías recta y diagonal; es una adaptación al renderizado moderno, no el antiguo renderizador de entidad. El rotor se detiene con redstone.

## Fabricación e investigación

Receta arcana original ` T /RCP/ T `: dos tubos, resonador mórfico, constructo alquímico básico y mecanismo simple. Consume 100 vis y un cristal de Ordo y de Perditio. Requiere `CENTRIFUGE`, con el padre `TUBES` y una teoría de Alquimia. La página del libro y los textos español/inglés incluyen el funcionamiento del port.

## Persistencia

`centrifuge` es una entidad nueva con formato propio versión 1. Guarda entrada, salida y ticks restantes, sin cambiar los IDs ni los formatos de tubos, tarros y hornos existentes. Redstone persiste como señal física del mundo.

Versiones futuras, aspectos desconocidos, primarios como entrada y tiempos incompatibles se conservan sin reinterpretarlos, con la máquina desactivada. Romper una máquina válida libera como flux las unidades que aún contiene y deja el bloque vacío, siguiendo el tratamiento de las máquinas de esencia del port.

## Verificación

Las fixtures `-PalchemyProbe=centrifuge-write/read/client` usan mundos desechables y se excluyen del JAR normal. Comprueban receta e investigación reales, conservación, azar, caras, redstone, depósitos bloqueados, simulación, datos incompatibles, transporte físico y reinicio. El cliente muestra una instalación activa junto a otra pausada por redstone.

### Servidor y reinicio

- Receta real bloqueada sin investigación; avance pagando una teoría (32 puntos), fabricación con exactamente 100 vis y pago de todas las ranuras de ingredientes y cristales. Prueba de fabricación registrada.
- 64 unidades de Potentia dieron 28 Ignis y 36 Ordo. Se verificaron las caras, succión 128/64/0, reloj de 39 ticks, pausa, salida bloqueada, simulaciones y rechazo/preservación de datos de una versión futura.
- Durante 650 ticks normales del servidor, una instalación con tubos y tarro conservó las 12 unidades iniciales entre fuente, entrada, salida, tubos y tarro. El tarro recogió una unidad antes de que una salida de otro aspecto bloqueara la única ruta disponible, sin sobrescribir ni perder reservas.
- Reinicio real: entrada Lux, salida Ignis, 17 ticks pendientes y señal redstone recuperados exactamente. Tras retirar la señal y la salida anterior, el trabajo terminó sin volver a pagar entrada. También se cargó el horno del vacío del mundo de prueba de port.30.

### Cliente y recursos

Cliente nativo con el mismo recorrido de receta, separación y red física. Verificación en el cliente de cambios de fase del rotor, recogida real en un tarro y otra máquina pausada con entrada pagada y 23 ticks exactos. Guardado del mundo, regreso al título sin capturarlo y cierre normal; todas las ejecuciones finales terminaron con `BUILD SUCCESSFUL`.

Captura real de 1280×720 compartida antes de generar el JAR y pushear. SHA-256: `4014221c4b678ea4370be6891d15b7507c6a3a45fb89c4f31d87d13e50a6251a`. A la izquierda está la instalación activa con buffer y dos tarros; a la derecha, la instalación pausada por el bloque de redstone.

Cadena de los 19 generadores ejecutada dos veces: segunda pasada sin cambios. Las fixtures y sus teletransportes, bloques y conocimiento de pruebas no se incluyen en el artefacto normal.

`clean build`: **130 tests, 0 fallos, 0 errores y 0 omitidos**. JAR normal sin fixtures, con 304 recetas, 264 definiciones de objeto y todos los JSON válidos. Se conservan las máquinas, mejoras y mallas de gólems de la versión anterior.

Artefacto: `thaumcraft-6.1.BETA26-port.31-forge66.0.13.jar` (8089856 bytes). SHA-256: `cf42de22761e3fe158f3f3c76923bdc1763fb515df2063eeb150ffdb6416788f`. Compilado después de compartir la captura anterior.
