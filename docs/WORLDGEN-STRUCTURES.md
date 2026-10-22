# Estructuras y tesoros naturales: port.50

## Túmulo

`thaumcraft:mound` es una estructura nativa con pieza persistente y conjunto `thaumcraft:mounds`. Puede localizarse con `/locate structure thaumcraft:mound`. Se coloca en terreno nuevo de biomas de bosque, taiga, jungla, sabana, colinas, montaña y otras superficies compatibles del Overworld; se excluyen los generadores planos y se comprueban biomas, desnivel y columnas de terreno. `world.generateStructure=false` impide nuevos inicios; las estructuras ya guardadas siguen cargándose y sus piezas pendientes pueden terminar por el flujo nativo. No hay regeneración automática de chunks antiguos.

`tools/generate_structures.py` extrae las 2441 colocaciones únicas de la geometría original de `WorldGenMound.java`: volumen de 19×16×19, cámaras, acceso, escalera de mano y decoración. El recurso `data/thaumcraft/blueprints/mound.json` conserva el SHA-256 del original y una paleta de estados modernos. `STONE_STAIRS` pasa a escaleras de adoquín, sus ocho combinaciones de orientación/mitad se conservan, las escalerillas miran al este y `STONEBRICK` 3 pasa a ladrillos cincelados. Minecraft resuelve formas y conexiones al terminar cada chunk.

El contenido original es funcional: dos cajas/urnas independientes, generadores de esqueletos y zombis, y cofre con `minecraft:chests/simple_dungeon`. Cada tesoro tiene un 67 % de probabilidad de ser común, 23 % poco común y 10 % raro; un 30 % de las veces es caja y el resto urna. El cofre puede ser trampa (1/3) con TNT dos bloques por debajo, activada por la señal de redstone nativa al abrirlo. No se genera el botín de los contenedores hasta romperlos; Seda conserva el bloque como en port.48.

La semilla de botín se guarda en la pieza. Las elecciones se derivan de ella y de la posición lógica, de modo que no dependen del orden de generación de los chunks. La pieza anota los chunks completados y los serializa: volver a procesarlos no reconstruye tesoros abiertos ni repone inventarios. Las escrituras se recortan al chunk en generación; no se carga terreno a la fuerza desde un tick ni se sobrescriben chunks completos ya guardados.

La distribución adapta la tirada antigua de 1/100 por chunk a una rejilla nativa de separación: espaciado 10, separación mínima 5 y sal estable 68413261. La etiqueta `thaumcraft:has_mound` excluye desiertos, océanos y campos de hongos: la consulta base de columnas ocurre antes de aplicar las reglas de superficie y no basta para distinguir arena de piedra. Los datapacks pueden ampliar esa etiqueta para sus biomas compatibles. Se comprueban las alturas de centro y cuatro esquinas, con desnivel máximo de dos bloques respecto al centro. No reproduce las posiciones de semillas de 1.12.2. La decoración vegetal sigue el orden moderno y puede crecer sobre el túmulo.

## Greatwood infestado

Uno de cada ocho greatwoods naturales que consiguen crecer intenta añadir la variante infestada: generador de arañas de cueva bajo el tronco, cofre de mazmorra un bloque más abajo y 50 intentos de telaraña junto a troncos/hojas de greatwood. Depende de la generación de árboles, como el original. Los brotes plantados no crean esta variante. Se comprueban los dos bloques inferiores antes de escribir y se rechaza reemplazar entidades de bloque o materiales ajenos al suelo natural.

## Alcance pendiente

Esta entrega conecta tesoros con las estructuras naturales disponibles en el generador original. El portal de culto que ese generador también intentaba crear es una criatura/encuentro y queda con el contenido de culto pendiente; no se sustituye por otra construcción. Tampoco completa la dimensión, bioma eerie ni contenido eldritch. Las bolsas de tesoro siguen pendientes de sus fuentes de campeones/jefes; no se inventan recetas.

## Verificación

Servidor dedicado y cliente: pieza repartida en cuatro chunks con serialización entre callbacks, recompensas del tesoro, rechazo de reponer un contenedor roto y activación real de TNT por la apertura del cofre trampa. Se verificaron el generador de arañas de cueva y el cofre del greatwood, con rechazo a sustituir entidades de bloque existentes.

Un segundo proceso de servidor, con `generateStructure=false`, recuperó la pieza nativa y volvió a procesarla sin borrar el inventario marcado; conservó ambos tesoros, los dos generadores y el cofre/generador del árbol infestado. Se restauró la configuración después.

Con el filtro final de biomas, la búsqueda nativa encontró un túmulo en (-4297, 61, -3962) en el servidor y otro en (-4295, 58, -4103) en el cliente, ambos en `minecraft:plains`. Se comprobaron el interior con los dos tesoros y el cofre trampa, la sincronización y el exterior intacto. Se tomaron ambas capturas antes de generar el JAR; la visión nocturna solo pertenece a la escena de pruebas.

Compilación limpia `clean build test`: 187 pruebas, sin fallos, errores ni omisiones; 205 tablas de botín y 635 referencias de modelos validadas. El JAR conserva 336 recetas y 325 definiciones de objetos, sin clases de las pruebas nativas.

Artefacto: `thaumcraft-6.1.BETA26-port.50-forge66.0.13.jar`, 8980726 bytes. SHA-256: `59f1cce1a09c94e65ef0959776ea6c389f6017f69b7d2d19b03a2f7c5f9ab94b`.
