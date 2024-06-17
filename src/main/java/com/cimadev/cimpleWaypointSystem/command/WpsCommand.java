package com.cimadev.cimpleWaypointSystem.command;

import com.cimadev.cimpleWaypointSystem.Main;
import com.cimadev.cimpleWaypointSystem.command.persistentData.OfflinePlayer;
import com.cimadev.cimpleWaypointSystem.command.persistentData.PlayerHome;
import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;
import com.cimadev.cimpleWaypointSystem.command.persistentData.WaypointKey;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static com.cimadev.cimpleWaypointSystem.Main.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;


public class WpsCommand {

    private static final String COMMAND_NAME = "wps";

    private static final String[] DEFAULT_HELP = {
            "/wps go [name] : Teleports you to your waypoint called [waypoint name].",
            "/wps go [name] [owner] : Teleports you [owner]'s waypoint [waypoint name] if allowed.",
            "/wps go [name] open : Teleports you to the open waypoint [waypoint name].",
            "/wps [Arguments] : Shorthand for /wps go [Arguments].",
            "",
            "/wps here [name] <access>: Creates new waypoint [name] (with access level <access>) at your position.",
            "",
            "/wps list : Lists all waypoints accessible to you.",
            "/wps list mine : Lists your waypoints.",
            "/wps list [owner] : Lists [owner]'s waypoints accessible to you.",
            "/wps list open : Lists all open waypoints.",
            "",
            "/wps remove [name] : Removes your waypoint [name].",
            "/wps rename [name] [newName] : Renames your waypoint [name] to [newName].",
            "/wps access [name] [access] : Changes the access level of your waypoint [name] to [access].",
            "",
            "/wps friend add [friend] : Lets [friend] see your private waypoints.",
            "/wps friend remove [friend] : Prevents [friend] from seeing your private waypoints.",
            "",
            "/wps sethome [name] : Sets your /home at the position of the waypoint [name].",
            ""
    };

