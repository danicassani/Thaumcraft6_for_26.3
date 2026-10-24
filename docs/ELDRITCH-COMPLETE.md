# Criaturas y contenido eldritch: port.56

Minecraft 26.3 / Forge 66.0.13 / Java 25. Esta entrega completa las criaturas que faltaban del registro de TC6 y revisa los encuentros de jefes. Conserva los IDs de contenido ya publicado y no añade retrogeneración.

## Registro y comportamiento

| Entidad | Comportamiento principal |
| --- | --- |
| `brainy_zombie` | Zombi nativo, 25 de salud, daño base 5, armadura adicional, sin refuerzos; cerebro con probabilidad 50% + 10 puntos por Saqueo. |
| `giant_brainy_zombie` | Zombi nativo, 60 de salud, ira sincronizada y persistente, crecimiento hasta ×3 y daño según la ira. |
| `inhabited_zombie` | Zombi con equipo carmesí; libera exactamente un cangrejo al morir. |
| `eldritch_crab` | 20 de salud, caparazón que se rompe a media salud, inmunidad al veneno, salto/montura y ataques al huésped. |
| `firebat` | Murciélago hostil de 5 de salud, vuelo nativo, inmunidad a fuego/explosiones, daño al mojarse y explosión de contacto de 1/10 sin destruir terreno. |
| `cultist_leader` | Pretor con 150 de salud, equipo de pretor, ataques de orbe y bolsa rara. |
| `eldritch_golem` | Constructo de 400 de salud, llegada protegida, transición al perder la cabeza ante daño letal y descarga/recarga de orbes en la segunda fase. |
| `eldritch_warden` | Carcelero de 400 de salud y 264 de escudo, orbes sobrenaturales, grito, ruptura del escudo, regreso al origen y frenesí de campos de drenaje. |
| `cultist_portal_greater` | Portal de 500 de salud, apertura, oleadas iniciales, pretor en la etapa 12 y agotamiento posterior. Salud, etapa y temporizador persistentes. |

El tentáculo gigante conserva `taintacle_giant`, registrado en M13. Se completan barra de jefe, regeneración, límite de daño y perla primordial solo al morir el último tentáculo gigante próximo; no se registra otra entidad con el mismo ID.

Los jefes mantienen barras nativas, límite de daño por golpe, regeneración, enfurecimiento y, en los encuentros eldritch, adaptación a hasta cinco jugadores adicionales (+50 de salud y +0,5 de daño por jugador). La selección de objetivo usa UUID y amenaza de entidades vivas cargadas. Se evita copiar el error del original que confundía el ID de la entidad con su amenaza. El escudo usa `MAX_ABSORPTION`, obligatorio en la API moderna.

Los campos `effect_sap` no tienen colisión ni objeto de inventario: producen wither 40 ticks, lentitud II y hambre II. Solo se colocan en aire sobre suelo sólido, dentro del mundo y de chunks cargados; desaparecen tras 250–399 ticks mediante ticks programados que se guardan con el chunk. El modo wuss desactiva su daño. No sobrescriben contenedores ni fluidos.

Las nueve entidades nuevas tienen huevos de aparición en la pestaña creativa. Se conservan texturas y sonidos originales; los modelos estáticos de cangrejo y constructo traducen sus 21 y 36 cajas originales, respectivamente. El pretor utiliza el equipo y renderer de armaduras moderno; los zombis y murciélago usan modelos nativos compatibles. Se añaden los aspectos originales de escaneo.

## Aparición natural y contenido ausente del original

Se añaden el zombi furioso a biomas del Overworld (peso 10, un individuo) y el murciélago de fuego al Nether (peso 10, uno o dos). Los predicados comprueban sus opciones de configuración, dificultad, modo wuss, luz y espacio adecuados. Eerie conserva sus pesos de bruja, enderman, zombis, wisps y guardianes. El bioma eldritch conserva zombis habitados y guardianes. Los jefes y el portal mayor no reciben una aparición natural inventada: el código original solo los registra, sin generarlos en `ThaumcraftWorldGenerator`.

La lista anterior arrastraba un pendiente incorrecto de «dimensión». En este repositorio TC6 registra los biomas `eerie` y `eldritch` (`Registrar.registerBiomes`), pero no registra `WorldProvider`, proveedor/generador del Outer Lands ni una dimensión. `dimensionOuterId` es únicamente una opción usada para excluir su supuesto ID de la generación general. Se portan ambos registros de bioma, colores, clima y listas de criaturas; no se conectan al clima del Overworld ni se crea una dimensión nueva. Están disponibles para datapacks y `/fillbiome`. Una dimensión jugable, su acceso y estructuras serían contenido nuevo, no una parte de TC6 pendiente de traducir.

## Adaptaciones modernas

- El crecimiento del gigante actualiza también su colisión mediante `SCALE`; el original dejaba temporalmente desacoplados modelo y caja física.
- La IA de combate, el renderizado de armaduras, la interpolación y las partículas usan los servicios nativos de 26.3. No se reproducen shaders ni los paquetes de efectos de 1.12.2.
- El frenesí del carcelero conserva los anillos de campos de drenaje; sus partículas se adaptan al motor actual. La selección de amenaza corrige el error de IDs del código antiguo.
- El portal limita a 24 cultistas locales para evitar crecimiento sin límite. Una invocación obstruida no avanza la etapa ni gasta salud. No carga chunks a la fuerza.
- El aspecto de los huevos usa el icono nativo de huevo; sus nombres y tipos de entidad son independientes.
- Los dos biomas conservados son registros para contenido externo; no se inventa una ruta de generación para ellos. La bonificación de murciélagos del Overworld del 31 de octubre conserva peso 5 y grupos de uno o dos; se evalúa al cargar los datos de bioma, como al arrancar el original.

## Verificación

Fixtures optativas, excluidas del JAR: `-PeldritchProbe=write runServer`, `-PeldritchProbe=read runServer` y `-PeldritchProbe=client runClient`. Comprueban registros, atributos, guardado, cambio de fase del constructo, nacimiento sin duplicación del cangrejo, aparición real del pretor, final del frenesí, regeneración de escudo y recuperación en otro proceso. Las comprobaciones de botín matan entidades reales y cuentan las perlas y bolsas emitidas; las comprobaciones de cliente exigen entidades sincronizadas y renderer activo, además de capturas dentro del mundo.

Resultados nativos de port.56: los procesos dedicados y el reinicio concluyeron con éxito. Se verificaron el umbral real de armadura del cangrejo y 64 muertes de zombis furiosos, con botín vanilla y como máximo un cerebro por muerte. Dos muertes de tentáculos gigantes produjeron cero perlas antes del último y una sola en la última. La aparición del guardián respeta su opción de configuración y conserva la regla original de luz, además de su límite local. Los generadores son estables en dos pasadas; 648 referencias de modelos y 215 tablas de botín superan los verificadores.

Revisión visual: el constructo conserva también la escala ×1,7 del renderer original; la fixture de cliente comprueba ese factor en el estado de renderizado extraído, además de la sincronización de las nueve entidades. Esta entrega incluye las correcciones de navegación del Thaumonomicon y salida nativa de la mesa arcana documentadas en [BOOK-ARCANE-UI.md](BOOK-ARCANE-UI.md).
