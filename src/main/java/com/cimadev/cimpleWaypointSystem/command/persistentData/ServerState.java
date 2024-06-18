package com.cimadev.cimpleWaypointSystem.command.persistentData;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.*;

public class ServerState extends PersistentState {

    private final HashMap<WaypointKey, Waypoint> worldWideWaypoints = new HashMap<>();
    private final HashMap<UUID, PlayerHome> playerHomes = new HashMap<>();
    private final HashMap<String, OfflinePlayer> playersByName = new HashMap<>();
    private final HashMap<UUID, OfflinePlayer> playersByUuid = new HashMap<>();

    public void setPlayerHome(UUID uuid, PlayerHome playerHome) {
        playerHomes.put(uuid, playerHome);
    }

    public void removePlayerHome(UUID uuid) {
        playerHomes.remove(uuid);
    }

    public PlayerHome getPlayerHome(UUID uuid) {
        return playerHomes.get(uuid);
    }

    public void setWaypoint(Waypoint waypoint) {
        worldWideWaypoints.put(waypoint.getKey(), waypoint);
    }

    public void removeWaypoint(WaypointKey waypointKey) {
        worldWideWaypoints.remove(waypointKey);
    }

    public Waypoint getWaypoint(WaypointKey waypointKey) {
        return worldWideWaypoints.get(waypointKey);
    }

    public HashMap<WaypointKey, Waypoint> copyWaypointMap() {
        return new HashMap<>(worldWideWaypoints);
    }

    public OfflinePlayer getPlayerByName(String name) {
        return playersByName.get(name);
    }

    public OfflinePlayer getPlayerByUuid(UUID uuid) {
        return playersByUuid.get(uuid);
    }

    public Iterable<String> getPlayerNames() {
        Stack<String> playerNames = new Stack<>();
        playersByName.forEach((name, uuid) -> playerNames.push(name));
        return playerNames;
    }

    public void setPlayer(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        UUID playerUuid = player.getUuid();

        setPlayer( playerName, playerUuid );
    }

    public boolean waypointAccess(Waypoint waypoint, ServerPlayerEntity player) {
        return waypointAccess(waypoint, player.getUuid());
    }

    public boolean waypointAccess(Waypoint waypoint, OfflinePlayer player) {
        return waypointAccess(waypoint, player.getUuid());
    }

    public boolean waypointAccess(Waypoint waypoint, UUID playerUuid) {
        UUID ownerUuid = waypoint.getOwner();
        if ( waypoint.getAccess() == AccessLevel.OPEN || waypoint.getAccess() == AccessLevel.PUBLIC ) return true;       // all public waypoints freely accessible

        // only happens if access type of an open waypoint was corrupted in NBT. In this case, ownerUuid == null && AccessLevel.SECRET
        // waypoint only visible by admins by listing all waypoints
        if ( ownerUuid == null ) return false;

        OfflinePlayer owner = getPlayerByUuid(ownerUuid);
        if ( waypoint.getAccess() == AccessLevel.PRIVATE && owner.likes( playerUuid ) ) {
            return true;
        }

        return waypoint.getAccess() == AccessLevel.SECRET && ownerUuid.equals(playerUuid);
    }

    private void setPlayer(String playerName, UUID playerUuid) {
        OfflinePlayer pByUuid = playersByUuid.get(playerUuid);
        OfflinePlayer pByName = playersByName.get(playerName);

        if ( pByUuid == null && pByName == null ) {     // player is new and there is not a player with the same name in the tables
            OfflinePlayer newPlayer = new OfflinePlayer(playerUuid, playerName);
            playersByUuid.put(playerUuid, newPlayer);
            playersByName.put(playerName, newPlayer);
        } else if ( pByUuid == null ) {                 // player is new but there is a player with the same name in the tables
            OfflinePlayer newPlayer = new OfflinePlayer(playerUuid, playerName);
            pByName.emptyName();
            playersByUuid.put(playerUuid, newPlayer);
            playersByName.put(playerName, newPlayer);
        } else if ( pByName == null ) {                 // player is not new but has changed name
            playersByName.remove(pByUuid.getName());
            playersByName.put(playerName, pByUuid);
            pByUuid.setName(playerName);
        } else {
            if ( pByUuid.equals(pByName) ) return;      // player is already in table, all good (shouldn't happen)

            // player is not new, but has a new name that another player had previously
            pByName.emptyName();
            pByUuid.setName(playerName);
            playersByName.remove(pByUuid.getName());
            playersByName.put(playerName, pByUuid);
        }

        this.markDirty();
    }

    private void loadPlayer(OfflinePlayer player) {
        playersByUuid.put(player.getUuid(), player);
        playersByName.put(player.getName(), player);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) { // todo: build a playerList, waypointList and homesList NbtElement to avoid redundancy of key
        NbtCompound pList = new NbtCompound();
        playersByUuid.forEach((uuid, offlinePlayer) -> pList.put(uuid.toString(), offlinePlayer.toNbt()));
        nbt.put("playerList", pList);

        NbtCompound waypointList = new NbtCompound();
        worldWideWaypoints.forEach((waypointKey, waypoint) -> waypointList.put(waypointKey.toString(), waypoint.writeNbt()));
        nbt.put("waypoints",waypointList);

        NbtCompound playerHomesCompound = new NbtCompound();
        playerHomes.forEach((UUID, playerHome) -> playerHome.writeNbt(playerHomesCompound));
        nbt.put("playerHomes", playerHomesCompound);

        return nbt;
    }

    public static ServerState createFromNbt(NbtCompound tag) {
        ServerState serverState = new ServerState();
        NbtCompound pList = tag.getCompound("playerList");
        pList.getKeys().forEach(key -> serverState.loadPlayer( OfflinePlayer.fromNbt( pList.getCompound(key) ) ));

        NbtCompound waypointList = tag.getCompound("waypoints");
        waypointList.getKeys().forEach(key -> serverState.setWaypoint( Waypoint.fromNbt( waypointList.getCompound(key) )));

        NbtCompound playerHomesCompound = tag.getCompound("playerHomes");
        playerHomesCompound.getKeys().forEach(key -> {
            NbtCompound homeCompound = playerHomesCompound.getCompound(key);

            PlayerHome playerHome = new PlayerHome( homeCompound, key );

            serverState.setPlayerHome( playerHome.getOwner(), playerHome );
        });

        return serverState;
    }

    public static ServerState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server
                .getWorld(World.OVERWORLD).getPersistentStateManager();

        return persistentStateManager.getOrCreate(
                ServerState::createFromNbt,
                ServerState::new,
                "cimple-waypoint-system");
    }
}
