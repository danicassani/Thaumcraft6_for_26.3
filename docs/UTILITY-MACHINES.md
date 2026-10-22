# Lámparas, urna, spa y pulverizador — port.44

Completa los seis dispositivos pendientes de M14. Conserva los cinco dispositivos de movimiento/redstone de port.43 y el levitador de port.23.

## Lámparas

Las tres lámparas se fijan por cualquiera de sus seis caras a un bloque no vacío, incluidos tarros y tubos. La cara de soporte es la entrada de essentia. Una señal de redstone detiene el trabajo y apaga la lámpara; las dos lámparas alimentadas pueden acumular su reserva mientras están pausadas.

La lámpara arcana ilumina a distancia sin coste de aura, con una búsqueda aleatoria cada cinco ticks dentro de 15 bloques por eje, comprobando oscuridad y línea de visión. Cada luz creada conserva posición e identidad de su lámpara. Apagar o retirar una lámpara elimina solo sus propias luces. Una luz cuyo dueño estaba descargado espera su carga para comprobarlo; no carga terreno a la fuerza. La lista está limitada a 2048 posiciones y se depura. El bloque `effect_glimmer` existente se amplía con propiedad opcional: las luces permanentes de la infusión y las de mundos anteriores sin dueño se conservan.

La lámpara de crecimiento consume una unidad de `herba` para obtener 20 intentos y almacena otra unidad como reserva. Recorre una columna por tick dentro del radio original de seis bloques y ejecuta el tick aleatorio nativo sobre cultivos válidos, plantas apiladas y cactus. No se aplica bonemeal gratuito: el crecimiento sigue las reglas y probabilidades del bloque. La reserva y el cursor sobreviven al guardado.

La lámpara de fertilidad almacena hasta diez unidades de `desiderium` y busca cada 300 ticks una pareja adulta de la misma especie en un volumen de siete bloques. Respeta edad, enfriamiento, estado de amor y límite de nueve animales de la especie en el área. Paga cinco unidades y usa el estado de reproducción nativo. Adaptación deliberada: exige las cinco unidades antes de actuar, sin permitir la deuda negativa que podía producir el original.

## Urna inagotable

Almacena 1000 mB de agua. Cada cinco ticks convierte como máximo 0,1 vis en 100 mB; solo produce la cantidad pagada y que cabe. No admite llenado externo. Ofrece extracción Forge por arriba y uso manual de recipientes.

Busca receptores cargados en un volumen de 5×3×5. Entrega hasta 25 mB por operación a capacidades de fluidos, descontando lo aceptado. Llena también los calderos nativos con 333 mB por nivel. Un receptor lleno o una simulación no consumen agua. Los destinos se recorren de forma circular y el cursor se guarda. La integración antigua específica de Botania se sustituye por su capacidad Forge cuando el receptor la ofrezca.

## Spa arcano

Depósito de 5000 mB, una ranura de sales de baño y dos modos con botón nativo. En mezcla consume exactamente 1000 mB de agua y una sal para colocar una fuente de fluido purificador. En vertido consume 1000 mB de un fluido colocable y conserva las sales.

Cada 40 ticks, sin redstone, intenta llenar primero la posición sobre el spa. Después extiende el baño por una superficie 5×5, solo sobre soporte sólido y junto a una fuente existente. Respeta chunks, bloqueos, evaporación de agua y cancelación Forge. Una colocación fallida conserva sus ingredientes. Ofrece fluido por capacidad y recipientes; las sales se automatizan por las caras salvo la superior. El contenido del depósito, sales y modo se guardan; la ranura se entrega al romper el spa.

## Pulverizador

Acepta una poción normal, arrojadiza o persistente con efectos como patrón, que no se consume. Cambiar o retirar el patrón descarta sus cargas y progreso anteriores. La receta de essentia se deriva de los reactivos de las recetas de pociones cargadas, sin reflexión. Conserva el factor original y el límite de diez aspectos; usa la reserva de magia/alquimia original si no puede resolver los reactivos, incluidas pociones personalizadas.

Recibe essentia con succión 128 por caras distintas de la boquilla y consulta también vecinos del nivel superior. Consume una unidad por trasvase y paga la receta completa antes de añadir una carga, hasta ocho. Las simulaciones son puras. El menú muestra cargas y progreso.

Un flanco de encendido de redstone gasta una carga y aplica los efectos nativos, duración y potencia completas, en el volumen 3×3×3 situado dos bloques delante. Respeta inmunidad a pociones e incluye efectos instantáneos. Mantener la señal no repite el disparo; cargar mientras está encendido espera el siguiente flanco. El patrón, receta pagada, progreso, cargas y estado de la señal sobreviven al reinicio para impedir repetir el efecto al cargar el mundo.

## Datos, recetas y visuales

Los estados guardados están versionados. Un formato futuro o inválido desactiva el dispositivo y conserva sus datos en el objeto del bloque al romperlo, sin abrir acceso a un depósito vacío ni descartar el original.

Se incorporan las seis recetas originales: tres arcanas, dos infusiones y la transmutación de la urna en crisol. Mantienen materiales, vis, cristales, essentia e investigación; los cristales de infusión conservan el aspecto específico. El libro muestra los ingredientes. Se reutilizan cuerpos y soportes originales de lámparas, modelo de urna y texturas del spa y pulverizador; efectos de agua, crecimiento y rociado usan partículas nativas. Menús y datos se sincronizan con el protocolo de contenedores de Minecraft.

## Verificación

El servidor nativo verifica luz real, oscuridad/visión, limpieza por redstone y propiedad independiente; conserva también luces permanentes de infusión sin dueño. El cultivo aumenta su edad mediante ticks nativos pagados; se validan reserva, filtrado de cara/aspecto y pausa. Dos animales adultos reciben amor nativo pagando cinco unidades, sin deuda.

La urna comprueba la conversión pagada de vis, extracción simulada, transferencia exacta a una capacidad, rechazo de destino lleno y llenado de caldero. El spa comprueba simulación, bloqueo/cancelación sin pago y consumo exacto de sales/fluido en ambos modos. En el Nether permite el baño mágico y rechaza agua normal sin cobrar. El pulverizador deriva una receta real de poción de velocidad, paga la carga, conserva el patrón, aplica el efecto nativo una vez por flanco y reinicia el progreso al cambiar de patrón. El formato futuro se conserva en el objeto nativo del bloque.

Verificación completada en servidor dedicado y cliente nativo. Un segundo proceso comprueba la persistencia de luz, reservas y poción, sin repetir un disparo con redstone mantenida. En cliente se abre el menú del spa, se cambia el modo mediante su botón y se transfieren sales en ambos sentidos con entradas reales. El mundo se guarda y vuelve al título correctamente.

Compilación limpia `clean build test`: 180 pruebas, cero fallos, errores u omisiones. El JAR de port.44 contiene 336 recetas y 316 definiciones de objeto, sin las clases de prueba. Tamaño: 8 897 748 bytes; SHA-256: `4bb4410b90c94c2777fa553c78e454cc50301d4b0cda46010539598590951a05`. La captura en el mundo se tomó antes de generar el JAR y publicar los cambios.
