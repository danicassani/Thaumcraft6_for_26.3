# Opciones de focos — port.18

Actualización port.74: Rotura ya usa trabajo gradual y pago local por bloque; véase [FOCUS-BREAKING.md](FOCUS-BREAKING.md). El resto de esta página describe la entrega original.

Minecraft 26.3 / Forge 66.0.13 / Java 25. Amplía M10 sin nuevos objetos, bloques ni recetas: 230 IDs de objeto, 118 bloques y 264 recetas.

## Uso

El manipulador tiene una cuarta página **Setup / Más**. Sus cuatro controles se aplican a la rama seleccionada en la segunda página; Fortuna y Seda afectan al último efecto de esa rama.

- **Velocidad:** niveles 1–5, equivalentes a nivel / 3 bloques por tick al salir. Los medios Proyectil nuevos empiezan en nivel 1. La inercia y la gravedad nativas modifican la velocidad durante el vuelo.
- **Normal:** impacto físico que ejecuta todos los efectos del programa.
- **Rebote:** refleja la componente normal a la cara, pierde velocidad y no ejecuta efectos sobre los bloques en los que rebota. Se consume al alcanzar una entidad o al quedar por debajo de 0,2 bloques/tick.
- **Busca enemigos / Busca aliados:** adquisición en un cono frontal y hasta 16 bloques, con comprobación de línea de visión, equipos, animales del propietario y PVP. Adquiere un objetivo en el primer tick y revisa candidatos cada cinco ticks y gira suavemente conservando el módulo de la velocidad antes de la inercia. Una pared bloquea el seguimiento y el impacto. Las opciones especiales requieren completar FOCUSPROJECTILE; el proyectil normal conserva el requisito FOCUSPROJECTILE@2.
- **Fortuna 0–IV y Toque de seda:** disponibles en Rotura e Intercambio. Los encantamientos se aplican al botín y experiencia nativos, con prioridad nativa de Seda cuando se combinan. Los eventos de rotura y las restricciones de modo de juego siguen activos. El objeto real de la mano principal se conserva, incluso si se cancela la rotura. Intercambio consume los bloques de la mano secundaria mediante la colocación nativa.

Los proyectiles nuevos duran como máximo 1200 ticks de tiempo del mundo, con 64 activos por propietario y dimensión. No entran en chunks descargados ni fuerzan su carga. Si el propietario está desconectado se pausan, conservando su plazo absoluto. La muerte, cambio de dimensión o modo espectador del propietario impide su ejecución. Se guardan programa, potencia, velocidad y plazo; no se renueva la duración al reiniciar.

**G** cambia los radios de Plan entre 0 y 3. **Ctrl+G** cambia el eje seleccionado: todos, X, Z, Y. La tecla puede reasignarse en Opciones. El guantelete guarda y sincroniza su selección; vista previa y lanzamiento usan las mismas dimensiones, sin recrear ni modificar el foco. Volumen/Superficie sigue siendo una propiedad del foco. El servidor solo recibe la acción y calcula los valores, comprueba el guantelete/foco/investigación y limita la frecuencia. No acepta dimensiones ni componentes enviados por el cliente.

## Costes y compatibilidad

Velocidad añade (nivel − 1) / 2 con división entera a la complejidad base 4 del proyectil. Rebote añade 3 y búsqueda añade 5. Estos costes se incluyen por rama antes del sobrecoste por repetir medio. Seda añade 4 por efecto; Fortuna añade (nivel + 1) × 3 si el nivel es distinto de cero. Se mantiene la complejidad base existente de Rotura e Intercambio; no se reproduce la expresión con precedencia defectuosa del antiguo código de Intercambio.

Los focos anteriores sin estos campos decodifican con Fortuna 0 y sin Seda. Los proyectiles sin modificar conservan su velocidad inicial anterior de 1,5, sin gravedad y su duración de 100 ticks, además del coste anterior. Cambiar explícitamente la velocidad adopta los niveles originales. Las ramas conservan sus ajustes al editar otros controles o cambiar de modificador. Los códecs rechazan niveles fuera de rango y Fortuna/Seda sobre efectos incompatibles.

La minería del hechizo usa temporalmente un pico nativo de netherita con los encantamientos elegidos durante `ServerPlayerGameMode.destroyBlock`, restaurando la referencia original de la mano en un bloque `finally`. No se añade esa herramienta al inventario ni se altera el equipo real. Sigue siendo la adaptación de Rotura por umbral de dureza del port; no reproduce todavía el trabajo gradual ni el pago adicional por bloque del original.

