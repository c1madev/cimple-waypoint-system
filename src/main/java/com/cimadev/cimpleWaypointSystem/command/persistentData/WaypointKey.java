package com.cimadev.cimpleWaypointSystem.command.persistentData;

import com.cimadev.cimpleWaypointSystem.network.NullableCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WaypointKey {
    public static final PacketCodec<RegistryByteBuf, WaypointKey> PACKET_CODEC = PacketCodec.tuple(
            new NullableCodec<>(Uuids.PACKET_CODEC), WaypointKey::getOwner,
            PacketCodecs.STRING, WaypointKey::getName,
            new NullableCodec<>(PacketCodecs.STRING), WaypointKey::getOwnerName,
            (uuid, name, ownerName) -> new WaypointKey(uuid, name)
    );

    @Nullable
    private final UUID owner;
    private String name;

    public @Nullable UUID getOwner() {
        return owner;
    }

    private @Nullable String getOwnerName() {
        return owner == null ? null : OfflinePlayer.fromUuid(owner).getName();
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
        if (this.owner != null) nbt.putUuid("owner", this.owner);

        return nbt;
    }

    public static WaypointKey fromNbt( NbtCompound nbt ) {
        return new WaypointKey(
                nbt.contains("owner") ? nbt.getUuid("owner") : null,
                nbt.getString("name")
        );
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
