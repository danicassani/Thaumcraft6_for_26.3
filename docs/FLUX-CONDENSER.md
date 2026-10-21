# Condensador de flux — port.34

Quinto lote de alquimia avanzada para Minecraft 26.3 / Forge 66.0.13 / Java 25. Añade condensador, celosías limpias/atascadas, transporte, mantenimiento y recetas originales. No cambia generación de mundo ni IDs o formatos anteriores.

## Montaje y recetas

Investiga `FLUXCLEANUP` con el frasco de diez Vitium y los costes originales de observación/teoría. Receta arcana del condensador: 500 vis, cinco cristales de aire/agua/entropía, cuatro placas de latón, dos tablones, resonador mórfico, mecanismo complejo y tubo. Cada celosía cuesta 100 vis, tres cristales de tierra y aire, seis cuarzos, dos placas de taumio y un filtro.

La primera celosía va directamente encima. Se ramifica por encima de la altura de la base y dentro de distancia al cuadrado 74. Limpias y atascadas cuentan para el coste. No compartas una red entre condensadores: se desactiva el conjunto afectado.

Conserva el recorrido ordenado original de conexiones y ramas atascadas. Cada nodo eficaz contribuye `1 − 0,15 × conexiones`, con eficiencia total limitada a 40. Intervalo: `max(5, round(600 − 15 × eficiencia))` ticks. Coste: `int(4 + sqrt(número de celosías))`. Una celosía limpia da eficiencia 0,85, intervalo 587 y coste 5; dos verticales dan 1,7/575/5 y tres 2,4/564/5. Las ramas detrás de una celosía atascada pierden eficiencia; una raíz atascada detiene toda la máquina.

El recorrido iterativo evita desbordar la pila. Nunca fuerza chunks: ante un borde desconocido se pausa. Revisa la estructura cada segundo y antes de pagar una conversión. Las conexiones visuales se actualizan con las vecinas; las celosías sin soporte se desprenden, conservando si estaban atascadas. La comprobación de soporte tiene un límite de 4096 celdas y espera ante redes desconocidas o excepcionalmente grandes.

## Transporte y pago

Entrada por los cuatro lados, succión 128, hasta cien unidades de combustible. Acepta todos los aspectos conocidos salvo Vitium. Cada cinco ticks puede extraer una unidad por lado desde transportes compatibles. La simulación no modifica depósitos.

Un ciclo completo consume su coste de esencia y exactamente una unidad de flux ambiental; entrega una unidad de Vitium en un depósito separado de cien. Solo exporta por abajo, directamente a tarros o tubos. No convierte fracciones de flux, pausa la conversión con salida llena (puede seguir rellenando combustible hasta cien) y no vuelve a pagar una operación terminada.

Redstone pausa entrada y progreso, pero permite retirar salida. Si falta combustible o flux, o se bloquea la salida, conserva el progreso. Adaptación de guardado: el reloj avanza durante trabajo elegible y persiste; cambiar el intervalo conserva el tiempo acumulado hasta un tick antes del nuevo final. No usa el contador global volátil del original.

## Mantenimiento y guardado

Una de cada cincuenta conversiones puede atascar una celosía limpia, con la selección aleatoria original. Ofrecer Vitium desde un lateral atasca una celosía sin consumir ese Vitium; no lo transforma en combustible.

Usa un filtro sobre una celosía atascada para limpiarla. Paga uno en supervivencia, respeta protección y tiene un 50 % de devolver un cristal de Vitium. Creativo conserva el filtro. La mano vacía sobre la base muestra depósitos, coste, progreso, intervalo y estado de la red; el resonador puede consultar la salida inferior.

Formato versión 1: combustible, Vitium, progreso exacto y contador de conversiones. Los formatos futuros o inválidos se conservan sin reinterpretarlos y desactivan la máquina. Al romper la base se libera como flux el contenido de ambos depósitos; no se duplican como objetos. Los modelos y texturas conectados son los originales.

## Verificación

Trece tests nuevos cubren pago, pausa, límites, guardado, datos incompatibles, cambios de intervalo, límites geométricos, redes compartidas, chunks desconocidos y equivalencia del recorrido con el original en cien redes ramificadas. Una red grande alcanza la eficiencia máxima sin recursión. Comprueba también que todas las variantes resuelven sus modelos y texturas: evita referencias con la antigua ruta de modelos sin `block/`.

Fixtures separadas `-PalchemyProbe=condenser-write/read/client`, excluidas del JAR normal: investigación pagada, depósitos y simulación, consumo ambiental exacto, mantenimiento protegido, Vitium contaminante, reinicio y transporte real de alambiques a tarros. Cliente nativo con una instalación activa y otra atascada/pausada.

### Resultados nativos

- Servidor dedicado: investigación con frasco y puntos originales; límites de cien unidades, simulación sin cambios, succión y caras correctas, pago exacto de cinco esencias y un flux, rechazo de fracciones y pausa con salida llena/redstone.
- Limpieza mediante interacción real con filtro: un evento de protección impide el cambio y el gasto; una limpieza permitida paga un filtro y recupera la celosía. Un alambique con Vitium atasca la raíz sin perder sus cinco unidades ni alimentar el depósito. Romper una base con siete combustibles y tres Vitium devuelve exactamente diez flux al aura.
- En ticks normales del mundo: un alambique entrega cinco esencias, el condensador completa 587 ticks de trabajo y un tarro inferior recoge exactamente un Vitium. En cliente se comprueba también la caída de una celosía sin soporte.
- Reinicio dedicado: recuperación exacta de 67 combustibles, 39 Vitium, 311 ticks y 81 conversiones. Tras 276 ticks adicionales paga una operación: 62/40/0/82, con exactamente un flux ambiental retirado. Conserva una segunda máquina con datos futuros y carga el Thaumatorium real de port.33.
- Cliente: escaneo mediante rayo de una grieta real para obtener `f_toomuchflux`; avance de investigación sin conceder esa señal manualmente; repetición de las pruebas y fabricación real. Red activa de siete celosías y segunda red con raíz atascada y redstone. El menú nativo del tarro mostró trece Vitium generados durante la prueba.
- Modelos comprobados tras recarga nativa de recursos. Las referencias finales de ambas celosías llevan `thaumcraft:block/`; sus hashes se cotejan con los recursos del JAR. Test de regresión para resolver todas las variantes, modelos y texturas.
- Capturas reales 1280×720 compartidas antes de generar el JAR y pushear. Instalación corregida: `6434e0ab16ff3649e64bbf0c7dd9a10670ca61d6f9b3aeaaed9770312c898552`. Tarro: `dbc7b2392ef4881cf11185765ccb30ac659bf07c96ed842f3f5974fdcfbc1a19`.

Cadena de 22 generadores ejecutada dos veces tras la corrección visual; segunda pasada sin cambios. Compilación limpia: **147 tests, 0 fallos, 0 errores y 0 omitidos**. Los tres recorridos nativos terminan con `BUILD SUCCESSFUL`, sin excepciones de prueba ni crashes; guardado y cierre normales del cliente, sin captura del título.

JAR normal sin fixtures, con 307 recetas y 270 definiciones de objeto; JSON válidos, modelos finales verificados y clases/recursos/etiquetas anteriores conservados. Artefacto `thaumcraft-6.1.BETA26-port.34-forge66.0.13.jar` (8203657 bytes), SHA-256 `4d4993af1b9615b43304ab7797b0df0ff99cbe5ad2a021de6eb42b45a1e05a5c`. Generado después de compartir las capturas corregidas.
