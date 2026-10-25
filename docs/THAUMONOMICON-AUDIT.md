# Auditoría de progresión del Thaumonomicon — port.68, actualizada en port.70

## Alcance y resultado

Se revisan las 148 investigaciones, 271 etapas, 16 ampliaciones condicionales, los seis desbloqueos de capítulos y los 34 requisitos de fabricación. El informe reproducible está en `THAUMONOMICON-AUDIT.json`; se genera con `python3 tools/audit_research_progression.py`.

Después de las correcciones, el análisis simbólico alcanza las 148 investigaciones: no quedan consultas a investigaciones inexistentes, índices de etapa inválidos ni ciclos que impidan cumplir los requisitos de fabricación. Los objetos encantados ficticios del original se reconocen como requisitos de encantamiento nativos, no como objetos que deban fabricarse.

Se enumeran además las fuentes de los 28 aspectos exigidos: por ejemplo, gancho de cuerda para Machina, un gólem de hierro para Praecantatio y un wisp para Auram. El análisis presupone materiales, conocimiento y encuentros obtenidos mediante sus fuentes de juego. No demuestra el balance de cantidades o duración de una partida, la disponibilidad geográfica de cada encuentro ni todas las combinaciones con mods y datapacks. La ruta inicial sí se prueba con las acciones del servidor, partiendo de conocimiento vacío, sin conceder investigaciones y con materiales de prueba suministrados.

## Bloqueos y omisiones corregidos

| Hallazgo | Consecuencia | Corrección |
| --- | --- | --- |
| Disparadores de clases de 1.12 convertidos en IDs inexistentes (`minecraft:golem`, `minecraft:cultist`, etc.) | Escanear un gólem de hierro no satisfacía el acceso a golemancia; los cultistas y otras criaturas no descubrían sus entradas | IDs modernos comprobados contra el registro nativo, incluidas variantes, gólems de hierro/nieve, constructos y firebat |
| Spellbat seguía usando el sustituto provisional de blaze | Descubrir el murciélago ígneo real no cumplía su padre original | Se recupera `!Firebat` |
| Escaneos antiguos no volvían a disparar descubrimientos corregidos | Una partida actualizada podía conservar el bloqueo después de escanear de nuevo | Reparación idempotente a partir del historial, sin volver a conceder puntos ni cambiar los escaneos |
| Estudiar Ritos carmesíes guardaba una bandera, pero el catálogo exigía terminar sus dos etapas | El estudio no habilitaba correctamente el capítulo Eldritch | El estudio completa su entrada real; se reconocen también las banderas legítimas de versiones anteriores |
| `WARP` no tenía productor y el medidor de cordura pedía `SANITYCHECKER`, que no existe en el catálogo | Tema oculto permanentemente y receta imposible de autorizar | Distorsión duradera tras Primeros pasos descubre `WARP`; la receta vuelve a pedir `WARP`, como el original |
| Se habían omitido bonificaciones de aspectos complejos en objetos básicos | Las antorchas no aportaban Lux al primer nitor; faltaban fuentes normales de Alkimia y otras observaciones | Bonificaciones originales para diez objetos de inicio, sumadas a sus aspectos inferidos de recetas |
| El catálogo ignoraba las 16 ampliaciones (`addenda`) | Faltaban las instrucciones ampliadas de cinco sellos avanzados y de plantas, minerales y fuelles; las recetas avanzadas se habían añadido provisionalmente a páginas básicas, revelándolas antes de tiempo | Carga, comprobación de requisitos, sincronización y páginas condicionales; no se revelan antes de tiempo |
| No había recetas para batería de vis, raíl arcano ni abrazadera | El libro mencionaba objetos sin fabricación normal disponible | Se restauran recetas y costes originales; se corrige el alias de la abrazadera |
| La purificación de cobre no estaba conectada al cobre moderno | No había ilustración ni receta para el mineral de cobre nativo | Recetas para cobre normal y profundo, con su alias original |
| Requisitos mostrados mediante claves internas y sin distinguir `@etapa` | El jugador no sabía qué observar o qué etapa alcanzar | Nombres comprensibles en inglés/español, números de etapa y frascos ilustrados con su esencia |

La extracción de recursos se ejecuta en este orden al regenerar el contenido: generadores históricos, `generate_progression_fixes.py`, `generate_recipe_views.py`, `audit_research_progression.py`. Las bonificaciones corregidas se suman en el resolvedor compartido, de modo que escaneo y crisol usan los mismos aspectos. Esto no certifica equivalencia completa de todas las anotaciones de aspectos complejos de TC6.

## Comprobaciones

El servidor dedicado comprueba los IDs de todos los disparadores. La ruta inicial crea el libro con Salis Mundus, abre Primeros pasos, transforma una mesa, recoge un taumómetro fabricado mediante el menú nativo y paga sus cristales y vis. Obtiene observaciones escaneando objetos, transforma un caldero autorizado y prepara nitor en un crisol con agua y calor usando carbón, antorchas y el catalizador. Solo entonces completa el desbloqueo de Alquimia.

