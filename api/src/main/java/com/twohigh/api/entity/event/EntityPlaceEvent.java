package com.twohigh.api.entity.event;

import com.twohigh.api.entity.EntityDefinition;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class EntityPlaceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID player;
    private final EntityDefinition definition;
    private final Location location;
    private boolean cancelled;

    public EntityPlaceEvent(UUID player, EntityDefinition definition, Location location) {
        this.player = player;
        this.definition = definition;
        this.location = location;
    }

    public UUID getPlayerUuid() { return player; }
    public EntityDefinition getDefinition() { return definition; }
    public Location getLocation() { return location; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
