# Bóvedas vanilla reutilizables — 0.1.1

Minecraft 1.21.1 / NeoForge. Compilada y probada con NeoForge 21.1.259 y Youer 4239b0d9. Se conserva el requisito anterior NeoForge 21.1.252 o superior.

Cada jugador tiene un tiempo independiente para cada bóveda, identificado por dimensión y coordenadas. Las normales esperan 30 minutos y las ominosas 60 por defecto. También se aplica a bloques vanilla de bóveda colocados por otros mods, como Legendary Monuments, conservando sus llaves y tablas de botín. Al reutilizarla vuelve a necesitar su llave correspondiente. Minecraft conserva el control de botín, consumo de llave, sonidos y animación. No se reinician estructuras ni generadores; no se modifican Pokémon ni NPC.

## Administración

- `/zianmanager vaults status`: estado y tiempos actuales.
- `/zianmanager vaults times normal 1`: cambia solo las normales a un minuto.
- `/zianmanager vaults times ominous 1`: cambia solo las ominosas a un minuto.
- `/zianmanager vaults times 1`: cambia ambas a un minuto. El formato anterior `times 30 60` sigue disponible.
- Tab sugiere tipos y minutos; `/zianmanager vaults` muestra ayuda.
- `/zianmanager vaults cooldown 1`: mirando una bóveda a un máximo de seis bloques, ajusta TU espera en esa bóveda a un minuto desde ahora. Requiere administración y no cambia otras bóvedas ni jugadores.
- `/zianmanager vaults reset` (también `cooldown 0`): termina explícitamente TU espera en la bóveda que miras, conservando el historial. No entrega botín ni consume llaves.
- `/zianmanager vaults enabled false`: volver al comportamiento vanilla. No borra el historial nativo ni los tiempos guardados.
- `/zianmanager vaults enabled true`: activar reutilización.
- `/zianmanager vaults reload`: recargar configuración.

Requiere el permiso administrativo existente `zianmanager.admin` o su alternativa OP/consola. Ser OP no salta los tiempos de las bóvedas; el modo creativo conserva su consumo de llaves vanilla.

Los cambios de duración afectan a aperturas nuevas. Un tiempo de espera ya iniciado mantiene su vencimiento original. Si una bóveda ya tenía al jugador en su historial vanilla antes de instalar esta función, su primera interacción con una llave válida inicia un tiempo de espera sin consumirla: vanilla no guarda la fecha original.

La configuración y las reservas viven lógicamente en `data/zianmanager/vault-cooldowns-settings.json` y `data/zianmanager/vault-cooldowns/`, dentro del mundo principal. Si Manager usa SQLite, se guardan en su base de datos existente; usa los comandos, no necesitas editar SQLite. Conserva y respalda estos datos junto al mundo. No borres el historial de tiempos para intentar reparar un error.

## Protección de entregas

Se guarda una reserva antes de ejecutar la apertura nativa. Las llaves incorrectas, bóvedas ocupadas y esperas activas no generan una apertura. Una tabla que no produce botín devuelve la reserva sin cobrar llave. Si ocurre una excepción durante efectos nativos, se conserva la reserva por precaución; no se repite una entrega incierta. Un fallo o corrupción del almacenamiento bloquea las aperturas controladas y queda registrado para revisar.

Durante la detección nativa se ocultan temporalmente solo los UUID con un tiempo ya vencido, restaurándolos al terminar, incluso ante excepciones. Así una bóveda inactiva puede volver a activarse para ese jugador sin borrar permanentemente el historial vanilla. La reserva persistente sigue bloqueando al jugador aunque el historial nativo de 128 UUID lo expulse.

## Validación

La función se prueba en un mundo desechable con red externa desactivada, los mismos Youer/Java del host y luego los 59 mods instalados. No incluye los plugins ni configuraciones del mundo de producción; no sustituye una comprobación jugable allí.

Para comprobar dentro del juego:

1. Instalar esta versión de Manager en el servidor de prueba y cliente, sustituyendo el JAR anterior, con copia y el servidor apagado.
2. Usar `/zianmanager vaults times 1 2` antes de abrir una bóveda. Verificar el botín y que intentar repetir consume cero llaves y muestra el tiempo restante.
3. Otro jugador puede usar la misma bóveda cuando termina su animación. Repetir en una ominosa con la llave ominosa.
4. Volver después del tiempo y comprobar una nueva apertura; desconectar/reconectar y reiniciar para comprobar el mensaje. Probar en supervivencia aunque seas OP.
5. Restaurar `/zianmanager vaults times 30 60`. Los tiempos cortos ya iniciados terminan normalmente; los siguientes usan los nuevos valores.

El propietario confirmó las cuatro comprobaciones jugables de la beta.2: ominosa, independencia entre jugadores, llaves/botín y permisos. También confirmó la persistencia de las esperas después de reiniciar. La versión 0.1.1 incorpora las mismas reglas de bóvedas y amplía el catálogo de skins; su presentación visual final debe comprobarse con el nuevo JAR en el cliente.

Implementación independiente, reutilizando el almacenamiento propio de Zian Manager. No incorpora código ni archivos de BetterTrialChambers.
