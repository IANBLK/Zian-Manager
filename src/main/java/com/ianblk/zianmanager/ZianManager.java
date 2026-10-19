package com.ianblk.zianmanager;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
@Mod("zianmanager")
public final class ZianManager {
    public static final Logger LOGGER=LogUtils.getLogger();
    public ZianManager(IEventBus bus,ModContainer container){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e)->PersonalRewards.forget(e.getEntity().getUUID()));
        if(FMLEnvironment.dist==Dist.CLIENT)ItemPresentation.register();
        ManagerBlocks.register(bus);ManagerEquipment.register(bus);ManagerCreative.register(bus);ManagerNpcs.register(bus);NpcTradeMenu.register(bus);ManagerNetwork.register(bus);new VaultCooldownRuntime();new ManagerRuntime();net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e)->PersonalRewards.clear());new ManagerCommands();HybridPublicAccess.register();new ManagerSmoke();new NpcTradeSmoke();
        if(FMLEnvironment.dist==Dist.CLIENT)com.ianblk.zianmanager.client.ManagerClient.init(bus);
        LOGGER.info("Zian Manager dungeon runtime initialized");
    }
}
