# Mesa de investigación y observación celeste (port.9)

Minecraft 26.3 / Forge 66.0.13 / Java 25. Continúa el núcleo de investigación de
[port.8](RESEARCH-SCANNING.md). No se declara terminada toda M04.

## Acceso y materiales

Abre Teorías en el Thaumonomicon. Fabrica herramientas de escritura con botella
de cristal, pluma y tinte negro. La mesa de madera usa tres losas de madera
sobre dos tablones en los extremos. Aplica las herramientas a la mesa: se
transfiere el objeto completo, incluida su tinta restante, y se registra la
prueba de fabricación. El libro permite completar THEORYRESEARCH.

La mesa tiene dos casillas persistentes, para herramientas y papel. Las
herramientas conservan el recipiente al agotar sus 100 cargas. La receta de
recarga combina un recipiente usado con tinte negro, preservando sus otros
componentes. Las herramientas nuevas no aceptan esa receta. Las recetas nativas
registran las pruebas de fabricación del sistema existente.

## Teorías

Una sesión pertenece al UUID de quien la inicia. Se puede cerrar la interfaz y
retomarla tras desconectarse o reiniciar el servidor. Otros jugadores pueden
consultar la mesa pero no jugar, finalizar ni abandonar esa sesión. Romper la
mesa devuelve sus dos contenidos y descarta la teoría pendiente sin recompensarla.

La inspiración inicial conserva la fórmula original: 5, +0,5 por investigación
SPIKY completa, +0,1 por HIDDEN completa, redondeada y limitada a 15. Las ayudas
seleccionadas restan una inspiración cada una. El servidor comprueba que existen
en un radio horizontal de cuatro bloques y vertical de uno, sin cargar chunks.
Hay ayudas para la mesa arcana, encantamientos, baliza, glifos antiguos y los
portales del Nether y End. Sus cartas adicionales son finitas durante la sesión.

Robar cuesta un papel y presenta hasta dos cartas elegibles. Un robo extra permite hasta tres. Hay que
jugar una de las opciones antes de robar de nuevo; cerrar o repetir un paquete no
vuelve a sortearlas. Jugar cuesta una tinta y la inspiración de la carta; Scripting
requiere papel y tinta adicionales. Los objetos exigidos se muestran al pasar el
ratón; algunas cartas los conservan, otras consumen cantidades y variantes exactas.
Las cartas con niveles de experiencia, observaciones o warp validan su coste antes
de modificar inventario, conocimiento o sesión. `wussMode` omite el warp concedido.

Al agotar la inspiración se puede terminar. Los porcentajes se convierten a puntos
brutos de teoría con `round(porcentaje / 100 * 32)`. Se conserva la reducción a dos
tercios para categorías secundarias y las cartas que retrasan esa penalización.
Los empates se ordenan por clave para que el resultado sea estable al reiniciar.
Los puntos permiten pagar las etapas del libro; no desbloquean otras mecánicas.

Se incluyen 27 cartas: Experimentation, Analyze, Balance, Ponder, Rethink, Reject,
Inspired, Calibrate, Focus, Measure, Scripting, Concentrate, Reactions, Synthesis,
Tinker, Mind over Matter, Celestial, Spellbinding, Awareness, Realization,
Revelation, Truth, Synergy, Enchantment, Beacon, Glyphs y Portal. Las categorías
normalmente disponibles comprueban el desbloqueo original de su rama; las ayudas
mantienen la posibilidad original de aportar cartas de otras ramas. Experimentation
conserva sus resultados en categorías aleatorias. Las combinaciones de aspectos,
consumo y producción de cristales y efectos están resueltos en el servidor.

No se introducen sustitutos para cartas que necesitan focos, infusión, frascos de
esencia, curiosidades o cerebros en frasco aún ausentes: Channel, Infuse, Sculpting,
Curio y Dark Whispers quedan con sus mecánicas. Study y Notation, marcadas en el
original como exclusivas de ayudas sin proveedor activo, tampoco se añaden al
mazo normal. No se implementa todavía una API extensible para cartas externas.

## Cielo y notas

Completa CELESTIALSCANNING en el libro (observaciones de Básicos, Artificio y
Auromancia). Usa el taumómetro mirando al cielo abierto del Overworld, con papel
y herramientas con tinta en el inventario. Debe verse el cielo y no llover. El
sol y la luna se apuntan con una tolerancia angular de siete grados; de noche
se pueden observar también las estrellas de norte, sur, este y oeste.

Se leen SUN_ANGLE, MOON_ANGLE y MOON_PHASE de los atributos nativos del entorno,
por lo que se sigue el cielo real de 26.3. Cada resultado cuesta un papel y una
tinta, y produce una de trece variantes: sol, cuatro direcciones y ocho fases
lunares. Cada variante se obtiene una vez por día de tiempo de juego del servidor
(`gameTime / 24000`, igual al totalWorldTime usado por TC6); cambiar el reloj con
un comando no reinicia ese límite. El registro se conserva por jugador y elimina
las claves de días anteriores para no crecer indefinidamente.

Las notas usan un componente persistente y sincronizado, modelos y texturas
originales, nombres en español/inglés y una identidad de escaneo por variante.
La carta Celestial consume dos notas distintas y aplica los bonos originales de
sol/luna/estrellas, incluido el warp cuando corresponde.

## Autoridad y guardado

El cliente solo solicita acciones del menú nativo; no envía cartas, semillas,
porcentajes ni recompensas. Cada acción incluye una revisión de la sesión. Una
petición anterior se rechaza tras cualquier modificación. Se verifican distancia,
bloque, propietario, investigación, tinta, papel y costes en el hilo del servidor.
El codec de sesión tiene versión, límites y categorías validadas. Un tag ilegible
o futuro se conserva sin sustituirlo por una sesión vacía y bloquea acciones.

## Verificación

Fixtures optativos (excluidos del JAR normal):

```sh
./gradlew -PtheoryProbe=write runServer
./gradlew -PtheoryProbe=read runServer
./gradlew -PtheoryProbe=client runClient
./gradlew clean build
```

Las pruebas usan el mundo aislado `theory-smoke`; no convierten partidas antiguas.
Los registros y capturas permanecen en `work/` y no se publican en la rama de JAR.

Resultados del 18 de octubre de 2026: `THEORY_RUNTIME_OK` en servidor dedicado,
con las 27 cartas inicializadas y resueltas dos veces desde la misma semilla
para comprobar estabilidad. Se verificaron creación y prueba de fabricación de
la mesa, ayudas, pagos, propiedad, rechazo de paquetes repetidos, fallo sin
gastos parciales, notas exactas, recarga y observaciones de sol/estrellas/luna.
`THEORY_RESTART_OK` confirmó el bloque y el archivo de jugador en otro proceso:
propietario, tinta 7/100, 13 papeles, dos cartas pendientes con su semilla y
32 puntos brutos de teoría de Básicos. Ambos servidores se cerraron con `stop`.
La escena cliente confirmó la sincronización del menú y las peticiones nativas
de empezar, robar y jugar (`THEORY_CLIENT_MENU/START/DRAW/PLAY_OK`). La primera
carta de la prueba consumió una tinta y dos inspiraciones y dejó 34% de Básicos;
las trece variantes de notas se renderizaron sin modelos ausentes. Los
generadores de conocimiento y teorías son idempotentes al ejecutarlos en orden.
La compilación limpia terminó correctamente con 28 pruebas y cero fallos.
El JAR normal contiene 92 bloques, 159 IDs de item y 124 recetas y excluye los
fixtures de integración.
