package com.cimadev.cimpleWaypointSystem.command.tpa;

import de.fisch37.datastructures.mi.MINode;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;

public class TeleportRequest extends MINode {
        private final PlayerEntity origin, target;
        private long expiresAt;

        public TeleportRequest(PlayerEntity origin, PlayerEntity target) {
                this.origin = origin;
                this.target = target;
        }

        public void setExpirationDate(long tick) {
                expiresAt = tick;
        }

        public boolean isExpired(long currentTick) {
                return expiresAt <= currentTick;
        }

        public PlayerEntity getOrigin() {
                return origin;
        }

        public PlayerEntity getTarget() {
                return target;
        }

        public void perform() {
                this.origin.teleport(
                        this.target.getServer().getWorld(this.target.getWorld().getRegistryKey()),
                        this.target.getX(),
                        this.target.getY(),
                        this.target.getZ(),
                        new HashSet<>(),
                        this.target.getYaw(),
                        this.target.getPitch()
                );
        }
}
