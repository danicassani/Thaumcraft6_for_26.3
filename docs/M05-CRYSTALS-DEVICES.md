# Cristales y dispositivos de aura — port.6

Esta entrega amplía M05 sobre el núcleo de port.5. Cliente y servidor deben
usar port.6 para compartir los registros de componentes y block entities. Añade siete bloques de
cristal, un objeto de esencia con 37 variantes de aspecto, césped ambiental,
generador de vis, dioptra y pedestal de recarga. No declara terminados la
investigación, la mesa arcana, los focos ni los rifts.

## Cristales

IDs: `crystal_aer`, `crystal_ignis`, `crystal_aqua`, `crystal_terra`,
`crystal_ordo`, `crystal_perditio` y `crystal_vitium`.

- Tamaño 0–3, generación 1–4 y soporte en cualquiera de las seis caras. Las
  caras se actualizan al cambiar los vecinos; sin soporte el cristal se rompe
  y suelta sus fragmentos. En la API moderna se consideran soportes los bloques
  con cara sólida del tag de minería con pico, pues ya no existe Material.ROCK.
- No hay colisión. La forma de selección corresponde a las caras ocupadas y
  se conserva la emisión de luz 1.
- Cada intento aleatorio sigue el umbral y la probabilidad originales. Con
  aura por encima de base+10, crecer o propagarse cuesta 10 vis; el vitium usa
  10 flux. Con 10 o menos, el cristal pierde tamaño y devuelve 10 al canal
  correspondiente. Las generaciones altas limitan la propagación.
- Se conserva el drop de tamaño+1 cristales de esencia del aspecto correcto.
  Fortuna y toque de seda no permiten recuperar el bloque ni multiplicar el drop.
- Los 37 nombres y colores originales de esencia se guardan en el componente
  `thaumcraft:crystal_aspect`. No son 37 items diferentes. Se conservan al
  serializar y enviar el stack; el nombre y el modelo/tinte cambian con el aspecto.
  Esto porta los datos de esencia, no las combinaciones de aspectos ni alquimia.
- Se reutilizan los ocho fragmentos de la malla OBJ original, con 32 modelos de
  crecimiento/variación, orientación por cara y tintes. El cargador evita el fallo
  del baker OBJ de Forge 66.0.13 que ya afectaba a los pilares.
- Generación en chunks nuevos del Overworld y Nether: seis variedades primales,
  clusters en aire junto a soporte mineral, ocho intentos base en Overworld y
  uno en Nether. El reparto usa azar con afinidad adaptada a temperatura y
  precipitación de los biomas modernos. Vitium no se regala en la generación.
  `world.generateCrystals` y `world.oreDensity` controlan los intentos. No se
  hace regeneración destructiva ni se insertan cristales en chunks existentes.

## Generador de vis (`vis_generator`)

Convierte hasta una unidad de vis en un buffer de 1000 FE y entrega como máximo
20 FE/tick al receptor Forge Energy de la cara orientada. Selecciona una cara
receptora al colocar el bloque o utiliza la cara de colocación. Redstone apaga
la conversión y la salida. Las fracciones de vis inferiores a un FE no se gastan;
las que sí producen energía se convierten sin redondear a una unidad de vis.

La energía se guarda en la block entity. La capability se expone solo por la
cara de salida y se invalida al retirar el bloque. Como el original, el generador
empuja hacia el receptor; su interfaz no permite introducir FE ni extraerla por
pull. Clic derecho informa del buffer y de si está activo. Los hornos vanilla
no son receptores Forge Energy: esta conexión está destinada a otros mods.

## Dioptra (`dioptra`)

Cada segundo muestrea los 169 chunks de una cuadrícula de 13×13 sin cargar
terreno. Sin redstone representa vis; con redstone representa flux. Se conserva
la escala original 0–64, equivalente a 0–500 unidades, y la altura de la
superficie (muestra/96). Los chunks descargados se representan como cero.

La cuadrícula se sincroniza a los clientes que siguen el bloque. El renderizador
moderno representa la superficie en el mundo y cambia su color entre vis y
flux; las líneas y el sombreado sustituyen la textura/shader OpenGL antiguo.
Clic derecho actualiza la lectura y muestra base, vis y flux locales. No necesita
un menú de inventario vacío.

## Pedestal de recarga (`recharge_pedestal`)

