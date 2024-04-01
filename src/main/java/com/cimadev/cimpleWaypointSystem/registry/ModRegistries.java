package com.cimadev.cimpleWaypointSystem.registry;

import com.cimadev.cimpleWaypointSystem.command.*;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;

public class ModRegistries {
    public static void registerAll() {
        registerCommands();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(WpsCommand::register);
        CommandRegistrationCallback.EVENT.register(HomeCommand::register);
        CommandRegistrationCallback.EVENT.register(SpawnCommand::register);
        //ArgumentTypeRegistry.registerArgumentType(new Identifier("cimple-waypoint-system", "access"), AccessArgumentType.class, ConstantArgumentSerializer.of(AccessArgumentType::access));
        //ArgumentTypeRegistry.registerArgumentType(new Identifier("cimple-waypoint-system", "offlineplayer"), OfflinePlayerArgumentType.class, ConstantArgumentSerializer.of(OfflinePlayerArgumentType::offlinePlayer));
    }
}
