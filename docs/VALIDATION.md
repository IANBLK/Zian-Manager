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

## Alpha.3

Interfaz reorganizada y editor de loot inspirado en el gacha. Compilación y 25 pruebas automatizadas verificadas. Las reglas de selección y entrega no cambian. Los datos del editor incluyen iconos, cantidades, pesos y catálogos para selectores. La presentación necesita comprobación visual en el cliente del usuario; no se afirma una prueba visual automatizada. El protocolo exige actualizar cliente y servidor para evitar mezclar interfaces antiguas.

Alpha.3: servidor local con Lootr y reinicio superados, incluyendo generación de datos de los editores de loot, mobs, zonas y NPC.

## Alpha.4

32 pruebas unitarias. Verificación de 40 modelos: coordenadas finitas, UV/índices válidos, hashes de PNG originales y jerarquías completas con datos de animación. Servidor dedicado local con Lootr: daño 7/8/8, ataques y esperas 15/30/120 s, efectos I por 30 s, martillo 3×3×1 con durabilidad y bloque protegido, llave correcta consumida una vez, vista previa sin consumo, y restauración/reinicio manteniendo tipo y orientación. El renderizado final y la guía de zonas no se han confirmado visualmente con un cliente real.

## Alpha.5

36 pruebas unitarias. Auditoría de materiales/PNG verifica rutas bajo textures/item para el atlas, hashes originales y ausencia de datos de animación bajo models. Servidor local con Lootr: apertura sin sesión de GUI, inventario lleno en supervivencia y creativo, sobrante exacto al suelo, clic repetido sin duplicación, habilidades/llaves/minería y reinicio. Migración diferida de bloques antiguos evita acceder a un chunk mientras aún se está cargando. Falta confirmar visualmente la corrección en el cliente real del usuario.

## Alpha.6

Compilación y 36 pruebas unitarias superadas. Auditoría de 27 modelos activos y exclusión del arte de 13 objetos retirados del JAR. Prueba local con Lootr y reinicio: se conserva apertura sin interfaz, sobrantes al suelo, prevención de repetición, llaves y habilidades. El resumen del chat se construye únicamente con los componentes confirmados y conserva los nombres traducibles del objeto para el cliente.

## Alpha.7

41 pruebas unitarias. Prueba local con Lootr: creación de zona desde el bloque del suelo, radio 3 = 7×7, ampliación sin mover centro y cancelación conservando el centro. Verificados daños 7/8/10/9/9/9/10/9, efectos y niveles por 30 s, diez corazones de absorción, Wither de 5 s, aceptación de encantamientos de espada/minería/tridente y lanzamiento de Rompeolas conservando Lealtad y el objeto. La espera de la habilidad no activa el bloqueo de uso normal del tridente. También superadas apertura directa, llaves, sobrantes al suelo, oleadas y reinicio. La nueva vista previa y el aspecto del proyectil necesitan confirmación visual en el cliente.

## Alpha.8

45 pruebas unitarias. Prueba local con Lootr: centro no cambia al repetir creación desde otra posición, redimensionado conserva el ancla, comandos principal y adicional ejecutables sin OP con esperas independientes, repetición rechazada y reinicio. Conservadas pruebas de armas, encantamientos, tridente, loot e inventario lleno. Corrección de render basada en el código de LevelRenderer: el model-view ya está aplicado globalmente; se elimina su segunda aplicación y se usa una pose nueva con traducción relativa a la cámara. Vista y lore necesitan confirmación visual en el cliente del usuario.

## Alpha.9: llaves, población y probabilidades de mobs

- Compilación Java 21/NeoForge 21.1.252; 55 pruebas unitarias sin fallos.
- Porcentajes independientes: simulación reproducible de 100.000 muertes con entradas al 10% y 5%, posibilidad de ninguna o ambas. Jefes y cofres rechazan este modo.
- Resultado vacío persistido y protegido de rerolls después de reiniciar.
- Zona de una sola plantilla sin puntos, escala 1–3/+2/límite ocho, estado guardado después de muerte y reinicio.
- Prueba nativa aislada con Lootr: dos aperturas inmediatas con una llave cada una; rechazo sin llave, cofres libres con espera, zona automática y aumento de dos mobs con segundo jugador. Prueba de reinicio conserva configuración y entregas.
- Solo aparecen mobs si hay suelo sólido y espacio libre dentro del área; zonas estrechas pueden tener menos mobs. El límite es por oleada y no hay multiplicación al reentrar.
- La presentación del cliente y entidades de otros mods necesitan comprobación dentro del juego; el servidor real no se modifica.