Conserva un objeto y lo recarga hasta cinco unidades cada diez ticks desde el
vis local. Solo admite objetos que implementan la versión moderna del contrato
original `thaumcraft.api.items.IRechargable`. El equipo nativo que utiliza ese
contrato todavía pertenece a M10/M12; este lote no inventa carga para herramientas
normales ni publica un objeto de demostración como si fuera equipo de Thaumcraft.

- Inserción y retirada con clic derecho, desde la mano utilizada. La propiedad
  del stack se mueve incluso en creativo: no se crea una copia gratuita.
- Capability de inventario para automatización, un solo slot, límite de uno,
  filtro de objetos recargables y extracción por cualquier cara.
- Guardado del stack y del componente `thaumcraft:vis_charge`, sincronización y
  representación del objeto flotante. Al romper el pedestal cae el contenido una
  sola vez y se invalida la capability.
- Recarga con simulación previa y gasto entero exacto: con 3,75 vis recarga tres
  unidades y conserva 0,75 en el aura. Nunca se gasta esa fracción truncándola.

`RechargeHelper` permite consultar/consumir carga y recargar desde el aura a los
objetos que implementan el contrato. El límite serializado de carga es 32766.
La preservación de aura basada en investigación y su HUD dependen de M04/M12.

## Césped ambiental

`grass_ambient` conserva el ciclo, nieve, bonemeal, color de bioma y drops del
césped de Minecraft; emite motas nativas con poca luz. No consume aura. Las
plantas portadas aceptan también esta clase de césped.

## Recetas y dependencias que siguen pendientes

Los dispositivos están disponibles en creativo. Sus recetas especiales de
investigación/mesa arcana/infusión se incorporan con M04/M06/M09; no se añaden
recetas normales que eviten esa progresión.

Quedan dos bloques de M05 que no pueden funcionar con sus consumidores actuales:

| Bloque | Mecánica necesaria |
|---|---|
| `arcane_workbench_charger` | Mesa arcana M06 y manipulador de focos M10: amplía su acceso al aura de los chunks vecinos y abre su interfaz. |
| `void_siphon` | Rifts M13, drenaje de su energía y producción de semillas de vacío, además del inventario y la interfaz. |

Ambos conservan su código original en cuarentena. No se publican versiones que
únicamente aparenten ser máquinas. Se activarán al portar sus dependencias.

## Pruebas reproducibles

```text
./gradlew -Pm05Probe=write runServer
# esperar M05_RUNTIME_PROBE_OK, introducir stop y esperar M05_SAVE_PROBE_READY
./gradlew -Pm05Probe=read runServer
# esperar M05_RESTART_PROBE_OK y detener el servidor
./gradlew -Pm05Probe=client runClient
./gradlew clean build
```

Los fixtures están separados en `src/deviceTest` y no entran en el JAR normal.
Incluyen un objeto recargable de prueba y un receptor Forge Energy que ejercen
las APIs reales; no requieren descargar otro mod para validar las conexiones.
El mundo de servidor aislado es `m05-devices-smoke`. La escena visual se prepara
solo en el modo optativo `client`; no forma parte del comportamiento normal.

La prueba real cubre soporte, rotura, drops, crecimiento/propagación y serialización
de los 37 aspectos; generación natural; conversión/total FE, caras y redstone;
fracciones de vis, capacidad y consumo de carga; inserción/extracción, contenido
al romper y capabilities invalidadas; ambos modos de la dioptra y reinicio.
Las evidencias quedan en `work/`, fuera de la rama de artefactos.

Resultados: `M05_RUNTIME_PROBE_OK` en `work/m05-server-write-verified.log`
(incluidos 101 bloques naturales en 25 chunks), `M05_SAVE_PROBE_READY` y
`M05_RESTART_PROBE_OK` en el reinicio real. El cliente muestra
`M05_CLIENT_SYNC_OK`: cuadrícula recibida con valor central 57 y objeto del
pedestal con carga 10, recargado por el ticker automático; también se confirma
energía en el generador del cliente. Se revisaron visualmente los siete colores,
los fragmentos en la mano/hotbar, el objeto flotante y la superficie de la dioptra.
No hubo errores de modelos/texturas ni excepciones de renderizado.

Compilación limpia final correcta: 17 regresiones unitarias sin fallos/errores
(7 de simulación de aura, 4 de codec y 6 de warp). La integración nueva se prueba
en el servidor real con los fixtures anteriores. El JAR normal contiene 149
definiciones de item y 111 recetas, sin clases ni objeto de prueba. Evidencia:
`work/m05-build-final.log`.
