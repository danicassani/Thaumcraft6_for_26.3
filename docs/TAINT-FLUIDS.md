# Corrupción y fluidos — port.35

Esta entrega registra 310 recetas y 282 definiciones de objetos.

La corrupción depende de semillas vivas en chunks cargados. Se portan fibras
con conexiones y cuatro crecimientos, costra, tierra, roca, troncos, excrecencias
y géiseres. Semillas normales/grandes, reptadores, enjambres, tentáculos
normales/menores/gigantes y slimes usan entidades nativas persistentes. Se
conservan los IDs de los bloques, fluidos, efectos y entidades entre partidas.

## Ciclo de corrupción

El radio y la frecuencia proceden de `taintSpreadArea` y `taintSpreadRate`.
`wussMode` impide propagación y descarta criaturas corruptas; `mobGriefing`
impide propagación y colocación de goo por botellas. Las semillas aprovechan el
flux local y su impulso inicial. El terreno cercado por corrupción se convierte
según su tipo, evitando inventarios, bloques irrompibles y líquidos existentes.
El anillo exterior permite nuevas semillas pagando cinco flux. Al eliminar
las semillas, fibras desaparecen, tierra vuelve a tierra normal, roca deja
piedra porosa y las otras variantes se descomponen en goo. Si hay chunks
vecinos sin cargar, la degradación espera para no confundir una semilla
inaccesible con una semilla muerta. Ninguna de estas operaciones fuerza chunks.

La piedra porosa ofrece grava o el fondo ponderado de cristales compuestos,
ámbar, conglomerados de hierro/oro/cinabrio y materiales comunes. Fortuna
mejora la probabilidad y Toque de Seda conserva el bloque. Los metales de otros
mods no se añaden al fondo mientras su integración no esté portada.

Las criaturas corruptas son aliadas. La enfermedad hiere seres vivos no muertos
y cura criaturas corruptas. Las semillas hacen brotar tentáculos menores bajo
objetivos sobre terreno corrupto; los tentáculos pierden salud fuera de dicho
terreno. Los menores duran 200 ticks. Los slimes absorben goo, lanzan pequeños
slimes reduciendo su tamaño y se dividen al morir en ejemplares de tamaño uno.
El taumómetro reconoce sus aspectos y los descubrimientos originales, también
en las variantes que heredaban el escaneo en TC6. El comportamiento de combate se adapta a las metas/atributos nativos; la
representación usa segmentos animados y las texturas originales. El enjambre
se dibuja como un conjunto móvil de pequeñas criaturas.

## Fluidos y recetas

- **Flux goo:** ocho unidades máximas por celda, transferencia conservativa
  hacia abajo y después entre vecinos. Decae en flux o fibras y puede producir
  slimes, consumiendo la celda. No tiene cubo que permita fuentes infinitas.
- **Muerte líquida:** fluido Forge con fuente y corriente, sin multiplicación
  de fuentes. El contacto frena y causa cinco puntos de daño en fuente,
  disminuyendo con la corriente. Una muerte por disolución añade cristales de
  los aspectos conocidos de la criatura. Receta de crisol original: cubo,
  Mortuus 100, Alkimia 20 y Perditio 50; investigación LIQUIDDEATH.
- **Fluido purificador:** se obtiene soltando sales en agua fuente durante
  200 ticks. Una sal transforma una celda; se preservan las unidades sobrantes.
  Se transporta en cubo. Una fuente paga la protección contra la distorsión;
  no se consume otra mientras el jugador ya la tenga. Duración original
  `min(32000, 200000 / max(1, entero(raíz(distorsión permanente))))`.
  Conserva la distorsión almacenada. Sales: Salis Mundus, Cognitio/Aer/Ordo/
  Victus 40 cada uno; BATHSALTS. La pista oculta se descubre al acumular
  más de diez puntos de distorsión permanente y normal, conservando la
  investigación y sus requisitos.
