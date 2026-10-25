# Gancho: modelos y cable — port.66

Se recuperan las dos mallas originales del lanzador: 14 elementos con la punta colocada y 10 elementos durante el disparo. Se conservan sus texturas, UV y transformaciones de inventario y tercera persona. La definición nativa del objeto elige el modelo mediante el componente propio `thaumcraft:grapple_link`, sin utilizar CustomModelData. El cambio de estado conserva la transición nativa de equipamiento.

La cabeza del proyectil usa las cajas y UV de ModelGrappler y su textura original. El cable es una malla de cuatro caras con `textures/misc/rope.png`, radio 0,025 y un máximo de 128 segmentos. Las ondas de vuelo se amortiguan al anclarse. Sus extremos coinciden con el gancho y la mano que disparó; se tienen en cuenta mano principal/secundaria, brazo dominante, cámara, movimiento y balanceo. El volumen de recorte incluye todo el cable para evitar que desaparezca al salir la cabeza de la pantalla. Las tareas de dibujo consumen instantáneas y no consultan entidades durante la ejecución diferida.

Cada disparo guarda su mano y vincula el UUID del proyectil al lanzador real. Una segunda pistola no hereda el cable. Soltar, cambiar de mano, tirar la pistola o terminar el recorrido elimina la vinculación y repone el modelo con punta; los estados guardados sin proyectil válido se limpian al actualizar el inventario. Los proyectiles anteriores sin vinculación se adaptan a la mano que todavía sujete su lanzador.

Se conservan las comprobaciones de investigación, carga, alcance, chunks cargados, obstáculos, permisos, soporte y caducidad. Un disparo consume una carga; soltarlo no consume otra. El permiso de anclaje usa el objeto de la mano que disparó. Se recuperan el impulso vertical inicial y la compensación de elevación del original, manteniendo el límite de velocidad de 1,2 y el movimiento físico sin teletransporte. Se guardan el estado de anclaje, su momento, la mano, el impulso ya aplicado y la duración del vuelo. También se recuperan los tres sonidos originales de disparo (`ice1`–`ice3`).

## Pruebas

Las pruebas unitarias verifican extremos exactos, límite de geometría, amortiguación del cable, rechazo de coordenadas no finitas y conservación de mallas, transformaciones y sonidos originales. `-PbookProbe=grapple runServer` comprueba ambas manos y ambos brazos dominantes, coste exacto, anclaje físico, arrastre limitado, instantánea, liberación, sustitución de pistola, obstáculos, soporte alterado, vencimiento, estados obsoletos y carga cero.

La escena `-PbookProbe=grapple-client runClient` utiliza entrada nativa para disparar. Se comprueban la selección real de los modelos, el cable y la cabeza en primera y tercera persona y con el arma en la otra mano. La escena se congela solo para inspección visual; la comprobación del ascenso usa un disparo nuevo, tiempo de juego normal y gravedad normal. Las fixtures no se incluyen en el JAR normal.

## Resultado de la validación

El servidor dedicado completa las cuatro combinaciones de mano y brazo dominante y todas las reglas de liberación. El cliente nativo confirma los modelos cargado/disparado, las dos manos y la tercera persona. La prueba física con gravedad normal eleva al jugador 1,124 bloques hacia el anclaje y comprueba la liberación y restauración del lanzador. Se capturan vistas reales del cable y un GIF de 40 fotogramas (4,09 segundos) antes de generar el JAR; la escena congelada se usa únicamente para inspeccionar la geometría. Se revisa además la conexión a la boca del arma desde la cámara frontal de tercera persona.

La compilación de publicación ejecuta la suite completa (208 pruebas) y verifica que el JAR excluye las fixtures, conserva las mallas, texturas y sonidos originales y mantiene el contenido de port.65 ajeno a este cambio.
