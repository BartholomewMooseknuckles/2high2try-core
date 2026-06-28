package com.twohigh.api.law.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class LockdownEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID issuer;
    private final boolean started;

    public LockdownEvent(UUID issuer, boolean started) {
        this.issuer = issuer;
        this.started = started;
    }

    public UUID getIssuerUuid() { return issuer; }
    public boolean wasStarted() { return started; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
