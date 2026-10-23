# Infusión avanzada: port.13

Cierre y actualización en port.55: [INFUSION-COMPLETE.md](INFUSION-COMPLETE.md).

Minecraft 26.3 / Forge 66.0.13 / Java 25.

## Estabilización

El estabilizador conserva hasta 15 unidades. Recupera una cada 20 ticks y produce
0,25 flux por unidad regenerada, como el original. Con la reserva llena deja de
contaminar. La carga se guarda; romper el bloque no permite recuperar energía
ni multiplicarla. Un comparador lee la reserva.

Las incrustaciones requieren suelo sólido y conducen horizontalmente, también
a través de pedestales. No transportan redstone vanilla. Cada tramo adicional
resta una unidad de intensidad: un pedestal adyacente recibe la carga completa;
con dos incrustaciones entre ambos recibe dos menos. Se recalculan los caminos
cargados cada cinco ticks, sin cargar chunks. La intensidad mostrada es derivada,
no otra reserva de energía; cortar el camino desactiva la protección de inmediato
al intentar usarla. La búsqueda está limitada a quince conexiones.

Ante un accidente de ingredientes, el pedestal busca un estabilizador conectado
con reserva suficiente. Protegerlo cuesta entre cinco y diez unidades reales.
La red no impide los accidentes de daño, warp o contaminación de la matriz.
Los estabilizadores emparejados también aportan 0,25 a la simetría, con el mismo
rendimiento decreciente de los otros estabilizadores; cada par de incrustaciones
aporta 0,025. Dentro de ocho bloques
mejoran grietas no colapsadas, consumiendo una unidad por 0,125 puntos hasta superar estabilidad
50; no crean energía al compartirla con infusión.

Las conexiones y la intensidad se muestran en el modelo y el tinte nativos.
El resonador o la interacción muestran la carga. Una incrustación sin soporte
se rompe con su drop.

## Mejoras de la matriz

Hay cuatro posiciones: debajo de cada pilar del altar (respecto a la matriz,
x/z ±1, y -3). Cada piedra de velocidad resta uno al tiempo base y aumenta el
multiplicador de esencia en 0,01. Cada piedra de coste suma uno y resta 0,02 al
multiplicador. Se aplican antes del mínimo del 50% y del redondeo por aspecto.
El ciclo usa la mitad entera del tiempo base, con mínimo un tick. Los valores
base son 10 para arcano, 9 para antiguo y 7 para oculto.

Los costes de un trabajo en curso permanecen fijados en su instantánea; cambiar
las piedras afecta al siguiente cálculo de receta, no reembolsa ni duplica
esencia. El intervalo de trabajo sí refleja el montaje actual.

Se incorporan las cuatro recetas arcanas originales de incrustaciones,
estabilizador y las dos piedras, con vis, cristales e investigaciones originales.
La marca `!INSTABILITY` se descubre al presenciar un accidente dentro de diez
bloques, permitiendo investigar la estabilización.

## Ocho encantamientos activos de herramientas

Las recetas nativas de infusión usan un libro encantado y el componente original.
El objeto central debe ser compatible y no haber alcanzado el máximo. Se copia
completo, incluyendo nombre, desgaste y componentes previos, y se aumenta un
rango. No se convierte en un objeto nuevo ni se pierden encantamientos vanilla.
El coste por aspecto se multiplica por el rango siguiente más 0,33 por cada otro
tipo de encantamiento de infusión; después se aplica el modificador del altar.
El resultado calculado se guarda al comenzar y persiste en trabajos incompletos.

| Encantamiento | Máximo | Componente | Efecto nativo |
| --- | ---: | --- | --- |
| Collector | 1 | Correa | Lleva drops de minería y combate al jugador, con demora de recogida. |
| Destructive | 1 | TNT | Rompe el plano 3×3 orientado por la cara de minería. |
| Burrowing | 1 | Pata de conejo | Sigue troncos o mineral conectado del mismo bloque, hasta 32 posiciones. |
| Sounding | 4 | Mapa vacío | Agacharse y golpear muestra partículas de minerales en un radio de 4×rango. |
| Refining | 4 | Salis Mundus | Convierte hierro, oro y cobre crudos en racimos, con probabilidad (rango+1)×0,125. |
| Arcing | 4 | Bloque de redstone | Propaga la mitad del daño a hasta rango criaturas cercanas. |
| Essence | 5 | Cristal de esencia | Los enemigos pueden soltar cristales de sus aspectos. |
| Lamplight | 1 | Nitor | Deja resplandor invisible de luz 15 en el hueco minado si está oscuro. |

