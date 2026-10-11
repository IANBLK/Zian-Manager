# Almacenamiento por jugador — beta.2

## Qué cambia

- Consultas de recompensas por jugador y detección de combates repetidos mediante
  índices reconstruidos al abrir el registro y después de una escritura confirmada.
- Una sola serialización JSON por cambio; archivo temporal, fsync y reemplazo
  atómico. En Linux también se sincroniza el directorio antes de publicar el estado.
- Migración opcional de recompensas y llaves a archivos por jugador. La caché
  mantiene hasta 32 jugadores por defecto (configurable entre 1 y 64 en el manifiesto).
  Una entrega activa queda protegida frente a expulsiones de caché por callbacks.
- Los límites anteriores de 20.000 reclamaciones /16 MiB pasan a ser por jugador
  después de migrar, en lugar de compartirse entre todos los usuarios. Las llaves
  mantienen el límite de 200.000 entradas /16 MiB por archivo de jugador.
- No se eliminan recibos, ciclos, identificadores de combate ni costes de llaves.
  PENDING/APPLYING/REVIEW permanecen registrados; no se reintentan a ciegas.

El formato compartido anterior sigue funcionando hasta que el administrador
ejecute la migración. Instalar el JAR por sí solo **no migra los datos**.

## Migración offline

1. Detener el servidor y deshabilitar su reinicio automático durante el mantenimiento.
2. Crear una copia consistente del servidor completo: mundo, datos de economía,
   configuraciones y JAR. Una copia solo de `claims.json` no es una recuperación
   completa de una operación que también modifica una billetera/inventario.
3. Usar Java 21 y el JAR beta.2 junto con Gson de las bibliotecas del servidor:

```text
java -cp <zianmanager.jar>:<gson.jar> com.ianblk.zianmanager.reward.RewardStorageMaintenance migrate-offline <mundo>/data/zianmanager/claims.json
java -cp <zianmanager.jar>:<gson.jar> com.ianblk.zianmanager.core.KeyStorageMaintenance migrate-offline <mundo>/data/zianmanager/keys.json
```

En Windows el separador del classpath es `;`. Los parámetros de rutas con espacios
deben entrecomillarse. Las herramientas también pueden inicializar un registro
nuevo sin recibos anteriores.

Cada migración verifica los archivos de jugador y crea una copia byte a byte del
JSON original (`.archive-<uuid>`). Solo entonces sustituye el JSON raíz por un
manifiesto que apunta al directorio nuevo. El manifiesto de recompensas es esquema
3; el de llaves es esquema 2. Los archivos de jugador conservan sus formatos
anteriores y todos sus recibos.

No arrancar versiones antiguas sobre los nuevos manifiestos. Para retroceder después
de nuevos pagos, usar una copia completa y consistente; restaurar solo un diario
viejo puede deshacer el recibo sin deshacer el pago. No borrar directorios de staging,
copias o marcadores `.maintenance` tras un corte sin revisar primero el estado.

## Diagnóstico

```text
python tools/storage-audit.py <mundo>
python tools/storage-audit.py <mundo> --deep
```

El modo normal informa tamaños y cantidades sin mostrar UUID ni contenido de
jugadores. `--deep` lee los archivos de jugador para contar recibos y pendientes;
conviene ejecutarlo sobre una copia offline cuando el mundo sea grande. Advierte
cuando un archivo o contador se acerca al 80 % de su límite. No repara ni elimina datos.

Los archivos por jugador aún crecen con su historial individual. La separación y
la caché acotada evitan un único registro global enorme, pero no constituyen una
política de borrado ni una garantía de crecimiento ilimitado. Conservar las copias
de auditoría y vigilar los límites individuales antes de programar mantenimiento.

## Ajuste visual

El HUD muestra `Tiempo Ilimitado`; título, cuenta regresiva y estado comparten el
margen izquierdo `x + 8` del recuadro. No cambia el consumo de tiempo por mundo.
