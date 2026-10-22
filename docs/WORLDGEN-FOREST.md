# Generación de mundo: bosque mágico y árboles (port.49)

## Alcance

Bioma `thaumcraft:magical_forest`, integrado en el preset climático estándar del Overworld antes de crear los niveles. Se seleccionan celdas completas de bosque templado superficial mediante un hash estable; océanos, desiertos, cuevas y demás climas conservan sus entradas. El peso usa la proporción `peso / (50 + peso)` como umbral del hash sobre esas celdas, una adaptación al clima multidimensional moderno: no es un porcentaje de todo el Overworld ni reproduce la distribución de semillas de 1.12.2.

`world.generateMagicForest=false` o peso cero desactivan las nuevas áreas; ambas opciones requieren reiniciar el mundo/servidor. El registro del bioma sigue disponible para leer chunks guardados. El preset se mantiene serializado como `minecraft:overworld`: no se sustituye por un generador de dimensión propio. Un access transformer limitado a `MultiNoiseBiomeSourceParameterList.parameters` y `BiomeSource.possibleBiomes` permite reconstruir su índice climático y la caché de biomas antes de crear los niveles. También se invalida el índice de decoración mediante el método nativo de Forge `refreshFeaturesPerStep`, porque el menú de selección del mundo puede haberlo evaluado antes. Los generadores de otros mods que no utilicen ese preset conservan su fuente de biomas.

## Árboles y vegetación

Greatwood recupera el tronco 2×2, dos pisos de copa y ramas de altura variable. Silverwood recupera el tronco en cruz de cinco columnas, raíces, brazos superiores y copa redondeada irregular. El roble del bosque mágico usa una copa ramificada alta de un solo tronco. La geometría se deriva de los generadores originales; las obstrucciones se comprueban antes de escribir y las hojas reciben distancias nativas calculadas sobre el follaje realmente colocable. Se omiten las hojas sin conexión a seis pasos de un tronco, para respetar el ciclo moderno de caída de hojas.

Las ramas no sustituyen bloques sólidos. La generación falla sin colocar partes del árbol cuando encuentra un obstáculo obligatorio o rebasa la altura del mundo. El follaje puede adaptarse a obstáculos sin destruirlos. Los brotes usan estas mismas formas y siguen creciendo aunque `generateTrees` desactive la generación natural.

El bosque tiene dos intentos de árbol por chunk: silverwood 1/18, greatwood 1/12 de los restantes y, en otro caso, roble mágico. Conserva además los intentos escasos de greatwood/silverwood de los modificadores de bosques existentes. Hay césped ambiental, vishrooms junto a troncos y shimmerleaf junto a silverwoods naturales. La flora y decoración vanilla conservan la compatibilidad con el terreno y las menas actuales.

Los colores predeterminados originales son césped 5635969, follaje 6750149 y agua 30702. La base de aura aplica el promedio original de las categorías bosque y mágico: 0,625. Se conservan los animales y monstruos vanilla, con lobos, caballos, brujas, endermen y vex de los pesos originales.

## Partidas existentes

No hay regeneración automática: ni se recolorean ni se reconstruyen los biomas de chunks guardados, ni se sustituyen sus bloques o aura persistente. Los nuevos biomas y formas aparecen en terreno sin generar; los brotes ya plantados crecen con las formas nuevas. Pueden verse transiciones en la frontera entre terreno antiguo y nuevo. Cambiar las opciones de generación también puede producir esas transiciones.

## Pendiente de generación de mundo

Esta entrega no completa toda M01/M16. Quedan estructuras con tesoros, greatwoods infestados con arañas/botín, variantes de decoración como rocas y hongos grandes, opción visual azul, aparición natural de pechs/wisps con sus opciones, bioma eerie, contenido eldritch y dimensión. Se conectarán por entregas; no se marcan como implementados por tener un ID registrado.

## Verificación

Prueba nativa de servidor dedicado: crecimiento desde ambos brotes, troncos y ramas con ejes, copas con distancias de hojas válidas, rechazo sin escrituras parciales de obstáculos y del techo del mundo. La búsqueda climática encontró un bosque natural en (-1344, -1472); la inspección de 25 chunks contó 1904 troncos de roble, 986 troncos mágicos y 21 bloques de césped ambiental. La selección predeterminada ocupa 29 celdas climáticas originales de bosque superficial.

Los generadores de recursos completos terminaron dos pasadas sin cambios en la segunda, incluido el nuevo generador de bioma. Las pruebas y escenas nativas viven en `src/worldgenTest` y solo se compilan con `-PworldgenProbe`; no se incluyen en el JAR normal.

Un segundo proceso de servidor, con `generateMagicForest=false`, confirmó que la fuente climática ya no ofrece nuevos bosques mágicos y que se conservan el bioma guardado, su árbol natural y el árbol crecido desde brote. Se restauró la configuración después de la prueba.

La revisión visual detectó y corrigió la ausencia del tinte de greatwood: el bloque vuelve a usar el color del follaje de su bioma y el objeto el color básico nativo. Silverwood conserva el tinte blanco original sobre su textura pálida.

La ejecución final del cliente encontró otro bosque natural en (2752, 2752), con 2608 troncos de roble, 515 troncos mágicos y 23 bloques de césped ambiental en los 25 chunks inspeccionados. Se verificaron los árboles nativos y la sincronización del bioma al cliente. La captura revisada, con greatwood tintado, se realizó antes de generar el JAR final.

Compilación limpia `clean build test`: 183 pruebas sin fallos, errores ni omisiones; 205 tablas de botín y 635 referencias de modelos validadas. El JAR conserva las 336 recetas y 325 definiciones de objetos e incluye el registro de colores y los dos access transformers; no contiene clases de las pruebas nativas.

Artefacto: `thaumcraft-6.1.BETA26-port.49-forge66.0.13.jar`, 8957615 bytes. SHA-256: `922e9b6cebd3f3498db959f8536323c146914ca9d52f957566d4af01adafc208`.
