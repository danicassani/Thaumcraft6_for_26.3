# Sucesos de grieta y wisps — port.36

Minecraft 26.3 / Forge 66.0.13 / Java 25. Se mantienen los identificadores de las partidas anteriores.

## Mecánica

Los cinco sucesos de corrupción de las grietas están conectados: 50 posibilidades de wisp, 10 de semilla primordial, 20 de agotamiento infeccioso, 20 de nube y una de colapso, sobre 101. Se mantiene el disparador existente cada 600 ticks con estabilidad negativa. Una aparición de wisp sólo paga +5 de estabilidad si tiene éxito; agotamiento paga +10 si afecta a alguien. La nube no altera la estabilidad, como el comportamiento ejecutado por el original.

Los wisps tienen 22 de vida, vuelo sin gravedad, 5 XP, aspectos y sonidos originales. Eligen aspecto primario el 90 % de las veces; las grietas fuerzan Vitium el 20 %. Respetan luz, dificultad pacífica, configuración de aparición, modo pacífico del mod, colisión, agua y un máximo local de ocho. No fuerzan chunks. Represalia y agresión ocasional cargan un ataque de 20 ticks, con pausa variable posterior, alcance de 16 bloques y visión directa. Contra objetivos quietos: probabilidad 66 % y cuatro de daño; contra objetivos en movimiento: 40 % y tres. Sueltan un cristal de su aspecto. Escanearlos descubre `!Wisp` y otorga sus aspectos originales: Auram 5, Volatus 5 y el aspecto propio 5, sumando si coinciden.

Las nubes ambientales tienen radio 1–3 y duración 5–30 segundos, acotada al ajuste admitido por la nube original. Infligen dos de daño de flux, ignoran armadura y escudo, respetan paredes y objetivos creativos/espectadores y no modifican terreno. Comparten la inmunidad persistente de dos segundos de todas las nubes de focos de la dimensión. No necesitan un lanzador conectado. Su plazo absoluto se guarda; descargar y recargar el chunk no renueva su vida. Consultan como máximo 64 objetivos por pulso y las grietas limitan las nubes locales a 16.

El cliente usa los atlas animados originales para los wisps y las nubes, con color por aspecto, resplandor y descarga sincronizada. La caja de culling de la nube incluye su radio.

## Alcance

No se añaden biomas, generación retroactiva ni tablas de aparición natural. Siguen pendientes la perla primordial y la equivalencia completa de grietas, además del resto de criaturas y generación de mundo de M16. Este paso completa los dos sucesos reservados de port.35; no declara terminado todo M13.

## Verificación

Verificado el 21 de octubre de 2026:

- `clean build test --offline`: 157 tests, cero fallos, errores u omisiones.
- Servidor dedicado: vida y aspectos de wisps, daño estacionario/móvil, visión, cristales al morir, configuración, límite de ocho, rechazo de coordenadas inválidas y chunks descargados; estabilidad pagada sólo tras una aparición real.
- Nubes: dos de daño, paredes, inmunidad compartida al solapar y expiración durante ticks reales del servidor.
- Reinicio sobre la misma partida: UUID y aspecto del wisp, UUID/radio/plazo exacto de la nube; semilla corrupta y condensador anteriores conservados.
- Cliente nativo con Vulkan: cuatro aspectos visibles, atacante activo y nube ambiental sincronizados; suceso real de nube y límite local de 16, inmunidad de vuelo a caídas/ahogamiento; guardado, vuelta al menú y cierre limpio.
- Generadores completos en dos pasadas: segunda pasada sin cambios.
- JAR sin entradas duplicadas ni fixtures; 310 recetas y 282 definiciones de objetos, etiquetas de herramientas anteriores preservadas.

La captura dentro del mundo se compartió antes de generar el JAR y de pushear.
SHA-256 de la captura: `df25a48e2460852c8bb974777547382f5f3d78e0b3108643f48c66a43a4d191e`.

Entrega: `thaumcraft-6.1.BETA26-port.36-forge66.0.13.jar` (8411952 bytes).
SHA-256: `64fca9f39362f1aee29285659451ba3473466a59a48fe5d5afe892ea040040d9`.

Actualización port.37: la perla primordial y los efectos finales de colapso quedan implementados; la comparación funcional y las adaptaciones gráficas se detallan en [PRIMORDIAL-RIFTS.md](PRIMORDIAL-RIFTS.md).
