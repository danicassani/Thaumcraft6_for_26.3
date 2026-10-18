# Estado del port: contenido y mecánicas

La lista de contenido añadido está en [SIMPLE-CONTENT.md](SIMPLE-CONTENT.md).
Las listas auditadas están en [CONTENT-INVENTORY.md](CONTENT-INVENTORY.md) y
[MECHANICS-ROADMAP.md](MECHANICS-ROADMAP.md). La auditoría cubre 263 referencias
originales, incluidas las familias de 16 colores. No confundir referencias,
variantes por metadata, IDs modernos y sistemas completos.

## Contenido sin un sistema propio nuevo

75 referencias originales se apoyan en bloques/componentes de Minecraft o son
materiales de crafteo. Las seis losas dobles originales se representan con el
estado `type=double`; no tienen otro item. Se añaden 35 bloques, 35 variantes de
materiales, cinco herramientas de taumio, cuatro piezas de armadura de taumio y
siete alimentos. Se conservan los ocho bloques y nueve materiales del paso 3.

El catálogo exacto de IDs y clases está en [simple_content.json](../tools/simple_content.json).
Los materiales de máquinas pendientes se pueden usar como ingredientes o en
creativo; registrarlos no implementa la máquina que los consume. Lo mismo sucede
con los constructos alquímicos y las piedras de mejora de infusión.

Se portan las formas y estados de troncos, escaleras, losas y pilares, y los
44 triángulos y UV originales de los pilares. Un cargador cliente propio los
convierte a quads sin pasar por el cursor defectuoso de BakedQuadBuilder de
Forge 66.0.13; se conservan geometría, orientaciones y texturas. Plantas con soporte, luz y náusea del
vishroom; herramientas con durabilidad, minería y reparación; armadura con
estadísticas y texturas equipadas; comida con nutrición y efectos vanilla.
Los pilares sueltan dos piedras base y tienen su colisión original de dos bloques.
Los bloques ancestrales irrompibles no producen drops.

Las recetas normales se conservan: conversiones de madera, escaleras, losas,
carne, placas, herramientas y armadura. La golosina de carne exige tres tipos
distintos (20 combinaciones de seis tipos) y azúcar; se rechazan duplicados.
No se añaden recetas vanilla para saltarse alquimia, investigación o vis.
Los metales de otros mods (estaño, plomo, plata) tienen ingredientes y tags de
interoperabilidad, pero no se inventan lingotes vanilla que no existen.

## Primera mecánica: iluminación de velas y nitor (M02)

32 bloques: candle_* y nitor_* en 16 colores. Se conservan los sufijos antiguos
`silver` y `lightblue`. Las velas emiten luz 14, requieren suelo sólido, se rompen
con drop si pierden soporte y no bloquean el paso. El nitor flota, no tiene
colisión, emite luz 15 y partículas coloreadas sin combustible. El estado luminoso
no necesita una block entity vacía en la API moderna. Se porta el recoloreado y
la receta de tres velas con hilo y dos sebos. Producir nitor desde cero sigue
requiriendo la futura alquimia.

Los tintes se registran solo en cliente. Las partículas usan los proveedores de
Minecraft; los shaders y emisores artísticos originales de Thaumcraft aún no se
han portado. Los tags de fuentes de calor y estabilizadores preparan los
consumidores de crisol e infusión: esos sistemas todavía están pendientes.

## Verificación del lote base (port.4)

Pruebas optativas con `-PbasicContentProbe=<mundo de prueba> runServer`:

- BASIC_CONTENT_PROBE_OK: regresiones del paso 3 y minerales naturales.
- SIMPLE_CONTENT_PROBE_OK: 35 bloques, losas dobles, colisión de pilares,
  suelos de plantas, herramientas, comida y recetas de tres carnes distintas.
- LUMINOUS_CONTENT_PROBE_OK: 32 variantes, emisión de luz, colisión, soporte,
  drops y recoloreado.

