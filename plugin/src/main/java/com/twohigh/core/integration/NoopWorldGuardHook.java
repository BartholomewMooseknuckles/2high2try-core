package com.twohigh.core.integration;

import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

public final class NoopWorldGuardHook implements WorldGuardHook {

    @Override public boolean isAvailable() { return false; }

    @Override public boolean isInRegion(Location location, String regionId) { return false; }

    @Override public Optional<String> findCoreRegionAt(Location location) { return Optional.empty(); }

    @Override
    public boolean createCoreRegion(String regionId, UUID owner, Location corner1, Location corner2) {
        return false;
    }

    @Override public boolean removeCoreRegion(String regionId, String worldName) { return false; }

    @Override public boolean setFlag(String regionId, String worldName, String flag, String value) {
        return false;
    }
}
