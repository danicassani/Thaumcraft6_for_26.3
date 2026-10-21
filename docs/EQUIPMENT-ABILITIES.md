# Habilidades elementales y protección de equipo — port.40

Cierra las habilidades adicionales y la protección especial pendientes de port.39 sin cambiar IDs ni generación del mundo.

## Herramientas

- Espada elemental: mantener uso activa el viento. Cada tick amortigua la velocidad descendente y la distancia de caída dividiendo entre 1,2; añade 0,08 vertical, con el mismo límite original. Empuja criaturas próximas a 2,5 bloques, excluyendo jugadores y aliados. Consume una unidad de durabilidad por segundo. El servidor permite este vuelo mediante su API nativa de reinicio del contador de flotación; no habilita vuelo permanente.
- Pico elemental: sus golpes cuerpo a cuerpo encienden al objetivo durante dos segundos, con las mismas protecciones PVP/equipos.
- Hacha elemental: mantener uso atrae los objetos físicos dentro de diez bloques, con velocidad limitada a 0,25 por eje. No crea objetos, cambia propietarios ni evita las reglas de recogida.
- Azada elemental: uso normal transforma suelo en 3×3 mediante el transformador nativo de azada; agachado selecciona una sola posición. Si no transforma suelo, aplica crecimiento nativo pagando tres unidades de durabilidad. Cada posición comprueba permisos y conserva los eventos Forge de transformación/bonemeal.
- Pala elemental: uso sobre un bloque sin entidad de bloque amplía su cara en un plano de nueve posiciones. Selecciona las tres orientaciones originales mediante uso agachado en el aire; agachado sobre bloques conserva el uso nativo de pala. Hay previsualización. Usa bloques reales equivalentes del inventario y paga un punto de durabilidad por colocación; acepta tierra cuando se copia césped. Comprueba carga del chunk, límites del mundo, permisos, colisión, supervivencia y cancelación Forge por posición. Una cancelación restaura el bloque y no cobra. No sustituye bloques existentes ni copia inventarios.
- Herramientas del vacío: los golpes cuerpo a cuerpo aplican debilidad 80 ticks, 60 para la espada. Respetan PVP y equipos; sujetar un arma mientras se lanza un foco no aplica el efecto.

Las infusiones pasivas (Burrowing, Collector, Refining, Sounding, Arcing y Destructive) siguen usando su implementación existente.

## Protección y movimiento

Las piezas de fortaleza absorben magia con la proporción original puntos/35 y fuego/explosiones con puntos/20. Las túnicas del vacío absorben magia con puntos/35. La magia tiene precedencia; la absorción se suma por piezas, se limita por durabilidad restante y desgasta la armadura. `thaumcraft:is_magic` es una etiqueta extensible con los daños mágicos nativos. Los daños que omiten invulnerabilidad no se interceptan.

Adaptación al motor moderno: esta fase especial antecede a la armadura, dureza, encantamientos y resistencia nativos. El daño físico ordinario conserva esos cálculos modernos; no se reintroduce globalmente ISpecialArmor de Forge 1.12. Los atributos de fortaleza de port.39 permanecen independientes.

Las botas del viajero usan una reserva de 60 segundos entre pagos de carga, en lugar del consumo por segundo del port anterior. La velocidad y altura de paso se conceden mediante modificadores transitorios y se retiran al agacharse, volar, quedarse sin carga o desequiparse. La amortiguación original de caídas de botas y anillo se calcula sobre el daño, primero botas (mitad menos uno), después anillo (tercio menos dos), suprimiendo residuos menores de un punto. No consume carga por caída ni altera las pociones de otros sistemas. El control aéreo se integra con los atributos modernos en lugar de reemplazar el antiguo `jumpMovementFactor`.

## Telas

Los modelos del caballero, el pretor y la túnica carmesí recuperan las ecuaciones originales de oscilación frontal y posterior según fase y velocidad de marcha. Las túnicas del vacío, que comparten geometría de túnica, usan el mismo movimiento. Se recalcula cada cuadro para evitar que un modelo cacheado herede la pose de otra entidad.

## Verificación

La prueba nativa de servidor verifica las proporciones de protección de fortaleza/túnicas, exclusión del daño que omite invulnerabilidad, ascenso y amortiguación/repulsión/coste de espada, atracción física sin duplicar objetos, transformación real de nueve suelos y pago exacto de la pala ante una posición cancelada por Forge. Comprueba también reserva, altura de paso y retirada del modificador del viajero. Otra JVM vuelve a cargar la pala desde un cofre y confirma orientación y desgaste.

Las regresiones incluyen los nueve destinos de cada cara/orientación, proporciones y límites de absorción, composición de caídas, jerarquía de los ocho modelos, ecuaciones de telas y modelos interiores sin duplicación del pecho. Los generadores completos pasan dos veces sin cambios en la segunda.

En cliente se realiza un uso real de la pala desde la escena cargada: nueve bloques de tierra aparecen sincronizados y se muestra el siguiente plano. La captura se toma antes de generar el JAR y antes del push.

El cliente guarda, vuelve al título y cierra normalmente. Compilación limpia: **177 pruebas**, sin fallos, errores ni omitidas. Paquete revisado con 320 recetas y 301 definiciones de objetos, sin fixtures de pruebas ni entradas ZIP duplicadas.

Artefacto: `thaumcraft-6.1.BETA26-port.40-forge66.0.13.jar` (8690520 bytes). SHA-256: `4c1ce629e065efbcc659fd95de0ae469b8baa3672e76f67f892c281f90057ac9`.
