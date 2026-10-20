# Cabezas exploradoras — port.24

Minecraft 26.3 / Forge 66.0.13 / Java 25. Añade Explorador y Explorador inteligente a la selección de cabeza de la prensa. Continúa M11; los brazos de dardos, rangos/experiencia y otras opciones siguen pendientes.

## Fabricación y rasgos

Se conservan los índices anteriores: Básica 0, Inteligente 1, Inteligente blindada 2. Las nuevas cabezas ocupan 3 y 4. Ambas añaden un módulo de visión y los rasgos Explorador y Frágil. Explorador emplea mente mecánica y exige GOLEMVISION; Explorador inteligente emplea mente biotaumatúrgica, exige además MINDBIOTHAUMIC y añade Inteligente para las tareas avanzadas. Las mejoras cancelan Frágil como antes. Las cuatro patas siguen siendo combinables con todas las cabezas.

El módulo de visión ya registrado recibe su receta arcana original: 50 vis, un cristal de agua, dos botellas, dos ojos de araña fermentados, dos placas de latón y un mecanismo simple. El libro muestra receta e icono y explica los radios. No se añaden identificadores de objeto, bloque, entidad o componente.

La prensa reserva todos los componentes o ninguno, bloquea el diseño pagado y conserva el progreso al guardar. El coste Machina sigue siendo componentes más dos por rasgo final. Los diseños anteriores conservan componentes y coste.

## Radio de trabajo

El radio sigue centrado en el hogar. Las cabezas originales mantienen 32 bloques; las exploradoras permiten 48, con distancia tridimensional inclusiva para los sellos. El atributo nativo de búsqueda de caminos se configura en 40/56 bloques, respectivamente, como el original. Seguir al dueño usa el límite 32/48.

El mismo predicado se usa al enumerar sellos, reservar una tarea y validar proveedores/entregas guardadas. Cambiar el hogar invalida una entrega que quede fuera del nuevo radio, conservando la carga para otra tarea o la campana. Explorador no amplía el radio de tarea configurado en cada sello, no concede Inteligente por sí solo ni ignora propietario, canal, redstone o requisitos. Los sellos y rutas siguen necesitando chunks cargados; no se fuerzan chunks nuevos.

El tipo de cabeza viaja en datos de entidad sincronizados. El modelo adaptado añade prismáticos a las exploradoras y conserva las variantes de patas de port.23. Siguen pendientes los modelos OBJ originales.

## Compatibilidad y pruebas

El codec amplía únicamente el rango de cabeza de 0–2 a 0–4, conservando campos e índices existentes. Los objetos, entidades y trabajos anteriores no necesitan conversión de formato. La cabeza se reconstruye al cargar y se conserva al recoger con la campana. El cambio no toca generación de mundo.

Las pruebas de datos cubren las 768 combinaciones nuevas, costes originales, progresión de mente mecánica/biotaumatúrgica, compatibilidad de cuatro campos, límites del codec, cancelación de rasgos y ciclo de edición. Fixtures `scout-write/read/client`, excluidas del JAR normal, comprueban fabricación, límites, guardado y rutas físicas.

Servidor dedicado: `work/scout-server-write1.log` y `work/scout-server-read1.log` comprueban 32/33/40/48/49 bloques en índice, reserva y elegibilidad de entregas, canal y cambio de hogar; reserva atómica y bloqueo del trabajo; finalización tras reiniciar de una cabeza nueva y una antigua ya pagadas. Recuperan un Explorador inteligente con 21 esmeraldas, salud 5 y navegación trepadora. Un volador real de port.23 conserva cabeza antigua, radio 32, 19 diamantes y salud 5; el porteador anterior conserva sus 17 diamantes y salud 7.

Los doce generadores se ejecutan dos veces: 1494 recursos idénticos.

Cliente: `work/scout-client1.log` supera la receta original y una ruta con proveedor a 40 bloques y destino a 42: la cabeza antigua no inicia la ruta; Explorador transporta 23 esmeraldas, guarda/restaura la entrega y conserva la carga durante una pausa por redstone. El proveedor en otro canal no se usa y un destino de mayor prioridad a 49 bloques queda vacío. Explorador rechaza Guardar avanzado, Explorador inteligente con ruedas entrega siete lingotes en él, sin alterar las 23 esmeraldas anteriores. La campana recoge Explorador con su diseño y salud 5/16 y deja caer exactamente sus 13 lingotes. Las cabezas 3 y 4 llegan sincronizadas al cliente. Clics nativos recorren las cinco cabezas de la prensa y pagan/bloquean Explorador inteligente; pantalla inspeccionada a 854 × 480.

El cliente vuelve al menú principal tras guardar el mundo y se cierra mediante Salir del juego, sin watchdog ni cierre forzado; Gradle termina correctamente. Servidor y reinicio también finalizan correctamente.

Compilación final limpia: 98 pruebas correctas, sin fallos, errores ni omitidas; 295 recetas y 257 definiciones de objeto. JAR sin fixtures: `thaumcraft-6.1.BETA26-port.24-forge66.0.13.jar` (7966247 bytes), SHA-256 `41245ea9c06c20ad222f516f13855c638aa4d446f4a09e6f13f83ff8ec8bdcf4`.

## Pendiente

Brazos de dardos y combate a distancia de gólems, rangos/experiencia, opciones originales adicionales de coincidencia/área, fabricación y modelos/animaciones originales. Esta entrega no completa toda la golemancia original.

Actualización posterior: [Brazos lanzadardos — port.25](GOLEMS-DARTS.md).
