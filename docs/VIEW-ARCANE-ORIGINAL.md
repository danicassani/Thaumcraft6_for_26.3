# Sujeción y mesa arcana originales — port.62

El Thaumometer equipado permanece levantado en primera persona, con la lente centrada y las proporciones de su modelo original. La otra mano queda oculta visualmente, conservando su objeto y sus componentes en el inventario. Funciona con el instrumento en cualquiera de las dos manos y se dibuja una sola vez por fotograma. Las manos de soporte quedan bajas para dejar libre la lente.

Se conserva la transformación nativa de caminar de Minecraft, incluida la opción de balanceo del cliente. No se añade una segunda oscilación independiente. Mantener usar sigue iniciando el escaneo de veinte ticks; añade un leve acercamiento y temblor a la lente ya levantada. La selección de la otra mano al agacharse, el progreso y las cantidades no cambian.

La mesa arcana recupera `textures/gui/arcaneworkbench.png`, sus dimensiones de 190×234 y las coordenadas del contenedor original. Los seis cristales rodean la cuadrícula, con sus halos de coste giratorios. Se mantienen los índices de inventario, los filtros por aspecto y la salida nativa de fabricación; no se convierte la previsualización en una segunda salida ni se añade un botón. Los inventarios guardados no necesitan migración.

## Verificación

La prueba dedicada del libro y la mesa verifica la fabricación real con clic normal y Mayús, el pago de cristales y vis, el inventario lleno y el resultado parcialmente recogido. La escena optativa `-PbookProbe=view-client` comprueba en cliente la pose en reposo mediante los eventos de renderizado de brazos, el balanceo nativo al caminar, la sujeción con el instrumento en la otra mano y la sincronización de todas las coordenadas de ranuras. Un clic real sobre la salida fabrica un Thaumometer, consume los ingredientes y una unidad de cada pila de cristal y registra la fabricación para la investigación. Las capturas de la lente en reposo y de la mesa, y la secuencia al caminar, se toman antes de crear el JAR final. Las clases de prueba se excluyen del artefacto normal.
