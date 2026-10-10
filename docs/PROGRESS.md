# Progreso de Zian Manager

Actualizado el 10 de octubre de 2026. Versión actual: **0.1.0-alpha.22**.

## Estado guardado

El desarrollo está en `feature/dungeon-core` y la propuesta completa está en [PR #1](https://github.com/IANBLK/Zian-Manager/pull/1). Sigue como borrador para las pruebas dentro del juego.

Alpha.22 añade perfiles independientes y configurables para Dungeon, Nether, End y Farmeo, con consumo, bonos, avisos y temporizador propios. Rechaza asignar un mundo a dos perfiles activos. Alpha.21 añade descuento administrativo de tiempo y títulos del temporizador por mundo. Alpha.20 añade penalizaciones infinitas de nivel V mientras un jugador agotado permanece dentro, con limpieza al salir/recuperar saldo y conservación de efectos previos. Añade avisos persistentes personalizables a 5 min/1 min/30 s y bloqueo previo en viajes NeoForge, con respaldo por cambio de dimensión y revisión cada segundo. Alpha.19 añade salida nativa por cama válida o spawn del mundo principal y respaldo para comandos de plugin ausentes. Alpha.18 corrige las herramientas a durabilidad vanilla +75. Conserva los 30 cascos y 20 herramientas con nombres de fantasía y encantamientos. Los cascos conservan durabilidad de netherita con +1 de armadura; las herramientas tienen +1 de daño y durabilidad vanilla +75. Conserva las mejoras de alpha.16, que compacta el temporizador y decora llaves/cofres con nombres y lore propios. El cofre del voto se renombra a Cofre de loot diario manteniendo su ID. Conserva el temporizador movible, las cuatro armas y su lore decorado. Conserva límites diarios, bonos temporales, rangos configurables, exención OP/bypass, anuncio personalizable, zonas y letreros. El log de alpha.14 recibido confirma aplicación de cambios y oleadas sin errores del runtime Zian Manager. En Youer se verificaron la salida al Spawn exacto de Multiverse y los accesos públicos a tiempo y tienda. Las rutas de teleport adicionales deben comprobarse con cada plugin. Guías y validaciones se guardan junto al código; mundos, cachés y credenciales quedan excluidos.


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
- Pestaña creativa propia con 81 modelos activos; trece objetos retirados del catálogo y los sorteos nuevos, con identificadores mínimos para datos anteriores.
- Armas y herramientas con encantamientos de su categoría, balance solicitado, tridente lanzable, habilidades conservadas y nombres/lore de fantasía. Tooltips muestran solo reutilización respecto a las habilidades.

## Validación realizada

Pasaron 78 pruebas unitarias, las pruebas nativas y los reinicios con y sin Lootr. La prueba de perfiles verifica aislamiento de consumo/bonos, persistencia, guardas por destino, exención OP y conservación de la penalización frente a cambios en otros perfiles. Las pruebas locales confirman daño, efectos y duración, absorción temporal, encantamientos, lanzamiento conservando Lealtad, minería protegida 3×3×1, zona centrada y redimensionado, comandos de NPC sin OP, esperas por botón, apertura de cofre sin pantalla, inventario lleno en supervivencia/creativo, no duplicación, llaves y persistencia después de reiniciar.

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

## Automatización de GitHub

El flujo usa acciones oficiales con Node 24 fijadas a commits verificados y Ubuntu 24.04. La ejecución [38042283033](https://github.com/IANBLK/Zian-Manager/actions/runs/38042283033) completó compilación, modelos, pruebas nativas y reinicios con/sin Lootr sin anotaciones. Las referencias a alpha.20 de secciones históricas indican cuándo se añadió esa función; la versión actual es alpha.22. El desarrollo sigue en la rama feature/dungeon-core y la PR de borrador; main contiene únicamente la presentación inicial.
