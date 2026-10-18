# Mecánicas antes que cartas: port.10

Minecraft 26.3 / Forge 66.0.13 / Java 25. Este lote desarrolla los consumidores
reales; el mazo de port.9 conserva sus 27 cartas. M07 y M08 siguen siendo parciales.

## Orden de dependencias

1. Árboles y hojas proporcionan las maderas especiales de los filtros y alambiques.
2. Crisol, recetas y resolución de aspectos permiten fabricar materiales y cristales.
3. Horno, alambiques, frascos y tarros permiten obtener y almacenar esencia.
4. Después corresponden tubos/filtros/etiquetas y la red de esencia, pedestales e
   infusión, focos y sus equipos, y el cerebro en frasco/curiosidades con sus
   fuentes y consumidores. Las cartas pendientes se incorporarán después.

Corrección de la lista anterior: Channel necesita un frasco lleno. Infuse consume
un frasco y un material; no ejecuta una infusión en una matriz. Sculpting consume
arcilla vanilla y exige acceso a su categoría, no una prensa de gólems. Curio
necesita curiosidades obtenibles y Dark Whispers el cerebro en frasco como ayuda.
No se declara que el registro de esos objetos complete sus mecánicas.

## Greatwood y silverwood

Se añaden dos saplings y dos tipos de hojas con crecimiento nativo por tiempo o
polvo de hueso, drops y caída de hojas sin tronco. Los troncos y tablones ya
registrados se reutilizan. Las hojas naturales de silverwood recuperan 0,01 vis
por random tick, hasta la base del chunk; las hojas colocadas por el jugador no
lo hacen. No eliminan flux.

La generación básica usa bosques vanilla en **chunks nuevos**: un intento cada
8 chunks para greatwood y cada 64 para silverwood, antes de comprobar suelo y
espacio. `SERVER world.generateTrees` controla esa generación; los saplings
plantados pueden crecer con ella desactivada. Las formas son una adaptación
limitada al motor actual: greatwood de tronco 2×2 y silverwood de tronco simple,
con copas amplias. No reproducen todavía las ramas, raíces especiales, arañas ni
biomas mágicos originales. La colocación comprueba el árbol completo antes de
escribir, respeta obstáculos y no fuerza chunks desde un sapling.

## Crisol y progreso

Tras Primeros pasos, avanza Descubrir alquimia hasta `UNLOCKALCHEMY@2`. Aplica
Salis Mundus a un caldero vacío: consume uno y registra la fabricación del crisol.
Llénalo con un cubo de agua y colócalo sobre magma, fuego, lava o nitor. El calor
sube un punto por tick hasta 200; funciona por encima de 150 y se enfría al
retirar la fuente. El calor, agua, aspectos y temporizadores se guardan.

Usa un ingrediente sobre él o lanza objetos dentro para disolverlos. Las recetas
se resuelven en el servidor y necesitan su investigación, catalizador, aspectos
y 50 mB de agua por operación. Un catalizador preparado pero bloqueado por
investigación se conserva. Un objeto sin receta aplicable puede disolverse como
ingrediente, igual que en el flujo original. Los objetos fabricados se expulsan
con una marca persistente que evita su reabsorción inmediata.

El primer nitor usa 10 potentia, 10 ignis y 10 lux, más polvo de glowstone como
catalizador. Carbón y glowstone aportan esos aspectos; queda sensus sobrante.
Fabricarlo completa la prueba del libro y permite abrir Alquimia básica. El
crisol también convierte dos unidades de un aspecto y una esquirla de cuarzo
en un cristal de ese aspecto (37 recetas), y fabrica latón, thaumium, materiales
de alquimia práctica y racimos de minerales con sus requisitos originales.
La receta de metal del vacío existe, pero sigue exigiendo su rama eldritch.

Los residuos decaen gradualmente en flux; el exceso de 500 unidades también se
derrama. Agacharse con la mano vacía vacía el crisol y contamina el aura. Romperlo
libera sus residuos. El líquido caliente daña a las entidades dentro. La forma
de colisión conserva paredes y fondo, permitiendo echar objetos desde arriba.
El tanque admite solo agua y expone la capacidad Forge con simulación sin gasto.

Las recetas se cargan como `thaumcraft:crucible` desde datapacks, separadas de las
recetas de crafteo. Usan plantillas de salida para el orden de carga de componentes
de 26.3. Se corrigen las equivalencias antiguas de nitor amarillo, cuero, carbón
vegetal y metadatos de tintes, para que costes y pruebas de fabricación coincidan.

## Aspectos derivados

Las asignaciones originales explícitas tienen prioridad. Para objetos sin ellas
se recorren recetas nativas shaped/shapeless, cocción, crafteo arcano y crisol.
Se descuentan recipientes devueltos; el crafteo conserva el factor original 0,75,
el divisor por cantidad y su frontera de redondeo. La alquimia usa su fórmula
original de raíz cuadrada. El crafteo arcano añade su componente mágico.

Se exigen ingredientes resolubles, se evitan ciclos y hay límites de profundidad,
trabajo y alternativas. La caché pertenece a la instancia de RecipeManager y se
renueva con un reload. El escáner utiliza esta misma resolución en el servidor.
Recetas especiales de otros mods, pociones y encantamientos aún no se infieren.

