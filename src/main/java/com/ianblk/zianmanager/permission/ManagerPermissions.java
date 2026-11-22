package com.ianblk.zianmanager.permission;

import net.minecraft.commands.CommandSourceStack;
import java.util.function.Supplier;

public final class ManagerPermissions {
    private ManagerPermissions() {}

    public static boolean allows(CommandSourceStack source, String action, boolean administrative) {
        if (source.getPlayer() == null) return source.hasPermission(2);
        return evaluate(administrative ? source.hasPermission(2) : true, () -> {
            try {
                return LuckPermsLookup.lookup(source.getPlayer(), "zianmanager." + action);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    /** Guardar comandos de NPC (se ejecutan con nivel 4): exige OP 4 y que LuckPerms no lo deniegue; un TRUE solo no basta. */
    public static boolean allowsNpcCommands(CommandSourceStack source) {
        if (!source.hasPermission(4)) return false;
        if (source.getPlayer() == null) return true;
        try {
            return npcCommandsDecision(true, LuckPermsLookup.lookup(source.getPlayer(), "zianmanager.admin.npc-commands"));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            return false;
        }
    }

    static boolean npcCommandsDecision(boolean op4, LuckPermsLookup.Decision decision) {
        return op4 && decision != LuckPermsLookup.Decision.FALSE;
    }

    static boolean evaluate(boolean fallback, Supplier<LuckPermsLookup.Decision> lookup) {
        try {
            return switch (lookup.get()) {
                case TRUE -> true;
                case FALSE -> false;
                case ABSENT, UNDEFINED -> fallback;
            };
        } catch (RuntimeException | LinkageError exception) {
            return false;
        }
    }
}
