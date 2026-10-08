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
    public ZianManager(IEventBus bus,ModContainer container){
        ManagerBlocks.register(bus);ManagerNetwork.register(bus);new ManagerRuntime();new ManagerCommands();new ManagerSmoke();
        if(FMLEnvironment.dist==Dist.CLIENT)com.ianblk.zianmanager.client.ManagerClient.init();
        LOGGER.info("Zian Manager dungeon runtime initialized");
    }
}
