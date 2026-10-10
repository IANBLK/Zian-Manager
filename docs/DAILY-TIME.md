# Tiempo diario por mundo — alpha.22

En `/zianmanager`, abre **Tiempo diario de dungeons**. Los límites se dejan desactivados hasta que selecciones tus mundos; no se adivinan nombres de Multiverse.

## Configurar

1. Desde tu dungeon puedes pulsar **Añadir mundo actual**. También, en Mundos escribe los nombres exactos de los mundos de Multiverse o los identificadores de dimensión, separados por comas. Incluye todas las dimensiones de dungeon y excluye el mundo de spawn.
2. Activa los límites y guarda. La zona horaria inicial es `America/Guayaquil`: el saldo diario se renueva a las 00:00 UTC -5.
3. DEFAULT comienza con 30 minutos de lunes a viernes y 90 minutos sábado/domingo. VIP comienza con 120 y 180 minutos. Todos los valores son editables y se comparten entre las dungeons seleccionadas.
4. Concede al rango VIP el permiso configurado, inicialmente `zianmanager.dungeon.vip`. Los jugadores sin ese permiso usan DEFAULT. OP y el permiso `zianmanager.dungeon.bypass` tienen tiempo ilimitado.
5. En Rangos añade perfiles adicionales: nombre, permiso, minutos entre semana y minutos de fin de semana. Aplicar rangos y después Guardar cambios. Se usa el mayor límite permitido, sin sumar los límites de los rangos.
6. En Salida el valor inicial es `bed`: cama válida del jugador y, si falta, está bloqueada o pertenece a una dungeon limitada, spawn del mundo principal. Los botones **Cama → spawn** y **Spawn del mundo** permiten elegir; guarda los cambios. `worldspawn` usa siempre el spawn configurado con `/setworldspawn` en el mundo principal, respetando la búsqueda vanilla de aparición. Para EternalCore escribe `spawn {player}` o `eternalcore:spawn {player}`. Las configuraciones existentes conservan su comando; si no existe o falla, se usa cama/spawn como respaldo.
7. En Mensajes personaliza el aviso de tiempo agotado y el anuncio global de renovación, enviado una vez a las 00:01. El registro evita duplicarlo al reiniciar durante esa hora.

Permisos de ejemplo para LuckPerms:

```
/lp group vip permission set zianmanager.dungeon.vip true
/lp user Nombre permission set zianmanager.dungeon.bypass true
```

## Bonos del día

```
/zianmanager bonustime player Nombre 30
/zianmanager bonustime all 30
/zianmanager time
```

Los dos primeros comandos requieren administración. El bono personal se suma al saldo de ese jugador; el bono global también afecta a quienes entren más tarde ese día. Se conservan después de reiniciar y vencen al reinicio diario. El comando time muestra el saldo o que el jugador tiene tiempo ilimitado.

## Entrada y protección

Se observa el mundo actual, no solo un comando de entrada: TPA, Waystones, portales y otros teleports quedan sujetos al mismo saldo. Se comprueba al cambiar de dimensión y cada segundo como respaldo para servidores híbridos. Al agotarse se ejecuta la salida elegida: cama/spawn nativo o comando desde consola. Una reentrada sin saldo vuelve a activar la salida. Durante una entrada agotada no se activan encuentros ni se abre loot de cofres.

El tiempo usado se guarda cada segundo en el mundo, junto a configuración, bonos y registro del anuncio. Solo consume tiempo mientras el jugador está conectado dentro de un mundo configurado. OP o bypass no consumen. Salir, volver a entrar, cambiar de dungeon o reiniciar no crea un saldo nuevo. Un fallo persistente de escritura devuelve al jugador en lugar de seguir contando sin guardar.

El texto inicial de agotamiento es: Se te acabó el tiempo por el día de hoy. Tu tiempo se renovará a las 00:00 UTC -5.

## Compatibilidad y límites

Los nombres de mundos y el comando se adaptan mediante Bukkit cuando existe; en NeoForge puro se usan identificadores de dimensión y el dispatcher del servidor. Multiverse, LuckPerms y EternalCore no son dependencias obligatorias para arrancar el mod.

La prueba local usa NeoForge con Lootr y comprueba tanto la salida nativa por cama/spawn como el comando de teleportación equivalente. La combinación concreta de Youer, Multiverse, EternalCore y Waystones debe comprobarse en el servidor de pruebas: deben funcionar el comando de spawn desde consola y los permisos del rango. Si EternalCore configura demora o cancelación de teleportación, su comportamiento también se conserva; el mod comprueba el mundo y reintenta cuando el jugador sigue dentro.

No se ha instalado ni modificado nada en el servidor real.

