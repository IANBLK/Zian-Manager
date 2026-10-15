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
        new Skin("guardian_boreal","Guardián Boreal",false)
    );
    public static List<String> ids(){return ALL.stream().map(Skin::id).toList();}
    public static Skin get(String id){return ALL.stream().filter(s->s.id().equals(id)).findFirst().orElse(ALL.getFirst());}
    public static String label(String id){return get(id).name();}
}
