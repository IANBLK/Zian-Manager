# Progreso de Zian Manager

Actualizado el 8 de octubre de 2026. Versión de pruebas: **0.1.0-alpha.8**.

## Estado guardado

El desarrollo está en `feature/dungeon-core` y la propuesta completa está en [PR #1](https://github.com/IANBLK/Zian-Manager/pull/1). Sigue como borrador para las pruebas dentro del juego.

La implementación de alpha.8 corresponde al commit `757f43422a043bee9fee072571d384deb06d1245`. [Las comprobaciones de GitHub](https://github.com/IANBLK/Zian-Manager/actions/runs/37749567467) terminaron correctamente: compilación, 45 pruebas unitarias, auditoría de modelos, servidor local y reinicio, con y sin Lootr.

El JAR y los registros están disponibles como artefacto `zianmanager-alpha-and-evidence` en esa ejecución. No se guardan mundos de prueba, cachés, credenciales ni archivos del servidor Rassvet en el repositorio.

## Implementado

- Mobs y jefes configurables, equipo, efectos, encuentros compartidos, oleadas y renovación.
- Zonas creadas desde el bloque central, tamaño X/Z/altura/profundidad, vista previa y centro bloqueado. Corregida la doble transformación de cámara del contorno.
- Editor de loot visual inspirado en el gacha; selección ponderada sin repetir entradas y tablas para mobs, jefes y cofres.
- Cofres importados con apertura directa, animaciones y variantes con llave. Loot al inventario, sobrantes al suelo y resumen de objetos/cantidades en el chat.
- Registro persistente de recompensas, consumo de llave, renovación y revisión de entregas ambiguas.
- NPC humanos Alex/slim con diez skins, diálogo y hasta ocho botones de comando con esperas independientes.
- Pestaña creativa propia con 27 modelos activos; trece objetos retirados del catálogo y los sorteos nuevos, con identificadores mínimos para datos anteriores.
- Armas y herramientas con encantamientos de su categoría, balance solicitado, tridente lanzable, habilidades conservadas y nombres/lore de fantasía. Tooltips muestran solo reutilización respecto a las habilidades.

## Validación realizada

Pruebas locales y GitHub confirman daño, efectos y duración, absorción temporal, encantamientos, lanzamiento conservando Lealtad, minería protegida 3×3×1, zona centrada y redimensionado, comandos de NPC sin OP, esperas por botón, apertura de cofre sin pantalla, inventario lleno en supervivencia/creativo, no duplicación, llaves y persistencia después de reiniciar.

El usuario confirmó funcionamiento durante versiones anteriores y aportó las capturas que guiaron las correcciones de texturas e interfaz. La corrección visual más reciente aún necesita confirmación del usuario.

## Pendiente antes de dar la alpha por validada

- Confirmar en el cliente que el contorno queda fijo al caminar/girar la cámara y al ampliar el tamaño.
- Confirmar el aspecto de nombres/lore y la presentación de los botones del NPC.
- Probar la última versión en Youer; no se ha validado allí este nuevo mod.
- Si se usan entidades de Cataclysm u otro mod, comprobar sus atributos, ataques y fases concretas.

## Archivos para continuar

- [Guía principal](../README.md)
- [Armas, cofres, zonas y NPC](EQUIPMENT.md)
- [Cambios por versión](CHANGELOG.md)
- [Validación y límites](VALIDATION.md)
- [Procedencia del código y arte](REUSE.md)
- [Inventario de modelos importados](imported-assets.json)

Para reconstruir: Java 21 y `./gradlew build`. Para repetir el servidor de pruebas: `python tools/manager-smoke.py`. La prueba es aislada y usa únicamente localhost; no vigila ni modifica el servidor real.
