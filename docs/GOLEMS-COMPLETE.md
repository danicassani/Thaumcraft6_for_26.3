# Golemancia — cierre de port.29

Minecraft 26.3 / Forge 66.0.13 / Java 25. Documento de estado actual que sustituye los pendientes históricos de las entregas port.20–28. El cierre se refiere a la golemancia y su cadena de fabricación; el resto del mod conserva sus pendientes independientes.

## Alcance comprobable

| Familia original | Implementación actual |
| --- | --- |
| Materiales | Gran madera, hierro, arcilla, latón, taumio y vacío, con atributos y rasgos |
| Cabezas | Básica, inteligente, inteligente blindada, exploradora e inteligente exploradora |
| Brazos | Básicos, precisos, garras, rompedores y lanzadardos |
| Patas | Caminantes, ruedas, trepadoras y levitadoras |
| Mejoras | Ninguna, blindaje, luchador y porteador |
| Progresión | 2400 diseños válidos, investigación, componentes, Machina, rangos y experiencia persistentes |
| Órdenes | Dueño, hogar, canal, trabajar/seguir/esperar, recogida, nombre, salud y carga física |
| Sellos | Los 16 tipos originales: Recoger, Guardar, Vaciar, Vigilar, Cosechar, Romper; cinco avanzados; Proveer, Reponer, Usar, Talar y Carnicero |
| Configuración | Prioridad, redstone, seis orientaciones, áreas X/Y/Z, filtros múltiples, componentes/desgaste, equivalencia de materiales y procedencia |
| Logística adicional | Conservar uno, suministro individual, vaciado cíclico, guardar solo existentes, cuotas y compartición explícita entre dueños |
| Agricultura | Replantar configurable, solicitudes de semillas físicas, cola persistente, cultivos básicos, verrugas, cacao, bayas y plantas altas |
| Combate | Vigilancia con categorías combinables, protección de aliados/PVP, carnicería, defensa propia/del dueño y defensa a distancia |
| Rotura | Progreso y grietas, tiempo según dureza, seda más lenta, botín nativo con herramienta de diamante, contenedores y sus objetos |
| Prensa original | Montaje con salis, cuatro orientaciones, componentes físicos reversibles, esencia/trabajo/resultado persistentes y modelo OBJ animado |
| Fabricación auxiliar | Fabricador de patrones: diez disposiciones originales, recetas normales, inventarios superior/inferior, recipientes restantes, vis y pausa redstone |
| Presentación | 17 OBJ originales de piezas y OBJ de prensa, materiales y texturas originales, accesorios, carga visible, colores, animación y HUD con campana |

## Opciones nuevas

La última página de los sellos muestra las opciones aplicables y Compartido. Vaciar avanzado puede recorrer las muestras en orden cíclico y dejar una unidad de cada grupo aceptado. Proveer puede limitar cada extracción a una unidad y conservar la última. Guardar avanzado admite cuotas por muestra y Solo existentes; conserva físicamente lo que exceda la cuota. Sin inventario de destino puede dejar los objetos físicamente, contando las existencias cercanas para las cuotas. Vigilar avanzado permite combinar monstruos, animales y jugadores, con los permisos y protecciones habituales.

Compartido permite trabajar a gólems de otros dueños del mismo canal; solo el dueño puede configurar el sello. Las reservas de reposición cuentan también la carga de esos colaboradores. Los sellos anteriores permanecen privados al actualizar. Cambiar las opciones invalida la tarea/reserva, como las demás ediciones.

Cosechar conserva Replantar activado por defecto. Puede utilizar una semilla del botín real; cuando no la hay conserva una solicitud de replantado, acotada al área, y Pedir semillas permite buscarla en un proveedor. La colocación usa una interacción nativa de supervivencia, consume la semilla y pasa los eventos Forge. Desactivar Replantar impide crear/ejecutar esas solicitudes. En plantas altas se conserva la base; las bayas conservan el arbusto. La cola está limitada a 256 posiciones por sello y no carga chunks.

Los luchadores responden a agresores recientes del gólem o del dueño; al seguir también pueden asistir al dueño. Esperar impide la defensa. Los dardos de defensa están ligados al trabajador, dueño, canal y agresor concreto y vuelven a validar sus permisos al impactar. Cambiar a Esperar revoca el impacto. Nunca pueden redirigirse hacia el dueño o un aliado.

## Fabricación y prensa

No existe un sello de crafteo independiente en el registro original. La cadena automatizada utiliza proveedores y Reponer/Guardar para alimentar el inventario superior del fabricador. El inferior recibe el resultado y todos los recipientes de la receta normal. No fabrica recetas arcanas, de crisol o de infusión para saltarse sus costes.

Antes de extraer se comprueban ingredientes idénticos, extracción por cara y capacidad conjunta para resultado y recipientes. Se consume una unidad de vis por operación desde una reserva persistente de hasta cinco. Un destino lleno o la redstone detienen la fabricación. Si un inventario de otro mod cambia su respuesta durante una operación, los objetos extraídos se devuelven o caen físicamente; los resultados que ya se hayan producido nunca desaparecen por falta de espacio. No se simulan ingredientes ni se generan copias para abastecer.

