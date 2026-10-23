# Encuentros del culto — port.51

Los portales carmesíes menores aparecen mediante una feature nativa al generar chunks nuevos del Overworld. Conservan el ID `thaumcraft:cultist_portal_lesser` y las entidades de caballero y clérigo de los sucesos de distorsión.

## Generación y compatibilidad

- Césped, podzol y micelio se comprueban explícitamente: la etiqueta nativa `minecraft:dirt` de 26.3 ya no los incluye.
- Filtro de rareza de 1/500 por chunk, seguido de comprobaciones de terreno y espacio. Permite tierra, piedra natural, arena y nieve compacta, con suelo sólido y tres bloques de aire. No aparece en agua, fuera del borde o de los límites verticales, en mundos planos, en otras dimensiones, en pacífico o con `wussMode`.
- `world.generateStructure` controla los encuentros nuevos. No se recorren chunks ya cargados ni se hace retrogeneración. Al actualizar un mundo, los encuentros podrán aparecer en zonas que se generen por primera vez.
- La entidad queda guardada con su chunk, no como un temporizador que la vuelva a crear. Destruirla no regenera el encuentro. Su salud, activación y próxima invocación sobreviven al guardado. Los portales de versiones anteriores mantienen su ID y se consideran activos si ya tenían un plazo de invocación.
- La probabilidad original se adapta a un filtro de decoración independiente del sorteo de túmulos. El espacio libre y la superficie válida reducen la frecuencia efectiva. Los datapacks pueden modificar la feature colocada y su modificador de biomas.

## Encuentro

Un portal nuevo permanece dormido hasta detectar un jugador elegible a 32 bloques. Abre su animación original durante 50 ticks; espera 100 ticks desde la activación para la primera oleada. Las siguientes comprobaciones llegan cada 50–99 ticks.

Requiere un jugador visible y admite como máximo 2/4/6 cultistas vivos próximos en fácil/normal/difícil. Consulta la visión directamente, sin la caché de una IA desactivada. Cada invocación exitosa crea un caballero (67 %) o clérigo (33 %) y cuesta 5–9 de los 100 puntos de salud del portal. Una invocación obstruida, cancelada o limitada no gasta salud. Mantiene las restricciones de chunks cargados y colisión del port anterior.

El portal es inmóvil, persistente e inmune a fuego, caídas y efectos de poción. Tocarlo puede infligir cuatro puntos de daño mágico atribuido al portal. Su muerte provoca una explosión de fuerza 1,5 sin destruir bloques ni generar fuego, como el encuentro original. La aparición de esbirros emite partículas y un pulso visible. Pacífico y `wussMode` siguen retirando estas criaturas, incluidos los portales de distorsión.

El atlas original se anima con apertura y oscilación según la salud. El daño de contacto usa el sonido original `zap`; la apertura y la invocación usan sonidos nativos como adaptación. Caballeros y clérigos conservan su equipo, botín y ataques anteriores.

## Proyectil perseguidor del clérigo (M16.3)

El clérigo elige en cada ataque a distancia entre las tres bolas de fuego pequeñas originales (66 %) y el orbe perseguidor (34 %). El orbe se registra como `thaumcraft:golem_orb` y reproduce `EntityGolemOrb`: sin gravedad, 240 ticks de vida, aceleración de persecución de 0,2 por tick con cada eje limitado a ±0,25, y daño indirecto de magia igual al atributo de ataque del lanzador. Se dirige al centro del cuerpo del objetivo, con el mismo desplazamiento de puntería y dispersión que el disparo original. Al impactar usa la estela de partículas nativa y el sonido `zap` del portal, la aproximación disponible al `shock` original. El renderer de orbe se comparte entre `WarpOrb` y `ClericOrb`. El identificador del objetivo se sincroniza al cliente (y se guarda), de modo que la persecución visible utiliza la misma trayectoria que el servidor, como el dato de aparición del original.

## Botín de cultistas y cerebros

