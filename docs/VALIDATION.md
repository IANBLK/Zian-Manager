# Validación de 0.1.0-alpha.2

- Java 21, Minecraft 1.21.1, NeoForge 21.1.252.
- 25 pruebas unitarias: cero errores, cero fallos.
- Servidor dedicado local sin Lootr: encuentros compartidos y persistencia.
- Servidor dedicado local con Lootr 1.11.37.122: dos jugadores simulados, loot personal, dos oleadas de tres zombies, reaparición, NPC, entrega y lectura de inventario persistido, cofre sin receta y rotura denegada en supervivencia y creativo sin permiso; retirada administrativa en creativo elimina su registro.
- Segunda pasada con Lootr: reinicio conserva cofre, NPC, entrega y renovación.
- JAR comprobado: versión correcta, PNG 1024×1024, ambas etiquetas de exclusión de Lootr y ausencia de recetas.

La automatización reproducible está en tools/manager-smoke.py. No se ha probado visualmente con un cliente real ni con Youer o Cataclysm. Las comprobaciones de durabilidad verifican rotura nativa y propiedades de resistencia; no cubren herramientas externas de modificación de mundos.

- PNG del cofre comprobado con ImageIO: todos los píxeles con alfa 255. Forma no oclusiva y renderizado sólido.
- NPC humano con skin sincronizada; comando de objeto ejecutado para jugador sin OP, rechazo de repetición del token y de segundo uso durante la espera.
- Cuatro pruebas nuevas: espera persistente de comandos, bloqueo de ejecución ambigua, jugadores independientes y carga de NPC antiguos.
