# 0.1.0-alpha.18

- Corregida la interpretación de durabilidad: las 20 herramientas tienen vanilla +75 puntos, en vez de 75 totales.
- Madera 134, piedra 206, hierro 325, oro 107 y diamante 1636; se conserva el daño actual de los objetos usados.
- Sombreros mantienen 407 de durabilidad de netherita; demás estadísticas, encantamientos y modelos conservados.

# 0.1.0-alpha.17

- 30 sombreros como cascos reales: 4 de armadura, dureza/resistencia al empuje y 407 de durabilidad de netherita; encantamientos, reparación y desgaste normales.
- Renderizador equipado que conserva los modelos originales de sombreros en lugar de usar el casco vanilla.
- 20 herramientas reimaginadas de cinco materiales y cuatro tipos: estadísticas vanilla, +1 de daño y durabilidad 75.
- Nombres de fantasía en el estilo existente, sin añadir habilidades ni lore a estos objetos nuevos.
- Encantamientos de casco, espada, hacha, pico y pala; pestaña creativa propia.
- Importación incremental sin modificar PNG ni transformaciones de presentación; 81 modelos activos.

# 0.1.0-alpha.16

- Temporizador compacto: ancho ajustado al texto, sin espacio sobrante a la derecha; editor de arrastre usa el mismo tamaño.
- Llaves y las nueve variantes de cofre con nombre coloreado, separadores y lore de fantasía al estilo de las armas.
- Cofre del voto renombrado a Cofre de loot diario, conservando loot_vote_crate y su configuración guardada.
- Se conservan modelos, texturas, correspondencia entre llaves/cofres y mecánica de loot.

# 0.1.0-alpha.15

- Temporizador de pantalla sincronizado por el servidor, solo en mundos de dungeon con límite configurado; OP/bypass muestran Tiempo ilimitado.
- Posición movible mediante arrastre o porcentajes desde Pantalla en el menú de tiempo diario; compatible con configuración anterior.
- Cuatro armas importadas: Solaris (11), Réquiem (9), Tsukikage (10) y Kárnax (11); buffs de 30 s hasta nivel II y espera de 90 s.
- Encantamientos de espada para las armas nuevas y registro en la pestaña creativa propia.
- Nombres coloreados y lore de fantasía con separadores y secciones para las armas/herramientas.
- Importación incremental de modelos OBJ y PNG originales; corrección de resolución UV del ninja sin modificar píxeles.
- Revisión del log de alpha.14: registra aplicación de zonas y oleadas; no se encontraron errores del runtime Zian Manager.

# 0.1.0-alpha.14

- Límites diarios compartidos entre mundos de dungeon: DEFAULT 30/90 y VIP 120/180 minutos, configurables, con perfiles adicionales por permiso.
- OP y permiso de bypass tienen tiempo ilimitado y no consumen saldo.
- Saldo guardado por jugador y fecha local; reinicio a las 00:00 de Ecuador, sin reinicio de saldo al salir o reconectar.
- Salida por comando de consola al agotar tiempo; comprobación de mundo real al cambiar dimensión y cada segundo, incluyendo entradas por terceros.
- Bonos diarios acumulables personales y globales, también para quienes entren más tarde; expiran con el día.
- Aviso de agotamiento y anuncio de las 00:01 personalizables; anuncio diario persistido para evitar duplicados.
- Menú de mundos, tiempos, rangos, salida y mensajes; consulta time y comandos bonustime.

# 0.1.0-alpha.13

- Corregido el formato incompleto del Text Display que producía Display entityNot a string: se conservan sus propiedades válidas al actualizar el texto.
- Cartel con nombre propio de cada zona y cuenta atrás explícita: Oleada N en X s; regeneración de dungeon independiente.
- Lista de zonas muestra nombre visible y referencia interna; applyzone usa la referencia con Tab.
- La prueba de servidor falla si detecta el error de formato del cartel y verifica el contador de la próxima oleada.
- El log recibido cargaba alpha.11, que todavía no incluía applyzone ni los nombres de zona de alpha.12.

# 0.1.0-alpha.12

- Nombre visible de dungeon, compatible con zonas anteriores que usaban solo una referencia.
- Guardar regeneración y pausas recalcula la espera del encuentro actual desde su finalización original.
- Botón Aplicar y reiniciar y comando applyzone con autocompletado para reiniciar encuentros sin premios, conservando la configuración y el estado habilitado.
- Letrero público permanente por estado: disponible, en curso, próxima oleada, regeneración o revisión, con nombre personalizado.
- Actualización del texto sin recargar ni mover la entidad; mayor alcance visual, texto iluminado y legible a través de obstáculos.
- Pruebas de nombres persistentes, esperas modificadas y plataforma suspendida en otra dimensión sin terreno natural.

