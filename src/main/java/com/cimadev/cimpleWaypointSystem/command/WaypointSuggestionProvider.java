package com.cimadev.cimpleWaypointSystem.command;
import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class WaypointSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        /* TODO: Implement more consistent suggestions.
            This class currently suggests open waypoints,
            however these are typically not used by the underlying commands.
            An example: /wps go open-waypoint will fail as /wps go open-waypoint open would be required.

            Possible avenues to fix this:
            1. Pass-through system for waypoint lookup.
                This would mean waypoints names that aren't found will
                be looked up in the open list, solving that problem.
            2. Add argument constructor with boolean settings.
                This would mean consistency but may be a worse solution overall.
                Still it will cover /wps remove and /wps rename better.
            3. Add more complicated permissions-based access determination
                Most involved option but possibly best for user experience.
         */
        ServerPlayerEntity player = context.getSource().getPlayer();
        ArrayList<Waypoint> waypoints = WpsUtils.getAccessibleWaypoints(player,null, false, false);
        for (Waypoint waypoint : waypoints) {
            builder.suggest(waypoint.getName());
        }

        return builder.buildFuture();
    }
}
