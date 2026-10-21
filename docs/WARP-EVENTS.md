# Distorsión y jabón de cordura — port.38

Implementación del motor de sucesos de distorsión y de la limpieza de cordura. Continúa M12; no cierra todavía sus siete ranuras de accesorios, máscaras ni equipamiento del culto.

## Comportamiento

- Conserva los datos existentes de distorsión permanente, normal, temporal y contador. Ganar distorsión vuelve a armar el contador; reducirla no lo rearma. Los datos corruptos o de versiones futuras no se sustituyen.
- Comprobación cada 2000 ticks de mundo por jugador. El plazo se guarda para no reiniciar el intervalo al volver a entrar. Decaimiento temporal, probabilidad `sqrt(contador)`, agotamiento y selección de severidad según TC6; las piezas del vacío contribuyen al peso.
- Los jugadores creativos, espectadores y muertos no reciben comprobaciones. `wussMode` y la protección del baño impiden nuevos sucesos. No modifica la distorsión permanente al limpiar.
- Sucesos: sonidos privados de creeper/explosión, mensajes, agotamiento, taumarrea, hambre antinatural, oscuridad/apariciones, visión borrosa, rechazo solar, fatiga minera, agotamiento infeccioso, visión nocturna, mirada mortal, arañas mentales, ceguera, alivio de distorsión normal y portal carmesí.
- El hambre provoca agotamiento alimentario y se cura comiendo carne podrida o un cerebro. La leche preserva las aflicciones propias no curables de TC6; puede eliminar visión borrosa. La mirada mortal exige cono de visión, línea despejada y respeta las protecciones de equipos/PVP.
- El jabón tarda 100 ticks; soltarlo desde el tick 96 también completa un lavado. Consume una unidad, elimina toda la distorsión temporal y reduce la normal en uno, más uno con protección del baño y otro dentro del fluido purificador. Conserva la permanente, el contador y los efectos ya activos. Su receta original requiere SANESOAP, bloque de carne, Cognitio 75, Alienis 50, Ordo 75 y Victus 50.
- Arañas mentales alucinadas: dueño por UUID, visibles solo a él, sin daño ni botín, duración absoluta de 1200 ticks guardada. Las reales pueden atacar. Guardianes y cultistas usan entidades/IA nativas; el portal tiene salud 100, espera inicial de 100 ticks, límite local de esbirros por dificultad y coste de salud por invocación.
- Las apariciones solo prueban posiciones cargadas, sin líquido ni colisión, dentro del mundo; límites locales de arañas/guardianes/portales. El modo pacífico o `wussMode` retira estas criaturas. El orbe expira a los 100 ticks, inflige daño mágico en dos bloques y debilidad durante 160 ticks; respeta a no muertos y criaturas sobrenaturales aliadas. El guardián puede usar su ataque de aflicción/ distorsión temporal; el clérigo alterna proyectil mágico y ráfaga de fuego.

## Adaptaciones y trabajo pendiente

La niebla y la visión borrosa usan oscuridad y náusea nativas; no reproducen el shader 1.12. Guardianes conservan las 53 cajas, jerarquía y oscilaciones del modelo original; el portal usa su atlas animado original. Las arañas usan el modelo nativo a escala 0,6. El proyectil mágico del clérigo reutiliza provisionalmente el orbe, en lugar del orbe perseguidor original. Las armaduras/armas y modelos especiales del culto, ojos luminosos y animación específica de lanzamiento quedan para el bloque de equipamiento. Los botines propios del culto conservan pepitas y la curiosidad de ritos; todavía falta el botín de su equipo.

`graphics.nostress` permite desactivar los sonidos privados repentinos. Los cambios no sustituyen IDs de bloques u objetos anteriores ni regeneran terreno.

## Verificación

- JUnit: 169 pruebas sin fallos, incluidas reactivación, retirada, límites sin overflow, probabilidad/agotamiento del contador, limpieza y la banda excepcional de alivio.
- Servidor dedicado: jabón soltado antes de tiempo sin consumo, frontera de 96 ticks, una unidad por lavado, bonificaciones de baño/protección, permanente y contador conservados; datos futuros intactos y sin consumir jabón; contador rearmado, decaimiento temporal, aflicciones aplicadas y araña sin daño. Impacto de orbe contra bloque con daño mágico y debilidad reales a una vaca cercana.
- Reinicio dedicado: dueño/estado inofensivo de araña, guardián, portal y pilas de jabón conservados.
- La primera ejecución de cliente reveló un error en el escenario de prueba: la cámara quedaba fuera de la plataforma. Se corrigió el fixture y se repitió; no era un fallo de la mecánica.

- Cliente nativo: invocación real del portal ante un jugador en supervivencia, aparición de esbirro y coste de salud; modelos de guardián, portal animado y cultistas. Uso mantenido real de jabón, con una unidad consumida en la primera finalización, distorsión temporal eliminada, normal de 10 a 9 y permanente de 30 conservada.
- La prueba del portal se ejecutó en dificultad normal para permitir invocar junto a los dos cultistas iniciales. El fixture fija ahora esa dificultad.
- Captura compartida antes de crear el JAR y de pushear. La foto muestra guardián, portal y tres cultistas; no prueba por sí sola la visibilidad privada de las arañas.
- Generadores de recursos en dos pasadas; segunda sin cambios. Modelo original generado con jerarquía validada.

SHA-256 de la captura: `0463f71a0ad968de0847aeb27a1687422d9e729dc7a065a8c19120a23771a4f5`.

Construcción normal `clean build test --offline`: 169 pruebas, cero fallos, errores u omisiones. JAR sin fixtures ni entradas ZIP duplicadas; 310 recetas y 283 definiciones de objetos. Etiquetas anteriores conservadas y todos los JSON parseados.

El entorno se reinició durante un primer intento de construcción; los resultados nativos y la captura persistieron. La construcción completa se repitió correctamente tras recuperar la conexión.

Entrega: `thaumcraft-6.1.BETA26-port.38-forge66.0.13.jar` (8532169 bytes).
SHA-256: `2e24a982451d73a09c96fff2fadf930bfecf7e664a1030a525115dc204763616`.
