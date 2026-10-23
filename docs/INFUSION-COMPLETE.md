# Infusión completa: port.55

Minecraft 26.3 / Forge 66.0.13 / Java 25. Cierra la infusión funcional activa de TC6: los tres altares, mejoras, estabilización, recetas dinámicas y los ocho encantamientos que registra el original. Conserva las recetas, investigaciones, costes, herramientas y accesorios de las entregas anteriores.

## Ritual y recuperación

El altar se monta con Salis Mundus y se inicia con el guantelete. Se comprueban estructura, investigación, objeto central y asignación exacta de ofrendas. Los costes y el resultado quedan fijados al empezar. La esencia se consume de tarros o fuentes remotas ya cargadas; faltas de esencia u ofrendas detienen el progreso. Cambiar el objeto central cancela sin producir resultado. No se reembolsa esencia ya consumida.

La absorción de cada ofrenda dura cinco ciclos de animación y se consume en el siguiente, como el contador original. Ese avance se guarda y sincroniza. La ofrenda puede recuperarse o trasladarse a otro pedestal válido: se busca una coincidencia exacta de objeto y componentes sin consumir más de una. Los restos de fabricación se conservan. La salida aparece una sola vez.

Se conserva el formato de guardado versión 1: `offering_ticks` es opcional y los guardados antiguos lo interpretan como cero. Los datos de versiones desconocidas o inválidos se preservan sin activar trabajos. No se cargan chunks a la fuerza. Se conserva el contexto de registros al leer y escribir objetos, para que los encantamientos vanilla no invaliden el contenido del pedestal ni el trabajo guardado.

## Estabilidad y accidentes

Se conservan los coeficientes de los altares, cuatro posiciones de mejoras, mínimo del 50% de coste, simetría y rendimiento decreciente. Las parejas de cráneos modernos comparten la identidad de la antigua familia de cráneos. Una pareja incompleta tiene la penalización mínima original de 0,1, también para incrustaciones y pedestales arcanos.

La selección conserva los 24 resultados ponderados del original:

| Resultado | Peso / 24 |
| --- | ---: |
| Expulsión de ingrediente | 4 |
| Warp a un jugador cercano | 3 |
| Descarga a uno / todos | 3 / 2 |
| Expulsión con goo (dos entradas originales) | 2 + 1 |
| Expulsión con flux | 2 |
| Destrucción de ingrediente con flux | 1 |
| Enfermedad a uno / todos | 2 / 1 |
| Expulsión con explosión | 2 |
| Explosión en la matriz | 1 |

Las descargas infligen 4–7 de daño mágico. Las enfermedades alternan corrupción de flux (120 ticks) y agotamiento de vis (2.400 ticks); el agotamiento ya usa la protección frente a leche de M13. El warp elige un jugador vivo cercano: 25% de un punto normal, o 75% de 2–5 temporales. Presenciar un accidente descubre `!INSTABILITY`.

Los seis accidentes de ingredientes pueden impedirse pagando 5–10 unidades de un estabilizador conectado. No se consumen ni expulsan ofrendas cuando la mitigación tiene éxito. Las explosiones dañan entidades y conservan el terreno, como el parámetro original que desactiva destrucción de bloques. La creación de goo no sobrescribe block entities. El modo wuss suprime daño, enfermedades, explosiones y warp; siguen existiendo los costes y los accidentes de materiales.

## Encantamientos y mejoras

Los ocho encantamientos activos conservan rangos, compatibilidad, coste `rango siguiente + 0,33 × otros tipos` y componentes del objeto. Se añade la posibilidad original de adquirir warp: una tirada de diez con probabilidad igual al número de tipos previos. Se decide una sola vez al confirmar el trabajo; consultar recetas no tira dados. El componente `thaumcraft:infusion_warp` se guarda, sincroniza, muestra en el tooltip y suma al warp del equipo utilizado. Se limita a 127 para evitar desbordamientos del byte antiguo.

Las nueve mejoras rúnicas mantienen sus costes y límite, y las gafas y tres máscaras de fortaleza conservan las otras propiedades del casco. La aplicación del warp propia de los ocho encantamientos no se extiende a estas recetas.

## Recursos y alcance

Se recuperan los sonidos originales de inicio, fallo y finalización. Los flujos de esencia y accidentes usan partículas nativas; se conservan los modelos de matriz, pedestales, incrustaciones y estabilizadores. Las adaptaciones de los ocho efectos de herramientas están documentadas en [INFUSION-ADVANCED.md](INFUSION-ADVANCED.md).

`VISBATTERY`, `VISCHARGE`, `SWIFT`, `AGILE` e `INFESTED` figuran en el enum antiguo pero no tienen recetas de infusión registradas en TC6; no se inventan recetas para ellos. La rama de XP de la matriz tampoco se activa por ninguna receta registrada. La obtención de recursos del contenido eldritch aún pendiente de M16 sigue siendo una dependencia de mundo, no una mecánica de altar nueva.

## Verificación

La fixture optativa `-PinfusionProbe=finish-write` comprueba las reglas antiguas, los tres altares, simetría, nueve rangos rúnicos, cuatro mejoras de fortaleza, recuperación de ofrendas y cada resultado de los 24 accidentes. `finish-read` arranca en otro proceso con el mundo `infusion-smoke55` y debe recuperar un trabajo con toda la esencia pagada y una ofrenda parcialmente absorbida. Las fixtures quedan fuera del JAR.

El cliente `finish-client` monta una mejora real a Sondeo IV conservando Recolector, nombre y componentes, con cuatro piedras de coste y 500 unidades de esencia disponibles. El inicio requiere el clic nativo del guantelete; se comprueban avance sincronizado, consumo exacto y resultado final. Los marcadores de éxito deben aparecer sin `AssertionError`; un código de salida de Gradle por sí solo no acredita las pruebas nativas.

Resultados de port.55: servidor dedicado y reinicio en procesos separados superados. El trabajo guardado tenía 25 unidades ya pagadas y tres ciclos de absorción; al recuperar completó una sola salida sin repetir el cobro. El cliente completó Sondeo IV junto con Recolector, conservó el nombre y consumió exactamente 397 unidades (cuatro piedras de coste, intervalo de siete ticks). La comprobación visual usa el mismo ritual y un clérigo que lanza su orbe mediante su IA real; el escenario de inspección solo pertenece a las fixtures optativas.

Validación final: `clean build test` completado con 199 tests, cero fallos, errores u omitidos. Recursos: 638 referencias de modelos y 206 tablas de botín válidas; la segunda ejecución del cliente repitió el resultado y el consumo exacto. El JAR excluye las fixtures y conserva las 336 recetas y los 326 recursos de objetos de port.54. Entrega: `thaumcraft-6.1.BETA26-port.55-forge66.0.13.jar`, 9.229.411 bytes; SHA-256 `e09c5ee2fb96f6fe0e868f1a72be2d79e6122ea8fb51e0ec104f0ac1c01e359a`.
