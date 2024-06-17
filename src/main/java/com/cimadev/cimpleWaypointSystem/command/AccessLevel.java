package com.cimadev.cimpleWaypointSystem.command;

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

    public static AccessLevel fromString(String name) {
        switch (name) {
            case "private": return AccessLevel.PRIVATE;
            case "public": return AccessLevel.PUBLIC;
            case "open": return AccessLevel.OPEN;
            default: return AccessLevel.SECRET;
        }
    }

    public String getName() {
        return name;
    }

    public Text getNameFormatted() {
        return nameFormatted;
    }
}
