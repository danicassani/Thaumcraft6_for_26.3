# Sellos avanzados y abastecimiento — port.21

Minecraft 26.3 / Forge 66.0.13 / Java 25. Amplía los gólems de port.20 con siete sellos: Recoger, Guardar, Vaciar, Vigilar y Romper avanzados, Proporcionar y Abastecer. Hay trece sellos en total. M11 sigue parcial.

## Filtros e interfaz

Recoger/Guardar/Vaciar/Romper avanzados tienen nueve muestras. Requieren Inteligente, más Luchador o Rompedor donde proceda. Vigilar avanzado conserva una sola muestra sin uso en su selección: se configura por tipo de objetivo. Proporcionar y Abastecer tienen nueve muestras y conservan las reglas originales: no necesitan Inteligente y rechazan Torpe.

La primera página configura canal, prioridad, radio, redstone y muestras. Pulsa un hueco fantasma para seleccionarlo y Fijar filtro para copiar el objeto en mano; si sostienes la campana, toma la secundaria. No se consumen ni se pueden retirar las muestras. Los huecos que no corresponden al tipo de sello no son seleccionables. La segunda página permite exclusión y coincidencia de datos exactos; sin datos exactos compara solo el tipo de objeto. Una lista blanca vacía admite cualquiera y una lista de exclusión vacía no admite nada. Abastecer exige muestras y no admite exclusión.

Cada muestra de Abastecer tiene un objetivo entre 1 y 4096. Al fijarla se toma la cantidad de la pila en mano; los botones +1/−1 ajustan el objetivo seleccionado. El pie muestra índice y cantidad, sincronizados por el menú. Dos muestras equivalentes no suman sus objetivos: se usa el mayor. Con datos exactos se distinguen nombres y componentes; al desactivarlos se agrupan por tipo de objeto.

Recoger/Guardar/Vaciar básicos y avanzados rechazan Torpe, como los sellos originales. Este ajuste puede dejar sin trabajo de transporte a un diseño Torpe de port.20; no elimina su carga ni su entidad. Guardar/Vaciar siguen usando la capacidad Forge de la cara del soporte. Los nueve filtros no amplían la carga física del gólem: una pila o dos con Porteador. La reserva de trabajo, prioridad, propietario, canal, pausa por redstone y límites del hogar permanecen activos.

## Pedidos y transporte físico

Proporcionar es pasivo: no vacía su cofre por sí solo. Abastecer cuenta lo que hay, descuenta entregas físicas cargadas en gólems cercanos que ya van al mismo sello y busca proveedores del mismo propietario/canal dentro del ámbito de trabajo. No requiere Inteligente. Los proveedores filtran también lo que ofrecen y exponen únicamente la cara marcada.

El plan simula extracción, espacio destino y carga disponible sin mover objetos. Solo pide el déficit que cabe. El gólem reserva la tarea, camina al proveedor, revalida y extrae la cantidad real; después camina al destino con los objetos en su inventario. No hay transferencia instantánea ni duplicación de una pila de muestra.

Al entregar vuelve a contar y limita la inserción al déficit actual. Si otro jugador, máquina o gólem ha llenado el cofre, el sobrante permanece en la carga. Si el destino sigue pidiendo pero no admite objetos, espera y reintenta. Los inventarios y objetos conservan sus componentes.

Un destino cargado pausado por redstone retiene la entrega; un destino descargado espera sin forzar su chunk. Retirar el sello o cambiar propietario/canal/filtros a una configuración incompatible libera la ruta conservando la carga, que puede ir a otro destino compatible o recuperarse con la campana. El dueño ausente o en otra dimensión sigue pausando el gólem. Las rutas inaccesibles no teletransportan objetos.

Las reservas vacías se reconstruyen. Tras extraer, se guardan destino, muestra exacta y carga física en la entidad. Al cargar recupera el trayecto. Las reservas de carga se calculan sobre entidades cargadas en un radio de 96 bloques; no son un registro global de entidades descargadas. La revalidación de la entrega impide sobreabastecer incluso cuando una reserva descargada no estaba visible y se ha preparado otro viaje; en ese caso puede quedar carga sobrante en un gólem.

