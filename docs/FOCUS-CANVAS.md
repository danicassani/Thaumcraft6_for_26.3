# Editor visual de focos — port.65

El manipulador muestra el árbol completo con los iconos originales de medios, efectos y modificadores. Seleccionar un nodo abre su paleta y sus ajustes. Se pueden editar y eliminar efectos anteriores al último sin modificar sus hermanos. Cada menú conserva su propia selección, incluso cuando dos usuarios comparten mesa.

El fondo se arrastra para desplazar el árbol; la rueda amplía o reduce la vista y el botón de centrado la recupera. La distribución de nodos es automática. Las conexiones representan continuaciones de impacto y bifurcaciones; en Split Target, «mismo impacto» representa la rama que no cobra un segundo medio. El árbol sigue siendo acíclico, con una continuación por rama.

Los botones E+ y E− añaden o retiran efectos. →+ añade una continuación y permite configurar su medio; → entra en una continuación existente y →− la elimina. El servidor rechaza una eliminación que dejaría un terminal sin efectos. Los menús cuya ruta desaparece por una edición de otro usuario vuelven al inicio.

Se amplía la profundidad de cuatro a ocho niveles, conservando un máximo de ocho nodos de programa, ocho efectos y 64 entregas por expansión. Un medio intermedio puede tener cero efectos si dispone de continuación: por ejemplo, Rayo → Toque con Rotura. No hace falta introducir un efecto artificial. Plan mantiene su exclusividad. Los códecs siguen teniendo profundidad finita y rechazan datos excesivos antes de descender por ellos.

El foco de entrada, los nueve huecos de cristales y el inventario son contenedores nativos. La vista previa del programa se sincroniza mediante el componente del menú; no se puede recoger ni duplicar. El servidor valida la distancia, el trabajo en curso y el programa completo para cada cambio. La selección envía únicamente un índice acotado; las paletas envían opciones cerradas, nunca un programa arbitrario del cliente.

La creación muestra niveles de experiencia, vis de fabricación y cristales necesarios. Un aviso distingue falta de foco, capacidad, investigación, experiencia o cristales. La reserva de cristales usada para decidir disponibilidad es la misma que se usa para pagar; un intento fallido no consume recursos. La fabricación bloquea cambios y conserva el foco físico y su programa al reiniciar.

Los programas válidos anteriores conservan orden, ajustes y costes. No cambian los IDs de objetos, recetas ni bloques. Los cambios de interfaz se traducen al inglés y al español. Se verifica la disposición a 1280×720 y 854×480.

## Pruebas

`FocusCanvasTest` comprueba direcciones de selección, conservación de hermanos, costes de medios sin efectos, persistencia de una secuencia de ocho niveles y geometría sin nodos superpuestos. Las pruebas de programas anteriores siguen verificando costes y compatibilidad.

Las fixtures `-PbookProbe=canvas-write` y `canvas-read` verifican en dos JVM: selección independiente, edición del primer efecto, paletas, eliminación segura, octavo nivel y rechazo del noveno, rutas obsoletas, guantelete real con secuencia sin efecto intermedio y coste único de aura, pago y bloqueo del foco, guardado a mitad de trabajo y finalización después de reiniciar sin un segundo pago.

`-PbookProbe=canvas-client` abre una mesa real. La prueba usa clics nativos para cambiar el primer efecto, ajustar su potencia, editar la otra rama y crear el foco físico con pago exacto. Estas clases quedan fuera del JAR normal.

La verificación nativa detectó que la numeración de botones del backend SDL de 26.3 difiere de GLFW. El lienzo utiliza `InputConstants.MOUSE_BUTTON_LEFT/RIGHT`; los clics de selección, arrastre, rueda y centrado se verifican mediante entrada X11 real. La inspección también corrigió los fondos individuales de los huecos vacíos. Se conserva una captura del árbol editado y otra del foco físico terminado, ambas anteriores al JAR de publicación. Los procesos guardan y cierran correctamente.

Publicación verificada: `clean build test`, 204 pruebas sin fallos, errores ni omisiones. Las pruebas dedicadas de escritura y reinicio y el cliente nativo completan todos sus marcadores. El JAR excluye las fixtures; la comparación con port.64 conserva byte por byte todo el contenido anterior salvo las clases del editor/programa, las traducciones y la versión. Las capturas preceden a la creación del artefacto.