Para la prensa multibloque, coloca un pistón hacia arriba, barras encima, caldero a un lado, mesa de piedra al lado perpendicular y yunque en la esquina restante del cuadrado 2×2. Usa salis sobre el pistón con la investigación correspondiente. Se admiten las cuatro rotaciones. Las cuatro piezas auxiliares se guardan como partes del montaje, conservando sus estados originales. Romper el controlador devuelve un pistón y restaura las otras piezas; romper una pieza devuelve esa pieza y restaura el resto. Un trabajo incompleto no devuelve los materiales ya pagados ni genera un gólem. El resultado terminado cae una sola vez. Los montajes que atraviesan chunks esperan a que sus partes estén cargadas, sin forzarlas.

## Presentación y adaptaciones modernas

Se sustituyen los cubos aproximados del trabajador por las mallas originales, con sus grupos de objetos, UV y normales. Se seleccionan las cabezas, brazos, patas y accesorios por diseño. Se animan caminar, rueda, garras, trituradora, postura de disparo y trabajo; el porteador muestra ambas cargas. Los modelos y las texturas se reconstruyen desde el paquete activo cuando se recargan los renderizadores. La prensa ensamblada usa sus 316 triángulos originales; las piezas de gólem suman otros 884.

Se mantienen tres adaptaciones deliberadas: los sellos ocupan celdas de placa sin colisión junto al soporte; las pantallas y el HUD usan la interfaz nativa moderna; las prensas compactas de partidas anteriores y su receta siguen disponibles junto al montaje original. No se intenta compatibilidad binaria con extensiones de la API de Minecraft 1.12.2. Estas decisiones no borran ni convierten automáticamente construcciones anteriores.

## Compatibilidad

Se conservan IDs y ordinales de sellos, diseños y datos de port.28. Los campos nuevos tienen valores compatibles: sin compartición, sin restricciones adicionales de extracción, replantado activado, y categorías de vigilancia derivadas del selector anterior. Se guardan cursor, cuotas, permisos, cola de semillas, reserva/patrón del fabricador y estados de la prensa. Las cargas solo se copian a datos de renderizado para mostrarlas; esos datos nunca son inventarios ni botín. No cambia la generación del mundo. Cliente y servidor deben llevar la misma versión.

## Verificación

Las fixtures `finish-write/read/client` se excluyen del JAR normal. El servidor dedicado cubre cuatro montajes mediante uso real de salis, desmontaje, trabajo pagado, nueve recetas válidas en diez patrones, disposición inválida, bloqueo de salida/redstone, consumo exacto de vis, permisos compartidos y opciones/cursor/cola persistentes. El reinicio comprueba esos datos y un sello real de port.28.

La prueba de cliente recorre vaciado cíclico → cuotas de Guardar, clics reales de Solo existentes y cantidades, suministro individual, vuelo con carga, proveedor → Reponer → fabricador, recipiente de receta, replantado pagado, desactivación de replantado, caña, defensa de cuerpo a cuerpo y dardo, minería progresiva y contenido de cofre. Al terminar presenta seis diseños que reúnen los seis materiales, las cinco cabezas, los cinco brazos, las cuatro movilidades y las mejoras junto a la prensa original. Las capturas relevantes se comparten antes de generar el JAR y pushear.

### Ejecuciones verificadas (20 de octubre de 2026)

- `finish-write`: montaje con salis en cuatro orientaciones, desmontaje reversible, reserva de componentes y Machina, diez patrones y opciones de sellos.
- `finish-read`: reinicio real del servidor, recuperación del trabajo y datos de port.28; replantación nativa de cacao pagando una semilla en cada una de las cuatro orientaciones.
- `finish-client`: recorrido completo en un mundo cargado, con clics reales en las pantallas de sellos; conservación de oro/esmeraldas, suministro y vuelo, fabricación, cultivos, defensa, obsidiana y cofre con exactamente tres diamantes. Guardado y salida normales.
- `finish-view`: comprobación adicional de las mallas, carga, canales, modo y luz exterior de la prensa. Captura real de 1280×720 compartida antes de generar el JAR o pushear. SHA-256 de la imagen: `72d894bef8b283a2f2cc31d5cee6fde868e127b50d45d3e3e42776ec9ebe9bbb`.
- Cadena de los 17 generadores ejecutada dos veces: segunda pasada sin cambios. Todos los JSON de recursos se pueden analizar.

Las ejecuciones anteriores usan mundos desechables de verificación. Las fixtures no forman parte del artefacto de juego.

`clean build`: **120 tests, 0 fallos, 0 errores y 0 omitidos**. JAR normal comprobado sin fixtures, con 296 recetas, 258 definiciones de objeto y las 18 mallas/1200 triángulos.

Artefacto: `thaumcraft-6.1.BETA26-port.29-forge66.0.13.jar` (8055333 bytes). SHA-256: `a99d56454eecd0c4998342f3a251b23a0a4c72f6646da0e40cd559bd2a5e89e9`. La compilación se realizó después de compartir la captura anterior.
