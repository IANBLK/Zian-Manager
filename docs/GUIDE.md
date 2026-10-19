# Zian Manager 0.3.0 — English guide

**Language notice:** The current in-game interface, preset dialogues and administrator messages are primarily Spanish. English localization is incomplete. The English guide explains the controls and includes their Spanish button names. Changing Minecraft to English does not translate administrator-written NPC names, dialogues or configuration text.

## Function split: combat moved to Zian Mobs

Manager retains **personal chest loot/tables, reusable vaults, dialogue/trading NPCs, dungeon entrances and world time limits**. **Zian Mobs** manages dungeon zones, mobs, bosses, waves, encounters and mob/boss loot tables. An entrance is a travel service, not the combat editor.

## Administrators: chests and vaults

Open `/zianmanager`. In **Cofres de dungeon** (Dungeon chests), configure the chest reward table and held-item entries, quantity and weight. Weights are relative draws, not independent percentages. Obtain/place a chest, then **Shift + right-click** to assign its table, renewal and supported key requirement. Test with a normal player: reward rolls and cooldowns are personal. Right-click delivers after the opening sequence; inventory overflow may appear by the player. Pending results are retained rather than rerolled.

**Bóvedas** (Vaults) has a GUI and `/zianmanager vaults` commands. Normal/ominous defaults are **30/60 minutes**, per player and block. Reuse requires its native key; vanilla supplies loot, sound and animation. Prefix these branches with `/zianmanager`:

- `vaults status`: inspect.
- `vaults times normal 30`, `vaults times ominous 60`, or `vaults times 30 60`: set minutes.
- `vaults cooldown 1`: looking at a nearby vault, set your own wait there to one minute.
- `vaults reset`: end your own wait at the targeted vault; no reward is generated.
- `vaults enabled false`/`true`: disable/enable reuse without erasing history.

Existing waits keep their deadline when durations change. OP does not bypass cooldowns. Administration uses `zianmanager.admin` or the supported fallback.

## Administrators: dialogue and unlimited trades

1. In **Personajes y diálogos** create/select an NPC, choose appearance and edit dialogue/command buttons. Save explicitly.
2. For a merchant put payment stacks in **hotbar slots 1–4**, result stacks in **6–9**; **slot 5 is ignored**. Each stack's amount sets its quantity. Use at least one stack per side.
3. In the trade editor choose **Preparar desde inventario** (Prepare), review, then **Guardar oferta preparada** (Save offer). Preparation does not consume administrator items.
4. Stock and trade counts are unlimited. Each offer exchanges 1–4 payments for 1–4 results, preserving vanilla/modded item components.
5. Players inspect **Entregas** (You give), **Recibes** (You receive), then **Intercambiar** (Trade). All materials, space, distance and session checks happen on the server. Only administrators configure offers; players must pay the configured cost.

## Entrances, daily time and migration

In **Entradas de dungeon**, capture safe arrival points and link the group to an NPC. Multiple dungeons can share a world; travel respects world-time limits. **Tiempos por mundo** configures profiles/ranks/reset/HUD; players use `/tiempos`. Administrators can use `/zianmanager bonustime player <player> <minutes>` and `/zianmanager removetime <player> <minutes>` for today's time. Exhaustion applies the configured dungeon exit.

To migrate combat, back up and retain Manager data. Open Mobs tools and **Importar combate antiguo de Manager…**, review and confirm. Conflicting IDs get distinct references; imported zones start **disabled**. Check every center/range/point/mob/reward before activating. Chests, NPCs, vaults and pending Manager receipts stay in Manager. Existing active encounters retain their finishing/delivery path; new combat editing belongs to Mobs. Protocol: **manager20**.

## Installation, updating and recovery

Use Minecraft **1.21.1**, **Java 21** and **NeoForge**, with the separate required libraries listed in the README. Stop the server and back up the entire world, configuration and mod list before replacing a version. Install the same current JAR on the server and every client. Remove the previous JAR, keep the required libraries on both sides, start and inspect the log, then verify your existing configuration and progress. For rollback, stop and restore the complete matching backup and mod versions together. Do not edit schema numbers to force migration.

Youer has been tested; this does not guarantee every hybrid server/plugin combination. Command-whitelist plugins may separately block public commands. Allow the needed public roots while keeping administrative operations restricted. LuckPerms is optional; administrator configuration remains protected.

Keep world data and configuration together. Optional SQLite maintenance is explicit, not required automatically by an update. Never edit a live database or delete a journal to clear an error. For an uncertain payment/delivery, keep its reference, reconcile the recorded outcome with inventory/PC/wallet state and use the administrator review flow. Confirmation records a verified result; it must never be used to guess or generate a second reward.

Report the mod/library versions, reproduction steps and sanitized logs through this repository's Issues. Do not upload credentials, tokens, player databases or private world backups.
