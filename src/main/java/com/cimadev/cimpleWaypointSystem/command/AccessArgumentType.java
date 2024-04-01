package com.cimadev.cimpleWaypointSystem.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class AccessArgumentType implements ArgumentType<Integer> {

    public static AccessArgumentType access() {
        return new AccessArgumentType();
    }
    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int argStart = reader.getCursor();
        if ( !reader.canRead() ) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        String accessString = reader.getString().substring(argStart, reader.getCursor());

        switch (accessString) {
            case "public": return 0;
            case "private": return 1;
            case "secret": return 2;
            default: return -1; //throw new SimpleCommandExceptionType(() -> "Invalid access type.").create();
        }
    }

    public static <S> Integer getAccess( CommandContext<S> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ArgumentType.super.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }
}
