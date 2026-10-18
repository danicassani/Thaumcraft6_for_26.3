# Núcleo de aura, vis y flux — port.5

La ampliación de cristales y dispositivos de port.6 está en
[M05-CRYSTALS-DEVICES.md](M05-CRYSTALS-DEVICES.md). La lista de pendientes de
este documento describe el estado de port.5.

Esta entrega implementa el núcleo de M05 y dos consumidores funcionales. M05
sigue siendo parcial: no incluye todavía todos sus dispositivos ni los sistemas
que dependen de él. Requiere el mismo JAR port.5 en cliente y servidor.

## Comportamiento implementado

- Estado por chunk: base 0–500, vis y flux finitos entre 0 y 32766.
- Inicialización determinista por semilla y coordenadas. Se muestrean el bioma
  central y cuatro vecinos a altura 50 sin cargar sus chunks, con el ruido
  gaussiano original y la base limitada a 500.
- Los tipos de BiomeDictionary 1.12 ya no existen. Se usan tags modernos de océano,
  río, Nether, End, badlands, taiga, bosque, playa, jungla, sabana y montaña;
  mushroom_fields conserva 0,75. Temperatura, precipitación y llanuras cubren
  los casos restantes. Es una adaptación de las afinidades originales, no una
  promesa de reproducir la base de un mundo 1.12 con el nuevo generador.
- Ciclo cada 20 ticks de servidor. Tablas lunares originales de regeneración
  (0–0,25 por ciclo) y capacidad (85–115 %); el flux ocupa capacidad del aura.
- Transferencia de hasta una unidad de vis o flux al vecino cargado adecuado,
  con los umbrales originales de diferencia y saturación. Se conservan las
  conversiones de exceso de vis y los episodios de flux por agotamiento.
- API de añadir/drenar vis y flux, con simulación de drenaje sin consumo,
  límites, devolución de la cantidad realmente transferida y rechazo de NaN,
  infinito y cantidades negativas. Consultar un chunk descargado no lo carga.
- Simulación y mutaciones en el hilo del servidor. No hay un hilo de fondo que
  modifique chunks mientras Minecraft los guarda o descarga.
- Archivo SavedData `data/thaumcraft/aura.dat` separado por dimensión, formato
  versionado, coordenadas con signo y valores fraccionarios. Los chunks
  descargados se conservan pero no se simulan. Las cargas con errores parciales
  o versiones desconocidas se rechazan; si existe un archivo que no se puede
  leer, se detiene el acceso al aura antes de sustituirlo por uno vacío.
- Paquete exclusivamente servidor → cliente, con dimensión, chunk y valores,
  cada 20 ticks por jugador. Se descarta la caché al salir, cambiar de dimensión
  o desplazarse a otro chunk; ningún paquete del cliente modifica el aura.

El aura se inicializa también en los chunks de mundos del port.4 que no tenían
estos datos. `world.generateAura=false` inicializa los nuevos registros a cero,
sin borrar los ya guardados; volver a activarlo no sustituye estados existentes.
La regeneración destructiva por `regenAura`/`regenKey` no se activa en esta entrega.
Esto no convierte archivos ni NBT de Thaumcraft para Minecraft 1.12.

## Bloques y objetos

**`thaumcraft:vis_battery`**: batería de diez unidades. Carga una unidad cuando
el vis supera el 90 % de la base, devuelve una cuando baja del 75 %, y libera
una cada cinco ticks al recibir redstone. Conserva la carga como blockstate,
emite luz y señal de comparador iguales a la carga y utiliza las cinco texturas
originales. Una devolución parcial por falta de espacio no duplica energía ni
destruye la carga. Al romperse cae una batería vacía, como en el original.
Su receta arcana queda pendiente de M06; está disponible en creativo.

**`thaumcraft:creative_flux_sponge`**: uso restringido a jugadores en creativo.
Limpia todo el flux de los chunks cargados en el área original de 9×9, sin
cargar terreno. A diferencia del límite de 500 de la llamada antigua, también
limpia cantidades mayores. Informa de la cantidad realmente eliminada.
La eliminación de rifts al agacharse depende de las entidades de M13 y todavía
no se anuncia en el objeto como funcionalidad disponible.

## Comprobación dentro del juego

Hasta portar el taumómetro y su HUD, los operadores disponen de:

```text
/thaumcraft aura
/thaumcraft aura addvis <cantidad>
/thaumcraft aura addflux <cantidad>
```

El primer comando muestra chunk, base, vis y flux. Los otros añaden cantidades
limitadas por la capacidad numérica; requieren permiso de administrador, igual
que la consulta. No se añade una lectura de aura falsa al resonador de esencia.

## Dependencias pendientes

- M01: hojas de silverwood que interactúan con el aura y crecimiento de árboles.
- M04: aspectos, cristales como objetos, investigación y HUD del taumómetro.
- M05: crecimiento/generación de los siete cristales, hierba ambiental, dioptra,
  cargador de mesa arcana, pedestal de recarga, generador de vis y sifón de vacío.
  Cada uno necesita su consumidor y, en algunos casos, aspectos, energía o rifts.
- M06/M10/M12: consumo y recarga de herramientas, focos y equipo con vis.
- M13: creación de rifts, efectos de contaminación y eliminación de sus entidades.
  El flux se conserva; no se simula haber implementado esas consecuencias.
- El resonador pertenece a M08: consulta cantidad de esencia y succión en tubos
  y recipientes, no mide aura. Se corrige su clasificación en el inventario.

## Verificación

Las pruebas de simulación y codec se ejecutan con `test`. La integración usa
fuentes optativas fuera del JAR normal:

```text
./gradlew -PauraProbe=write runServer
# tras AURA_RUNTIME_PROBE_OK, ejecutar stop y esperar el guardado
./gradlew -PauraProbe=read runServer
# debe mostrar AURA_RESTART_PROBE_OK
./gradlew -PauraProbe=client runClient
# al entrar en un mundo: AURA_CLIENT_SYNC_OK
./gradlew clean build
```

La prueba real comprueba límites y simulación de drenaje, consultas sin cargar
terreno, transferencia de la batería y redstone, comparador/luz/drops, límites
espaciales de la esponja, dimensiones y persistencia después de reiniciar.
El mundo aislado de servidor se llama `aura-step5-smoke`. Las evidencias de esta
sesión se guardan localmente en `work/`, fuera de la rama de artefactos.

Resultados de esta entrega: `AURA_RUNTIME_PROBE_OK`, `AURA_SAVE_PROBE_READY`,
`AURA_RESTART_PROBE_OK` y `AURA_CLIENT_SYNC_OK`. El cliente ha recibido al menos
tres paquetes distintos; la muestra comprobada tenía base 200, vis 200,75 y flux
0, mostrando regeneración. Se revisaron la batería colocada y cargando en el
mundo, su modelo en mano y el uso real de la esponja con mensaje de resultado.
No hubo errores de modelos o texturas. La compilación normal final y sus pruebas
se registran en `work/aura-build-final.log`.

Compilación limpia final correcta: 17 pruebas, sin fallos ni errores (7 de
simulación de aura, 4 de codec/persistencia y las 6 regresiones de WarpCodec).
El JAR normal contiene 137 definiciones de item y 111 recetas, sin clases Probe.
