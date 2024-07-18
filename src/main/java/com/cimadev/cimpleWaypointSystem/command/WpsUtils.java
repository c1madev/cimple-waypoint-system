package com.cimadev.cimpleWaypointSystem.command;

import com.cimadev.cimpleWaypointSystem.Main;
import com.cimadev.cimpleWaypointSystem.command.persistentData.OfflinePlayer;
import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public final class WpsUtils {
    public static Stream<Waypoint> getAccessibleWaypoints(
            @Nullable ServerPlayerEntity caller,
            @Nullable OfflinePlayer wantedOwner,
            boolean overrideAccessibility,
            boolean onlyOpen
    ) throws CommandSyntaxException {
        return getAllWaypointsUnsorted()
                .filter(waypoint -> {
                    OfflinePlayer owner = waypoint.getOwnerPlayer();
                    if (onlyOpen && owner != null)
                        return false;

                    boolean canAccess = overrideAccessibility // Variable lookups earlier because faster
                                || caller == null
                                || Main.serverState.waypointAccess(waypoint, caller)
                            ;
                    if (owner == null)
                        return canAccess && wantedOwner == null;
                    else
                        return wantedOwner == null || owner.equals(wantedOwner);
                })
                .sorted();  // Sort after filter because also faster
    }

    public static Stream<Waypoint> getAllWaypoints() {
        return getAllWaypointsUnsorted()
                .sorted();
    }

    private static Stream<Waypoint> getAllWaypointsUnsorted() {
        return Main.serverState
                .getAllWaypoints()
                .stream();
    }
}
