package com.cimadev.cimpleWaypointSystem.command;

import com.cimadev.cimpleWaypointSystem.Main;
import com.cimadev.cimpleWaypointSystem.command.persistentData.OfflinePlayer;
import com.cimadev.cimpleWaypointSystem.command.persistentData.PlayerHome;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Supplier;

import static com.cimadev.cimpleWaypointSystem.Main.*;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class HomeCommand {
    private static final String COMMAND_NAME = "home";
    private static final String[] DEFAULT_HELP = {
        "/home : Teleports you to your home position, or your current respawn point if none is set.",
        "/home here : Sets the home to your current position, including your horizontal rotation.",
        "/home clear : Resets your home to your current respawn position.",
        "/home where? : Tells you in chat, where your home is, and contains a click action to go there.",
        ""
    };

    private static final String[] ADMIN_HELP = {
        "Administrator features:",
        "/home of [player] : Tells you in chat, where [player]'s home is, and contains a click action to teleport you there.",
        "/home set [position] [world] [player] : Sets the home of [player] to the [position] in [world].",
        ""
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal(COMMAND_NAME)
                .executes(HomeCommand::homeGo)
                .then(CommandManager.literal("here")
                        .executes(HomeCommand::homeHere))
                .then(CommandManager.literal("clear")
                        .executes(HomeCommand::homeClear))
                .then(CommandManager.literal("where?")
                        .executes(HomeCommand::homeWhere))
                .then(CommandManager.literal("help")
                        .executes(HomeCommand::homeHelp)));

        //dispatcher.register(CommandManager.literal(COMMAND_NAME).then(CommandManager.literal("listAll").requires(source -> source.hasPermissionLevel(4)).executes(HomeCommand::homeListAll)));
        dispatcher.register(CommandManager.literal(COMMAND_NAME)
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(3))   // only admin and owner may set other's homes
                        .then(CommandManager.argument("position", BlockPosArgumentType.blockPos())
                                .then(CommandManager.argument("world", DimensionArgumentType.dimension())
                                        .then(CommandManager.argument("player", word())
                                                .executes(HomeCommand::homeSet)))))
                .then(CommandManager.literal("of")
                        .requires(source-> source.hasPermissionLevel(3))
                        .then(CommandManager.argument("owner", word())
                        .executes(HomeCommand::homeOf))));

    }

    public static int homeGo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        PlayerHome playerHome = Main.serverState.getPlayerHome(player.getUuid());
        ServerWorld world;
        BlockPos homePos;
        float yaw = 0;
        boolean respawnForced;

        if ( playerHome == null ) {
            world = player.getServer().getWorld(player.getSpawnPointDimension());
            homePos = player.getSpawnPointPosition();
            respawnForced = false;

            if ( homePos == null ) {
                homePos = player.getServer().getOverworld().getSpawnPos();
            }
            MutableText spawnpoint = Text.literal("spawnpoint").formatted(LINK_COLOR, Formatting.UNDERLINE);
            Style style = spawnpoint.getStyle();
            HoverEvent spawncoords = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("x: " + homePos.getX() + ", y: " + homePos.getY() + ", z: " + homePos.getZ()));
            spawnpoint.setStyle(style.withHoverEvent(spawncoords));
            messageText = () -> Text.literal("Teleported to your ")
                    .append(spawnpoint)
                    .append(".")
                    .formatted(DEFAULT_COLOR);
        } else {
            homePos = playerHome.getPosition();
            yaw = playerHome.getYaw();
            world = player.getServer().getWorld(playerHome.worldRegistryKey());
            respawnForced = true;

            messageText = () -> Text.literal("Teleported to your ")
                    .append(playerHome.positionHover("home"))
                    .append(".")
                    .formatted(DEFAULT_COLOR);
        }

        Optional<Vec3d> teleportPosMaybe = ServerPlayerEntity.findRespawnPosition(world, homePos, 0, respawnForced, true);
        if ( teleportPosMaybe.isEmpty() ) {
            world = player.getServer().getOverworld();
            homePos = world.getSpawnPos();
            respawnForced = true;
            teleportPosMaybe = ServerPlayerEntity.findRespawnPosition(world, homePos, 0, respawnForced, true);
        }
        Vec3d teleportPos = teleportPosMaybe.get();

        player.teleport(world, teleportPos.getX(), teleportPos.getY(), teleportPos.getZ() , yaw, 0);

        context.getSource().sendFeedback(messageText, true);

        return 1;
    }

    private static int homeHere(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;

        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        BlockPos blockPos = new BlockPos(player.getBlockPos());
        double yaw = player.getYaw();
        ServerWorld world = player.getServerWorld();
        PlayerHome playerHome = new PlayerHome(blockPos, yaw, world.getRegistryKey(), player.getUuid());
        Main.serverState.setPlayerHome(player.getUuid(), playerHome);
        messageText = () -> Text.literal("Your ")
                .append(playerHome.positionHover("home"))
                .append(" has been set.")
                .formatted(DEFAULT_COLOR);
        context.getSource().sendFeedback(messageText, true);
        Main.serverState.markDirty();
        return 1;
    }

    private static int homeWhere(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        PlayerHome playerHome = Main.serverState.getPlayerHome(player.getUuid());
        if ( playerHome != null ) {
            BlockPos pHposition = playerHome.getPosition();
            ServerWorld world = player.getServer().getWorld(playerHome.worldRegistryKey());
            String worldName;
            if ( world != null ) {
                worldName = world.getDimensionKey().getValue().getPath(); // Iamhere
            } else {
                worldName = "[world could not be identified]";
            }
            MutableText here = Text.literal(pHposition.getX() + "x " + pHposition.getY() + "y " + pHposition.getZ() + "z").formatted(LINK_COLOR, Formatting.UNDERLINE);
            Style style = here.getStyle();
            HoverEvent goHomeTooltip = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to go home!"));
            ClickEvent goHome = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home");
            here.setStyle(style.withClickEvent(goHome).withHoverEvent(goHomeTooltip));
            messageText = () -> Text.literal("At ")
                    .append(here)
                    .append(" in the " + worldName + "!")
                    .formatted(DEFAULT_COLOR);
        } else {
            MutableText respawnPoint = Text.literal("respawn point").formatted(LINK_COLOR, Formatting.UNDERLINE);
            Style style = respawnPoint.getStyle();
            HoverEvent goHomeTooltip = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to go home!"));
            ClickEvent goHome = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home");
            respawnPoint.setStyle(style.withClickEvent(goHome).withHoverEvent(goHomeTooltip));
            messageText = () -> Text.literal("You have not set a home. You will be teleported to your ")
                    .append(respawnPoint)
                    .append(".")
                    .formatted(DEFAULT_COLOR);
        }
        context.getSource().sendFeedback(messageText, true);
        return 1;
    }

    private static int homeSet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;
        OfflinePlayer player = OfflinePlayerArgumentParser.offlinePlayerFromContext(context, "player");
        BlockPos blockPos = BlockPosArgumentType.getBlockPos(context, "position");
        ServerWorld world = DimensionArgumentType.getDimensionArgument(context, "world");

        PlayerHome playerHome = new PlayerHome(blockPos, 0.0, world.getRegistryKey(), player.getUuid());
        Main.serverState.setPlayerHome(player.getUuid(), playerHome);
        messageText = () -> Text.literal(player.getName()+ "'s ")
                .append(playerHome.positionHover("home"))
                .append(" has been moved.")
                .formatted(DEFAULT_COLOR);
        context.getSource().sendFeedback(messageText, true);
        Main.serverState.markDirty();
        return 1;
    }

    private static int homeClear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;

        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        PlayerHome playerHome = Main.serverState.getPlayerHome(player.getUuid());
        Main.serverState.removePlayerHome(player.getUuid());

        messageText = () -> Text.literal("Your ")
                .append(playerHome.positionHover("home"))
                .append(" has been removed.")
                .formatted(DEFAULT_COLOR);
        context.getSource().sendFeedback(messageText, true);
        Main.serverState.markDirty();
        return 1;
    }

    private static int homeOf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;

        OfflinePlayer player = OfflinePlayerArgumentParser.offlinePlayerFromContext(context, "owner");
        PlayerHome ph = Main.serverState.getPlayerHome(player.getUuid());

        if ( ph != null ) {
            BlockPos homePos = ph.getPosition();

            MutableText position = Text.literal(homePos.getX() + "x " + homePos.getY() + "y " + homePos.getZ() + "z").formatted(LINK_COLOR, Formatting.UNDERLINE);
            Style style = position.getStyle();
            HoverEvent goHomeTooltip = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to visit " + player.getName() + "'s home!"));
            ClickEvent goHome = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + homePos.getX() + " " + homePos.getY() + " " + homePos.getZ());
            position.setStyle(style.withClickEvent(goHome).withHoverEvent(goHomeTooltip));

            messageText = () -> Text.literal(player.getName() + "'s home is at ")
                    .append(position)
                    .append("!")
                    .formatted(DEFAULT_COLOR);
        } else {
            messageText = () -> Text.literal(player.getName() + " has not set a home.");
        }

        context.getSource().sendFeedback(messageText, false);
        return 1;
    }

    private static int homeHelp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;
        ServerCommandSource source = context.getSource();
        messageText = () -> Text.literal("-=- -=- -=- /home help menu -=- -=- -=-").formatted(SECONDARY_COLOR);
        source.sendFeedback(messageText, false);

        for( String help : DEFAULT_HELP ) {
            messageText = () -> Text.literal(help).formatted(DEFAULT_COLOR);
            source.sendFeedback(messageText, false);
        }

        if (context.getSource().hasPermissionLevel(3)) {
            for (String help : ADMIN_HELP ) {
                messageText = () -> Text.literal(help).formatted(DEFAULT_COLOR);
                source.sendFeedback(messageText, false);
            }
        }

        messageText = () -> Text.literal("/home help : Shows this menu.").formatted(DEFAULT_COLOR);
        source.sendFeedback(messageText, false);

        messageText = () -> Text.literal("-=- -=- -=- -=- -=- -=- -=- -=- -=- -=-").formatted(SECONDARY_COLOR);
        source.sendFeedback(messageText, false);

        return 1;
    }
}