## Guardia y Seda

Vigilar avanzado exige Inteligente y Luchador. Alterna monstruos, animales o jugadores. Animales no es Carnicero: no conserva parejas ni excluye crías. La selección y el golpe protegen dueño, aliados y mascotas aliadas; creativo y PVP desactivado excluyen jugadores. Se mantiene distancia, visibilidad y navegación.

Romper avanzado exige Inteligente y Rompedor y permite Seda. Sigue siendo la tarea de un bloque soporte, no una cantera. Mantiene exclusiones de entidades de bloque, dureza mayor que 5, bloques inmunes/irrompibles, permisos del dueño y cancelación/denegación Forge. El botín se calcula con un pico de hierro real y el encantamiento nativo de Seda cuando está activado.

## Recetas y progresión

Se conservan catalizadores, aspectos y requisitos originales de las siete recetas de crisol. Proporcionar transforma Vaciar avanzado; Abastecer transforma Guardar básico. Los capítulos correspondientes muestran las recetas y las instrucciones actuales. Las variantes avanzadas requieren la investigación básica y Mente biotaumática. No se concede investigación al colocar o configurar un sello.

## Guardados

Se mantienen los seis identificadores de bloque anteriores, sus primeras seis posiciones del enum y todos los identificadores de objetos/componentes. El filtro de port.20 sigue guardado como `filter`; las muestras adicionales son un campo opcional nuevo. Si no hay campos nuevos, carga con datos exactos, objetivos 64, sin Seda y objetivos de guardia monstruos. Diseño, salud, propietario y carga de los gólems mantienen su formato previo. Las rutas nuevas son opcionales.

La prueba dedicada carga un mundo de port.20 real con un sello Guardar y un gólem nombrado con 17 diamantes y salud 7; se comprueba conservación antes de guardar datos nuevos. Esto verifica esa actualización concreta, no todas las versiones futuras ni un descenso a port.20. Conserva una copia completa del mundo antes de actualizar: la versión anterior no conoce los siete bloques nuevos.

## Pendiente

Usar, Talar y Carnicero; tareas externas a Abastecer como peticiones de fabricación; cantidades, opciones de coincidencia y variantes originales adicionales; patas con ruedas/trepadoras/voladoras, cabezas exploradoras, dardos, rangos y experiencia. Los modelos OBJ, accesorios y animaciones originales siguen pendientes. La prensa compacta y las placas siguen siendo las adaptaciones documentadas de port.20.

## Verificación

Fixtures `-PthaumaturgyProbe=logistics-write/logistics-read/logistics-client`, excluidas del JAR normal. Cubren migración de port.20, filtros exactos/de tipo/exclusión, muestras fantasma, objetivos superpuestos, planificación sin pago, guardado de carga/ruta y reinicio. La fixture despliega y sigue dos entidades distintas incluso si nacen en bloques contiguos; la restauración de una entrega no cuenta la entidad descartada como carga física. El cliente recorre los cinco sellos avanzados y los dos logísticos con navegación, dos gólems, canales, pausa por redstone, entrega restaurada, cambios externos en el inventario destino, Seda y protección de jugadores. Los clics nativos verifican selección de la novena muestra, cantidad y coincidencia exacta.

Resultado de la pasada nativa: proveedor 64 y destino 7 con objetivo 23; se extraen 16, se guarda/restaura el gólem, se pausa por redstone y se entregan exactamente 16 con dos trabajadores distintos. Al elevar el objetivo a 40 se extraen 17; si el destino se llena externamente, los 17 permanecen en la carga. Cliente cerrado normalmente tras guardar el mundo; servidor dedicado y reinicio terminan correctamente.

Compilación limpia: 82 pruebas, 0 fallos/errores/omitidas; 290 recetas y 253 definiciones de objeto. JAR normal sin fixtures de prueba: `thaumcraft-6.1.BETA26-port.21-forge66.0.13.jar` (7926144 bytes), SHA-256 `56da67dcabbc001984ba6c9587abebe713db9cce70277f309581b21969a2289a`.
