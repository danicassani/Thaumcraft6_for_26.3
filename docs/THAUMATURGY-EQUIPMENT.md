# Taumaturgia, equipo y guantelete — port.14

Implementación nativa para Minecraft 26.3 / Forge 66.0.13 / Java 25. Añade 43 IDs de objetos (incluidos dos bloques) y 44 recetas: 230 IDs de objeto y 264 recetas de Thaumcraft en total. Esta entrega abre las mecánicas M10/M12; sus variantes avanzadas pendientes se enumeran al final.

## Crear y usar un foco

1. Sigue UNLOCKAUROMANCY para fabricar el resonador de vis y `caster_basic` en la mesa arcana. Crea `focus_1` en el crisol con un cristal de **ordo**, 20 vitreus, 10 praecantatio y 5 auram.
2. BASEAUROMANCY permite fabricar la mesa de piedra y el manipulador de focos (`wand_workbench`). Su receta está en el Taumonomicón.
3. Coloca el foco en el manipulador. Los botones cambian medio, último efecto, potencia y duración; permiten añadir o quitar efectos. La hoja de vista previa muestra el programa entero, cristales y niveles necesarios. Coloca los cristales en los nueve espacios inferiores. Desde port.15 se completan las cantidades desde el inventario principal si hacen falta más aspectos.
4. Crear descuenta todos los cristales y niveles después de comprobarlos. El trabajo consume hasta 20 vis por segundo, se detiene si falta aura y continúa tras guardar/reabrir el mundo. Un cargador encima permite usar los nueve chunks vecinos que ya estén cargados. El foco no se puede extraer durante el trabajo. Romper la mesa devuelve el foco sin completar; no devuelve costes ya pagados.
5. Pon el foco en la mano secundaria y usa el guantelete agachado para instalarlo. Repite con la mano secundaria vacía para retirarlo, o con otro foco para intercambiarlos. Se conservan las pilas físicas, sus nombres y componentes.
6. Usa el guantelete para lanzar. Cada lanzamiento paga aura local y respeta la investigación, capacidad del foco y enfriamiento. El guantelete también inicia una infusión; usarlo agachado cancela una infusión propia. La mano vacía consulta el estado de la matriz.

Capacidades de los tres focos: 15 / 25 / 50. Coste de lanzar: complejidad / 5 vis antes de descuentos. Coste de crear: complejidad × 10 + capacidad / 5 vis; niveles = redondeo de la raíz de complejidad. Los efectos repetidos aumentan su coste de complejidad. Los datos y paquetes se limitan a ocho efectos, potencia/duración 1–5 y nombres conocidos.

## Efectos disponibles

| Medio o efecto | Comportamiento |
| --- | --- |
| Toque / rayo / proyectil | Alcances 4 / 32 / proyectil físico con propietario y programa persistentes; expira a los 100 ticks. |
| Fuego | Daño y combustión; en bloques puede encender un espacio válido, con comprobaciones de protección. |
| Aire / tierra / flux | Empuje y daño / daño / daño mágico. |
| Hielo | Daño y ralentización. |
| Curación | Cura seres vivos y daña no muertos. Permite curar aliados; los efectos hostiles respetan equipos y la regla PVP. |
| Rotura | Destruye bloques según dureza y potencia mediante el controlador nativo del jugador, con sus reglas de cosecha y eventos Forge. Excluye bloques con inventario. |
| Intercambio | Usa el bloque real de la mano secundaria. Destrucción y colocación nativas; respeta permisos/eventos y consume el bloque si se coloca. Una colocación rechazada puede dejar el bloque original ya retirado. |
| Maldición | Debilidad y ralentización temporales. |

Cada nodo exige su investigación original. No existe un paquete cliente que asigne un programa o un coste arbitrario: el servidor construye el programa desde botones acotados y calcula los pagos.

## Equipo

- Gafas de revelación: 5 % de descuento y lectura del aura. El guantelete también muestra el aura local.
- Túnica, pantalones y botas de tela: descuentos 3 / 3 / 2 %. Túnicas del vacío: 5 % por pieza. Los descuentos se aplican tanto al pago real como al coste mostrado por la mesa arcana.
- Cinco herramientas del vacío y cuatro armaduras del vacío; tres piezas de fortaleza; tres túnicas del vacío; botas del viajero; cinco herramientas elementales y triturador primordial. Materiales, reparación y recetas nativas. Las piezas del vacío y el triturador reparan una unidad de durabilidad por segundo mientras se llevan.
- Herramientas elementales: hacha con Burrowing/Collector, pico con Refining/Sounding II, espada con Arcing II, pala con Destructive. El triturador incorpora Destructive/Refining. La azada elemental permite crecimiento con bonemeal nativo usando clic derecho agachado y pagando durabilidad.
- Botas del viajero: capacidad 240 vis, recargables en el pedestal existente. Al correr consumen una carga por segundo para velocidad/salto; amortiguar una caída consume una carga y reduce seis bloques de distancia efectiva.
- Escudo rúnico: infusión dinámica sobre armaduras/accesorios, conservando nombre, daño y componentes. Salis + ámbar × siguiente rango; praemunio = 20 + 20 × 2^rango actual, vitreus y potentia a la mitad. Inestabilidad 5 + rango actual / 2. Límite nativo de nueve rangos por pieza para respetar el presupuesto de 20 000 essentia por infusión. Solo suma piezas equipadas. Cada punto se recarga con un vis, cada dos segundos, con cuatro segundos de espera tras recibir daño. El escudo usa su propia reserva persistente y no sobrescribe absorción de pociones.

