# Transfusores de esencia — port.71

Se portan `essentia_input` (llenado) y `essentia_output` (vaciado), con los modelos, texturas, colisiones de media pieza, seis orientaciones y costes originales. Ambos trabajan con una unidad cada cinco ticks. El llenador aspira por la conexión trasera con succión 128; el vaciador entrega por esa conexión y no genera succión propia.

## Uso

La cara pulsada al colocar determina hacia dónde apunta la boquilla. Usar un resonador o guantelete mientras se está agachado rota la pieza. La mano vacía y el resonador muestran el estado; el contador solo aumenta cuando una unidad llega al receptor. No se añade un menú ni un coste de vis por funcionamiento. Las válvulas de la red permiten detener o distribuir el flujo.

El llenador toma esencia de tubos o dispositivos compatibles por detrás y la envía a tarros de delante. Prefiere recipientes ya ocupados por ese aspecto antes que tarros vacíos, conservando la prioridad por cercanía dentro de cada grupo. Respeta filtros, capacidad y abrazaderas. Un tarro normal lleno no consume entrada; un tarro del vacío acepta y elimina el excedente, como el original.

El vaciador necesita que su receptor trasero tenga succión positiva y pida un aspecto concreto. Un tarro etiquetado proporciona esa demanda; un tubo filtrado necesita además una fuente de succión en la red. Un depósito sin tipo solicitado no recibe esencia al azar. Un receptor lleno detiene la extracción.

El volumen original es un prisma: desplazamientos laterales de −16 a +16 y profundidad frontal de 0 a 15. No incluye la propia pieza ni los bloques de detrás. Como el original, las paredes no bloquean el transporte aéreo. La búsqueda utiliza únicamente entidades de bloque de chunks ya cargados; no fuerza terreno ni dimensiones.

Los espejos de esencia enlazados sirven de acceso a tarros remotos. Se comprueban identidad y enlace, estado soportado, chunks cargados y abrazaderas de los recipientes físicos. Las rutas se deduplican por tarro físico y el paso por el espejo paga su inestabilidad habitual. La conexión trasera a un espejo también conserva la exclusión del recipiente receptor para evitar extraer de ese mismo tarro.

## Recetas y Thaumonomicon

Ambas recetas exigen `ESSENTIATRANSPORT`, 100 vis y un cristal de Aer y Aqua. Conservan la disposición original: dos placas de latón alrededor de un dispensador para llenar, o una tolva para vaciar; debajo, dos placas de hierro alrededor de un bloque de metal alquímico. Son recetas arcanas, no atajos de fabricación normal.

`EssentiaTransportIn` y `EssentiaTransportOut` dejan de apuntar al espejo provisional y muestran los dispositivos reales. También se corrige el icono del tema. Las instrucciones en inglés y español explican orientación, alcance, demanda y reserva. `generate_transfusers.py` se ejecuta después de los generadores históricos: conserva los alias del generador de recetas, regenera las páginas y el atlas y aplica la guía actualizada.

## Conservación y guardado

Normalmente la unidad se extrae y entrega en la misma llamada del servidor. Como adaptación defensiva, se reserva físicamente una unidad después de la extracción: si el receptor rechaza la entrega tras la comprobación, esa unidad permanece en el transfusor y no se extrae otra. Puede entregarse cuando vuelva a haber un destino válido. Las simulaciones no cambian inventarios, estadísticas, esencia ni costes de espejo.

El formato versionado guarda el aspecto reservado, la fase exacta de cinco ticks y el contador. Recoger una pieza con reserva conserva esos datos en el objeto de bloque; volver a colocarla permite continuar. Un formato desconocido se mantiene íntegro, desactiva el funcionamiento y se conserva también en el botín. Una pieza sin reserva cae como bloque normal. No se modifica la generación del mundo ni el formato de las máquinas anteriores.

## Verificación

Las pruebas unitarias cubren los límites del prisma en las seis direcciones, serialización y rechazo de datos inválidos/futuros, conservación de fase/reserva, geometría y orientaciones originales, costes y alias reales del libro.

La prueba dedicada `-PbookProbe=transfuser-write runServer` comprueba los seis montajes, demanda real propagada por tubos filtrados, intervalo, tarros traseros excluidos, conservación, prioridades, etiquetas y abrazaderas, receptores llenos, excedente del vacío, espejos y coste. Una entidad de prueba adversaria acepta la simulación y rechaza la entrega: se verifica una sola extracción, espera, botín nativo con datos, recolocación y entrega posterior. Esa entidad solo existe durante la fixture y no se guarda como contenido del mod.

`transfuser-read` arranca otra JVM, recupera una reserva con fase dos y libera un hueco: tres ticks después recibe exactamente esa unidad, sin extraer otra del origen. La escena `transfuser-client` usa tickers normales, dos circuitos físicos y diez unidades por circuito. Comprueba sincronización y rotación mediante entrada nativa antes de preparar la captura. Las fixtures quedan fuera del JAR publicado.

Resultado de port.71: servidor dedicado y otra JVM de reinicio superados; cliente nativo con conservación de diez unidades por circuito, sincronización de contadores y rotación real con resonador. Captura tomada antes de generar el JAR. La compilación final ejecuta 229 pruebas unitarias.
