package com.ianblk.zianmanager.core;
import java.util.*;
import com.ianblk.zianmanager.core.Definitions.LootEntry;
public final class WeightedLoot {
    private WeightedLoot(){}
    public static List<LootEntry> independent(List<LootEntry> entries,Random random){
        var result=new ArrayList<LootEntry>();for(var entry:entries){if(entry.weight()<1 || entry.weight()>100)throw new IllegalArgumentException("Porcentaje fuera de 1–100");if(random.nextInt(100)<entry.weight())result.add(entry);}return List.copyOf(result);
    }
    public static List<LootEntry> select(List<LootEntry> entries,int count,Random random){
        var remaining=new ArrayList<>(entries);var result=new ArrayList<LootEntry>();
        for(int i=0;i<count && !remaining.isEmpty();i++){
            int total=remaining.stream().mapToInt(LootEntry::weight).sum();int roll=random.nextInt(total);
            for(int j=0;j<remaining.size();j++){roll-=remaining.get(j).weight();if(roll<0){result.add(remaining.remove(j));break;}}
        }
        return List.copyOf(result);
    }
}
