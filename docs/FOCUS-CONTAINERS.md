# Rotura e Intercambio sobre contenedores — port.76

## Comportamiento

Rotura puede minar contenedores y otros bloques con entidad. Mantiene el trabajo gradual, las grietas, el pago local, Seda/Fortuna y los permisos existentes. La cosecha se ejecuta mediante el controlador nativo del jugador: cofres y hornos sueltan sus inventarios; las shulkers conservan su contenido en el objeto. Los inventarios especiales de Thaumcraft utilizan sus propios procedimientos de retirada. No se vacía manualmente una lista genérica de ranuras que pudiera duplicar los datos de un contenedor portátil.

El trabajo recuerda la identidad de la entidad de bloque original. Cambiar sus ranuras durante el minado es válido: se cosecha el contenido actual. Si se retira el contenedor y se coloca otro del mismo tipo y estado, el trabajo anterior se cancela y limpia las grietas. También se conserva la cancelación por salida del propietario, falta de aura, descarga o cambios del bloque.

Con un foco que incluya Rotura o Intercambio, el clic normal sobre un contenedor lanza el hechizo en lugar de abrir su menú. Se conservan las interacciones especiales de la matriz de infusión y los transfusores. Para abrir el contenedor puede usarse otro objeto o un foco sin estos efectos. No se añade un paquete de red nuevo.

## Intercambio

Una muestra de contenedor guarda exclusivamente el tipo del bloque. Agacharse y seleccionarla nunca copia inventario, esencia, nombre, bloqueos ni datos internos del contenedor del mundo.

Para una muestra sin datos, se busca un objeto del tipo elegido. El foco coloca una copia de la unidad real que consume, conservando sus componentes y datos de entidad mediante las APIs de colocación nativas; `setPlacedBy` conserva las cargas propias del port, como la esencia de un tarro. La alternativa antigua de la mano secundaria exige coincidencia de componentes: una shulker vacía no autoriza consumir otra llena del mismo color. Antes de pagar se vuelve a verificar la unidad elegida y sus datos.

Se prepara el destino sin publicar cambios, invocar la retirada del origen ni ejecutar sus efectos de colocación. Los eventos de protección pueden comprobar el estado y la entidad del destino. Si se deniega, se restaura el mismo objeto de entidad original —con inventario, nombre, menú y capacidades de automatización—; el destino temporal se descarta sin soltar su contenido y el objeto del inventario no se consume.

Al confirmar, se ejecuta una sola vez `preRemoveSideEffects` del origen y después se calcula su botín con esa entidad. Este orden reproduce la retirada nativa: los cofres sueltan sus ranuras, mientras que las shulkers dejan que la tabla de botín empaquete el contenido. El objeto del bloque retirado se recoge en el inventario y el exceso cae en el mundo; el contenido suelto conserva la caída nativa. En creativo se mantiene el comportamiento nativo de las shulkers llenas, que dejan un objeto con su contenido.

Romper o intercambiar una mitad de un cofre doble solo retira su inventario local; la mitad restante conserva sus ranuras y se actualiza a cofre individual. No se aplican los datos del contenedor retirado al bloque nuevo.

## Protección y compatibilidad

`AuraDeviceEntity.supported()` formaliza la comprobación de datos que ya existe en las entidades versionadas del port. Los focos rechazan orígenes y destinos cuyo estado no se puede interpretar, conservando el bloque o el objeto completo. No cambian versiones de guardado, identificadores, recetas ni programas de foco existentes.

Los bloques irrompibles y de administración siguen protegidos. Intercambio mantiene la exclusión de puertas, plantas dobles y camas; no monta estructuras de varias posiciones. Reemplazar un contenedor por el mismo bloque también se rechaza, evitando vaciar o reescribir su inventario como efecto de un cambio de orientación. Las restricciones de aura, construcción, borde, altura y chunks cargados continúan activas.

Se usan los mecanismos de retirada y colocación de cada bloque. La compatibilidad con entidades de otros mods depende de que respeten estos contratos nativos; esta entrega verifica explícitamente contenedores vanilla y los ejemplos de Thaumcraft indicados abajo.

## Verificación

`-PcontainerFocusProbe=write test runServer` verifica:

- Denegación de rotura y colocación sin cobro, botín anticipado ni cambio del inventario. Se comprueban identidad, NBT, capacidad de objetos y un menú previamente abierto.
- Cofre doble, shulker con nombre y contenido, destino lleno denegado sin entidad fantasma, colocación de un objeto lleno y selección sin clonar datos.
- Alternativa de la mano secundaria con una shulker vacía y otra llena del mismo color en el inventario, y con dos shulkers de contenido distinto.
- Cofre hambriento, tarro de esencia, inventario del jugador lleno, creativo y datos de una ofrenda de versión desconocida.
- Rotura gradual que recoge las ranuras actuales, rechazo de duplicados, cancelación Forge, sustitución de la entidad por otra idéntica, shulker portátil y mesa arcana con objetos.

`read` abre otra JVM, recupera un guantelete con muestra de shulker y una shulker llena guardados en un cofre real, y coloca de nuevo su contenido mediante Intercambio.

`client` requiere dos clics físicos en supervivencia: Intercambio sobre un cofre lleno sin abrir su interfaz, seguido de Rotura sobre otro cofre lleno. Comprueba el total exacto de objetos con nombre y datos personalizados, el material consumido, las manos, los paquetes de grietas y su limpieza. Se revisan capturas de ambos casos antes de generar el JAR. Estas escenas se excluyen de la compilación normal.

Resultado final: 242 pruebas unitarias sin fallos, servidor dedicado y reinicio correctos. Cliente nativo con dos clics físicos, sin abrir la interfaz del cofre, y total exacto de 20 diamantes con nombre y datos personalizados (13 de Intercambio + 7 de Rotura). El cliente confirma la retirada del bloque y la limpieza del progreso; dos capturas revisadas antes de generar el JAR, con la de Intercambio compartida antes del push.
