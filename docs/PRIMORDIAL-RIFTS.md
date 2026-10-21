# Perla primordial y revisión de grietas — port.37

Minecraft 26.3 / Forge 66.0.13 / Java 25. No cambia identificadores ni añade generación retroactiva.

## Perla

`thaumcraft:primordial_pearl` deja de ser un material genérico. Tiene ocho estados, de desgaste 0 a 7; una utilización como reactivo devuelve una unidad con desgaste +1, y el último uso no devuelve resto. Desgaste 0–2 muestra la perla, 3–5 el nódulo y 6–7 la mota. Nombres, tres modelos/texturas originales y barra de usos se sincronizan con componentes nativos.

El desgaste se guarda en el primer número de `minecraft:custom_model_data`, separado de la durabilidad reparable. Así no admiten reparación por mesa, yunque, piedra de afilar, Mending ni Unbreaking. El evento original del yunque cancela también cualquier operación con una perla en cualquiera de las dos ranuras, incluido renombrarla. No son encantables y las nuevas pilas admiten una unidad. Los objetos anteriores sin componente conservan el mismo ID y se interpretan como perlas completas. No se recorren inventarios ni se borran pilas antiguas.

Se conserva `primordial_nugget` para partidas y recetas anteriores del port. Un fragmento se convierte en una mota de un uso. Se elimina la receta inversa: dividir una perla reutilizable en fragmentos permitiría generar múltiples objetos con todos sus aspectos. Las seis infusiones que antes pedían fragmentos admiten ahora fragmento o perla; con perla gastan un uso y devuelven el resto. Los fragmentos existentes siguen sirviendo directamente. Es una adaptación de compatibilidad del port, no una receta original de TC6.

Las perlas completas de jefes y bolsas se conectarán al portar esos sistemas de M16; las grietas entregan perlas gastadas, como el original.

Los restos dinámicos funcionan tanto en fabricación normal como en mesa arcana y en ingredientes de los pedestales de infusión: foco superior, amuleto del vidente del vacío y construcción alquímica avanzada. Cuando la perla es el objeto central del triturador primordial, se transforma por completo en el resultado, como en la matriz original; no se devuelve una segunda perla. Escanear cualquiera de los estados conserva el descubrimiento `PRIMPEARL`.

## Grietas: comparación con el código original

| Comportamiento | Resultado del port |
|---|---|
| Perla al colapsar | `floor(sqrt(tamaño al iniciar colapso))` posibilidades entre 100; una perla con desgaste aleatorio 4–7. Sustituye el antiguo lote de fragmentos. |
| Semillas del vacío | `floor(sqrt(tamaño al iniciar colapso))`, una sola entrega. |
| Colapso guardado | Conserva el tamaño inicial; volver a solicitar el colapso no reduce el botín. Entidades retiradas no vuelven a entregar objetos. |
| Reducción y aura | Una unidad de tamaño por tick, final al llegar a uno; cada paso devuelve una unidad de vis o flux. |
| Explosiones de colapso | Probabilidad 1/10 por paso y potencia aleatoria inferior a 0,5; adaptación moderna sin destrucción adicional de terreno. |
| Muy inestable (`<= -25`) | Enfermedad de flux, debilidad y distorsión normal/temporal. |
| Inestable (`-25 < s < 0`) | Debilidad y distorsión normal/temporal. |
| Estable (`0 <= s <= 50`) | Distorsión normal/temporal. |
| Muy estable (`> 50`) | Sin esos efectos finales. |
| Alcance de efectos | Conserva literalmente la fórmula original `1 - distancia² / 32`, con truncado entero. Aunque consulta 32 bloques, el alcance efectivo es inferior a 5,66 bloques. |
| Crecimiento y estabilidad | Mantiene consumo de flux, límite 100, umbral de estabilidad 50 y pérdida de 0,2 cada 120 ticks. |
| Sucesos | Mantiene los cinco sucesos y pesos 50:10:20:20:1 de port.36. |
| Sonido | Restaura `evilportal` original cada 300 ticks, con variación de volumen y tono. |

El modo `wussMode` evita los efectos finales perjudiciales y las pequeñas explosiones del colapso. Es una adaptación explícita para respetar la configuración actual del port. No completa los demás sucesos del sistema de distorsión.

El trazado y la malla sembrados existentes se conservan. Antes de trazar las colisiones se comprueba todo el rectángulo de chunks del segmento, también cuando cruza una esquina; no se cargan vecinos para resolverlo. Se mantiene la destrucción local y la absorción de objetos propias de una grieta. Durante el colapso no se permite volver a crecer. El renderizador usa el núcleo y las costuras de la API moderna; el shader de distorsión de pantalla de OpenGL 1.12 no se reproduce literalmente. Las partículas usan las primitivas modernas disponibles. Esto cierra la obtención/consumo de la perla y los efectos funcionales pendientes del colapso; no implica equivalencia gráfica exacta con 1.12.

## Verificación

Verificado el 21 de octubre de 2026:

- `clean build test --offline`: 160 tests, cero fallos, errores u omisiones.
- Servidor dedicado: restos de los ocho estados sin mutar vistas previas, rechazo de reparación, migración de fragmentos y ausencia de conversión inversa; receta arcana real de construcción alquímica avanzada pagando 200 vis y devolviendo la perla gastada.
- Infusión real de foco superior con pago completo de esencia y perla de desgaste 5 a 6; guardado con dos ingredientes consumidos y reanudación tras reiniciar sin gastar otro uso.
- Colapsos nativos con semillas aleatorias fijadas: perla de desgaste 4–7, diez semillas para tamaño inicial 100, rechazo de doble entrega y enfermedad/debilidad de colapso muy inestable.
- Reinicio: los ocho desgastes y nueve fragmentos antiguos conservados; grieta guardada con tamaño restante 50 y tamaño inicial 100 produce diez semillas. Condensador y semilla corrupta de entregas anteriores conservados.
- Cliente nativo con Vulkan: modelos de perla/nódulo/mota, seis infusiones compatibles con todos los desgastes, salida del triturador sin daño heredado de la perla, cancelación del yunque en ambas ranuras, descubrimiento por escaneo y distorsión normal/temporal de un jugador real con el valor esperado. Colapso temporizado de tamaño 80 entrega ocho semillas. Guardado, retorno al menú y cierre limpio.
- Generadores completos en dos pasadas, segunda sin cambios.
- ZIP sin duplicados ni fixtures; 309 recetas (se retira la conversión inversa explotable) y 282 definiciones de objetos. Etiquetas de herramientas anteriores preservadas.

La captura se compartió antes de generar el JAR y de pushear.
SHA-256 de la captura: `0fa99fe4e3f8a2c71a3f1f8e5bdaa688e6f0fb1ee9f64b72c3b7256cc4c76521`.

Entrega: `thaumcraft-6.1.BETA26-port.37-forge66.0.13.jar` (8478056 bytes).
SHA-256: `b07a60c700ea890a2d1580d70af63c17819bb3d8c50849e5d7f438f58d29a8c5`.
