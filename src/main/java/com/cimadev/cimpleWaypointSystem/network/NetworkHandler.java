package com.cimadev.cimpleWaypointSystem.network;

import com.cimadev.cimpleWaypointSystem.Main;
import com.cimadev.cimpleWaypointSystem.command.WpsUtils;
import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;
import com.cimadev.cimpleWaypointSystem.network.packet.WaypointInfo;
import com.cimadev.cimpleWaypointSystem.network.packet.WaypointsPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class NetworkHandler {
    private static void registerReceivers() {

    }

    public static void register() {
        PacketTypes.register();
        NetworkHandler.registerReceivers();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final ServerPlayerEntity player = handler.getPlayer();

            List<Waypoint> waypoints;
            if (handler.getPlayer().hasPermissionLevel(4)) {
                waypoints = WpsUtils.getAccessibleWaypoints(player);
            } else {
                waypoints = WpsUtils.getAllWaypoints();
            }

            List<WaypointInfo> waypointInfos = new ArrayList<>(waypoints.size());
            for (Waypoint waypoint : waypoints) {
                waypointInfos.add(new WaypointInfo(
                        waypoint,
                        Main.serverState.waypointAccess(waypoint,player)
                ));
            }

            sender.sendPacket(new WaypointsPayload(waypointInfos));
        });
    }
}
