# Thaumonomicon — port.70

Se cierra la lista concreta del libro de port.69: buscador, transiciones, formato de páginas, doce referencias especiales, traducción y abrazadera. No es una certificación de paridad de todo el mod ni una reproducción binaria del renderizador 1.12.

## Consulta y composición

El buscador consulta los títulos localizados de las investigaciones visibles y pertenecientes a capítulos descubiertos. La comparación ignora mayúsculas y tildes; presenta hasta ocho resultados y abre el tema mediante el menú y sus paquetes existentes. No concede conocimiento ni revela temas ocultos. La escritura tiene prioridad sobre la tecla del inventario cuando el campo está enfocado. La cámara se ajusta al cambiar el tamaño de ventana.

Las seis etiquetas del formato original se interpretan: `BR`, `LINE`, `DIV`, `PAGE`, `IMG` y su cierre. Los saltos `PAGE` producen hojas distintas. Las líneas y divisores usan el recurso original; las imágenes conservan sus regiones y escalas normalizadas originales y se adaptan al espacio disponible. Los estilos `§` conservan negrita, cursiva, subrayado y colores. Las imágenes inválidas no se imprimen como rutas internas. Se añaden una transición de hoja con pliegue/sombra, sonido de página y pulsación visual de los nodos señalados; son adaptaciones al renderizador moderno.

Los títulos admiten dos líneas. El cuerpo tiene márgenes propios en cada hoja y deja libre una zona inferior para numeración y controles. Receta y Continuar se sitúan dentro del pergamino, con texto centrado, separados de las flechas y de los números. El subtítulo de receta se envuelve en lugar de recortarse. La altura de las ilustraciones depende del área interior real.

Se corrigen también las vistas de inventario de los tres pedestales y la matriz: poses de interfaz, iluminación frontal y UV del atlas de la matriz. Los modelos y renderizadores colocados en el mundo no cambian. Los recursos finales se contrastan con los que se recargaron y revisaron en el cliente nativo.

## Contenido

`generate_book_completion.py` completa las doce referencias, crea dieciséis recetas arcanas de estandartes de colores con resultados vanilla, las tres purificaciones opcionales basadas en etiquetas y sus páginas. Los diagramas se presentan por capas numeradas de abajo arriba y muestran los objetos registrados reales con información al pasar el cursor. La planta de la prensa y la disposición del horno coinciden con los ensambladores modernos. Los estandartes conservan las capacidades vanilla de colocación y telar; no se añade el antiguo pintado directo con símbolos de esencia.

Las etiquetas de minerales opcionales están declaradas vacías. Sin proveedores no hay coincidencia posible y el libro muestra su indisponibilidad. No se crean minerales nuevos en el mundo. Los grupos de accesorios remiten a recetas existentes, no a una dependencia del antiguo Baubles.

Las 433 claves de investigación referenciadas tienen español. `tools/es_research_texts.lang` mantiene las 378 incorporadas en esta entrega, con instrucciones adaptadas a los controles modernos y sin sustituirlas por claves internas. También se traducen las glosas de los aspectos, incluidos sus nombres en las etiquetas y los viales.

## Tarros y compatibilidad

Usar una abrazadera sobre un tarro normal o del vacío cierra su tapa y consume una unidad. No se aplica dos veces. Los viales y las fuentes remotas de infusión/espejos respetan el cierre; las conexiones de tubos continúan funcionando. El estado se guarda y sincroniza y se representa mediante dos bandas de latón sobre la tapa. Al recoger el tarro se devuelven por separado el recipiente con su esencia/filtro y una abrazadera. Los datos antiguos sin el nuevo campo significan tapa abierta. No cambia la versión del conocimiento ni se eliminan identificadores publicados.

## Verificación

Las pruebas nativas de servidor comprueban ambos tarros, pago, rechazo de aplicación duplicada, viales en ambos sentidos, exclusión de extracción remota, transporte por tubos, serialización, botín y migración de tarros antiguos. También se repite la progresión inicial hasta Alquimia y se comprueban todas las referencias y las dieciséis recetas de estandartes en el registro real.

Las pruebas del cliente comprueban el estado inicial oculto, búsqueda sin tildes, apertura nativa, límites de títulos/cuerpo/controles y objetos reales de los diagramas. Las escenas usan conocimiento avanzado controlado para revisar presentación; no se declaran una ruta de supervivencia ganada. Las fixtures se excluyen del JAR final.

## Entrega verificada

La compilación limpia `clean build test` final pasa 224 pruebas, sin fallos, errores ni omisiones. Cliente y servidor finalizan con normalidad en sus ejecuciones finales. Las revisiones intermedias con problemas de arranque SDL o watchdog de Vulkan se conservan como registros separados y no se cuentan como comprobaciones satisfactorias.

La interfaz se comprueba en una ventana nativa de 1600 × 960 a escalas 2, 3 y 4, con áreas GUI de 800 × 480, 534 × 320 y 400 × 240. Se revisan márgenes, títulos, numeración, Receta/Continuar, los seis diagramas de estructuras, ilustraciones en el texto, separadores, etiquetas de varios aspectos —incluido un nombre de dos líneas— y el aviso de minerales opcionales ausentes. Las ocho capturas de comprobación se tomaron antes de generar el JAR. Los cambios del grupo de seis accesorios básicos se contrastan adicionalmente con las recetas y los objetos registrados en el servidor final.

La verificación del artefacto contrasta los modelos de inventario con los recursos recargados en el cliente y conserva las entradas de port.69 ajenas a los cambios enumerados. No incluye escenas ni clases de pruebas.
