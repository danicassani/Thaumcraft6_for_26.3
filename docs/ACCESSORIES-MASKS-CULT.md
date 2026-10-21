# Accesorios, máscaras y equipo carmesí — port.39

M12 incorpora siete ranuras persistentes, las cuatro infusiones del casco de fortaleza y el equipo carmesí. El registro de objetos no sustituye IDs anteriores ni regenera terreno.

## Accesorios

B abre el inventario de accesorios; es una tecla configurable. Uso agachado de un accesorio o del comprobador de cordura también lo abre. Clic derecho con un accesorio equipa la primera ranura compatible vacía y, si están ocupadas, intercambia con la primera compatible devolviendo la pieza anterior a la mano. La mano secundaria de versiones anteriores conserva sus efectos.

| Ranura | Tipo | Ejemplos |
|---|---|---|
| 1 | Amuleto | Amuleto de vis, amuletos simples/refinados |
| 2–3 | Anillo | Anillo de nube, simple, refinado, aprendiz |
| 4 | Cinturón | Faja simple/refinada |
| 5 | Cabeza | Banda de curiosidad |
| 6 | Cuerpo | Reservada al tipo corporal; TC6 no aporta una pieza propia compatible |
| 7 | Talismán | Corazones verdes, vidente del vacío, inmortalidad |

Las piezas sueltas en el inventario no conceden efectos. Se conectan recarga, curación/alimentación, resistencia a veneno/marchitamiento, descuento de vis, distorsión del vidente, conversión de experiencia y escudo rúnico. El amuleto recarga también talismanes puestos. Dos anillos de nube siguen concediendo un único salto adicional por fase de vuelo: no se rearma al cambiar de anillo. Se restaura el impulso de 0,75, bonificación de salto y empuje al esprintar originales. Dos anillos del aprendiz aportan sus descuentos de 5% por pieza.

Se añaden siete variantes de joyería y sus seis recetas simples/refinadas activas. El anillo del aprendiz conserva el objeto y sus efectos, pero no inventa una receta que el original tenía desactivada. Las infusiones recuperan las bases originales de joyería; se conserva también el centro de oro y la cuerda de bolsa usados por versiones previas del port.

El almacenamiento `thaumcraft:accessories` usa formato versionado y codecs con registros. Conserva componentes de cada objeto. Rechaza tipo, cantidad, número de ranuras o versión inválidos, desactiva ese almacenamiento y conserva el NBT original. Un menú abierto deja de aceptar acciones si sus datos cambian externamente.

La muerte entrega cada pieza una vez; `keep_inventory` conserva el equipo. Maldición de desaparición destruye las piezas correspondientes. El clon conserva el estado después de procesar la muerte, evitando una segunda copia. El talismán de inmortalidad se consume una vez desde su ranura y aplica los efectos originales: salud 1, limpieza de estados, regeneración 900 ticks II y absorción 100 ticks II. No añade resistencia al fuego. Respeta los daños que omiten invulnerabilidad, como el tótem nativo; es una adaptación deliberada respecto al evento original sin ese filtro.

## Máscaras y fortaleza

Las tres máscaras y las gafas se añaden por las cuatro infusiones originales de FORTRESSMASK. Conservan el casco real, desgaste, nombre, mejoras rúnicas y demás componentes. Se puede sustituir una máscara por otra; repetir una mejora idéntica no se admite. Máscara y gafas pueden coexistir.

- Diablo sonriente: reduce en 2–5 la severidad de un suceso de distorsión.
- Fantasma furioso: probabilidad proporcional al daño recibido de aplicar marchitamiento al atacante durante 80 ticks.
- Demonio bebedor: probabilidad proporcional al daño infligido de curar un punto de salud.
- Gafas: descuento de 5% y HUD del taumómetro. El HUD también reconoce el casco de túnica del vacío.

Represalias y robo de vida respetan las protecciones de equipos/PVP. Se incorporan los bonus de conjunto de fortaleza como modificadores nativos transitorios; se retiran al desequipar y no se acumulan al volver a entrar. Tres piezas con máscara alcanzan armadura 22 y dureza 12 antes de los cálculos del motor moderno.

## Culto y modelos

Se registran las tres piezas de placas, túnica y pretor, las botas y la espada carmesí. Conservan materiales, durabilidad, encantabilidad, descuentos y distorsión propios. La espada se repara con el tiempo y aplica debilidad 60 ticks y hambre 120 ticks. Los aspectos parten de la pieza de hierro/cuero equivalente y agregan los aspectos originales de culto; el anillo del aprendiz conserva sus aspectos explícitos.

Caballeros y clérigos llevan su equipo correspondiente, con posibilidad nativa de soltar cada pieza del 5%. El pretor se registra sin inventar una receta o sustituir su criatura original pendiente de M16.

Los modelos de fortaleza, caballero, túnica y pretor se generan a partir de las cajas, pivotes, espejos y jerarquías originales, con variantes interiores/exteriores. Jugadores, soportes y cultistas usan el sistema nativo de equipo; los componentes del casco seleccionan las máscaras y gafas visibles. Las poses de torso y extremidades usan la animación moderna. No se reproduce literalmente cada oscilación de telas de 1.12 ni el motor antiguo ISpecialArmor de protección mágica/fuego: la equivalencia completa de esos acabados y otras habilidades activas de equipo sigue en auditoría M12. El proyectil perseguidor del clérigo y sus variantes completas de aparición siguen en M16, como se indicó en port.38.

## Verificación

Las pruebas nativas de servidor verifican las siete ranuras, intercambio y traslado rápido, componentes rúnicos, compatibilidad con mano secundaria, recarga y efectos, salto único, preservación de datos desconocidos, inmortalidad, muerte sin duplicar, keepInventory y clone. Las cuatro infusiones preservan nombre, desgaste y componentes del casco; también se comprueban las represalias y el robo de vida.

Una segunda JVM verifica los datos almacenados en NBT y los modificadores transitorios de fortaleza sin acumulación. En cliente se comprueban tres conjuntos sobre soportes, armadura 22 y dureza 12 del jugador, apertura con B, sincronización y traslado real del anillo con sus componentes. Las capturas se realizan dentro del mundo antes de generar el JAR.

El renderizado descubrió una incompatibilidad de jerarquía: el modelo moderno requiere que `hat` sea hijo de `head`. El generador se corrige y una regresión instancia los ocho modelos nativos para evitar que reaparezca.

El cierre nativo del cliente confirma también el archivo real `players/data/<uuid>.dat`: siete ranuras válidas y el anillo trasladado conserva su componente rúnico. La primera comprobación utilizaba la ruta antigua `playerdata`; se corrigió la prueba a la ruta nativa de 26.3 y se repitió el ciclo completo con éxito.

Compilación limpia: **170 pruebas**, sin fallos, errores ni omitidas. El paquete contiene 320 recetas y 301 definiciones de objetos, sin clases de pruebas. Los generadores completos pasan dos veces y la segunda no modifica archivos.

Artefacto: `thaumcraft-6.1.BETA26-port.39-forge66.0.13.jar` (8672516 bytes). SHA-256: `6f0f73e8dd11a0cfc636056ec1585284d2059a4c81dc9f27ad02441cda2146d5`.

Actualización: [port.40](EQUIPMENT-ABILITIES.md) completa la protección especial, habilidades de herramientas, movimiento del viajero y oscilación de telas, y corrige la visibilidad del faldón del modelo interior. Las adaptaciones concretas al motor moderno se describen allí.
