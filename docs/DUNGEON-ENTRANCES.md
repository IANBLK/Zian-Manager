# Entradas aleatorias de dungeon — Manager beta.6

En el menú administrativo de Zian Manager, abre **Entradas de dungeon** y crea una dungeon. La referencia identifica el conjunto y permanece fija; el nombre visible puede cambiar. No es necesario crear un mundo por dungeon.

1. Sitúate sobre suelo firme en el primer lugar de llegada.
2. Abre la dungeon, entra en **Puntos**, escribe un nombre y pulsa **Añadir punto aquí**. El servidor captura mundo, posición y orientación.
3. Ve al segundo lugar y repite. Puedes guardar hasta 32 puntos por dungeon y administrar hasta 64 dungeons.
4. En el NPC, abre **Acción**, selecciona **Dungeon de llegada aleatoria**, escribe el texto del botón y guarda. El botón principal ahora usa ese conjunto de entradas.

Para actualizar la distribución, selecciona un punto guardado y usa **Mover punto aquí**, o quítalo con confirmación. Puedes añadir o quitar puntos sin modificar los NPC vinculados. Cambiar el nombre visible no altera el vínculo. Si se elimina o cambia la identidad del mundo, vuelve a capturar sus puntos en el mundo correcto. La referencia de dungeon no depende de su nombre visible ni del nombre del mundo.

El servidor elige al azar entre los puntos que siguen siendo seguros. Comprueba que el mundo exista, el destino esté dentro del borde y haya suelo firme y espacio libre sin líquidos ni peligros básicos. Si ninguno está disponible, no traslada al jugador. La entrada respeta las restricciones existentes de tiempo diario y requiere bajar de la montura. No busca coordenadas arbitrarias fuera de los puntos configurados.

Solo administradores configuran puntos. Los jugadores pueden usar el NPC conforme al permiso habitual. La selección procede de la configuración del servidor, no de coordenadas enviadas por el cliente. Los usos conservan el diario y la espera por jugador del NPC. Un traslado no confirmado requiere revisión antes de volver a ejecutarlo. No se puede eliminar una dungeon mientras algún NPC la referencia.

Los puntos se guardan mediante el almacenamiento habitual de Manager: documento lógico `data/zianmanager/dungeon_entrances.json` en SQLite cuando está habilitado. No cambia el formato existente de NPC; los comandos normales continúan funcionando. Esta versión requiere actualizar Manager en cliente y servidor por la nueva versión de su canal.
