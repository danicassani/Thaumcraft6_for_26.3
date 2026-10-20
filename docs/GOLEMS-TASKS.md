# Usar, Talar y Carnicero — port.22

**Estado actual:** [cierre de golemancia en port.29](GOLEMS-COMPLETE.md). Este documento describe una entrega histórica; sus listas de pendientes quedan sustituidas por el estado actual.

Minecraft 26.3 / Forge 66.0.13 / Java 25. Se añaden los tres tipos de sello restantes: dieciséis tipos registrados en total, conservando los trece identificadores y posiciones anteriores. M11 continúa parcial por opciones, movilidad, experiencia y presentación pendientes.

## Usar

Requiere Inteligente y Hábil. Un filtro de muestra, comparación exacta de componentes o solo de tipo, lista blanca/exclusión, propietario, canal, prioridad y redstone. Usa un objeto real de la carga; las muestras fantasma no pagan acciones. Puede recibir carga mediante Vaciar o, al activar Pedir objeto con una muestra blanca, obtener un objeto de Proporcionar. Reutiliza su recorrido físico, reservas y guardado de destino/carga de port.21. Solo pide una unidad por viaje; sin muestra, con exclusión o con mano vacía no genera pedidos. No fabrica objetos ni toma del inventario del dueño.

La tercera página configura clic izquierdo, objetivo aire, mano vacía, agacharse y petición. Sin aire, usa la cara marcada del soporte. Con aire exige vacía la celda delante del sello; el clic derecho se dirige a la cara superior del bloque bajo esa celda para colocar allí. El gólem se coloca a dos bloques de esa celda antes de actuar para no impedir la colocación con su cuerpo. La colisión y las reglas de colocación son nativas. Activa Agacharse si el bloque bajo esa celda intercepta el clic, por ejemplo otro sello o un contenedor. Cambiar el objetivo durante un viaje conserva la carga, pero puede dejar la tarea sin acción válida.

Ejecuta interacciones mediante un actor de supervivencia no añadido al mundo, con inventario independiente y UUID del gólem. No cambia la mano, posición, modo o inventario del dueño. Comprueba los permisos básicos del dueño y pasa los eventos Forge con la identidad del actor; un gestor de protección puede denegarlo. No concede permisos de operador. Retorna herramientas gastadas, recipientes y otros resultados al inventario físico del gólem; lo que no cabe cae al suelo. El actor no abre ni opera menús. Para usar repetidamente una herramienta desgastable conviene desactivar Datos exactos, ya que el daño forma parte de sus componentes.

El clic izquierdo realiza una rotura nativa por acción, con herramienta, desgaste, botín, permisos y eventos; no reproduce el progreso temporal de minado de un jugador. Si elimina el soporte, el sello se desprende y cae como objeto. Una azada requiere aire sobre la tierra; monta el sello en un lateral para labrarla. No hay clics en entidades, clic derecho mantenido ni automatización de menús. Coincidencias originales por mod/etiquetas y separación entre daño y otros datos siguen pendientes. Una colocación o cambio de estado puede desactivar el objetivo hasta que vuelva a cumplir la condición. La redstone del propio soporte también puede pausar el sello.

## Talar

Requiere Inteligente y Rompedor. Encuentra un tronco inferior dentro del área y corta primero el tronco conectado más lejano del mismo tipo de bloque, una acción por segundo. Recalcula tras cada corte. El radio horizontal es 1–8; verticalmente abarca desde un bloque bajo el sello hasta el doble del radio sobre él. La búsqueda considera hasta 256 troncos por paso y solo chunks cargados. No fuerza chunks ni realiza una tala completa en un único tick.

Cada corte vuelve a comprobar soporte de trabajo, visibilidad del tronco, área, permisos del dueño, dureza, ausencia de entidad de bloque y evento Forge de rotura. Usa el botín de un hacha de hierro. Introduce objetos en la carga y deja el exceso en el suelo, donde Recoger puede obtenerlo. Las hojas usan el decaimiento nativo. No replanta ni distingue árboles naturales de troncos colocados: el área también puede incluir construcciones de madera. Ramas desconectadas no se cortan como parte del mismo trabajo.

