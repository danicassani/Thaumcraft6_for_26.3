# Dispositivos auxiliares: movimiento y redstone — port.43

Este bloque implementa cinco dispositivos de M14. El levitador ya existía; port.44 incorpora las lámparas, urna, spa y pulverizador restantes (UTILITY-MACHINES.md).

## Piedras

La piedra de viaje mide 15/16 de bloque. Al caminar sobre ella, las entidades vivas reciben velocidad II y salto I durante 40 ticks, sin partículas de poción, como en el original. No consume aura.

La piedra de barrera mantiene dos bloques de campo invisibles sobre ella, únicamente donde hay aire. No reemplaza construcciones ni líquidos. La colisión depende de la entidad: bloquea criaturas vivas, deja pasar jugadores, criaturas montadas por jugadores y objetos. La señal de redstone en la piedra suspende la colisión. Retirar la piedra elimina su campo; los bloques internos comprueban también su origen para no dejar barreras huérfanas tras cargar un mundo. La reparación del campo y el empuje de criaturas aéreas se ejecutan cada cinco ticks; no se cargan chunks externos.

## Orejas arcanas

Las dos variantes se montan en cualquiera de las seis caras de un bloque sólido y se desprenden si desaparece el soporte. Un clic vacío cambia la nota, de 0 a 24, reproduce la afinación y muestra nota/instrumento. El instrumento depende del soporte y usa el sistema nativo moderno, incluidos sus nuevos instrumentos.

Escuchan bloques musicales en su propia dimensión a un máximo de 64 bloques, comparando instrumento y nota. La variante normal emite 15 durante diez ticks; la variante conmutador cambia de estado al escuchar una nota coincidente. Repeticiones dentro del mismo tick cuentan una vez por oreja, como la lista de eventos original. Las reproducciones canceladas por Forge no las activan. La afinación no retransmite un evento de bloque musical ni crea bucles entre orejas. Nota, pulso pendiente y estado conmutado se guardan.

La recepción consulta únicamente entidades de bloque de chunks ya cargados. El monitor final del evento Forge recibe la nota/instrumento definitivos y el resultado de cancelación; no depende del audio del cliente.

## Relé

Usa el diodo nativo moderno para mantener dirección, retraso de dos ticks, prioridades de actualización y pulsos breves. El lado de entrada coincide con la orientación; la salida va al lado opuesto. Umbral y salida se ajustan por separado, ambos de 1 a 15 y con vuelta al principio. Un clic vacío en la mitad de entrada ajusta el umbral; en la mitad de salida ajusta la potencia. El mensaje muestra los dos valores.

Recibe potencia normal o de polvo de redstone y emite la salida configurada cuando la entrada alcanza el umbral. Notifica el bloque de salida y sus vecinos, respetando cancelaciones Forge, para alimentar también a través de un bloque sólido. Umbral y potencia se guardan y sincronizan al cliente. No necesita consumo de aura ni sondeo continuo.

## Contenido y adaptaciones

Se incorporan las cuatro recetas arcanas originales (dos piedras, relé y oreja) y la conversión de oreja más palanca a conmutador. Conservan vis, cristales, cantidades e investigación. Se usan etiquetas nativas para las losas de madera y la losa de piedra lisa moderna como equivalente de la losa original. El libro muestra patrones, ingredientes y costes.

Se conservan los modelos de orejas y relé, las dos texturas de estado y las texturas animadas originales de las piedras. Las perillas del relé se controlan por mitades y muestran sus valores en texto; su modelo conserva el aspecto original sin desplazar dinámicamente las perillas.

## Verificación

Servidor nativo: efectos exactos de viaje, campo de dos bloques, colisión de criaturas, paso del jugador, anulación por redstone y retirada sin barreras huérfanas. Pruebas de relé con polvo de potencia 7/8, umbral 8, salida 9, dirección y vuelta independiente de ajustes; un pulso menor de dos ticks se conserva y se apaga con el retraso nativo. Las orejas verifican nota, instrumento, alcance de 64, expiración del pulso de diez ticks, conmutación, repetición en un mismo tick, cancelación Forge, afinación y retirada por pérdida de soporte.

Una segunda JVM verifica umbral/salida, nota y conmutador tras guardar y reiniciar. Los generadores pasan dos veces; la segunda produce cero cambios.

Cliente: selección real de las dos piedras en la mesa por sus cristales y conversión de oreja más palanca. Un jugador conectado se monta sobre una criatura y comprueba que el campo permite su paso. Un clic real en el bloque musical activa la oreja; el cliente recibe el relé con entrada 8/salida 9 y ve una lámpara encendida a través de un bloque sólido. La captura se toma antes de generar el JAR final y antes del push.

Cliente guardado y cerrado normalmente. Compilación limpia: **180 pruebas**, sin fallos, errores ni omitidas. El JAR contiene 330 recetas y 310 definiciones de objetos, conserva las clases de las entregas anteriores y excluye las fixtures de pruebas.

Artefacto: `thaumcraft-6.1.BETA26-port.43-forge66.0.13.jar` (8801285 bytes). SHA-256: `af31d9d360643c48c5cba87f8a1eeefb91920f3f988c1abaa97bfc594d77bb79`.