- **Botella de corrupción:** lanzamiento físico y enfermedad en área, con
  colocación limitada de goo. Receta original: ampolla con exactamente diez
  Vitium, Vitium 30 y Aqua 30; BOTTLETAINT. Rechaza ampollas vacías, de otro
  aspecto o con otra cantidad.

Se consulta `BlockEvent.FluidPlaceBlockEvent` antes de gastar recursos o
modificar terreno, incluyendo ambas celdas de una transferencia de goo.
Las acciones con jugador comprueban además sus permisos de uso del terreno. Los fluidos
nativos esperan si su búsqueda de pendientes pudiera alcanzar chunks ausentes.
La propagación de agotamiento infeccioso respeta los equipos y el PvP entre
jugadores. El agotamiento incrementa en diez puntos porcentuales por nivel el
coste real de vis, compensando los descuentos del equipo. Las variantes de
agotamiento sobreviven al consumo nativo de leche: los eventos de uso conservan
sólo esos efectos, descontando el tiempo transcurrido y descartando las
capturas si se interrumpe el consumo. Los demás efectos se curan normalmente.

## Grietas y límites del alcance

Se conectan dos sucesos de grietas: conversión en gran semilla con impulso
igual al tamaño, y agotamiento infeccioso cercano. Se conservan sus pesos
originales y el espacio de los sucesos aún pendientes, sin aumentar su
frecuencia para compensarlos. Los sucesos de wisps y nubes de focos de la grieta
siguen pendientes, así como su equivalencia visual completa y la perla
primordial. La protección temporal del baño queda disponible para el sistema
de sucesos de distorsión, cuyo port completo no forma parte de esta entrega.
No se añaden biomas corruptos ni estructuras a chunks ya generados. Los
huevos de aparición y las tablas generales de aparición natural siguen en
el trabajo de criaturas/mundo; aquí las apariciones de juego proceden de
grietas, semillas, goo, géiseres y excrecencias. El cubo purificador usa el
modelo nativo de cubo con tinte, pues no existía una textura estática original.

## Verificación

Verificado el 21 de octubre de 2026 en Minecraft 26.3 / Forge 66.0.13 / Java 25.

- `clean build test --offline`: 154 tests, cero fallos, errores u omisiones.
- Servidor dedicado: propagación, conversión de piedra, protección de
  inventarios y eventos, conservación del goo, fuentes no infinitas,
  recogida de cubos y rechazo de corrientes, ampollas exactas, descubrimiento
  de sales, coste real de vis, leche, baño pagado sin eliminar distorsión,
  curación de slimes, daño y cristales por disolución, configuración y
  degradación tras retirar la semilla.
- Flujos programados y sales soltadas físicamente durante 200 ticks,
  conservando una unidad sobrante.
- Reinicio: terreno, fuentes, semilla e impulso exacto 37 conservados;
  condensador de la partida real de port.34 sigue disponible.
- Cliente nativo con Vulkan: mismas mecánicas, modelos y fluidos sincronizados,
  capas interiores/exteriores originales del slime, captura dentro del mundo,
  guardado, regreso al menú y cierre limpio. Se observó además una sesión de
  varias horas con propagación y crecimiento de slimes. Un cierre anterior
  solicitado antes de terminar el guardado activó el watchdog del entorno;
  se repitió esperando explícitamente al menú y la salida final fue correcta.
- Generadores completos en dos pasadas: segunda pasada sin cambios.
- Archivo ZIP sin duplicados ni fixtures; 310 recetas y 282 definiciones de
  objetos; etiquetas anteriores de herramientas preservadas.

La captura se compartió antes de compilar el JAR y antes de pushear.
SHA-256 de la captura: `0fd92f75511a48ef581fab07982113eec661254543d6b5aa5a3efec2d7e3ac46`.

Entrega: `thaumcraft-6.1.BETA26-port.35-forge66.0.13.jar` (8317214 bytes).
SHA-256: `c761910d9225eaf31bb8980254e6e9969fc4cfc858d067108a2e8d84083690d7`.
