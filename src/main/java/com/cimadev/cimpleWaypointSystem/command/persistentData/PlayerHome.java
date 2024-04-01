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

import java.util.UUID;

import static com.cimadev.cimpleWaypointSystem.Main.LINK_COLOR;

public class PlayerHome {
    private BlockPos position;

    private int yaw;
    private RegistryKey worldRegKey;

    private UUID owner;

    public BlockPos getPosition() {
        return position;
    }

    public int getYaw() {
        return yaw;
    }

    public RegistryKey worldRegistryKey() {
        return worldRegKey;
    }

    public UUID getOwner() {
        return owner;
    }

    public Text positionHover(String text) {
        HoverEvent positionTooltip = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("x: " + position.getX() + ", y: " + position.getY() + ", z: " + position.getZ()));
        MutableText formatted = Text.literal(text).formatted(LINK_COLOR, Formatting.UNDERLINE);
        Style waypointStyle = formatted.getStyle();
        formatted.setStyle(waypointStyle.withHoverEvent(positionTooltip));
        return formatted;
    }

    public PlayerHome (BlockPos position, Double yaw, RegistryKey world, UUID owner) {
        this.position = position;
        this.yaw = yaw.intValue();
        this.worldRegKey = world;
        this.owner = owner;
    }

    public PlayerHome ( NbtCompound nbt, String ownerUUID ) {
        int position[] = nbt.getIntArray("position");
        this.position = new BlockPos( position[0], position[1], position[2] );
        this.yaw = nbt.getInt("yaw");
        Identifier regKeyVal = new Identifier(nbt.getString( "worldRegKeyValue" ));
        Identifier regKeyReg = new Identifier(nbt.getString( "worldRegKeyRegistry" ));
        this.worldRegKey = RegistryKey.of( RegistryKey.ofRegistry(regKeyReg), regKeyVal );
        this.owner = UUID.fromString(ownerUUID);
    }

    public NbtCompound writeNbt( NbtCompound nbt ) {
        NbtCompound playerStateNbt = new NbtCompound();

        playerStateNbt.putIntArray("position", new int[] {position.getX(), position.getY(), position.getZ()});
        playerStateNbt.putInt("yaw", yaw);
        playerStateNbt.putString("worldRegKeyRegistry", worldRegKey.getRegistry().toString() );
        playerStateNbt.putString("worldRegKeyValue", worldRegKey.getValue().toString() );

        nbt.put(String.valueOf(owner), playerStateNbt);

        return nbt;
    }
}
