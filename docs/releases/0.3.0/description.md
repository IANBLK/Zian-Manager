# Zian Manager

**Language notice:** The current in-game interface, preset dialogues and administrator messages are primarily Spanish. English localization is incomplete. The English guide explains the controls and includes their Spanish button names. Changing Minecraft to English does not translate administrator-written NPC names, dialogues or configuration text.

[English user and administrator guide](https://github.com/IANBLK/Zian-Manager/blob/main/docs/GUIDE.md) · [Guía en español](https://github.com/IANBLK/Zian-Manager/blob/main/docs/GUIA_ES.md)

## Dungeon configuration moved to Zian Mobs / Configuración de dungeons trasladada

Dungeon zones, mobs, bosses, waves, encounters and mob/boss loot tables have moved from **Zian Manager to Zian Mobs**. Configure combat in Zian Mobs. Manager retains chest loot, vaults, NPCs/trades, entrances and world time limits. For existing Manager definitions, use the guarded import preview in Mobs after backing up the world; pending delivery receipts are not moved.

La configuración de zonas de dungeon, mobs, jefes, oleadas, encuentros y tablas de loot de mobs/jefes pasó de **Zian Manager a Zian Mobs**. Las definiciones antiguas se conservan para importarlas con la vista previa protegida de Mobs; respalda el mundo antes y no elimines los datos antiguos. Los recibos de entregas pendientes no se trasladan.

Build the services around your dungeon with native administrator menus: personal reward chests, reusable vanilla vaults, dialogue NPCs, physical trading, dungeon entrances and per-world playtime limits.

## Personal rewards and vaults
Configure chest reward tables, per-player renewal and key requirements in game. Each player receives their own reward roll and cooldown. Vaults support independent normal/ominous cooldown settings through both GUI and commands. Pending deliveries retain their recorded result for recovery and review.

## Dialogue NPCs and unlimited trades
Administrators configure appearance, dialogue and command buttons. Merchants exchange up to four payment stacks for up to four result stacks with fixed quantities and unlimited stock. Vanilla and modded items keep their components. Setup copies hotbar slots 1–4 for payment and 6–9 for results without consuming them; slot 5 separates both sides. Players use their inventory to pay and must have room for the complete result. The GUI separates offers, payment, rewards and inventory.

## Dungeon services
Configure entrances and daily time by world. Players can consult their own remaining time; configuration remains administrator-only. Combat definitions, mobs, bosses, waves, encounters and combat reward tables are now managed by **Zian Mobs**, a separate mod. Existing Manager combat definitions are retained for its guarded import workflow.

## Installation
Minecraft 1.21.1, Java 21 and NeoForge 21.1.252 or newer for Minecraft 1.21.1. Install on client and server. Lootr and LuckPerms are optional integrations; Cobblemon and RCT API are not required. Open /zianmanager with administrative access. Existing saves must be backed up before migration. Rassvet-specific server plugins and Mundo Casas tools are separate and are not bundled.

## License and credits
Original code uses MIT. Imported models, skins and other artwork retain their separate rights and notices. The maintainer identifies https://es.namemc.com/ as the source of the 27 additional NPC skins and states they are publicly distributed; this is source attribution, not a claim that NameMC or Zian authored them or that they are MIT licensed.

## Latest update — 0.3.0

Manager now focuses on personal dungeon chests and chest reward tables, reusable vaults with GUI and commands, dialogue/trading NPCs, dungeon entrances and per-world time limits. Combat editing belongs to Zian Mobs. Legacy combat definitions remain available for guarded import; existing active encounters retain their completion and delivery handling. Physical NPC trades exchange 1–4 payment stacks for 1–4 result stacks with unlimited stock, including modded item components. Administrator setup copies hotbar slots 1–4 as payment and 6–9 as output; slot 5 is ignored. The trading GUI has more spacing and full hover details. Protocol manager20: update both sides.

## Novedades — Español

Manager conserva cofres y sus tablas de loot, bóvedas con GUI/comandos, NPC y comercios, entradas y tiempos por mundo. Mobs, jefes, zonas y sus tablas de combate se gestionan en Zian Mobs. Intercambios físicos ilimitados de hasta cuatro pagos por cuatro resultados, con objetos de mods y componentes. Solo los administradores configuran las ofertas; los jugadores intercambian.

Minecraft 1.21.1 · NeoForge · Java 21. Install the same mod version on client and server. Back up your world before upgrading; replace the old JAR instead of keeping both copies.
