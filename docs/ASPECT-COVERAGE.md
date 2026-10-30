# Estudio y cobertura de aspectos — port.79

## Comportamiento

El taumómetro permite registrar cualquier pila no vacía del mundo aunque aún no tenga composición calculable. La vista previa muestra «Sin estudiar» antes del uso y «Estudiado» después; cuando hay composición muestra sus iconos y cantidades por unidad. Los objetos sin aspectos no dan puntos de observación ficticios. Un segundo escaneo no cambia el conocimiento. Aire y pilas vacías no se registran. No se amplía el escaneo de criaturas sin aspectos/disparadores.

La decisión anterior descartaba objetos que no tenían aspectos ni disparadores. Por eso un hueco del catálogo se presentaba como un fallo del instrumento. La vista previa y la escritura del servidor ahora admiten el mismo objeto físico sin depender de esa cobertura.

## Composición

Se conserva el catálogo existente. El suplemento restaura asignaciones perdidas, entre ellas `thaumcraft:amber` (Vinculum 10, Vitreus 10), mena de ámbar y cinabrio, según `ConfigAspects.java` original. También añade materiales modernos: amatista, cobre en bruto, netherita, bambú, sculk, plantas, resina, miel, componentes marinos, cerámica y plantillas, entre otros. Sus cantidades son adaptaciones del port; esos objetos no tenían valores originales en Minecraft 1.12.

Las equivalencias explícitas cubren oxidación, encerado, colores y transformaciones en el mundo. Se resuelven a partir de materiales concretos, con protección contra ciclos. El catálogo original tiene prioridad. Algunas transformaciones de Thaumcraft hechas con salis se basan en su soporte original más los aspectos añadidos.

El cálculo de recetas incorpora cortapiedras, transformaciones de herrería e infusión con salida fija, además de fabricación, mesa arcana, cocción y alquimia. Para infusión se añaden la raíz cuadrada del coste de esencia por salida y los ingredientes con la reducción original de fabricación. Se respetan las variantes de cristales de sus ingredientes. Las mejoras con salida dinámica no se interpretan como recetas de un objeto fijo. Se conservan los límites de profundidad, presupuesto y redondeo existentes.

No se asigna un aspecto genérico a todo. Algunos resultados de coste bajo redondean a cero; hay objetos creativos/técnicos, variantes dinámicas y composiciones todavía pendientes. Su registro de estudio funciona igualmente, sin inventar valores ni puntos. Esto no certifica el balance o la composición exacta de cada objeto moderno.

## Auditoría y pruebas

- `ASPECT-COVERAGE-BASELINE.json`: port.78, 2.012 tipos registrados de Minecraft/Thaumcraft, 981 con aspectos y 1.031 sin composición calculable.
- `ASPECT-COVERAGE.json`: port.79, 1.873 con aspectos y 139 sin composición calculable. Son tipos con pila predeterminada; no todas las combinaciones de componentes posibles.
- `tools/generate_aspect_supplement.py` reproduce el suplemento a partir de familias explícitas y el inventario inicial. No modifica el catálogo original ni concede estudio por sí solo.
- `-PbookProbe=aspect-coverage test runServer` comprueba que los 2.012 tipos pueden previsualizarse sin revelar aspectos, registrarse y repetirse sin premios duplicados. Las composiciones vacías no dan puntos. Comprueba el ámbar, representantes modernos, equivalencias del registro y todas las variantes de cristales/viales. Tres pruebas unitarias validan aspectos, cantidades, preservación de asignaciones y ausencia de ciclos; total de la suite: 249.
- `-PbookProbe=aspect-client runClient` prepara pilas tiradas realmente por el jugador: ámbar, amatista, cobre en bruto y un huevo creativo sin composición. Las entradas reales del cliente deben registrar cada pila, conservar sus siete unidades y el objeto de la mano opuesta, y recibir los iconos/cantidades o el estado estudiado vacío. Se exige regreso al menú antes de cerrar.

Primeros pasos explica que algunos objetos pueden quedar registrados sin revelar aspectos. El formato de conocimiento guardado y los identificadores existentes no cambian. Las fixtures se excluyen del JAR normal.

Resultado del cliente: las cuatro pilas quedaron estudiadas mediante uso real y se verificaron los iconos y cantidades recibidos, incluido el estado conocido vacío sin puntos. Las capturas del ámbar y la amatista se revisaron antes del JAR final.