# 0.1.0-alpha.11

- Autocompletado de IDs en `resolve` y `resolvekey`, filtrado por jugador y estado correspondiente.
- Autocompletado de componentes inciertos en `resolve`; conserva el autocompletado de pendientes propios en `claim`.
- Las sugerencias respetan los permisos y no ejecutan entregas ni confirmaciones.

# 0.1.0-alpha.10

- Texto flotante público y fijo sobre el centro de la dungeon con cuenta atrás de regeneración según su configuración.
- Espera de seguridad de dos segundos entre intentos de abrir cofres con llave, por jugador y compartida entre cofres. Clics bloqueados no consumen llaves ni crean reclamaciones.
- Limpieza de textos al finalizar la espera, desactivar/eliminar zona y reiniciar, evitando duplicados.
- Reintento breve del reemplazo atómico ante acceso denegado transitorio; errores persistentes conservan la protección de entregas ambiguas.
- Registro detallado de errores de escritura al abrir cofres. Las reclamaciones antiguas en revisión requieren comprobación manual.

# 0.1.0-alpha.9

- Cofres con llave: opción Sin tiempo de reutilización; cada apertura nueva consume una llave y genera otro sorteo. Los cofres libres mantienen renovación configurable.
- Zonas automáticas sin puntos manuales: selección de 1–8 tipos de mob, 1–3 iniciales, dos por jugador simultáneo adicional y máximo ocho por oleada.
- Suelo firme y espacio libre dentro del área; centro y guía visual conservados.
- Registro de población y participación persistente. Reentrar no crea oleadas adicionales.
- Modo opcional de porcentajes absolutos solo para mobs: cada entrada se comprueba independientemente y puede no haber drop; jefes y cofres conservan selección por peso. El resultado vacío queda registrado para impedir rerolls.
- Pruebas de entregas consecutivas con llave y sin espera, zonas con una sola plantilla y escalado compartido.

# 0.1.0-alpha.8

- Corregida la doble transformación de cámara del contorno de zonas: ahora se dibuja una sola vez en el espacio del mundo.
- Centro bloqueado al crear la zona; ampliación con tamaño X/Z/altura/profundidad mantiene la misma posición.
- Nombres y lore de fantasía para las catorce armas/herramientas; tooltip sin descripción técnica de habilidades, solo reutilización.
- Habilidades, daño y encantamientos se conservan.
- NPC con hasta ocho botones de comando (principal y siete adicionales), editor dentro del juego y cooldown por botón/jugador.
- Compatibilidad con diálogos y esperas guardados de versiones anteriores.

# 0.1.0-alpha.7

- Encantamientos normales según categoría: martillos/pico, hacha, espadas/lanzas/bastón y tridente Rompeolas.
- Lanza Ígnea: reutilización 1 s; Bastón del Vacío: 10 s; todas las esperas anteriores de 120 s: 90 s.
- Aumentado daño base: Gladiador 10, Ancestral 9, Dragón 9, Marchito 9 y Lanza del Presagio 10.
- Ancestral: Regeneración I y diez corazones de absorción temporal, 30 s.
- Dragón: Fuerza I y Absorción I, 30 s. Marchito: Wither I por golpe confirmado durante 5 s y Fuerza II con clic derecho durante 30 s.
- Presagio renombrado como lanza: Regeneración I y Absorción II por 30 s, sin visión nocturna.
- Rompeolas ahora es un tridente lanzable; Regeneración I y Fuerza I por 30 s. Su habilidad no bloquea lanzar; el proyectil conserva objeto, encantamientos y modelo.
- Zonas centradas: Crear zona de dungeon usa el bloque bajo el jugador; radio X/Z, altura y profundidad editables con −/+ y vista previa en vivo.
- Conservación del centro al ampliar y de las zonas anteriores al cargar.

# 0.1.0-alpha.6

- Retirados los trece objetos señalados del catálogo creativo, visores compatibles y nuevos sorteos de loot.
- Conservados únicamente identificadores mínimos para leer datos anteriores; eliminados sus modelos y texturas activos.
- Chat de recompensas: muestra nombre, cantidad y destino de cada objeto después de confirmar la entrega.
- Se conserva apertura sin GUI, animaciones, llaves, inventario/sobrante al suelo y protección contra repetición.

# 0.1.0-alpha.5

