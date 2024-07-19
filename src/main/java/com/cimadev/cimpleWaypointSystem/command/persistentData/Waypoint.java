package com.cimadev.cimpleWaypointSystem.command.persistentData;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.cimadev.cimpleWaypointSystem.Main.*;

public class Waypoint implements Comparable<Waypoint> {
    public static final PacketCodec<RegistryByteBuf, Waypoint> PACKET_CODEC = PacketCodec.tuple(
            WaypointKey.PACKET_CODEC, buf -> null,
            BlockPos.PACKET_CODEC, Waypoint::getPosition,
            RegistryKey.createPacketCodec(RegistryKeys.WORLD), Waypoint::getWorldRegKey,
            PacketCodecs.INTEGER, Waypoint::getYaw,
            AccessLevel.PACKET_CODEC, Waypoint::getAccess,
            Waypoint::new
    );

    private static final Logger log = LoggerFactory.getLogger(Waypoint.class);
    private final WaypointKey key;
    private BlockPos position;
    private int yaw;
    private AccessLevel access;

    private final RegistryKey<World> worldRegKey;

    public String getName() {
        return key.getName();
    }

    public BlockPos getPosition() {
        return position;
    }

    public int getYaw() {
        return yaw;
    }

    @Nullable
    public UUID getOwner() {
        return key.getOwner();
    }

    @Nullable
    public OfflinePlayer getOwnerPlayer() {
        UUID ownerUuid = this.getOwner();
        return serverState.getPlayerByUuid(ownerUuid);
    }

    public WaypointKey getKey() {
        return key;
    }

    public RegistryKey<World> getWorldRegKey() {
        return worldRegKey;
    }

    public void setName( String name ) {
        this.key.setName(name);
    }


    public void rename( String name ) {
        setName( name );
    }

    public void setPosition( BlockPos position ) {
        this.position = position;
    }

    public void setYaw( int yaw ) {
        this.yaw = yaw;
    }

    public void setAccess( AccessLevel access ) {
        if ( access != AccessLevel.OPEN ) {
            this.access = access;
        }
    }

    public AccessLevel getAccess() {
        return access;
    }

    public Text getAccessFormatted() {
        return this.access.getNameFormatted();
    }

    public Text getNameFormatted() {
        HoverEvent waypointTooltip = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("x: " + position.getX() + ", y: " + position.getY() + ", z: " + position.getZ()));
        MutableText waypointName = Text.literal(key.getName()).formatted(LINK_COLOR, Formatting.UNDERLINE);
        Style waypointStyle = waypointName.getStyle();
        waypointName.setStyle(waypointStyle.withHoverEvent(waypointTooltip));
        return waypointName;
    }

    private Waypoint(WaypointKey key, BlockPos pos, RegistryKey<World> world, Integer yaw, AccessLevel access) {
        this.key = key;
        this.position = pos;
        this.worldRegKey = world;
        this.yaw = yaw;
        this.access = access;
    }

    public Waypoint(String name, BlockPos position, Double yaw, RegistryKey<World> world, UUID owner, AccessLevel access) {
        this.key = new WaypointKey(owner, name);
        this.position = position;
        this.yaw = yaw.intValue();
        this.worldRegKey = world;
        this.access = access;
    }

    private Waypoint( NbtCompound nbt ) {
        this.key = WaypointKey.fromNbt(nbt.getCompound("key"));
        int[] position = nbt.getIntArray("position");
        this.position = new BlockPos( position[0], position[1], position[2] );
        this.yaw = nbt.getInt("yaw");
        try {
            this.access = AccessLevel.fromString(nbt.getString("access"));
        } catch (IllegalArgumentException i) {
            this.access = AccessLevel.SECRET;
            log.warn("Found unknown access level while loading waypoint. Set waypoint to secret");
        }
        Identifier regKeyVal = Identifier.of(nbt.getString( "worldRegKeyValue" ));
        Identifier regKeyReg = Identifier.of(nbt.getString( "worldRegKeyRegistry" ));
        this.worldRegKey = RegistryKey.of( RegistryKey.ofRegistry(regKeyReg), regKeyVal );
    }

    public static Waypoint fromNbt( NbtCompound nbt ) {
        return new Waypoint( nbt );
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("key", key.toNbt());
        nbt.putIntArray("position", new int[] {position.getX(), position.getY(), position.getZ()});
        nbt.putInt("yaw", yaw);
        nbt.putString("access", access.getName());
        nbt.putString("worldRegKeyRegistry", worldRegKey.getRegistry().toString() );
        nbt.putString("worldRegKeyValue", worldRegKey.getValue().toString() );

        return nbt;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
     * all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception if and only if {@code
     * y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param other the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     * @apiNote It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(@NotNull Waypoint other) {
        // TODO: WTF is going on here?
        OfflinePlayer owner1 = this.getOwnerPlayer();
        OfflinePlayer owner2 = other.getOwnerPlayer();

        if ( owner1 == null ) return -1;
        else if (owner2 == null) return 1;

        int ownerRelation = owner1.getName().compareTo(owner2.getName());
        if (ownerRelation > 0) return 1;
        else if (ownerRelation < 0) return -1;
        else {
            int nameRelation = this.getName().compareTo(other.getName());
            return Integer.compare(nameRelation, 0);
        }
    }
}
