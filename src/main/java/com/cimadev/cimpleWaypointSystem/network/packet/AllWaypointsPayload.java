package com.cimadev.cimpleWaypointSystem.network.packet;

import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

import static com.cimadev.cimpleWaypointSystem.network.PacketTypes.ALL_WAYPOINTS;

public record AllWaypointsPayload(List<Waypoint> waypoints) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, AllWaypointsPayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.collection(size -> new ArrayList<Waypoint>(), Waypoint.PACKET_CODEC),
            AllWaypointsPayload::waypoints,
            AllWaypointsPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ALL_WAYPOINTS;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ALL_WAYPOINTS, PACKET_CODEC);
    }
}
