package com.cimadev.cimpleWaypointSystem.command;

import com.cimadev.cimpleWaypointSystem.Main;
import com.cimadev.cimpleWaypointSystem.command.persistentData.OfflinePlayer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class OfflinePlayerArgumentType implements ArgumentType<OfflinePlayer> {
    public static final DynamicCommandExceptionType INVALID_PLAYER_NAME = new DynamicCommandExceptionType(
            o -> Text.literal("Player ").append( Text.literal( o+"" ).formatted(Formatting.RED) ).append( " not found. Did they change their name?")
            .formatted(Formatting.GRAY));


    public static OfflinePlayerArgumentType offlinePlayer() {
        return new OfflinePlayerArgumentType();
    }
    @Override
    public OfflinePlayer parse(StringReader reader) throws CommandSyntaxException {
        String playerName = parseLiteral(reader);
        OfflinePlayer p = Main.serverState.getPlayerByName( playerName );
        if ( p == null ) throw INVALID_PLAYER_NAME.create( playerName );
        return p;
    }

    private static String parseLiteral(StringReader reader) {
        int argStart = reader.getCursor();
        if ( !reader.canRead() ) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        return reader.getString().substring(argStart, reader.getCursor());
    }

    public static <S> OfflinePlayer getOfflinePlayer( CommandContext<S> context, String name) {
        OfflinePlayer p;
        try {
            p = context.getArgument(name, OfflinePlayer.class);
        } catch (Exception e) {
            p = null;
        }
        return p;
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
