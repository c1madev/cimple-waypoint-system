package com.cimadev.cimpleWaypointSystem.command.suggestions;
import com.cimadev.cimpleWaypointSystem.command.WpsUtils;
import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;

public class WaypointSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaypointSuggestionProvider.class);

    private final boolean withOwner, removeImpossible;
    private final @Nullable BiPredicate<ServerCommandSource, Waypoint> predicate;

    public WaypointSuggestionProvider() { this(false, false); }
    public WaypointSuggestionProvider(boolean withOwner, boolean removeImpossible) {
        this(withOwner, removeImpossible, null);
    }
    public WaypointSuggestionProvider(
            boolean withOwner,
            boolean removeImpossible,
            @Nullable BiPredicate<ServerCommandSource, Waypoint> predicate
    ) {
        this.withOwner = withOwner;
        this.removeImpossible = removeImpossible;
        this.predicate = predicate;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String currentName;
        // TODO: This is not the cleanest as it forces an argument "name" to exist for this to work
        try {
            currentName = StringArgumentType.getString(context, "name");
        } catch (IllegalArgumentException e) {
            currentName = "";
        }
        List<Waypoint> waypoints = WpsUtils.getAccessibleWaypoints(player,null, false, false);
        for (Waypoint waypoint : waypoints) {
            if (removeImpossible && !waypoint.getName().toLowerCase().startsWith(currentName.toLowerCase()))
                continue;
            if (predicate != null && !predicate.test(context.getSource(), waypoint))
                continue;
            String suggestion;
            if (!withOwner)
                suggestion = waypoint.getNameForCommand();
            else {
                try {
                    suggestion = waypoint.getCommandComponent();
                } catch (IllegalStateException e) {
                    LOGGER.warn("Could not find owner of waypoint {}", waypoint.getName());
                    continue;
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Unowned secret (or other broken waypoint state) encountered", e);
                    continue;
                }
            }
            builder.suggest(suggestion);
        }

        return builder.buildFuture();
    }
}
