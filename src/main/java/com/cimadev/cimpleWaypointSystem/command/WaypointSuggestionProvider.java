package com.cimadev.cimpleWaypointSystem.command;
import com.cimadev.cimpleWaypointSystem.command.persistentData.OfflinePlayer;
import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WaypointSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        /**
         * Implements a passthrough-and-alias system for quicker waypoint access.
         * Your own and open waypoints can simply be acccessed via the name.
         * Waypoints owned by other users (that are accessible) are prefixed with <owner>/
         */
        ServerPlayerEntity player = context.getSource().getPlayer();
        ArrayList<Waypoint> waypoints = WpsUtils.getAccessibleWaypoints(player,null, false, false);
        for (Waypoint waypoint : waypoints) {
            UUID ownerUuid = waypoint.getOwner();
            if (
                ownerUuid == null
                || (
                    player != null
                    && player.getUuid().equals(ownerUuid)
                )
            ) {
                builder.suggest(waypoint.getName());
            } else {
                OfflinePlayer owner = waypoint.getOwnerPlayer();
                if (owner != null) {
                    builder.suggest(owner.getName() + "/" + waypoint.getName());
                }
            }
        }

        return builder.buildFuture();
    }
}
