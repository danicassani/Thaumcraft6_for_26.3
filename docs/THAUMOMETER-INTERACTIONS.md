# Taumómetro e interactuables — port.78

Agacharse y mantener Usar estudia el objetivo del mundo sin abrirlo ni accionarlo. El escaneo de la mano opuesta se elimina; la vista previa y el servidor seleccionan siempre mediante el mismo rayo de nueve bloques. Los objetos pueden tirarse al suelo para estudiarlos.

`ThaumometerItem.onItemUseFirst` consume el uso agachado antes de la acción del bloque, en ambos lados, mediante el punto de extensión de Forge. Sin agacharse devuelve PASS y conserva la prioridad de apertura normal del bloque. No se anulan eventos de interacción ni se cambian los permisos de protección.

Se conservan los veinte ticks, la cancelación al soltar, la pose a dos manos, el sonido de éxito, la comprobación de terreno cargado, la oclusión y la protección contra conocimiento duplicado. No cambian los identificadores ni el formato del conocimiento guardado. El objeto opuesto permanece equipado y no se consume. Primeros pasos explica el nuevo gesto en español e inglés.

## Verificación

La fixture `-PbookProbe=scan runServer` comprueba ambos lados del hook (PASS de pie / consumo agachado), cofres, barriles, hornos, mesas y palancas; también el tiempo, cancelación, ambos órdenes de manos, duplicados y ausencia de escaneo del objeto opuesto. Conserva las pruebas de lanzamiento real de siete diamantes, cantidades por unidad, privacidad, oclusión y alcance.

`-PbookProbe=interactable-client runClient` prepara un cofre con siete diamantes y fases de horno, palanca y taumómetro en mano secundaria. El uso procede de entradas reales de teclado y ratón. Se exige que se registre el bloque sin abrir menús, activar la palanca ni consumir el objeto opuesto o el inventario del cofre. De pie debe seguir siendo posible abrir el cofre.

Las clases de escena y prueba solo se compilan con la propiedad explícita y se excluyen del JAR normal.

Resultado final: servidor dedicado superado con ambos marcadores (`SCAN78_NATIVE_OK`, `SIGHT61_NATIVE_OK`); cliente superado en las cuatro fases, con apertura normal del cofre y vista previa conocida al agacharse en cada fase. Captura del cofre cerrado revisada y mostrada antes del JAR final. La suite automatizada conserva 246 pruebas.

La primera salida del cliente se interrumpió al cerrar la ventana durante la pantalla de guardado; el watchdog mostró una espera de presentación de Vulkan. Ese registro queda como diagnóstico, no como salida limpia. La repetición exige las cuatro fases y el marcador de regreso al menú antes de cerrar la ventana. No se añaden cambios de backend gráfico al mod.

La repetición final terminó con `SCAN78_CLIENT_MENU_READY` y `BUILD SUCCESSFUL`, sin watchdog ni crash.