Al morir un caballero o clérigo a manos de un jugador, puede soltar una curiosidad carmesí de la variante `rites` (equivalente al metadata 6). El denominador es 4 por defecto, 20 si el jugador conoce `!CrimsonCultist@2` y 50 si lleva una curiosidad `rites` en el inventario; el curio se sortea con `nextInt(denominador) == 0`. La variante `rites` se identifica por su componente, no por un metadata inexistente en 26.3.

El cerebro cae de zombis recién golpeados con `rand.nextInt(10) - Fortuna < 1`, usando el nivel de Fortuna del arma del asesino. Se excluye a los zombis con cerebro, pendientes de portar; no se añade una variante provisional. Estas reglas puras viven en `CultistRules` y tienen pruebas JUnit dedicadas.


## Alcance pendiente

Los portales mayores, el pretor, jefes, bioma eerie, estructuras eldritch y su dimensión siguen pendientes. Las variantes de zombi con cerebro y firebat, y el resto de criaturas de M16, también quedan fuera de esta entrega. No se considera cerrado M16.

## Verificación

Los fixtures opcionales `cult-server`, `cult-read` y `cult-client` no forman parte del JAR normal. La búsqueda de encuentros usa la ordenación nativa de features y el RNG de decoración para seleccionar candidatos del filtro de rareza publicado; no modifica su frecuencia ni crea un portal de sustitución para la captura.

Servidor dedicado: encuentro generado por la feature publicada en `(-3959, 80, -3235)`, activación, primera oleada real y coste de nueve puntos de salud; contacto mágico y límite normal sin gasto adicional. También pasan suelo de césped/podzol/micelio/arena, obstrucción, opciones de generación, `wussMode`, inmunidad a pociones y cierre sin romper el suelo.

Segundo proceso dedicado: mismo UUID, activación, persistencia y salud de 91 conservados, con `generateStructure=false` durante la comprobación.

El primer fixture de cliente seleccionó un portal antiguo de distorsión del mundo de pruebas. Se corrigió para aceptar únicamente entidades del chunk candidato de la feature nativa, evitando confundir una entidad previa con generación nueva.

Cliente nativo: portal natural en `(-5011, 71, -3439)` en llanura de girasoles; activación, oleada, estado sincronizado y render de portal/cultistas. Captura compartida antes del JAR final y de ambos pushes. SHA-256 de la captura: `463eed4104dbd1509ce9f338489db1fe012afefaf49bc74573ff725a3372f5fe`.

Construcción normal `clean build test --offline`: 187 pruebas, cero fallos, errores u omisiones; 635 referencias de modelos, 336 recetas, 325 definiciones de objetos y 205 tablas de botín. Generadores en dos pasadas, segunda sin cambios. JAR sin fixtures, sin entradas ZIP duplicadas y con JSON válidos.

Entrega: `thaumcraft-6.1.BETA26-port.51-forge66.0.13.jar`, 8987411 bytes. SHA-256: `81faa666b1417d3fa9e2a8ac68939fead7e08eb74679d18b8eb0f3476e3e1b69`.

Actualización port.52: las apariciones naturales de wisps en bosque mágico y Nether pasan a estar implementadas; véase [WISP-NATURAL-SPAWNS.md](WISP-NATURAL-SPAWNS.md). Los demás pendientes de esta entrega se mantienen.

Actualización port.54: los pechs ya están implementados; véase [PECHS.md](PECHS.md). Las apariciones naturales de wisps están disponibles desde port.52.

Actualización M16.3: el proyectil perseguidor y los drops de curiosidad/cerebro están implementados. `ClericOrb`, `WarpMonster`, `WarpCreatures`, `CuriosityContent` y `WarpRenderers` compilan contra las APIs reales de Forge/MC 26.3; las clases con APIs parcheadas ajenas (`KnowledgeStore`, `BasicContent`, `SpellEngine`, `RiftContent`) se sustituyeron por stubs mínimos solo para esa comprobación dirigida. `CultistRulesTest` pasa 4/4 con JUnit. No se ejecutó el `clean build` completo: el decompilador de `MinecraftMavenizer` sigue agotando memoria (código 137). Queda pendiente una prueba en cliente de la persecución visible y del drop real en un mundo dedicado.
