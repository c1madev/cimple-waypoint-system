package com.cimadev.cimpleWaypointSystem.command.tpa;

import com.cimadev.cimpleWaypointSystem.Colors;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class TphereCommand {
    public static final SimpleCommandExceptionType SELF_TELEPORT_EXC = new SimpleCommandExceptionType(
            Text.literal("You cannot teleport to yourself!")
    );

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.register(CommandManager.literal("tphere")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(TphereCommand::execute)
        ));
    }

    private static MutableText literalCommand(String command, String hover) {
        return Text.literal(command).setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover)))
        );
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity origin = context.getSource().getPlayerOrThrow(),
                target = EntityArgumentType.getPlayer(context, "player");
        if (origin.equals(target)) {
            throw SELF_TELEPORT_EXC.create();
        }
        TeleportRequestManager.getInstance().addRequest(new TeleportRequest(origin, target, true));
        target.sendMessage(Text.literal("")
                .append(origin.getName().copy().formatted(Colors.PLAYER))
                .append(" wants to teleport ")
                .append(Text.literal("you to them").formatted(Colors.SECONDARY))
                .append("! Type ")
                .append(literalCommand("/tpaccept", "Click to accept").formatted(Formatting.GREEN))
                .append(" to accept or ")
                .append(literalCommand("/tpdeny", "Click to deny").formatted(Formatting.RED))
                .append(" to deny it. The request expires in ")
                .append(Text.literal(Long.toString(TeleportRequestManager.getInstance().getRequestTTL()))
                        .append(" seconds.")
                        .formatted(Colors.TIME)
                )
                .formatted(Colors.DEFAULT)
        );
        context.getSource().sendFeedback(() ->
                Text.literal("")
                        .append(target.getName().copy().formatted(Colors.PLAYER))
                        .append(" has received your teleport request!")
                        .formatted(Colors.DEFAULT)
                ,
                false
        );
        return 0;
    }
}
