# Hornos de esencia avanzados — port.30

Primer lote de alquimia avanzada para Minecraft 26.3 / Forge 66.0.13 / Java 25. Añade cinco bloques funcionales y siete recetas arcanas, conservando el horno básico y sus datos. M07 sigue parcial: este lote no incluye aún centrifugadora, horno infernal, Thaumatorium ni condensador.

La centrifugadora se añade en [port.31](CENTRIFUGE.md).

## Mecánica

- Horno básico / taumio / vacío: capacidad 256; probabilidad de retención por unidad 80 / 90 / 95 %. Para vitium se aplica el multiplicador original 0,66. Los aspectos no retenidos producen flux salvo que los eliminen los respiraderos.
- Fuelles: apuntan al consumidor en seis orientaciones; redstone los desactiva. Cada fuelle válido reduce un 12,5 % el tiempo de procesado del horno de esencia, con máximo de tres y excluyendo su frente. El tiempo nunca es cero. También añaden un tick de cocción cada dos ticks a los hornos vanilla activos, sin cambiar inventarios, recetas, combustible, experiencia ni el proceso nativo de terminación.
- Depósito de tubos: cada fuelle válido eleva su succión en 32; el estrangulamiento conserva los valores 1/0 para sus dos estados restringidos. Se mantiene la prioridad de los consumidores.
- Puerto auxiliar: se coloca contra un lateral o parte posterior del horno y permite otra columna de hasta cinco alambiques. Cada puerto realiza una transferencia física por intervalo; no copia la reserva del horno. Los alambiques con el aspecto correspondiente se prefieren a los vacíos.
- Respiradero: se coloca como el puerto y tiene un tercio de probabilidad de eliminar cada unidad de contaminación restante. Se comprueban como máximo tres respiraderos por unidad; un fallo produce flux, sin volver a intentar indefinidamente. No aumenta la cantidad de esencia retenida.
- Destilación: intervalos originales de 15 / 10 / 15 ticks. Alumentum los reduce un 20 % mientras ese combustible siga ardiendo. La bonificación se decide antes de consumir la pila, de modo que funciona también con la última unidad.

Los modelos y las texturas de bloques son los originales. Los fuelles alternan geometrías de compresión mediante ticks programados nativos; esta animación es una adaptación a estados de bloque, sin el renderizador de 1.12. La pantalla muestra retención y cantidades de fuelles, puertos y respiraderos, además de la reserva y el trabajo existentes.

## Recetas y progreso

Se añaden las recetas originales de los dos hornos, fuelles, puerto auxiliar, respiradero y constructos alquímicos básico/avanzado. Costes de vis: 250, 750, 25, 100, 150, 75 y 200 respectivamente; se conservan cristales, ingredientes y rendimiento de dos constructos básicos. El horno del vacío requiere el cargador o un suministro de aura suficiente para pagar sus 750 vis. Las mesas normales no pueden eludir estos costes.

Las páginas de investigación conservan sus padres, observaciones y teorías. Sus referencias antiguas a placas y bloques de metal se resuelven a las placas modernas y a los constructos correspondientes. Se exigen pruebas reales de fabricación y no se desbloquea conocimiento automáticamente para jugadores.

Los constructos usan la perla y su conversión ya presentes en el port. El desgaste por usos de la perla original sigue dentro del pendiente general de equivalencia de esos objetos; este lote conserva el comportamiento del componente moderno existente.

## Guardado y seguridad de las transferencias

Se conserva `AlchemyState` versión 1, los IDs existentes, los ordinales de tipo y las tres ranuras. Los nuevos hornos usan la misma entidad y capacidades de entrada superior, combustible lateral y extracción inferior. La bonificación de alumentum y los contadores de aspectos procesados/flux/ventilación se guardan por separado, con valores por defecto compatibles.

Se conserva combustible, progreso, inventario y esencia al guardar. Los datos incompatibles siguen preservándose con la máquina desactivada. Las consultas a mejoras y columnas solo usan chunks cargados; no fuerzan cargas. La redstone se vuelve a comprobar al contar fuelles. Romper máquinas conserva las reglas de drops y contaminación anteriores. No cambia la generación del mundo.

Se corrigen dos defectos del código antiguo: el cast entero que podía convertir el intervalo de alumentum en cero y el bucle de respiraderos que podía no terminar. Se mantienen cálculos acotados, con cero contaminación evitada cuando no hay respiraderos.

## Verificación

Las fixtures `-PalchemyProbe=smelters-write/read/client` usan mundos de prueba desechables y se excluyen del JAR normal. Cubren recetas con investigación bloqueada y costes reales, progreso del libro, contabilidad de cada aspecto, columnas, montaje, redstone, succión/prioridades, horno vanilla, trabajo pagado y reinicio. La escena de cliente presenta los tres hornos activos con mejoras y permite verificar la pantalla sincronizada.

### Resultados de port.30

- Servidor dedicado: siete fabricaciones arcanas pagadas, rechazo con investigación bloqueada y avance real de las dos investigaciones. Para 120 aspectos de entrada: básico 99 retenidos / 21 flux; taumio 109 / 11; vacío 115 / 2 flux / 3 ventilados. Sin pérdida ni duplicación fuera de las reglas de retención.
- Mejoras: tres fuelles, exclusión frontal y redstone; cuatro columnas independientes con dos unidades cada una; succión 32/1/0 y colocación mediante el objeto real.
- Hornos vanilla: cocción acelerada durante 80 ticks reales, parada del incremento adicional con redstone y conservación de entradas/salidas.
- Reinicio dedicado: recuperación de inventario, combustible, trabajo y bonificación de alumentum; conservación del total procesado más el pendiente mientras transcurren ticks normales. También se cargó la máquina de la fixture anterior a esta ampliación.
- Cliente nativo: repetición de recetas y mecánicas, tres hornos activos sincronizados y pantalla de taumio con retención 90 %, un fuelle y un puerto. Guardado, regreso al título sin capturarlo y cierre normal; `BUILD SUCCESSFUL`. La escena protege al jugador de pruebas en creativo durante las esperas y limita la espera de sincronización. El cliente usó Vulkan por el fallback gráfico del entorno de pruebas.
- Dos capturas reales de 1280×720 compartidas antes de generar el JAR y antes del push. SHA-256 de instalación: `dac791f4b8356fc5ab26cf383453f5c4d2172b4734eef38324ed23ba5d5b17ae`; de pantalla: `7679adf430ffdc83fcec4b262b5a2bfc38022adb6a68c93536d5c70027e0d618`.
- Cadena de 18 generadores ejecutada dos veces sin cambios en la segunda pasada; generador avanzado comprobado de nuevo tras ajustar los textos.

`clean build`: **124 tests, 0 fallos, 0 errores y 0 omitidos**. JAR normal sin fixtures; 303 recetas, 263 definiciones de objeto y todos los JSON válidos. Conserva la prensa y las mallas de gólems anteriores.

Artefacto: `thaumcraft-6.1.BETA26-port.30-forge66.0.13.jar` (8077105 bytes). SHA-256: `7b80bd4efd5351171159118511cb6e5ab1c33ee90fa2ca1187627a031e9cc61c`.
