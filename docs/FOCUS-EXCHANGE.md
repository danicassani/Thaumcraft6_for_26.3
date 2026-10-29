# Intercambio mediante focos — port.75

Actualización port.76: la exclusión histórica de entidades de bloque se sustituye por soporte de contenedores y protección de datos desconocidos; véase [FOCUS-CONTAINERS.md](FOCUS-CONTAINERS.md).

## Uso

Equipa un foco con Intercambio en el guantelete. Agáchate y usa el guantelete sobre un bloque para guardar una muestra, sin consumirlo ni gastar vis. Puede estar en cualquier rama o continuación del programa. El foco y sus investigaciones deben ser válidos. La muestra aparece con su icono y nombre bajo el indicador de aura y en el tooltip del guantelete.

Al lanzar el foco, se buscan bloques iguales en el inventario principal y, después, en la mano secundaria. La coincidencia incluye los componentes del objeto. Cada sustitución consume una unidad; el botín del bloque retirado se recoge en el inventario y el exceso cae en el mundo. Fortuna y Seda utilizan la tabla de botín nativa y los encantamientos de la herramienta mágica. Se conserva la experiencia nativa de la versión anterior del port y se respeta la regla de botín de bloques.

Sin muestra guardada se conserva el comportamiento anterior de usar el bloque de la mano secundaria. Una muestra guardada tiene prioridad sobre esa alternativa. Seleccionar otro bloque reemplaza la muestra, no el foco. No se añaden objetos, bloques o recetas; el componente opcional `thaumcraft:picked_block` se guarda y sincroniza en el guantelete junto con su foco y demás componentes.

## Costes y sustitución

Intercambio no usa el trabajo gradual de Rotura: en TC6 la cola de sustituciones efectuaba un cambio por posición. El lanzamiento conserva su coste habitual y descuentos del equipo. Cada cambio válido añade un pago en el chunk del bloque: `0,25 + (Seda ? 0,25 : 0) + Fortuna × 0,1` vis. El pago local no usa descuentos. En creativo no se consumen materiales ni se cobra este pago adicional, como en el original; se mantiene el coste normal del lanzamiento del port.

Desaparece el umbral de dureza `potencia × 4`. La obsidiana se puede sustituir con un foco básico. Se conservan los campos serializados, límites y complejidad de los programas anteriores; no se reescriben los focos existentes.

La sustitución se prepara sin enviar paquetes, actualizar vecinos ni ejecutar `onPlace`. El evento Forge de colocación ve el estado de destino real y la instantánea del bloque original. Se comprueban ambos eventos, materiales, aura y permisos antes de cobrar. Una denegación restaura el original sin hueco, consumo o botín anticipado. El pago y el consumo se realizan una vez y después se publican los cambios y efectos nativos. No se cargan chunks y se rechaza la reentrada de otra sustitución durante los eventos de una operación activa.

Plan usa el mismo procedimiento por objetivo y se detiene por falta de materiales o aura sin deshacer las sustituciones anteriores ya pagadas. Un bloque idéntico no se modifica ni se cobra.

## Alcance

Se conservan las protecciones de modo de juego, construcción, borde, altura y eventos Forge. Se comprueban supervivencia del destino, orientación de colocación y colisiones. La muestra se obtiene mediante el paquete nativo de uso del guantelete; el cliente no envía un objeto o estado arbitrario para seleccionar.

Las entidades de bloque, inventarios, bloques de administración y estructuras que necesitan varias posiciones —puertas, plantas dobles y camas— quedan excluidas como origen o destino. No se promete intercambiar contenedores ni construir medias estructuras. Los bloques irrompibles tampoco pueden retirarse. El apoyo de una antorcha u otro bloque dependiente tiene que ser válido antes de cambiar el original.

El original utilizaba metadatos de 1.12 para algunos estados; esta adaptación usa contexto y componentes de colocación de 26.3. La experiencia se conserva del comportamiento previo del port, mientras que la cola antigua de Intercambio no la entregaba. Los efectos de impacto usan las primitivas gráficas modernas existentes.

## Verificación

`-PexchangeProbe=write test runServer` comprueba selección nativa sin coste, material fuera de la mano secundaria, pago local exacto, recogida del botín, bloque idéntico, cancelaciones Forge de rotura/colocación, obsidiana, Seda/Fortuna IV, contenedor intacto, bedrock, agotamiento de aura y material desde un evento, reentrada, terreno descargado, soporte inválido, estructuras dobles, alternativa antigua y creativo. Un lanzamiento real de Plan sustituye exactamente dos de tres bloques con dos materiales y paga exactamente el lanzamiento más dos sustituciones.

`read` abre el mundo en otra JVM y comprueba muestra, programa anidado y nombre personalizado guardados en un cofre real. Usa el guantelete recuperado para otra sustitución. `client` exige agacharse/clic derecho físicos para seleccionar y otro clic derecho para lanzar, con material en el inventario y un palo en la mano secundaria; verifica la sincronización de muestra, mundo y cantidades antes de la captura. Las fixtures se excluyen del JAR normal.

Resultado: 242 pruebas unitarias sin fallos; servidor dedicado, reinicio en una segunda JVM y cliente nativo completados. Selección y lanzamiento se realizan con clics físicos; la captura revisada y compartida muestra la muestra, el bloque cambiado, el botín recogido y la mano secundaria intacta antes de generar el JAR.