- Corregidos los iconos y texturas ausentes: materiales y PNG ahora están en la carpeta de ítems que se incorpora al atlas de Minecraft.
- Datos de animación movidos fuera de models para evitar errores del cargador de modelos.
- Retirado el cofre original del creativo y de los visores de recetas compatibles; migración diferida de bloques e inventarios al cofre común.
- Apertura directa: loot al inventario y sobrantes al suelo, sin pantalla de premios, también en creativo.
- Consumo único de llave y confirmación conjunta del inventario y las entidades del mundo; sin reintentos automáticos de entregas ambiguas.
- Se mantienen tablas, UUID de cofre, renovaciones y registros de los mundos existentes.

# 0.1.0-alpha.4

- Pestaña creativa propia con armas, martillos, accesorios, llaves y todos los cofres.
- Importados 40 modelos de los cinco paquetes proporcionados; texturas originales y conversión OBJ para rotaciones de formatos más recientes.
- Martillos de colores de nivel diamante con minería 3×3×1, respeto de rotura cancelada y Shift para minería individual.
- Flame Spear: espada de diamante y bola de fuego, 15 s. Void Staff: espada de netherita y ataque sónico, 30 s. Gladiator Sword: espada de netherita y tres efectos I durante 30 s, espera 120 s.
- Objetos del paquete Altar con estadísticas de diamante y habilidades moderadas; accesorios y emblemas decorativos.
- Cinco cofres de Crates and Stuff con apertura directa y cuatro cofres animados con llave por rareza.
- Consumo de llave confirmado al recibir loot, sorteo congelado, registro persistente y bloqueo de reintentos ambiguos.
- Tipo y orientación de cofre guardados; migración de los cofres originales. Todas las variantes se excluyen de Lootr.
- Vista de zona con contorno, esquinas de colores, coordenadas y puntos de aparición, con opción de mostrar/ocultar.

# 0.1.0-alpha.3

- Editor de loot visual inspirado en el gacha de Zian Utilities: iconos, porcentaje, cantidades, probabilidad con −/+ y eliminación por fila.
- Pulsar un objeto abre la edición de cantidad mínima/máxima; ya no hace falta escribir índices.
- Inicio en español y editores con pestañas por tarea, ayuda breve y ajustes avanzados aparte.
- Selectores de recompensas, equipo de oleadas, skins, entidad y ranura; creación automática de referencias.
- Editor de efectos por nombre, nivel y duración; se conserva el formato manual avanzado.
- Diálogo del NPC con edición de varias líneas.
- Botón Obtener cofre en el menú y retorno a la categoría que se estaba editando.
- Selección de una entrada carga sus cantidades y peso reales; conservar pestaña y página al guardar.
- Sin cambiar la selección aleatoria, protección, permisos, persistencia o entrega de recompensas.

# 0.1.0-alpha.2

- Eliminado el desenfoque que se aplicaba sobre títulos y paneles.
- Interfaz compacta centrada, botones oscuros con borde dorado, selectores y confirmación coherentes con Zian GUI.
- Cofre con forma de colisión y selección ajustada al modelo; no oculta caras vecinas como un cubo completo y usa capa sólida. Su textura original es totalmente opaca.
- Retirada en creativo para administradores: elimina el registro para evitar su restauración. Supervivencia permanece protegida.
- NPC humanos independientes de RCT, modelo Alex/slim y diez skins suministradas por el usuario.
- Diálogo con botón opcional, comando con {player}, espera por jugador y ejecución de servidor sin dar OP al jugador.
- Diario persistente de comandos: una ejecución ambigua no se repite automáticamente.
- Migración del NPC anterior conservando UUID, ubicación y texto.

## Alpha.19

Salida nativa por cama válida o spawn del mundo principal, selector desde Tiempo diario → Salida y respaldo si el comando de un plugin no existe. Las camas en mundos limitados se descartan. No se mata al jugador ni se cambia su punto de reaparición.

## Alpha.20

Avisos personalizables a 5 minutos, 1 minuto y 30 segundos. Registro persistente evita repetición al reconectar/reiniciar; contempla lag, renovación diaria y bonos. Bloqueo previo de entrada agotada mediante el evento cancelable de viaje de NeoForge, sin bloquear las salidas ni OP/bypass. El respaldo por dimensión y por segundo sigue cubriendo rutas de teleportación alternativas. WorldTimeLimit se usó como referencia funcional, sin nuevas dependencias obligatorias.

Penalización de respaldo: si un jugador sin exención permanece dentro de una dungeon limitada sin saldo, recibe Lentitud V, Oscuridad V y Debilidad V con duración infinita. Se retiran al salir, al recuperar saldo por renovación diaria o bono, al obtener exención o al desactivar los límites. Una limpieza como leche no evita que se vuelvan a aplicar en la siguiente revisión. Los efectos ajenos se conservan mediante la cadena de efectos ocultos de Minecraft, con su duración restante.
