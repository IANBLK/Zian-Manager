# Objetos, armas, cofres y zonas — alpha.6

## Pestaña creativa

La pestaña **Zian Manager** contiene los 27 modelos activos: 4 martillos, 3 armas de fantasía, 4 llaves, 9 cofres y 7 armas/herramientas del paquete Altar. Todos usan IDs propios `zianmanager:`; no reemplazan objetos, sonidos ni interfaces de Minecraft.

No se añaden recetas: están destinados al editor de loot, comandos y creativo.

## Armas y martillos

- `flame_spear`: daño base 7, equivalente a espada de diamante. Clic derecho: pequeña bola de fuego de 5 de daño, quema entidades como la del blaze; no incendia bloques. Reutilización: **15 s**.
- `void_staff`: daño base 8, equivalente a espada de netherita. Clic derecho: ataque sónico de 10 de daño a un objetivo en la dirección de la mirada, alcance 15 bloques; atraviesa paredes y usa la fuente de daño del warden. Respeta PvP y daño cancelado. Reutilización: **30 s**.
- `gladiator_sword`: daño base 8. Clic derecho: Regeneración I, Absorción I y Fuerza I durante **30 s**. Reutilización: **120 s**.
- `blue_hammer`, `green_hammer`, `grey_hammer`, `red_hammer`: herramienta de nivel diamante, minería **3×3×1** perpendicular a la cara golpeada. **Shift** mina solo un bloque. Cada bloque pasa por la rotura normal del servidor, consume su durabilidad y produce sus drops normales; no rompe bloques con entidad de bloque ni bloques que la herramienta no puede cosechar.

Las habilidades se calculan en el servidor y sus esperas se guardan por jugador y tipo de habilidad. Cambiar de copia del arma, volver a entrar o morir no reinicia deliberadamente la espera. La minería del martillo es su función habitual, sin habilidad de clic derecho.

## Objetos Altar con balance moderado

- Espada ancestral (`altar_ancientblade`): nivel diamante, Regeneración I durante 5 s, espera 120 s.
- Espada del presagio (`altar_omen`): nivel diamante, Visión nocturna I durante 30 s, espera 120 s.
- Rompeolas (`altar_tide`): nivel diamante, Gracia del delfín I durante 10 s, espera 120 s.
- Filo del dragón (`altar_dragonrend`) y Filo marchito (`altar_withersym`): espadas de diamante, sin ataques adicionales.
- Hacha y pico de amatista (`altar_amaxe`, `altar_ampick`): atributos de sus equivalentes de diamante.

## Cofres de loot

Colócalos desde creativo, usa **Shift + clic derecho** y elige tabla y renovación. Comparten las reglas de loot personal y recuperación del cofre original. Se protegen en supervivencia y un administrador puede retirarlos en creativo.

**Apertura directa — Crates and Stuff:**

```text
zianmanager:loot_common_crate
zianmanager:loot_rare_crate
zianmanager:loot_legendary_crate
zianmanager:loot_cosmetic_crate
zianmanager:loot_vote_crate
```

**Apertura con llave — paquete animado:**

```text
locked_common_crate     → common_key
locked_rare_crate       → rare_key
locked_epic_crate       → epic_key
locked_legendary_crate  → legendary_key
```

Todos llevan prefijo `zianmanager:`. La llave puede estar en el inventario; no tiene que estar en la mano. Una llave incorrecta no abre el cofre. **Clic derecho consume una llave y entrega el loot directamente**, sin una pantalla que tape la animación. Los objetos que no caben en el inventario aparecen al suelo junto al jugador, incluso en creativo; quedan reservados para ese jugador mediante la propiedad nativa de dueño del objeto.

El sorteo queda registrado antes de entregar. Una reclamación pagada no consume otra llave. La entrega confirma el inventario guardado y, si hay sobrantes, guarda las entidades del mundo antes de completar el registro. Volver a hacer clic durante la renovación no duplica la entrega.

Una interrupción ambigua pausa el consumo para revisión; no se descuenta otra llave automáticamente. Después de verificar o compensar manualmente, el administrador puede confirmar el coste:

```text
/zianmanager resolvekey <jugador conectado> <UUID de reclamación> <evidencia>
```

Este comando no crea objetos ni llaves; confirma la revisión de su consumo. Los cofres quedan excluidos de la conversión de Lootr.

## Vista de zonas

En **Zonas → Ubicación** marca las dos esquinas y los puntos de aparición. **Cerrar y ver zona** permite caminar viendo el contorno azul, el **punto 1 verde**, el **punto 2 rojo** y los **mobs dorados**, con coordenadas. **Mostrar / ocultar zona** activa o desactiva la guía. Solo se muestra la última zona que seleccionaste, en su dimensión, durante diez minutos; volver al editor la actualiza.

## Modelos y validación

Las rotaciones incompatibles con el formato de cubos de 1.21.1 se convierten a geometría OBJ. Se conservan UV, pivotes y poses del objeto; las texturas se extraen sin cambiar sus bytes. Los cofres usan un renderer propio que conserva jerarquía y canales de animación, sin requerir GeckoLib. Se normalizan uniformemente a la escala de un bloque para mantener sus proporciones.

Las pruebas locales verifican daño, habilidades, espera, martillo, llaves y persistencia. El aspecto, las animaciones y la vista de zonas requieren confirmación visual en el cliente del usuario. Los paquetes Altar usan un formato original más reciente; se importan sus modelos como objetos propios, no sus cambios globales de interfaz o los sistemas de CustomModelData originales.

## Compatibilidad del cofre retirado

El cofre original ya no se ofrece en creativo ni en visores que respetan la etiqueta de ocultación. Los bloques antiguos pasan a `loot_common_crate` y los objetos antiguos del inventario se convierten al entrar. Se mantiene únicamente su ID interno de compatibilidad para evitar perder datos de mundos anteriores; no se incluye su textura antigua. UUID, loot y esperas de los cofres configurados se conservan.

## Objetos retirados y aviso de loot

Se retiran `altar_void1`, `altar_void2`, `altar_void3`, `altar_soul`, `altar_hotbarsym`, `altar_handle`, `altar_warden`, `altar_harness`, `altar_dragonheart`, `altar_clockdragonrend`, `altar_arrowdragonrend`, `straw_hat` y `shield1`. No se ofrecen en creativo ni en visores compatibles; el editor impide añadirlos y los sorteos nuevos ignoran entradas antiguas de esos objetos. Sus identificadores mínimos de compatibilidad mantienen legibles inventarios y reclamaciones ya guardadas, sin sus modelos ni habilidades anteriores.

Después de confirmar una entrega, el chat muestra cada objeto con su cantidad y destino: inventario o suelo. Las cantidades solo describen componentes confirmados; un clic repetido durante la renovación no vuelve a entregar ni anuncia otra entrega.
