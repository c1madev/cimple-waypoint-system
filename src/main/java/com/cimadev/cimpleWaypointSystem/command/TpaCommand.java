package com.cimadev.cimpleWaypointSystem.command;

import com.cimadev.cimpleWaypointSystem.command.tpa.TeleportRequest;
import com.cimadev.cimpleWaypointSystem.command.tpa.TeleportRequestManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TpaCommand {
    private static final String COMMAND_NAME = "tpa";

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess commandRegistryAccess,
            CommandManager.RegistrationEnvironment registrationEnvironment
    ) {
        dispatcher.register(CommandManager.literal(COMMAND_NAME)
                .then(CommandManager.argument("target", EntityArgumentType.player())
                    .executes(TpaCommand::askTp)
                )
        );

    }

    private static int askTp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity origin = context.getSource().getPlayerOrThrow();
        EntitySelector entity = context.getArgument("target", EntitySelector.class);
        ServerPlayerEntity target = entity.getPlayer(context.getSource());

        TeleportRequestManager.getInstance().addRequest(new TeleportRequest(
                origin,
                target
        ));

        return 1;
    }
}
