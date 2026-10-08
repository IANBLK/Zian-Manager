# 🏰 Zian Manager

**Administra dungeons dentro del juego:** mobs personalizados, jefes, zonas con oleadas, loot personal y NPC de diálogo.

**0.1.0-alpha.4 · Minecraft 1.21.1 · NeoForge 21.1.252 · Java 21**

Instala el mismo JAR en cliente y servidor. Es independiente de Cobblemon, RCT API y RCT Mod. Lootr y LuckPerms son opcionales.

## 🛠️ Nuevo en alpha.4

Pestaña creativa **Zian Manager**, 40 modelos importados, martillos 3×3×1, armas con habilidades y esperas, nueve variantes de cofre con loot personal (cuatro requieren llave), y vista de esquinas/contorno/puntos de aparición. [Consulta la guía de objetos y zonas](docs/EQUIPMENT.md).

## ✨ Funciones

- Plantillas de mobs: entidad, nombre, vida, daño, armadura, dureza, resistencia al empuje, efectos y seis ranuras de equipo. Conservan la IA original.
- Jefes con barra de vida y loot configurable.
- Zonas con **3 a 6 mobs por oleada**, hasta 16 oleadas, pausa y reaparición configurable.
- Tablas **MOB, BOSS y CHEST**, con selección ponderada sin repetir entradas.
- Cofre negro, plateado y dorado: **sin receta**, protegido contra rotura en supervivencia, explosiones y empuje. Loot renovable por jugador, sin inventario accesible a tolvas.
- NPC humano Alex/slim, diez skins y diálogo con botón de comando configurable.
- Configuraciones, encuentros y entregas persistentes.

## 🎮 Configuración fácil

Abre `/zianmanager`. El inicio muestra **Mobs y jefes**, **Tablas de recompensas**, **Zonas de dungeon**, **Cofres de dungeon** y **Personajes y diálogos**.

### Recompensas: editor visual como el gacha

1. Abre **Tablas de recompensas → Crear nuevo**. La referencia se genera automáticamente; puedes cambiarla antes de crear la tabla.
2. Elige **Mobs**, **Jefes** o **Cofres** y usa **− / +** para indicar cuántos objetos se sortean en cada entrega.
3. Sostén un objeto y pulsa **Añadir objeto que tengo en la mano**. Se copian sus componentes, incluidos encantamientos; el objeto de tu mano no se consume.
4. La lista muestra su icono, nombre, porcentaje y cantidad. Los botones **− / +** de cada fila ajustan su probabilidad relativa. El porcentaje se recalcula entre todas las entradas.
5. Pulsa el objeto para ajustar cantidad mínima/máxima; usa **Quitar** para eliminar esa entrada, con confirmación.
6. **Opciones** conserva el uso de una tabla externa; es opcional y sustituye el sorteo de entradas propias.

### Mobs, zonas y personajes

- **Mobs y jefes:** pestañas **Básico**, **Combate**, **Equipo**, **Efectos** y **Avanzado**. Elige entidad y recompensas con selectores. Para equipar, elige la ranura y copia el objeto en mano. El selector de efectos permite elegir nombre, nivel y duración; el formato manual sigue disponible en Avanzado. La vista previa dura 20 segundos, es inmóvil y no da loot.
- **Zonas:** pestañas **General**, **Ubicación** y **Oleadas**. El selector de equipo añade de 3 a 6 mobs, incluso repetidos. Marca las esquinas y un punto libre por mob desde tu posición. Configura pausas, reaparición y recompensa de finalización. Activa la zona cuando esté lista. Puedes probar o detener el encuentro desde Oleadas.
- **Personajes:** pestañas **Apariencia**, **Diálogo** y **Acción**. Elige la skin, escribe el diálogo en varias líneas y configura el botón/comando opcional. Puedes mover el personaje a tu posición y restablecer sus usos después de revisar entregas.
- **Cofres:** el botón **Obtener cofre** evita escribir otro comando. Colócalo y usa **Shift + clic derecho**; selecciona la tabla y su renovación.

Los ID se generan para los mobs nuevos. Las referencias admiten minúsculas, números y guion bajo, hasta 32 caracteres. Una referencia guardada se mantiene estable. Los ajustes se guardan por sección y la interfaz conserva la pestaña al recibir la respuesta. Las selecciones de equipo y efectos requieren **Aplicar** y después **Guardar cambios**.

Las oleadas comparten composición en esta alpha. Varios jugadores participan en un encuentro compartido, sin duplicar mobs. Creativo y espectador no activan las zonas automáticamente; **Probar encuentro** permite hacerlo como administrador. El loot de mob se entrega al jugador que lo mata; el de finalización a participantes conectados que siguen dentro de la zona. El loot personalizado sustituye los objetos normales del mob y se entrega al inventario, no al suelo.

## 🎲 Tablas y probabilidades

Tus ejemplos están cubiertos:

- **Mob:** 10 entradas, sortear **2**.
- **Boss:** 10 entradas, sortear **5**.
- **Cofre:** 20 entradas, sortear **10**.

El **peso** expresa una probabilidad relativa: peso 20 tiene el doble de probabilidad que peso 10. La interfaz muestra el porcentaje **del primer sorteo**. Después de cada elección se recalculan los porcentajes entre las entradas restantes; no son probabilidades independientes y fijas de caída.

