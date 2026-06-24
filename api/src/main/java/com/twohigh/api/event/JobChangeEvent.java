package com.twohigh.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class JobChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID player;
    private final String oldJobId;
    private final String newJobId;

    public JobChangeEvent(UUID player, String oldJobId, String newJobId) {
        this.player = player;
        this.oldJobId = oldJobId;
        this.newJobId = newJobId;
    }

    public UUID getPlayerUuid() { return player; }
    public String getOldJobId() { return oldJobId; }
    public String getNewJobId() { return newJobId; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