    private static final String[] ADMIN_HELP = {
            "Administrator features:",
            "/wps here [name] open : Creates open waypoint [name] at your position.",
            "/wps list [owner] all : Lists all of [owner]'s waypoints.",
            "/wps listAll : Lists all waypoints",
            "",
            "/wps remove [name] [owner] : Removes [owner]'s waypoint [name].",
            "/wps remove [name] open : Removes open waypoint [name].",
            "/wps rename [name] [newName] [owner] : Renames [owner]'s waypoint [name].",
            "/wps rename [name] [newName] open : Renames open waypoint [name].",
            ""
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {

        dispatcher.register(CommandManager.literal(COMMAND_NAME)
                .then(CommandManager.argument("name", greedyString())
                        .executes(WpsCommand::wpsGo)
                )
                .then(CommandManager.literal("help")
                        .executes(WpsCommand::wpsHelp))
                .then(CommandManager.literal("go")
                        .then(CommandManager.argument("name", greedyString())
                                .suggests(new WaypointSuggestionProvider())
                                .executes(WpsCommand::wpsGo)))
                .then(CommandManager.literal("here")
                        .then(CommandManager.argument("name", word())
                                .executes(WpsCommand::wpsHereMine)
                                .then(CommandManager.argument("access", word())
                                        .suggests(new AccessSuggestionProvider())
                                        .executes(WpsCommand::wpsHereMine))
                                .then(CommandManager.literal("open")
                                        .executes(WpsCommand::wpsHereOpen))))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("name", word())
                                .executes(WpsCommand::wpsAddMine)
                                .then(CommandManager.argument("access", word())
                                        .suggests(new AccessSuggestionProvider())
                                        .executes(WpsCommand::wpsAddMine))
                                .then(CommandManager.literal("open")
                                        .executes(WpsCommand::wpsAddOpen))))
                .then(CommandManager.literal("list")
                        .executes(WpsCommand::wpsListAccessible)
                        .then(CommandManager.argument("owner", word())
                                .suggests(new OfflinePlayerSuggestionProvider())
                                .executes(WpsCommand::wpsListOwnedAccessible)
                                .requires(source -> source.hasPermissionLevel(3))
                                .then(CommandManager.literal("all")
                                        .executes(WpsCommand::wpsListOwnedAll)))
                        .then(CommandManager.literal("mine")
                                .executes(WpsCommand::wpsListMine))
                        .then(CommandManager.literal("open")
                                .executes(WpsCommand::wpsListOpen)))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("name", word())
                                .suggests(new WaypointSuggestionProvider())
                                .executes(WpsCommand::wpsRemoveMine)
                                .requires(source -> source.hasPermissionLevel(3))
                                .then(CommandManager.argument("owner", word())
                                        .executes(WpsCommand::wpsRemoveOwned))
                                .then(CommandManager.literal("open")
                                        .executes(WpsCommand::wpsRemoveOpen))))
                .then(CommandManager.literal("rename")
                        .then(CommandManager.argument("oldName", word())
                                .suggests(new WaypointSuggestionProvider())
                                .then(CommandManager.argument("newName", word())
                                        .executes(WpsCommand::wpsRenameMine).requires(source -> source.hasPermissionLevel(3))
                                        .then(CommandManager.argument("owner", word())
                                                .executes(WpsCommand::wpsRenameOwned))
                                        .then(CommandManager.literal("open")
                                                .executes(WpsCommand::wpsRenameOpen)))))
                .then(CommandManager.literal("access")
                        .then(CommandManager.argument("name", word())
                                .then(CommandManager.argument("access", word())
                                        .suggests(new AccessSuggestionProvider())
                                        .executes(WpsCommand::wpsSetAccess))))
                .then(CommandManager.literal("friend")
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("player", word())
                                        .suggests(new OfflinePlayerSuggestionProvider())
                                        .executes(WpsCommand::wpsAddFriend)))
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("player", word())
                                        .suggests(new OfflinePlayerSuggestionProvider())
                                        .executes(WpsCommand::wpsRemoveFriend))))
                .then(CommandManager.literal("sethome")
                        .executes(WpsCommand::wpsSetHome)
                        .then(CommandManager.argument("name", word())
                                .suggests(new WaypointSuggestionProvider())
                                .then(CommandManager.argument("owner", word())
                                .suggests(new OfflinePlayerSuggestionProvider())
                                        .executes(WpsCommand::wpsSetHome))
                                .then(CommandManager.literal("open")
                                        .executes(WpsCommand::wpsSetHome)))
                        ));

        // administrator wps options
        dispatcher.register(CommandManager.literal(COMMAND_NAME)
                .requires(source -> source.hasPermissionLevel(4)) // only meant for printing to console
                .then(CommandManager.literal("listAll")
                        .executes(WpsCommand::wpsListAll)));
    }

    private static int wpsHelp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;
        ServerCommandSource source = context.getSource();
        messageText = () -> Text.literal("-=- -=- -=- /wps help menu -=- -=- -=-").formatted(SECONDARY_COLOR);
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

        messageText = () -> Text.literal("/wps help : Shows this menu.").formatted(DEFAULT_COLOR);
        source.sendFeedback(messageText, false);

        messageText = () -> Text.literal("-=- -=- -=- -=- -=- -=- -=- -=- -=- -=-").formatted(SECONDARY_COLOR);
        source.sendFeedback(messageText, false);

        return 1;
    }

    private static int wpsGo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;
        ServerCommandSource commandSource = context.getSource();
        ServerPlayerEntity player = commandSource.getPlayerOrThrow();
        MinecraftServer server = commandSource.getServer();
        String name = StringArgumentType.getString(context, "name");

        Waypoint waypoint = serverState.getWaypoint(new WaypointKey(player.getUuid(), name));
        if (waypoint == null) {
            waypoint = serverState.getWaypoint(new WaypointKey(null, name));
        }
        if (waypoint == null) {
            // Get by <foreignName>/<waypoint> if not found
            String[] waypointComponents = name.split("/", 2);
            if (waypointComponents.length == 2) {
                UUID foreignUuid = serverState.getPlayerByName(waypointComponents[0]).getUuid();
                waypoint = serverState.getWaypoint(new WaypointKey(foreignUuid, waypointComponents[1]));
            }
        }

        if (waypoint != null && serverState.waypointAccess(waypoint, player)) {
            String ownerName;
            if (waypoint.getOwner() != null) {
                OfflinePlayer owner = waypoint.getOwnerPlayer();
                if (owner == null) ownerName = "ERR: UNKNOWN PLAYER ";
                else ownerName = owner.getName() + "'s ";

            } else {
                ownerName = "";
            }

            BlockPos wpPos = waypoint.getPosition();
            ServerWorld world = server.getWorld(waypoint.getWorldRegKey());
            if ( world == null ) return -1;
            int yaw = waypoint.getYaw();
            Optional<Vec3d> teleportPosMaybe = ServerPlayerEntity.findRespawnPosition(world, wpPos, 0, true, true);
            if ( teleportPosMaybe.isEmpty() ) return -1;
            Vec3d teleportPos = teleportPosMaybe.get();
            player.teleport(world, teleportPos.getX(), teleportPos.getY(), teleportPos.getZ(), yaw, 0);

            Waypoint finalWaypoint = waypoint; // Need to do this because of very intelligent java compiler
            messageText = () -> Text.literal("Teleported to ")
                    .append(Text.literal(ownerName).formatted(PLAYER_COLOR))
                    .append(finalWaypoint.getAccessFormatted())
                    .append(Text.literal(" waypoint "))
                    .append(finalWaypoint.getNameFormatted())
                    .append(Text.literal("."))
                    .formatted(DEFAULT_COLOR);
        } else {
            messageText = () -> Text.literal("Waypoint ")
                    .append(Text.literal( name ).formatted(LINK_INACTIVE_COLOR))
                    .append(Text.literal(" could not be found."))
                    .formatted(DEFAULT_COLOR);
        }

        commandSource.sendFeedback(messageText, false);
        return 1;
    }

    // wpsAddMine and wpsAddOpen for compatibility reasons
    private static int wpsAddMine(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return wpsHere(context, false, false);
    }

    private static int wpsAddOpen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return wpsHere(context, true, false);
    }

    private static int wpsHereMine(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return wpsHere(context, false, true);
    }

    private static int wpsHereOpen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return wpsHere(context, true, true);
    }

    private static int wpsHere(CommandContext<ServerCommandSource> context, boolean isOpen, boolean moveIfExists) throws CommandSyntaxException {
        Supplier<Text> messageText;

        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        BlockPos blockPos = new BlockPos(player.getBlockPos());
        double yaw = player.getYaw();
        ServerWorld world = player.getServerWorld();
        UUID owner = (isOpen) ? null : player.getUuid();

        String name = StringArgumentType.getString(context, "name");
        int access = -1;
        if ( context.getNodes().size() == 4 && !isOpen ) {
            access = AccessArgumentParser.accessValueFromContext(context, "access");
        }

        Waypoint newWaypoint = new Waypoint(name, blockPos, yaw, world.getRegistryKey(), owner, access);
        Waypoint oldWaypoint = Main.serverState.getWaypoint(newWaypoint.getKey());
        if ( oldWaypoint == null ) {
            messageText = () -> wpsAdd(isOpen, newWaypoint);
        } else if ( moveIfExists ) {
            messageText = () -> wpsMove(isOpen, oldWaypoint, newWaypoint);
        } else {
            messageText = () -> Text.literal("Your ")
                    .append(oldWaypoint.getAccessFormatted())
                    .append( " waypoint " )
                    .append(oldWaypoint.getNameFormatted())
                    .append(" already exists!").formatted(DEFAULT_COLOR);
        }

        context.getSource().sendFeedback(messageText, true);
        Main.serverState.markDirty();
        return 1;
    }

    private static MutableText wpsAdd(boolean isOpen, Waypoint newWaypoint) {
        Main.serverState.setWaypoint(newWaypoint.getKey(), newWaypoint);
        return Text.literal("Set ")
                .append( (isOpen) ? Text.literal("open").formatted(PUBLIC_COLOR) : newWaypoint.getAccessFormatted())
                .append(" waypoint ")
                .append(newWaypoint.getNameFormatted())
                .append(".")
                .formatted(DEFAULT_COLOR);
    }

    private static MutableText wpsMove(boolean isOpen, Waypoint oldWaypoint, Waypoint newWaypoint) {
        BlockPos nwp = newWaypoint.getPosition();
        BlockPos owp = oldWaypoint.getPosition();
        oldWaypoint.setPosition(nwp);
        oldWaypoint.setYaw(newWaypoint.getYaw());
        int access = newWaypoint.getAccess();


        HoverEvent movedTooltip = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(" Formerly at x: "  + owp.getX() + ", y: " + owp.getY() + ", z: " + owp.getZ()));
        MutableText moved = Text.literal("Moved").formatted(Formatting.UNDERLINE);
        Style waypointStyle = moved.getStyle();
        moved.setStyle(waypointStyle.withHoverEvent(movedTooltip));
        Text oldAccess = oldWaypoint.getAccessFormatted();

        MutableText message = Text.literal("")
                .append(moved);
        if (isOpen) message.append(" the ").append(Text.literal("open").formatted(PUBLIC_COLOR));
        else message.append("your").append(oldAccess);
        message.append(" waypoint ")
                .append(newWaypoint.getNameFormatted())
                .append(".")
                .formatted(DEFAULT_COLOR);
        if ( oldWaypoint.getAccess() != access && access != -1 ) {
            message.append(" It is now ")
                    .append(newWaypoint.getAccessFormatted())
                    .append(".")
                    .formatted(DEFAULT_COLOR);
        }
        return message;
    }

    private static int wpsListAccessible(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if ( player == null ) return wpsListAll(context);
        else {
            ArrayList<Waypoint> waypoints = WpsUtils.getAccessibleWaypoints(player, null, false, false);
            printWaypointsToUser(context, waypoints);
        }
        return 1;
    }

    private static int wpsListOwnedAccessible(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        System.out.println("Listing owned accessible");
        ServerPlayerEntity player = context.getSource().getPlayer();
        if ( player == null ) return wpsListOwnedAll(context);
        else {
            OfflinePlayer owner = OfflinePlayerArgumentParser.offlinePlayerFromContext(context, "owner");
            /*todo: if( owner == null ) error "not a valid player", return 1*/
            ArrayList<Waypoint> waypoints = WpsUtils.getAccessibleWaypoints(player, owner, false, false);
            printWaypointsToUser(context, waypoints);
        }
        return 1;
    }

    private static int wpsListAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ArrayList<Waypoint> waypoints = WpsUtils.getAllWaypoints();
        printWaypointsToUser(context, waypoints);
        return 1;
    }
    private static int wpsListOwnedAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity player = context.getSource().getPlayer();
        OfflinePlayer owner = OfflinePlayerArgumentParser.offlinePlayerFromContext(context, "owner");
        /*todo: if( owner == null ) error "not a valid player", return 1*/
        ArrayList<Waypoint> waypoints = WpsUtils.getAccessibleWaypoints(player, owner, true, false);
        printWaypointsToUser(context, waypoints);
        return 1;

    }
    private static int wpsListMine(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        OfflinePlayer alsoPlayer = Main.serverState.getPlayerByUuid(player.getUuid());
        ArrayList<Waypoint> waypoints = WpsUtils.getAccessibleWaypoints(player, alsoPlayer, false, false);
        printWaypointsToUser(context, waypoints);
        return 1;

    }
    private static int wpsListOpen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity player = context.getSource().getPlayer();
        ArrayList<Waypoint> waypoints = WpsUtils.getAccessibleWaypoints(player, null, false, true);
        printWaypointsToUser(context, waypoints);
        return 1;

    }

    private static void printWaypointsToUser(CommandContext<ServerCommandSource> context, ArrayList<Waypoint> waypoints) {
        Supplier<Text> messageText;
        ServerPlayerEntity player = context.getSource().getPlayer();
        UUID playerUuid = ( player == null ) ? null : player.getUuid();

        if ( waypoints.isEmpty() ) {
            messageText = () -> Text.literal("No waypoints found.").formatted(DEFAULT_COLOR);
            context.getSource().sendFeedback(messageText, false);
            return;
        }

        messageText = () -> Text.literal("-=- -=- -=- /" + context.getInput() + " -=- -=- -=-").formatted(SECONDARY_COLOR);
        context.getSource().sendFeedback(messageText, false);
        for ( int i = 0 ; i < waypoints.size() ; i++ ) {
            Waypoint waypoint = waypoints.get(i);
            UUID ownerUuid = waypoint.getOwner();
            MutableText ownerTitle;
            if ( ownerUuid == null ) {
                ownerTitle = Text.literal("Open").formatted(PUBLIC_COLOR);
            } else {
                OfflinePlayer owner = Main.serverState.getPlayerByUuid(ownerUuid);
                if ( owner == null ) {
                    ownerTitle = Text.literal("[Error finding name]'s ").formatted(SECONDARY_COLOR);
                } else if ( owner.getUuid().equals( playerUuid ) ) {
                    ownerTitle = Text.literal("Your ").formatted(PLAYER_COLOR);
                } else {
                    ownerTitle = Text.literal(owner.getName() + "'s ").formatted(PLAYER_COLOR);
                }
            }
            messageText = () -> Text.literal("")
                    .append(ownerTitle)
                    .append((ownerUuid == null) ? Text.literal("") : waypoint.getAccessFormatted()) // dirty because lazy
                    .append(" waypoint ")
                    .append(waypoint.getNameFormatted())
                    .append(".")
                    .formatted(DEFAULT_COLOR);

            context.getSource().sendFeedback(messageText, false);
        }
        messageText = () -> Text.literal("-=- -=- -=- -=- -=- -=- -=- -=- -=- -=-").formatted(SECONDARY_COLOR);
        context.getSource().sendFeedback(messageText, false);
    }

    private static int wpsRemoveMine(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String name = StringArgumentType.getString(context, "name");
        WaypointKey wpKey = new WaypointKey(player.getUuid(), name);
        Waypoint waypoint = Main.serverState.getWaypoint(wpKey);
        return wpsRemove(context, waypoint, name, true);
    }

    private static int wpsRemoveOpen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        WaypointKey wpKey = new WaypointKey(null, name);
        Waypoint waypoint = Main.serverState.getWaypoint(wpKey);
        return wpsRemove(context, waypoint, name, false);
    }

    private static int wpsRemoveOwned(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        OfflinePlayer owner = OfflinePlayerArgumentParser.offlinePlayerFromContext(context, "owner");
        WaypointKey wpKey = new WaypointKey(owner.getUuid(), name);
        Waypoint waypoint = Main.serverState.getWaypoint(wpKey);
        return wpsRemove(context, waypoint, name, false);
    }

    private static int wpsRemove(CommandContext<ServerCommandSource> context, @Nullable Waypoint waypoint, String inputName, boolean ownedByCaller) {
        Supplier<Text> messageText;

        MutableText ownerTitle;
        UUID ownerUuid = (waypoint == null) ? null : waypoint.getOwner();
        if ( ownedByCaller ) ownerTitle = Text.literal("Your ");
        else {
            if ( ownerUuid == null ) {
                ownerTitle = Text.literal("The ").append(Text.literal("open ").formatted(PUBLIC_COLOR));
            } else {
                ownerTitle = Text.literal(Main.serverState.getPlayerByUuid(ownerUuid).getName() + "'s ");
            }
        }

        if (waypoint == null) {
            messageText = () -> ownerTitle.append("waypoint ")
                    .append(Text.literal( inputName ).formatted(LINK_INACTIVE_COLOR))
                    .append(Text.literal(" could not be found."))
                    .formatted(DEFAULT_COLOR);
        } else {
            Text waypointNameFormatted = waypoint.getNameFormatted();
            Main.serverState.removeWaypoint(waypoint.getKey());
            Main.serverState.markDirty();
            MutableText message = Text.literal("")
                    .append(ownerTitle);
            if ( ownerUuid != null ) message.append(waypoint.getAccessFormatted());
            message.append(" waypoint ")
                    .append(waypointNameFormatted)
                    .append(Text.literal(" has been removed."))
                    .formatted(DEFAULT_COLOR);
            messageText = () -> message;
        }
        context.getSource().sendFeedback(messageText, false);
        return 1;
    }

    private static int wpsSetAccess(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;

        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String name = StringArgumentType.getString(context, "name");
        WaypointKey wpKey = new WaypointKey(player.getUuid(), name);
        Waypoint waypoint = Main.serverState.getWaypoint(wpKey);
        if (waypoint == null) {
            messageText = () -> Text.literal("Your waypoint ")
                    .append(Text.literal( name ).formatted(LINK_INACTIVE_COLOR))
                    .append(Text.literal(" could not be found."))
                    .formatted(DEFAULT_COLOR);
        } else {
            int access = AccessArgumentParser.accessValueFromContext(context, "access");
            Text oldAccess = waypoint.getAccessFormatted();
            waypoint.setAccess(access);
            messageText = () -> Text.literal("Your ")
                    .append(oldAccess)
                    .append(" waypoint ")
                    .append(waypoint.getNameFormatted())
                    .append(Text.literal(" is now "))
                    .append(waypoint.getAccessFormatted())
                    .append(".")
                    .formatted(DEFAULT_COLOR);
            Main.serverState.markDirty();
        }

        context.getSource().sendFeedback(messageText, false);
        return 1;
    }

    private static int wpsRenameMine(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String oldName = StringArgumentType.getString(context, "oldName");
        String newName = StringArgumentType.getString(context, "newName");
        WaypointKey wpKey = new WaypointKey(player.getUuid(), oldName);
        Waypoint waypoint = Main.serverState.getWaypoint(wpKey);
        return wpsRename(context, waypoint, oldName, newName, true);
    }

    private static int wpsRenameOpen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String oldName = StringArgumentType.getString(context, "oldName");
        String newName = StringArgumentType.getString(context, "newName");
        WaypointKey wpKey = new WaypointKey(null, oldName);
        Waypoint waypoint = Main.serverState.getWaypoint(wpKey);
        return wpsRename(context, waypoint, oldName, newName, false);
    }

    private static int wpsRenameOwned(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String oldName = StringArgumentType.getString(context, "oldName");
        String newName = StringArgumentType.getString(context, "newName");
        OfflinePlayer owner = OfflinePlayerArgumentParser.offlinePlayerFromContext(context, "owner");
        WaypointKey wpKey = new WaypointKey(owner.getUuid(), oldName);
        Waypoint waypoint = Main.serverState.getWaypoint(wpKey);
        return wpsRename(context, waypoint, oldName, newName, false);
    }

    private static int wpsRename(CommandContext<ServerCommandSource> context, Waypoint waypoint, String oldName, String newName, boolean ownedByCaller) throws CommandSyntaxException {
        Supplier<Text> messageText;

        MutableText ownerTitle;
        UUID ownerUuid = (waypoint == null) ? null : waypoint.getOwner();
        if ( ownedByCaller ) ownerTitle = Text.literal("Your ");
        else {
            if ( ownerUuid == null ) {
                ownerTitle = Text.literal("The ").append(Text.literal("open ").formatted(PUBLIC_COLOR));
            } else {
                ownerTitle = Text.literal(Main.serverState.getPlayerByUuid(ownerUuid).getName() + "'s ");
            }
        }

        if (waypoint == null) {
            String finalOldName = oldName;
            messageText = () -> ownerTitle.append("waypoint ")
                    .append(Text.literal(finalOldName).formatted(LINK_INACTIVE_COLOR))
                    .append(Text.literal(" could not be found."))
                    .formatted(DEFAULT_COLOR);
        } else {
            Main.serverState.removeWaypoint(waypoint.getKey());
            oldName = waypoint.getName();
            waypoint.rename(newName);
            Main.serverState.setWaypoint(waypoint.getKey(), waypoint);
            String finalOldName = oldName;
            MutableText message = Text.literal("Your waypoint ");
            if ( ownerUuid != null ) message.append(Text.literal(finalOldName).formatted(LINK_INACTIVE_COLOR));
            message.append(Text.literal(" is now called "))
                    .append(waypoint.getNameFormatted())
                    .append(".")
                    .formatted(DEFAULT_COLOR);
            messageText = () -> message;
            Main.serverState.markDirty();
        }

        context.getSource().sendFeedback(messageText, true);
        return 1;
    }

    private static int wpsAddFriend(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;

        ServerPlayerEntity p = context.getSource().getPlayerOrThrow();
        OfflinePlayer player = Main.serverState.getPlayerByUuid(p.getUuid());
        OfflinePlayer friend = OfflinePlayerArgumentParser.offlinePlayerFromContext(context, "friend");
        if ( friend != null ) {
            boolean newFriend = player.addFriend(friend);
            if ( newFriend ) {
                messageText = () -> Text.literal("You are now friends with " + friend.getName() + ".").formatted(DEFAULT_COLOR);
            } else {
                messageText = () -> Text.literal("You are already friends with " + friend.getName() + "!").formatted(DEFAULT_COLOR);
            }
        } else {
            messageText = () -> Text.literal("The specified player could not be found.").formatted(DEFAULT_COLOR);
        }

        context.getSource().sendFeedback(messageText, false);
        return 1;
    }

    private static int wpsRemoveFriend(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;

        ServerPlayerEntity p = context.getSource().getPlayerOrThrow();
        OfflinePlayer player = Main.serverState.getPlayerByUuid(p.getUuid());
        OfflinePlayer friend = OfflinePlayerArgumentParser.offlinePlayerFromContext(context, "friend");
        if ( friend != null ) {
            boolean newFriend = player.removeFriend(friend);
            if ( newFriend ) {
                messageText = () -> Text.literal("You are no longer friends with " + friend.getName() + ".").formatted(DEFAULT_COLOR);
            } else {
                messageText = () -> Text.literal("You weren't friends with " + friend.getName() + "!").formatted(DEFAULT_COLOR);
            }
        } else {
            messageText = () -> Text.literal("The specified player could not be found.").formatted(DEFAULT_COLOR);
        }

        context.getSource().sendFeedback(messageText, false);
        return 1;
    }

    private static int wpsSetHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Supplier<Text> messageText;

        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        UUID playerUuid = player.getUuid();
        String name = StringArgumentType.getString(context, "name");
        OfflinePlayer owner;
        MutableText ownerName;
        UUID ownerUuid;
        if (context.getNodes().size() == 4) {
            try {
                owner = OfflinePlayerArgumentParser.offlinePlayerFromContext(context, "owner");
                ownerName = Text.literal(owner.getName() + "'s ");
                ownerUuid = owner.getUuid();
            } catch (Exception e) {
                ownerName = Text.literal("open ").formatted(PUBLIC_COLOR);
                ownerUuid = null;
            }
        } else {
            owner = Main.serverState.getPlayerByUuid(playerUuid);
            ownerName = Text.literal("Your ");
            ownerUuid = owner.getUuid();
        }

        MutableText finalOwnerName = ownerName;
        messageText = () -> Text.literal("").append(finalOwnerName).append("waypoint ")
                .append(Text.literal( name ).formatted(LINK_INACTIVE_COLOR))
                .append(Text.literal(" could not be found."))
                .formatted(DEFAULT_COLOR);

        WaypointKey wpKey = new WaypointKey(ownerUuid, name);
        Waypoint waypoint = Main.serverState.getWaypoint(wpKey);
        if (waypoint != null) {
            if ( Main.serverState.waypointAccess(waypoint, player) ) {
                PlayerHome home = new PlayerHome(waypoint.getPosition(), (double) waypoint.getYaw(), waypoint.getWorldRegKey(), playerUuid);
                Main.serverState.setPlayerHome(playerUuid, home);
                messageText = () -> Text.literal("Your ")
                        .append(home.positionHover("home"))
                        .append(" has been moved to ")
                        .append(finalOwnerName)
                        .append("waypoint ")
                        .append(waypoint.getNameFormatted())
                        .append(Text.literal("."))
                        .formatted(DEFAULT_COLOR);
            }
        }

        context.getSource().sendFeedback(messageText, false);
        return 1;
    }
}
