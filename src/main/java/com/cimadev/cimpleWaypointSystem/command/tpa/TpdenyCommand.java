package com.cimadev.cimpleWaypointSystem.command.tpa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TpdenyCommand implements CommandRegistrationCallback {
    private static final String COMMAND_NAME = "tpdeny";
    private static final Text NO_TPA_ERROR =
            Text.literal("There is no").formatted(Formatting.RED)
            .append(Text.literal("/tpa").formatted(Formatting.YELLOW))
            .append(Text.literal(" request open for you").formatted(Formatting.RED));

    @Override
    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.register(
                CommandManager.literal(COMMAND_NAME)
                        .executes(TpdenyCommand::denyTeleport)
        );
    }

    public static int denyTeleport(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        TeleportRequest request = TeleportRequestManager.getInstance().removeRequest(player);
        if (request == null) {
            player.sendMessage(NO_TPA_ERROR);
            return 0;
        }
        PlayerEntity origin = request.getOrigin();
        player.sendMessage(
                Text.literal("The teleport request from ")
                        .append(origin.getName().copy().formatted(Formatting.YELLOW))
                        .append(" has been denied!")
        );
        origin.sendMessage(
                player.getName().copy()
                        .append(" has denied your teleport request")
        );
        return 1;
    }
}
