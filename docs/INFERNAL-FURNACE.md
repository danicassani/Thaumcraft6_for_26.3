# Horno infernal — port.32

Tercer lote de alquimia avanzada, para Minecraft 26.3 / Forge 66.0.13 / Java 25. Añade el multibloque, Salis, cocción nativa, aura, fuelles, bonificaciones y persistencia. No cambia la generación del mundo. Thaumatorium y condensador siguen pendientes.

## Formación y desmontaje

Estructura de 3×3×3: 12 bloques de ladrillos del Nether y 12 de obsidiana. Las cuatro esquinas de cada capa son ladrillos; el resto del marco es obsidiana. En la capa inferior el centro es obsidiana, en la intermedia una fuente de lava, y arriba queda abierto. Un lateral de la capa intermedia lleva barrotes en lugar de obsidiana: ese es el frente.

Con `INFERNALFURNACE` investigado, aplica Salis Mundus a un bloque del marco o a los barrotes. Admite las cuatro orientaciones. Primero comprueba toda la estructura cargada, permisos y eventos de protección; después transforma el conjunto y consume una unidad de Salis, salvo en creativo. Rechaza lava que no sea fuente y estructuras incompletas. Registra una prueba de fabricación.

Los 24 bloques del marco conservan posición y propietario del multibloque. Al romper una pieza se restauran las restantes y los barrotes; el controlador vuelve a lava. La pieza rota usa su drop de material original, con herramienta apropiada. Al romper directamente el controlador se restauran los materiales y el bloque eliminado queda eliminado. No existe un horno transportable ni una receta de mesa para saltarse el montaje. El objeto con su ID sirve como icono del catálogo.

Los inventarios pendientes se devuelven una sola vez, en una esquina superior del marco para que no caigan en la lava central. La experiencia ya ganada también se entrega. Si faltan chunks, se pausa el trabajo; no se fuerzan cargas ni se destruye una estructura porque otra parte esté descargada.

## Cocción y entradas

Utiliza recetas nativas `SMELTING`, incluidos sus resultados y componentes. No necesita combustible. Tiene 32 ranuras de entrada mediante la capacidad superior de objetos; no permite extraer por esa capacidad. La simulación no cambia el inventario y los objetos no cocinables se rechazan en automatización.

También recoge objetos arrojados por la abertura superior. Los no cocinables se destruyen con efectos, como en el original; si falta sitio para un objeto válido, su resto sigue en el mundo. Los seres vivos que entran en la cámara reciben daño de lava y fuego.

Cada operación tarda 140 ticks sin carga de aura o 80 con carga. Al iniciar trabajo puede pagar hasta 20 unidades enteras de vis; cada cocción consume un crédito. No drena aura estando vacío ni descarta fracciones de vis al convertirlas en créditos. La carga pagada persiste al guardar.

## Fuelles y bonificaciones

Hasta cuatro fuelles a dos bloques del centro, en cualquier cara salvo la superior y orientados hacia el horno. Redstone desactiva cada fuelle. La reducción original es `b × (21 − b)` ticks: con 0/1/2/3/4 fuelles son 140/120/102/86/72 ticks sin aura y 80/60/42/26/12 con aura. Los cambios se aplican al siguiente trabajo.

Cada operación realiza 1 + fuelles tiradas de las bonificaciones originales: pepitas de metales o cuarzo, trozos de carne y tierras raras. Hay 27 reglas con probabilidades originales, incluida la base 0,33 y las excepciones 0,01–0,025. Adaptación moderna: minerales profundos y metales en bruto usan los grupos de su metal; bacalao y salmón dan el trozo de pescado. Los metales de otros mods pueden coincidir mediante etiquetas `c:ores/...`, si tienen una receta de cocción válida. Las bonificaciones se agrupan sin alterar su cantidad.

Una de cada veinte operaciones puede añadir una unidad de flux, como en el original. La experiencia corresponde al resultado principal de la receta, sin experiencia adicional por las bonificaciones.

## Salida y guardado

Expulsa por el frente. Puede entregar a un contenedor en la abertura o justo fuera del marco; sin contenedor deja objetos físicos en la boca. Una boca bloqueada detiene el trabajo. Si el contenedor está lleno, conserva las salidas pendientes en lugar de tirarlas al suelo. Admite entregas parciales y no vuelve a sortear bonificaciones. La experiencia espera hasta entregar la cola.

