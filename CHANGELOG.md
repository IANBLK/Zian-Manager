# Changelog

## 0.1.1

- Bóvedas vanilla reutilizables por jugador y ubicación; valores predeterminados de 30 minutos para normales y 60 para ominosas. Conserva las llaves, recompensas y animaciones originales.
- Tiempos persistentes tras reinicios, ayuda y autocompletado administrativo, configuración por tipo y reset individual de la bóveda que mira el administrador.
- Ocho skins nuevas con nombres en el editor; 18 apariencias en total. Los NPC existentes conservan sus IDs y apariencias.
- Renderizado con brazos clásicos o delgados según cada skin nueva.

## 0.1.1-beta.2 — administración de bóvedas

- Configuración independiente con `vaults times normal <minutos>` y `vaults times ominous <minutos>`.
- Sugerencias de tipos y minutos con Tab; ayuda desde `vaults` y `vaults help`.
- Conserva el formato anterior de dos números y permite un número para ambas clases.
- `vaults reset` termina la espera del administrador en la bóveda que mira.
- `vaults cooldown <minutos>` ajusta la espera del administrador en la bóveda que mira; cero la termina conservando el historial. Las esperas de otros jugadores y bóvedas se conservan.
- Las configuraciones generales siguen afectando a aperturas nuevas; los mensajes explican cómo ajustar una espera que ya empezó.

## 0.1.0-beta.2 — candidata de almacenamiento

- Índices de recompensas y escritura JSON sin serialización duplicada.
- Migración offline opcional de recibos y llaves a archivos por jugador con caché acotada y protección de entregas activas.
- Conserva todos los pagos, ciclos, costes y estados inciertos; los formatos anteriores continúan funcionando.
- HUD: Tiempo Ilimitado y alineación a la izquierda.
- 85 pruebas aprobadas, incluyendo 22.000 recibos, y dos arranques aislados con Lootr y reinicio. No desplegado ni migrado en producción.

