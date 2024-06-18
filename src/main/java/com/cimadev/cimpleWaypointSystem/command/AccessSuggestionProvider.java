package com.cimadev.cimpleWaypointSystem.command;

import com.cimadev.cimpleWaypointSystem.command.persistentData.AccessLevel;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class AccessSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        for (AccessLevel access : AccessLevel.values() ) {
            if ( access == AccessLevel.OPEN && !context.getSource().hasPermissionLevel(3) ) continue;
            builder.suggest(access.getName());
        }
        return builder.buildFuture();
    }
}
