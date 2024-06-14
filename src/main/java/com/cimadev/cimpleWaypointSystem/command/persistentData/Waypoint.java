package com.cimadev.cimpleWaypointSystem.command.persistentData;

import com.cimadev.cimpleWaypointSystem.Main;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.cimadev.cimpleWaypointSystem.Main.*;

public class Waypoint {
    private WaypointKey key;
    private BlockPos position;
    private int yaw;
    private int access; // 0 = public, 1 = private, 2 = secret

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

    public void setAccess( int access ) {
        if ( key.getOwner() != null && !(access < 0 || access > 2) ) {
            this.access = access;
        }
    }

    public int getAccess() {
        return access;
    }
    public String getAccessString() {
        switch (this.access) {
            case 0: return "public";
            case 1: return "private";
            case 2: return "secret";
            default: return "[An error has occurred in determining the access. Try setting it manually.]";
        }
    }

    public Text getAccessFormatted() {
        if (this.getOwner() == null)
            return Text.literal("open").formatted(PUBLIC_COLOR);
        return switch (this.access) {
            case 0 -> Text.literal("public").formatted(PUBLIC_COLOR);
            case 1 -> Text.literal("private").formatted(PRIVATE_COLOR);
            case 2 -> Text.literal("secret").formatted(SECRET_COLOR);
            default -> Text.literal("[An error has occurred in determining the access. Try setting it manually.");
        };
    }

    public Text getNameFormatted() {
        HoverEvent waypointTooltip = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("x: " + position.getX() + ", y: " + position.getY() + ", z: " + position.getZ()));
        MutableText waypointName = Text.literal(key.getName()).formatted(LINK_COLOR, Formatting.UNDERLINE);
        Style waypointStyle = waypointName.getStyle();
        waypointName.setStyle(waypointStyle.withHoverEvent(waypointTooltip));
        return waypointName;
    }

    public Waypoint(String name, BlockPos position, Double yaw, RegistryKey world, UUID owner, int access) {
        this.key = new WaypointKey(owner, name);
        this.position = position;
        this.yaw = yaw.intValue();
        this.worldRegKey = world;
        if ( owner == null ) this.access = 0;
        else this.access = !(access < 0 || access > 2) ? access : 0;
    }

    public Waypoint (NbtCompound nbt, String waypointKey ) {
        this.key = WaypointKey.fromString(waypointKey);
        int position[] = nbt.getIntArray("position");
        this.position = new BlockPos( position[0], position[1], position[2] );
        this.yaw = nbt.getInt("yaw");
        access = nbt.getInt("access");
        Identifier regKeyVal = new Identifier(nbt.getString( "worldRegKeyValue" ));
        Identifier regKeyReg = new Identifier(nbt.getString( "worldRegKeyRegistry" ));
        this.worldRegKey = RegistryKey.of( RegistryKey.ofRegistry(regKeyReg), regKeyVal );
    }

    public NbtCompound writeNbt( NbtCompound nbt ) {
        NbtCompound waypointNbt = new NbtCompound();
        waypointNbt.putIntArray("position", new int[] {position.getX(), position.getY(), position.getZ()});
        waypointNbt.putInt("yaw", yaw);
        waypointNbt.putInt("access", access);
        waypointNbt.putString("worldRegKeyRegistry", worldRegKey.getRegistry().toString() );
        waypointNbt.putString("worldRegKeyValue", worldRegKey.getValue().toString() );

        nbt.put(key.toString(), waypointNbt);

        return nbt;
    }

}
