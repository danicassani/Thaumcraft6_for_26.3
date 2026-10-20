> Estado actualizado de hornos y fuelles en port.30: [ADVANCED-SMELTERS.md](ADVANCED-SMELTERS.md). Este documento conserva las pruebas y límites de su entrega original.

# Red de esencia: port.11

Minecraft 26.3 / Forge 66.0.13 / Java 25. Se implementa el transporte físico de
M08 antes de añadir cartas nuevas. El mazo sigue teniendo 27 cartas.

## Montaje

Investiga `TUBES`, después de la destilación y los tarros, y fabrica los tubos en
la mesa arcana. Un alambique entrega esencia por arriba y los laterales excepto
su cara frontal; no la recibe por tubos. El tarro se conecta **por arriba**.
Coloca tubos entre una salida del alambique y la parte superior del tarro. El
horno sigue exportando a su columna de alambiques como en port.10.

Los tubos normales guardan una unidad. Cada cinco ticks intentan tomar una
unidad de un vecino con menor succión; los tarros también toman una unidad cada
cinco ticks. Solo se opera en chunks cargados y se comprueban las dos caras de
cada conexión. Las operaciones simuladas no consumen ni producen esencia.

| Destino | Succión | Succión mínima para extraer de él |
| --- | ---: | ---: |
| Tarro sin etiqueta, con sitio | 32 | 32 |
| Tarro etiquetado, con sitio | 64 | 64 |
| Tarro normal lleno | 0 | 32 o 64 |
| Tarro del vacío etiquetado, con sitio | 48 | 48 |
| Tarro del vacío lleno o sin etiqueta | 32 | 32 o 48 |
| Alambique | 0 | 0 |

La succión se recalcula cada dos ticks. Un tubo normal resta uno a la demanda
más fuerte compatible. Esto limita la distancia y permite dar prioridad a
ciertos destinos. Los tipos no se mezclan en tubos normales ni en tarros.
Dos demandas distintas de fuerza igual o casi igual pueden provocar venteo:
el tubo se detiene durante 40 ticks, emite humo y conserva su contenido.
Separar las ramas o filtrar cada una evita el conflicto. No es una red global
que teletransporte contenido; la esencia permanece en los bloques intermedios.

## Variantes y controles

- **Normal:** transporte de una unidad y conexiones por las seis caras.
- **Válvula:** clic con mano vacía abre/cierra; el flanco que tiene el mando no
  conecta. Un flanco de subida de redstone cierra y uno de bajada abre, conservando
  el control manual entre cambios de señal. Cerrada bloquea succión y transporte.
- **Filtro:** usar un frasco lleno o una etiqueta escrita copia su aspecto sin
  consumir el objeto. Agacharse con mano vacía borra el filtro. Si contiene otra
  esencia hay que vaciarlo antes de cambiar a un aspecto incompatible.
- **Restrictor:** divide por dos la succión recibida en lugar de restar uno.
- **Direccional:** recibe por las caras distintas de la flecha y entrega solo
  por la cara señalada. Solo propaga demanda desde ese destino.
- **Depósito:** conserva hasta diez unidades repartidas entre varios aspectos;
  su succión base es uno; sin fuelles debe estar junto a una salida de
  alambique para alimentarse, porque no atrae a través de un tubo normal. Entrega a destinos compatibles y respeta demandas más
  fuertes en otras caras. Agacharse con mano vacía alterna la restricción de la
  cara: 0 normal, 1 limitada y 2 sin succión. Los modos 0 y 1 tienen fuerza uno
  mientras no se implemente el refuerzo por fuelles.

El **resonador de esencia** muestra contenido, succión y tipo demandado al usarlo
sobre un tubo, tarro o alambique. También informa de filtros, conflictos y modo
de restricción. Agacharse y usarlo sobre un brazo de tubo abre/cierra esa cara,
y refleja el ajuste en el tubo vecino. En el centro de una válvula o tubo
unidireccional rota el mando/flecha entre las seis direcciones.
Las recetas restrictora y direccional comparten cuadrícula; la mesa elige según
los cristales disponibles (terra o aqua). Si hay ambos, el orden por ID es estable.

