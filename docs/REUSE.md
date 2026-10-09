# Procedencia

Proyecto original de IANBLK, bajo la licencia MIT del repositorio.

- [Zian-RCT](https://github.com/IANBLK/Zian-RCT): adaptados RewardFiles, RewardClaim, RewardDefinition, RewardJournal, RewardDelivery y consulta opcional de LuckPerms. Claves propias, hasta 32 componentes y renovación desde la entrega completada.
- [Zian-GUI](https://github.com/IANBLK/Zian-GUI): referencia de presentación y paginación; pantallas nuevas.
- [Zian-GTS](https://github.com/IANBLK/Zian-GTS) y [Zian-Utilities](https://github.com/IANBLK/Zian-Utilities): revisados sus patrones de persistencia y administración, sin incorporar funcionalidades ni dependencias.

No se incluye código de RCT Mod, RCT API, Cobblemon ni Lootr. Se utilizan las etiquetas públicas de exclusión de conversión de Lootr. La prueba emplea Lootr 1.11.37.122; no garantiza otras versiones o mods.

Textura original generada con imagegen para este proyecto; véase art/PROMPT.md. El importador Java solo ajusta dimensiones mediante vecino más cercano.

En alpha.2 se adapta el modelo PlayerModel de CustomTrainerRenderer de ZianRCT y el estilo de botones de ZianGuiScreen. Las diez nuevas skins fueron proporcionadas por el usuario; se incorporan sin modificar sus píxeles, con nombres nuevos y modelo Alex/slim.

Alpha.3: el editor visual se inspira en GachaScreen de Zian Utilities (MIT © ZIANBLK), reutilizando el patrón de filas con iconos, controles de peso −/+ y edición directa. No se incluyen pagos, tiradas ni dependencias del gacha.

Alpha.4 incorpora modelos y texturas de los cinco ZIP proporcionados por el usuario. `imported-assets.json` documenta los archivos fuente y sus hashes. La licencia MIT del código no cambia ni atribuye una licencia nueva al arte de esos paquetes; se mantienen separados del código original.

Alpha.15 incorpora WeaponReskins-vol1.zip y Chainsaw.zip proporcionados por el usuario. Se extraen los PNG sin modificar sus bytes y se conservan cubos, transformaciones y presentación. Para modelos Java se usa la resolución UV del proyecto, conforme al [codec oficial de Blockbench](https://github.com/JannisX11/blockbench/blob/master/js/formats/java/java_block.ts); el ninja declara resolución UV 16 con PNG de 32 píxeles. El importador incremental --extra conserva los modelos existentes cuando sus ZIP fuente ya no están disponibles.
