# Visualización de aspectos — port.82

Las recetas del Thaumonomicon reducen ahora la textura completa de 32×32 al icono de 14×14. La sobrecarga anterior de `blit` recortaba una región de 14×14 de la esquina de la imagen, perdiendo la mayor parte del símbolo. No cambian los ingredientes, costes, cantidades o áreas de tooltip.

Al pasar el ratón por una ranura del inventario o de un contenedor abierto, los objetos estudiados muestran iconos completos de aspectos y cantidades por unidad. No hace falta sostener el thaumometer ni pulsar Shift. El tooltip conserva el nombre y la información vanilla. El cliente pide únicamente el identificador del menú abierto y el índice de ranura; el servidor resuelve la pila real, valida el descubrimiento `item:<proof>` y calcula sus aspectos. Los objetos sin estudiar no reciben valores. La respuesta conserva los componentes y variantes de la pila y se descarta si ya no corresponde al objeto consultado. Se limita la frecuencia en el servidor y se renueva la consulta mientras se mantiene el ratón para recoger cambios de conocimiento. Los datos del cliente se descartan al salir del mundo. No se sincroniza todo el conocimiento del jugador ni se concede investigación al consultar.

Al apuntar un crisol con el thaumometer o con gafas de revelación equipadas (incluidas sus variantes de casco ya admitidas), el HUD muestra los aspectos disueltos y sus cantidades. Sustituye la ficha del material del bloque mientras se apunta el crisol. Lee el estado ya sincronizado del bloque: cambios de contenido, vaciado, cambio de objetivo y retirada del equipo se reflejan sin recargar chunks. No cambia la alquimia ni sus costes. Un crisol vacío no conserva símbolos de un contenido anterior.

## Verificación

- `-PbookProbe=inventory-aspects runServer`: objetos desconocidos, estudio real, cantidades por unidad en pilas de 64, componentes y variantes de cristales, menús antiguos, índices inválidos, conservación de objetos/conocimiento y reinicio del conocimiento.
- `-PbookProbe=aspect-display runClient`: página real de Nitor, tooltip de carbón estudiado, diamante desconocido, cristal de aire estudiado y cristal de fuego desconocido; contenido real de crisol con thaumometer y con gafas, actualización de cantidades, cambio de mirada y vaciado.
- Las escenas preparan conocimiento y contenido para comprobar la presentación; no sustituyen una partida de supervivencia completa. Las clases de ensayo quedan fuera del JAR de distribución.
