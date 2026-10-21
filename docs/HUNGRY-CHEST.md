# Cofre hambriento — port.41

Completa el inventario pendiente de M03. Registra `thaumcraft:hungry_chest`, sin sustituir IDs anteriores ni modificar generación del mundo.

El cofre conserva las 27 ranuras originales. Recoge pilas físicas que tocan su volumen, incluida la superficie de la tapa; no aspira desde lejos. Une únicamente objetos con componentes compatibles. Si no cabe toda la pila, conserva el resto en la entidad del suelo; si está lleno no retira nada. Procesa hasta 64 entidades por tick para acotar el trabajo, dejando las demás para ticks posteriores.

Los cofres vecinos son independientes y no forman un cofre doble. Mantiene contenedor, inventario, nombre, bloqueo, comparación redstone y acceso de tolvas/capacidad Forge nativos. La tapa y los sonidos usan la animación de cofre moderna con la textura original. Puede abrirse con un bloque encima, como el original. Los gólems pueden usar su inventario mediante la interfaz existente.

La receta arcana original de HUNGRYCHEST usa siete tablones de gran árbol y una trampilla de madera, 15 vis, un cristal de tierra y uno de agua. Está incluida en la guía de recetas del Taumonomicón.

Adaptaciones visuales: el modelo de cofre nativo moderno anima la tapa; el objeto usa una representación estática de las mismas piezas y textura. No se adapta el cofre a contenedores dobles ni se añade una zona de aspiración.

## Verificación

El servidor nativo comprueba las 27 ranuras independientes, recogida conservando nombre/componentes, rechazo de entidades retiradas y objetos alejados, extracción simulada y real por capacidad Forge, restos parciales al llenarse, comparador completo y entrega exacta del contenido al romperlo. Una segunda JVM carga el chunk y verifica inventario y nombre; también valida la receta registrada y sus costes de vis/cristales.

Los generadores pasan dos veces y la segunda no modifica archivos. Se corrige el requisito de investigación de OreDictionary a un cofre de Minecraft para que pueda completarse la investigación original.

El cliente abre el menú mediante un clic real y confirma doce diamantes recogidos del suelo en las 27 ranuras. Se comprueban el modelo y la textura dentro del mundo; ambas capturas se toman antes de generar el JAR final y antes del push.

Cliente guardado y cerrado normalmente. Compilación limpia: **177 pruebas**, sin fallos, errores ni omitidas. El JAR contiene 321 recetas y 302 definiciones de objetos, conserva la textura original y el requisito de cofre corregido, y excluye las fixtures de pruebas.

Artefacto: `thaumcraft-6.1.BETA26-port.41-forge66.0.13.jar` (8704471 bytes). SHA-256: `ff4443fa0ba54bedf86491b2381a26d5c57f5d834d9bab2a8770031fb3d2f2b7`.
