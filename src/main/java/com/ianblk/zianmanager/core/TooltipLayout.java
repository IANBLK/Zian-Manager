package com.ianblk.zianmanager.core;
import java.util.ArrayList;
import java.util.List;
public final class TooltipLayout {
 private TooltipLayout(){}
 public enum Kind{KIND,LORE,BLANK,LEGEND,DETAIL,ABILITY,DURATION,COOLDOWN,RARITY,HINT}
 public record Line(Kind kind,String key,int arg){}
 public record Spec(String id,int durationSeconds,int cooldownSeconds){}
 public static String rarity(String id){if(id.startsWith("reimagined_"))return null;if(id.contains("common"))return "common";if(id.contains("rare"))return "rare";if(id.contains("epic"))return "epic";if(id.contains("cosmetic"))return "cosmetic";return null;}
 public static List<Line> lines(Spec spec,boolean expanded){String p="flavor.zianmanager."+spec.id();List<Line> out=new ArrayList<>();out.add(new Line(Kind.KIND,p+".kind",0));out.add(new Line(Kind.LORE,p+".line1",0));boolean ability=spec.cooldownSeconds()>0;
  if(!expanded){if(ability)out.add(new Line(Kind.COOLDOWN,"tooltip.zianmanager.cooldown",spec.cooldownSeconds()));out.add(new Line(Kind.HINT,"tooltip.zianmanager.shift",0));return out;}
  out.add(new Line(Kind.LORE,p+".line2",0));out.add(new Line(Kind.BLANK,"",0));out.add(new Line(Kind.LEGEND,p+".legend",0));out.add(new Line(Kind.DETAIL,p+".line3",0));
  if(ability){out.add(new Line(Kind.ABILITY,"tooltip.zianmanager.ability",0));if(spec.durationSeconds()>0)out.add(new Line(Kind.DURATION,"tooltip.zianmanager.duration",spec.durationSeconds()));out.add(new Line(Kind.COOLDOWN,"tooltip.zianmanager.cooldown",spec.cooldownSeconds()));}
  String r=rarity(spec.id());if(r!=null)out.add(new Line(Kind.RARITY,"tooltip.zianmanager.rarity."+r,0));return out;}
}
