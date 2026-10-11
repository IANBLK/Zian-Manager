# Zian Manager

Build configurable dungeon encounters, personal loot chests and dialogue NPCs from an in-game editor. Zian Manager also adds fantasy equipment and optional daily time limits for selected worlds.

## Features

- **Mobs and bosses:** configure existing entity types, names, health, damage, armor, effects and equipment. Bosses can display a boss bar.
- **Dungeon zones:** define a fixed center and resize the area with a visual preview. Select mob templates, waves, pauses and encounter regeneration. Automatic appearances can scale with nearby players within configured limits.
- **Personal loot chests:** configure per-player rewards and refill timers. Sealed chest variants consume their matching key; administrators can disable their refill cooldown. A short key-use delay protects reward processing.
- **Loot tables:** use weighted rewards for mobs, bosses and chests. Regular mobs also support independent drop chances, including no reward; bosses and chests retain weighted selection.
- **Dialogue NPCs:** choose a supplied skin, write multiline dialogue and add command buttons with individual cooldowns. Editing NPC commands requires administrative access.
- **Fantasy equipment:** themed weapons, mining hammers, enchantable hats and tools with imported models. Configure custom items as encounter rewards through the loot editor. These items are supplied through administrative/creative tools rather than a survival crafting progression.
- **World time limits:** optional, configurable daily budgets, rank permissions, temporary bonus time, removal commands and a movable timer. You can configure reset messages and exit destinations.
- **Persistent encounters and rewards:** settings and player reward state survive restarts. Ambiguous deliveries remain available for administrative review rather than being paid twice.

## Installation

- Minecraft **1.21.1**.
- **NeoForge 21.1.252 or newer within Minecraft 1.21.1**.
- **Java 21**.
- Install the **same Zian Manager beta.1 JAR on the client and server**. Replace an older Zian Manager JAR; do not leave duplicate versions in the mods folder.

Open `/zianmanager` as an administrator to create mobs, reward tables, zones, chests and NPCs. The mod does not automatically generate dungeons or populate a world: administrators build and configure their encounters. Daily time limits are optional and must be configured for the worlds you want to limit. Back up your world and configuration before upgrading.

## Optional integrations

**Lootr is optional.** Zian Manager uses its own personal reward chests and excludes them from Lootr conversion; ordinary Lootr chests keep their own behavior. **LuckPerms is optional** for rank-based permission setups. Cobblemon, RCT API, Zian RCT and Zian Utilities are **not required**.

Multiverse-Core, EternalCore and server plugins are optional external tools. Youer compatibility has been exercised in a configured hybrid server, but each plugin/mod combination should be tested separately. A teleport command configured for a particular server is not supplied by Zian Manager itself.

The separate Rassvet server companion and the isolated Casas world generator are **not included in this mod JAR** and are not required for its standard features.

## Beta status and compatibility

This is the first public beta, promoted from alpha.22. Existing item IDs, configuration formats and the network protocol are retained. Both sides must use the same published version. Automated checks and native server/restart scenarios cover the main runtime; unusual teleport integrations, modded entity behavior and rendering combinations may require additional testing. Keep backups and report issues with the Minecraft version, NeoForge version and relevant log excerpt.

## Support

- Source: https://github.com/IANBLK/Zian-Manager/tree/feature/dungeon-core
- Issues: https://github.com/IANBLK/Zian-Manager/issues

Created and maintained by **IANBLK**. Original code is MIT licensed; bundled artwork retains its separate redistribution permissions.
