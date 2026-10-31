# Mesa de investigación — port.80

## Interfaz y objetos sobre la mesa

Se recuperan los recursos originales `gui_research_table.png`, `paper.png`, `papergilded.png` y `gui_base.png`. El tablero sustituye al panel provisional y a sus rectángulos vacíos. El inventario vuelve a la disposición original: barra rápida en una cuadrícula de 3×3 a la izquierda y las otras 27 ranuras a la derecha. Los índices del menú y las pilas almacenadas no cambian.

La pantalla ajusta temporalmente su escala cuando la preferencia de GUI haría que el tablero quedase recortado, y restaura la escala al cerrarse sin guardar cambios en las opciones. La inspiración se presenta mediante bombillas. Las ayudas cercanas son iconos seleccionables. Crear teoría, completar y descartar conservan sus comprobaciones del servidor. El montón de papel dibuja dos cartas o consume un bonus para dibujar tres. Las opciones son pergaminos pulsables: título, coste de inspiración, descripción completa y objetos requeridos. Los porcentajes literales de las traducciones se escapan correctamente para que se sustituyan las categorías en lugar de dejar `%s` en pantalla. El texto se ajusta a su área; el tooltip conserva también la descripción y especifica qué objetos se consumen. El progreso no nulo aparece con los iconos de categoría al lado del tablero.

El nuevo renderizador usa las texturas originales de tintero y pluma. Se muestran si hay útiles de escritura en la ranura de la mesa y se retiran al quitarlos. Una teoría activa muestra además el rollo. La caja de render incluye la pluma por encima del bloque. El inventario y los cambios de sesión sincronizan el bloque, por lo que no hace falta recargar el chunk para actualizar esos objetos. No se alteran costes, sorteo de cartas, puntos, ownership o protección por revisión.

## Fabricación y progreso del libro

No se reprodujo una pérdida del registro de fabricación. La prueba inicial mostró el requisito registrado en los ocho casos de fabricación normal: cuadrícula 2×2 y mesa 3×3, recogida normal/con Shift, botella/vial. El evento de Forge llegó con la pila correcta en todos ellos; no se añade un workaround para la hipótesis de una salida vaciada.

La prueba final amplía a doce combinaciones, incluida la mesa arcana. Comprueba la casilla de útiles en `ResearchMenu`, exige también fabricar la mesa de investigación, convierte una mesa de madera con la pila realmente fabricada y completa la entrada mediante `ResearchService.advance`. Una previsualización o una recogida fallida por inventario lleno no concede el requisito. Se aclaran ambos pasos y Continuar en la explicación y en el tooltip del botón bloqueado. Si falta ese registro, cada segundo se contrasta la estadística nativa `ITEM_CRAFTED` de los útiles y se recupera únicamente ese requisito cuando hubo fabricación real. No se avanza la etapa ni se suprime el requisito de crear la mesa. Esto cubre también mundos donde ya se fabricaron y falta la prueba del mod. No se concede un requisito de fabricación por poseer herramientas compradas, encontradas o generadas.

La pérdida de registro del mundo del usuario no se reprodujo con la fabricación normal. La recuperación se verifica borrando deliberadamente la prueba tras una fabricación pagada y conservando la estadística vanilla; también se comprueba que la mera posesión y un intento fallido por inventario lleno no conceden prueba. Un jugador real de la escena del cliente verifica la recuperación por el tick del servidor.

## Verificación

- `-PtheoryProbe=write test runServer` y `read runServer`: bootstrap, costes, requisitos atómicos, variantes celestiales, ownership, rechazo de acciones repetidas, todas las cartas y guardado/reinicio. La fixture suministra curiosidades físicas al probar la carta Curio. También comprueba la geometría de ranuras y el traslado de útiles con Shift sin duplicarlos.
- `-PbookProbe=scribe-craft runServer`: doce fabricaciones reales, la secuencia de progreso descrita arriba y recuperación desde estadísticas sin conceder prueba por posesión o intento fallido.
- `-PbookProbe=research-art runClient`: aspecto en el mundo, pantalla inicial, retirada/reinserción real de útiles con Shift y actualización del render sin recargar; crear, dibujar tres cartas, elegir un pergamino y descartar mediante entradas nativas. La escena prepara un bonus y progreso para mostrar las tres cartas y las categorías; no equivale a una partida de supervivencia sin preparación.

Se revisan capturas del mundo, del tablero inicial y de las tres cartas antes de generar el JAR final. Las clases de prueba quedan fuera de la distribución normal.

La entrega incorpora una prueba de formato para los 32 efectos de cartas en ambos idiomas (64 textos), además de la comprobación de traducciones resueltas en el cliente. Total de la suite automatizada: 253 pruebas.
