package com.cimadev.cimpleWaypointSystem.command.persistentData;

import java.util.UUID;

public class WaypointKey {
    private final UUID owner;
    private String name;

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public WaypointKey(UUID owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public String toString() {
        if ( this.owner == null ) return name+"/";
        return name+"/"+owner;
    }
    public static WaypointKey fromString( String waypointKey ) {
        String parts[] = waypointKey.split("/", 2);
        if ( parts[1] == "" ) return new WaypointKey(null, parts[0]);
        return new WaypointKey(UUID.fromString(parts[1]), parts[0]);
    }

    @Override
    public int hashCode() {
        if ( this.owner == null ) return name.toLowerCase().hashCode();
        return owner.hashCode() * name.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof WaypointKey) ) return false;
        WaypointKey that = (WaypointKey) obj;
        if ( this.name.toLowerCase().equals(that.getName().toLowerCase()) && this.owner == null && that.getOwner() == null ) return true;
        if (this.name.toLowerCase().equals(that.getName().toLowerCase()) && this.owner.equals(that.getOwner())) return true;
        return false;
    }
}
