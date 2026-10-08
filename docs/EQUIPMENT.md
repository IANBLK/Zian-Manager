# Objetos, armas, cofres y zonas — alpha.4

## Pestaña creativa

La pestaña **Zian Manager** contiene el cofre original y los 40 modelos importados: 4 martillos, 3 armas de fantasía, 4 llaves, 9 cofres, 2 accesorios y 18 objetos del paquete Altar. Todos usan IDs propios `zianmanager:`; no reemplazan objetos, sonidos ni interfaces de Minecraft.

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
- Los emblemas, fragmentos, reloj, flecha ornamental, arnés y altar son objetos decorativos, sin ataques especiales. No incorporan sistemas de invocación ni funciones de Minecraft 1.21.11.
- Sombrero de paja (`straw_hat`): se equipa en la cabeza, sin armadura adicional. Escudo ornamental (`shield1`): bloqueo normal de escudo.

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

Todos llevan prefijo `zianmanager:`. La llave puede estar en el inventario; no tiene que estar en la mano. Una llave incorrecta no abre el cofre. Abrir la vista previa no consume la llave ni cambia el sorteo. **Recibir recompensas** consume una llave, confirma el inventario guardado y entrega el loot. Una reclamación pagada no consume otra llave. Si falta espacio antes de comenzar, se conserva la llave y el loot pendiente.

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