Las pruebas no se empaquetan en el JAR normal. Evidencias en work/, sin publicarlas
junto a los artefactos. La compilación limpia y la compilación final pasan con
Java 25 / Forge 66.0.13; WarpCodecTest conserva sus seis pruebas correctas.
El JAR de port.4 contiene 135 definiciones de item y 111 recetas.
Cliente normal: mundo integrado cargado, pilares completos con textura y
rotación, velas de colores en creativo y vela roja colocada con llama e
iluminación, nitor rojo con partículas y armadura completa de taumio equipada
con sus texturas. Recarga F3+T sin errores de modelos ni texturas ausentes.
Servidor dedicado normal: arranque hasta Done y parada con guardado.

El entorno gráfico usa software; no tiene dispositivo de sonido y los
servicios externos de Realms/autenticación no están disponibles en esta prueba.
El servidor muestra un error del appender al sondear kqueue en Linux y
continúa con su arranque normal. No se certifican audio ni servicios online.

## Núcleo de aura añadido en port.5

Ver [AURA-CORE.md](AURA-CORE.md): estado por chunk, ciclo lunar, regeneración,
difusión, API de simulación/consumo, guardado por dimensión y sincronización de
servidor a cliente. Se añaden una batería de vis funcional y la esponja creativa
de flux. El catálogo total de port.5 contiene 76 bloques y 61 objetos sin
bloque (137 entradas creativas), con las 111 recetas anteriores. Los comandos de operador permiten consultar y añadir vis/flux durante
las pruebas; el HUD del taumómetro depende de M04.

La ampliación de [port.6](M05-CRYSTALS-DEVICES.md) incorpora siete cristales,
37 variantes del objeto de esencia, césped ambiental, generador FE, dioptra y
pedestal de recarga. El catálogo total alcanza 87 bloques, 62 objetos sin bloque
(149 IDs de item y 185 entradas creativas al contar los aspectos), y 111 recetas.
El pedestal tiene el contrato y la mecánica de recarga; el equipo nativo que lo
utiliza se portará en M10/M12.

[Port.7](ARCANE-RIFTS.md) añade la mesa arcana persistente, Salis Mundus, el
cargador de mesa y el sifón de vacío con grietas de flux funcionales. El catálogo
alcanza 90 bloques y 63 objetos sin bloque (153 IDs de item y 189 entradas creativas)
y 119 recetas. La mesa consume vis y cristales mediante recetas de datos; el
cargador amplía su alcance a 3×3 chunks. El sifón produce semillas y admite
extracción automática. Las grietas aparecen por exceso de flux, crecen, pierden
estabilidad y colapsan; los eventos avanzados de M13 siguen pendientes.
El resonador de esencia conserva su dependencia de M08.

[Port.8](RESEARCH-SCANNING.md) incorpora el núcleo de investigación y escaneo:
conocimiento persistente, diario del sueño, taumómetro, Thaumonomicon y requisitos
de las recetas arcanas. Añade las recetas del taumómetro y la dioptra. El catálogo
suma 90 bloques, 65 objetos sin bloque, 155 IDs de item, 191 entradas creativas y
121 recetas. Se conservan 148 investigaciones y 271 etapas originales; las ramas
que necesitan mecánicas posteriores mantienen sus bloqueos. La mesa de teorías
y el cielo siguen pendientes.

## Lo que sigue pendiente

La hoja de ruta detalla los bloques y objetos de cada sistema: árboles y hojas;
inventarios; teorías, observación celeste e integración avanzada de aspectos; consumidores adicionales de
aura/vis/flux; progresión y descuentos de mesa arcana; alquimia y fluidos; esencia y estandartes con aspectos;
infusión y redes de estabilización; hechizos; gólems; equipo avanzado/warp/
accesorios; corrupción; dispositivos; espejos; botín y dimensiones. No se declara
completo el port de Thaumcraft 6 ni se activan bloques de máquinas vacíos.

## port.9: teorías y cielo

Se añaden mesa de madera, mesa de investigación, herramientas de escritura con
recarga y notas celestes de trece variantes. Total: 92 bloques, 67 objetos sin
bloque, 159 IDs de item, 207 entradas creativas y 124 recetas. Véase
[THEORY-CELESTIAL.md](THEORY-CELESTIAL.md) para las 27 cartas y sus límites.
