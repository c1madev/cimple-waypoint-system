package com.cimadev.cimpleWaypointSystem.command.sugesstions;

import com.cimadev.cimpleWaypointSystem.command.persistentData.AccessLevel;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class AccessSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    private static AccessLevel[] accessLevels = AccessLevel.values();
    private final AccessLevel[] includeAccessLevels;
    private final Predicate<ServerCommandSource> override;

    public AccessSuggestionProvider(AccessLevel... exclude) {
        /* Exclude any access level from a suggestion provider by passing it to the access provider. */
        this(source -> (false), exclude);
    }

    public AccessSuggestionProvider(final Predicate<ServerCommandSource> override, AccessLevel... exclude) {
        /*
        override: pass a function which overrides the exclusion of access levels in getSuggestions. Intended for permission levels.
        exclude: any number of access levels which to exclude from suggestion if not overridden
         */
        this.override = override;
        int includedCounter = 0;
        includeAccessLevels = new AccessLevel[ accessLevels.length - exclude.length ];
        for( int i = 0, j = 0 ; i < accessLevels.length ; i++ ) {
            boolean include = true;
            for ( AccessLevel e : exclude ) {
                if (accessLevels[i] == e) include = false;
            }
            if (include == true) {
                includeAccessLevels[includedCounter] = accessLevels[i];
                includedCounter++;
            }
        }
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        AccessLevel[] list = (override.test( context.getSource()) ? accessLevels : includeAccessLevels );
        for (AccessLevel access : list ) {
            builder.suggest(access.getName());
        }
        return builder.buildFuture();
    }
}