Referencias de integración: [mundos y claves de Multiverse](https://mvplugins.org/core/how-to/customise-world-creation/), [comandos y permisos de EternalCore](https://eternalcode.pl/projects/eternalcore), [permisos de LuckPerms](https://luckperms.net/wiki/Permission-commands).

## Temporizador movible

En Tiempo diario de dungeons → Pantalla activa Mostrar temporizador y pulsa Mover temporizador. Arrastra la tarjeta y guarda su posición. También puedes ajustar los porcentajes X/Y de 0 a 100; se adaptan a la resolución y escala de GUI. La posición se guarda en la configuración del servidor.

La tarjeta solo aparece dentro de los mundos configurados mientras el límite diario está habilitado. Muestra HH:MM:SS; OP y bypass ven Tiempo ilimitado. Se oculta al salir, desconectarse o desactivarse. El editor permite una vista previa para colocarla incluso fuera de una dungeon. El saldo proviene del servidor y el cliente solo suaviza la cuenta atrás.

## Avisos y bloqueo previo — alpha.20

El jugador recibe un aviso al pasar por 5 minutos, 1 minuto y 30 segundos restantes. Si entra con poco saldo o el servidor salta varios umbrales por lag, se envía un único aviso con el saldo actual. Los avisos se guardan para evitar duplicarlos al reconectar o reiniciar. Un bono que eleva el saldo vuelve a habilitar los avisos correspondientes. OP/bypass no reciben estos avisos.

En Tiempo diario → Mensajes puedes cambiar **Aviso previo**, conservando `{time}` para insertar el tiempo restante. Las configuraciones antiguas reciben el texto inicial automáticamente.

Los viajes que pasan por `EntityTravelToDimensionEvent` se cancelan antes de entrar a una dimensión limitada si el jugador agotó el saldo. Salir de una dungeon sigue permitido. Se conserva la revisión al cambiar de dimensión y cada segundo: ciertos teleports Bukkit de Youer usan otra ruta, por lo que no se promete bloqueo previo para todos los plugins.

No requiere WorldTimeLimit, CommandAPI ni PlaceholderAPI. Se han adaptado las ideas del plugin compartido por el usuario al registro diario común de Zian Manager. La integración real con TPA, Multiverse y Waystones debe verificarse en Youer.

Penalización de respaldo: si un jugador sin exención permanece dentro de una dungeon limitada sin saldo, recibe Lentitud V, Oscuridad V y Debilidad V con duración infinita. Se retiran al salir, al recuperar saldo por renovación diaria o bono, al obtener exención o al desactivar los límites. Una limpieza como leche no evita que se vuelvan a aplicar en la siguiente revisión. Los efectos ajenos se conservan mediante la cadena de efectos ocultos de Minecraft, con su duración restante.

## Tiempos independientes por mundo (alpha.22)

En `/zianmanager` → **Tiempos por mundo** se editan Dungeon, Nether, End y Farmeo. Cada perfil guarda su configuración, consumo diario, avisos y prórrogas en archivos separados. Los límites, permisos de rango, mensajes, salida y posición/nombre del temporizador se pueden cambiar en su formulario. Guarda para aplicar sin reiniciar.

Nether, End y Farmeo se configuraron en Rassvet con 180 minutos para Aventurero y 240 para Explorador, Guardián y Astral, todos los días. Cada mundo tiene su propio saldo; no comparte consumo con dungeon. Spawn y Casas quedan fuera de los perfiles limitados. El reinicio diario sigue a las 00:00 America/Guayaquil y el anuncio a partir de las 00:01. OP y el permiso de bypass configurado conservan tiempo ilimitado.

Consulta pública: `/rassvet tiempo` o el botón **Mis tiempos diarios** de ZianGUI. La consulta nativa es `/zianmanager time [dungeon|nether|end|farmeo]`; en Youer se recomienda el acceso Rassvet por sus permisos específicos.

Comandos administrativos:

- `/zianmanager worldtime nether bonus <jugador> <minutos>`
- `/zianmanager worldtime farmeo bonus all <minutos>`
- `/zianmanager worldtime end remove <jugador> <minutos>`

Los comandos anteriores `/zianmanager bonustime ...` y `/zianmanager removetime ...` siguen afectando únicamente dungeon. Las prórrogas vencen al reinicio diario. No se permite asignar un mundo simultáneamente a dos perfiles activos. La salida nativa rechaza camas/spawns dentro de cualquiera de los perfiles limitados; en Rassvet la salida se configuró como `mv tp {player} world --unsafe`.

Verificación: 78 pruebas unitarias y prueba local con Lootr; aislamiento de consumo y bonos, recarga de registros, guardas de destino, exención OP y penalizaciones que no se borran por actualizar un mundo diferente. Los registros previos de dungeon se conservan.
