package com.cimadev.cimpleWaypointSystem.command.tpa;

import com.cimadev.cimpleWaypointSystem.Colors;
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
import net.minecraft.util.Formatting;

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

        if (TeleportRequestManager.getInstance().hasRequest(target)) {
            origin.sendMessage(
                    target.getName().copy().formatted(Colors.PLAYER)
                    .append(
                            Text.literal(" already has a teleport request waiting. Try again later")
                            .formatted(Formatting.RED)
                    )
            );
            return 0;
        }
        final TeleportRequest request = new TeleportRequest(origin, target, false);
        TeleportRequestManager.getInstance().addRequest(request);
        TpaMessages.sendRequestMessages(request);

        return 1;
    }
}