## Accesorios nativos

Adaptación explícita: se equipan con clic derecho en la **mano secundaria**; las copias en el inventario no conceden efectos. Mantienen el guardado, intercambio y pérdida al morir nativos. Todavía no existe el inventario de siete accesorios de Baubles.

- Amuleto de vis: cada dos segundos recarga una unidad de un objeto recargable transportado, descontando aura real.
- Amuletos verdantes (capacidad 200): eliminan veneno/wither pagando 20 cargas; las variantes vida/sustento pueden curar o alimentar pagando dos cargas.
- Perla del vidente del vacío: descuento según distorsión permanente, máximo 25 %.
- Banda de curiosidad: convierte parte de las ganancias positivas de experiencia en observación básica, con probabilidad de un tercio.
- Anillo de nube: un segundo salto por vuelo. Solicitud nativa de cliente validada por servidor; no permite repetir saltos hasta aterrizar, entrar en agua o trepar.
- Talismán inmortal: protección de muerte nativa, consumiendo el objeto equipado.
- Comprobador de cordura: consulta los tres tipos de distorsión persistente.

Las bases de joyería y las pociones específicas de mejora se adaptan en estas recetas a ingredientes nativos disponibles. Las recetas concretas están en los datos y las páginas del libro.

Los colapsos de grietas conservan su probabilidad original de recompensa primordial según raíz del tamaño máximo. Esta entrega representa la recompensa en motas (`primordial_nugget`); nueve se unen en una perla y una perla se divide en nueve motas. Es una adaptación al antiguo objeto con metadatos y degradación.

## Pendiente de M10/M12

- Port.15 añade Scatter, Split Target/Trajectory de dos ramas y Cloud/Mine con ajustes y persistencia: [FOCUS-ADVANCED.md](FOCUS-ADVANCED.md). Siguen pendientes grafos de mayor profundidad, opciones originales de fortuna/seda y trayectoria.
- Port.16 añade [Plan](FOCUS-PLAN.md) con volumen/superficie, radios y vista previa nativa; ajustes de tamaño mediante teclas todavía pendientes.
- Port.17 añade [Spellbat y Rift](FOCUS-SPELLBAT-RIFT.md) con usos y restauración persistentes.
- Bolsa de focos, vara pech, torretas, colapsador causal y gancho.
- Siete ranuras de accesorios, bases de joyería y equivalencia exacta de las recetas/variantes originales.
- Mejoras de máscaras/gafas de fortaleza, bonificaciones completas de conjuntos, geometría original de armaduras y habilidades activas adicionales de herramientas elementales.
- Espada/armaduras del culto, jabón, sales de baño, efectos completos de distorsión y penalizaciones del equipo corrupto.

M10 y M12 permanecen **parciales**; registrar estas bases funcionales no da por terminadas sus dependencias avanzadas.

## Verificación

Fixture `-PthaumaturgyProbe=write/read/client`, excluida del JAR normal. 47 pruebas JUnit (cero fallos, errores u omisiones): complejidad, costes repetidos, validación de datos y persistencia del programa, más la batería existente. Servidor: costes atómicos, bloqueo del trabajo, conservación del foco físico, componentes rúnicos, absorción, descuento, salto acotado y daño. Reinicio: foco nombrado guardado a medio trabajo, continuación y finalización sin nuevo pago de cristales/XP. Cliente: lanzamiento real, pago de aura descontado, objetivo dañado, enfriamiento, pantalla y componentes sincronizados.

Registros finales de trabajo: `work/thaumaturgy-client4.log`, `work/thaumaturgy-server-final-write.log`, `work/thaumaturgy-server-final-read.log` y `work/thaumaturgy-clean-build.log`. La prueba gráfica verificó además colisión del proyectil y denegaciones Forge de rotura/colocación. Inspección visual: pantalla completa dentro de 854 × 480 y guantelete con textura resuelta.

Limitación del entorno gráfico: después de guardar y salir al título, el servidor integrado terminó y guardó el mundo, pero el renderizador Vulkan por software quedó esperando en `vkQueuePresentKHR`. Se detuvo ese proceso del cliente. Una ejecución anterior al cerrar la ventana activó el watchdog del cliente por esa misma espera nativa. El volcado sitúa la espera en el backend gráfico nativo, sin ejecución de Thaumcraft en esa pila; el cierre gráfico normal no queda certificado en este entorno.

Entrega: `thaumcraft-6.1.BETA26-port.14-forge66.0.13.jar`, 7 674 166 bytes. SHA-256: `1da9f2232f9af1c6d0a81c326d31fe9d8218bfec4d749f4d2b106e51cd5c054b`. Compilación limpia correcta; el JAR contiene las clases de producción y excluye las fixtures `thaumcraft/test/`.
