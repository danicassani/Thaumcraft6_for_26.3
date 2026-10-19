# Spellbat y Rift — port.17

Minecraft 26.3 / Forge 66.0.13 / Java 25. Añade un medio, un efecto, una entidad y el bloque interno `hole`; este último no tiene objeto, receta ni entrada creativa. Se mantienen 230 IDs de objeto y 264 recetas; ahora hay 118 bloques registrados.

## Spellbat

Selecciona el medio Spellbat en el manipulador. Su coste base es 8 de complejidad y un cristal bestia. En la segunda página Setup / Más, elige amigos o enemigos. Admite Scatter y ambos Split, conservando la reducción de potencia de cada modificador.

El guantelete invoca un murciélago con propietario y programa propios. Busca el objetivo visible más cercano hasta 12 bloques y se acerca mediante vuelo con colisiones; si pierde la línea de visión, deja de atacarlo. Sin objetivo sigue a su propietario. Los efectos se aplican con potencia 0,33, multiplicada por el modificador del foco, como en el original.

Cada bat tiene cinco vidas, como máximo cinco aplicaciones de efectos y un intervalo de 40 ticks entre ellas. Cada aplicación consume una vida y un uso. También puede recibir daño normal; no deja botín. Su plazo máximo es de 600 ticks de tiempo del mundo. Se limita a 16 murciélagos por propietario y dimensión.

Amigos incluye al propietario, sus equipos y animales domesticados. Un bat de curación amiga busca aliados heridos; los enemigos excluyen aliados, jugadores creativos y jugadores protegidos por PVP. Los bats de otros propietarios aliados también se consideran aliados al aplicar efectos de focos. Sin propietario conectado/vivo en su dimensión, o si este es espectador, el bat queda inactivo y su plazo sigue transcurriendo.

Se guardan programa, propietario, modo, salud, usos restantes, potencia del modificador, caducidad y próximo ataque. Guardar/reabrir no renueva la duración ni los usos. La representación usa el modelo animado y los sonidos nativos de murciélago con partículas mágicas; la apariencia original específica sigue pendiente.

La investigación conserva FOCUSMINE y el escaneo de murciélago. Adaptación explícita: se sustituye el requisito del Firebat todavía no portado por **escanear un blaze nativo**. Se mantienen los costes de teoría y la distorsión de FOCUSSPELLBAT.

## Rift

Selecciona Rift como efecto. El botón de potencia se convierte en **Profundidad**: 8/16/24/32. Duración permite 2–10 segundos. Complejidad del nodo = 3 + duración / 2 + profundidad / 4, con división entera; consume un cristal alienis y requiere FOCUSRIFT. Se conservan sus padres FOCUSBREAK/FOCUSEXCHANGE, descubrimiento de vacuos, observación eldritch, teoría y distorsión.

En un impacto de bloque, abre un túnel de hasta 3 × 3 por la profundidad seleccionada, hacia dentro de la cara apuntada. La profundidad se reduce con la potencia del modificador. Se detiene al encontrar un bloque central no elegible o protegido; los laterales se comprueban individualmente. Rift no tiene efecto sobre impactos de entidades: un Spellbat entrega impactos de entidad, por lo que para combinar ambas mecánicas conviene usar dos ramas con medios propios.

Adaptación de terreno: admite bloques opacos con colisión completa, dureza no negativa, sin inventarios/entidad de bloque, fluidos, caída, pistones ni bloques de administración. La etiqueta ampliable `thaumcraft:rift_immune` excluye además bloques inmunes.

Cada sustitución comprueba borde/altura, chunks ya cargados, permisos del jugador y los eventos Forge de rotura y colocación. No ejecuta cosecha ni genera drops, experiencia o consumo de bloques. El hueco es luminoso y atravesable, mantiene soporte para bloques vecinos, y no puede obtenerse, romperse, moverse con pistones ni reemplazarse por fluidos o colocación normal.

