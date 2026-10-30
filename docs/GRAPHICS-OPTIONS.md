# Opciones gráficas — port.77

Configuración de cliente: `run/config/thaumcraft-client.toml` en el entorno de desarrollo; `config/thaumcraft-client.toml` en una instalación normal. Se conservan las claves y valores predeterminados originales.

## Etiquetas de tarros

`graphics.crooked = true` aplica el pequeño giro original tanto al papel como al símbolo de los tarros normales y del vacío. El ángulo depende exclusivamente del identificador del aspecto, la coordenada X y la cara horizontal. Se conserva la expresión con resto firmado de Java, incluidos sus valores en coordenadas negativas; no se introduce un movimiento aleatorio por fotograma. `false` deja las etiquetas rectas. Las etiquetas de tubos conservan su orientación anterior.

El renderizado de etiquetas también corrige su orientación de vértices hacia fuera: anteriormente las caras miraban hacia el interior del tarro. Se usan quads con recorte de transparencia y normales de superficie, y el símbolo queda ligeramente por delante del papel. Ambos comparten el mismo pivote y transformación.

## Efecto avanzado de portal

`graphics.disableShaders = false` mantiene la superficie avanzada nativa de portal en espejos activos y en el núcleo de las grietas. Con `true`, esas mismas mallas se dibujan mediante una textura ordinaria visible, con UV, normales y luz completos. Se conservan marcos, estados cerrados, geometría, halo, colisiones y peligros.

La opción no desactiva los shaders fundamentales de Minecraft ni cambia el backend OpenGL/Vulkan. No oculta los objetos, no desactiva transferencias o aparición de grietas y no sincroniza una decisión gráfica con el servidor. La textura reutilizada es `minecraft:textures/entity/end_portal/end_portal.png`, la ruta real de 26.3; se corrige también el halo, que aún referenciaba la ruta antigua.

Los valores se leen al extraer el estado de renderizado. Los nodos diferidos conservan la elección y el formato de vértices de ese estado aunque cambie la configuración antes de su envío. Tras una recarga de configuración, los siguientes estados toman los valores nuevos sin reconstruir los chunks ni modificar entidades del mundo.

## Pruebas

Las cuatro pruebas unitarias nuevas comprueban estabilidad del giro, correspondencia de caras, resto firmado a coordenadas negativas, modo recto, orientación de las cuatro caras y separación del símbolo respecto al papel.

`-PgraphicsProbe=write test runServer` y `read` verifican en JVM separadas cuatro tarros, filtros, cantidades, enlaces de espejos y transporte real de tres objetos, incluidos cola y emisión después de reiniciar. El servidor dedicado no carga la configuración CLIENT ni necesita los renderizadores.

`-PgraphicsProbe=client runClient` alterna efecto avanzado → sencillo → avanzado y etiquetas inclinadas → rectas → inclinadas. Se contrastan los estados extraídos y los tipos de renderizado realmente enviados al colector, las matrices compartidas por papel/símbolo, el formato diferido bajo un cambio de configuración y la existencia de las texturas en los recursos nativos. La escena verifica que cantidades, filtros, enlace y tamaño/estabilidad de la grieta no cambian. Se restauran los valores originales al desconectar.

Las primeras capturas detectaron etiquetas ocultas y una ruta de textura antigua pese a que las comprobaciones de envío habían pasado. Esas imágenes y registros se conservan como diagnósticos y no se cuentan como validación final. La entrega utiliza nuevas capturas, revisadas visualmente en ambas configuraciones antes de generar el JAR.

Verificación nativa final de port.77: escritura y reinicio en dos JVM de servidor dedicadas completados; cliente con las cinco fases, estado de juego intacto y restauración de configuración al desconectar. Se revisaron las cuatro capturas finales (avanzado, inclinado, recto y sencillo) antes de generar el JAR de distribución. La suite suma 246 pruebas, incluidas las cuatro nuevas de geometría. Las escenas de prueba se excluyen de la distribución normal.
