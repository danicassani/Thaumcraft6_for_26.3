> Actualización port.72: [estandartes originales y equivalencias visuales](BANNERS-VISUALS.md). Las descripciones de sustitutos vanilla y superficies estáticas siguientes corresponden a entregas anteriores.

# Espejos y enlaces — port.42

Registra `mirror`, `mirror_essentia` y `hand_mirror` y conecta el transporte remoto al sistema existente. Las dimensiones se guardan por su clave moderna; cada espejo tiene identidad persistente. Un bloque nuevo en las coordenadas de uno destruido no hereda sus enlaces.

## Vincular y mover

Usa un espejo como objeto sobre otro espejo libre del mismo tipo, y coloca después el objeto marcado sobre una cara sólida. Ambos extremos se enlazan en el siguiente tick. El enlace espera si la otra dimensión o su chunk no están cargados; no fuerza su carga. No admite autoenlaces, tipos mezclados ni apropiarse de una pareja válida. Los extremos guardados se reintentan cada dos segundos.

Se puede montar sobre suelo, techo o pared. Al romperlo, el objeto conserva identidad, destino e inestabilidad; el contenido pendiente cae físicamente una vez, separado del bloque. Si el otro extremo está cargado se libera su enlace para recolocarlo. Si estaba descargado y conserva una referencia a la ubicación anterior, la reparación puede esperar a que esa ubicación vuelva a cargarse: no se carga terreno para comprobarla.

El formato de datos está versionado. Datos ilegibles o de versiones futuras quedan desactivados y se conservan en el objeto del bloque, sin vaciarlos ni reemplazarlos por un inventario nuevo.

## Objetos

Los objetos que tocan la superficie del espejo se ofrecen a la cola del extremo enlazado. La capacidad Forge ofrece una ranura de entrada sin extracción ficticia para automatización. No se consume lo que no cabe o no puede entregarse. La cola admite 27 pilas normales como adaptación acotada a la lista ilimitada de 1.12.

El destino expulsa una unidad por tick después de sus primeros 20 ticks, con componentes originales y velocidad hacia fuera. Solo descuenta después de que el mundo acepte la nueva entidad; una cancelación Forge conserva la cola. La entidad de salida tiene enfriamiento y una marca que impide reabsorberla por ese mismo espejo. Recogerla y volver a soltarla permite usarla de nuevo. Esta marca evita el rebote automático continuo entre parejas.

Enviar suma inestabilidad por objeto en el remitente; expulsar suma una unidad en el receptor. Al superar 128 genera un flux y resta 128; disminuye una unidad cada 100 ticks, como el original.

## Esencia

La pareja expone los tarros cargados delante del extremo remoto: profundidad 0–7 y radio lateral de ocho bloques en ambos ejes, excluyendo la posición del propio espejo. No atraviesa otros espejos para buscar fuentes. Respeta tipo de esencia, filtros, capacidad y comportamiento de tarros del vacío. Las consultas y simulaciones no consumen nada. Al insertar se prefieren tarros que ya contienen esencia compatible antes de usar los vacíos. Un tarro receptor se excluye de las fuentes remotas de ese trasvase para impedir que se drene a sí mismo en zonas superpuestas.

Cada extracción o inserción transporta una unidad y suma una unidad de inestabilidad en el extremo usado. El umbral de contaminación es 64, con la misma decadencia de 100 ticks.

Las matrices de infusión aceptan el espejo como fuente, además de los tarros locales. Adaptación al transporte moderno: expone `EssentiaTransport`, permite a tubos y tarros extraer esencia remota, y una succión baja de 16 puede recibir de tubos/alambiques para entregar a tarros remotos. No drena tarros locales a través de esa succión, cuyo mínimo es mayor. Las operaciones fallidas conservan la esencia en origen.

## Espejo de mano

Usa el espejo de mano sobre un espejo de objetos para vincularlo, aunque este ya pertenezca a una pareja. El uso en la mano principal abre una ranura de envío. Acepta clic y Mayús; bloquea el objeto sostenido y valida su identidad/vínculo en el servidor.

Si el destino no está disponible o está lleno, lo no enviado permanece en la ranura. Se puede retirar y se devuelve al cerrar. Mover o volver a vincular el espejo invalida el menú anterior. No hace falta que el destino pertenezca a una pareja para recibir envíos directos de mano.

## Recetas y visuales

Se conservan las tres infusiones originales: oro para espejo de objetos; hierro para esencia; espejo, palo, brújula y mapa para el de mano. Se añade la receta arcana original de vidrio espejado: azogue y panel de vidrio, 50 vis y un cristal de agua y de orden. Las páginas y requisitos usan MIRROR, MIRRORESSENTIA y MIRRORHAND, sin desbloqueos gratuitos.

Se reutilizan los marcos, vidrio y textura del espejo de mano originales en las seis orientaciones. La superficie activa y partículas nativas sustituyen el antiguo shader multicapa OpenGL; no renderiza una cámara real del destino. Los espejos como bloques se limitan a una unidad por pila para mantener clara su identidad y vinculación.

## Verificación

El servidor nativo verifica vinculación mediante el objeto y colocación real, cancelación Forge sin efectos remotos, enlaces por identidad y entre dimensiones, automatización simulada y real, transporte físico, entrega parcial, límite de cola, cancelación de aparición sin descontar, componentes de objetos y rotura/recolocación sin duplicación. Los formatos futuros se desactivan y se conservan al romper el bloque.

El transporte de esencia comprueba volumen y orientación, succión, tarros receptores, prioridad de tarros compatibles, ausencia de autoextracción en zonas superpuestas y flux. Una matriz real fabrica la receta original de espejo consumiendo exactamente 75 unidades de esencia de tres tarros del Nether y cuatro ofrendas físicas.

Una segunda JVM verifica enlaces, identidad, componentes y conservación exacta de objetos y esencia tras guardar y reiniciar. Los generadores pasan dos veces, con cero cambios en la segunda pasada.

En cliente se validan las cuatro recetas registradas; se abre el espejo de mano con entrada real y se envían ocho diamantes con Mayús. El cliente observa los ocho objetos físicos en el destino. Con un destino descargado, tres lingotes permanecen en la ranura y vuelven al inventario al cerrar. La captura del mundo se toma antes de generar el JAR final y antes del push.

Cliente guardado y cerrado normalmente. Compilación limpia: **180 pruebas**, sin fallos, errores ni omitidas. El JAR contiene 325 recetas y 305 definiciones de objetos; conserva las clases de las entregas anteriores y excluye todas las fixtures de prueba.

Artefacto: `thaumcraft-6.1.BETA26-port.42-forge66.0.13.jar` (8758910 bytes). SHA-256: `428a894e4340583a33ae281cb6753ae8292e4e09569a0f1b2db394117cf45fd4`.