Los modelos muestran los brazos conectados, válvula abierta/cerrada, flecha y
símbolo del filtro. Los comparadores indican el llenado.

## Etiquetas

Cuatro papeles, tinte negro y slime producen cuatro etiquetas. Una etiqueta
vacía con un frasco lleno se convierte en una etiqueta de ese aspecto y devuelve
el frasco vacío; consume las diez unidades. Una etiqueta escrita sola puede
borrarse en una cuadrícula de crafteo.

Usa una etiqueta sobre una cara lateral de un tarro o alambique. Si ya contiene
esencia se etiqueta con ese aspecto; si está vacío necesita una etiqueta escrita.
Agacharse con la mano vacía retira la etiqueta y devuelve una vacía. El filtro
permanece al vaciar el contenido y evita recibir otros tipos. El alambique
filtrado también restringe qué recibe del horno.

El drop y la colocación del tarro conservan su etiqueta y esencia, incluso vacío;
la mejora arcana a tarro del vacío conserva ambos componentes. El símbolo se
renderiza en la cara etiquetada. Las recetas de depósito exigen frascos vacíos,
para que no destruyan por accidente esencia almacenada.

## Persistencia y límites

Los tubos guardan versión, contenido, caras abiertas, restricciones, filtro,
válvula y venteo. La succión se reconstruye al cargar, evitando mantener una
demanda de una red que ha cambiado. Romper tubos con contenido libera flux,
como en el original; el contenido no se duplica en su drop. Los estados futuros o malformados se
conservan sin sobrescribir y desactivan el bloque. Los mundos de port.10 sin
etiquetas se cargan como antes; el campo nuevo es opcional.

Faltan fuelles para reforzar depósitos, extracción remota, espejos de esencia,
centrifugadora, máquinas avanzadas y cerebro en frasco. La infusión y sus
consumidores corresponden al siguiente conjunto de mecánicas. No se declara
M08 completa. Las animaciones de los antiguos renderers tienen una adaptación
nativa más sencilla; no se reproduce su trazado de subpartes exactamente.

## Verificación reproducible

Las fixtures optativas quedan excluidas del JAR normal:

```sh
./gradlew -PessentiaProbe=write runServer
./gradlew -PessentiaProbe=read runServer
./gradlew -PessentiaProbe=client runClient
./gradlew clean build
```

Usan mundos de prueba aislados. `write` comprueba variantes, etiquetas, costes,
conservación, límites, conflictos y estados incompatibles, además del flujo con
tickers nativos; `read` verifica los mismos bloques en otro proceso.

Resultados del 18 de octubre de 2026: `ESSENTIA_RULES_OK` verificó las seis
variantes, aislamiento, redstone, filtros, depósitos mixtos, etiquetas y recetas,
incluidas las dos recetas arcanas con la misma cuadrícula. `ESSENTIA_RUNTIME_OK`
confirmó que los tickers nativos trasladaron exactamente 20 unidades a un tarro.
`ESSENTIA_RESTART_OK` comprobó en otro proceso el tarro etiquetado con 30 aer,
filtro con una unidad y caras aisladas, válvula cerrada/accionada por redstone
y depósito con cuatro aer, tres ignis y restricciones por cara.

En el cliente gráfico, un clic real abrió la válvula (`ESSENTIA_CLIENT_USE_OK`).
La cadena con filtro, válvula, tubo direccional y restrictor entregó 20 aer al
tarro etiquetado mediante tickers nativos (`ESSENTIA_CLIENT_FLOW_OK`); contenido,
etiqueta y apertura se verificaron en el cliente (`ESSENTIA_CLIENT_SYNC_OK`).
Se inspeccionaron los modelos conectados, el líquido y las etiquetas, y se
comprobó la lectura del resonador mediante uso real y el chat nativo.

La compilación limpia final pasó 37 tests sin fallos ni omisiones. Se analizaron
los JSON del JAR y sus 107 blockstates, 178 definiciones de item y 195 recetas;
no incluye clases de fixtures ni recursos de prueba.

La continuación de port.12 implementa la matriz, sus pedestales, absorción
remota para infusión y cerebro en frasco, y amplía el mazo a 32 cartas activas.
Véase [INFUSION-CARDS.md](INFUSION-CARDS.md) para recetas y límites.
