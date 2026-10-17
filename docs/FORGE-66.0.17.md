# Actualización a Forge 66.0.17

Estado histórico: intento bloqueado por versión no publicada.

El usuario autorizó después Forge 26.3-66.0.13. La configuración final y sus
resultados se documentan en [FORGE-66.0.13.md](FORGE-66.0.13.md).

En este intento, el proyecto apuntaba a `net.minecraftforge:forge:26.3-66.0.17`, declara
`[66.0.17]` en META-INF/mods.toml y usa la versión de mod
`6.1.BETA26-port.2-forge66.0.17`. Se conservan Java 25, ForgeGradle 7.0.17
y Gradle 9.5.0. No se pudo contrastar el MDK exacto porque no está disponible.

## Comprobación del 18 de octubre de 2026 (Europe/Madrid)

- environment_status: observaciones actuales, entorno running, política enforced.
- maven.minecraftforge.net está permitido tanto en la política actual como en
  /etc/codex/network-policy.json. VPN no configurada.
- Acceso HTTPS mediante el proxy configurado, manteniendo verificación TLS.
  El sandbox inicial impedía abrir sockets; con permiso de red por comando
  la conexión al proxy y a Maven funciona.
- El catálogo oficial https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml
  responde correctamente. No contiene 26.3-66.0.17; la última versión 26.3
  enumerada es 26.3-66.0.13.
- Los artefactos forge-26.3-66.0.17.pom y forge-26.3-66.0.17-mdk.zip
  devuelven HTTP 404.
- `python3 /workspace/.cloud-setup/gradle-modern build --console=plain`
  falla configurando Mavenizer: forge-26.3-66.0.17-userdev.jar devuelve HTTP 404.
  No se llega a compilar ni a ejecutar los tests.
- runClient y runServer fallaron también resolviendo userdev.jar (HTTP 404), antes de arrancar.
- `git diff --check`: correcto.

Evidencia local en work/forge-metadata.xml y work/forge66.0.17-*.log.
No se genera ni publica un JAR 66.0.17. Las verificaciones anteriores de
66.0.0 en docs/PORT-STEP-2.md no validan esta actualización. La rama de
artefactos no se modifica con binarios antiguos o no verificados.

Para continuar se necesita que se publique 26.3-66.0.17, una fuente oficial
alternativa para esa versión o autorización para seleccionar otra versión.
