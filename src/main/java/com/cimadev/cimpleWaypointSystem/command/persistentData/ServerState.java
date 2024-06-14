package com.cimadev.cimpleWaypointSystem.command.persistentData;

import com.cimadev.cimpleWaypointSystem.Main;
import com.mojang.datafixers.types.Type;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.*;

public class ServerState extends PersistentState {
    private HashMap<WaypointKey, Waypoint> worldWideWaypoints = new HashMap<>();
    private HashMap<UUID, PlayerHome> playerHomes = new HashMap<>();
    private HashMap<String, OfflinePlayer> playersByName = new HashMap<>();
    private HashMap<UUID, OfflinePlayer> playersByUuid = new HashMap<>();
    private int waypointCount;

    public void setPlayerHome(UUID uuid, PlayerHome playerHome) {
        playerHomes.put(uuid, playerHome);
    }
    public void removePlayerHome(UUID uuid) {
        playerHomes.remove(uuid);
    }

    public PlayerHome getPlayerHome(UUID uuid) {
        return playerHomes.get(uuid);
    }

    public void setWaypoint(WaypointKey waypointKey, Waypoint waypoint) {
        worldWideWaypoints.put(waypointKey, waypoint);
    }

    public void removeWaypoint(WaypointKey waypointKey) {
        worldWideWaypoints.remove(waypointKey);
    }

    public Waypoint getWaypoint(WaypointKey waypointKey) {
        Waypoint waypoint = worldWideWaypoints.get(waypointKey);
        if (waypoint == null) {
            waypoint = worldWideWaypoints.get(new WaypointKey(null, waypointKey.getName()));
        }
        return waypoint;
    }

    public HashMap<WaypointKey, Waypoint> copyWaypointMap() {
        return new HashMap<>(worldWideWaypoints);
    }

    public OfflinePlayer getPlayerByName(String name) {
        OfflinePlayer p = playersByName.get(name);
        return playersByName.get(name);
    }

    public OfflinePlayer getPlayerByUuid(UUID uuid) {
        return playersByUuid.get(uuid);
    }

    public HashMap<UUID, OfflinePlayer> copyPlayersByUuidMap() {
        return new HashMap<>(playersByUuid);
    }

    public HashMap<UUID, PlayerHome> copyPlayerHomesMap() {
        return new HashMap<>(playerHomes);
    }

    public Iterable<String> getPlayerNames() {
        Stack<String> playerNames = new Stack<>();
        playersByName.forEach((name, player) -> {
            playerNames.push(name);
        });
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
        if ( waypoint.getAccess() == 0 ) return true;       // all public waypoints freely accessible

        OfflinePlayer owner = getPlayerByUuid(ownerUuid);
        if ( waypoint.getAccess() == 1 && owner.likes( getPlayerByUuid( playerUuid ) ) ) {
            return true;
        }

        if ( waypoint.getAccess() == 2 && owner.getUuid().equals(playerUuid)) {
            return true;
        }

        return false;
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

    public void setWaypointCount(int waypointCount) {
        this.waypointCount = waypointCount;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound pList = new NbtCompound();
        playersByUuid.forEach((uuid, offlinePlayer) -> offlinePlayer.writeNbt(pList));
        nbt.put("playerList", pList);

        NbtCompound waypointList = new NbtCompound();
        worldWideWaypoints.forEach((waypointKey, waypoint) -> waypoint.writeNbt(waypointList));
        nbt.putInt("waypointCount", worldWideWaypoints.size());
        nbt.put("waypoints",waypointList);

        NbtCompound playerHomesCompound = new NbtCompound();
        playerHomes.forEach((UUID, playerHome) -> playerHome.writeNbt(playerHomesCompound));
        nbt.put("playerHomes", playerHomesCompound);

        return nbt;
    }

    public static ServerState createFromNbt(NbtCompound tag) {
        ServerState serverState = new ServerState();
        NbtCompound pList = tag.getCompound("playerList");
        pList.getKeys().forEach(key -> {
            serverState.loadPlayer( new OfflinePlayer(pList.getCompound(key), key) );
        });
        HashMap<UUID, OfflinePlayer> playersByUuidCopy = serverState.copyPlayersByUuidMap();
        pList.getKeys().forEach(key -> {
            OfflinePlayer player = serverState.getPlayerByUuid(UUID.fromString(key));
            player.loadFriends(pList.getCompound(key), playersByUuidCopy);
        });

        NbtCompound waypointList = tag.getCompound("waypoints");
        serverState.setWaypointCount(tag.getInt("waypointCount"));
        waypointList.getKeys().forEach(key -> {
            NbtCompound waypointCompound = waypointList.getCompound(key);

            Waypoint waypoint = new Waypoint( waypointCompound, key );

            serverState.setWaypoint( WaypointKey.fromString(key) , waypoint );
        });

        NbtCompound playerHomesCompound = tag.getCompound("playerHomes");
        playerHomesCompound.getKeys().forEach(key -> {
            NbtCompound homeCompound = playerHomesCompound.getCompound(key);

            PlayerHome playerHome = new PlayerHome( homeCompound, key );

            serverState.setPlayerHome( playerHome.getOwner(), playerHome );
        });

        return serverState;
    }

    private final static Type<ServerState> type = new Type<>(
            ServerState::new,
            ServerState::createFromNbt,
            null
    );

    public static ServerState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server
                .getWorld(World.OVERWORLD).getPersistentStateManager();

        return persistentStateManager.getOrCreate(
                type,
                Main.MOD_ID
        );
    }
}
