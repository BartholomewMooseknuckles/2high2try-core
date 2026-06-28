package com.twohigh.api.law.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerArrestedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID player;
    private final UUID officer;
    private final long releaseAt;

    public PlayerArrestedEvent(UUID player, UUID officer, long releaseAt) {
        this.player = player;
        this.officer = officer;
        this.releaseAt = releaseAt;
    }

    public UUID getPlayerUuid() { return player; }
    public UUID getOfficerUuid() { return officer; }
    public long getReleaseAt() { return releaseAt; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
