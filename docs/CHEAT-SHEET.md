# Thaumonomicon de trucos — port.73

`[misc] allowCheatSheet = true` en la configuración de servidor habilita una variante especial del Thaumonomicon en la pestaña creativa de Thaumcraft y en la búsqueda. El valor predeterminado sigue siendo `false`. Se usa la configuración del servidor conectado, incluida su sincronización normal de Forge. El cliente actualiza la pestaña y la búsqueda cuando cambia esa opción.

La variante conserva el ID `thaumcraft:thaumonomicon` y añade el componente persistente `thaumcraft:cheat_sheet`. Tiene nombre traducido, rareza épica y la textura original `thaumonomicon_cheat.png`. El libro normal y sus recetas siguen produciendo la variante normal. Cambiar un nombre o una textura no otorga investigaciones.

Al usar el libro especial, el servidor comprueba el objeto real de la mano y la opción activa. Concede las 148 investigaciones, sus etapas y predicados de descubrimiento, de modo que también quedan accesibles las siete categorías y los addenda. Funciona en ambas manos. Como en el original, se obtiene desde creativo pero puede entregarse a otro jugador y usarse en supervivencia si el servidor lo permite. No tiene receta de fabricación.

La operación conserva los escaneos, puntos, fabricaciones registradas, investigaciones de otros complementos y etapas superiores existentes. No consume objetos, experiencia ni puntos de conocimiento. El desbloqueo sí añade la distorsión de las etapas que faltaban, siguiendo las reglas de investigación del port; `wussMode` la desactiva. Repetir el uso no vuelve a añadir distorsión.

Una vez desactivada la opción, un ejemplar guardado se comporta como un libro normal y no concede investigaciones. Las investigaciones ya concedidas permanecen. Se conservan sin modificaciones los guardados desconocidos o corruptos: si no pueden prepararse conocimiento y distorsión, la operación falla sin conceder una parte de las investigaciones. El Thaumonomicon normal utiliza su progresión habitual.

## Verificación

Las pruebas unitarias comprueban las 148 entradas y sus predicados/categorías, conservación del conocimiento independiente y de etapas superiores, serialización, idempotencia y distorsión pendiente, además del selector del icono original.

Las fixtures `-PbookProbe=cheat-write runServer` y `cheat-read` comprueban la opción desactivada/activada, pestaña y búsqueda, nombres falsificados y marcadores sobre otros objetos, uso real en la mano secundaria, menú, distorsión una sola vez, independencia de jugadores, formatos futuros y modo sin distorsión. El reinicio utiliza un objeto serializado y datos completos de jugador guardados mediante el serializador nativo, en una segunda JVM.

`cheat-client` comprueba la actualización real de pestaña/búsqueda y la sincronización del componente. Espera el uso nativo del libro y verifica la pantalla real con todas las investigaciones, categorías y addenda desbloqueados. Las fixtures se excluyen del JAR publicado.

Resultado de la verificación de port.73: 239 pruebas unitarias sin fallos; servidor dedicado con escritura y reinicio en otra JVM; cliente nativo con búsqueda creativa y uso real del libro. La pantalla sincroniza las 148 investigaciones, las siete categorías y todos los addenda. Se capturaron ambas interfaces antes de generar el JAR de distribución.
