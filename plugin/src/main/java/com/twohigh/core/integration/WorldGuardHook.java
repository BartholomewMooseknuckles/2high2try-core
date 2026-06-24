package com.twohigh.core.integration;

import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

public interface WorldGuardHook {

    boolean isAvailable();

    boolean isInRegion(Location location, String regionId);

    Optional<String> findCoreRegionAt(Location location);

    boolean createCoreRegion(String regionId, UUID owner, Location corner1, Location corner2);

    boolean removeCoreRegion(String regionId, String worldName);

    boolean setFlag(String regionId, String worldName, String flag, String value);
}
