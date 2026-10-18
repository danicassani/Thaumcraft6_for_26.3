# Mesa arcana, cargador y sifón de vacío (port.7)

Minecraft 26.3, Forge 66.0.13 y Java 25. Se añaden tres bloques funcionales,
Salis Mundus y la entidad de grieta de flux. Este lote amplía M05 e implementa
el núcleo de M06 y la parte de grietas necesaria de M13; no completa investigación,
hechizos ni corrupción.

## Mesa y cargador

Salis Mundus se fabrica con tres cristales de **aspectos distintos**, redstone,
un cuenco y pedernal. El cuenco y el pedernal se devuelven. Aplicarlo a una mesa
de crafteo vanilla la transforma en mesa arcana. Esta transformación todavía no
exige investigación: M04 sigue pendiente.

La mesa conserva nueve casillas de ingredientes, seis casillas de cristales
primarios y una salida real. La casilla con borde violeta muestra la receta;
el botón Crear fabrica una unidad de receta. La salida solo contiene objetos
ya pagados. Shift mueve objetos entre la mesa y el inventario. Los contenidos
permanecen al cerrar el menú y se sueltan una sola vez al romper el bloque.

El servidor vuelve a resolver la receta al pulsar el botón y comprueba espacio,
ingredientes, aspectos y aura antes de gastar nada. Las recetas arcanas tienen
tipo propio `thaumcraft:arcane`, separado del crafteo vanilla. Admiten una receta
de crafteo anidada, coste de vis y cantidades de cristales por aspecto. Los datos
se recargan con el gestor nativo de recetas. También se pueden fabricar recetas
vanilla en la mesa, con sus recipientes devueltos y sin coste de vis.

Sin cargador, la mesa usa su chunk. Con el cargador colocado encima suma los
nueve chunks de un área de 3×3 y reparte el gasto. Solo consume unidades enteras
de cada chunk y conserva las fracciones; no carga terreno para obtener aura.
El cargador abre la mesa inferior al usarlo y se rompe con drop al perderla.
Se conservan el modelo y las UV originales, incluidos los 192 triángulos del OBJ.

Se incorporan siete recetas originales con sus costes: mecanismo simple (10),
mecanismo complejo (50), resonador de vis (50), cargador (200), pedestal de recarga
(100), generador de vis (25) y tela encantada (5). El resonador sigue siendo un
ingrediente; su inspección de redes de esencia depende de M08. La dioptra necesita
el taumómetro (M04) y la receta original del sifón necesita infusión (M09);
ambas permanecen pendientes. El sifón está disponible en creativo.

Faltan los desbloqueos por conocimiento, la presentación de recetas en el
Thaumonomicon, los descuentos del equipo y el manipulador de focos. El cargador
solo acepta la mesa arcana hasta que exista ese segundo consumidor.

## Grietas y sifón

La simulación de aura comprueba el umbral original de flux (>75% de la base) y
su probabilidad por segundo. Solo intenta crear una grieta por dimensión y ciclo,
en chunks de terreno y entidades ya cargados. Conserva la exclusión de 32 bloques,
la posición superficial y el coste inicial `sqrt(flux*3)`. El tamaño se limita a
100 para acotar la geometría, el trabajo por tick y los datos de red.
`world.generateFluxRifts=false` desactiva nuevas apariciones, sin borrar grietas.

La entidad guarda UUID, semilla, tamaño, estabilidad decimal y colapso. Su recorrido
determinista de dos ramas se comparte entre el dibujo y las colisiones. Tiene
un núcleo oscuro con costuras violetas, partículas nativas y sincronización del
servidor. Consume flux al crecer, pierde estabilidad con el tiempo, daña entidades,
absorbe objetos y elimina bloques rompibles de su recorrido. No carga chunks
vecinos para efectuar esas colisiones. El colapso reduce su tamaño, devuelve
vis/flux y deja semillas de vacío. La esponja creativa también elimina grietas.

