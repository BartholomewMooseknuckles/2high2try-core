package com.twohigh.api.entity;

import org.bukkit.Location;

import java.util.UUID;

public record PlacedEntity(
        String entityId,
        UUID owner,
        Location location,
        long placedAt
) {}
