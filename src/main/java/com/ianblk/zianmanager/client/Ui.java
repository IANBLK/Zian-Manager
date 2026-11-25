package com.ianblk.zianmanager.client;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
/** Traduce en el cliente textos fijos del admin: busca el texto en español en es_es.json y usa su clave; si no existe (datos escritos por el admin) lo deja igual. */
public final class Ui {
 private Ui(){}
 private static final Pattern NUMBER=Pattern.compile("\\d+");
 private static Map<String,String> keys;
 private static synchronized Map<String,String> keys(){
  if(keys==null){var map=new HashMap<String,String>();
   try(var in=Ui.class.getResourceAsStream("/assets/zianmanager/lang/es_es.json")){
    JsonParser.parseString(new String(in.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject().entrySet().forEach(e->{String k=e.getKey();if(k.startsWith("zianmanager.ui.")||k.startsWith("zianmanager.msg.")||k.startsWith("ui.zianmanager."))map.putIfAbsent(e.getValue().getAsString(),k);});
   }catch(Exception ignored){}
   keys=map;}
  return keys;
 }
 /** Texto fijo; con argumentos, el español lleva %s. */
 public static String t(String es,Object... args){String key=keys().get(es);return key==null?(args.length==0?es:String.format(es,args)):Component.translatableWithFallback(key,es,args).getString();}
 public static Component c(String es,Object... args){return Component.literal(t(es,args));}
 /** Aviso del servidor: traduce cada tramo separado por " · " (con números como %s o con "texto: valor"); lo desconocido queda igual. */
 public static String notice(String text){
  if(text==null||text.isEmpty())return "";
  var out=new StringBuilder();
  for(String part:text.split(" · ",-1)){if(out.length()>0)out.append(" · ");out.append(segment(part));}
  return out.toString();
 }
 private static String segment(String part){
  if(keys().containsKey(part))return t(part);
  var numbers=new ArrayList<Object>();var shape=NUMBER.matcher(part).replaceAll(m->{numbers.add(m.group());return "%s";});
  if(!numbers.isEmpty()&&keys().containsKey(shape))return t(shape,numbers.toArray());
  int colon=part.indexOf(": ");
  if(colon>0){String head=part.substring(0,colon+2)+"%s";if(keys().containsKey(head))return t(head,part.substring(colon+2));}
  return part;
 }
 /** Nombre armado por el servidor con "dato del admin · detalles": el primer tramo se deja tal cual y solo se traducen los detalles. */
 public static String name(String text){
  if(text==null)return "";
  int cut=text.indexOf(" · ");
  return cut<0?text:text.substring(0,cut+3)+notice(text.substring(cut+3));
 }
 /** "Comercio · nombre del NPC": se traduce la palabra fija y el nombre se deja tal cual. */
 public static String title(String text){
  String head="Comercio · ";
  return text!=null&&text.startsWith(head)?t("Comercio")+" · "+text.substring(head.length()):text==null?"":text;
 }
 /** Etiquetas de oferta que genera el servidor ("ninguno", "⚠ objeto no disponible"); el resto es nombre de objeto y no se toca. */
 public static String offer(String text){
  if(text==null)return "";
  return text.replaceAll("(?<=^|\\+ |→ )ninguno(?= |$)",java.util.regex.Matcher.quoteReplacement(t("ninguno"))).replaceAll("(?<=^|\\+ |→ )⚠ objeto no disponible(?= |$)",java.util.regex.Matcher.quoteReplacement(t("⚠ objeto no disponible")));
 }
}
