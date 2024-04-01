package com.cimadev.cimpleWaypointSystem.command.persistentData;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.UUID;

public class OfflinePlayer implements Comparable<OfflinePlayer> {

    private UUID uuid;
    private String name;
    private TreeSet<OfflinePlayer> friends = new TreeSet<>();

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void emptyName() {
        this.name = "";
    }

    public boolean addFriend(OfflinePlayer friend) {
        return friends.add(friend);
    }

    public boolean removeFriend(OfflinePlayer notFriend) {
        return friends.remove(notFriend);
    }

    @Override
    public int compareTo(@NotNull OfflinePlayer that) {
        return this.uuid.compareTo(that.getUuid());
    }

    @Override
    public boolean equals(Object that) {
        if ( that instanceof OfflinePlayer ) return this.uuid.equals( ( (OfflinePlayer) that ).getUuid() );
        return false;
    }

    public OfflinePlayer (ServerPlayerEntity player) {
        this( player.getUuid(), player.getName().getString() );
    }

    public OfflinePlayer (UUID uuid, String name) {
        this.name = name;
        this.uuid = uuid;
    }

    public boolean likes(OfflinePlayer maybeFriend) {
        if ( maybeFriend.getUuid().equals(uuid) ) return true;
        return friends.contains(maybeFriend);
    }

    public boolean likes(ServerPlayerEntity maybeFriend) {
        if ( maybeFriend.getUuid().equals(uuid) ) return true;
        return likes( new OfflinePlayer(maybeFriend) ); // probably not good practise?
    }

    public OfflinePlayer (NbtCompound nbt, String uuid ) {
        this.name = nbt.getString("name");
        this.uuid = UUID.fromString(uuid);
    }

    public void loadFriends (NbtCompound nbt, HashMap<UUID, OfflinePlayer> offlinePlayers) {
        NbtCompound friendList = nbt.getCompound("friendList");
        friendList.getKeys().forEach(key -> {
            friends.add( offlinePlayers.get( key ) );
            friends.add( offlinePlayers.get( friendList.getString(key) ) );
        });
    }

    public NbtCompound writeNbt( NbtCompound nbt ) {
        NbtCompound offlinePlayerCompound = new NbtCompound();
        offlinePlayerCompound.putString("name", name);
        int friendnum = friends.size();

        NbtCompound friendList = new NbtCompound();
        for ( int i = 0 ; i < (friendnum - 1) / 2 ; i++) {
            friendList.putString( friends.pollFirst().getUuid().toString() , friends.pollLast().getUuid().toString() );
        }
        if (friendnum != 0) {
            String key = friends.pollFirst().getUuid().toString();
            String value = friends.pollLast().getUuid().toString();
            if (value == null) value = key;
            friendList.putString(key, value);
        }

        offlinePlayerCompound.put("friendList", friendList);

        nbt.put(uuid.toString(), offlinePlayerCompound);
        return nbt;
    }

}
