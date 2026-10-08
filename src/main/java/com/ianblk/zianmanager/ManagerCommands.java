package com.ianblk.zianmanager;
import com.ianblk.zianmanager.permission.ManagerPermissions;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.UUID;
public final class ManagerCommands {
    public ManagerCommands(){NeoForge.EVENT_BUS.addListener(this::register);}
    private void register(RegisterCommandsEvent event){event.getDispatcher().register(Commands.literal("zianmanager")
        .executes(c->{var player=c.getSource().getPlayerOrException();ManagerRuntime.get().request(player,"root","");return 1;})
        .then(Commands.literal("givechest").requires(s->ManagerPermissions.allows(s,"admin",true)).executes(c->{var player=c.getSource().getPlayerOrException();player.getInventory().add(new ItemStack(ManagerBlocks.CRATES.get("loot_common_crate").get().asItem()));return 1;}))
        .then(Commands.literal("pending").requires(s->ManagerPermissions.allows(s,"loot",false)).executes(c->{var player=c.getSource().getPlayerOrException();for(var claim:ManagerRuntime.get().loot().pending(player.getUUID()))player.sendSystemMessage(Component.literal(claim.id()+" · "+claim.trainer()+(claim.review()?" · REVISIÓN":" · PENDIENTE")).withStyle(style->style.withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,"/zianmanager claim "+claim.id()))));return 1;}))
        .then(Commands.literal("claim").requires(s->ManagerPermissions.allows(s,"loot",false)).then(Commands.argument("id",StringArgumentType.word()).suggests((c,b)->SharedSuggestionProvider.suggest(ManagerRuntime.get().loot().pending(c.getSource().getPlayerOrException().getUUID()).stream().map(v->v.id().toString()),b)).executes(c->{try{ManagerRuntime.get().loot().deliver(c.getSource().getPlayerOrException(),UUID.fromString(StringArgumentType.getString(c,"id")));return 1;}catch(Exception error){c.getSource().sendFailure(Component.literal(error.getMessage()));return 0;}})))
        .then(Commands.literal("resolvekey").requires(s->ManagerPermissions.allows(s,"admin",true)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("id",StringArgumentType.word()).suggests((c,b)->SharedSuggestionProvider.suggest(ManagerRuntime.get().loot().keyReviews(EntityArgument.getPlayer(c,"player").getUUID()).stream().map(v->v.id().toString()),b)).then(Commands.argument("evidence",StringArgumentType.greedyString()).executes(c->{try{var admin=c.getSource().getPlayerOrException();ManagerRuntime.get().loot().resolveKey(UUID.fromString(StringArgumentType.getString(c,"id")),EntityArgument.getPlayer(c,"player").getUUID(),admin.getUUID(),StringArgumentType.getString(c,"evidence"));return 1;}catch(Exception error){c.getSource().sendFailure(Component.literal(error.getMessage()));return 0;}})))))
        .then(Commands.literal("resolve").requires(s->ManagerPermissions.allows(s,"admin",true)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("id",StringArgumentType.word()).suggests((c,b)->SharedSuggestionProvider.suggest(ManagerRuntime.get().loot().reviewClaims(EntityArgument.getPlayer(c,"player").getUUID()).stream().map(v->v.id().toString()),b)).then(Commands.argument("part",IntegerArgumentType.integer(1,32)).suggests((c,b)->{try{return SharedSuggestionProvider.suggest(ManagerRuntime.get().loot().reviewParts(EntityArgument.getPlayer(c,"player").getUUID(),UUID.fromString(StringArgumentType.getString(c,"id"))),b);}catch(IllegalArgumentException error){return b.buildFuture();}}).then(Commands.argument("evidence",StringArgumentType.greedyString()).executes(c->{try{var admin=c.getSource().getPlayerOrException();ManagerRuntime.get().loot().resolve(EntityArgument.getPlayer(c,"player").getUUID(),UUID.fromString(StringArgumentType.getString(c,"id")),IntegerArgumentType.getInteger(c,"part")-1,admin.getUUID(),StringArgumentType.getString(c,"evidence"));return 1;}catch(Exception error){c.getSource().sendFailure(Component.literal(error.getMessage()));return 0;}}))))))
    );}
}
