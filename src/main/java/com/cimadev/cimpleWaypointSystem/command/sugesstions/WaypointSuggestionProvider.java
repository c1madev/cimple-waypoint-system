package com.cimadev.cimpleWaypointSystem.command.sugesstions;
import com.cimadev.cimpleWaypointSystem.command.WpsUtils;
import com.cimadev.cimpleWaypointSystem.command.persistentData.AccessLevel;
import com.cimadev.cimpleWaypointSystem.command.persistentData.OfflinePlayer;
import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class WaypointSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaypointSuggestionProvider.class);

    private final boolean withOwner;

    public WaypointSuggestionProvider() { this(false); }
    public WaypointSuggestionProvider(boolean withOwner) {
        this.withOwner = withOwner;
    }

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
            String suggestion;
            if (!withOwner)
                suggestion = waypoint.getName();
            else if (waypoint.getAccess() == AccessLevel.OPEN)
                suggestion = waypoint.getName() + " " + AccessLevel.OPEN.getName();
            else {
                OfflinePlayer owner = waypoint.getOwnerPlayer();
                if (owner == null) {
                    LOGGER.warn("Waypoint {} has unknown owner", waypoint.getName());
                    continue;
                }
                suggestion = waypoint.getName() + " " + owner.getName();
            }
            builder.suggest(suggestion);
        }

        return builder.buildFuture();
    }
}
