# Pechs (port.54)

Port de las tres variantes de Thaumcraft 6: recolector, mago y acechador. Se conservan modelos, texturas, sonidos, equipo aleatorio y tablas de intercambio originales. La aparición usa la tabla nativa del bosque mágico (peso 10, grupo de uno), oscuridad y límite local de cuatro; respeta las opciones de configuración y dificultad.

Una ofrenda valiosa arrojada al suelo puede ganarse su amistad. Consume una unidad; las perlas de ender garantizan la amistad. Los pechs amigos recogen objetos en una mochila de nueve espacios. El comercio tiene una entrada y cuatro salidas, devuelve objetos pendientes al cerrar y conserva la transacción y su propietario al guardar. Se rechazan pagos repetidos, salidas ocupadas y un segundo cliente simultáneo. Las inserciones se preparan antes de consumir el pago para evitar pérdidas y duplicaciones.

Incluye reacción de enfado del grupo, combate cuerpo a cuerpo, arco con veneno, cinco efectos mágicos, regeneración, botín, huevo de aparición y descubrimiento al escanear. La varita vuelve a obtenerse del mago; se elimina el botín provisional de las brujas.

Compatibilidad: el ojo eldritch nunca se registra en las fuentes originales de TC6; se mantiene su resultado vacío ponderado en vez de inventar un objeto funcional. Se conserva la distribución efectiva de las tablas originales, incluidos los objetos añadidos a la lista del recolector y los quince colores de velas del acechador. Las pociones usan componentes modernos.

Validación: fixture nativa `PechProbe` en `src/worldgenTest` (excluida del JAR), servidor dedicado, reinicio en frío y cliente integrado. Las pruebas de servidor comprueban equipo y variantes, los cinco rangos de recompensas, escaneo, regalos físicos, mochila llena y restos exactos, cierre normal y con inventario lleno, rechazo de pagos repetidos y clientes simultáneos, enfado de grupo, botín nativo, navegación y daño cuerpo a cuerpo, flechas con veneno y Llama, cinco efectos mágicos y aparición nativa seguida de amistad mediante IA. El generador de prueba acelera los intentos de aparición y retira otros monstruos entre intentos para evitar saturar el área; no sustituye la tabla, el bioma ni las reglas de colocación. No mide la frecuencia normal de aparición. El reinicio en frío conserva UUID, motivo de aparición, amistad, variante, cinco espacios ocupados de mochila y una transacción interrumpida: otro jugador no puede reclamarla y su propietario recupera exactamente tres perlas y una varita, incluso con la aparición de pechs desactivada. El cliente integrado muestra los modelos y texturas originales de las tres variantes y abre el menú mediante la interacción real con el pech. Un clic nativo de comercio consume exactamente una de ocho perlas, sincroniza los premios y deja siete ofrendas. Las capturas se toman antes de generar el JAR.

Comandos de verificación (Java 25, Forge 26.3-66.0.13):

```sh
./gradlew -PworldgenProbe=pech-server runServer
./gradlew -PworldgenProbe=pech-read runServer
./gradlew clean build test
python3 tools/verify_model_resources.py
python3 tools/verify_loot_resources.py
```

Las ejecuciones nativas deben contener los marcadores `PECH_*_OK` y no contener `AssertionError`; el código de salida de Gradle por sí solo no acredita una prueba de servidor satisfactoria. `pech-read` requiere el mundo y el comprobante generados por `pech-server`. La escena de cliente usa una copia aislada del mundo: un pech de aparición natural y dos variantes colocadas explícitamente para inspección visual.

Se corrige un bloqueo intermitente de la búsqueda de objetos: el planificador de objetivos no consulta cada tick, por lo que una condición de módulo podía impedir recoger regalos. Se usan plazos de reintento independientes de ese ritmo; una prueba con seis desfases distintos comprueba la recogida mediante IA sin la vía nativa de equipamiento.
