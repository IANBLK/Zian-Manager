package com.ianblk.zianmanager.core;

import java.util.List;

/** Stable saved IDs are separate from labels and the texture's arm layout. */
public final class NpcSkins {
    private NpcSkins(){}
    public record Skin(String id,String name,boolean slim){}
    public static final List<Skin> ALL=List.of(
        new Skin("heraldo_real","Heraldo Real",true),
        new Skin("guardian_celeste","Guardián Celeste",true),
        new Skin("centinela_sombrio","Centinela Sombrío",true),
        new Skin("caballero_infernal","Caballero Infernal",true),
        new Skin("guardian_abisal","Guardián Abisal",true),
        new Skin("maga_prismatica","Maga Prismática",true),
        new Skin("paladin_dorado","Paladín Dorado",true),
        new Skin("explorador_bronce","Explorador de Bronce",true),
        new Skin("guardiana_celestial","Guardiana Celestial",true),
        new Skin("hechicera_aurora","Hechicera Aurora",true),
        new Skin("aurelia_solar","Aurelia Solar",true),
        new Skin("sakura_nacar","Sakura de Nácar",false),
        new Skin("lia_bosque","Lía del Bosque",true),
        new Skin("raven_nocturna","Raven Nocturna",false),
        new Skin("kael_ocaso","Kael del Ocaso",true),
        new Skin("akari_imperial","Akari Imperial",true),
        new Skin("centinela_ambar","Centinela Ámbar",false),
        new Skin("guardian_boreal","Guardián Boreal",false),
        new Skin("alaric_zafiro","Alaric del Zafiro",true),
        new Skin("soren_hielo","Soren del Hielo",true),
        new Skin("nyx_umbral","Nyx del Umbral",true),
        new Skin("iris_prismatica","Iris Prismática",false),
        new Skin("kael_invierno","Kael del Invierno",true),
        new Skin("elian_alba","Elian del Alba",true),
        new Skin("darian_roble","Darian del Roble",false),
        new Skin("varek_carmesi","Varek Carmesí",false),
        new Skin("liora_arena","Liora de Arena",false),
        new Skin("vigia_sculk","Vigía del Sculk",false),
        new Skin("morgana_cosecha","Morgana de la Cosecha",false),
        new Skin("heraldo_vacio","Heraldo del Vacío",false),
        new Skin("sombra_violeta","Sombra Violeta",true),
        new Skin("centinela_carmesi","Centinela Carmesí",true),
        new Skin("nadir_medianoche","Nadir de Medianoche",true),
        new Skin("guardia_cobalto","Guardia de Cobalto",true),
        new Skin("serafina_nieves","Serafina de las Nieves",true),
        new Skin("lucien_ocaso","Lucien del Ocaso",false),
        new Skin("mika_rosada","Mika Rosada",true),
        new Skin("espectro_sculk","Espectro del Sculk",false),
        new Skin("ignis_brasero","Ignis del Brasero",true),
        new Skin("noir_eclipse","Noir del Eclipse",true),
        new Skin("silas_obsidiana","Silas de Obsidiana",true),
        new Skin("amelia_flores","Amelia de las Flores",true),
        new Skin("sol_ambar","Sol del Ámbar",true),
        new Skin("magnus_cobre","Magnus de Cobre",true)
    );
    public static List<String> ids(){return ALL.stream().map(Skin::id).toList();}
    public static Skin get(String id){return ALL.stream().filter(s->s.id().equals(id)).findFirst().orElse(ALL.getFirst());}
    public static String label(String id){return get(id).name();}
}
