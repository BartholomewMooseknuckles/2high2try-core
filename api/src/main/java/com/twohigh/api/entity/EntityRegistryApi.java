package com.twohigh.api.entity;

import org.bukkit.Location;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface EntityRegistryApi {

    void registerEntity(EntityDefinition definition);

    void unregisterEntity(String entityId);

    Optional<EntityDefinition> getDefinition(String entityId);

    Collection<EntityDefinition> allDefinitions();

    boolean place(UUID owner, String entityId, Location location);

    boolean remove(Location location);

    Optional<PlacedEntity> getAt(Location location);

    Collection<PlacedEntity> getByOwner(UUID owner);

    int countByOwner(UUID owner, String entityId);
}
