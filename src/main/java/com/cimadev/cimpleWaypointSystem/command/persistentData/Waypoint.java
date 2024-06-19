package com.cimadev.cimpleWaypointSystem.command.persistentData;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.cimadev.cimpleWaypointSystem.Main.*;

public class Waypoint {
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

    public Waypoint(String name, BlockPos position, Double yaw, RegistryKey<World> world, UUID owner, AccessLevel access) {
        this.key = new WaypointKey(owner, name);
        this.position = position;
        this.yaw = yaw.intValue();
        this.worldRegKey = world;
        this.access = access;
    }

    private Waypoint( NbtCompound nbt ) {
        this.key = WaypointKey.fromString(nbt.getString("key"));
        int[] position = nbt.getIntArray("position");
        this.position = new BlockPos( position[0], position[1], position[2] );
        this.yaw = nbt.getInt("yaw");
        try {
            this.access = AccessLevel.fromString(nbt.getString("access"), true);
        } catch (IllegalArgumentException i) {
            /*todo: log the problem*/
            this.access = AccessLevel.SECRET;
        }
        Identifier regKeyVal = new Identifier(nbt.getString( "worldRegKeyValue" ));
        Identifier regKeyReg = new Identifier(nbt.getString( "worldRegKeyRegistry" ));
        this.worldRegKey = RegistryKey.of( RegistryKey.ofRegistry(regKeyReg), regKeyVal );
    }

    public static Waypoint fromNbt( NbtCompound nbt ) {
        return new Waypoint( nbt );
    }

    public NbtCompound writeNbt( ) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("key", key.toString());
        nbt.putIntArray("position", new int[] {position.getX(), position.getY(), position.getZ()});
        nbt.putInt("yaw", yaw);
        nbt.putString("access", access.getName());
        nbt.putString("worldRegKeyRegistry", worldRegKey.getRegistry().toString() );
        nbt.putString("worldRegKeyValue", worldRegKey.getValue().toString() );

        return nbt;
    }

}
