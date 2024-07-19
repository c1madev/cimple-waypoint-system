package com.cimadev.cimpleWaypointSystem.network;

import com.cimadev.cimpleWaypointSystem.Main;
import com.cimadev.cimpleWaypointSystem.command.WpsUtils;
import com.cimadev.cimpleWaypointSystem.network.packet.AccessibleWaypointsPayload;
import com.cimadev.cimpleWaypointSystem.network.packet.AllWaypointsPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

import java.util.ArrayList;

import static com.cimadev.cimpleWaypointSystem.network.PacketTypes.ERR_NO_PERMISSION;

public abstract class NetworkHandler {
    private static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(PacketTypes.REQ_ALL_WAYPOINTS.id(), (payload, context) -> {
            if (!context.player().hasPermissionLevel(4)) {
                context.responseSender().sendPacket(ERR_NO_PERMISSION.make());
            } else {
                context.responseSender().sendPacket(
                        new AllWaypointsPayload(new ArrayList<>(Main.serverState.getAllWaypoints()))
                );
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(PacketTypes.REQ_ACCESSIBLE_WAYPOINTS.id(), (payload, context) -> {
            context.responseSender().sendPacket(new AccessibleWaypointsPayload(new ArrayList<>(
                    WpsUtils.getAccessibleWaypoints(context.player())
            )));
            context.responseSender().sendPacket(new Packet<PacketListener>() {
                @Override
                public PacketType<? extends Packet<PacketListener>> getPacketId() {
                    return null;
                }

                @Override
                public void apply(PacketListener listener) {

                }
            });
        });
    }

    public static void register() {
        PacketTypes.register();
        NetworkHandler.registerReceivers();
    }
}
