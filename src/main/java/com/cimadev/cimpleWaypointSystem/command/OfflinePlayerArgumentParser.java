package com.cimadev.cimpleWaypointSystem.command;

import com.cimadev.cimpleWaypointSystem.Main;
import com.cimadev.cimpleWaypointSystem.command.persistentData.OfflinePlayer;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.cimadev.cimpleWaypointSystem.Main.*;

public class OfflinePlayerArgumentParser {
    public static final DynamicCommandExceptionType INVALID_PLAYER_NAME = new DynamicCommandExceptionType(
            o -> Text.literal("Player ").append( Text.literal( o+"" ).formatted(LINK_INACTIVE_COLOR) ).append( " not found. Did they change their name?").formatted(DEFAULT_COLOR)
                    .formatted(Formatting.GRAY));

    public static OfflinePlayer offlinePlayerFromName(String name) throws CommandSyntaxException {
        OfflinePlayer p = Main.serverState.getPlayerByName( name );
        if ( p == null ) throw INVALID_PLAYER_NAME.create( name );
        return p;
    }

    public static OfflinePlayer offlinePlayerFromContext( CommandContext<ServerCommandSource> context, String id ) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(context, id);
        return offlinePlayerFromName( playerName );
    }
}
