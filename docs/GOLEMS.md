# Gólems — port.20

Minecraft 26.3 / Forge 66.0.13 / Java 25. Primera entrega funcional de M11. Incluye prensa, gólem persistente, campana, seis sellos, transporte físico por capacidades Forge y recetas/progresión del libro. M11 permanece parcial.

## Fabricación y piezas

La prensa compacta sustituye al multibloque antiguo de placeholders. Su receta arcana conserva barras de hierro, caldero, pistón, yunque y mesa de piedra, y añade salis mundus y 50 vis. El libro lo indica expresamente. No consume ni transforma un montaje colocado en el mundo.

Selecciona material, cabeza, brazos y mejora; el botón Construir muestra los materiales exactos al pasar el cursor. La prensa comprueba todas las investigaciones de las piezas y reserva los materiales del inventario principal del jugador antes de consumirlos. No cobra parcialmente ni permite editar o repetir un trabajo activo. Se consumen materiales también en creativo. Las mentes y módulos tienen sus recetas propias; mecanismos y placas utilizan las existentes.

Coste de Machina: suma de componentes físicos más dos por rasgo final, como el original. La prensa absorbe una unidad cada cinco ticks por laterales o debajo con succión 128, usando la red de esencia existente. Los ganchos de cuerda proporcionan Machina (5 por unidad) al horno alquímico existente. No acepta por arriba, no acepta otros aspectos ni consume después de terminar. El trabajo pagado y su progreso persisten. Al romper la prensa cae únicamente el resultado terminado; un trabajo incompleto pierde sus materiales, sin generar un gólem gratis.

Materiales: gran madera, hierro, arcilla, latón, taumio y vacío. Mantienen salud base original 6/20/10/16/24/20, armadura y daño por material. Ligero/Pesado cambian la velocidad; Incombustible evita fuego; Resistente a explosiones reduce a la mitad su daño; Vacío repara un punto cada 100 ticks activos. Blindado añade armadura y Frágil la reduce. Rasgos opuestos se cancelan en el orden de las piezas.

Cabezas básica, inteligente e inteligente blindada; brazos básicos, precisos, garras y rompedores; mejoras ninguna, blindado, luchador y porteador. Precisos habilita Cosechar; Garras o Luchador habilitan Vigilar; Rompedores habilita Romper; Porteador lleva dos pilas en lugar de una. Inteligente queda guardado como rasgo para los sellos avanzados, todavía pendientes: no concede sus tareas anticipadamente. Las cabezas inteligentes requieren sus investigaciones y costes originales. Solo hay patas caminantes; no se ofrecen ruedas, trepadores o vuelo sin implementar.

Los escaneos de materiales de hierro/latón/taumio/vacío tienen proveedores modernos concretos para recuperar los disparadores originales `f_MAT*`. No se concede investigación completa al fabricar o desplegar.

## Campana y propietario

Usa el objeto gólem sobre un bloque con espacio: se consume únicamente después de colocar la entidad. Respeta colisión y permisos. Se conservan diseño, canal, nombre y salud al recogerlo y desplegarlo; recogerlo no lo cura. Su carga cae físicamente al recogerlo o morir.

La campana sobre un gólem propio alterna Trabajar → Seguir → Esperar y copia el canal de la campana. Agachado lo recoge. La campana al aire cambia de canal (0–15). Sobre el suelo establece hogar y modo Trabajar para gólems propios del mismo canal a 16 bloques. Sobre un sello copia el canal; agachado abre su configuración. No controla gólems de otros jugadores.

Trabajan con sellos de su propietario/canal a 32 bloques del hogar. Si el dueño está desconectado, muerto, en otra dimensión o espectador, paran y liberan la tarea. Seguir usa navegación y no teletransporta. Al volver, las tareas se recuperan desde los sellos activos.

## Sellos y tareas

Adaptación: cada sello es una placa sin colisión en la celda libre junto al soporte. Solo cabe una placa por celda; no reproduce los seis sellos originales dentro de un mismo bloque. Hay orientación para las seis caras. El soporte debe existir; retirarlo deja caer el sello. Se muestran las texturas animadas originales.

Configuración solo del dueño y a distancia de interacción: canal, prioridad −5 a 5, radio 1–8, pausa por señal de redstone y filtro exacto por objeto/componentes. Fijar filtro toma una muestra sin consumir; si la mano principal es la campana, usa la secundaria. Quitar filtro admite cualquiera. Excluir invierte el filtro; con filtro vacío excluye todos. Cambiar la configuración invalida la reserva vigente. Las ranuras fantasma nunca permiten retirar una muestra.

- **Recoger:** recoge objetos físicos sin retardo de recogida dentro del radio. Camina hasta el objeto y vuelve a validar filtro, alcance y visibilidad.
- **Guardar:** entrega en el inventario del soporte, por la cara marcada. Conserva lo que no cabe. Un inventario lleno no destruye ni expulsa la carga.
- **Vaciar:** extrae del inventario del soporte por su cara hacia la carga; después otro sello Guardar entrega. Respeta restricciones de inserción/extracción Forge. Inspección limitada a 256 ranuras por inventario.
- **Vigilar:** requiere Luchador. Ataca monstruos hostiles visibles en el radio con daño del material y garras. No selecciona al dueño, aliados o mascotas. No habilita caza de animales ni PVP.
- **Cosechar:** requiere Destreza, excluye Torpe. Cosecha `CropBlock` maduros; reserva una semilla del botín real para replantar a edad cero y guarda el resto. Sin semilla no cosecha. No incluye caña, cactus, árboles ni cultivos especiales.
- **Romper:** requiere Rompedor. Trabaja el bloque soporte con botín de pico de hierro; rechaza entidades de bloque, dureza mayor que 5, bloques inmunes al wither o irrompibles. Respeta permisos del dueño y cancelación/denegación Forge. No es una cantera ni tala árboles enteros.

