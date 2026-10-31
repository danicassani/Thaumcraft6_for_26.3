# Escaneo en agua y líquido del crisol — port.80

## Escaneo

Mientras el jugador toca agua o está sumergido, `ScanTarget` omite únicamente la forma del fluido etiquetado como agua. El rayo compartido por previsualización y escaneo sigue respetando las formas de los bloques: una pared o un bloque anegado no se vuelven transparentes. La lava y los demás fluidos conservan su intercepción. Desde tierra, el agua sigue siendo un objetivo válido.

Se conservan los nueve bloques de alcance, la comprobación de terreno cargado, el objetivo de entidades/objetos más próximo y los veinte ticks de uso. No se usan coordenadas solicitadas por el cliente ni se fuerzan chunks.

## Crisol

El líquido del crisol deja de utilizar los quads de depuración de color plano. Su superficie superior usa el sprite nativo `minecraft:block/water_still` del atlas de bloques, con UV, normales, luz y overlay completos. El tinte de agua procede del bioma; la cantidad de esencia aplica el oscurecimiento violáceo del renderizador original. El tono no se calcula a partir del cielo.

La superficie usa profundidad y una capa opaca para que el fondo del cielo no intervenga en su apariencia. Se mantienen el nivel del líquido y las paredes del modelo. Un crisol sin agua no muestra superficie de agua aunque conserve esencia. Los tarros y alambiques conservan su representación y colores existentes. La extracción captura atlas, UV, color y altura para la geometría diferida, sin modificar el contenido de la máquina.

No cambia el formato guardado ni las cantidades, recetas, calentamiento o consumo de agua.

## Verificación

- Tres pruebas de color: agua limpia con tinte propio y alfa opaca; fórmula original de manchado; saturación ante exceso de esencia. Suite completa de la entrega: 253 pruebas.
- `-PbookProbe=water test runServer`: jugador seco, sumergido y con agua solo en los pies; agua corriente; pared; bloque anegado; lava; pila física de siete amatistas escaneada durante veinte ticks sin consumirla; terreno no cargado.
- `-PbookProbe=water-client runClient`: comparación de crisol lleno limpio, lleno con esencia, vacío y caldero vanilla. Se comprueban el sprite real del atlas, color del bioma, alturas, el formato de los cuatro vértices enviados y la conservación del contenido. Una segunda escena exige escanear un bloque de diamante desde dentro de un tanque de agua mediante entrada real, con vista previa coincidente y sin escanear el agua ni consumir la mano opuesta.

Las capturas deben mostrar la comparación del líquido y el objetivo estudiado bajo el agua antes del JAR final. La escena regresa al menú y se cierra tras completar el guardado. Las fixtures quedan fuera de la distribución normal.

Verificación nativa final: servidor y cliente completan sus marcadores. Se revisaron las dos capturas del cliente antes de generar la distribución. El agua limpia coincide con el tinte del bioma, la mezcla con esencia se distingue y el crisol vacío no muestra líquido. El objetivo submarino fue registrado por el servidor y mostrado como conocido por el cliente.
