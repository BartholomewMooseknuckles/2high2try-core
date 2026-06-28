package com.twohigh.core.entity;

import com.twohigh.api.entity.EntityDefinition;
import com.twohigh.api.entity.EntityRegistryApi;
import com.twohigh.api.entity.PlacedEntity;
import com.twohigh.api.entity.event.EntityBreakEvent;
import com.twohigh.api.entity.event.EntityPlaceEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class EntityRegistryImpl implements EntityRegistryApi {

    private final ConcurrentHashMap<String, EntityDefinition> definitions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PlacedEntity> placed = new ConcurrentHashMap<>();
    private final Logger logger;

    public EntityRegistryImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void registerEntity(EntityDefinition definition) {
        if (definitions.putIfAbsent(definition.id(), definition) != null) {
            logger.warning("Entity '" + definition.id() + "' already registered — ignoring.");
            return;
        }
        logger.info("Registered entity: " + definition.displayName() + " (" + definition.id()
                + ") from " + definition.owningPlugin().getName());
    }

    @Override
    public void unregisterEntity(String entityId) {
        definitions.remove(entityId);
    }

    @Override
    public Optional<EntityDefinition> getDefinition(String entityId) {
        return Optional.ofNullable(definitions.get(entityId));
    }

    @Override
    public Collection<EntityDefinition> allDefinitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    @Override
    public boolean place(UUID owner, String entityId, Location location) {
        EntityDefinition def = definitions.get(entityId);
        if (def == null) return false;

        if (def.maxPerPlayer() > 0 && countByOwner(owner, entityId) >= def.maxPerPlayer()) {
            return false;
        }

        EntityPlaceEvent event = new EntityPlaceEvent(owner, def, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        String key = locationKey(location);
        PlacedEntity entity = new PlacedEntity(entityId, owner, location.clone(), System.currentTimeMillis());
        placed.put(key, entity);

        Block block = location.getBlock();
        block.setType(def.blockMaterial());

        return true;
    }

    @Override
    public boolean remove(Location location) {
        String key = locationKey(location);
        PlacedEntity entity = placed.remove(key);
        if (entity == null) return false;

        Bukkit.getPluginManager().callEvent(new EntityBreakEvent(null, entity));
        location.getBlock().setType(org.bukkit.Material.AIR);
        return true;
    }

    public boolean remove(Location location, UUID breaker) {
        String key = locationKey(location);
        PlacedEntity entity = placed.remove(key);
        if (entity == null) return false;

        Bukkit.getPluginManager().callEvent(new EntityBreakEvent(breaker, entity));
        location.getBlock().setType(org.bukkit.Material.AIR);
        return true;
    }

    @Override
    public Optional<PlacedEntity> getAt(Location location) {
        return Optional.ofNullable(placed.get(locationKey(location)));
    }

    @Override
    public Collection<PlacedEntity> getByOwner(UUID owner) {
        Collection<PlacedEntity> result = new ArrayList<>();
        for (PlacedEntity e : placed.values()) {
            if (e.owner().equals(owner)) result.add(e);
        }
        return result;
    }

    @Override
    public int countByOwner(UUID owner, String entityId) {
        int count = 0;
        for (PlacedEntity e : placed.values()) {
            if (e.owner().equals(owner) && e.entityId().equals(entityId)) count++;
        }
        return count;
    }

    public Map<String, PlacedEntity> allPlaced() {
        return Collections.unmodifiableMap(placed);
    }

    private String locationKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }
}