Los sorteos cuentan **entradas/stacks**, no unidades: una entrada puede entregar tres diamantes. Cada entrada elegida produce una cantidad entre mínimo y máximo. Máximo 64 entradas y 32 sorteos por tabla; si faltan entradas, se entregan las disponibles. Una entrada no se repite, pero añadir dos entradas del mismo objeto permite obtenerlo dos veces.

Sin entradas propias puedes indicar una tabla nativa de **cofre**, por ejemplo `minecraft:chests/simple_dungeon`. Se limita la salida generada, que puede contener menos stacks que el límite. Esta alpha no ofrece el contexto completo de tablas nativas de entidades: para mobs y jefes usa las entradas propias del editor.

## 🗝️ Cofres personales

Usa `/zianmanager givechest`, coloca el cofre y **Shift + clic derecho** para asignarle tabla y renovación en minutos. Cada jugador tiene su propio sorteo y tiempo.

El jugador abre la pantalla y pulsa **Recibir todo**. Abrir reserva el sorteo: cerrar y reabrir no cambia los objetos. El tiempo empieza al completar la entrega. Si falta espacio, el loot pendiente se conserva:

```text
/zianmanager pending
/zianmanager claim <UUID>
```

Para retirar el cofre usa **Eliminar confirmado** en el editor o rómpelo en creativo con `zianmanager.admin`. Se elimina su registro y no reaparece. Un cofre registrado se restaura si desaparece mientras su zona está cargada; herramientas externas de edición de mundo o quitar el mod quedan fuera de esta protección.

**Lootr:** se utiliza un bloque propio y un diario personal propio, excluidos mediante sus etiquetas públicas de conversión. Los cofres normales de Lootr siguen usando Lootr. No es una extensión de su inventario.

## 💬 NPC, skins y acciones

Las skins suministradas usan el modelo **Alex/slim** de brazos delgados:

1. Heraldo real
2. Guardián celeste
3. Centinela sombrío
4. Caballero infernal
5. Guardián abisal
6. Maga prismática
7. Paladín dorado
8. Explorador de bronce
9. Guardiana celestial
10. Hechicera aurora

El editor permite seleccionar skin, texto del diálogo, nombre del botón, comando y espera en segundos por jugador (0 permite repetir). El comando se configura sin barra inicial y se ejecuta con autoridad del servidor al pulsar el botón. Usa `{player}` para el nombre del jugador; solo los administradores configuran el comando. Ejemplos:

```text
minecraft:tp {player} 100 70 -200
minecraft:give {player} minecraft:diamond 1
```

Un comando vacío deja solo el diálogo. Las sesiones solo permiten una pulsación y verifican la cercanía al NPC. El diario conserva la espera después de reiniciar. Si una ejecución queda ambigua o devuelve fallo, se bloquea para revisión y no se repite sola; revisar el resultado antes de compensar. El botón administrativo **Reiniciar usos**, con confirmación, borra las esperas y revisiones de ese NPC.

## Permisos

En **npcs**, crea un NPC en tu posición, configura nombre y texto. Clic derecho abre el diálogo y el botón configurado. Escribe `\n` para saltos de línea. Puedes moverlo a tu posición o eliminarlo desde el editor. Los NPC antiguos se convierten al modelo humano manteniendo posición y texto.

- `zianmanager.admin`: editor y administración; sin LuckPerms requiere OP de nivel 2.
- `zianmanager.npc`: conversar y pulsar acciones; permitido por defecto.
- `zianmanager.loot`: abrir y recibir loot; sin una regla explícita se permite a los jugadores.

LuckPerms es opcional. Una denegación explícita también se respeta para OP. Los jugadores no necesitan OP.

## 💾 Persistencia y revisión

Respalda `<mundo>/data/zianmanager/` junto al mundo. Un archivo ilegible se conserva y deshabilita el sistema para evitar sobrescribirlo.

Las entregas registran intención y confirmación por componente. Una entrega ambigua después de un cierre requiere revisión y no se repite automáticamente. Después de verificar o compensar manualmente, un administrador puede cerrar el componente con evidencia:

```text
/zianmanager resolve <jugador conectado> <UUID entrega> <componente 1–32> <evidencia>
```

Esto confirma la revisión; no genera objetos de nuevo. Un encuentro con un mob desaparecido se pausa: revisa la causa, cancela explícitamente desde el editor y vuelve a habilitar la zona. Cancelar deshabilita la zona y retira los mobs.

## 🧪 Validación y límites

32 pruebas automáticas de selección, persistencia y entregas. Pruebas locales de servidor dedicado con dos jugadores simulados, oleadas compartidas, reaparición, NPC, protección del cofre, loot personal y reinicio. También probado con **Lootr 1.11.37.122**.

Falta una prueba visual en tu cliente. Esta alpha no está validada en Youer ni con Cataclysm. El selector permite entidades instaladas que hereden de `Mob`, pero sus ataques, fases y atributos especiales pueden requerir adaptaciones. El cofre es un modelo estático, sin animación de tapa.

## 🛠️ Desarrollo

```text
./gradlew build
python tools/manager-smoke.py
```

La prueba usa un mundo aislado bajo `build/`, localhost y puertos 25586/25587. Define `ZIANMANAGER_WITH_LOOTR=true` para incluir Lootr desde el primer arranque. La segunda pasada verifica el reinicio con Lootr. Requiere Java 21 y acceso a las dependencias.

[Procedencia](docs/REUSE.md) · [Textura y prompt](art/PROMPT.md) · MIT © IANBLK
