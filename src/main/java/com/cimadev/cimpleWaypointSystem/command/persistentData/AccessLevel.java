package com.cimadev.cimpleWaypointSystem.command;

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

    public static AccessLevel fromString(String name) throws IllegalArgumentException {
        switch (name) {
            case "secret": return AccessLevel.SECRET;
            case "private": return AccessLevel.PRIVATE;
            case "public": return AccessLevel.PUBLIC;
            case "open": return AccessLevel.OPEN;
            default: throw new IllegalArgumentException(name + " is not an acceptable access name.");
        }
    }

    public static AccessLevel fromContext(CommandContext<ServerCommandSource> context, String id ) throws CommandSyntaxException {
        String access = StringArgumentType.getString( context , id );
        try {
            return fromString( access );
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
