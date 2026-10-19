package com.ianblk.zianmanager;

import com.ianblk.zianmanager.core.AtomicJson;
import com.ianblk.zianmanager.core.VaultCooldownLedger;
import com.ianblk.zianmanager.permission.ManagerPermissions;
import java.nio.file.Path;
import java.time.Clock;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;

/** Server-only policy. The native vault still owns loot, key payment and animation. */
public final class VaultCooldownRuntime {
    public record Settings(boolean enabled,int normalMinutes,int ominousMinutes) {
        public static Settings defaults(){return new Settings(true,30,60);}
        public Settings { if(normalMinutes<1 || normalMinutes>43200 || ominousMinutes<1 || ominousMinutes>43200)throw new IllegalArgumentException("Vault minutes must be 1..43200"); }
    }
    public static VaultCooldownRuntime INSTANCE;
    private VaultCooldownLedger ledger;
    private Settings settings;
    private Path config;
    private Clock clock=Clock.systemUTC();
    private boolean failed;
    public VaultCooldownRuntime(){
        INSTANCE=this;
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e)->{
            Path root=e.getServer().getWorldPath(LevelResource.ROOT).resolve("data/zianmanager");
            config=root.resolve("vault-cooldowns-settings.json");ledger=new VaultCooldownLedger(root.resolve("vault-cooldowns"));
            reload();
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e)->{ledger=null;settings=null;});
        NeoForge.EVENT_BUS.addListener(this::commands);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::click);
        new VaultCooldownSmoke();
    }
    private void reload(){try{
        var loaded=AtomicJson.read(config,Settings.class,Settings.defaults());
        if(loaded==null)throw new IllegalArgumentException("Empty vault settings");
        settings=loaded;AtomicJson.write(config,settings);failed=false;
    }catch(Exception error){fail(error);}}
    private void fail(Exception error){if(!failed)ZianManager.LOGGER.error("Vault cooldown policy failed closed; native openings blocked until reviewed",error);failed=true;}
    private static VaultCooldownLedger.Vault id(ServerLevel level,BlockPos pos){return new VaultCooldownLedger.Vault(level.dimension().location().toString(),pos.getX(),pos.getY(),pos.getZ());}
    private boolean enabled(){return failed || settings==null || settings.enabled();}
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<net.minecraft.commands.CommandSourceStack,Integer> minutes(String name,int min){
        return Commands.argument(name,com.mojang.brigadier.arguments.IntegerArgumentType.integer(min,43200)).suggests((c,b)->net.minecraft.commands.SharedSuggestionProvider.suggest(min==0?List.of("0","1","5","30","60"):List.of("1","5","15","30","60"),b));
    }
    private static int number(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c,String name){return com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,name);}
    private Settings changeTimes(String kind,int value){
        var current=settings==null?Settings.defaults():settings;
        return new Settings(current.enabled(),kind.equals("ominous")?current.normalMinutes():value,kind.equals("normal")?current.ominousMinutes():value);
    }
    private int help(net.minecraft.commands.CommandSourceStack source){
        source.sendSuccess(()->Component.literal("Bóvedas: times normal|ominous <minutos>, times <normal> <ominous>, reset (quita tu espera en la bóveda que miras), cooldown <minutos> (ajusta tu espera), status, enabled true|false, reload. Usa Tab para completar. Cambiar times solo afecta a las siguientes aperturas."),false);return 1;
    }
    private void commands(RegisterCommandsEvent event){
        var times=Commands.literal("times").executes(c->help(c.getSource()));
        times.then(minutes("normalMinutes",1)
            .executes(c->configure(c.getSource(),changeTimes("both",number(c,"normalMinutes"))))
            .then(minutes("ominousMinutes",1).executes(c->configure(c.getSource(),new Settings(settings==null || settings.enabled(),number(c,"normalMinutes"),number(c,"ominousMinutes")))))
            .then(Commands.literal("normal").executes(c->configure(c.getSource(),changeTimes("normal",number(c,"normalMinutes")))))
            .then(Commands.literal("ominous").executes(c->configure(c.getSource(),changeTimes("ominous",number(c,"normalMinutes"))))));
        for(String kind:List.of("normal","ominous","both"))times.then(Commands.literal(kind).then(minutes("minutes",1).executes(c->configure(c.getSource(),changeTimes(kind,number(c,"minutes"))))));
        var root=Commands.literal("zianmanager").then(Commands.literal("vaults").requires(s->ManagerPermissions.allows(s,"admin",true)).executes(c->help(c.getSource()))
            .then(Commands.literal("help").executes(c->help(c.getSource())))
            .then(Commands.literal("status").executes(c->{c.getSource().sendSuccess(()->Component.literal("Bóvedas: "+(failed || ledger==null || !ledger.healthy()?"bloqueadas por error":settings.enabled()?"activadas":"vanilla")+" · normales "+(settings==null?"?":settings.normalMinutes())+" min · ominosas "+(settings==null?"?":settings.ominousMinutes())+" min · sin excepción para OP"),false);return 1;}))
            .then(times)
            .then(Commands.literal("reset").executes(c->adjustLookedAt(c.getSource(),0)))
            .then(Commands.literal("cooldown").executes(c->help(c.getSource())).then(minutes("minutes",0).executes(c->adjustLookedAt(c.getSource(),number(c,"minutes")))))
            .then(Commands.literal("enabled").then(Commands.argument("enabled",com.mojang.brigadier.arguments.BoolArgumentType.bool()).executes(c->configure(c.getSource(),new Settings(com.mojang.brigadier.arguments.BoolArgumentType.getBool(c,"enabled"),settings==null?30:settings.normalMinutes(),settings==null?60:settings.ominousMinutes())))))
            .then(Commands.literal("reload").executes(c->{reload();if(failed){c.getSource().sendFailure(Component.literal("Configuración inválida; revisa el registro."));return 0;}c.getSource().sendSuccess(()->Component.literal("Configuración de bóvedas recargada. Los tiempos ya iniciados se conservan; usa cooldown mirando una bóveda para ajustar tu espera."),false);return 1;})));
        event.getDispatcher().register(root);
    }
    private int adjustLookedAt(net.minecraft.commands.CommandSourceStack source,int minutes) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player=source.getPlayerOrException();var hit=player.pick(6,1,false);
        if(!(hit instanceof net.minecraft.world.phys.BlockHitResult block) || hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK || !(source.getLevel().getBlockEntity(block.getBlockPos()) instanceof VaultBlockEntity)){
            source.sendFailure(Component.literal("Mira una bóveda a un máximo de 6 bloques para ajustar tu espera."));return 0;
        }
        try{
            if(failed || ledger==null || !ledger.healthy())throw new IllegalStateException("Vault ledger unavailable");
            ledger.adjust(id(source.getLevel(),block.getBlockPos()),player.getUUID(),clock.millis(),minutes*60000L);
            source.sendSuccess(()->Component.literal(minutes==0?"Tu espera en esta bóveda terminó. Puedes usar una llave cuando esté disponible.":"Tu espera en esta bóveda queda en "+minutes+" min desde ahora. Los demás jugadores conservan su tiempo."),false);return 1;
        }catch(Exception error){fail(error);source.sendFailure(Component.literal("No se pudo ajustar la espera; revisa el registro."));return 0;}
    }
    private int configure(net.minecraft.commands.CommandSourceStack source,Settings next){
        try{if(config==null || ledger==null || !ledger.healthy())throw new IllegalStateException("Revisa el almacenamiento antes de cambiar la configuración.");
            AtomicJson.write(config,next);settings=next;failed=false;
            source.sendSuccess(()->Component.literal("Bóvedas "+(next.enabled()?"activadas":"en modo vanilla")+": "+next.normalMinutes()+" min normales, "+next.ominousMinutes()+" min ominosas. Para cambiar tu espera actual, mira la bóveda y usa /zianmanager vaults cooldown 1."),true);return 1;
        }catch(Exception error){fail(error);source.sendFailure(Component.literal("No se pudo guardar la configuración de bóvedas."));return 0;}
    }
    public Settings settingsSnapshot(){return settings==null?Settings.defaults():settings;}
    public String statusText(){return failed||ledger==null||!ledger.healthy()?"Bóvedas bloqueadas por error":settingsSnapshot().enabled()?"Reutilización activada":"Comportamiento vanilla";}
    public void saveFromUi(net.minecraft.server.level.ServerPlayer p,Settings next){if(!ManagerPermissions.allows(p.createCommandSourceStack(),"admin",true))throw new IllegalArgumentException("Necesitas permiso administrativo");if(configure(p.createCommandSourceStack(),next)!=1)throw new IllegalStateException("No se guardaron los ajustes de bóvedas");}
    public void adjustFromUi(net.minecraft.server.level.ServerPlayer p,int minutes)throws com.mojang.brigadier.exceptions.CommandSyntaxException{if(minutes<0||minutes>43200||!ManagerPermissions.allows(p.createCommandSourceStack(),"admin",true))throw new IllegalArgumentException("Espera o permiso inválidos");if(adjustLookedAt(p.createCommandSourceStack(),minutes)!=1)throw new IllegalArgumentException("Mira una bóveda a un máximo de 6 bloques");}
    public void reloadFromUi(){reload();if(failed)throw new IllegalStateException("No se pudo recargar la configuración de bóvedas");}
    private void click(PlayerInteractEvent.RightClickBlock e){
        if(e.isCanceled() || !(e.getLevel() instanceof ServerLevel level) || !level.getBlockState(e.getPos()).is(net.minecraft.world.level.block.Blocks.VAULT) || !enabled())return;
        if(!(level.getBlockEntity(e.getPos()) instanceof VaultBlockEntity vault))return;
        var key=vault.getConfig().keyItem();var held=e.getItemStack();
        if(!ItemStack.isSameItemSameComponents(held,key) || held.getCount()<key.getCount())return;
        try {
            if(failed || ledger==null)throw new IllegalStateException("Vault runtime unavailable");
            var vaultId=id(level,e.getPos());var uuid=e.getEntity().getUUID();
            if(vault.getServerData().getRewardedPlayers().contains(uuid) && !ledger.known(vaultId,uuid)) {
                ledger.reserve(vaultId,uuid,clock.millis(),duration(level.getBlockState(e.getPos())));
            }
            long left=ledger.remaining(vaultId,uuid,clock.millis());
            if(left>0){e.setCanceled(true);message(e.getEntity(),left);}
        }catch(Exception error){fail(error);e.setCanceled(true);e.getEntity().displayClientMessage(Component.literal("Bóveda temporalmente no disponible."),true);}
    }
    private static void message(Player player,long millis){long seconds=(millis+999)/1000;player.displayClientMessage(Component.literal("Puedes volver a usar esta bóveda en "+seconds/60+" min "+seconds%60+" s."),true);}
    public void insert(ServerLevel level,BlockPos pos,BlockState state,VaultConfig config,VaultServerData data,VaultSharedData shared,Player player,ItemStack stack,Runnable vanilla){
        if(!enabled()){vanilla.run();return;}
        if(failed || ledger==null)return;
        // Do not reserve an inactive/busy vault, an invalid key or an empty loot table.
        if(state.getValue(VaultBlock.STATE)!=VaultState.ACTIVE)return;
        if(config.lootTable().equals(net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY)
            || config.keyItem().isEmpty() || !ItemStack.isSameItemSameComponents(stack,config.keyItem()) || stack.getCount()<config.keyItem().getCount()) {vanilla.run();return;}
        var vault=id(level,pos);UUID uuid=player.getUUID();boolean wasRewarded=data.getRewardedPlayers().contains(uuid);
        VaultCooldownLedger.Reservation reservation;
        try {
            long now=clock.millis(),left=ledger.remaining(vault,uuid,now);
            if(left>0){message(player,left);return;}
            // Existing vanilla redemptions have no reliable timestamp. Start one wait on first encounter.
            if(wasRewarded && !ledger.known(vault,uuid)){
                ledger.reserve(vault,uuid,now,duration(state));message(player,duration(state));return;
            }
            reservation=ledger.reserve(vault,uuid,now,duration(state));
        }catch(Exception error){fail(error);return;}
        data.getRewardedPlayers().remove(uuid);
        try {
            vanilla.run();
            boolean opened=data.getRewardedPlayers().contains(uuid);
            if(!opened){if(wasRewarded)data.getRewardedPlayers().add(uuid);ledger.rollback(reservation);}
        }catch(RuntimeException | Error error){
            if(wasRewarded)data.getRewardedPlayers().add(uuid);
            // Native effects may already have happened. Retain the durable reservation, never retry blindly.
            throw error;
        }catch(java.io.IOException error){fail(error);}
    }
    private long duration(BlockState state){return 60000L*(state.getValue(VaultBlock.OMINOUS)?settings.ominousMinutes():settings.normalMinutes());}
    public void tick(ServerLevel level,BlockPos pos,VaultServerData data,Runnable vanilla){
        if(!enabled() || failed || ledger==null){vanilla.run();return;}
        Set<UUID> mask;
        try {mask=ledger.expired(id(level,pos),data.getRewardedPlayers(),clock.millis());}
        catch(Exception error){fail(error);vanilla.run();return;}
        // Let native detection activate for eligible returning players; retain the original permanent record.
        data.getRewardedPlayers().removeAll(mask);
        try{vanilla.run();}finally{data.getRewardedPlayers().addAll(mask);}
    }
    void testClock(Clock clock){this.clock=clock;}
    Settings testSettings(){return settings;}
    VaultCooldownLedger testLedger(){return ledger;}
}
