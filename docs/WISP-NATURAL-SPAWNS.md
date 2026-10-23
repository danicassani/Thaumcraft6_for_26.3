# Wisps naturales — port.52

Los wisps existentes de las grietas se incorporan a la aparición normal de monstruos. Conservan `thaumcraft:wisp`, sus aspectos, vuelo, combate y cristal de botín.

## Aparición

- Bosque mágico: peso 20 y grupos de uno o dos, como `BiomeGenMagicalForest` original.
- Biomas de la etiqueta `minecraft:is_nether`: peso 5 y grupos de uno, como `ConfigEntities` original.
- Modificadores `forge:add_spawns` con el formato de 26.3: Forge adapta las listas al atributo ambiental nativo de aparición de criaturas. No se añade un temporizador que cree entidades alrededor del jugador.
- Colocación aérea sin exigir suelo, comprobaciones nativas de colisión y líquidos, luz original probabilística y límite local de ocho wisps vivos en 16 bloques. Cada ciclo admite como máximo dos wisps por grupo.
- La categoría `MONSTER` mantiene los límites y las distancias nativas del generador. `allowSpawnWisp=false`, `wussMode` o pacífico impiden nuevas apariciones naturales. Desactivar `allowSpawnWisp` conserva los existentes; el comportamiento anterior de `wussMode` y pacífico se mantiene.

La iluminación se comparte con las apariciones de grieta, conservando sus comprobaciones anteriores. La oscuridad por clima usa el cálculo nativo moderno. Desactivar la opción rechaza los candidatos inmediatamente, aunque la entrada ponderada siga presente en la tabla cargada; no necesita regenerar terreno ni reiniciar el mundo.

## Aspectos y guardado

Al finalizar un nacimiento nativo se elige un aspecto: 90 % de los casos entre los seis primarios y 10 % entre los compuestos, según la selección ya usada por las grietas. Un aspecto especificado o leído del guardado no se vuelve a sortear. Esto evita que el nuevo origen produzca únicamente wisps de aire.

No cambia el formato guardado, el ID ni el botín. La aparición normal puede funcionar también en chunks existentes, porque es fauna, sin reemplazar bloques o biomas. Los wisps naturales conservan el despawn nativo de monstruos; no se convierten en entidades permanentes. Cuando se guardan, mantienen aspecto, vuelo y estado de combate.

El bioma eerie y su población quedarán para su entrega, junto con pechs y otras criaturas. Esta entrega no implementa portales mayores, pretor ni jefes.

## Verificación

Los fixtures opcionales `wisp-server`, `wisp-read` y `wisp-client` están separados del JAR normal. Invocan el ciclo nativo de aparición sobre posiciones de biomas reales con los pesos publicados, conservando su elección aleatoria de criatura, distancias, colocación, comprobaciones de luz y finalización. Aceleran los intentos mediante la entrada de depuración del generador; no sustituyen la tabla por una de wisps ni construyen un wisp de reemplazo para la captura. El límite global de monstruos corresponde al llamador normal del juego, que no se modifica.

Los ejemplares ya nacidos se inmovilizan y se marcan persistentes únicamente en el fixture para inspección y prueba de reinicio. La visión nocturna del jugador de prueba no modifica la luz del mundo.

Servidor dedicado: pesos/grupos efectivos comprobados en las tablas nativas (bosque 20, 1–2; Nether 5, 1), y ejemplares reales con origen `NATURAL` en `(-1026, 83, -3199)` (Ignis, bosque mágico) y `(262, 48, 273)` (Aer, Nether). El del Nether deja físicamente un cristal de Aer al morir.

Pasan oscuridad y colocación aérea, rechazo a cielo abierto de día, agua y pacífico, las dos opciones de configuración, límite de ocho vivos, tamaño máximo de grupo de dos, variación de aspectos al finalizar el nacimiento y conservación de uno especificado.

Segundo proceso dedicado: el wisp natural de Ignis mantiene UUID, aspecto, vuelo y origen `NATURAL` con `allowSpawnWisp=false` durante la comprobación.

El primer fixture de cliente dejó sus ocho ejemplares de la prueba del límite lejos del jugador, por lo que desaparecieron por despawn antes de la aserción. Se marcaron persistentes dentro del fixture y se repitió. La prueba de desaparición normal no se sustituye por persistencia en el código del juego.

La segunda ejecución alcanzó el límite correctamente, pero el jugador murió por un clérigo conservado del escenario anterior antes de cambiar de zona. El fixture restaura salud y protege al inspector desde el evento de entrada, antes de preparar las comprobaciones.

Durante la siguiente ejecución apareció una desconexión real al emitir partículas de rotura de un multipart sin piezas seleccionadas. Los siete cristales y la fibra corrupta reciben ahora un modelo incondicional sin geometría, con su textura de partículas original. Esto cubre estados sin soporte/conexiones sin dibujar un bloque nuevo ni modificar sus formas. El generador final `generate_model_particle_fallbacks.py` debe ejecutarse después de los generadores de bloques. El verificador de recursos exige esta cobertura en esos modelos.

Cliente nativo: wisp natural de Aer en `(2748, 82, 2754)`, con estado sincronizado y captura compartida antes de generar el JAR final y de los pushes. Visión nocturna solo para inspección. SHA-256 de la captura: `5952aaaeefa9ca4be3bac30fe275de7d7666028cee4f7d98e66d2283122b2ea5`.

El evento que desconectaba al cliente correspondía exactamente a `crystal_aqua`, tamaño 2, generación 1, sin caras de apoyo. La comprobación nativa recorre los 7.488 estados de los siete cristales y la fibra corrupta, consulta sus materiales de partículas y ejecuta las partículas de rotura de los siete cristales sin apoyos. El verificador también se probó retirando el respaldo de Aer en una copia temporal: rechaza el recurso defectuoso.

Entrega final: `clean build test` completado; 187 pruebas, sin fallos, errores ni omisiones. Generadores estables en una segunda pasada y recursos verificados: 637 referencias de modelos, 336 recetas, 325 definiciones de objetos y 205 tablas de botín. El JAR excluye los fixtures opcionales.

Artefacto: `thaumcraft-6.1.BETA26-port.52-forge66.0.13.jar`, 8991404 bytes. SHA-256: `5226209731255b659c93762bafa4c280b85e5a2597e2117fc5124c142775f118`.

Actualización port.54: los pechs ya están implementados; véase [PECHS.md](PECHS.md). Las apariciones naturales de wisps están disponibles desde port.52.
