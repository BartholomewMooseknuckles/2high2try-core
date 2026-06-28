package com.twohigh.api.social.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerDemotedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID player;
    private final String oldJobId;
    private final String demoteGroup;

    public PlayerDemotedEvent(UUID player, String oldJobId, String demoteGroup) {
        this.player = player;
        this.oldJobId = oldJobId;
        this.demoteGroup = demoteGroup;
    }

    public UUID getPlayerUuid() { return player; }
    public String getOldJobId() { return oldJobId; }
    public String getDemoteGroup() { return demoteGroup; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
