package com.cimadev.cimpleWaypointSystem.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;

public class AccessArgumentParser {
    public static int accessValueFromString(String access) throws CommandSyntaxException {
        switch (access) {
            case "public": return 0;
            case "private": return 1;
            case "secret": return 2;
            default: throw new SimpleCommandExceptionType(() -> "Invalid access type " + access + ".").create();
        }
    }

    public static int accessValueFromContext( CommandContext<ServerCommandSource> context, String id ) throws CommandSyntaxException {
        String access = StringArgumentType.getString(context, id);
        return accessValueFromString( access );
    }
}
