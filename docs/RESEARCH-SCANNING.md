# Investigación y escaneo: núcleo de M04 (port.8)

Minecraft 26.3, Forge 66.0.13, Java 25. Este lote incorpora conocimiento del
jugador, escaneo, Thaumonomicon y requisitos de investigación del crafteo arcano.
La mesa de investigación, las cartas de teorías y la observación celeste son el
siguiente subpaso; no se declara M04 completo.

## Cómo empezar

1. Recoge cristales de vis y duerme. Al despertar recibes un diario con las
   instrucciones. `SERVER misc.noSleep=true` permite obtenerlo sin dormir.
2. Fabrica Salis Mundus con tres cristales de aspectos distintos, redstone,
   pedernal y cuenco. Se devuelven el pedernal y el cuenco.
3. Aplica Salis Mundus a una librería: se transforma en un Thaumonomicon.
4. Abre el libro y sigue Primeros pasos. Aplica Salis Mundus a una mesa de
   crafteo; el libro permite avanzar a la etapa que desbloquea el taumómetro.
5. Fabrica el taumómetro en la mesa arcana: cuatro lingotes de oro alrededor
   de un panel de cristal, 20 vis y un cristal de cada uno de los seis aspectos
   primarios. Escanea objetos para obtener las observaciones de la etapa.

Las mesas de port.7 siguen existiendo; abrir una mesa anterior desde su bloque
reconoce su fabricación al llegar a Primeros pasos. El cargador conserva su
alcance de nueve chunks. Las recetas arcanas ahora exigen su investigación,
también en creativo; tener ingredientes y vis no sustituye ese requisito.

## Conocimiento y etapas

`thaumcraft:knowledge` se guarda en los datos persistentes Forge del jugador.
Conserva etapas, descubrimientos, identidades escaneadas, pruebas de fabricación
y puntos brutos de observación/teoría por categoría. El formato tiene versión,
límites y validación; los datos corruptos o de versiones futuras no se convierten
en conocimiento vacío ni se sobrescriben. La copia al morir conserva el tag.

Se extraen las 148 entradas y 271 etapas del registro original. Se conservan
padres, requisitos de etapa `@N`, condiciones `&&`/`||`, objetos entregables,
fabricaciones, descubrimientos y costes. Las páginas finales sin requisitos se
completan al superar la etapa anterior. Se mantienen 16 puntos brutos por una
observación y 32 por una teoría. Escanear no otorga teorías.

El servidor comprueba todos los requisitos y reserva cantidades por casilla
antes de consumir objetos o puntos; una condición incumplida no causa gasto
parcial. Las peticiones del libro contienen solo la selección o la acción:
el cliente no envía etapas, recompensas ni cantidades. Las entradas de escaneo
no se pueden desbloquear pulsando Continuar. Las etapas con warp actualizan el
almacén existente, respetando `wussMode`; sus efectos de juego siguen en M12.

Los requisitos con objetos/NBT antiguos sin equivalencia comprobada se conservan
como `unavailable:` y bloquean la etapa. No se sustituyen por aire ni por otro
objeto gratuito. El generador normaliza la errata original
`required_knowledgeauram` a `required_knowledge`. El catálogo es un recurso
empaquetado compartido, no un registro de investigaciones recargable por datapack.

## Taumómetro y aspectos

El uso del taumómetro se resuelve en el servidor, con alcance de nueve bloques,
oclusión y comprobación de chunks cargados antes del trazado. La espera entre
usos es de un segundo. Escanea bloques, entidades, objetos del suelo y hasta
100 casillas del inventario accesible de un bloque. Agacharse permite escanear
el objeto de la mano secundaria. Las variantes de esencia se distinguen por
componente; repetir el mismo tipo, aunque cambien posición o cantidad, no vuelve
a conceder observaciones.

El catálogo inicial conserva 37 aspectos y sus combinaciones, las siete fórmulas
de categorías originales, 141 selectores de objetos (cuatro son tags) y 52 tipos
de entidad con asignación explícita. Se traducen familias estables y nombres
modernos comprobados. Descubrir un aspecto concede el punto original adicional
en Básicos, Alquimia y Aura. Las fórmulas por categoría conservan la raíz cuadrada
y el redondeo superior originales. Los disparadores explícitos incluyen minerales,
plantas, materiales y grietas. La deduplicación se guarda por jugador.

