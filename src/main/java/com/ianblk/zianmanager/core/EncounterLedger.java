package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.core.Definitions.Point;
import java.util.*;
import java.nio.file.Path;
import java.io.IOException;
public final class EncounterLedger {
    public enum Phase { ACTIVE, WAITING, COMPLETE, REVIEW, CANCELLED }
    public record Spawn(UUID uuid,String template,Point point,boolean defeated){public Spawn{Objects.requireNonNull(uuid);Definitions.id(template);Objects.requireNonNull(point);}}
    public record Run(UUID uuid,String zone,int wave,Phase phase,long readyAt,List<Spawn> spawns,Set<UUID> players,int baseMobs,int scaledPlayers){
        public Run(UUID uuid,String zone,int wave,Phase phase,long readyAt,List<Spawn> spawns,Set<UUID> players){this(uuid,zone,wave,phase,readyAt,spawns,players,0,0);}
        public Run{Objects.requireNonNull(uuid);Definitions.id(zone);Objects.requireNonNull(phase);spawns=List.copyOf(spawns);players=Set.copyOf(players);if(wave<1 || wave>16 || readyAt<0 || spawns.size()>8 || players.size()>64 || baseMobs<0 || baseMobs>3 || scaledPlayers<0 || scaledPlayers>64)throw new IllegalArgumentException("Encuentro inválido");}
    }
    private record Data(int schema,Map<String,Run> runs){Data{if(schema!=1)throw new IllegalArgumentException("Versión desconocida");runs=Map.copyOf(runs);}}
    private final Path path;private Map<String,Run> runs;
    public EncounterLedger(Path path) throws IOException{this.path=path;var data=AtomicJson.read(path,Data.class,new Data(1,Map.of()));runs=data.runs;}
    public Run get(String zone){return runs.get(zone);}
    public Collection<Run> all(){return runs.values();}
    public void put(Run run) throws IOException{var next=new LinkedHashMap<>(runs);next.put(run.zone,run);AtomicJson.write(path,new Data(1,next));runs=Map.copyOf(next);}
    public void updateWait(String zone,int oldPause,int newPause,int oldCooldown,int newCooldown)throws IOException{var run=get(zone);if(run==null || run.phase()!=Phase.COMPLETE && run.phase()!=Phase.WAITING)return;int old=run.phase()==Phase.COMPLETE?oldCooldown:oldPause,next=run.phase()==Phase.COMPLETE?newCooldown:newPause;if(old==next)return;long ready=Math.max(0,run.readyAt()-(long)old*1000+(long)next*1000);put(new Run(run.uuid(),run.zone(),run.wave(),run.phase(),ready,run.spawns(),run.players(),run.baseMobs(),run.scaledPlayers()));}
    public boolean defeated(String zone,UUID runId,UUID entity,int totalWaves,int pause,int cooldown,long now) throws IOException{
        Run old=runs.get(zone);if(old==null || !old.uuid.equals(runId) || old.phase!=Phase.ACTIVE)return false;
        List<Spawn> next=new ArrayList<>();boolean changed=false;
        for(var slot:old.spawns){if(slot.uuid.equals(entity) && !slot.defeated){next.add(new Spawn(slot.uuid,slot.template,slot.point,true));changed=true;}else next.add(slot);}
        if(!changed)return false;
        boolean done=next.stream().allMatch(Spawn::defeated);Phase phase=done?(old.wave>=totalWaves?Phase.COMPLETE:Phase.WAITING):Phase.ACTIVE;
        put(new Run(old.uuid,old.zone,old.wave,phase,done?Math.addExact(now,Math.multiplyExact(1000L,phase==Phase.COMPLETE?cooldown:pause)):0,next,old.players,old.baseMobs,old.scaledPlayers));return true;
    }
}