## Carnicero

Requiere Inteligente y Luchador. Selecciona adultos de un tipo de entidad del que haya más de dos adultos válidos en el área. Antes de cada golpe vuelve a contar, también si trabajan varios gólems o varios sellos. Crías, domesticados, animales con correa, aliados y mascotas del dueño no son objetivos ni cuentan como la pareja reproductora. La pareja restante puede haber recibido daño antes de completarse el trabajo; al quedar dos, cesan los golpes.

Usa daño y botín de entidades nativos, con distancia, navegación y visibilidad. El botín queda en el suelo para Recoger. No cría, alimenta ni administra inventarios de animales. Los tipos y edades provienen de la API moderna Animal; criaturas ajenas que no la implementen no se incluyen.

## Progresión y guardados

Usar: crisol, sello en blanco, Fabrico 20 / Sensus 10 / Cognitio 20, investigación SEALUSE. Talar: crisol, Romper básico, Herba 40 / Sensus 20, SEALLUMBER. Carnicero: infusión de Vigilar básico con cuero, lana de cualquier color, piel de conejo y carne cruda de cerdo/oveja/vaca; Bestia 10 / Sensus 10 / Humanus 10, inestabilidad 0, SEALBUTCHER. Se conservan las recetas originales y sus entradas del libro.

Los cinco ajustes de Usar son campos opcionales nuevos y cargan desactivados. No cambia el formato de los diseños, inventarios ni filtros anteriores. La ruta opcional de carga admite Usar además de Abastecer. Talar y Carnicero reconstruyen sus trabajos y vuelven a validar el mundo al cargar; no guardan listas de árboles ni víctimas. Los mundos con bloques nuevos no deben abrirse con port.21 sin restaurar una copia previa.

## Pendiente

Movilidad con ruedas/trepadoras/vuelo, cabezas exploradoras, dardos, rangos/experiencia, modelos OBJ y animaciones originales; opciones avanzadas de coincidencia/área y tareas de fabricación. Registrar los dieciséis sellos no equivale a haber completado toda la golemancia original.

## Verificación

Fixtures aisladas `tasks-write`, `tasks-read`, `tasks-client`, excluidas del JAR normal. Servidor: interacción vacía, denegación Forge, desgaste de azada/hacha, pago al colocar, devolución de cubo, recorrido de troncos, recuento de adultos y configuración persistente. Cliente: viaje real a proveedor y vuelta, pausa por canal/redstone y restauración de carga para Usar; tala progresiva con cancelación y tronco testigo fuera del radio; dos carniceros con adultos, cría y mascota. Clics nativos verifican las opciones nuevas del menú.

La pasada nativa verifica petición de una unidad desde cinco, pausa/recarga del gólem y una colocación pagada, además de interacción vacía; ocho troncos conectados con ramas y un tronco testigo fuera del área; cuatro adultos reducidos a dos con dos carniceros, conservando cría y mascota sin daño. La aproximación de colocación usa alcance de navegación cero para alcanzar la casilla indicada, evitando detenerse en una casilla vecina. La infusión admite lana roja y devuelve Carnicero. El reinicio dedicado conserva también un pedido físico de Abastecer guardado con port.21.

Compilación limpia: 86 pruebas correctas, 0 fallos/errores/omitidas. 293 recetas, 256 definiciones de objeto y 16 tipos de sello. JAR sin fixtures: `thaumcraft-6.1.BETA26-port.22-forge66.0.13.jar` (7951119 bytes), SHA-256 `c4dbe8bc886bd354b289ab08b4a2fe7b19b8788c660c8b30882d64ae156b22cf`. Cliente cerrado normalmente tras guardar; servidor y reinicio concluyen correctamente.

Actualización posterior: [Movilidad de gólems — port.23](GOLEMS-MOBILITY.md) incorpora ruedas, trepadoras y levitadoras.
