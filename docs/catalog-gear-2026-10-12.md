# Beta.5: animación de cofres y avisos

Tras reservar una apertura válida, los cofres entregan el premio a los 30 ticks del servidor (1,5 segundos a 20 TPS). Los clics repetidos no crean otra reserva ni prolongan la espera. La reclamación manual respeta esa espera. La reserva duradera y los recibos existentes siguen controlando consumo de llave y entrega; una desconexión deja un pendiente recuperable sin duplicar el premio.

Los avisos y resúmenes de tesoros usan encabezados dorados y conservan nombres, colores y enlaces originales. Las pruebas unitarias y dos arranques nativos SQL/Lootr verifican ausencia de entrega temprana, veinte clics rápidos con una sola reserva y dos entregas con llave. El ajuste del fixture de dungeon espera el cambio real de fase sin modificar la lógica de producción.
