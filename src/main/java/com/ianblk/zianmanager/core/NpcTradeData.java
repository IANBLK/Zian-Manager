package com.ianblk.zianmanager.core;
import com.google.gson.*;
import java.util.*;
/** Pure persistent offer schema and lossless schema-1 migration. */
public final class NpcTradeData {
 private NpcTradeData(){}
    public record Offer(List<String> payments,List<String> results) {
        public Offer { payments=validateSide(payments);results=validateSide(results); }
        public Offer(String payment,String extra,String result){this(legacyPayments(payment,extra),Collections.singletonList(result));}
        private static List<String> legacyPayments(String first,String second){if(first==null||first.isBlank()||second==null)throw new IllegalArgumentException("Pago inválido");return second.isEmpty()?List.of(first):List.of(first,second);}
        private static List<String> validateSide(List<String> rows){if(rows==null||rows.isEmpty()||rows.size()>4||rows.stream().anyMatch(s->s==null||s.isBlank()||s.length()>32768))throw new IllegalArgumentException("Cada lado necesita entre 1 y 4 pilas válidas");return List.copyOf(rows);}
    }
    public record Data(int schema,Map<UUID,List<Offer>> npcs) {
        public Data { if(schema!=2||npcs==null||npcs.size()>4096)throw new IllegalArgumentException("Archivo de comercios inválido");var copy=new LinkedHashMap<UUID,List<Offer>>();npcs.forEach((id,offers)->{if(id==null||offers==null||offers.size()>64)throw new IllegalArgumentException("Máximo 64 ofertas por NPC");copy.put(id,List.copyOf(offers));});npcs=Collections.unmodifiableMap(copy); }
    }
    public static Data decode(JsonObject raw){
        int schema=raw.get("schema").getAsInt();if(schema==2)return AtomicJson.GSON.fromJson(raw,Data.class);if(schema!=1)throw new IllegalArgumentException("Formato de comercios desconocido");
        var next=new LinkedHashMap<UUID,List<Offer>>();for(var entry:raw.getAsJsonObject("npcs").entrySet()){var rows=new ArrayList<Offer>();for(var value:entry.getValue().getAsJsonArray()){var row=value.getAsJsonObject();rows.add(new Offer(row.get("payment").getAsString(),row.get("extra").getAsString(),row.get("result").getAsString()));}next.put(UUID.fromString(entry.getKey()),rows);}return new Data(2,next);
    }
}
