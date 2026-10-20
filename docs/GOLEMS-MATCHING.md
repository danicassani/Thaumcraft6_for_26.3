# Coincidencia de sellos — port.28

**Estado actual:** [cierre de golemancia en port.29](GOLEMS-COMPLETE.md). Este documento describe una entrega histórica; sus listas de pendientes quedan sustituidas por el estado actual.

Minecraft 26.3 / Forge 66.0.13 / Java 25. Recupera las opciones de comparación del original para los sellos con filtro avanzados, Proveer, Reponer y Usar. M11 sigue parcial: fabricación y modelos/animaciones originales quedan pendientes.

## Reglas y controles

La última página de configuración ofrece Desgaste, Materiales y Mismo mod. Está tras las áreas o las opciones de Usar cuando existen; los sellos de inventario sin estas opciones no muestran una página vacía. Exacto conserva su función como preset de comparación completa: cambia componentes y desgaste juntos. Desgaste puede ajustarse después por separado, conservando nombres, encantamientos y demás componentes cuando Exacto está activado.

Mismo mod compara el namespace del registro y tiene prioridad sobre las otras opciones, igual que el original. Materiales acepta etiquetas comunes `c` específicas de material bajo `ingots/`, `nuggets/`, `gems/`, `dusts/`, `raw_materials/`, `ores/`, `storage_blocks/`, `plates/`, `rods/` y `gears/`; además de `minecraft:logs`, `planks`, `wool` y `sand`. No considera equivalentes todos los lingotes por la etiqueta genérica, ni herramientas por etiquetas de uso o encantamiento. La coincidencia de materiales prevalece sobre componentes y desgaste, como el antiguo OreDictionary. Sin equivalencia se aplica la comparación del mismo objeto, desgaste y componentes configurados. Pilas vacías nunca coinciden.

Se aplican las mismas reglas a muestras, listas blancas/negras, selección de objetos físicos, inventarios, reservas y reposición. Un filtro amplio de Reponer cuenta las variantes aceptadas juntas; las muestras coincidentes no suman sus cuotas, mantienen el máximo existente. La carga conserva el tipo, componentes y cantidad extraídos. Editar invalida tareas/reservas y exige dueño, misma dimensión y alcance de ocho bloques. Los sellos básicos no aceptan los nuevos botones.

## Compatibilidad

Se guardan `match_damage`, `match_tags` y `match_mod`. Al cargar datos anteriores, desgaste adopta el valor de `exact`, y materiales/mod quedan desactivados: se conserva tanto la comparación exacta como la relajada anterior. No se añaden IDs, recetas ni cambios de generación. Cliente y servidor deben usar la misma versión por la ampliación de datos del menú.

## Verificación

Las fixtures `matching-write/read/client` quedan excluidas del JAR. Cubren comparación independiente de daño y nombre, prioridad de equivalencias/mod, exclusión de etiquetas genéricas, permisos, migración de ambos valores de Exacto y persistencia del noveno filtro con nombre y daño. Se recarga además un sello real de port.27 con su área orientada intacta. Servidor dedicado: `work/matching-write2.log` y `work/matching-read1.log`, ambos con cierre normal y `BUILD SUCCESSFUL`.

El cliente (`work/matching-client2.log`) usa clics reales en los controles y transporte mediante gólems. Primero recoge solo un pico con el nombre correcto y diferente desgaste, conservando su daño, y deja intacto otro nombre. Después dos gólems reponen ocho tablones de abedul con nombre junto a tres de roble hasta una cuota de once, con muestras solapadas y sin sobreabastecer. Se comprueba también la sincronización del control Mismo mod. La captura relevante se comparte antes de compilar el JAR o pushear.

Los 16 generadores se ejecutan dos veces y la segunda pasada no modifica los 1494 recursos.

La fixture reinicia la distancia de caída al trasladar al jugador a la plataforma y recupera su reaparición antes de las pruebas; no modifica las reglas de gólems del mod publicado. `work/matching-capture-proof.json` registra la imagen real, su SHA-256, el commit anterior y que el JAR port.28 aún no existía al capturarla.

El verificador de pruebas nativas comprueba los marcadores de servidor, reinicio y cliente y rechaza errores de aserción o caídas del juego. El cliente guarda el mundo, confirma el título mediante su estado de pantalla y cierra normalmente, sin capturas del menú principal ni terminación forzada.

Compilación limpia: 117 pruebas, cero fallos, errores u omisiones. JAR `thaumcraft-6.1.BETA26-port.28-forge66.0.13.jar` de 7985999 bytes; SHA-256 `3dbae4e22765058e29b6cb78c5b7c9925110c1d980d422100a4f6ab7a629eec4`. Se verifican 295 recetas, 257 definiciones de objetos, los JSON y la exclusión de fixtures. El JAR se ha generado después de la captura compartida.