Las tareas tienen reserva exclusiva por sello, renovación y plazo máximo. Los trabajos inaccesibles se enfrían antes de reintentar para permitir otras tareas. No se crean objetos de transporte: todo permanece en la entidad del suelo, en la carga del gólem o en el inventario destino. La carga persiste mientras camina o espera; las reservas no persisten y se recuperan al cargar. El índice de sellos solo incluye entidades de bloque cargadas y se limpia al descargar el nivel. No se fuerzan chunks ni destinos.

## Pendiente

Sellos avanzados (recoger/guardar/vaciar/vigilar), proporcionar, abastecer, usar, talar y carnicero; filtros múltiples, cantidades objetivo y logística a petición. Patas con ruedas/trepadoras/voladoras, cabezas exploradoras, dardos, experiencia/rangos y detalles completos de combate, animación de tareas y accesorios visibles. El modelo es una aproximación articulada nativa con las seis texturas de material originales; los OBJ y accesorios originales no están portados. La prensa compacta y las placas son adaptaciones documentadas. No se declara equivalencia completa con TC6.

## Verificación

Las fixtures `-PthaumaturgyProbe=golem-write/golem-read/golem-client` quedan fuera del JAR normal. Prueban pagos atómicos, investigaciones de piezas, trabajo bloqueado, caras/aspecto de esencia, guardado del trabajo, configuración de sellos y entidad con salud, carga y nombre. El cliente recorre las seis tareas con navegación y objetos reales, redstone, canales, guardado de componentes y pantallas sincronizadas.

Servidor dedicado: `work/golem-server-write2.log` supera `GOLEM_RULES_OK` y `GOLEM_WRITE_OK`; `work/golem-server-read2.log` supera `GOLEM_RESTART_OK`. El trabajo se guarda con tres unidades de Machina pagadas, continúa desde esa cifra y produce exactamente el diseño seleccionado. Sobreviven propietario, canal, modo Esperar, nombre, 17 diamantes y salud 7 del porteador, además de radio/prioridad/filtro del sello.

El recorrido gráfico inicial completo (`work/golem-client3.log`) supera las seis tareas: prensa alimentada por tubo, 17 esmeraldas con nombre recogidas y entregadas, 23 diamantes movidos entre cofres, pausa por redstone y separación de canales, cultivo replantado pagando su semilla real, rotura denegada por Forge sin botín y después permitida con botín de pico de hierro, daño de guardia y órdenes nativas de la campana. Recoger conserva diseño/nombre/salud y suelta la carga física. Los escaneos de los cuatro materiales metálicos y la fuente de Machina también se verifican en el servidor integrado. Se comprueba que focos y torretas del propietario consideran aliado al gólem.

Las primeras fixtures se corrigieron para colocar suelo bajo el gólem del reinicio y conectar el tarro por su cara superior mediante un tubo. La repetición gráfica limpia descarta objetos que dejaron ejecuciones anteriores antes de empezar, evitando confundirlos con duplicaciones nuevas. El primer atlas rechazaba los sprites de objetos usados en modelos de bloque: las placas usan ahora copias idénticas de las texturas animadas en el atlas de bloques. La cuadrícula de inventario tiene contraste entre huecos. El filtro y radio se revalidan también al ejecutar una cosecha ya reservada.

Repetición final: `work/golem-client5.log` supera `GOLEM_PRESS_OK`, `GOLEM_PICKUP_OK`, `GOLEM_ROUTE_OK`, `GOLEM_HARVEST_OK`, `GOLEM_BREAKER_OK` y `GOLEM_CLIENT_OK`, sin aserciones ni rechazos de modelos de sellos. Las pantallas superan `GOLEM_BUILDER_GUI_OK`, `GOLEM_BUILDER_EDIT_OK`, `GOLEM_SEAL_GUI_OK` y `GOLEM_SEAL_EDIT_OK`: los clics nativos cambian material y canal en el servidor y vuelven sincronizados al cliente. Ambas cuadrículas se inspeccionan a 854 × 480. El mundo del cliente se guarda antes de finalizar el proceso gráfico del entorno.

Compilación final `gradle --offline clean build`: 79 pruebas, cero fallos, errores u omisiones. Se comprueban 288 diseños válidos por códec, cancelación de rasgos opuestos, costes/componentes y aptitudes de tareas. La cadena completa de generadores converge sin diferencias. El cliente final guarda y cierra normalmente (`BUILD SUCCESSFUL`). El JAR contiene 283 recetas y 246 definiciones de objetos y excluye `thaumcraft/test/`. Artefacto `thaumcraft-6.1.BETA26-port.20-forge66.0.13.jar` (7,888,119 bytes), SHA-256 `f173071d5cd3b9e9b37896f341b37eb77a366477535dbf92448a85632fea4758`.

Actualización: [port.21 — sellos avanzados y abastecimiento](GOLEMS-LOGISTICS.md) incorpora filtros múltiples, cinco variantes avanzadas y logística física a petición. Véanse allí el estado y los pendientes actuales.

Port.22 añade los tres sellos restantes: [Usar, Talar y Carnicero](GOLEMS-TASKS.md), con límites de la adaptación y pruebas.

Actualización posterior: [Movilidad de gólems — port.23](GOLEMS-MOBILITY.md) incorpora ruedas, trepadoras y levitadoras.
