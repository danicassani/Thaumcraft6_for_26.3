# Thaumatorium — port.33

Cuarto lote de alquimia avanzada para Minecraft 26.3 / Forge 66.0.13 / Java 25. Incorpora el Thaumatorium y las matrices mnemónicas; el condensador queda como siguiente mecánica. No modifica la generación del mundo ni los IDs de contenido anterior.

## Montaje y uso

Coloca dos estructuras alquímicas (`metal_alchemical`) verticalmente sobre un crisol y aplica Salis Mundus después de investigar `THAUMATORIUM`. Conserva el crisol y su contenido. Comprueba permisos y eventos de protección antes de consumir materiales. El frente apunta hacia quien lo monta.

Calienta debajo del crisol con fuego, lava, magma o nitor. La mano vacía abre el selector de recetas de alquimia investigadas. Las páginas muestran resultados y los botones +/− activan o retiran recetas; pasar el cursor sobre un botón muestra sus aspectos y cantidades. El servidor valida distancia, investigación y capacidad. Los resultados mostrados son muestras que no pueden retirarse del menú.

Una receta de base. Cada matriz mnemónica orientada hacia un lateral o la parte trasera de cualquiera de las dos piezas, o hacia arriba de la pieza superior, añade dos espacios: siete matrices y quince recetas como máximo. Su receta arcana original usa cuatro placas de hierro, cuatro ámbares, una mente de relojería, 50 vis y un cristal de tierra y otro de orden.

Al retirar matrices se conservan las selecciones; solo se programan las primeras que caben en la capacidad actual. No cancela trabajo pagado. No se permite retirar la receta activa.

## Automatización y pago

Las dos piezas comparten una ranura física de catalizadores, accesible por tolvas y la capacidad Forge de objetos. Se reserva una unidad al iniciar; el jugador no puede retirar esa unidad en proceso. Admite las recetas nativas de crisol, incluidos los catalizadores con componentes, cristales específicos y recipientes devueltos.

Esencia por los lados y detrás de ambas partes o por arriba de la pieza superior. No conecta por delante ni por abajo. Succión 128 para un aspecto requerido cada vez; extrae una unidad cada cinco ticks, sin cargar chunks vecinos. La simulación no consume recursos. Redstone en el controlador, la pieza superior o el crisol pausa entrada y fabricación; también espera si falta calor.

El trabajo reserva el resultado y los costes al empezar. Un cambio de recetas no altera una operación ya pagada. Si el datapack cambia el requisito de investigación de una selección, hay que retirarla y volver a autorizarla antes de programar más trabajo. Al completar se entrega por delante a un inventario; si está lleno o bloqueado, conserva productos y no inicia otro trabajo. Sin inventario deja objetos físicos.

## Guardado y desmontaje

Formato versión 1 con identificadores estables de receta, UUID del selector y requisito de investigación autorizado, catalizador reservado, entrada, costes pendientes y productos. Usa contexto de registros para conservar nombres, encantamientos y otros componentes de objetos. Un formato futuro se conserva íntegro y desactiva la máquina.

Romper una pieza restaura la otra como metal alquímico. Si se retira el crisol se restauran ambas. Devuelve entrada, catalizador reservado o productos ya terminados una sola vez; la esencia recibida en una operación incompleta se libera como flux. Las matrices sin soporte se desprenden. Las partes se obtienen por montaje, sin un objeto transportable que omita el multibloque.

## Verificación

Fixtures aisladas `-PalchemyProbe=thaumatorium-write/read/client`, excluidas del JAR normal. Prueban protección e investigación, capacidad y conservación de selecciones, calor/redstone, transporte por ambas piezas, tolva, cofre lleno, desmontaje, formatos futuros y reinicio. Cliente nativo con el modelo OBJ original convertido a 216 triángulos, menú real y productos físicos.

### Resultados nativos

- Servidor dedicado: rechazo de montaje sin investigación y por protección sin consumir Salis; selección autorizada y límites de una/quince recetas. Las selecciones sobreviven al retirar matrices.
- Catalizador reservado exactamente una vez, simulación sin pago, rechazo de caras/aspectos incorrectos, entrada superior compartida, pausa por frío/redstone y entrega diferida a un cofre lleno. Desmontaje con exactamente dos catalizadores recuperados y crisol conservado. Datos futuros preservados e inactivos.
- Tolva real sobre la pieza superior: dos catalizadores, veinte unidades de esencia extraídas de dos alambiques por ambas piezas, exactamente dos lingotes de taumio en el cofre nativo.
- Reinicio dedicado: tres unidades pagadas, una entrada reservada, dos catalizadores restantes y sus identificadores/UUID recuperados; reanudación sin duplicar. Segundo aparato con dos productos bloqueados y un arma encantada: recuperación de componentes y entrega exacta. Carga también el horno infernal del mundo real de port.32.
- Cliente nativo: repite las pruebas mecánicas, comprueba la autorización tras cambiar el requisito de investigación, guarda/recupera un cristal fabricado con sus componentes y verifica los bloques sincronizados. Paquetes nativos del menú seleccionan y retiran una receta, avanzan y retroceden de página.
- Capturas reales de 1280×720 compartidas antes del JAR y del push. Instalación: `f6f2ac8d0ad3d6c4f01cabbe677fca0520f027e75bef18c826b0d019dddf6c22`. Selector final, con flechas laterales sin solapamiento: `8165263e46ce0f61c4dffb47bc7baff2ca2812f6dc3fb85328566fcdc97f87f9`.
- Cadena de 21 generadores ejecutada dos veces; segunda pasada sin cambios. Se conservan las etiquetas anteriores de minería.

Compilación limpia final: **134 tests, 0 fallos, 0 errores y 0 omitidos**. Los tres recorridos nativos finales terminaron con `BUILD SUCCESSFUL`, sin excepciones de prueba ni crashes. Cliente guardado, regreso al título sin capturarlo y cierre normales.

JAR normal sin fixtures, con 305 recetas y 267 definiciones de objeto; JSON válidos y recursos/clases anteriores conservados. Artefacto `thaumcraft-6.1.BETA26-port.33-forge66.0.13.jar` (8171472 bytes), SHA-256 `37327b49a32fa7149a6a9b1630992e6f5e18f2aef0c4f4d062848e819673aaf2`. Generado después de compartir las capturas.