También se escanean entidades reales de gólem de hierro, cultista y murciélago ígneo; se estudia físicamente una curiosidad de Ritos carmesíes, se verifica el descubrimiento de distorsión y el requisito del medidor de cordura y se comprueba que el sello avanzado se oculta y aparece según la mente biotaúmica. Las pruebas unitarias verifican la reparación de partidas sin recompensas duplicadas, sus límites de desbloqueo y la compatibilidad de Ritos antiguos.

La escena del cliente utiliza conocimiento controlado para comprobar exclusivamente la presentación: sello básico terminado, mente biotaúmica inicialmente sin descubrir y posterior finalización desde el servidor. Se exige navegación por clics nativos hasta la ilustración avanzada. Esta escena no se contabiliza como una prueba de supervivencia ni como una ruta de progresión ganada por el jugador.

## Pendientes detectados en port.68, resueltos en port.70

Quedan doce referencias originales sin una ilustración de receta estática:

- Siete describen procesos especiales o estructuras: horno infernal, tres variantes del altar, prensa de gólems, Thaumatorium y etiquetado de esencia. Sus procesos funcionan y tienen instrucciones, pero falta una representación específica en esas referencias del libro.
- Tres son variantes de purificación de estaño, plata y plomo. No hay minerales vanilla correspondientes; esta auditoría no añade integración con minerales de otros mods ni certifica sus recetas opcionales.
- `Banners` carece del grupo de fabricación original. Es una omisión de contenido decorativo, sin dependencia en las 271 etapas.
- `baubles_stuff` es una referencia al grupo del accesorio antiguo. El port tiene su sistema de accesorios adaptado, pero falta una página equivalente para ese grupo.

De 433 claves de títulos, etapas y ampliaciones, 378 recurren al inglés en español. La traducción española del libro sigue siendo parcial y numerosos títulos y párrafos usan la traducción inglesa de respaldo. Esta entrega traduce los nuevos objetivos, no las 148 investigaciones completas. También quedan por probar recorridos largos de supervivencia y multijugador, presupuestos completos de observaciones/teorías y combinaciones con datapacks. No se declara equivalencia total del Thaumonomicon.

Se detecta también una instrucción funcional pendiente en Tarros protegidos: el texto original atribuye a la abrazadera el bloqueo de llenado/extracción manual, pero el código moderno no contiene aún ese consumidor del objeto. Esta entrega restaura su fabricación e ilustración; no implementa ni certifica la función de cierre. No es un requisito de una etapa, pero debe corregirse antes de declarar equivalencia del capítulo.

## Entrega verificada de port.68

La compilación limpia final pasa 214 pruebas sin fallos, errores ni omisiones. Cliente y servidor finalizan normalmente. El JAR excluye las fixtures y conserva el contenido de port.67 ajeno a estas correcciones. Las capturas de la receta condicional se tomaron antes de generar el artefacto. No cambia la versión 1 del conocimiento guardado ni se retiran identificadores publicados.

## Cierre de las omisiones del libro — port.70

Las doce referencias anteriores ya tienen representación: seis diagramas de estructuras por capas (horno, tres altares, prensa y Thaumatorium), etiquetas codificadas con su aspecto real, grupos de accesorios y estandartes, y las tres purificaciones opcionales. El generador `generate_book_completion.py` debe ejecutarse después de los generadores anteriores. La auditoría reproducible ya devuelve `references_without_diagram: []`.

Los estandartes usan objetos vanilla, con su colocación, patrones de telar y persistencia nativos; se añaden las dieciséis recetas arcanas de colores. No son un nuevo bloque con el antiguo renderizador de símbolos de esencia. Las purificaciones opcionales usan `c:ores/tin`, `c:ores/silver` y `c:ores/lead`: se declaran etiquetas vacías y la receta solo puede coincidir cuando un mod o paquete de datos aporta minerales. El libro indica su ausencia. Esto no certifica la integración de todos los metales de terceros ni añade sus minerales al mundo.

Las 433 claves referenciadas de títulos, etapas y ampliaciones tienen traducción española, sin recurrir al inglés. Las 378 traducciones añadidas están en `tools/es_research_texts.lang`, con redacción adaptada a los controles del port y conservando instrucciones, imágenes y divisiones de página. Se añaden asimismo las glosas españolas de los aspectos.

La abrazadera se consume una sola vez, persiste en tarros normales y del vacío, impide transferencias por viales y extracción remota, permite tubos y vuelve como un objeto separado al recoger el tarro. La esencia y la etiqueta permanecen en el objeto de tarro. Los tarros antiguos sin el nuevo campo siguen abiertos.

La entrega port.70 pasa 224 pruebas sin fallos, errores ni omisiones y las verificaciones nativas finales de cliente y servidor, con cierre normal. Los detalles de composición, capturas y alcance están en [THAUMONOMICON-COMPLETION.md](THAUMONOMICON-COMPLETION.md).
