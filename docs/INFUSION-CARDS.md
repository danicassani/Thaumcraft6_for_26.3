# Infusión y cartas: port.12

Minecraft 26.3 / Forge 66.0.13 / Java 25. Se añade el núcleo funcional de M09 y
se amplía el mazo de 27 a **32 cartas activas**. M09 sigue siendo parcial para
recetas de equipos avanzados, encantamientos y mejoras aún ausentes.

## Acceso y montaje del altar

El Thaumonomicon acepta ahora los frascos por aspecto y cantidad: el requisito
de Infusión necesita un frasco con diez aer, no uno vacío ni de otro tipo.
Completa la segunda etapa y fabrica la matriz en la mesa arcana: cuatro ladrillos
de piedra arcana en las esquinas, nitor en el centro, 150 vis y un cristal de
cada aspecto primordial. Fabricarla registra la prueba que permite completar
INFUSION y fabricar pedestales arcanos.

Coloca un pedestal central y la matriz dos bloques por encima. En las cuatro
esquinas diagonales, a un bloque del centro, coloca columnas de dos bloques de
piedra arcana. Usa Salis Mundus sobre la matriz: las ocho piedras se convierten
en cuatro pilares bajos y aire por encima, consumiendo un Salis. Se valida el
conjunto completo antes de modificarlo. Las variantes antigua y oculta usan
sus piedras, pedestales y requisitos de investigación respectivos. Los tres
pedestales tienen recetas arcanas y un inventario de un objeto con capacidad
Forge para tolvas y automatización. Clic coloca una unidad o retira la existente;
romperlos devuelve su contenido. La simulación no altera el inventario.

Coloca el objeto principal en el pedestal central y los componentes en otros
pedestales, dentro de ocho bloques horizontales de la matriz y entre siete
bloques por debajo y tres por encima. El orden es libre; la coincidencia reserva
pedestales distintos y resuelve ingredientes repetidos o etiquetas solapadas.
Los pedestales ocupados adicionales impiden iniciar una receta exacta.

**Inicio provisional:** mano vacía sobre la matriz montada. El resonador muestra
estado, estabilidad, regeneración, esencia e ingredientes pendientes. Agacharse
con la mano vacía cancela la fabricación propia. El guantelete de lanzador sigue
pendiente; no se añade un objeto decorativo para sustituir su mecánica.

## Esencia, progreso y estabilidad

La matriz toma una unidad por ciclo desde tarros normales o del vacío cargados,
en una caja de alcance doce, prefiriendo los más próximos. Esta absorción remota
es propia de la infusión y permite usar la red física de port.11 para rellenar
los tarros; no necesita conectar un tubo a la matriz. No se fuerzan chunks.
Primero se pagan los aspectos y después se consumen los ingredientes, dejando
sus recipientes cuando corresponda, por ejemplo el cubo de agua de la receta
del cerebro. El resultado reemplaza una sola vez el objeto central y registra
la prueba de fabricación si el propietario está conectado, como en el original.

La receta, propietario, objeto central exacto, resultado, cantidades restantes,
componentes concretos y progreso se guardan al iniciar. Un reload de datapacks
no cambia los costes de un trabajo ya aprobado. La reserva no crea copias de
objetos físicos. Retirar o cambiar el objeto central interrumpe la fabricación;
retirar un ingrediente la deja esperando y pierde estabilidad. Los costes ya
pagados no se reembolsan al cancelar, romper la matriz o invalidar el altar.
Si un chunk necesario está descargado se espera, sin borrar el trabajo.

La estabilidad se limita a -100…25 y se recupera en reposo. Velas de Thaumcraft,
cabezas y pedestales participan en pares de simetría central, a igual altura.
Los pares repetidos del mismo bloque tienen rendimiento decreciente de 0,75.
Un elemento sin pareja resta estabilidad; los pedestales emparejados con distinta
ocupación también penalizan. El nitor no sustituye a una vela estabilizadora.
Los altares antiguos/ocultos y sus pedestales conservan sus modificadores de
coste y regeneración; los ciclos básicos son de cinco ticks, cuatro para antiguo
y tres para oculto. El coste tiene un mínimo del 50% y redondeo por aspecto.

