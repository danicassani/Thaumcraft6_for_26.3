# Estado revisado — port.77

Este documento sustituye como lista de pendientes a los planes históricos. La revisión de esta entrega contrasta configuración, consumidores modernos y fuentes originales, depura estados ya resueltos y verifica dos opciones gráficas en ejecución. **No certifica una equivalencia completa de cada función de TC6 ni una partida de supervivencia de principio a fin.**

## Pendientes funcionales confirmados

| Grupo | Diferencia comprobada | Evidencia |
| --- | --- | --- |
| Presentación de aspectos y HUD | `largeTagText`, `dialBottom` y `showTags` siguen declaradas sin consumidor moderno. | `ThaumcraftConfig.java`; inventario reproducible en `CONFIGURATION-AUDIT.json`. |
| Emotes de gólems | La opción de servidor `showGolemEmotes` no se consulta. Esto no significa que falte la golemancia funcional. | Mismo inventario de referencias; `GOLEMS-COMPLETE.md` documenta las mecánicas existentes. |
| Ajustes del escudo rúnico | `shieldRecharge`, `shieldWait` y `shieldCost` no se aplican. El escudo funciona con 40/80 ticks y 1 vis fijados en el código, equivalentes a los valores predeterminados originales. | `equipment/EquipmentContent.java`, frente a `common/lib/events/PlayerEvents.java` original. |
| Regeneración en terreno existente | `regenKey` y los siete controles `regenAura`, `regenStructure`, `regenCinnabar`, `regenAmber`, `regenQuartz`, `regenCrystals` y `regenTrees` no tienen consumidores. La generación en terreno nuevo sí está implementada; no hay que tratar estos interruptores como una retrogeneración operativa. | `CONFIGURATION-AUDIT.json`, frente a `common/lib/events/ChunkEvents.java` y `common/world/ThaumcraftWorldGenerator.java` originales. |
| Intercambio de estructuras de varias posiciones | Puertas, camas y plantas dobles se excluyen explícitamente. Los contenedores están implementados desde port.76. | `thaumaturgy/ExchangeSelection.java`; `FOCUS-CONTAINERS.md`. |

El análisis estático encuentra **42 opciones declaradas, 26 con referencias modernas y 16 sin referencias**. No son dieciséis mecánicas ausentes: ocho claves corresponden a regeneración, tres a ajustes de una mecánica existente, y `allowSpawnTaintacle` tampoco tiene consumidor en las fuentes originales revisadas. Esta última no se clasifica como función activa de TC6 perdida durante el port.

Ejecutar `python3 tools/audit_configuration_consumers.py` regenera las rutas y líneas de referencia. Se excluyen los archivos antiguos, las definiciones y las escenas de prueba. Encontrar una referencia tampoco demuestra por sí solo que toda la opción esté implementada correctamente: la semántica y las comprobaciones nativas deben revisarse aparte.

## Diferencias visuales y adaptaciones

- Spellbat utiliza `BatRenderer`; los campos de nubes y minas tienen `NoopRenderer` y se muestran mediante partículas. Falta contrastar/restaurar sus representaciones específicas originales; la lógica de estos medios ya existe.
- Flujos de esencia, accidentes de infusión y varios efectos utilizan primitivas modernas. No se afirma igualdad de píxeles con todos los shaders y partículas de 1.12.
- Los grafos de focos tienen límites explícitos de ocho niveles, ocho nodos, ocho efectos y 64 entregas. Son límites actuales documentados; esta revisión no demuestra que excluyan un foco legal bajo el presupuesto original, por lo que no se cuentan automáticamente como una mecánica faltante.
- Las placas de sellos, prensas compactas antiguas y otras decisiones de compatibilidad se documentan como adaptaciones en `GOLEMS-COMPLETE.md`, no como trabajo pendiente de nuevo.

## Pendientes históricos ya resueltos

| Elemento | Entrega / documento |
| --- | --- |
| Gólems y automatización de patrones | port.29, `GOLEMS-COMPLETE.md` |
| Infusión avanzada funcional | port.55, `INFUSION-COMPLETE.md` |
| Pechs y criaturas restantes del registro original | port.54–56, `PECHS.md`, `ELDRITCH-COMPLETE.md` |
| Perforadora arcana | port.64, `ARCANE-BORE.md` |
| Lienzo de focos y continuaciones de ocho niveles | port.65, `FOCUS-CANVAS.md` |
| Gancho y ballestas con representación original | port.66–67, `GRAPPLE-VISUALS.md`, `AUTOMATED-CROSSBOWS.md` |
| Thaumonomicon visual, recetas, progresión revisada y abrazaderas | port.68–70, `THAUMONOMICON-COMPLETION.md` |
| Transfusores | port.71, `ESSENTIA-TRANSFUSERS.md` |
| Estandartes con aspectos, profundidad de espejos y grietas | port.72, `BANNERS-VISUALS.md` |
| `allowCheatSheet` | port.73, `CHEAT-SHEET.md` |
| Rotura gradual, Intercambio y contenedores | port.74–76, `FOCUS-BREAKING.md`, `FOCUS-EXCHANGE.md`, `FOCUS-CONTAINERS.md` |
| `disableShaders` y `crooked` | port.77, `GRAPHICS-OPTIONS.md` |

Los biomas `eerie` y `eldritch` están registrados. El código original revisado no registra una dimensión jugable del Outer Lands ni sus generadores: añadirlos sería contenido nuevo. Tampoco se inventan recetas para variantes inactivas de infusión o proveedores de cartas que el original no conectaba. Véanse `ELDRITCH-COMPLETE.md`, `INFUSION-COMPLETE.md` e `INFUSION-CARDS.md`.

## Verificación global todavía necesaria

Falta completar una ruta de supervivencia ganada, desde el diario y los primeros descubrimientos hasta los capítulos avanzados, y ensayar actualizaciones de mundos representativos con máquinas trabajando, inventarios, gólems y chunks antiguos. Las fixtures con conocimiento preparado permiten comprobar mecánicas y presentación, pero no equivalen a esa partida.

También queda contrastar de forma exhaustiva el catálogo activo original, sus costes, obtención de recursos y comportamiento combinado. Este informe cierra errores concretos de la lista de pendientes; no convierte una revisión estática de configuración en esa auditoría global completa.

Orden recomendado: controles de aspectos/HUD y emotes; opciones de escudo; diseño y pruebas de retrogeneración; estructuras de varias posiciones; equivalencias visuales y recorrido completo de supervivencia. La retrogeneración requiere especial atención a conservar construcciones y a no ejecutar el mismo trabajo dos veces.
