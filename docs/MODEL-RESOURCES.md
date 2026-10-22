# Recursos visuales: port.46

Se corrigen los avisos de texturas/modelos que seguían apareciendo tras port.45.

- **Cofre hambriento:** la textura original ya estaba en el atlas de cofres para el renderizador del bloque, pero faltaba en el atlas de bloques utilizado por el objeto JSON y las partículas de rotura. Se registra el mismo PNG como sprite explícito sin duplicarlo ni cambiar el atlas del renderizador.
- **Crecimiento de corrupción:** las cuatro variantes tenían geometría y textura de caras, pero ninguna definía la textura `particle`. Cada variante recibe su textura original como partícula; se conserva la geometría y el recorte. La tercera variante utiliza una clave de cara diferente, por lo que la partícula referencia directamente el recurso.

No cambian registros, recetas, capacidades ni formatos de guardado.

## Comprobación previa al JAR

`checkModelResources` forma parte de `check` y, por tanto, de `build`. Ejecuta `tools/verify_model_resources.py` después de `processResources`, sobre los recursos procesados: incluye las texturas heredadas que Gradle copia desde `src/main/resources`.

Comprueba las referencias alcanzables desde blockstates y definiciones de objetos, herencia local, ciclos, aliases de textura, archivos PNG y pertenencia a los atlas de modelos. Las plantillas que solo reciben sus texturas de modelos descendientes no se comprueban como modelos finales. No interpreta el contenido de modelos o texturas vanilla ni sustituye la carga nativa del cliente.

Requiere Python 3. Se usa `python3` en Unix y `python` en Windows; se puede especificar otra ruta mediante `-PpythonExecutable=...`. Ejecución manual tras procesar recursos: `python3 tools/verify_model_resources.py`.

La regresión reproduce los seis errores del recurso anterior. Cuatro mutaciones aisladas verifican el rechazo de PNG ausente, referencia de textura inexistente, omisión de atlas y ciclo de herencia. Dos pasadas de todos los generadores dejan la segunda sin modificaciones.

## Cliente

La fixture opcional `-PalchemyProbe=resource-visual` coloca un cofre, equipa su objeto y muestra las cuatro variantes de crecimiento en una zona aislada del mundo de pruebas. Comprueba el sprite real del atlas y la partícula horneada del cofre; espera la sincronización de las cuatro variantes antes de permitir la captura. Las fixtures quedan excluidas del JAR normal.

Resultado final: 626 referencias verificadas tanto en recursos procesados como extraídas del JAR; 180 pruebas sin fallos, errores u omisiones. Cliente con escena y modelos horneados verificados, sin avisos de texturas/referencias ausentes. La primera prueba terminó con un watchdog del backend Vulkan al cerrar directamente la ventana dentro del mundo; la repetición con compilación normal, salida por menú, guardado y cierre de ventana terminó correctamente. No se modifica el backend gráfico del port.

Artefacto: `thaumcraft-6.1.BETA26-port.46-forge66.0.13.jar`, 8 900 440 bytes. SHA-256: `07a2d0c9765e7a600de84b8cb43e727c13a41124d1a1a1b14cd8c4653a119844`. La captura de los bloques y el objeto se tomó antes de compilar este JAR y antes de pushear.
