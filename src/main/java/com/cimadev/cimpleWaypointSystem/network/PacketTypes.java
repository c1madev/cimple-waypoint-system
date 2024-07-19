package com.cimadev.cimpleWaypointSystem.network;

import com.cimadev.cimpleWaypointSystem.network.packet.AccessibleWaypointsPayload;
import com.cimadev.cimpleWaypointSystem.network.packet.AllWaypointsPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.lang.reflect.InvocationTargetException;

import static com.cimadev.cimpleWaypointSystem.Main.MOD_ID;

public abstract class PacketTypes {

    public static final CustomPayload.Id<AccessibleWaypointsPayload> ACCESSIBLE_WAYPOINTS = id("accessible_waypoints");
    public static final EmptyPacketType<?> REQ_ACCESSIBLE_WAYPOINTS = empty( "req_accessible_waypoints", false);
    public static final CustomPayload.Id<AllWaypointsPayload> ALL_WAYPOINTS = id("all_waypoints");
    public static final EmptyPacketType<?> REQ_ALL_WAYPOINTS = empty("req_all_waypoints", false);
    public static final EmptyPacketType<?> ERR_NO_PERMISSION = empty("err_no_permission", true);

    public static void register() {
        AccessibleWaypointsPayload.register();
        AllWaypointsPayload.register();
    }

    private static <T extends CustomPayload> CustomPayload.Id<T> id(String val) {
        return new CustomPayload.Id<>(Identifier.of(MOD_ID, val));
    }

    // Just please don't look at this
    private static EmptyPacketType<?> empty(String name, boolean s2c) {
        record EmptyPacket() implements CustomPayload {
            private static Id<EmptyPacket> id;

            @Override
            public Id<EmptyPacket> getId() {
                return id;
            }

            private static void setId(Id<EmptyPacket> newId) {
                id = newId;
            }
        }
        EmptyPacket.setId(id(name));

        EmptyPacketType<EmptyPacket> spec = new EmptyPacketType<>(
                EmptyPacket.id,
                PacketCodec.of(
                        (payload, buf) -> {},
                        buf -> new EmptyPacket()
                ),
                EmptyPacket.class
        );

        (s2c ? PayloadTypeRegistry.playS2C() : PayloadTypeRegistry.playC2S()).register(
                spec.id,
                spec.codec
        );
        return spec;
    }

    public record EmptyPacketType<T extends CustomPayload>(
            CustomPayload.Id<T> id,
            PacketCodec<RegistryByteBuf, T> codec,
            Class<T> clazz
    ) {
        public T make() {
            try {
                return clazz.getConstructor().newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                // Impossible (hopefully)
                return null;
            }
        }
    }
}