Cada ciclo pierde estabilidad según la inestabilidad de la receta y el estado
actual. La falta de esencia o ingredientes añade pérdida. Con estabilidad
negativa pueden ocurrir expulsiones de ingredientes, contaminación de flux,
daño mágico o warp temporal. La lista de sucesos es una adaptación limitada:
no incluye todavía flux goo, explosiones ni mitigación por estabilizadores e
incrustaciones. El consumo de componentes y las animaciones también son más
sencillos que los renderers y tiempos de desplazamiento originales.

## Recetas con consumidores reales

Se incorporan nueve recetas de infusión originales:

- Siete racimos de cristal: cristal de esencia del aspecto, semillas de trigo
  y Salis Mundus; 10 del aspecto, 10 vitreus y 5 vinculum. Inestabilidad cero,
  salvo vitium, que tiene cuatro.
- Cerebro en frasco: tarro protegido **vacío y sin etiqueta**, cerebro, dos ojos
  de araña y cubo de agua; 25 cognitio, 25 sensus y 25 exanimis; inestabilidad 4.
- Sifón del vacío: bloque de metal del vacío, dos piedras arcanas, mecanismo
  complejo, dos placas de latón y estrella del Nether; 50 alienis, 50 perditio,
  100 vacuos y 50 fabrico; inestabilidad 7.

Cada receta exige su investigación original. Son recetas `thaumcraft:infusion`
de datapack, separadas de crafteo, crisol y mesa arcana. Los ingredientes pueden
exigir una variante exacta de cristal. Los resultados de este lote son de una
unidad; no se aceptan salidas mayores en los pedestales. No se registran recetas
para objetos que todavía carecen de su mecánica.

El modelo nativo de la matriz usa ocho piezas y rotación. Los pedestales muestran
el objeto sincronizado; el cerebro conserva el modelo original con salmuera.
No son una reproducción de todas las animaciones de TC6.

## Cerebros, XP y curiosidades

Los zombis muertos por el jugador tienen una probabilidad base del 10% de dejar
cerebro. Se puede consumir como alimento, con hambre y posible warp temporal,
o usarlo como ingrediente y prueba de escaneo `f_BRAIN`.

El cerebro en frasco atrae orbes cercanos y guarda hasta 2000 XP. Se cuenta la
multiplicidad de los orbes fusionados; el exceso se devuelve como orbes para no
perder XP. Clic libera una cantidad de hasta 63 XP y aplica una pausa de
absorción de 40 ticks. El resonador consulta la reserva. Comparadores, drop,
colocación y guardado conservan la cantidad; el modelo no sigue todavía la
mirada del jugador.

Se añaden las siete variantes originales de curiosidad: arcana, preservada,
antigua, oculta, esclarecedora, retorcida y Ritos carmesíes. Hay una fuente de
supervivencia ya implementada para la preservada: sustituye el drop de ámbar
con probabilidad 0,066 al romper mineral sin Toque de seda. No se inventan
fuentes para las otras variantes; sus comerciantes, enemigos y estructuras
siguen pendientes, aunque las variantes pueden usarse mediante datapacks o
creativo. Estudiarlas con clic consume una y otorga observaciones/teorías según
su rama más bonos aleatorios; las ocultas y Ritos añaden warp. Los Ritos exigen
warp suficiente y permiten la marca de investigación correspondiente.

## Las cinco cartas nuevas

| Carta | Requisito y coste | Efecto |
| --- | --- | --- |
| Channel | Frasco de diez unidades de un aspecto compuesto; se conserva | Infusión +25% |
| Infuse | Consume un material obtenible y un frasco compuesto lleno | Infusión según los aspectos del material |
| Sculpting | Consume arcilla vanilla | Gólems +20% y un robo extra |
| Curio | Consume una variante de curiosidad que el jugador ya posee | Básicos y bonos de rama; hasta dos robos extra |
| Dark Whispers | Solo por ayuda de cerebro en frasco; agota los niveles | Progreso Oculto, warp normal y posible robo extra |

