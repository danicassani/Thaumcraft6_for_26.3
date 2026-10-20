# Áreas orientadas de sellos — port.27

**Estado actual:** [cierre de golemancia en port.29](GOLEMS-COMPLETE.md). Este documento describe una entrega histórica; sus listas de pendientes quedan sustituidas por el estado actual.

Minecraft 26.3 / Forge 66.0.13 / Java 25. Añade las dimensiones independientes X/Y/Z del original a Recoger, Vigilar, Romper, Cosechar, Talar y Carnicero, incluidas sus variantes avanzadas. Mantiene las zonas existentes como modo predeterminado; M11 sigue parcial.

## Configuración y geometría

La tercera página ofrece seis controles para reducir/aumentar X, Y y Z entre 1 y 8, y un botón de modo. En Radio anterior se mantiene exactamente el área anterior, incluidas la franja vertical de cosecha, el volumen de tala y el bloque de soporte de Romper. Al activar Según cara, las tres dimensiones definen el volumen original de TC6: en el eje de la cara de colocación avanza hacia fuera desde el soporte; en cada eje perpendicular se extiende dimensión menos uno a ambos lados. Por ejemplo, sobre una cara superior X=4, Y=2, Z=3 abarca 7 × 2 × 5 bloques. Los máximos de las cajas son exclusivos.

Los valores iniciales son 1 en el eje de la cara y 3 en los ejes perpendiculares. El área comienza en el bloque de la placa, junto al soporte. Romper nunca trata la placa como un bloque sin entidad y puede seleccionar varios bloques del volumen; el soporte queda fuera del volumen orientado. La rotura orientada permite actuar a tres bloques desde los ojos, para alcanzar bloques al subir al soporte; mantiene visión y permisos antes de romper. Las caras de inventario, las rutas de abastecimiento y el radio del hogar conservan su función independiente.

Romper en modo orientado deja el botín físicamente en el suelo, como el original, para que lo recoja un segundo gólem. Así puede continuar con los bloques siguientes sin quedar bloqueado por su propia carga. El modo anterior conserva su comportamiento de carga y los objetos ya transportados se mantienen.

El control de radio se desactiva en modo orientado. Los controles X/Y/Z se desactivan en modo anterior o en sus límites. Llenar, Vaciar, Proporcionar, Abastecer y Usar conservan sus interfaces y destinos de inventario/acción; no ofrecen áreas espaciales nuevas.

## Aplicación y protección

Recoger y el combate usan la caja para adquirir y revalidar objetivos. Carnicero cuenta la población reproductora dentro de ella. Cosechar y Romper recorren solo sus bloques cargados. Talar mantiene su límite de 256 troncos conectados y además restringe cada tronco a la caja configurada. No se fuerzan chunks durante estas búsquedas.

Los cambios de modo o dimensión incrementan la revisión, liberan la reserva y hacen que los gólems abandonen tareas anteriores. Los dardos vuelven a comprobar revisión y área al impactar. Se mantienen dueño, canal, permisos, redstone, investigación y requisitos del diseño. Editar exige ser dueño, estar en la misma dimensión y a ocho bloques como máximo.

La construcción de la entidad refresca también sus dimensiones físicas para la escala 0,4, en cliente y servidor. Corrige una caja de 1,4 × 2,7 y ojos sin escalar que persistían bajo el modelo pequeño; ahora la caja es 0,56 × 1,08 y la altura de ojos 0,918. No altera salud, carga, diseño ni progreso. Esto permite que colocación, navegación y alcance usen el tamaño del modelo.

## Persistencia y compatibilidad

El compuesto `seal_area` guarda X/Y/Z; `oriented_area` guarda el modo. Sin datos válidos, se conserva Radio anterior y el radio guardado. Las dimensiones numéricas se limitan a 1–8. Configurar una zona no cambia IDs, recetas, trabajos de prensa, componentes de objetos ni generación de mundo. La ampliación de datos del menú requiere la misma versión en cliente y servidor.

## Verificación

Seis pruebas unitarias nuevas cubren las seis caras, límites positivos y negativos, independencia de ejes, 512 configuraciones del codec y los sellos aplicables. Las fixtures `area-write/read/client` se excluyen del JAR publicado. Servidor dedicado: `work/area-server-write1.log` y `work/area-server-read3.log` finalizan correctamente. Guardan y restauran seis áreas X=3, Y=4, Z=2 con sus seis caras, canal 7, prioridad 4 y noveno filtro de esmeralda. Se comprueban límites de alcance de edición y datos antiguos sin área. Un sello real anterior conserva radio 3 y un veterano anterior conserva rango 10, salud 7 y nueve diamantes, con dimensiones físicas correctas tras recargar. El reinicio verifica además que el modo anterior conserva su radio, el retorno a orientado conserva sus dimensiones y los controles inactivos se rechazan en servidor.

Los quince generadores se ejecutan dos veces, con 1494 recursos idénticos en la segunda pasada.

Cliente: `work/area-client6.log` verifica dimensiones físicas de 0,56 × 1,08 también en el cliente. Recoge 17 esmeraldas y conserva tres pilas fuera de X/Y/Z; clics reales de X+, Y+ y Z+ permiten respectivamente 13 diamantes, siete diamantes elevados y once lingotes, con ocho XP de transferencias exactas. Los cambios de modo se sincronizan y conservan dimensiones. Un Romper hacia el este extrae dos bloques consecutivos y deja dos adoquines físicos, manteniendo soporte y bloque de diamante exterior intactos. Cosechar replanta el cultivo interior y deja maduro el exterior; Talar corta dos troncos y conserva el superior fuera de altura. Un objetivo válido y su dardo quedan excluidos al reducir X.

Se comparte una captura de los seis controles X=4/Y=2/Z=3 y del modo según cara después de estas pruebas y antes de generar el JAR o pushear. `work/area-capture-proof.json` registra el hash de la imagen, el commit anterior y la ausencia del JAR nuevo al capturarla.

El cliente guarda el mundo, vuelve al título (confirmado por estado de pantalla) y se cierra normalmente, sin capturas del menú ni terminación forzada. Servidor y cliente finalizan correctamente.

Compilación limpia: 116 pruebas, cero fallos, errores u omisiones. JAR `thaumcraft-6.1.BETA26-port.27-forge66.0.13.jar` de 7982513 bytes; SHA-256 `f0162897a091b51a35d92c8d0fc891c8a1748a992ee9311d4d9264f718bd1da5`. Se verifican 295 recetas, 257 definiciones de objetos, los JSON y la exclusión de fixtures. El verificador comprueba también que el JAR se generó después de la captura relevante.

## Pendiente

Coincidencia adicional de filtros, automatización de fabricación y modelos/animaciones originales. Las áreas configurables no completan toda la golemancia original.
