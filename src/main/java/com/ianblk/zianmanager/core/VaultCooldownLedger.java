package com.ianblk.zianmanager.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/** A durable reservation precedes vanilla's key consumption and queued loot. */
public final class VaultCooldownLedger {
    public record Vault(String dimension, int x, int y, int z) {
        public Vault { if (dimension == null || !dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("Invalid dimension"); }
        String filename() { return UUID.nameUUIDFromBytes((dimension+":"+x+":"+y+":"+z).getBytes(java.nio.charset.StandardCharsets.UTF_8))+".json"; }
    }
    public record Document(int schema, Vault vault, Map<UUID, Long> deadlines) {}
    public record Reservation(Vault vault, UUID player, Long previous, long deadline) {}
    @FunctionalInterface public interface Writer { void write(Path path, Document document) throws IOException; }
    private final Path directory;
    private final Writer writer;
    private final Map<Vault, Document> cache = new HashMap<>();
    private boolean healthy = true;
    public VaultCooldownLedger(Path directory) { this(directory, AtomicJson::write); }
    public VaultCooldownLedger(Path directory, Writer writer) { this.directory=directory; this.writer=writer; }
    private Document load(Vault vault) throws IOException {
        if (!healthy) throw new IOException("Vault ledger requires review");
        try {
            var document=cache.get(vault);
            if(document==null) {
                document=AtomicJson.read(directory.resolve(vault.filename()),Document.class,new Document(1,vault,new HashMap<>()));
                if(document==null || document.schema()!=1 || !vault.equals(document.vault()) || document.deadlines()==null
                    || document.deadlines().entrySet().stream().anyMatch(e->e.getKey()==null || e.getValue()==null || e.getValue()<0)) throw new IOException("Invalid vault ledger");
                cache.put(vault,document);
            }
            return document;
        } catch(IOException | RuntimeException failure) { healthy=false; throw new IOException("Vault ledger unavailable; openings blocked",failure); }
    }
    public synchronized long remaining(Vault vault, UUID player, long now) throws IOException {
        Long deadline=load(vault).deadlines().get(player);
        return deadline==null ? 0 : Math.max(0,deadline-Math.max(0,now));
    }
    public synchronized boolean known(Vault vault,UUID player) throws IOException { return load(vault).deadlines().containsKey(player); }
    /** Explicit administrator adjustment for one player and one vault; zero expires but retains adoption history. */
    public synchronized void adjust(Vault vault,UUID player,long now,long duration) throws IOException {
        if(now<0 || duration<0 || duration>30L*24*3600*1000)throw new IllegalArgumentException("Invalid vault cooldown");
        long deadline=Math.addExact(now,duration);
        var next=new HashMap<>(load(vault).deadlines());next.put(Objects.requireNonNull(player),deadline);
        save(new Document(1,vault,next));
    }
    public synchronized Set<UUID> expired(Vault vault,Set<UUID> rewarded,long now) throws IOException {
        var deadlines=load(vault).deadlines(); var result=new HashSet<UUID>();
        for(UUID id:rewarded) { Long deadline=deadlines.get(id); if(deadline!=null && deadline<=now)result.add(id); }
        return result;
    }
    public synchronized Reservation reserve(Vault vault,UUID player,long now,long duration) throws IOException {
        if(now<0 || duration<1000 || duration>30L*24*3600*1000)throw new IllegalArgumentException("Invalid vault cooldown");
        Document current=load(vault); Long previous=current.deadlines().get(player);
        if(previous!=null && previous>now)throw new IllegalStateException("Vault cooldown active");
        long deadline=Math.addExact(now,duration);var next=new HashMap<>(current.deadlines());next.put(player,deadline);
        save(new Document(1,vault,next));return new Reservation(vault,player,previous,deadline);
    }
    public synchronized void rollback(Reservation reservation) throws IOException {
        Document current=load(reservation.vault());
        if(!Objects.equals(current.deadlines().get(reservation.player()),reservation.deadline()))throw new IOException("Stale vault reservation");
        var next=new HashMap<>(current.deadlines());
        if(reservation.previous()==null)next.remove(reservation.player());else next.put(reservation.player(),reservation.previous());
        save(new Document(1,reservation.vault(),next));
    }
    private void save(Document document) throws IOException {
        try { writer.write(directory.resolve(document.vault().filename()),document); cache.put(document.vault(),document); }
        catch(IOException | RuntimeException failure) { healthy=false;throw new IOException("Vault reservation not confirmed; openings blocked",failure); }
    }
    public boolean healthy(){return healthy;}
}