Los efectos se ejecutan en el servidor. La minería secundaria utiliza el camino
nativo de rotura del jugador: respeta eventos de protección, herramientas,
drops, dureza y desgaste; no procesa block entities ni carga chunks. Hay límites
de distancia y de recursión. Agacharse suprime la minería adicional, la recogida
y la creación de luz. Los golpes de Arcing no se propagan recursivamente y no
seleccionan jugadores, aliados ni mascotas domesticadas.

Las etiquetas nativas incluyen las herramientas de taumio. Los rangos usan un
componente persistente sincronizado, con nombres traducidos en el tooltip.
El requisito original de investigación acepta un objeto con Filo I o superior,
incluidos libros, en lugar de tratar el marcador antiguo como un item ausente.
Se conservan los padres de investigación originales. Se conectan las observaciones
de caminar (>1600 m), correr (>800 m), nadar (>80 m) y saltar (>500 veces) a las
estadísticas persistentes de Minecraft; son requisitos de la cadena original
que conduce a estas investigaciones.

## Límites y dependencias

Los efectos son adaptaciones nativas: Collector desplaza drops en lugar de usar
la entidad animada original; Burrowing cosecha un conjunto conectado acotado en
vez de seleccionar solo el bloque más lejano; Sounding usa partículas en lugar
del shader de escaneo; Refining cubre los tres metales con mineral crudo nativo.
Port.55 incorpora el warp propio del objeto al encantar y lo conecta con M12.

Port.55 completa los 24 resultados ponderados, incluidos goo, enfermedades,
warp, descargas y los seis accidentes de ingredientes; véase INFUSION-COMPLETE.md.

El equipo elemental, fortaleza, accesorios, escudos rúnicos y focos se incorporaron
en M10/M12. El altar se inicia con el guantelete y conserva sus requisitos de investigación.

## Verificación

Fixtures optativas `-PinfusionProbe=write`, `read` y `advanced-client`, fuera del
JAR de distribución. Se usan los mundos aislados `infusion-smoke` y
`infusion-client`, cuyos montajes reemplazan el área de prueba. Las comprobaciones abarcan observaciones de movimiento, mejoras, ciclos lentos
con guardado de un contador mayor de cinco, circuitos interrumpidos,
carga consumida, grietas reales, límites de los ocho encantamientos, conservación
de componentes y una infusión real. Se reutilizan las regresiones de port.12.
El cliente gráfico confirmó `INFUSION_ADVANCED_RULES_OK`,
`INFUSION_ADVANCED_CLIENT_EFFECTS_OK` e `INFUSION_ADVANCED_CLIENT_SYNC_OK`:
se completó una infusión de 100 unidades, se probaron los ocho efectos nativos,
la minería 3×3 respetó una rotura denegada (ocho bloques y ocho de desgaste),
y agacharse suprimió el área. Se verificaron extracción de racimos, recogida,
luz real, troncos conectados, daño secundario, cristales de criaturas y sondeo.
Se inspeccionaron la red cargada, sus modelos y el tooltip de Collector en la
interfaz nativa; el cliente se cerró con guardado del mundo.

El servidor dedicado confirmó `INFUSION_RULES_OK`,
`INFUSION_ADVANCED_RULES_OK` e `INFUSION_RUNTIME_OK`. Las pruebas de entidades se
ejecutan después de activar los chunks, limpiando grietas de intentos anteriores.
La pasada de escritura se detuvo mediante SIGTERM: el shutdown hook guardó el
mundo y el wrapper de JavaExec terminó con código 143. En un proceso nuevo,
`INFUSION_ADVANCED_RESTART_OK` verificó la carga y el pico encantado completo;
`INFUSION_RESTART_OK` confirmó la continuación de una infusión con 24 unidades
pendientes hasta una sola salida, conservando también XP, teoría y warp.
La segunda pasada se detuvo con el comando `stop` de la consola.

La compilación limpia normal terminó correctamente con 44 tests, sin fallos,
errores ni omisiones. El JAR de distribución contiene 115 blockstates, 187
definiciones de item y 220 recetas. Se analizaron todos sus JSON y se comprobó
que incluye las clases nuevas y excluye las fixtures y clases de prueba. Los
seis generadores de conocimiento, teorías, alquimia, esencia e infusión convergen
sin cambios de contenido.
