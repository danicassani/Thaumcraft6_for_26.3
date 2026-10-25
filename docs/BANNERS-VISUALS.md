# Estandartes y equivalencias visuales — port.72

## Estandartes originales

Se registran dieciséis colores con sus identificadores originales, incluidos `banner_lightblue` y `banner_silver`, y `banner_crimson_cult`. Las dieciséis recetas arcanas conservan sus IDs, 10 vis, investigación `BASEINFUSION` e ingredientes originales, pero ahora producen los estandartes reales de Thaumcraft. Los objetos y bloques vanilla ya fabricados se conservan intactos. No se convierten automáticamente ni se interceptan sus patrones del telar.

Los estandartes se colocan de pie con dieciséis ángulos o en cualquiera de las cuatro paredes. No tienen colisión física, como el original, y rechazan el techo. Necesitan soporte: al perderlo cae un solo objeto conservando el símbolo. Romper, copiar con selección y recolocar conserva el aspecto. Se utilizan componente de objeto y datos versionados; un formato futuro se conserva íntegro en el botín y no permite modificaciones.

Usar un vial lleno pinta su aspecto y consume el vial, como en TC6. No devuelve esencia ni un vial vacío. En creativo no se consumen materiales. Agacharse al usarlo borra el símbolo sin devolver el vial. Se rechazan viales vacíos, parciales, aspectos inválidos y aplicaciones del mismo símbolo. El estandarte del Culto Carmesí tiene su emblema fijo.

El portal mayor vuelve a colocar los estandartes del culto a seis bloques de distancia, a los cuarenta ticks de su aparición. Miran hacia fuera del portal, como en el original y solo se colocan en ubicaciones cargadas con soporte y espacio reemplazable; se protegen construcciones ocupadas. La decoración se registra en el guardado y no se regenera cada vez que se recoge.

## Equivalencias visuales resueltas

| Elemento | Antes | port.72 |
| --- | --- | --- |
| Estandartes | Objetos vanilla y patrones de telar | Cinco piezas y UV originales de 128×64, dieciséis colores y textura del culto |
| Tela y símbolo | Sin pintado de aspectos | Oscilación original; símbolo original del aspecto al 75 % de opacidad unido a la misma transformación de la tela |
| Icono del estandarte | Icono vanilla | Capas originales de poste, tela y símbolo; tinte del aspecto guardado y nombre del aspecto en el tooltip |
| Espejos enlazados | Imagen plana de apertura | Superficie animada con profundidad aparente del shader nativo de portal en las seis caras; el marco original y el estado cerrado se conservan |
| Grieta de flux | Núcleo oscuro con costuras de color | Superficie estrellada animada, cono de seis lados, oscilación/anchura originales y tres capas exteriores translúcidas |
| Lanzador del gancho | Modelo original con aviso de material sin partícula | Slot de partícula explícito y contorno solo cuando corresponde al brillo nativo; misma geometría, UV, poses y selección cargado/disparado |

La superficie de espejos y grietas utiliza el shader de portal de Minecraft 26.3, compatible con el backend moderno. El original también usaba la textura del portal del End con un shader OpenGL propio. No se copian las llamadas OpenGL 1.12 ni se representa una cámara del destino. El halo respeta el búfer de profundidad moderno; no se añade el antiguo pase que ignoraba paredes al llevar gafas. La animación solo modifica la malla de presentación: colisiones, aura, transferencias, peligros, tamaño y guardado de grietas permanecen iguales.

Los flujos de esencia, accidentes de infusión y partículas continúan usando primitivas modernas con sus colores y sucesos funcionales. Se documentan como adaptaciones de la API; esta entrega no afirma igualdad de píxeles con todos los shaders y partículas de 1.12.

## Verificación reproducible

`-PbookProbe=banner-write runServer` ejerce colocación y uso reales del servidor: dieciséis colores/ángulos, cuatro paredes, techo, los 37 aspectos con pago, borrado, selección, rechazo de viales inválidos, emblema fijo, caída al perder soporte, formato futuro y aparición de estandartes del portal. `banner-read` recupera un estandarte de pared pintado en una segunda JVM y comprueba botín y recolocación.

`banner-client` monta los diecisiete modelos, cuatro estandartes de pared, seis espejos enlazados y una grieta aislada. Requiere un clic nativo para pintar el primer estandarte antes de permitir la captura. Comprueba sincronización, recetas reales del Thaumonomicon, oscilación en tiempo normal y superficies de espejo activas. Las fixtures quedan fuera del JAR publicado.

Resultado: 234 pruebas unitarias superadas. Las pruebas dedicadas de escritura y reinicio terminan limpiamente. El cliente confirma las recetas, pintura real, sincronización, oscilación y seis caras activas de espejo. Se revisan cinco vistas y se graban GIF de tela y grieta antes de generar el JAR.
