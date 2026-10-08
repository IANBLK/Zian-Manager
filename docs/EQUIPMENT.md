# Objetos, armas, cofres y zonas — alpha.9

## Pestaña creativa

La pestaña **Zian Manager** contiene los 27 modelos activos: 4 martillos, 3 armas de fantasía, 4 llaves, 9 cofres y 7 armas/herramientas del paquete Altar. Todos usan IDs propios `zianmanager:`; no reemplazan objetos, sonidos ni interfaces de Minecraft.

No se añaden recetas: están destinados al editor de loot, comandos y creativo.

## Armas, martillos y encantamientos

Los valores de daño siguientes son **daño base total**, antes de encantamientos y efectos de Fuerza.

- **Lanza Ígnea** (`flame_spear`): 7 de daño. Bola de fuego con clic derecho, espera **1 s**. Encantamientos de espada.
- **Bastón del Vacío** (`void_staff`): 8 de daño. Ataque sónico con clic derecho, espera **10 s**. Encantamientos de espada.
- **Espada del Gladiador** (`gladiator_sword`): **10 de daño**. Conserva Regeneración I, Absorción I y Fuerza I durante 30 s. Espera **90 s**. Encantamientos de espada.
- **Espada Ancestral** (`altar_ancientblade`): **9 de daño**. Regeneración I por 30 s y **10 corazones temporales** de absorción por 30 s (Absorción V). Espera **90 s**. Encantamientos de espada.
- **Filo del Dragón** (`altar_dragonrend`): **9 de daño**. Clic derecho: Fuerza I y Absorción I por 30 s. Espera **90 s**. Encantamientos de espada.
- **Filo Marchito** (`altar_withersym`): **9 de daño**. Cada golpe cuerpo a cuerpo confirmado aplica Wither I por 5 s. Clic derecho: Fuerza II por 30 s. Espera **90 s**. Encantamientos de espada.
- **Lanza del Presagio** (`altar_omen`): **10 de daño**, mantiene el ID para datos existentes. Clic derecho: Regeneración I y Absorción II por 30 s; ya no da visión nocturna. Espera **90 s**. Encantamientos de espada.
- **Tridente Rompeolas** (`altar_tide`): **9 de daño cuerpo a cuerpo**, lanzamiento de tridente y encantamientos de tridente, como Lealtad, Empalamiento, Canalización y Propulsión. Clic derecho: Regeneración I y Fuerza I por 30 s, espera **90 s**. Mantener y soltar clic derecho permite lanzarlo; la espera de la habilidad no bloquea los lanzamientos. Se conserva el modelo original del objeto y del proyectil.
- **Martillos** (`blue_hammer`, `green_hammer`, `grey_hammer`, `red_hammer`): nivel diamante, minería **3×3×1** y encantamientos de pico. **Shift** mina solo un bloque. La rotura normal del servidor respeta protecciones, drops y durabilidad de cada bloque.
- **Hacha y pico de amatista** (`altar_amaxe`, `altar_ampick`): atributos de diamante y encantamientos normales de hacha/pico.

Todas las habilidades que antes esperaban 120 s ahora esperan **90 s**. Las duraciones de efecto de 30 s se conservan. Compatibilidad e incompatibilidad entre encantamientos, disponibilidad en mesa/yunque y requisitos de Propulsión siguen las reglas normales de Minecraft. Las habilidades se ejecutan en el servidor y las esperas se guardan por jugador y tipo de habilidad.

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

El sorteo queda registrado antes de entregar. Una reclamación pagada no consume otra llave. La entrega confirma el inventario guardado y, si hay sobrantes, guarda las entidades del mundo antes de completar el registro. Volver a hacer clic durante la renovación no duplica la entrega. En los cofres con llave, la opción **Sin tiempo de reutilización** permite un nuevo sorteo inmediato con otra llave. Los cofres sin llave conservan renovación configurable de al menos un minuto.

Una interrupción ambigua pausa el consumo para revisión; no se descuenta otra llave automáticamente. Después de verificar o compensar manualmente, el administrador puede confirmar el coste:

```text
/zianmanager resolvekey <jugador conectado> <UUID de reclamación> <evidencia>
```

Este comando no crea objetos ni llaves; confirma la revisión de su consumo. Los cofres quedan excluidos de la conversión de Lootr.

## Zonas desde un centro

1. Abre **Zonas de dungeon → Crear nuevo** y configura su nombre y equipo.
2. Colócate sobre el bloque que quieres como centro y, en **Ubicación**, pulsa **Crear zona de dungeon**. Se utiliza el bloque bajo tus pies.
3. En **Tamaño**, ajusta los bloques a cada lado en **X** y **Z**, los bloques por encima y por debajo. Puedes escribir valores o usar **− / +**. Radio 3 significa 3 bloques por lado más el central: **7×7**.
4. La vista se actualiza mientras editas los valores válidos. **Aplicar tamaño** guarda el área; **Cerrar y ver zona** permite caminar y observarla. El HUD distingue una vista previa sin guardar.
5. El centro aparece en dorado, el contorno en azul y las apariciones de mobs con marcadores. Elige de 1 a 8 tipos de mob desde General y activa la zona: no necesitas puntos manuales. El primer jugador inicia 1–3 mobs; cada jugador adicional añade dos, hasta ocho por oleada.

