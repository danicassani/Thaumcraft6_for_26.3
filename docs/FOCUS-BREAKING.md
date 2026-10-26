# Rotura mediante focos — port.74

El efecto de Perditio sigue requiriendo FOCUSBREAK y un medio compatible. No añade objetos ni cambia los focos guardados, recetas, investigación ni costes de programación. Intercambio conserva su ejecución anterior.

## Funcionamiento

El impacto inicia un trabajo de minería en el servidor, en lugar de comparar la dureza con `potencia × 4` y destruir inmediatamente. El trabajo inicial es `sqrt(dureza × 100)`; cada tick resta `potencia × multiplicador del hechizo`. La obsidiana puede minarse incluso con potencia 1, aunque tarde más. Los bloques con dureza cero terminan en el primer tick de trabajo; los de dureza negativa siguen siendo irrompibles.

Las grietas se transmiten mediante el progreso de destrucción nativo. Plan ordena los objetivos por distancia al impacto y retrasa cada uno `floor(trabajo / potencia efectiva / 3 × índice)`, como el original. Los modificadores conservan sus multiplicadores de potencia y cada bloque paga por separado.

El lanzamiento paga su vis habitual en la posición del jugador. Terminar cada bloque exige además aura **en el chunk del bloque**: `0,25 + (Seda ? 0,25 : 0) + Fortuna × 0,1` vis. No se aplica el descuento de equipo a este pago local, igual que en el original. Si falta aura, el trabajo se cancela; reponerla no lo reinicia automáticamente.

La cosecha utiliza el controlador nativo del jugador y la herramienta temporal ya usada por el port. Conserva Seda/Fortuna, botín y experiencia nativos, modos de juego y eventos Forge. El objeto real de la mano se restaura siempre. Se reserva el pago antes de cosechar y se devuelve si la rotura falla o se deniega; a diferencia del código antiguo, no se cobra una cosecha cancelada.

## Límites y guardados

Cada tick comprueba el bloque original, altura, borde, chunk cargado, permisos y que el jugador siga vivo, en la misma dimensión y fuera de espectador. Cambiar de objeto no cancela un hechizo ya lanzado. Se mantiene la exclusión del port para entidades de bloque, incluidos inventarios. Descarga, cambio de bloque, falta de aura, salida del jugador y vencimiento limpian las grietas sin cosechar.

Hay como máximo un trabajo por bloque, 512 por jugador y 2048 por dimensión. Lanzar de nuevo sobre un trabajo activo no lo reinicia ni duplica su botín. Los retrasos están limitados a 1200 ticks; cada trabajo puede actuar durante otros 1200 ticks. No se cargan chunks para completar trabajos.

La cola es transitoria, como en TC6: guardar y salir cancela la minería pendiente. El bloque queda intacto y puede recibir un nuevo hechizo al volver. No hay escrituras nuevas en mundos, inventarios o focos existentes. También se elimina una dependencia del orden de inicialización del códec de efectos mediante inicialización diferida, sin cambiar su representación serializada.

## Verificación

`-PbreakProbe=write runServer` comprueba minería gradual, obsidiana a potencia 1, pago exacto, exclusión de duplicados, Seda/Fortuna, denegación Forge con devolución del pago y conservación del equipo. También comprueba cambio de bloque, espectador, bedrock, inventarios, terreno descargado, agotamiento de aura y Plan lanzado mediante el guantelete nativo. `read` abre el mundo en otra JVM y comprueba que no se repite un trabajo pendiente y que se puede lanzar de nuevo.

`-PbreakProbe=client runClient` prepara un guantelete en supervivencia y espera un clic derecho físico. Comprueba los paquetes de grietas recibidos por el cliente, pausa el mundo para una captura antes de compilar el JAR, y al continuar verifica botín, guantelete y limpieza de grietas. Las fixtures quedan fuera del JAR normal.

Tres pruebas unitarias nuevas cubren dureza/potencia, escalonado y costes. El Thaumonomicon explica el proceso y el pago local en inglés y español; el generador conserva esos textos.

Resultado: 242 pruebas unitarias sin fallos; servidor dedicado y reinicio en otra JVM correctos; cliente nativo con clic físico, grietas intermedias, botín con Seda y limpieza de grietas sincronizados. Captura relevante realizada y compartida antes de generar el JAR de distribución.
