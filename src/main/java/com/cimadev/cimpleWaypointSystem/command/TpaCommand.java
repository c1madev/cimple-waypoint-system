package com.cimadev.cimpleWaypointSystem.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class TpaCommand {
    private static final String COMMAND_NAME = "tpa";

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess commandRegistryAccess,
            CommandManager.RegistrationEnvironment registrationEnvironment
    ) {
        dispatcher.register(CommandManager.literal(COMMAND_NAME)
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(TpaCommand::askTp)
                )
        );

    }

    private static int askTp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        EntitySelector entity = context.getArgument("player", EntitySelector.class);
        ServerPlayerEntity player;
        player = entity.getPlayer(context.getSource());
        // TODO: Issue a teleport request
        context.getSource().sendFeedback(player::getName, false);

        return 1;
    }
}
