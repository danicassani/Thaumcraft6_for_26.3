# Thaumonomicon y mesa arcana — port.56

Esta actualización sustituye las recetas de texto por páginas con los objetos reales, su posición, resultado, cristales, aspectos y coste de vis. Los tooltips muestran los nombres exactos: el taumómetro utiliza cuatro lingotes de oro y un **panel de vidrio**, no un bloque de vidrio ni lingotes de hierro. Cuesta 20 vis y un cristal de cada aspecto primal. Los datos de las ilustraciones se generan de las recetas modernas utilizadas por el servidor, sin mantener otra receta independiente escrita a mano.

`salismundusfake` era el identificador de una receta ilustrativa del TC6 original (`addFakeCraftingRecipe`), no un objeto que el jugador deba fabricar. Se conserva como alias interno para las investigaciones guardadas y se presenta como Salis Mundus, con tres cristales de aspectos distintos, redstone, pedernal y cuenco. Se indica que se devuelven el pedernal y el cuenco.

El catálogo gráfico incluye las recetas registradas de fabricación, mesa arcana, crisol, infusión y horno, grupos de variantes y los casos especiales de Salis Mundus, mejora de tarros y depósito de esencia. Los resultados dinámicos de infusión muestran el equipo mejorado, no el libro usado como marcador en el serializador. Las referencias antiguas a procesos sin receta estática se presentan como instrucciones; no se inventa una receta a partir de su ID. Se recuperan las dos recetas ordinarias originales de piedra arcana y sus ladrillos que faltaban en el catálogo de fabricación.

## Descubrimiento y progreso

Solo Fundamentos aparece al empezar. Los otros seis capítulos requieren completar sus entradas `UNLOCK…`, como en `ConfigResearch` de TC6. Los temas no descubiertos se muestran cuando se cumplen sus padres; los que el jugador ya ha iniciado o descubierto siguen visibles. El tema en curso se ordena primero. Elegir un tema disponible abre su primera etapa; no se necesita confirmar una página vacía antes de poder leerlo.

Cada etapa muestra primero sus requisitos, con marcas de los ya cumplidos. Los objetos usan sus nombres traducidos en lugar de IDs; también se conservan los requisitos especiales de encantamientos. Las observaciones y teorías se presentan en unidades de conocimiento, con el progreso hacia la siguiente en el tooltip, sin los divisores internos de 16 y 32. Los datos siguen guardados con el mismo formato de conocimiento versión 1.

Abrir el libro inicia Primeros pasos, pero no desbloquea el crisol. Primero hay que terminar Primeros pasos y llegar a la segunda etapa de Alquimia básica; esa era ya la condición correcta del servidor. Se corrige el mensaje que, ante ese bloqueo, pedía volver a fabricar y abrir un libro que el jugador ya tenía.

## Salida nativa de la mesa arcana

Se retira el botón Craft y se utiliza una única casilla de resultado, que se recoge con clic normal o Mayús. La previsualización no gasta recursos; al recogerla, el servidor comprueba de nuevo investigación, ingredientes, cristales y vis y paga una sola vez. Si no hay hueco al usar Mayús, no se consume la receta. Un resultado parcialmente recogido queda almacenado como material ya pagado; puede recuperarse después sin cobrar otra vez. También se conservan los resultados pendientes de mesas guardadas con el sistema antiguo.

La mesa distingue receta inexistente, investigación pendiente, cristales insuficientes y falta de vis. Un bloque de vidrio o hierro en la receta del taumómetro deja la salida vacía y no consume nada. Se quitan la segunda casilla y el texto «1 chunks» de la pantalla. Este último era un detalle de alcance de la consulta de aura (un chunk local, nueve con cargador), no un ingrediente ni una condición adicional de fabricación. El alcance del cargador no cambia.

## Verificación

Las fixtures optativas `-PbookProbe=write runServer` y `read` comprueban la creación real de libro y mesa con Salis Mundus, las etapas originales, los capítulos y temas aún no descubiertos, los dos ingredientes incorrectos reportados, clic normal y Mayús mediante `AbstractContainerMenu.clicked`, el pago exacto de vis/cristales, intentos repetidos, inventario lleno, resultado parcial y transformación del crisol con su investigación. Un archivo de jugador real y el mundo guardado recuperan el progreso y cuatro unidades de resultado ya pagado en otro proceso.

`-PbookProbe=client runClient` valida la sincronización de capítulos, el panel de vidrio y el coste de la ilustración. La prueba de fabricación usa un clic nativo real en la salida y exige ingredientes/cristales consumidos y la prueba de fabricación registrada para Primeros pasos. Las fixtures y sus mundos no se incluyen en el JAR.

### Client shutdown limitation

The final native client run passed recipe rendering, native output pickup and all nine M16 renderer assertions. Closing the X11 window subsequently triggered the native client shutdown watchdog in `VulkanGpuSurface.present` / `vkQueuePresentKHR` (2026-10-24), after the integrated server began saving players and worlds. This run is not claimed as a clean client shutdown. Dedicated-server shutdown and cold-restart persistence checks passed separately.

## Disposición original — port.62

La mesa utiliza la textura original `arcaneworkbench.png`, con el tablero verde y el marco de madera. La cuadrícula está en (40,40), con separación de 24 píxeles; los cristales rodean el tablero en el orden original —aire arriba, fuego/agua en las esquinas superiores, tierra/orden en las inferiores y entropía abajo—. El resultado está en (160,64) y los inventarios conservan sus índices y contenidos. Los halos giratorios marcan los cristales requeridos y la reserva/coste de vis aparece junto a la salida. Se mantiene la recogida nativa del resultado y el pago del servidor.
