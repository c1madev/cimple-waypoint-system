package com.cimadev.cimpleWaypointSystem.command.persistentData;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WaypointKey {
    public static final PacketCodec<RegistryByteBuf, WaypointKey> PACKET_CODEC = PacketCodec.of(
            (key, buf) -> {
                buf.writeNullable(key.owner, (buf2, uuid) -> buf2.writeUuid(uuid));
                buf.writeString(key.name);
            },
            buf -> new WaypointKey(
                    buf.readNullable(RegistryByteBuf::readUuid),
                    buf.readString()
            )
    );

    @Nullable
    private final UUID owner;
    private String name;

    public @Nullable UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public WaypointKey(@Nullable UUID owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public String toString() {
        if ( this.owner == null ) return name+"/";
        return name+"/"+owner;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", this.name);
        nbt.putUuid("owner", this.owner);

        return nbt;
    }

    public static WaypointKey fromNbt( NbtCompound nbt ) {
        return new WaypointKey(nbt.getUuid("owner"), nbt.getString("name"));
    }

    @Override
    public int hashCode() {
        if ( this.owner == null ) return name.toLowerCase().hashCode();
        return owner.hashCode() * name.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof WaypointKey) ) return false;
        WaypointKey that = (WaypointKey) obj;
        if ( this.name.toLowerCase().equals(that.getName().toLowerCase()) && this.owner == null && that.getOwner() == null ) return true;
        if (this.name.toLowerCase().equals(that.getName().toLowerCase()) && this.owner.equals(that.getOwner())) return true;
        return false;
    }
}
