package com.cimadev.cimpleWaypointSystem.command.tpa;

import de.fisch37.datastructures.mi.MIQueue;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TeleportRequestManager {
    private final HashMap<PlayerEntity, TeleportRequest> playerToRequest;
    private final MIQueue<TeleportRequest> requests;
    private final static TeleportRequestManager SINGLETON = new TeleportRequestManager();
    private long currentTick = 0;

    protected TeleportRequestManager() {
        this.playerToRequest = new HashMap<>();
        this.requests = new MIQueue<>();
    }

    public void tick() {
        while (true) {
            TeleportRequest head = requests.peek();
            if (head != null && head.isExpired(currentTick)){
                requests.poll();
                this.playerToRequest.remove(head.getTarget());
                System.out.println("Teleport request has expired");
            }
            else break;
            currentTick++;
        }
    }

    public void addRequest(TeleportRequest request) {
        request.setExpirationDate(currentTick);
        this.requests.add(request);
        this.playerToRequest.put(request.getTarget(), request);
    }

    public @Nullable TeleportRequest getRequest(PlayerEntity target) {
        return playerToRequest.get(target);
    }

    public long getRequestTTL() {
        return 2*60*20;
    }

    public static TeleportRequestManager getInstance() {
        return SINGLETON;
    }
}
