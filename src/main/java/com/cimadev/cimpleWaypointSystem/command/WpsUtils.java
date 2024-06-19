package com.cimadev.cimpleWaypointSystem.command;

import com.cimadev.cimpleWaypointSystem.Main;
import com.cimadev.cimpleWaypointSystem.command.persistentData.OfflinePlayer;
import com.cimadev.cimpleWaypointSystem.command.persistentData.Waypoint;
import com.cimadev.cimpleWaypointSystem.command.persistentData.WaypointKey;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public final class WpsUtils {
    public static ArrayList<Waypoint> getAccessibleWaypoints(@Nullable ServerPlayerEntity caller, @Nullable OfflinePlayer wantedOwner, boolean overrideAccessibility, boolean onlyOpen) throws CommandSyntaxException {
        ArrayList<Waypoint> waypoints = getAllWaypoints();
        ArrayList<Waypoint> goodWaypoints = new ArrayList<>();

        for ( int i = 0 ; i < waypoints.size() ; i++ ) {
            Waypoint waypoint = waypoints.get(i);
            OfflinePlayer waypointOwner = Main.serverState.getPlayerByUuid(waypoint.getOwner());

            boolean canAccess;
            if ( caller == null ) canAccess = true;
            else canAccess = (Main.serverState.waypointAccess(waypoint, caller) || overrideAccessibility);
            if (waypointOwner == null) {
                if ((wantedOwner == null) && canAccess) {
                    goodWaypoints.add(waypoint);
                }
            } else if ( (waypointOwner.equals(wantedOwner) || wantedOwner == null)
                    && canAccess && !onlyOpen ) {
                goodWaypoints.add(waypoint);
            }
        }

        return goodWaypoints;
    }

    public static ArrayList<Waypoint> getAllWaypoints() {
        HashMap<WaypointKey, Waypoint> waypointsMap = Main.serverState.copyWaypointMap();
        ArrayList<Waypoint> waypointsList = new ArrayList<>();
        waypointsMap.forEach((wpk, wp) -> waypointsList.add(wp));
        waypointsList.sort((wp1, wp2) -> {
            OfflinePlayer owner1 = Main.serverState.getPlayerByUuid(wp1.getOwner());
            OfflinePlayer owner2 = Main.serverState.getPlayerByUuid(wp2.getOwner());

            if ( owner1 == null ) return -1;
            else if (owner2 == null) return 1;

            int relation = owner1.getName().compareTo(owner2.getName());
            if (relation > 0) return 1;
            else if (relation < 0) return -1;
            else {
                relation = wp1.getName().compareTo(wp2.getName());
                return Integer.compare(relation, 0);
            }
        });
        return waypointsList;
    }
}
