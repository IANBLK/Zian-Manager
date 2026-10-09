# Progreso de Zian Manager

Actualizado el 8 de octubre de 2026. Versión de pruebas: **0.1.0-alpha.14**.

## Estado guardado

El desarrollo está en `feature/dungeon-core` y la propuesta completa está en [PR #1](https://github.com/IANBLK/Zian-Manager/pull/1). Sigue como borrador para las pruebas dentro del juego.

Alpha.14 añade límites diarios, bonos temporales, rangos configurables, exención OP/bypass y anuncio personalizable. Conserva las correcciones de alpha.13: el formato del cartel y muestra la cuenta atrás explícita de la próxima oleada. El log recibido usaba alpha.11; se requiere reemplazar el JAR para disponer del comando applyzone. Conserva nombres visibles de dungeon, tiempos modificables durante la espera, botón/comando para aplicar y reiniciar y letreros públicos por estado sin recargar la entidad. Conserva el autocompletado de recompensas en revisión, consumo de llaves y componentes. Conserva el contador flotante de regeneración, protección de dos segundos entre usos de llaves y reintento breve de archivos bloqueados. Conserva cofres con llave sin espera, apariciones automáticas dentro de zonas y porcentajes reales de drop solo para mobs normales. Las validaciones locales y límites se documentan en VALIDATION.md. La compilación y los registros también se generan en GitHub Actions; los mundos, cachés y credenciales quedan fuera del repositorio.

## Implementado

- Mobs y jefes configurables, equipo, efectos, encuentros compartidos, oleadas y renovación.
- Zonas con 1–8 tipos seleccionados, sin puntos manuales: 1–3 iniciales, +2 por jugador simultáneo adicional y máximo ocho por oleada.
- Zonas creadas desde el bloque central, tamaño X/Z/altura/profundidad, vista previa y centro bloqueado. Corregida la doble transformación de cámara del contorno.
- Editor de loot visual inspirado en el gacha; selección ponderada sin repetir entradas y tablas para mobs, jefes y cofres.
- Modo opcional de porcentaje real por entrada exclusivamente para mobs; puede no haber drop. Jefes y cofres mantienen sorteo por peso.
- Cofres con llave pueden abrirse sin espera consumiendo una llave por sorteo; cofres libres mantienen renovación.
- Cofres importados con apertura directa, animaciones y variantes con llave. Loot al inventario, sobrantes al suelo y resumen de objetos/cantidades en el chat.
- Registro persistente de recompensas, consumo de llave, renovación y revisión de entregas ambiguas.
- NPC humanos Alex/slim con diez skins, diálogo y hasta ocho botones de comando con esperas independientes.
- Pestaña creativa propia con 27 modelos activos; trece objetos retirados del catálogo y los sorteos nuevos, con identificadores mínimos para datos anteriores.
- Armas y herramientas con encantamientos de su categoría, balance solicitado, tridente lanzable, habilidades conservadas y nombres/lore de fantasía. Tooltips muestran solo reutilización respecto a las habilidades.

## Validación realizada

Las pruebas locales confirman daño, efectos y duración, absorción temporal, encantamientos, lanzamiento conservando Lealtad, minería protegida 3×3×1, zona centrada y redimensionado, comandos de NPC sin OP, esperas por botón, apertura de cofre sin pantalla, inventario lleno en supervivencia/creativo, no duplicación, llaves y persistencia después de reiniciar.

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
