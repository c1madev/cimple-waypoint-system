package com.cimadev.cimpleWaypointSystem.command.tpa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TpacceptCommand {
    private static final String COMMAND_NAME = "tpaccept";
    private static final Text NO_TPA_ERROR_MESSAGE =
            Text.literal("There is no ").formatted(Formatting.RED)
            .append(Text.literal("/tpa").formatted(Formatting.YELLOW))
            .append(Text.literal(" request open for you").formatted(Formatting.RED));

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess commandRegistryAccess,
            CommandManager.RegistrationEnvironment registrationEnvironment
    ) {
        dispatcher.register(CommandManager.literal(COMMAND_NAME)
                .executes(TpacceptCommand::acceptTeleport)
        );

    }

    private static int acceptTeleport(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TeleportRequest request = TeleportRequestManager.getInstance().removeRequest(
                context.getSource().getPlayerOrThrow()
        );
        if (request == null) {
            context.getSource().sendFeedback(() -> NO_TPA_ERROR_MESSAGE, false);
            return 0;
        }
        request.perform();
        context.getSource().sendFeedback(() -> Text.literal("Teleport! Or something..."), false);
        return 1;
    }
}
