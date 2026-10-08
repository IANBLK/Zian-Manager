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
