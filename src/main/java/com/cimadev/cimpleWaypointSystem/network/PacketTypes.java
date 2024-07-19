package com.cimadev.cimpleWaypointSystem.network;

import com.cimadev.cimpleWaypointSystem.network.packet.WaypointsPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static com.cimadev.cimpleWaypointSystem.Main.MOD_ID;

public abstract class PacketTypes {
    public static final CustomPayload.Id<WaypointsPayload> WAYPOINTS = new CustomPayload.Id<>(Identifier.of(MOD_ID, "waypoints"));;

    public static void register() {
        WaypointsPayload.register();
    }
}