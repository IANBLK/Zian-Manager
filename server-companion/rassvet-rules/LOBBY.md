# Pegado controlado del lobby de Gyms

RassvetRules 1.0.6 añade una recuperación específica del esquema proporcionado por el usuario. WorldEdit es una dependencia opcional; se compila contra la API de WorldEdit 7.3.8 y se verificó con el fork WorldEdit-youer 7.3.8-2.

La consola o un administrador con `rassvet.admin` pueden usar `rassvet lobby`. Se prepara `plugins/WorldEdit/schematics/freemap18.schem`, se comprueba que conserva 911.569 bloques sólidos y se colocan únicamente en `gyms` desde el origen 0,64,0. Omite aire y entidades, conserva el NBT de bloques y limita cada tanda a 1.000 bloques y 6 ms antes del vaciado del editor. El mundo principal no se modifica. El límite temporal no incluye el trabajo interno de WorldEdit al cerrar cada tanda.

`rassvet lobbycancel` detiene la colocación; no revierte bloques ya aplicados. Estas opciones requieren administración y no se muestran a los rangos normales. No se ejecutan al arrancar. Esta utilidad no es un importador general: antes de repetirla en un Gyms editado, hay que respaldarlo y revisar el área de destino.

Verificación realizada: compilación Java 21, pegado real y guardado de los 911.569 bloques sin watchdog ni errores de operación, y muestreo de bloques guardados. La versión 1.0.5 desactiva validación, vecinos y callbacks de colocación durante las tandas; conserva las actualizaciones de iluminación y red. Las hojas se colocan persistentes y se cancela su descomposición únicamente en Gyms. El archivo original pertenece a Minecraft 1.21.8; se conservaron su versión y sus datos, sin etiquetarlo falsamente como 1.21.1. La presentación final requiere comprobación en el cliente.

Rescate del vacío: Spawn conserva su umbral configurado; Gyms rescata bajo minHeight + 4 (Y=-60), hacia su propia entrada, y al recibir daño de vacío. Se excluyen creativo y espectador. No intercepta daño de caída.

Se bloquean la creación y los viajes por portales vanilla de jugadores y entidades en todos los mundos, incluidos los no administrados por Multiverse y los creados posteriormente. Los viajes por comandos, NPC y TPA siguen disponibles. Complementar con `portal-form: none` en cada mundo registrado de Multiverse.

En 1.0.6, `/rassvet crops` permite retirar por tandas los cultivos decorativos del esquema original de Gyms. Solo elimina zanahorias, patatas, trigo y remolachas en sus posiciones originales si el bloque actual sigue siendo un cultivo; conserva los demás bloques y entidades. El crecimiento de esos cultivos se bloquea únicamente en Gyms. La herramienta requiere `rassvet.admin`; no se ejecuta automáticamente.
