package com.twohigh.api.entity.event;

import com.twohigh.api.entity.PlacedEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class EntityBreakEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID breaker;
    private final PlacedEntity entity;

    public EntityBreakEvent(UUID breaker, PlacedEntity entity) {
        this.breaker = breaker;
        this.entity = entity;
    }

    public UUID getBreakerUuid() { return breaker; }
    public PlacedEntity getEntity() { return entity; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
