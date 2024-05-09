package com.cimadev.cimpleWaypointSystem.command.tpa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
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

        TeleportRequestManager.getInstance().addRequest(new TeleportRequest(
                origin,
                target
        ));
        target.sendMessage(
                origin.getName().copy()
                .append(" has requested to teleport to you. Type ")
                .append(
                        Text.literal("/tpaccept")
                                .formatted(Formatting.YELLOW)
                                .setStyle(
                                        Style.EMPTY
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/tpaccept"
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Text.literal("Click here to accept")
                                        ))
                                )
                )
                .append(" to accept the request or ")
                .append(
                        Text.literal("/tpdeny")
                                .formatted(Formatting.RED)
                                .setStyle(
                                        Style.EMPTY
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/tpdeny"
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Text.literal("Click here to deny")
                                        ))
                                )
                )
                .append(" to deny it. The request will expire in")
                .append(Long.toString(TeleportRequestManager.getInstance().getRequestTTL()/20))
                .append(" seconds")
        );
        origin.sendMessage(
                origin.getName().copy()
                .append(" has received your teleport request.")
        );

        return 1;
    }
}
