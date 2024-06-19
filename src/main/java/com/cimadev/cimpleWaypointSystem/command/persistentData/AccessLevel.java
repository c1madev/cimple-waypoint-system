package com.cimadev.cimpleWaypointSystem.command.persistentData;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.cimadev.cimpleWaypointSystem.Main.*;

public enum AccessLevel {
    SECRET("secret", SECRET_COLOR),
    PRIVATE("private", PRIVATE_COLOR),
    PUBLIC("public", PUBLIC_COLOR),
    OPEN("open", OPEN_COLOR);

    private final String name;
    private final Text nameFormatted;

    AccessLevel(String name, Formatting color) {
        this.name = name;
        this.nameFormatted = Text.literal(this.name).formatted(color);
    }

    public static AccessLevel fromString(String name, boolean allowOpen) throws IllegalArgumentException {
        return switch (name) {
            case "secret" -> AccessLevel.SECRET;
            case "private" -> AccessLevel.PRIVATE;
            case "public" -> AccessLevel.PUBLIC;
            case "open" -> {
                if (allowOpen) yield AccessLevel.OPEN;
                throw new IllegalArgumentException("Access level OPEN is not allowed here");
            }
            default -> throw new IllegalArgumentException(name + " is not an acceptable access name.");
        };
    }

    public static AccessLevel fromContext(
            CommandContext<ServerCommandSource> context,
            String id
    ) throws CommandSyntaxException {
        return fromContext(context, id, true);
    }

    public static AccessLevel fromContext(
            CommandContext<ServerCommandSource> context,
            String id,
            boolean allowOpen
    ) throws CommandSyntaxException {
        String access = StringArgumentType.getString( context , id );
        try {
            return fromString( access , allowOpen );
        } catch ( IllegalArgumentException i ) {
            throw new SimpleCommandExceptionType(() -> "Invalid access type " + access + ".").create();
        }
    }

    public String getName() {
        return name;
    }

    public Text getNameFormatted() {
        return nameFormatted;
    }
}
