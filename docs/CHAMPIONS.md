# Campeones y bolsas de botín (M16.1)

Port de los campeones de Thaumcraft 6 y de su única fuente de bolsas de botín. No incluye jefes, dimensión exterior ni bioma eerie.

## Activación

- `world.allowChampionMobs` (servidor) conserva la lógica original: al desactivarlo se suma 2 a la tirada, desaparece el `-2` de difícil, Nether/End pasa a `-1` y la whitelist no aporta bonus. En la práctica los campeones quedan casi extinguidos salvo coincidencias muy raras; en el puerto, al no existir la dimensión exterior (que en TC6 aún restaba 3), desactivar la opción equivale a no generar campeones.
- Al aparecer un `Mob`, si su atributo `thaumcraft:champion_mod` vale `-2` se procesa una sola vez:
  - tirada base `0..99`;
  - dificultad fácil `+2`, difícil `-2` (solo si los campeones están permitidos);
  - Nether/End `-2` (o `-1` si están desactivados);
  - bonus de whitelist `max(0, valor - 1)`.
- Es campeón si es de la whitelist, la tirada ajustada menos el bonus es `<= 0` y su salud base es al menos 10.
- Los mobs procesados que no son campeones reciben el modificador `champion_none`; así no se vuelve a tirar al recargar.

La lista original se limita a lo que existe en el puerto: zombi, araña, blaze, enderman, esqueleto, bruja y taintacle. Los valores originales son `0,0,0,0,0,1,2`; el taintacle conserva el valor `2` (bonus `1`). EldritchCrab e InhabitedZombie no están portados todavía.

## Atributo y estadísticas

- Atributo sincronizado `thaumcraft:champion_mod`, rango `-2..100`.
- `-2`: sin procesar; `-1`: normal; `0..13`: índice del modificador.
- Todo campeón recibe `+100` de salud máxima y daño `×3` (`ADD_MULTIPLIED_TOTAL 2`), y se cura 25.
- El nombre usa el prefijo traducido `champion.mod.*` delante del nombre o tipo original.
- Un creeper siempre recibe `bold`, igual que en TC6.

| Índice | ID | Efecto portado |
| --- | --- | --- |
| 0 | bold | `+30%` velocidad base |
| 1 | spine | devuelve 1–3 de daño con espinas |
| 2 | armor | reduce el daño recibido a `19/25` |
| 3 | mighty | daño adicional `×3` |
| 4 | grim | 40% de aplicar wither 200 ticks |
| 5 | warded | absorción inicial de salud base/2 y +1 cada 25 ticks hasta ese límite |
| 6 | warp | 33% de añadir 1–3 warp temporal al jugador |
| 7 | undying | cura 1 cada 20 ticks |
| 8 | fiery | 40% de 4 segundos de fuego |
| 9 | sickly | 40% de hambre 500 ticks |
| 10 | venomous | 40% de veneno 100 ticks |
| 11 | vampiric | se cura `max(2, daño/2)` |
| 12 | infested | 40% de invocar un taint crawler |
| 13 | tainted | conserva las estadísticas base de campeón; la conversión `makeTainted` (IA y refuerzo extra) no forma parte de M16.1 |

## Botín

Al morir un campeón recién golpeado y que no sea `tainted`:

- una sola bolsa, con tier `min(2, floor((rand(9) + looting) / 5))`;
- `common`, `uncommon` o `rare` según el tier;
- entre 5 y 7 puntos de experiencia;
- se excluyen jefes y kills de FakePlayer.

Las tablas de las tres bolsas ya existían desde port.48; este milestone solo añade su fuente real por campeón.

## Fórmulas verificables

`ChampionRules` contiene las partes numéricas sin estado del mundo y `ChampionRulesTest` las cubre con JUnit: bonus de whitelist, ajustes de dificultad/configuración/bioma, condición de aparición y tier de bolsa.

## Adaptaciones conscientes

- No se porta la capa visual `tainted` ni los efectos de partículas/sonido de gore originales; el nombre, atributo y efectos sí se conservan. Tampoco se reescribe la IA al estilo `makeTainted`: un campeón `tainted` aleatorio conserva la IA de su especie y solo recibe las estadísticas base, como en `makeChampion` de TC6.
- `spooky` no se comprueba porque el puerto aún no tiene el bioma eerie; Nether y End sí.
- La localización de peligro del original devuelve siempre `false`; no se replica.
- El atributo persistido usa `Identifier` en lugar del UUID de 1.12.2, como exige la API moderna.

## Cierre de verificación port.55

Se corrige el registro efectivo del atributo: `getBaseClass()` describe la fábrica y no permitía identificar los zombis concretos. El evento añade el atributo a los tipos vivos disponibles y la lógica de aparición sigue limitada a `Mob`. Los campeones Warded reciben ahora un modificador persistente de `MAX_ABSORPTION`, necesario en 26.3; se repara también al actualizar campeones antiguos. El modificador de warp respeta `wussMode`.

La fixture nativa comprueba la conversión por `EntityJoinLevelEvent`, las 14 identidades, atributos y serialización, daño real de espinas/armadura y efectos ofensivos, crías de corrupción, regeneración de escudo y salud, y una sola bolsa tras una muerte real. Otras 64 muertes de zombis y 64 de cultistas verifican los drops y componentes con Looting III. Estas comprobaciones complementan los seis tests de fórmulas; las fixtures no entran en el JAR.

Se mantiene el alcance documentado de M16.1: no se añade aquí la conversión independiente `makeTainted` de criaturas pasivas, jefes, eerie ni dimensión.

La prueba de reinicio en otro proceso restaura el límite de absorción Warded. La inspección de cliente incluye su modelo y nombre sincronizados junto al clérigo y al proyectil real; no depende de una captura del menú principal.