## Destilación, frascos y tarros

El horno de esencia básico tiene alimentación, combustible y salida de recipientes.
Lee la duración del componente de combustible de 26.3 y el evento Forge.
Cada objeto tarda dos ticks por unidad de aspecto, con una reserva máxima de 256.
La eficiencia básica es 80%; vitium usa 80% × 0,66. Las pérdidas contaminan el aura.
Cada 15 ticks intenta exportar una unidad hacia una columna de hasta cinco
alambiques consecutivos, prefiriendo el que ya contiene ese aspecto. Cada
alambique guarda hasta 128 unidades de un solo aspecto. Si no hay destino el
horno conserva su reserva y se detiene al llenarse.

La interfaz nativa muestra ingredientes, combustible, progreso y aspectos; los
inventarios y contadores sobreviven al reinicio. La capacidad Forge permite
alimentación por arriba, combustible lateral y extracción de recipientes por
abajo. Las operaciones de simulación no alteran el inventario. Los cambios de
lit/agua y el líquido del crisol/tarros/alambiques se sincronizan y renderizan.

Los frascos vacíos se fabrican con cristal y arcilla. Usarlos sobre un alambique
o tarro extrae exactamente 10 unidades; un frasco lleno devuelve esas diez a un
tarro compatible y deja el frasco vacío. Los tipos no se mezclan. Se incluyen
las 37 variantes de esencia, con componentes persistentes, color y nombre.

Los tarros protegidos almacenan 250 unidades. Su drop conserva cantidad y tipo,
y vuelven a colocarse llenos. La mejora arcana a tarro del vacío cuesta 50 vis y
un cristal perditio, preservando toda la esencia. El tarro del vacío acepta y
elimina el exceso del mismo tipo; rechaza otros tipos. La receta alternativa de
escritura solo acepta frascos vacíos, para no destruir esencia accidentalmente.

Todavía no hay tubos, succión de una red, etiquetas ni extracción remota; en este
lote el traslado entre alambique y tarro es manual con frascos. Los hornos
mejorados, fuelles, respiraderos, centrifugadora y máquinas automáticas continúan
pendientes, igual que infusión y hechizos. No se crean sus cartas todavía.

## Alumentum

La receta alquímica mantiene sus requisitos. Alumentum funciona como combustible
nativo de 4800 ticks y también puede lanzarse: consume una unidad, crea un proyectil
sincronizado y explota al impactar con potencia 1,1. El proyectil usa el motor
actual y conserva propietario y objeto al guardarse.

## Guardado y verificación

El estado tiene versión, límites y validación de aspectos. Los tags corruptos o
futuros se conservan y desactivan la máquina; llenar, procesar o automatizar no
los sustituye por estado vacío. Los menús comprueban su bloque y distancia.
El cliente no envía cantidades, recetas ni resultados.

Fixtures optativos, excluidos del JAR normal:

```sh
./gradlew -PalchemyProbe=write runServer
./gradlew -PalchemyProbe=read runServer
./gradlew -PalchemyProbe=client runClient
./gradlew clean build
```

Usan `alchemy-smoke` y una copia aislada para el cliente. La escena visual congela
el tiempo para conservar los ingredientes preparados mientras se comprueban el
uso real del bloque y los menús. Los temporizadores, combustible, crecimiento y
caída de hojas se comprueban por separado en el servidor dedicado.

Resultados del 18 de octubre de 2026: `ALCHEMY_RUNTIME_OK` confirmó el recorrido
del libro al crisol, nitor, cristales, latón/thaumium, capacidades y simulación,
destilación, frascos, tarros, mejora arcana con costes y guardados incompatibles.
También se probaron crecimiento con saplings, protección de obstáculos, caída
de hojas, 0,01 vis de silverwood y exclusión de hojas persistentes, además del
combustible y lanzamiento/impacto real de alumentum. `ALCHEMY_RESTART_OK` confirmó
el archivo de jugador y los bloques en otro proceso: agua 950, calor 187,
aspectos lux 17/vitium 3, combustible 1234, progreso 7 y tarro con 30 aer.

En el cliente gráfico se creó nitor mediante clic derecho real sobre el crisol
(`ALCHEMY_CLIENT_CRAFT_OK`) y se abrió su menú nativo, que sincronizó agua 950,
calor 200 y aspectos residuales (`ALCHEMY_CLIENT_MENU_OK`). Se inspeccionaron
los modelos de crisol, horno, alambique y tarros, el líquido y las variantes de
frasco. El intercambio exacto de frascos se verificó en servidor dedicado.

La compilación limpia final pasó 34 tests sin fallos ni omisiones. El JAR normal
contiene 101 blockstates, 170 definiciones de item y 186 recetas; se analizaron
sus JSON y se comprobó que no incluye las fixtures de integración.

## Continuación port.11

La red física de tubos, succión, etiquetas y resonador se implementa en
[ESSENTIA-TRANSPORT.md](ESSENTIA-TRANSPORT.md). Sustituye el traslado exclusivamente
manual descrito para port.10; los frascos continúan disponibles.
