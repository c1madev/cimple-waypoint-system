package com.cimadev.cimpleWaypointSystem.command.persistentData;

import com.cimadev.cimpleWaypointSystem.command.AccessLevel;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

import static com.cimadev.cimpleWaypointSystem.Main.*;
import static com.cimadev.cimpleWaypointSystem.command.AccessArgumentParser.accessLevelFromString;

public class Waypoint {
    private WaypointKey key;
    private BlockPos position;
    private int yaw;
    private AccessLevel access; // 0 = public, 1 = private, 2 = secret

    private RegistryKey worldRegKey;

    public String getName() {
        return key.getName();
    }

    public BlockPos getPosition() {
        return position;
    }

    public int getYaw() {
        return yaw;
    }

    public UUID getOwner() {
        return key.getOwner();
    }

    public WaypointKey getKey() {
        return key;
    }

    public RegistryKey getWorldRegKey() {
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

    public String getAccessString() {
        return this.access.getName();
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

    public Waypoint(String name, BlockPos position, Double yaw, RegistryKey world, UUID owner, AccessLevel access) {
        this.key = new WaypointKey(owner, name);
        this.position = position;
        this.yaw = yaw.intValue();
        this.worldRegKey = world;
        this.access = access;
    }

    public Waypoint (NbtCompound nbt, String waypointKey ) {
        this.key = WaypointKey.fromString(waypointKey);
        int position[] = nbt.getIntArray("position");
        this.position = new BlockPos( position[0], position[1], position[2] );
        this.yaw = nbt.getInt("yaw");
        access = AccessLevel.fromString(nbt.getString("access"));
        Identifier regKeyVal = new Identifier(nbt.getString( "worldRegKeyValue" ));
        Identifier regKeyReg = new Identifier(nbt.getString( "worldRegKeyRegistry" ));
        this.worldRegKey = RegistryKey.of( RegistryKey.ofRegistry(regKeyReg), regKeyVal );
    }

    public NbtCompound writeNbt( NbtCompound nbt ) {
        NbtCompound waypointNbt = new NbtCompound();
        waypointNbt.putIntArray("position", new int[] {position.getX(), position.getY(), position.getZ()});
        waypointNbt.putInt("yaw", yaw);
        waypointNbt.putString("access", access.getName());
        waypointNbt.putString("worldRegKeyRegistry", worldRegKey.getRegistry().toString() );
        waypointNbt.putString("worldRegKeyValue", worldRegKey.getValue().toString() );

        nbt.put(key.toString(), waypointNbt);

        return nbt;
    }

}
