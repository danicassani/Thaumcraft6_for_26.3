# Escaneo de plantas acuáticas y phantom — port.81

La hierba marina alta (`tall_seagrass`, ambas mitades) y los tallos de kelp (`kelp_plant`) carecen de objeto de bloque propio. El escáner terminaba recurriendo al cubo del fluido que contienen. Se convierten explícitamente en `seagrass` y `kelp` antes de considerar el fluido. Conservan las identidades de investigación de esos objetos: no generan descubrimientos repetidos por cada sección ni alteran plantas o agua. No se cambian las formas de selección ni se permite atravesar paredes. La hierba marina corta ya se identificaba correctamente; el kelp superior requiere apuntar a su forma real.

El catálogo de entidades consulta ahora el suplemento cuando no existe una definición original. Se define el phantom como no muerto volador (`exanimis:10`, `volatus:10`, `bestia:5`). Las asignaciones originales y las variantes dinámicas de pechs y wisps mantienen su prioridad.

`-PbookProbe=aquatic-baseline runServer` reproduce las identidades incorrectas y los aspectos vacíos del phantom en la versión anterior. `-PbookProbe=aquatic runServer` verifica las cinco formas de plantas, el tiempo de 20 ticks, la conservación del bloque, que repetir no dé puntos, los aspectos y la privacidad de la previsualización del phantom, y la oclusión de una pared. La inclinación de la cámara apunta al interior de la forma del kelp, más baja que la hierba marina.

`-PbookProbe=aquatic-client runClient` prepara una escena de hierba marina alta sumergida y otra de phantom, para escanearlas con entrada real del ratón y comprobar que la previsualización conocida muestra sus aspectos. Las escenas y pruebas no se incluyen en el JAR normal.

La primera escena visual usaba arena suspendida y perdió el soporte por gravedad; durante la prueba se repuso con tierra estable y se colocaron las dos mitades mediante comandos vanilla. Los escaneos y sus previsualizaciones se completaron con el ratón. La fixture queda corregida para usar tierra desde el inicio. También se limita el ensayo de servidor a sus modos exactos para que no detenga el servidor integrado de la escena visual.
