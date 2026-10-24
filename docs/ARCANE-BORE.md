# Perforadora arcana — port.64

Entidad colocable con el modelo, UV, textura e interfaz originales de TC6. Su receta de infusión conserva la torreta básica como centro, ocho componentes, inestabilidad 4 y los aspectos originales. `ARCANEBORE` limita la colocación en supervivencia y dispone de receta gráfica e icono correcto en el Thaumonomicon.

Un pico ocupa el único hueco de herramienta. El soporte recibe redstone para activar la perforación; los raíles activadores arcanos invierten su señal como en el original, incluida la entidad transportada. El propietario la golpea para orientarla (horizontal o arriba) y puede recogerla agachándose e interactuando. El emisor parte del frente del aparato, evitando que el barrido alcance sus contenedores laterales o traseros. La recogida devuelve la perforadora y su pico como objetos físicos separados.

La excavación recorre una sección circular delante del aparato y busca superficies visibles sin cargar chunks. Radio mínimo 2, ampliado por encantabilidad y Destructivo; profundidad radio × 8 más la mejora Cavador. La dureza, velocidad del pico y Eficiencia determinan la demora. Cada bloque extraído cuesta 0,25 vis del depósito local de 10; el pico pierde un punto de durabilidad cada 50 extracciones. Se detiene antes de romper el pico. Seda, Fortuna, Sondeo y Refinado se aplican al botín; Destructivo y Cavador amplían el área en lugar de provocar roturas adicionales.

Los materiales pasan a inventarios adyacentes mediante sus capacidades de Forge; cualquier sobrante cae detrás. No se eliminan objetos previamente tirados por el jugador. Los contenedores destruidos entregan su botín nativo, sin copiar su contenido manualmente: solo se recogen las entidades nuevas producidas por esa rotura, conservando los objetos que ya había en el suelo.

Las protecciones usan la identidad del dueño y un actor de supervivencia aislado equipado con el pico de la máquina. No se alteran las manos ni el inventario del jugador. Se comprueban permisos al seleccionar y antes de destruir; una denegación no elimina el bloque ni cobra vis. El proceso pausa cuando el propietario no está conectado en esa dimensión, coherente con la autorización de los otros trabajadores del port. No perfora hacia abajo, líquidos, bloques irrompibles ni su propio soporte.

Se guardan dueño, orientación, herramienta completa (incluidos componentes), vis, activación, contador de desgaste y avance de búsqueda. Una excavación en curso se vuelve a seleccionar después de cargar, evitando conservar objetivos obsoletos.

## Audio eldritch

El generador `generate_eldritch.py` copia ahora los once OGG referenciados por ocho eventos eldritch. Los archivos son idénticos a los originales; una prueba comprueba que cada referencia dispone de su archivo y que sus bytes coinciden.

## Regeneración y pruebas

Ejecutar `tools/generate_bore_content.py` después de los generadores de conocimiento y contenido, y `tools/generate_recipe_views.py` al final. La prueba nativa dedicada se activa con `-PbookProbe=bore runServer`; la escena visual, con `-PbookProbe=bore-client runClient`. Estas fixtures quedan fuera del JAR normal.

La comprobación visual usa un cliente nativo: colocación en supervivencia, modelo original, objetivo sincronizado, excavación efectiva y entrega al cofre lateral. La pantalla sincroniza herramienta, vis, dimensiones de excavación y salud; un clic real con Shift transfiere el pico al inventario. Las capturas se realizan antes de crear el JAR de publicación.

Verificación de publicación: `clean build test`, 200 pruebas sin fallos, errores ni omisiones. La prueba dedicada completa termina con `BORE64_NATIVE_OK`, incluidas protección de roturas, Seda, desgaste 1/50, guardado/carga, menú, receta nativa, cofres con contenido, objetos existentes en el suelo, vis cero y redstone física en raíles. El cliente confirma excavación efectiva, cofre lateral conservado, modelo y objetivo sincronizados, interfaz y transferencia real con Shift. Ambos procesos guardan y finalizan correctamente. Los once OGG se verifican contra el original y se decodifican como Vorbis.
