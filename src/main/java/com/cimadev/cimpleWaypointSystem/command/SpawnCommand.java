package com.cimadev.cimpleWaypointSystem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.function.Supplier;

import static com.cimadev.cimpleWaypointSystem.Main.DEFAULT_COLOR;

public class SpawnCommand {

    private static final String COMMAND_NAME = "spawn";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {

        dispatcher.register(CommandManager.literal(COMMAND_NAME)
                .executes(SpawnCommand::goSpawn)
                .then(CommandManager.literal("help")
                        .executes(SpawnCommand::help)));
    }

    public static int goSpawn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource commandSource = context.getSource();
        ServerPlayerEntity player = commandSource.getPlayerOrThrow();
        ServerWorld overworld = player.getServer().getOverworld();
        BlockPos spawn = overworld.getSpawnPos();
        Optional<Vec3d> teleportPosMaybe = ServerPlayerEntity.findRespawnPosition(overworld, spawn, 0, true, true);
        if ( teleportPosMaybe.isEmpty() ) return -1;
        Vec3d teleportPos = teleportPosMaybe.get();
        player.teleport(overworld, teleportPos.getX(), teleportPos.getY(), teleportPos.getZ(), 0, 0);
        player.requestTeleport(spawn.getX(), spawn.getY(), spawn.getZ());
        Supplier<Text> messageText = () -> Text.literal("Teleported to the spawnpoint.").formatted(DEFAULT_COLOR);
        commandSource.sendFeedback(messageText, false);
        return 1;
    }

    public static int help(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText = () -> Text.literal("The command /spawn takes you to the overworld's default spawn point.").formatted(DEFAULT_COLOR);
        context.getSource().sendFeedback(messageText, false);
        return 1;
    }
}
