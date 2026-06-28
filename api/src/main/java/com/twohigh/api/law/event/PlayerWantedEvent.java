package com.twohigh.api.law.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerWantedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID target;
    private final UUID officer;
    private final String reason;
    private final boolean added;

    public PlayerWantedEvent(UUID target, UUID officer, String reason, boolean added) {
        this.target = target;
        this.officer = officer;
        this.reason = reason;
        this.added = added;
    }

    public UUID getTargetUuid() { return target; }
    public UUID getOfficerUuid() { return officer; }
    public String getReason() { return reason; }
    public boolean wasAdded() { return added; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
