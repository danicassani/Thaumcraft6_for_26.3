# Plan — port.16

Minecraft 26.3 / Forge 66.0.13 / Java 25. Añade el medio Plan al manipulador y al guantelete, sin nuevos IDs ni recetas: 230 IDs de objeto y 264 recetas en total.

## Crear y usar

Desbloquea FOCUSPLAN en el Taumonomicón: sus padres siguen siendo FOCUSEXCHANGE y FOCUSPROJECTILE; requiere una teoría de taumaturgia y descubrir fabrico. Selecciona Plan como medio en el manipulador. Su complejidad base es 4 y consume un cristal fabrico, además de los costes de los efectos elegidos.

El botón Setup / Más recorre ahora tres páginas: efectos, modificadores y Plan. La tercera ajusta los radios X/Y/Z entre 0 y 3 y alterna Volumen/Superficie. Los ajustes quedan en el programa del foco y se conservan durante la creación y al guardar. Adaptación explícita: se fijan al crear el foco en el manipulador; todavía no se redimensionan con G/CTRL+G sobre el guantelete.

Con el foco instalado, el guantelete apunta bloques hasta 16 bloques. Muestra cajas de selección mediante los gizmos nativos de Minecraft 26.3, sin cambiar el mundo desde el cliente. La selección representa la geometría; permisos, dureza, inventarios y materiales disponibles pueden impedir efectos concretos.

- **Volumen:** una caja de hasta 7 × 7 × 7. Se desplaza hacia dentro de la cara impactada para empezar en el bloque apuntado. Incluye bloques no vacíos aunque haya aire entre ellos; los procesa por distancia al impacto, con orden estable en los empates.
- **Superficie:** sigue bloques contiguos del mismo estado exacto que el impacto, con al menos una cara junto a un bloque no opaco, sobre el plano de la cara apuntada. No salta huecos ni materiales distintos. Usa X/Z como dimensiones del plano y no utiliza Y. En suelo/techo corresponden a X/Z del mundo; en caras X, X controla altura y Z anchura; en caras Z, X controla altura y Z anchura sobre el eje X del mundo.
- Los radios cero permiten una sola fila, columna o bloque. El volumen evalúa como máximo 343 posiciones y una superficie incluye como máximo 49 bloques. No carga chunks ausentes; también respeta altura de construcción y borde del mundo.

Plan puede aplicar los efectos de dos ramas mediante Split Target. No se combina con Split Trajectory, con otros medios ni con Scatter en esta entrega. El editor omite combinaciones incompatibles y el codec las rechaza al cargar datos. El límite global sigue siendo ocho efectos.

## Efectos, pagos y protecciones

La selección se calcula una sola vez antes de modificar bloques, por lo que romper o intercambiar el primer bloque no cambia el resto del lote. El servidor comprueba todas las investigaciones y cobra aura una sola vez por lanzamiento, sin multiplicarla por el número de bloques. Los pagos de creación siguen siendo atómicos y el trabajo queda bloqueado hasta completarse, como en port.15.

Cada efecto pasa por las protecciones existentes de `SpellEngine`: permiso de interacción, uso de objetos, dureza y eventos Forge. Rotura usa el controlador de supervivencia del jugador; no elimina bedrock ni bloques con entidad de bloque. Intercambio usa los bloques físicos de la mano secundaria y consume uno por colocación correcta. Una colocación denegada conserva el ingrediente, pero puede dejar retirado el bloque original, igual que en port.14/15. Los efectos que solo actúan sobre entidades no tienen efecto sobre esta selección de bloques.

Los programas de port.14/15 se cargan sin migración: el nuevo campo opcional toma radios 3/3/3 y modo Volumen, sin afectar sus medios ni costes. Las dimensiones se sincronizan por el menú y en el componente del foco.

## Pendiente

Spellbat y Rift se incorporan en [port.17](FOCUS-SPELLBAT-RIFT.md). Siguen pendientes grafos de mayor profundidad y otras opciones originales de trayectoria, fortuna y seda. Ajustes de Plan durante el uso del guantelete mediante teclas. M10 permanece parcial.

## Verificación

58 pruebas JUnit: las 53 anteriores y cinco pruebas de Plan para costes, codec y rechazo de combinaciones/dimensiones inválidas; cajas orientadas en las seis caras; superficies contiguas y ejes; filtrado por carga/exposición; radios cero y límites.

Servidor dedicado: creación pagada y bloqueada, guardado parcial, reinicio con dimensiones/modo exactos y finalización sin segundo pago (`work/plan-server-write2.log`, `work/plan-server-read.log`). Cliente integrado: lanzamiento real con cobro de aura único, denegación por investigación, volumen de 27 bloques con bedrock/inventario/bloque protegido preservados, superficies de estado idéntico contiguas y exposición mediante vidrio, intercambio con dos pagos reales y colocación denegada sin consumir ingrediente; ciclo de modificadores compatible y foco terminado/sincronizado (`work/plan-client.log`). Pantalla Plan inspeccionada a 854 × 480, con Y desactivado en modo Superficie.

Vista previa: selección de nueve bloques coincidente en cliente y servidor, con contornos nativos visibles sobre la pared de prueba. Registro `PLAN_CLIENT_PREVIEW_OK` e inspección visual completados.

Compilación limpia correcta (`work/plan-clean-build.log`): las 58 pruebas se ejecutaron sin fallos, errores ni omisiones. El JAR excluye `thaumcraft/test/` e incluye selector, ajustes y vista previa de producción. Entrega `thaumcraft-6.1.BETA26-port.16-forge66.0.13.jar`, 7 707 642 bytes; SHA-256 `ea9f5f55ccfe6483e00a0b87317cd8156c2d7cd714f7f40eec443a80ca0f730e`.
