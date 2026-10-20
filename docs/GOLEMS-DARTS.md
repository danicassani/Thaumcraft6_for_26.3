# Brazos lanzadardos — port.25

**Estado actual:** [cierre de golemancia en port.29](GOLEMS-COMPLETE.md). Este documento describe una entrega histórica; sus listas de pendientes quedan sustituidas por el estado actual.

Minecraft 26.3 / Forge 66.0.13 / Java 25. Añade la quinta selección de brazos a la prensa y un proyectil físico `thaumcraft:golem_dart`. Los índices de brazos 0–3 y todos los identificadores anteriores se conservan. M11 sigue parcial.

## Fabricación

Lanzadardos ocupa el índice 4 y exige GOLEMCOMBATADV. Sus materiales adicionales son un módulo de agresión, dos dispensadores, 32 flechas y un mecanismo simple, como el original. La mejora Luchador paga un segundo módulo propio. Concede Luchador, Torpe, A distancia y Frágil; la mejora de blindaje puede cancelar Frágil. No concede Destreza ni Inteligente. Las cabezas y patas existentes siguen combinándose con los brazos nuevos.

Las 32 flechas se pagan al construir los brazos. Los disparos posteriores no consumen munición ni ocupan la carga, siguiendo el comportamiento original. Los dardos no se pueden recoger y desaparecen al chocar con bloques, tras impactar en una víctima o a los 200 ticks. No crean flechas recuperables. La prensa mantiene la reserva atómica y el trabajo bloqueado y persistente; Machina conserva su cálculo habitual.

## Combate

Vigilar y Vigilar avanzado admiten combate a distancia con estos brazos. Carnicero los utiliza si el diseño cumple además Inteligente. La selección sigue limitada al área del sello, hogar, propietario y canal. Los otros brazos conservan su combate cuerpo a cuerpo.

El gólem se aproxima si está a más de 16 bloques o pierde la visión; se detiene para apuntar durante 20 ticks de visión válida y dispara cada 20–25 ticks según la distancia. Los brazos lanzadardos no usan un golpe cuerpo a cuerpo como alternativa. El daño base sigue ataque/3 + fracción de distancia + variación gaussiana, aplicado por una flecha nativa a velocidad 1,6 e imprecisión 3. El daño efectivo depende de velocidad, armadura, inmunidad y eventos Forge.

Cada dardo conserva dueño, gólem, víctima concreta, sello, revisión, canal y caducidad. Solo puede alcanzar a esa víctima. Revalida al impactar: el dueño debe seguir conectado, vivo y en la dimensión; el gólem debe estar vivo, en Trabajar, en su canal y con brazos a distancia; el sello debe estar cargado, activo y sin cambios, dentro del hogar y con requisitos válidos. Se comprueban otra vez aliados, mascotas, creatividad, PvP, área del sello y población reproductora de Carnicero. Cambiar el sello o detener/recoger el gólem invalida sus dardos en vuelo. El daño pasa por los eventos nativos de Forge.

No carga chunks para buscar dueño, gólem, sello o destinos. Comprueba los chunks del segmento de movimiento antes de avanzar. Una pared detiene el proyectil. La gravedad y las colisiones son nativas; no se aplica daño instantáneo a través de bloques.

## Guardado y presentación

El codec amplía únicamente brazos de 0–3 a 0–4; los diseños y pagos antiguos conservan sus costes. Las entidades guardan el próximo instante de acción, con límite defensivo al cargar; el apuntado comienza de nuevo al reconstruir la tarea. Los proyectiles guardan sus vínculos y caducidad; un vínculo inválido o dueño ausente hace que se descarten. Campana conserva diseño, nombre, salud y carga física como antes. No cambia la generación de mundo.

El cliente recibe el tipo de brazos sincronizado y muestra lanzadores adaptados sobre los brazos del modelo nativo. El proyectil usa el renderizador nativo de flecha. Los modelos y animaciones OBJ originales siguen pendientes.

## Verificación

Pruebas de datos: 480 combinaciones nuevas, codec anterior y límites, componentes compartidos, rasgos/requisitos, cadencia y daño. Fixtures `dart-write/read/client`, excluidas del JAR normal, comprueban fabricación, persistencia y combate nativo.

Servidor dedicado: `work/dart-server-write2.log` y `work/dart-server-read1.log` verifican componentes, investigación sin pago parcial, bloqueo, progreso de trabajos nuevos y antiguos y sus resultados. El dartero conserva brazos, salud 7 y nueve diamantes. Los vínculos y caducidad del dardo sobreviven al NBT, se fuerza Recogida desactivada y los dardos de un dueño ausente/gólem detenido se descartan tras reiniciar. Un Explorador inteligente real de port.24 conserva brazos antiguos, radio 48, salud 5 y 21 esmeraldas.

Los trece generadores se ejecutan dos veces con 1494 recursos idénticos.

Cliente: `work/dart-client2.log` registra dos impactos físicos cancelados por LivingAttackEvent, sin daño al dueño/mascota, sin golpe cuerpo a cuerpo y con intervalos 20–25. Comprueba NBT de un dardo activo, rechaza recogida incluso al alterar el tag, invalida disparos por edición/canal/redstone/Esperar y revalida PvP/creatividad. Una pared detiene un dardo y otro restaurado en vuelo causa daño real a distancia. Carnicero reduce cuatro adultos a dos y conserva la cría, también después de esperar 100 ticks por disparos en vuelo. La campana conserva brazos y salud 4/16 y suelta exactamente 11 lingotes; no hay flechas recuperables. Cliente recibe brazos y entidad del proyectil. Los clics nativos recorren los cinco brazos y pagan/bloquean Lanzadardos; pantalla inspeccionada a 854 × 480.

El cliente vuelve al menú principal tras guardar el mundo y se cierra desde Salir del juego, sin watchdog ni terminación forzada. Servidor, reinicio y cliente finalizan correctamente.

Compilación limpia: 104 pruebas, cero fallos, errores u omisiones. JAR `thaumcraft-6.1.BETA26-port.25-forge66.0.13.jar` de 7973613 bytes; SHA-256 `c4024d9e9d30445835c043848bfef6e3d455b244280065031399e881bdd93e4a`. Se verifican los recursos JSON, 295 recetas, 257 definiciones de objetos y la exclusión de fixtures del artefacto.

## Pendiente

Rangos y experiencia de gólems, opciones originales adicionales de área/coincidencia, tareas de fabricación y modelos/animaciones originales. Implementar los cinco tipos de brazo no completa toda la golemancia original.

port.26 incorpora [rangos y experiencia](GOLEMS-RANKS.md).
