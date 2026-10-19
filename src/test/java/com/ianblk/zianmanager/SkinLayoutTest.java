package com.ianblk.zianmanager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SkinLayoutTest {
 @Test void suppliedSkinModelsMatchTheirBundledUVLayout()throws Exception{
  var skins=com.ianblk.zianmanager.core.NpcSkins.ALL;
  assertEquals(skins.size(),skins.stream().map(s->s.id()).distinct().count());
  for(var s:skins){if(!s.id().matches("alaric_zafiro|soren_hielo|nyx_umbral|iris_prismatica|kael_invierno|elian_alba|darian_roble|varek_carmesi|liora_arena|vigia_sculk|morgana_cosecha|heraldo_vacio|arlequin_estelar|sombra_violeta|centinela_carmesi|nadir_medianoche|guardia_cobalto|serafina_nieves|lucien_ocaso|mika_rosada|espectro_sculk|ignis_brasero|noir_eclipse|silas_obsidiana|amelia_flores|sol_ambar|magnus_cobre"))continue;
   try(var in=getClass().getResourceAsStream("/assets/zianmanager/textures/entity/npcs/"+s.id()+".png")){assertNotNull(in,s.id());var image=javax.imageio.ImageIO.read(in);assertEquals(64,image.getWidth());assertEquals(64,image.getHeight());boolean slim=true;for(int y=0;y<12;y++)for(int x=0;x<2;x++)slim&=(image.getRGB(54+x,20+y)>>>24)==0&&(image.getRGB(46+x,52+y)>>>24)==0;assertEquals(slim,s.slim(),s.id());}
  }
 }
}
