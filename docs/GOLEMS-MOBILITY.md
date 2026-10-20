# Movilidad de gólems — port.23

Minecraft 26.3 / Forge 66.0.13 / Java 25. La prensa permite elegir Caminantes, Ruedas, Trepadoras o Levitadoras. Se conservan los identificadores anteriores y se añade el levitador arcano como dependencia fabricable; no se añaden sellos. M11 sigue parcial.

## Construcción y progresión

La quinta selección de la prensa son las patas. Caminantes conservan exactamente el coste y rasgos anteriores. Ruedas requieren dos cuencos y cuero y sustituyen un componente del material. Trepadoras requieren cuatro pedernales y un mecanismo adicional, además de GOLEMCLIMBER. Levitadoras sustituyen un componente del material por un levitador, cuatro placas de latón y una bola de slime; requieren GOLEMFLYER. Los componentes compartidos se suman: por ejemplo, el cuero de las ruedas y del Porteador se cobra dos veces.

Los rasgos de patas se aplican antes de los accesorios: el blindaje puede cancelar Frágil de las levitadoras. La reserva de materiales es atómica; una investigación o componente ausente no consume nada. Una vez pagado, el diseño queda bloqueado hasta terminar el trabajo. Machina conserva su cálculo por componentes y rasgos finales, sus caras de entrada y el límite de consumo del trabajo.

## Levitador arcano

Nuevo bloque orientable `thaumcraft:levitator`, con receta arcana original: 35 vis, un cristal de aire, cuatro tablones, una placa de taumio, dos placas de hierro, cualquier nitor y un mecanismo simple. Mano vacía alterna alcance 4/8/16/32; redstone lo desactiva. El haz impulsa criaturas y objetos con movimiento físico y se detiene ante colisiones o chunks descargados. Agacharse en un haz ascendente permite descender. Conserva una reserva de hasta 1200 unidades por vis; cada entidad y tick consume el doble del alcance configurado. Solo recarga cuando hay un objetivo y falta reserva. Rango y reserva persisten. Usa modelos de bloque adaptados con texturas originales.

## Desplazamiento

- Caminantes: navegación terrestre anterior.
- Ruedas: velocidad terrestre un 25 % mayor y altura de paso automático de medio bloque. Pueden saltar según las reglas de navegación nativas; no se garantiza atravesar cualquier escalera o desnivel.
- Trepadoras: navegación de trepador y ascenso al colisionar horizontalmente con una pared. Necesitan espacio y una salida transitable; no se adhieren a techos ni atraviesan bloques.
- Levitadoras: navegación aérea tridimensional, sin gravedad, a un 67 % de la velocidad base del diseño. Añaden Frágil antes de aplicar el accesorio. Esperar, dueño ausente y paradas detienen su movimiento y mantienen la altura.

Trepadoras y levitadoras no reciben daño de caída. La navegación respeta la colisión; no teletransporta el gólem ni su carga. Los destinos y siguientes nodos descargados detienen la ruta sin forzar chunks. Las tareas siguen revalidando propietario, canal, redstone y carga física.

El cliente recibe el tipo de patas mediante datos de entidad sincronizados. El modelo adaptado distingue ruedas, patas abiertas de trepador y base levitadora, usando las texturas del material. No reproduce los OBJ y animaciones originales.

## Guardado y compatibilidad

`legs` es opcional y su valor por defecto es Caminantes (0). Los diseños antiguos de cuatro campos, los objetos ya fabricados y los trabajos de prensa ya pagados conservan sus costes. Al cargar una entidad se reconstruyen navegación y gravedad a partir del diseño. Campana, salud, nombre, canal y carga mantienen su comportamiento anterior. No se cambia la generación de mundo.

Las pruebas usan mundos aislados; la compatibilidad comprobada corresponde a esta actualización concreta. No implica que una versión anterior pueda interpretar diseños nuevos ni garantiza futuras actualizaciones.

## Verificación

Fixtures `mobility-write`, `mobility-read` y `mobility-client`, excluidas del JAR normal. Las pruebas de datos cubren los 1152 diseños, decodificación antigua, pago de componentes compartidos, investigaciones, cancelación de rasgos, edición y velocidades.

Servidor dedicado: `work/mobility-server-write6.log` y `work/mobility-server-read1.log` verifican pago atómico, investigaciones, bloqueo de trabajo, continuación de trabajos nuevos y NBT antiguo, y recuperación de gólems con patas, navegación, salud y carga. Se carga un porteador real de la versión anterior con sus 17 diamantes y salud 7. El levitador verifica impulso pagado, ausencia de consumo en reposo, redstone, bloqueo sólido y reserva/rango guardados.

Cliente: `work/mobility-client3.log` comprueba la receta original con nitor rosa y entrega de 17 esmeraldas con cada movilidad. Las trepadoras suben una pared de seis bloques. Las levitadoras cruzan un hueco y un muro, se guardan/restauran durante el vuelo y entregan la carga; Esperar conserva la altura durante 60 ticks. La campana conserva diseño y salud y suelta exactamente 11 lingotes físicos. Los tres tipos llegan sincronizados al cliente. Los clics nativos recorren las cuatro patas de la prensa y pagan un trabajo volador, bloqueando las selecciones. Pantalla inspeccionada a 854 × 480. Los once generadores se ejecutan dos veces con 1493 recursos idénticos.

Cierre del cliente: las pruebas funcionales terminan y el registro muestra el guardado de jugadores y las tres dimensiones antes de salir. El backend Vulkan del entorno se bloquea al presentar la pantalla de salida y activa el watchdog de cierre (`VulkanGpuSurface.present`, informe `crash-2026-10-20_06.58.54-client.txt`); Gradle termina con código 0. No se presenta como un cierre gráfico limpio. Se mantiene fuera del cambio de movilidad por ser el problema de entorno conocido y no impedir las pruebas.

Compilación final limpia: 92 pruebas correctas, sin fallos, errores ni omitidas; 294 recetas y 257 definiciones de objeto. JAR sin fixtures: `thaumcraft-6.1.BETA26-port.23-forge66.0.13.jar` (7965005 bytes), SHA-256 `833ac905cdb2f0afed4ba0d7c7f516dd9b090b0115fcd56f587b54b20a8ac140`.

## Pendiente en M11

Cabezas exploradoras, brazos de dardos, rangos y experiencia, opciones originales adicionales de área/coincidencia, fabricación y modelos/animaciones originales. Los dieciséis tipos de sello registrados y las cuatro patas no completan por sí solos toda la golemancia de Thaumcraft 6.
