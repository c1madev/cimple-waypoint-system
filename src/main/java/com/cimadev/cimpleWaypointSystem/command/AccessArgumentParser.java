package com.cimadev.cimpleWaypointSystem.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;

public class AccessArgumentParser {
    public static AccessLevel accessLevelFromString(String access) throws CommandSyntaxException {
        switch (access) {
            case "public": return AccessLevel.PUBLIC;
            case "private": return AccessLevel.PRIVATE;
            case "secret": return AccessLevel.SECRET;
            case "open": return AccessLevel.OPEN;
            default: throw new SimpleCommandExceptionType(() -> "Invalid access type " + access + ".").create();
        }
    }

    public static AccessLevel accessLevelFromContext(CommandContext<ServerCommandSource> context, String id ) throws CommandSyntaxException {
        String access = StringArgumentType.getString(context, id);
        return accessLevelFromString( access );
    }
}
