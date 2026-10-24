# Visión del Thaumometer — port.61

El taumómetro resalta en violeta el objetivo que aún puede aportar un descubrimiento. El panel contextual muestra su nombre y una interrogación; no revela sus aspectos antes de estudiarlo. Una vez examinado, el resaltado desaparece y se muestran los iconos originales y sus cantidades. El texto conserva un 70 % de opacidad. La explicación permanece en Primeros pasos del Thaumonomicon, con tono de diario y sin instrucciones permanentes en pantalla.

Se admiten bloques, criaturas, objetos físicos tirados al suelo y el objeto de la otra mano. Los objetos tirados se estudian por su contenido y variante, no como una entidad genérica `minecraft:item`. Las cantidades de aspectos corresponden a una unidad: escanear siete diamantes no consume la pila ni multiplica por siete el conocimiento. Los escaneos repetidos conservan la protección existente frente a recompensas duplicadas.

`ScanTarget` comparte el rayo de nueve bloques entre el escaneo y la previsualización. El suelo no oculta un objeto situado delante de él; una pared sí impide seleccionarlo. Se comprueba el terreno cargado y no se fuerzan chunks. La forma del bloque o la caja física de la entidad delimita el resaltado.

El servidor resuelve los aspectos con el mismo `AspectResolver` utilizado para investigar, incluidos los inferidos de recetas. Envía una instantánea privada al jugador que sostiene el taumómetro cada cinco ticks. Solo contiene cantidades de aspectos si ese jugador ya conoce la identidad estudiada. Mirar o resaltar algo no escribe conocimiento. El protocolo no admite peticiones de mutación desde el cliente.

El cliente exige que dimensión, identidad, posición de bloque o ID de entidad coincidan con el objetivo actual. Las instantáneas caducan, se limpian al cambiar de mundo, desconectarse o dejar de sostener el instrumento, y no se muestran a través de una interfaz abierta. Cambiar el contenido de un objeto tirado invalida la identidad anterior. El modo de la otra mano respeta la selección de agacharse tomada al empezar la animación de escaneo.

## Verificación nativa

`-PbookProbe=scan runServer` utiliza la acción nativa `Player.drop` para producir una pila de siete diamantes sobre suelo sólido. Comprueba el objetivo real, los veinte ticks de uso, la identidad del contenido, la conservación de las siete unidades, las cantidades por unidad, el rechazo de recompensas repetidas y la privacidad frente a otro jugador. También prueba una pared, un cambio de contenido y un objeto fuera de alcance. Mantiene las pruebas previas de cancelación y selección de la otra mano.

`-PbookProbe=sight-client` activa una escena integrada con un objeto tirado por el jugador, un bloque de vidrio, una vaca y esmeraldas en la otra mano. Los clics reales del cliente deben producir los descubrimientos del servidor y las instantáneas conocidas correspondientes. Antes del primer escaneo se exige una instantánea sin aspectos revelados. Las capturas comparan el objeto en el suelo antes y después de estudiarlo. La escena protege al personaje de prueba; sus clases no se incluyen en el JAR normal.

No cambia el formato de conocimiento ni los identificadores guardados de objetos o bloques.