Cambiar el tamaño conserva el centro. El centro queda bloqueado después de crearlo; el botón pasa a **Centro fijado**. Volver a solicitar la creación conserva ese centro. Para una ubicación distinta, crea otra zona. Un encuentro activo debe detenerse antes de editar el área. Los mobs buscan suelo firme y espacio libre dentro del área; si falta espacio pueden aparecer menos. **Mostrar / ocultar** conserva el control de la guía, que se muestra en su dimensión durante diez minutos.

Las zonas anteriores mantienen sus límites y progreso al cargar. Puedes convertirlas al modo centrado creando el centro o aplicando tamaño; no se redimensionan automáticamente al actualizar el mod.

## Modelos y validación

Las rotaciones incompatibles con el formato de cubos de 1.21.1 se convierten a geometría OBJ. Se conservan UV, pivotes y poses del objeto; las texturas se extraen sin cambiar sus bytes. Los cofres usan un renderer propio que conserva jerarquía y canales de animación, sin requerir GeckoLib. Se normalizan uniformemente a la escala de un bloque para mantener sus proporciones.

Las pruebas locales verifican daño, habilidades, espera, martillo, llaves y persistencia. El aspecto, las animaciones y la vista de zonas requieren confirmación visual en el cliente del usuario. Los paquetes Altar usan un formato original más reciente; se importan sus modelos como objetos propios, no sus cambios globales de interfaz o los sistemas de CustomModelData originales.

## Compatibilidad del cofre retirado

El cofre original ya no se ofrece en creativo ni en visores que respetan la etiqueta de ocultación. Los bloques antiguos pasan a `loot_common_crate` y los objetos antiguos del inventario se convierten al entrar. Se mantiene únicamente su ID interno de compatibilidad para evitar perder datos de mundos anteriores; no se incluye su textura antigua. UUID, loot y esperas de los cofres configurados se conservan.

## Objetos retirados y aviso de loot

Se retiran `altar_void1`, `altar_void2`, `altar_void3`, `altar_soul`, `altar_hotbarsym`, `altar_handle`, `altar_warden`, `altar_harness`, `altar_dragonheart`, `altar_clockdragonrend`, `altar_arrowdragonrend`, `straw_hat` y `shield1`. No se ofrecen en creativo ni en visores compatibles; el editor impide añadirlos y los sorteos nuevos ignoran entradas antiguas de esos objetos. Sus identificadores mínimos de compatibilidad mantienen legibles inventarios y reclamaciones ya guardadas, sin sus modelos ni habilidades anteriores.

Después de confirmar una entrega, el chat muestra cada objeto con su cantidad y destino: inventario o suelo. Las cantidades solo describen componentes confirmados; un clic repetido durante la renovación no vuelve a entregar ni anuncia otra entrega.

## Nombres y lore de fantasía

Las habilidades y estadísticas siguen funcionando. El tooltip usa un nombre de fantasía, el tipo de arma, dos líneas de historia y, si corresponde, **Reutilización: N s**; ya no enumera los efectos de la habilidad. Los nombres personalizados puestos por el jugador se conservan.

- Lanza Ígnea → **Ascua Eterna**.
- Bastón del Vacío → **Susurro del Abismo**.
- Gladiador → **Juramento de la Arena**.
- Ancestral → **Memoria de los Primeros**.
- Filo del Dragón → **Colmillo de Azhâr**.
- Filo Marchito → **Lamento del Ocaso**.
- Lanza del Presagio → **Augurio de la Noche**.
- Rompeolas → **Corona de las Mareas**.
- Martillos → **Zafiro**, **Arboleda**, **Bastión** y **Brasa**.
- Hacha/pico de amatista → **Hacha del Crepúsculo** y **Pico del Eco Cristalino**.

Los IDs siguen siendo los mismos para conservar inventarios, encantamientos y tablas. Los nombres en la sección de balance identifican el tipo y los IDs, aunque el juego muestre el nombre de fantasía.

## Varios botones en un NPC

En **Personajes → Acción** configura el botón principal y pulsa **Más botones…** para añadir hasta siete adicionales. Cada uno tiene texto, comando y espera por jugador. Usa **Aplicar botones** y luego **Guardar cambios**. Un comando principal vacío permite un diálogo con solo los adicionales.

El diálogo muestra las opciones en dos columnas y permite desplazar el texto. Cada botón ejecuta únicamente su comando guardado en el servidor; una sesión admite una pulsación. Los cooldowns son independientes por botón y jugador y persisten tras reiniciar. El botón **Restablecer usos** borra las esperas/revisiones de todas las opciones de ese NPC. Los NPC anteriores mantienen su comando y su espera principal.

La escala considera el máximo de jugadores simultáneos de cada oleada. Salir y volver a entrar no añade mobs extra; no elimina mobs ya creados cuando un jugador sale. Las configuraciones anteriores conservan sus puntos guardados, pero las nuevas apariciones usan el área automáticamente.
