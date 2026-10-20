# Rangos y experiencia — port.26

**Estado actual:** [cierre de golemancia en port.29](GOLEMS-COMPLETE.md). Este documento describe una entrega histórica; sus listas de pendientes quedan sustituidas por el estado actual.

Minecraft 26.3 / Forge 66.0.13 / Java 25. Progresión de gólems Inteligentes, separada del diseño de construcción. M11 sigue parcial.

## Progresión

Solo Inteligente permite ganar experiencia: cabezas Inteligente, Inteligente blindada y Explorador inteligente. La cabeza básica y Explorador con mente mecánica pueden trabajar, pero no ganan XP. El límite original es rango 10, comenzando en cero. El siguiente rango cuesta `(rango + 1)² × 1000` XP; el sobrante se conserva. Alcanzar rango 10 requiere 385 000 XP acumulados. El máximo no guarda XP adicional y las sumas grandes no desbordan.

Cada rango suma un punto de salud máxima y un 2,5 % de la velocidad base del diseño, tanto terrestre como voladora. Luchador recibe además 0,25 de daño de ataque por rango. Los golpes y dardos utilizan el atributo de ataque actualizado. El diseño, su coste, investigación, rasgos, capacidad, radio y navegación no cambian. Ganar XP no reinicia rutas; subir de rango no cura.

El original concedía un XP al iniciar tareas de combate. El port lo concede al causar daño efectivo, para evitar experiencia por intentos cancelados. El sistema de velocidad se aplica sobre las velocidades de diseño adaptadas del port, conservando exactamente el comportamiento de rango cero.

## Trabajo efectivo

Un XP por pila físicamente recogida, extraída o entregada; incluye Recoger, Vaciar, Llenar, Proporcionar y Abastecer. Una ruta de dos tramos concede uno en cada transferencia efectiva. Dos pilas transferidas pueden conceder dos XP, pero la cantidad de objetos dentro de cada pila no multiplica el premio.

Un XP por bloque realmente roto o cosechado y replantado, por tronco cortado o por acción nativa de Usar que se completa. Un ataque que reduce salud o absorción concede uno; una baja añade ocho. Vigilar y Carnicero lo aplican tanto a golpes como a dardos. El dardo revalida sus protecciones antes del daño y concede XP al gólem que lo lanzó.

No se concede XP al simular inventarios, esperar, adquirir una tarea, fallar, usar un inventario lleno, activar una pausa redstone, perder el propietario o cancelar una acción mediante Forge. Se mantiene la comprobación de propietario, canal, requisitos del sello, población, permisos y chunks cargados.

## Guardado e interfaz

`thaumcraft:golem_progress` es un componente de objeto nuevo con rango y XP. Las entidades guardan el compuesto `golem_progress` y sincronizan ambos valores al cliente. Los datos antiguos sin él cargan rango cero sin alterar el diseño ni los trabajos pagados. Valores numéricos fuera de límites se normalizan; un compuesto ilegible vuelve a cero. Cargar datos no concede rangos ni premios.

Recoger con la campana guarda rango y XP, canal, nombre y fracción de salud. La carga se suelta físicamente como antes. Desplegar restaura las bonificaciones antes de aplicar la fracción de salud, evitando curaciones y pérdida de progreso. La descripción del objeto muestra rango y XP; los comandos de campana muestran modo y progreso. Subir de rango emite sonido y partículas nativas. La explicación bilingüe está en Mente biotaumatúrgica.

No cambia identificadores existentes, recetas, costes de construcción ni generación de mundo. Cliente y servidor necesitan la misma versión para el nuevo componente de objeto.

## Verificación

Seis pruebas unitarias nuevas cubren umbrales, sobrantes, exclusividad Inteligente, máximo, desbordamientos, datos antiguos/fuera de límites, codec en los once rangos y bonificaciones de rango cero/máximo.

Las fixtures `rank-write/read/client`, excluidas del JAR publicado, verifican guardado, actualización, transferencias físicas, cosecha, Usar, tala, combate, cancelaciones y recogida/despliegue. Servidor dedicado: `work/rank-server-write1.log` y `work/rank-server-read1.log` finalizan correctamente. Guardan rango 9 con 99 991 XP, salud 7, nueve diamantes y canal 7; tras reiniciar, nueve XP alcanzan rango 10, salud máxima 26 y ataque 5,5 sin curar. Se rechaza XP en una mente mecánica y se verifica un snapshot sin progreso. Un dartero real guardado por port.25 conserva diseño, salud 7, nueve diamantes y rango cero.

Cliente: `work/rank-client2.log` registra Recoger/Llenar sin XP con mente mecánica; con Inteligente, una recogida desde 999 XP sube a rango 1 y la entrega deja exactamente un XP sobrante, con salud 7 intacta. Vaciar/Llenar, Proporcionar/Abastecer, cosecha y Usar entregan los premios exactos; tres troncos conceden tres XP. Pausa redstone, inactividad y rotura denegada no dan XP. Dos golpes y dos impactos físicos de dardo cancelados tampoco; una baja a golpes concede nueve XP y otra con dardo alcanza rango 10 desde 99 991 XP pendientes. El objeto recogido conserva nombre, rango, XP y fracción 4/26 de salud tras serializarse, y suelta exactamente once lingotes físicos. Desplegar conserva salud 4 y máximo 26; XP adicional no supera el límite. El cliente confirma rango 1/XP 1, rango 10 y el componente del objeto, incluida su línea real de descripción.

El cliente guarda el mundo, vuelve al menú principal y se cierra desde Salir del juego; finaliza correctamente sin watchdog ni terminación forzada.

Los catorce generadores se ejecutan dos veces con 1494 recursos idénticos.

Compilación limpia: 110 pruebas, cero fallos, errores u omisiones. JAR `thaumcraft-6.1.BETA26-port.26-forge66.0.13.jar` de 7978153 bytes; SHA-256 `fe1a978aca30d4ab9099ced3a66bbf31255ff85bf8a2b9cff3770d4177960699`. Se verifican los JSON, 295 recetas, 257 definiciones de objetos y la exclusión de todas las fixtures del artefacto.

## Pendiente

Opciones adicionales de área y coincidencia de los sellos, tareas de fabricación y modelos/animaciones originales. La progresión no completa toda la golemancia original.

port.27 incorpora [áreas orientadas de sellos](GOLEMS-AREAS.md).