Infuse es un experimento de teoría: no inicia una infusión física en la matriz.
Los materiales incluyen alumentum, nitor, cerebro, tela y metales ya obtenibles.
La carta consume el frasco entero, según el comportamiento original, mientras
que la receta de escribir etiquetas sí devuelve el frasco vacío.

Se añaden ayudas de crisol (Concentrate/Reactions/Synthesis), matriz
(Measure/Channel/Infuse) y cerebro (Dark Whispers). Las nueve ayudas tienen dos
páginas en la interfaz. Los paquetes nativos usan un espacio de acciones mayor
para que las ayudas nuevas no se confundan con la revisión de la sesión.
Se mantienen propietario, validación completa de costes, resultados propuestos
sin efectos parciales, semillas persistentes y rechazo de paquetes repetidos.
Las identidades de las 27 cartas anteriores no cambian.

Study y Notation permanecen fuera del mazo normal: el original las marca como
exclusivas de ayuda pero no registra ningún proveedor activo. Son 32 cartas
activas, no una afirmación de que todas las mecánicas futuras estén terminadas.

## Verificación

Fixtures optativas, excluidas del JAR de distribución:

```sh
./gradlew -PinfusionProbe=write runServer
./gradlew -PinfusionProbe=read runServer
./gradlew -PinfusionProbe=client runClient
./gradlew clean build
```

El mundo `infusion-smoke` y la copia de cliente están aislados. Se verifican
montaje, libro, costes, progreso, manipulación de pedestales, recipientes,
estabilidad, cinco cartas y ayudas, curiosidades, XP, guardados incompatibles
y continuación en otro proceso; además de interacción e interfaz gráficas.

Resultados del servidor dedicado: `INFUSION_RULES_OK` confirmó el requisito de
frasco exacto en el libro, fabricación arcana de la matriz con 150 vis y seis
cristales, montaje con Salis, simulación de inventario, costes y rechazo de
manipulación del objeto central. También pasaron la receta real del cerebro
(75 unidades y cubo devuelto), simetría de velas y sucesos de inestabilidad,
las cinco cartas, selección nativa de la ayuda del cerebro, rechazo de paquetes
repetidos, estudio de curiosidad, loot de ámbar/drop Forge de zombi, conteo de
orbes fusionados y devolución del exceso de XP. `INFUSION_RUNTIME_OK` verificó
una infusión completa con tickers nativos y preparó otra con 24 unidades
pendientes, junto a 1999 XP y una elección de teoría con semilla guardada.

`INFUSION_RESTART_OK` verificó en otro proceso el estado parcial, la continuación
hasta una única salida con coste total exacto y la conservación de XP, semilla
de carta y warp.

En cliente gráfico, un clic real sobre la matriz montada inició la fabricación
(`INFUSION_CLIENT_START_OK`). Los tickers produjeron el racimo y registraron la
prueba del propietario conectado (`INFUSION_CLIENT_CRAFT_OK`). La interfaz nativa
mostró Channel, Infuse y Sculpting con requisitos sincronizados
(`INFUSION_CLIENT_MENU_OK`); pulsar el botón real de Infuse consumió esmeralda,
frasco lleno y una tinta, concedió 17% de Infusión y limpió las opciones
(`INFUSION_CLIENT_CARD_OK`). Se inspeccionaron matriz, pilares, pedestales y sus
objetos, junto a los textos y disposición de las cartas.

La compilación limpia final pasó 44 tests, sin fallos ni omisiones. El JAR
normal contiene 112 blockstates, 185 definiciones de item y 208 recetas; se
analizaron sus JSON y se comprobó que no incluye las fixtures de prueba. Los
generadores de conocimiento, teorías, alquimia, esencia e infusión convergen
sin cambios al regenerar todos los recursos.
