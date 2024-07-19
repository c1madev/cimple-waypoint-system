package com.cimadev.cimpleWaypointSystem.command;

import com.cimadev.cimpleWaypointSystem.Main;
import com.cimadev.cimpleWaypointSystem.command.persistentData.OfflinePlayer;
import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public final class WpsUtils {
    public static List<Waypoint> getAccessibleWaypoints(@Nullable ServerPlayerEntity player) {
        return getAccessibleWaypoints(player, null, false, false);
    }

    public static List<Waypoint> getAccessibleWaypoints(
            @Nullable ServerPlayerEntity caller,
            @Nullable OfflinePlayer wantedOwner,
            boolean overrideAccessibility,
            boolean onlyOpen
    ) {
        List<Waypoint> waypoints = getAllWaypoints();
        LinkedList<Waypoint> goodWaypoints = new LinkedList<>();

        for (Waypoint waypoint : waypoints) {
            OfflinePlayer waypointOwner = Main.serverState.getPlayerByUuid(waypoint.getOwner());

            boolean canAccess;
            if (caller == null) canAccess = true;
            else canAccess = (Main.serverState.waypointAccess(waypoint, caller) || overrideAccessibility);
            if (waypointOwner == null) {
                if ((wantedOwner == null) && canAccess) {
                    goodWaypoints.add(waypoint);
                }
            } else if ((waypointOwner.equals(wantedOwner) || wantedOwner == null)
                    && canAccess && !onlyOpen) {
                goodWaypoints.add(waypoint);
            }
        }

        return goodWaypoints;
    }

    public static List<Waypoint> getAllWaypoints() {
        ArrayList<Waypoint> waypointsList = new ArrayList<>(Main.serverState.getAllWaypoints());
        waypointsList.sort(null);  // null means "use built-in comparator (see Waypoint)
        return waypointsList;
    }
}