El sifón trabaja una vez por segundo con grietas de tamaño mínimo 2 a distancia
máxima 8 y con visión directa. Añade `floor(sqrt(tamaño))` de progreso por grieta,
reduce estabilidad en `sqrt(tamaño)/15` y tiene la probabilidad original 1/33
de reducir su tamaño. Cada 2000 puntos produce una semilla; conserva el sobrante.
Se detiene con señal de redstone o con una pila de salida llena. La salida se
recoge con clic derecho o mediante `ForgeCapabilities.ITEM_HANDLER` por cualquier
cara; la automatización solo extrae, nunca inserta. Progreso y salida se guardan
en cada cambio y la salida se suelta al romper el bloque.

Siguen pendientes los eventos ponderados de wisps, semillas de corrupción,
nubes de flux y agotamiento contagioso, los efectos avanzados del colapso,
perlas primordiales y el shader artístico original. No se sustituyen esos
sistemas por efectos vanilla inventados.

## Verificación

Fixtures optativos en `src/arcaneRiftTest`, excluidos del JAR normal:

```sh
./gradlew -ParcaneRiftProbe=write runServer
./gradlew -ParcaneRiftProbe=read runServer
./gradlew -ParcaneRiftProbe=client runClient
./gradlew clean build
```

El mundo dedicado aislado es `arcane-rifts-smoke`. El fixture carga los chunks
de entidades y congela el tiempo simulado para comparar transacciones y valores
guardados exactos; el cliente prueba los tickers con el tiempo normal.
Las pruebas comprueban costes,
fallos sin consumo parcial, fracciones, cristales, recetas vanilla y recipientes,
clics repetidos, distancia, soporte del cargador, línea de visión, redstone,
producción/extracción de semillas, colapso y generación desde la simulación real.
El segundo arranque comprueba inventario, componentes, progreso, salida y entidad
guardada. La escena de cliente permite revisar modelos, grieta y menú y fabricar
mediante un paquete real de botón. Las evidencias permanecen en `work/`.

El catálogo resultante es 90 bloques, 63 objetos sin bloque, 153 IDs de item,
189 entradas creativas al contar aspectos y 119 recetas. La integración nueva
se verifica en Minecraft real; las 17 regresiones unitarias previas se conservan.

Resultados de port.7: `ARCANE_RIFT_RUNTIME_OK`, `ARCANE_RIFT_SPAWNING_OK` y
`ARCANE_RIFT_SAVE_READY` en `work/m07-server-write-final.log`;
`ARCANE_RIFT_RESTART_OK` en `work/m07-server-read-final.log`. El cliente confirma
`ARCANE_CLIENT_RIFT_SYNC_OK`, `ARCANE_CLIENT_MENU_SYNC_OK` y
`ARCANE_CLIENT_CRAFT_OK`: un resonador fabricado mediante el botón, ingredientes
y cristales reducidos de 4 a 3 y coste de 50 vis. El menú recibió un alcance de
nueve chunks y 1949 vis inicialmente. Se revisaron visualmente los tres modelos,
la grieta y el menú sin errores de modelos ni excepciones de renderizado del mod.
Tras guardar el mundo integrado, el NBT del sifón contiene 1168 de progreso:
su ticker automático funcionó con el tiempo normal, sin llamadas de prueba a
`harvest`. Capturas, logs y lectura NBT quedan en `work/`.

El entorno usa Vulkan por software tras fallar GLX. No dispone de audio y los
servicios online de Minecraft/Realms no están disponibles; esos mensajes del
entorno no se consideran verificaciones de audio o conexión online. El servidor
también conserva el aviso previo del appender kqueue en Linux.

Compilación limpia final: `work/m07-build-final.log`, BUILD SUCCESSFUL (28 s),
17 pruebas unitarias, cero fallos/errores. Inspección del JAR: 153 definiciones de
item y 119 recetas, sin clases `thaumcraft.test` ni recursos de fixtures.