El taumómetro muestra base, vis y flux del chunk con los datos S2C de aura ya
existentes. Conserva el cuerpo y el panel del OBJ original mediante dos mallas
y sus texturas. Las partículas de escaneo usan proveedores nativos.

## Libro y recetas

El Thaumonomicon tiene categorías, entradas, etapas y páginas navegables. Muestra
requisitos, puntos y las recetas arcanas portadas, con sus patrones, ingredientes,
vis y cristales. Las recetas todavía no implementadas se identifican como tales.
La interfaz y el diario tienen textos en español e inglés; las descripciones
extensas originales usan el inglés conservado como fallback.

Las siete recetas de port.7 reciben sus claves originales. Se añaden dos:
taumómetro (`FIRSTSTEPS@2`) y dioptra (`DIOPTRA`). El campo `research` del codec
arcano es opcional para compatibilidad con recetas externas; las nueve recetas
incluidas tienen un requisito explícito. La mesa vuelve a comprobarlo al crear
la salida y no gasta ingredientes, cristales ni vis si está bloqueada. Las
fabricaciones vanilla y las transformaciones con Salis registran pruebas.

Para administración existe `/thaumcraft research grant <jugador> <entrada>`,
restringido al permiso de operador del resto de comandos Thaumcraft. Concede
solo una entrada válida y preserva los demás datos; el libro normal no lo usa.

## Límites del lote

Siguen pendientes la mesa/cartas de teorías, notas celestes, curiosidades,
herramientas de escritura, integración de pociones/encantamientos, inferencia
recursiva de aspectos desde recetas y puntos de extensión para otros mods.
Los apéndices del libro, arte y distribución espacial del navegador original,
algunos disparadores de estadísticas/eventos y las cadenas automáticas de
investigaciones avanzadas necesitan sus consumidores posteriores.

Las ramas que requieren crisol, infusión, focos, teorías o criaturas sin portar
siguen bloqueadas por esos requisitos originales. Catalogarlas no implementa
sus mecánicas. El contenido de este lote permite completar Primeros pasos y
conserva una base de progreso para las siguientes entregas.

## Verificación

```sh
./gradlew -PknowledgeProbe=write runServer
./gradlew -PknowledgeProbe=read runServer
./gradlew -PknowledgeProbe=client runClient
./gradlew clean build
```

El servidor usa el mundo aislado `knowledge-smoke`. Los fixtures de
`src/knowledgeTest` se excluyen del JAR normal. Las transacciones se prueban con
un jugador de prueba; un `ServerPlayer` real se guarda en el archivo de jugador
y se vuelve a cargar en otro proceso. También se comprueba el evento Clone.
La escena cliente no congela el tiempo y requiere escanear y pulsar el libro
mediante los paquetes nativos. Evidencias en `work/`, sin publicarlas como artefactos.

El catálogo de port.8 suma 90 bloques y 65 objetos sin bloque: 155 IDs de item,
191 entradas creativas al contar aspectos y 121 recetas. Se conservan las 17
regresiones previas y se añaden cinco pruebas de codec, puntos, requisitos y
fórmulas de aspectos.

Resultados del 18 de octubre de 2026: el cliente completó el escaneo real de
piedra (15 → 18 puntos), recibió la sincronización del menú y completó Primeros
pasos mediante el botón nativo (18 → 2). El servidor dedicado validó también
la entrega del diario mediante PlayerWakeUpEvent y todas las transacciones
anteriores (`KNOWLEDGE_RUNTIME_OK`). El generador es idempotente y los 775 JSON
modernos se validaron sintácticamente.
La recarga en otro proceso terminó con `KNOWLEDGE_RESTART_OK`: etapas, aspectos,
identidades, puntos y fabricaciones conservaron sus valores, igual que la copia
al morir. El servidor se cerró mediante `stop`.
La compilación limpia terminó correctamente: 22 pruebas, cero fallos. El JAR
normal contiene 121 recetas y excluye todos los fixtures de integración.
