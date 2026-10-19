# Focos avanzados — port.15

Minecraft 26.3 / Forge 66.0.13 / Java 25. Amplía M10 sin añadir IDs ni recetas: siguen siendo 230 IDs de objeto y 264 recetas. Los focos simples de port.14 conservan programa y costes al cargarse.

## Editor y uso

En el manipulador, el botón **Setup / Más** alterna entre efectos y ajustes avanzados. Selecciona el modificador y, si hay división, la rama 1 o 2. Cada usuario de la mesa tiene su propia selección de rama. La hoja de vista previa muestra el programa completo y los ingredientes; la complejidad y capacidad corresponden siempre al conjunto.

- **Scatter:** 2–10 ejecuciones con dispersión de 10, 30, 60, 90, 180, 270 o 360 grados. Cada ejecución conserva 2 / número de ejecuciones de la potencia. Complejidad: truncado de máximo(2, 2 × (ejecuciones − cono / 45)).
- **Split Target:** un impacto que ejecuta los efectos de las dos ramas con potencia 0,75. La segunda rama no añade otro medio ni otro cristal de medio. Complejidad adicional 4.
- **Split Trajectory:** dos ramas con medio y efectos propios sobre la misma dirección inicial, ambas con potencia 0,75. Permite, por ejemplo, proyectiles de tierra y hielo. Complejidad adicional 5; repetir el medio incrementa el coste de su segunda aparición.
- **Cloud:** radio 1–3, duración 5–30 segundos. Complejidad 4 + 2 × radio + segundos / 5; cristal alkimia. Pulsa cada cinco ticks y aplica potencia 0,5, además de cualquier reducción del modificador. Las nubes de todos los jugadores de una dimensión comparten 40 ticks de inmunidad por entidad o bloque: superponerlas no multiplica los impactos.
- **Mine:** objetivo amigo o enemigo, cristal vinculum y complejidad 4. Necesita una superficie; se arma tras 40 ticks y caduca a los 1200. Se consume antes de aplicar un único impacto a los objetivos elegibles cercanos. Reconoce al propietario, sus equipos y sus animales domesticados como amigos. Cambiar el bloque de soporte invalida la mina.

Adaptación de colocación: nube y mina se sitúan en el impacto apuntado hasta 16 bloques, en lugar de exigir otra secuencia de nodos de trayectoria. Una nube sin impacto aparece tres bloques por delante; una mina sin superficie no aparece. Los medios anteriores mantienen toque a cuatro bloques, rayo a 32 y proyectiles físicos.

## Costes y persistencia

El servidor valida capacidad, todas las investigaciones de medios/efectos/modificador y el pago de aura una sola vez por lanzamiento. Scatter requiere FOCUSSCATTER; ambos Split, FOCUSSPLIT; Cloud/Mine, FOCUSCLOUD/FOCUSMINE. Se conservan los requisitos de investigación originales importados al libro.

Los cristales se reservan primero en los nueve espacios de la mesa y después en los 36 del inventario principal. La ausencia de un ingrediente impide cualquier descuento de cristales o experiencia. Los modificadores no añaden un aspecto propio. Los efectos repetidos cuentan globalmente para el sobrecoste. La creación conserva el bloqueo del foco y el consumo gradual de vis de port.14.

Se guardan el grafo completo, ajustes, propietario, potencia reducida de los proyectiles/campos, bloque de soporte y plazos absolutos de armado/caducidad. Los campos no actúan mientras el propietario está desconectado, muerto o en otra dimensión; su plazo sigue transcurriendo con el tiempo del mundo. Los enfriamientos compartidos también sobreviven a un reinicio. Los datos de enfriamiento ilegibles o de una versión futura no se sustituyen por un archivo vacío.

## Límites y protecciones

El grafo admite un modificador y dos ramas como máximo, con ocho efectos en total; no representa todavía un árbol recursivo arbitrario. Potencia/duración de efectos 1–5 y nombres cerrados. Hay como máximo 16 campos activos por propietario y dimensión, 16 objetivos por pulso y hasta tres rayos de muestreo de bloques por nube. El registro compartido limita su tamaño a 4096 entradas y elimina las caducadas.

La selección de objetivos exige línea de visión. Los rayos y campos comprueban que los chunks ya estén cargados. Los efectos conservan las comprobaciones nativas de PVP, aliados y eventos/permisos de bloques de port.14. Las nubes/minas muestran partículas nativas sincronizadas; no reproducen todavía la geometría ni las animaciones originales.

## Pendiente

Plan se incorpora en [port.16](FOCUS-PLAN.md). Pendientes: Spellbat, Rift/agujeros temporales; grafos con más niveles y combinaciones de modificadores; variantes originales completas de trayectorias, fortuna y seda; bolsa de focos, vara pech, torretas, colapsador causal y gancho. M10 sigue siendo parcial.

## Verificación

53 pruebas JUnit, sin fallos, errores ni omisiones: compatibilidad de programas anteriores, costes de nodos repetidos entre ramas, cristales, límites del grafo, rechazo de datos incompletos y guardado/expiración del enfriamiento compartido, además de la batería anterior.

Pruebas nativas separadas del JAR normal mediante `-PthaumaturgyProbe=advanced-write/advanced-read/advanced-client`. Servidor dedicado: pago atómico, inventario como segunda fuente, bloqueo del editor y guardado a mitad de trabajo. Reinicio: conservación de nubes, minas amigas, ajustes y enfriamiento; finalización del foco nombrado sin un segundo pago. Cliente integrado: cuatro proyectiles Scatter; colisiones de dos proyectiles de ramas distintas; daño y maldición en un mismo impacto; investigación ausente sin pago de aura; coste de aura único; nubes superpuestas con impactos separados 40 ticks; armado, disparo enemigo, curación amiga y anulación de mina por cambio del soporte. Programa y controles sincronizados e inspección de ambas páginas a 854 × 480.

Evidencia local de trabajo: `work/focus-advanced-tests2.log`, `work/focus-advanced-server-write.log`, `work/focus-advanced-server-read.log`, `work/focus-advanced-client2.log` y `work/focus-advanced-client3.log`. La primera ejecución gráfica falló una aserción del impacto apuntado; la repetición completa con diagnóstico adicional superó todas las fases. Se desactivó la aparición de enemigos ambientales y se amplió la limpieza del área en la fixture para impedir interferencias de phantoms con la puntería; al terminar se deja al jugador en creativo para inspeccionar la pantalla sin ataques. Esta segunda repetición aislada también superó todas las fases. Estos escenarios son verificaciones funcionales acotadas, no una certificación de todos los grafos posibles.

Limitación gráfica del entorno: el servidor integrado guarda y finaliza, pero el renderizador Vulkan por software queda esperando en `vkQueuePresentKHR` al salir al título, como en port.14. Se conserva un volcado de hilos y se termina ese proceso del cliente. Las pruebas de juego pasan; el cierre normal de la interfaz gráfica no queda certificado aquí.

Compilación limpia: `work/focus-advanced-clean-build.log`, correcta; 53 pruebas verificadas y JAR sin `thaumcraft/test/`. Entrega: `thaumcraft-6.1.BETA26-port.15-forge66.0.13.jar`, 7 697 007 bytes. SHA-256: `3685bf4472bf72b96a57f9af685a667d4f5cf4681c652a3a2ccee888858abeb7`.
