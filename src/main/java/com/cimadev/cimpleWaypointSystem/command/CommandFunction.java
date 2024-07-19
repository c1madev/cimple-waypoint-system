package com.cimadev.cimpleWaypointSystem.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface CommandFunction {
    int run() throws CommandSyntaxException;
}
