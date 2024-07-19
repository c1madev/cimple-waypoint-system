package com.cimadev.cimpleWaypointSystem;

import com.cimadev.cimpleWaypointSystem.command.persistentData.ServerState;
import com.cimadev.cimpleWaypointSystem.registry.ModRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {

    public static final String MOD_ID = "cimple-waypoint-system";
    public static final Logger LOGGER = LoggerFactory.getLogger("CWPS");


    public static final Formatting DEFAULT_COLOR = Formatting.GOLD;
    public static final Formatting SECONDARY_COLOR = Formatting.DARK_RED;
    public static final Formatting LINK_COLOR = Formatting.LIGHT_PURPLE;
    public static final Formatting LINK_INACTIVE_COLOR = Formatting.DARK_PURPLE;
    public static final Formatting SECRET_COLOR = Formatting.RED;
    public static final Formatting PRIVATE_COLOR = Formatting.YELLOW;
    public static final Formatting PUBLIC_COLOR = Formatting.DARK_GREEN;
    public static final Formatting OPEN_COLOR = PUBLIC_COLOR;
    public static final Formatting PLAYER_COLOR = Formatting.GREEN;
    public static ServerState serverState;
    public static Config config;
    @Override
    public void onInitialize() {
        ModRegistries.registerAll();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            System.out.println("Initializing server state!");
            serverState = ServerState.getServerState(server);
        });
        config = Config.build();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> serverState.setPlayer(handler.player));

    }
}
