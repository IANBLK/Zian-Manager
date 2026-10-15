package com.ianblk.zianmanager.mixin;

import com.ianblk.zianmanager.VaultCooldownRuntime;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VaultBlockEntity.Server.class)
public abstract class VaultServerMixin {
    @WrapMethod(method="tryInsertKey")
    private static void zianmanager$insert(ServerLevel level,BlockPos pos,BlockState state,VaultConfig config,VaultServerData data,VaultSharedData shared,Player player,ItemStack stack,Operation<Void> original){
        Runnable vanilla=()->original.call(level,pos,state,config,data,shared,player,stack);
        if(VaultCooldownRuntime.INSTANCE==null){return;}
        VaultCooldownRuntime.INSTANCE.insert(level,pos,state,config,data,shared,player,stack,vanilla);
    }
    @WrapMethod(method="tick")
    private static void zianmanager$tick(ServerLevel level,BlockPos pos,BlockState state,VaultConfig config,VaultServerData data,VaultSharedData shared,Operation<Void> original){
        Runnable vanilla=()->original.call(level,pos,state,config,data,shared);
        if(VaultCooldownRuntime.INSTANCE==null){vanilla.run();return;}
        VaultCooldownRuntime.INSTANCE.tick(level,pos,data,vanilla);
    }
}