Formato nuevo versión 1: 32 entradas, cola de resultados ya pagados, experiencia pendiente, ranura en proceso, tiempo exacto y créditos de aura. Los objetos conservan sus componentes mediante codecs con contexto de registros, incluidos nombres y encantamientos. Formatos futuros o datos incompatibles se conservan sin reinterpretarlos y con la máquina desactivada. No cambia IDs ni formatos de las máquinas anteriores.

El modelo y textura del multibloque son los originales, con referencias modernas para lava y fuego animados. Las piezas del marco no duplican el dibujo del modelo central. La mano vacía sobre el marco muestra entradas, reloj, carga, fuelles y cola.

## Verificación

Las fixtures `-PalchemyProbe=infernal-write/read/client` usan mundos desechables y no forman parte del JAR normal. Comprueban investigación, Salis y protección, cuatro orientaciones, restauración y conservación, automatización, aura y fuelles, colas bloqueadas, componentes, reinicio y cocina con objetos reales. El cliente muestra el multibloque en marcha y permite abrir el cofre real de resultados.

### Resultados nativos

- Servidor dedicado: investigación pagada con 16 puntos de observación de Artificio y los tres objetos originales; rechazo sin investigación y por evento de protección sin consumir Salis ni transformar piezas. Cuatro orientaciones completas y desmontaje con exactamente tres entradas devueltas, sin contar objetos de pruebas anteriores.
- Entrada superior simulada y real, rechazo de palos por automatización y destrucción al arrojarlos; relojes de 140/80/12 ticks, 20 vis pagados exactamente, consumo de un crédito por cocción, cuatro fuelles y desactivación individual con redstone.
- Boca tapada, cofre lleno durante 80 ticks y posterior entrega: sin consumo prematuro, pérdidas ni nuevas tiradas. Datos de versión futura preservados y capacidad desactivada. Un arma con nombre y encantamiento conserva sus componentes al serializar.
- En 330 ticks normales del mundo, dos minerales arrojados se recogieron, cocinaron sin combustible y entregaron como exactamente dos lingotes al cofre nativo.
- Reinicio real: 17 ticks pendientes, tres entradas, 19 créditos y marco completo recuperados; reanudación con un único lingote y un crédito consumido.
- Segundo horno guardado con dos lingotes, tres pepitas, cinco puntos de experiencia y siete créditos ya pagados. Recuperación exacta de toda la cola y de un arma con nombre y encantamiento; desbloqueo y entrega una sola vez, incluidos exactamente cinco puntos de experiencia. Se cargó también la centrifugadora real del mundo de prueba de port.31.
- Cliente nativo: mismo recorrido de formación, cocina y red; comprobación de las 24 piezas sincronizadas, trabajo activo, carga y tres fuelles. Apertura real del cofre con lingotes y bonificaciones generadas por el horno. Guardado, regreso al título sin capturarlo y cierre normales. Todas las ejecuciones finales terminan con `BUILD SUCCESSFUL`.

Dos capturas reales de 1280×720 compartidas antes de generar el JAR y pushear. SHA-256 de instalación: `bee1d46279944731813472f4a0a9f293066c478aa3729625c9e9ef0ef650a5e2`. SHA-256 del cofre: `031e6d23359d76950f9a9e97e77db778e5dc6210491641083535770bd6ee0fa6`.

Cadena de 20 generadores ejecutada dos veces, sin cambios en la segunda pasada. Generador infernal revalidado después de cotejar las probabilidades originales. Se conservan las entradas anteriores de la etiqueta de minería.

Compilación limpia y comprobación final: **134 tests, 0 fallos, 0 errores y 0 omitidos**. JAR sin fixtures, con 304 recetas, 265 definiciones de objeto, modelo con recorte de transparencia y todos los JSON válidos. Se conservan las clases, recursos y etiquetas de minería de las máquinas anteriores.

Artefacto: `thaumcraft-6.1.BETA26-port.32-forge66.0.13.jar` (8127414 bytes). SHA-256: `8781348683bf289fd182a4e188f7e1f2a326425c34fbfcbce662cb928eea344d`. Generado después de compartir las capturas.
