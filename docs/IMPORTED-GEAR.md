# Cascos y herramientas — alpha.18

Los dos paquetes añaden 50 objetos propios a la pestaña Zian Manager. Mantienen sus PNG, geometría y transformaciones originales; no sustituyen objetos vanilla. Solo reciben nombres de fantasía coloreados, sin habilidades ni lore adicional.

## Sombreros

Son 30 ArmorItem de tipo HELMET, con 4 puntos de armadura (netherita +1), dureza 3, resistencia al empuje 0,1 y durabilidad 407, igual al casco de netherita. Son resistentes al fuego, se equipan en la cabeza y se desgastan al recibir daño como un casco normal. Encantamientos de casco: Protección, Respiración, Afinidad acuática, Irrompibilidad, Reparación y las maldiciones compatibles. Se reparan con netherita.

Un renderizador de armadura usa el modelo de cada sombrero y su transformación head original; la armadura vanilla de netherita no reemplaza su apariencia. La textura se obtiene del atlas de los modelos importados.

## Herramientas

Hay hachas, picos, palas y espadas de madera, piedra, hierro, oro y diamante (20 modelos). Conservan velocidad de ataque, velocidad y nivel de minería, reparación por material y capacidad de encantamiento de su equivalente vanilla. Añaden +1 al daño de ataque y todas tienen la durabilidad base vanilla más 75 puntos. Irrompibilidad y Reparación funcionan normalmente; no se simula una durabilidad fija invulnerable.

- Espadas: encantamientos de espada.
- Hachas: encantamientos de hacha y minería.
- Picos y palas: Eficiencia, Fortuna, Toque de seda y los demás compatibles de su categoría.
- Fortuna y Toque de seda conservan su incompatibilidad vanilla.

Los nombres se combinan con Roble Antiguo, Bastión de Piedra, Forja de Acero, Aurora Dorada y Cristal Eterno. Los ID de herramientas comienzan con reimagined_ y los de cascos con hat_.

## Sombreros disponibles

- Gorro del Navegante Celeste (`hat_aviator`).
- Capucha del Ajolote de Nácar (`hat_axolotl_hood`).
- Corona del Jardín Eterno (`hat_bloom_crown`).
- Yelmo de la Colmena Dorada (`hat_bumblebee`).
- Gorro de la Liebre Lunar (`hat_bunny_beanie`).
- Gorro del Banquete Real (`hat_chef`).
- Tricornio del Corsario Sombrío (`hat_corsair`).
- Gorro de las Nieves Serenas (`hat_cozy_pom`).
- Sombrero del Jinete del Ocaso (`hat_dusty_cowboy`).
- Sombrero del Festival del Alba (`hat_fiesta`).
- Orejas del Zorro de Ámbar (`hat_fox_ears`).
- Sombrero del Estanque Esmeralda (`hat_frog_bucket`).
- Diadema de la Escarcha Eterna (`hat_frost_diadem`).
- Manto de la Medusa Astral (`hat_jellyfish`).
- Cresta del Dragón Dormido (`hat_little_dragon`).
- Sombrero del Archimago Nocturno (`hat_midnight_wizard`).
- Casco del Guardián de las Profundidades (`hat_miner`).
- Gorro del Panda de Jade (`hat_panda_cap`).
- Paquete del Mensajero Arcano (`hat_parcel`).
- Visor del Oráculo de Cristal (`hat_retro_tv`).
- Corona de los Soberanos (`hat_royal_crown`).
- Gorro del Pato del Alba (`hat_rubber_duck`).
- Gorra del Almirante de las Mareas (`hat_sea_captain`).
- Chistera de la Forja de Vapor (`hat_steam_topper`).
- Sombrero del Jardín del Alba (`hat_straw_garden`).
- Gorra del Caminante Errante (`hat_street_cap`).
- Tocado del Festín de los Mares (`hat_sushi`).
- Sombrero del Bosque Encantado (`hat_toadstool`).
- Sombrero del Explorador de Horizontes (`hat_trailblazer`).
- Visor del Viajero de las Estrellas (`hat_ufo`).

La prueba local verifica estadísticas, desgaste y encantamientos; la apariencia de los 30 sombreros equipados debe confirmarse visualmente en el cliente. Instala el mismo JAR en cliente y servidor.

Durabilidad desde alpha.18: madera 134, piedra 206, hierro 325, oro 107 y diamante 1636. Los objetos ya usados conservan sus puntos de desgaste; se corrige su máximo base.
