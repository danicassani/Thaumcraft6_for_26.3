# Actualización a Forge 26.3-66.0.13

El usuario autorizó 66.0.13 tras comprobar que 66.0.17 no estaba publicada.
Se actualizan la dependencia Forge, el rango exacto en META-INF/mods.toml y
la versión del mod a 6.1.BETA26-port.2-forge66.0.13.

Se descargó el MDK oficial 26.3-66.0.13 desde maven.minecraftforge.net con TLS
verificado. Java 25, ForgeGradle 7.0.17 y eventbus-validator 7.0.6 coinciden
con la plantilla. El wrapper completo se actualiza al Gradle 9.7.1 del MDK.
Se conserva la unión explícita de clases y recursos existente en el proyecto.
Se corrige el destino de processResources mediante su convención destinationDir:
la asignación anterior no prevalecía sobre SourceSet.output.resourcesDir y
mergeMainMod eliminaba los recursos en un build limpio. Ahora processResources
escribe en build/staging/main/resources y mergeMainMod es el único propietario
de build/mod/main. No se desactiva ninguna tarea de compilación ni de tests.
El script Windows del wrapper solo normaliza CRLF a LF respecto al MDK.

## Verificación del 18 de octubre de 2026 (Europe/Madrid)

- Política de red actual enforced; maven.minecraftforge.net permitido.
  MDK descargado con TLS verificado, SHA-256
  a4c489d0f479057b81a1d9d93a1dbdb84e8eff017135797d470e6980fa066a6e.
- Compilación limpia con Java 25 y Gradle 9.7.1: BUILD SUCCESSFUL.
- WarpCodecTest: 6 tests, 0 fallos, 0 errores y 0 omitidos. La suite se
  ejecutó sobre 66.0.13; repeticiones posteriores pueden reutilizar caché válida.
- Inspección del JAR: 6 clases (bytecode major 69), 1.531 assets, pack.mcmeta
  moderno y META-INF/mods.toml con versión de mod y dependencia exacta correctas.
  Sin mcmod.info, tc_at.cfg ni WarpRestartProbe.class.
- runClient: menú principal con Minecraft 26.3, Forge Beta 66.0.13 y 3 mods.
  Pantalla Mods confirma Thaumcraft 6 versión 6.1.BETA26-port.2-forge66.0.13.
  Inicializaciones común y cliente registradas; salida con Quit Game,
  BUILD SUCCESSFUL.
- runServer: entorno SERVER, Forge 66.0.13 y Thaumcraft cargados. Servidor
  listo, respuesta JSON válida Minecraft 26.3/protocolo 777 en 127.0.0.1:25565.
  Cierre por stop, guardado de jugadores/mundos y BUILD SUCCESSFUL.
- git diff --check: correcto. src/main permanece intacto.

Los arranques usan las tareas de desarrollo ForgeGradle y su raíz combinada;
el JAR distribuible se comprueba adicionalmente por contenido y SHA-256.
Las capturas, XML de tests y logs se publican junto al JAR en la rama de artefactos.

Persisten avisos no fatales de servicios online inaccesibles, audio y comprobación
kqueue en Linux. No se validan Realms, autenticación remota ni reproducción de audio.
No se ha repetido la prueba de persistencia entre reinicios de 66.0.0.
La actualización sigue siendo de infraestructura: no añade el gameplay 1.12.2.
Las pruebas anteriores de docs/PORT-STEP-2.md corresponden a 66.0.0.
