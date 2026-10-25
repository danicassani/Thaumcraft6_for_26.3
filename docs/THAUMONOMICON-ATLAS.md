# Thaumonomicon: mapa y páginas originales — port.69

Se sustituye la lista provisional de botones por el mapa de investigaciones y el libro abierto. Se reutilizan los fondos originales de las siete disciplinas (convertidos de JPG a PNG con los mismos píxeles para el decodificador moderno), los marcos cuadrados, redondos, hexagonales y con puntas, las ilustraciones de temas y focos, los iconos de capítulos y el pergamino de dos hojas. Las 148 posiciones y sus metadatos se recuperan de las definiciones originales; los nombres antiguos de objetos se resuelven a objetos registrados modernos o al resultado de la receta exacta del tema.

El mapa se arrastra arrastrando una zona libre con el botón izquierdo o con el botón derecho y se amplía con la rueda alrededor del puntero. Cada capítulo conserva su cámara durante la consulta. Centrar devuelve la cámara al tema raíz. Las conexiones siguen los padres de la disciplina, incluidos los requisitos por etapa. Las líneas y los iconos se recortan al marco. Los nodos son botones nativos con tooltips y narración; no se muestran los temas o capítulos que el servidor todavía considera ocultos.

Usar un icono abre el tema mediante el paquete de menú ya existente. Se espera a su selección y etapa del servidor antes de mostrar las páginas. La consulta se distribuye en dos hojas de pergamino, con requisitos, etapa, texto y recetas. Las páginas de receta conservan los ingredientes y resultados reales, cristal de vis, costes de essentia y coste de vis; se usan los símbolos originales coloreados de aspectos. Los frascos conservan el contenido y las curiosidades su variante también cuando se usan como icono. Los marcadores de salto llevan a las recetas y las flechas recorren el libro. Escape o la flecha de retorno vuelven al mapa sin cerrar el menú ni perder su cámara; Escape desde el mapa cierra el libro.

La paginación se adapta al tamaño de la ventana y al tamaño de interfaz. El texto se recompone con la fuente moderna; los separadores de línea y página originales se conservan como párrafos. No se modifica el conocimiento guardado, la progresión, los precios ni las recetas. El botón de avanzar continúa dependiendo del servidor. Arrastrar o ampliar no altera el progreso; abrir un tema conserva el inicio de su primera etapa autorizado por el menú existente, sin omitir requisitos ni conceder puntos.

No se declara una reproducción exacta de todos los efectos, animaciones de transición o del intérprete de etiquetas de Forge 1.12. Las referencias especiales sin diagramas y los textos españoles incompletos conservan el alcance documentado en `THAUMONOMICON-AUDIT.md`.

## Recursos y comprobaciones

`tools/generate_research_visuals.py` genera el atlas y valida los iconos con los objetos modernos y las texturas originales. No modifica las imágenes. Las pruebas de cámara cubren la invariancia del punto bajo el cursor, desplazamiento reversible, límites y rechazo de valores no finitos. Otra prueba contrasta las posiciones y los marcos con las 148 entradas del código original.

La escena nativa `-PbookProbe=atlas-client` comienza con conocimiento vacío y exige que solo aparezca Fundamentos, que los temas no descubiertos permanezcan ocultos y que abrir Primeros pasos no avance sus requisitos. Una segunda fase, con estado de conocimiento controlado únicamente para la prueba visual, permite revisar los siete capítulos y todos los recursos del atlas. Se comprueban clics, desplazamiento y ampliación nativos y que las cámaras no alteren las etapas sincronizadas. Las clases de la escena se excluyen del JAR publicado.

## Verificación de la entrega

Compilación limpia y 218 pruebas, sin fallos, errores ni pruebas omitidas. En el cliente nativo se comprobaron los 148 iconos, el mapa inicial con solo Fundamentos y tres temas visibles, la apertura de Primeros pasos sin saltar requisitos, las siete disciplinas, ampliación, arrastre sin modificar etapas y la receta del taumómetro con panel de cristal, seis cristales y 20 vis. Se revisaron cuatro capturas tomadas antes de generar el JAR final. La prueba dedicada de progresión también pasó: conocimiento vacío hasta alquimia inicial y descubrimientos/adendas condicionados.

El cierre del cliente de esta ejecución volvió a activar el watchdog en `VulkanGpuSurface.present` / `vkQueuePresentKHR`, al salir del mundo y cerrar la ventana, después de las comprobaciones visuales. No se declara un cierre limpio del cliente. Es la limitación gráfica del entorno ya documentada; el servidor dedicado completó su prueba y cierre.

La comprobación del JAR exige que no se incluyan escenas de prueba, conserva las entradas anteriores ajenas a la interfaz y contrasta los píxeles de los siete fondos PNG con los JPG originales.

## Ampliación port.70

El [cierre del libro](THAUMONOMICON-COMPLETION.md) añade búsqueda, formato completo de las etiquetas presentes, transiciones adaptadas, diagramas pendientes, español y abrazaderas; también corrige la disposición inferior de los controles y los márgenes. Las limitaciones de port.69 descritas arriba corresponden a esa entrega histórica.