## Restauración

Un registro SavedData por dimensión guarda posición, estado exacto original, propietario y plazo absoluto **antes** de sustituir cada bloque. A los dos segundos, por ejemplo, un tronco orientado vuelve con su mismo eje. La restauración no depende de que el propietario siga conectado.

El registro permite como máximo 288 posiciones por propietario y 1024 por dimensión. Es un presupuesto de huecos pendientes, incluidos los chunks descargados. Al agotarlo no se añaden más posiciones. Los solapamientos no reemplazan el estado guardado ni renuevan plazos.

Cada cinco ticks se restauran los huecos vencidos de chunks cargados. Los descargados conservan su registro y se restauran después de cargar, sin forzar chunks. Guardar y reiniciar conserva el plazo: no concede otros diez segundos. Si un comando u otra mecánica ha sustituido expresamente un hueco, se respeta esa edición y se retira su registro. Un archivo de versión futura, entradas repetidas, incompletas, con inventarios o que se restauren a otro hueco se rechazan sin sustituir el archivo por datos vacíos.

## Pendiente de M10

Grafos de mayor profundidad, variantes completas de trayectorias, fortuna/seda, ajustes de Plan por teclas, bolsa de focos, vara pech, torretas, colapsador causal y gancho. La dimensión Exterior y sus restricciones originales no existen todavía en este port. Estas dos mecánicas no completan M10 ni el contenido de criaturas.

## Verificación

61 pruebas JUnit: compatibilidad anterior, coste/aspectos y modo de Spellbat, todas las combinaciones de profundidad/duración de Rift y rechazo de datos inválidos sin excepciones, además de la batería previa.

Fixtures excluidas del JAR normal: `-PthaumaturgyProbe=bat-rift-write/bat-rift-read/bat-rift-client`. Servidor dedicado (`work/bat-rift-server-final2-write.log`, `work/bat-rift-server-final2-read.log`): cupos por propietario y dimensión, codec exacto y rechazo de duplicados/versiones futuras/inventarios, investigación disponible, foco pagado con dos ramas y ajustes bloqueados, guardado de murciélago pausado, conservación de estado/plazo de Rift y respeto de ediciones posteriores. Un hueco lejano vence mientras su chunk permanece descargado; conserva el registro sin cargarlo y se restaura al cargarlo expresamente. El foco termina sin segundo pago.

Cliente integrado (`work/bat-rift-client.log`): aura única e investigación obligatoria; denegaciones de rotura/colocación, parada en protección central, exclusión de inventarios/bedrock/vidrio/arena/agua/pistones, soporte de antorcha sin drops, imposibilidad de minar el hueco o reemplazarlo por agua, restauración exacta y límite de 32 capas/288 huecos. Murciélagos: persecución, potencia 0,33, intervalo de 40 ticks, snapshot de usos/cadencia/salud, daño externo, curación amiga con exactamente cinco usos, selección por línea de visión y protección de jugadores por PVP/creativo. Foco combinado completado y sincronizado.

Inspección gráfica a 854 × 480: controles de profundidad/duración y segunda rama visibles; entidad sincronizada y representada con modelo nativo y partículas. No se certifica equivalencia visual con el renderer original. Los escenarios son acotados: no equivalen a reproducir todo el grafo y todas las interacciones de otros mods.

Compilación limpia correcta (`work/bat-rift-clean-build.log`): 61 pruebas ejecutadas sin fallos, errores u omisiones. JAR normal sin `thaumcraft/test/`, con SpellBat, RiftData, RiftHoleBlock y etiqueta de inmunidad; 264 recetas y ningún objeto para el hueco. Entrega `thaumcraft-6.1.BETA26-port.17-forge66.0.13.jar`, 7 730 712 bytes. SHA-256 `6c2368543355ec1d00bd0e9968163725b5e57461ff324c000149421506e783e6`.