## Alpha.10: cuenta atrás y espera de llaves

- Compilación correcta y 56 pruebas unitarias sin fallos.
- Prueba nativa con Lootr: 20 intentos rápidos consumen una sola llave y no dejan reclamaciones pendientes; después de la espera se consume la siguiente.
- Texto de regeneración creado en el mundo durante el tiempo configurado de espera; usa un Text Display nativo visible para clientes compatibles.
- La cuenta atrás se fija al centro y se actualiza cada segundo. La presentación visual con shaders aún requiere confirmación del usuario.
- El reintento de escritura solo cubre acceso denegado transitorio. La captura no permite determinar la causa exacta del error anterior. No se borran ni repiten las reclamaciones que ya están en revisión.

## Alpha.11: sugerencias de comandos

- Compilación y 56 pruebas unitarias correctas.
- Prueba del dispatcher real en servidor local con Lootr: `claim` sugiere pendientes propios; `resolve` y `resolvekey` sugieren los ID del jugador elegido según su estado; no muestran premios de otro jugador.
- `resolve` sugiere únicamente los componentes APPLYING o REVIEW_REQUIRED de esa entrega.
- Las sugerencias no ejecutan ni confirman recompensas y conservan los permisos de administración.

## Alpha.12: aplicar cambios y letreros públicos

- 59 pruebas unitarias: nombre persistente y compatible, modificación de regeneración desde la finalización original y modificación de pausa sin reiniciar una oleada activa.
- Prueba nativa de guardar nombre y tiempo durante la espera, comando applyzone y zona que mantiene su estado habilitado tras el reinicio.
- Letrero público visible durante la oleada, con nombre personalizado; conserva UUID y posición entre actualizaciones. Cuenta atrás comprobada después de completar el encuentro.
- No se recibió un log del usuario: el archivo adjunto era el JAR. La causa concreta de la falta de mobs necesita el registro correspondiente; la zona en revisión queda indicada y puede reiniciarse explícitamente.
- Plataforma suspendida en otra dimensión: sin suelo no se preparan mobs; al construir la plataforma aparecen dentro del área y en la dimensión configurada.
- Prueba completa con Lootr y reinicio correcta.
- La dimensión personalizada real y sus shaders no se probaron; las apariciones requieren una estructura con suelo firme y espacio libre en el área configurada.

## Alpha.13: diagnóstico del log y cartel de oleadas

El latest.log recibido indica zianmanager-0.1.0-alpha.11.jar. Esa versión no incluía applyzone ni el nombre visible por zona añadidos en alpha.12. También muestra oleadas 1, 2 y 3 y errores repetidos Display entityNot a string.

Se corrige la actualización del texto para conservar el NBT válido del display, incluida su transformación. El cartel usa el nombre individual de zona y muestra Oleada N en X s durante la pausa. La prueba nativa comprueba el cartel durante esa pausa y rechaza la ejecución si reaparece el error de formato.

La ejecución local con Lootr comprobó el comando, los nombres, el contador de oleada y el contador de regeneración sin errores de formato del display. Compilación y 59 pruebas unitarias correctas.

## Alpha.14: tiempo diario y bonos

- 65 pruebas unitarias correctas: límites entre semana/fines de semana, medianoche de Ecuador, saldo persistente, bonos individuales/globales, vencimiento y anuncio único a las 00:01.
- Prueba nativa con Lootr: OP exento, comandos de bonos de 30 minutos personal y global, saldo agotado, teleportación desde consola, rechazo de reentrada y uso persistido. Reinicio del servidor correcto.
- La salida nativa utiliza teleportación entre dimensiones para comprobar el mecanismo. La integración real Bukkit/LuckPerms/Multiverse/EternalCore/Waystones en Youer no está validada en este entorno y requiere prueba del servidor híbrido.
- La configuración empieza desactivada; hay que seleccionar mundos y habilitarla. La salida utiliza el comando configurable del plugin, no un spawn vanilla inventado.
