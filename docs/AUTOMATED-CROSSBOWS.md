# Ballestas automáticas — port.67

Minecraft 26.3 / Forge 66.0.13 / Java 25.

La básica y la avanzada vuelven a ser criaturas constructo persistentes, con salud, armadura, empujes reducidos, regeneración y posibilidad de montar en vagoneta. Mantienen los objetos y recetas `turret_basic` y `turret_advanced`. Los bloques y su entidad de bloque siguen registrados para leer partidas antiguas: al cargar se sustituyen por una ballesta del mismo tipo, conservando propietario, flechas y categorías equivalentes. La sustitución solo retira el bloque después de aceptar la entidad y vacía la munición anterior antes de retirarlo, evitando una segunda caída de objetos.

Se recuperan las 20 piezas, UV y pivotes de `ModelCrossbow`, y los 834 triángulos de los ocho grupos originales de `crossbow_advanced.obj`. Las texturas son las originales. Se anima la orientación del arma, el disparo de sus brazos y la recarga; en la avanzada se desplaza el cargador y se articulan por separado los dos arcos. Las patas se adaptan a la vagoneta. El renderizador usa instantáneas; las tareas diferidas no consultan entidades.

El propietario abre el inventario usando la ballesta. Hay una ranura para hasta 64 flechas normales, con poción o espectrales. Los componentes de la pila viajan al proyectil, al guardado, a la retirada y al botín. Las flechas usan sus colisiones y efectos nativos; las espectrales aplican Brillo. Cada disparo consume una unidad, sin poder recuperar después una flecha duplicada del suelo. La básica mantiene el intervalo de 40 ticks y la avanzada el de 20; ambas alcanzan 24 bloques. Un dispensador orientado hacia arriba debajo de la ballesta transfiere una pila completa cuando esta queda vacía, sin copiarla ni mezclar efectos. La redstone o el raíl activador taumatúrgico encendido pausan el funcionamiento.

La avanzada permite combinar animales, monstruos y jugadores con tres controles independientes. Se conservan las protecciones de esta adaptación: propietario, aliados, mascotas, compañeros constructos, creativo y reglas de PVP. No se añade el antiguo control para disparar deliberadamente a aliados. Las flechas también comprueban estas protecciones en vuelo. Sin el propietario vivo y presente en la dimensión no hay disparos, recargas ni proyectiles activos.

El propietario puede recoger la ballesta usando la mano vacía mientras se agacha. Se devuelven una ballesta y toda su munición física. Al morir se suelta la munición y se usan tablas nativas con las probabilidades originales de cerebro, mecanismo, madera y, en la avanzada, placas. Los modificadores de Saqueo conservan el incremento original de 0,15 por nivel. La antigua configuración mediante un palo queda sustituida por el inventario. El Thaumonomicon describe la nueva interacción en español e inglés.

## Verificación

`-PbookProbe=turret runServer` comprueba impactos reales de los tres tipos de flecha y sus efectos, consumo exacto, guardado con componentes, controles independientes, permisos del inventario, protección de dueño y mascota, redstone, paredes, dueño ausente, transferencia desde dispensador, migración de bloques, identidad de objetos, colocación y recogida sin duplicados. La prueba separa los impactos del período nativo de inmunidad del objetivo; no se modifica ese período en producción.

`-PbookProbe=turret-client runClient` muestra ambas ballestas disparando autónomamente a un enemigo. Se comprueban sus renderizadores y los eventos sincronizados de disparo y recarga, los impactos y la poción, y el inventario y controles mediante entrada nativa. Las fixtures se excluyen del JAR normal.

La compilación final ejecuta 210 pruebas, incluidas la conservación de todas las piezas y grupos originales y la validez de sus normales. La comprobación del artefacto exige los marcadores de servidor y cliente, capturas anteriores al JAR, ausencia de fixtures, texturas originales idénticas y conservación del contenido de port.66 ajeno a este cambio.
