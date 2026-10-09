# Tiempo diario de dungeons — alpha.17

En `/zianmanager`, abre **Tiempo diario de dungeons**. Los límites se dejan desactivados hasta que selecciones tus mundos; no se adivinan nombres de Multiverse.

## Configurar

1. Desde tu dungeon puedes pulsar **Añadir mundo actual**. También, en Mundos escribe los nombres exactos de los mundos de Multiverse o los identificadores de dimensión, separados por comas. Incluye todas las dimensiones de dungeon y excluye el mundo de spawn.
2. Activa los límites y guarda. La zona horaria inicial es `America/Guayaquil`: el saldo diario se renueva a las 00:00 UTC -5.
3. DEFAULT comienza con 30 minutos de lunes a viernes y 90 minutos sábado/domingo. VIP comienza con 120 y 180 minutos. Todos los valores son editables y se comparten entre las dungeons seleccionadas.
4. Concede al rango VIP el permiso configurado, inicialmente `zianmanager.dungeon.vip`. Los jugadores sin ese permiso usan DEFAULT. OP y el permiso `zianmanager.dungeon.bypass` tienen tiempo ilimitado.
5. En Rangos añade perfiles adicionales: nombre, permiso, minutos entre semana y minutos de fin de semana. Aplicar rangos y después Guardar cambios. Se usa el mayor límite permitido, sin sumar los límites de los rangos.
6. En Salida el comando inicial es `spawn {player}`, ejecutado desde consola para usar el spawn de EternalCore. Puedes escribir `eternalcore:spawn {player}` si necesitas evitar conflictos de alias.
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

Se observa el mundo actual, no solo un comando de entrada: TPA, Waystones, portales y otros teleports quedan sujetos al mismo saldo. Se comprueba al cambiar de dimensión y cada segundo como respaldo para servidores híbridos. Al agotarse se ejecuta la salida desde consola. Una reentrada sin saldo vuelve a activar la salida. Durante una entrada agotada no se activan encuentros ni se abre loot de cofres.

El tiempo usado se guarda cada segundo en el mundo, junto a configuración, bonos y registro del anuncio. Solo consume tiempo mientras el jugador está conectado dentro de un mundo configurado. OP o bypass no consumen. Salir, volver a entrar, cambiar de dungeon o reiniciar no crea un saldo nuevo. Un fallo persistente de escritura devuelve al jugador en lugar de seguir contando sin guardar.

El texto inicial de agotamiento es: Se te acabó el tiempo por el día de hoy. Tu tiempo se renovará a las 00:00 UTC -5.

## Compatibilidad y límites

Los nombres de mundos y el comando se adaptan mediante Bukkit cuando existe; en NeoForge puro se usan identificadores de dimensión y el dispatcher del servidor. Multiverse, LuckPerms y EternalCore no son dependencias obligatorias para arrancar el mod.

La prueba local usa NeoForge con Lootr y un comando de teleportación equivalente. La combinación concreta de Youer, Multiverse, EternalCore y Waystones debe comprobarse en el servidor de pruebas: deben funcionar el comando de spawn desde consola y los permisos del rango. Si EternalCore configura demora o cancelación de teleportación, su comportamiento también se conserva; el mod comprueba el mundo y reintenta cuando el jugador sigue dentro.

No se ha instalado ni modificado nada en el servidor real.

Referencias de integración: [mundos y claves de Multiverse](https://mvplugins.org/core/how-to/customise-world-creation/), [comandos y permisos de EternalCore](https://eternalcode.pl/projects/eternalcore), [permisos de LuckPerms](https://luckperms.net/wiki/Permission-commands).

## Temporizador movible

En Tiempo diario de dungeons → Pantalla activa Mostrar temporizador y pulsa Mover temporizador. Arrastra la tarjeta y guarda su posición. También puedes ajustar los porcentajes X/Y de 0 a 100; se adaptan a la resolución y escala de GUI. La posición se guarda en la configuración del servidor.

La tarjeta solo aparece dentro de los mundos configurados mientras el límite diario está habilitado. Muestra HH:MM:SS; OP y bypass ven Tiempo ilimitado. Se oculta al salir, desconectarse o desactivarse. El editor permite una vista previa para colocarla incluso fuera de una dungeon. El saldo proviene del servidor y el cliente solo suaviza la cuenta atrás.
