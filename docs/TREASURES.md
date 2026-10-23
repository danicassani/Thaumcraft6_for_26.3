# Tesoros: port.48

Tres bolsas y seis contenedores (cajas/urnas comunes, poco comunes y raras), con IDs separados para sustituir los metadatos antiguos. Se usan los modelos, texturas y sonidos originales.

## Apertura

Las bolsas se apilan hasta 16. Usarlas con cualquiera de las manos genera de 8 a 12 recompensas físicas en el servidor y consume exactamente una bolsa, también en creativo, como el original. El cliente no genera recompensas. Si Forge cancela la incorporación de una recompensa, se retiran las ya incorporadas y la bolsa se conserva; una tabla ausente tampoco cobra. Se usa el retardo de recogida nativo de los objetos soltados.

Las cajas y urnas se abren al romperlas: entre 1 y 3 recompensas comunes, 2 y 4 poco comunes, o 3 y 5 raras. Seda devuelve exclusivamente el bloque intacto, sin tirar contenido. No tienen inventario ni contenidos pregenerados. Se conservan las formas de selección/colisión del original, dureza 0,15 y sonidos de madera/urna. La condición nativa de supervivencia a explosiones evita entregar recompensas de bloques destruidos que no sobrevivan a la tirada.

## Recompensas

Tablas `thaumcraft:treasure/base_common`, `base_uncommon` y `base_rare`: pesos originales de pepitas (1/2/3 unidades), Salis Mundus, gemas, perlas de ender, accesorios, libros, manzanas y botellas de experiencia. Se conservan las variantes y pesos de la perla primordial mediante su componente actual de desgaste. La estrella del Nether solo está en la tabla rara.

Como el original recorría el registro de pociones, el generador enumera las 46 pociones vanilla de 26.3, con peso 2 por variante y recipiente; las persistentes solo aparecen desde poco común. Se guardan sus componentes nativos. Los datapacks pueden extender estas tablas para añadir pociones de otros mods.

Un 2,5 % de las recompensas poco comunes y un 5 % de las raras eligen equipo. Se conserva la distribución de calidad original: elección inicial 0/1 y cinco mejoras independientes (20 %, 15 %, 10 %, 9,5 %, 9,5 %), con cinco categorías de equipo y siete calidades, hasta diamante y vacío. Los pesos racionales suman 400 000 000, dentro del entero nativo.

Los libros se encantan siempre; el equipo, con probabilidad 25 %/50 %. Los niveles reproducen las 18 posibilidades de `int(5 + rareza × 0,75 × n)` mediante un proveedor ponderado nativo. Los encantamientos proceden de la mesa de encantamientos actual, sin encantamientos de tesoro. El equipo utiliza el modificador nativo de durabilidad entre 5/6 y 1: conserva aproximadamente el sexto de desgaste original, con el redondeo y las durabilidades actuales.

Las tablas `treasure/common|uncommon|rare` producen una recompensa; `bag_*` y `cache_*` aplican las cantidades. Las seis tablas de bloque reutilizan las de contenedor. No se añaden recetas gratuitas: estos tesoros no tenían fabricación en TC6.

## Disponibilidad y siguiente etapa

Los nueve objetos están registrados, disponibles en la pestaña creativa y utilizables por comandos/datapacks. Esta entrega implementa sus mecánicas y recursos. Su colocación natural en estructuras y la integración con la generación de mundo corresponden a la siguiente etapa. M16.1 conecta la bolsa de campeones: un campeón que no sea `tainted` suelta una bolsa con tier `min(2, floor((rand(9) + Fortuna) / 5))` y 5–7 XP (CHAMPIONS.md). Los drops de jefes siguen pendientes. No se inventan fuentes alternativas de fabricación.

## Verificación de port.48

Prueba nativa en servidor dedicado: 4096 extracciones base por rareza, 128 bolsas por rareza, 2048 extracciones de equipo por rareza superior y 128 semillas por cada uno de los seis contenedores con y sin Seda. Se verificaron las tres bolsas con mano secundaria, consumo en creativo, cancelación de la segunda entidad con retirada de la primera y rechazo de repetir el uso con una pila vacía.

Un segundo proceso de servidor recuperó desde un cofre las pociones, perlas desgastadas, libros y equipo encantados y bolsas sin abrir. En cliente, una interacción real consumió exactamente una de dos bolsas raras y sincronizó 19 unidades de recompensa. Se revisaron visualmente los seis modelos y se guardó y cerró el mundo. La captura se realizó antes de generar el JAR final. Los sonidos están incluidos; su reproducción no pudo comprobarse porque el entorno de pruebas desactivó el sistema de audio.

Compilación limpia `clean build test`: 180 pruebas, sin fallos, errores ni omisiones; 205 tablas de botín y 635 referencias de modelos validadas. El JAR contiene 336 recetas y 325 definiciones de objetos, sin clases de las pruebas nativas.

Artefacto: `thaumcraft-6.1.BETA26-port.48-forge66.0.13.jar`, 8941496 bytes. SHA-256: `283d8c1a4b6fb376fd2d38a042b928cdcca6bfc898102c2983f6eb4a6edc9a80`.

Actualización port.50: las cajas y urnas aparecen naturalmente en los túmulos; el cofre de greatwood infestado conserva el botín vanilla original. Véase [WORLDGEN-STRUCTURES.md](WORLDGEN-STRUCTURES.md). M16.1 aporta la fuente de bolsas de campeones; la de jefes sigue pendiente (CHAMPIONS.md).