## Pendiente

M10 continúa parcial. Faltan grafos de mayor profundidad y secuencias de medios, bolsa de focos, vara pech, torretas, colapsador causal y gancho, además de efectos visuales originales. Esta entrega completa estos ajustes dentro del grafo actual de dos ramas y ocho efectos; no cambia ese límite.

## Verificación

Las fixtures se compilan solo con `-PthaumaturgyProbe=options-write`, `options-read` u `options-client`. No se incluyen en la compilación normal del JAR.

67 pruebas JUnit comprueban compatibilidad de programas antiguos, costes de opciones y efectos repetidos, cristales, límites de ajustes, persistencia por rama y ciclos de los cuatro ejes de Plan, además de la batería anterior.

Servidor dedicado (`work/focus-options-server-write.log` y `work/focus-options-server-read.log`): creación pagada de un grafo de 48 de complejidad, guardado a mitad del consumo de vis, conservación de los ajustes de las dos ramas, dimensiones del guantelete y programa/plazo de un proyectil en vuelo. Tras reiniciar, el foco termina sin repetir el pago. Marcadores `FOCUS_OPTIONS_WRITE_OK` y `FOCUS_OPTIONS_RESTART_OK`.

Cliente integrado (`work/focus-options-client5.log`): Seda devuelve el mineral de diamante; 24 minerales con Fortuna IV producen 65 diamantes mediante el botín nativo; la cancelación de la rotura no genera drops ni cambia el objeto real de la mano; bedrock y cofres permanecen protegidos; Intercambio con Seda coloca tierra, consume una unidad y devuelve piedra. Se verifican velocidad inicial, rebote sin afectar el bloque, programa/velocidad/plazo en una instantánea, daño a un enemigo desviado del eje, curación de un lobo del propietario y bloqueo por paredes.

La prueba pulsa G, Ctrl+G y G por el sistema nativo de entrada; los paquetes cambian los radios a 1/0/0 y el lanzamiento rompe únicamente los tres bloques elegidos. La selección llega al cliente y el foco programado permanece intacto. Se comprueba que las opciones especiales exijan completar la investigación y que el proyectil normal siga admitiendo la etapa 2. El manipulador crea el grafo completo y sincroniza su componente. Se inspecciona visualmente la cuarta página a 854 × 480 y se pulsan sus cuatro controles: velocidad, opción, Fortuna y Seda cambian y sincronizan la complejidad resultante. Marcadores `FOCUS_OPTIONS_PROJECTILES_OK`, `FOCUS_OPTIONS_KEYS_OK`, `FOCUS_OPTIONS_CLIENT_PLAN_SYNC_OK`, `FOCUS_OPTIONS_CLIENT_OK`, `FOCUS_OPTIONS_MENU_SYNC_OK` y `FOCUS_OPTIONS_MENU_EDIT_OK`.

La primera fixture de rebote observaba el proyectil después de que volviese hasta el propietario; se corrigió para observar y guardar el primer rebote. El caso del lobo descubrió un retraso de cuatro ticks en la adquisición inicial, corregido en producción: se adquiere en el primer tick. La repetición completa supera todas las fases. Un arranque intermedio no llegó al juego por SDL; se fijó X11 en el proceso de prueba. El servidor integrado guarda y finaliza; el proceso gráfico se termina después de comprobar que el hilo del servidor ha desaparecido, debido a la espera conocida del renderizador del entorno.

La cadena completa de generadores reproduce los recursos sin diferencias. El JAR normal excluye las fixtures.

Compilación limpia correcta: `work/focus-options-clean-build.log`, siete tareas ejecutadas y 67 pruebas sin fallos, errores ni omisiones. Entrega `thaumcraft-6.1.BETA26-port.18-forge66.0.13.jar`, 7748568 bytes; SHA-256 `17852564f12563bbe48ea772a05128c2efb8c50e3d3a5d9fe67ec28034d62ec6`.

Actualización: [port.19](FOCUS-GRAPHS-DEVICES.md) incorpora continuaciones de grafos, bolsa, vara pech, colapsador, gancho y dos torretas adaptadas. Véanse allí los límites y diferencias respecto a 1.12.
