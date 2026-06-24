package com.twohigh.core.claim;

import com.twohigh.api.claim.ClaimApi;
import com.twohigh.api.claim.ClaimInfo;
import com.twohigh.core.integration.WorldGuardHook;

import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClaimManagerImpl implements ClaimApi {

    private final WorldGuardHook worldGuard;
    private final ConcurrentHashMap<String, ClaimData> claims = new ConcurrentHashMap<>();

    public ClaimManagerImpl(WorldGuardHook worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Override
    public boolean isInClaim(Location location) {
        if (!worldGuard.isAvailable()) return false;
        return worldGuard.findCoreRegionAt(location).isPresent();
    }

    @Override
    public Optional<UUID> getClaimOwner(Location location) {
        if (!worldGuard.isAvailable()) return Optional.empty();
        Optional<String> regionId = worldGuard.findCoreRegionAt(location);
        if (regionId.isEmpty()) return Optional.empty();

        ClaimData data = claims.get(regionId.get());
        return data != null ? Optional.of(data.owner) : Optional.empty();
    }

    @Override
    public boolean isRaidActive(Location location) {
        if (!worldGuard.isAvailable()) return false;
        Optional<String> regionId = worldGuard.findCoreRegionAt(location);
        if (regionId.isEmpty()) return false;

        ClaimData data = claims.get(regionId.get());
        return data != null && data.raidActive;
    }

    @Override
    public Optional<ClaimInfo> getClaimAt(Location location) {
        if (!worldGuard.isAvailable()) return Optional.empty();
        Optional<String> regionId = worldGuard.findCoreRegionAt(location);
        if (regionId.isEmpty()) return Optional.empty();

        ClaimData data = claims.get(regionId.get());
        if (data == null) return Optional.empty();

        return Optional.of(new ClaimInfo(data.owner, regionId.get(),
                data.raidActive, data.decaying, data.blockCount));
    }

    @Override
    public int getBlockCount(String regionId) {
        ClaimData data = claims.get(regionId);
        return data != null ? data.blockCount : 0;
    }

    public boolean createClaim(String regionId, UUID owner, Location corner1, Location corner2) {
        if (!worldGuard.isAvailable()) return false;
        if (!worldGuard.createCoreRegion(regionId, owner, corner1, corner2)) return false;

        claims.put(regionId, new ClaimData(owner, 0, false, false));
        return true;
    }

    public boolean removeClaim(String regionId, String worldName) {
        claims.remove(regionId);
        return worldGuard.removeCoreRegion(regionId, worldName);
    }

    public void setRaidActive(String regionId, boolean active) {
        ClaimData data = claims.get(regionId);
        if (data != null) {
            data.raidActive = active;
        }
    }

    public void setDecaying(String regionId, boolean decaying) {
        ClaimData data = claims.get(regionId);
        if (data != null) {
            data.decaying = decaying;
        }
    }

    public void incrementBlockCount(String regionId) {
        ClaimData data = claims.get(regionId);
        if (data != null) {
            data.blockCount++;
        }
    }

    public void decrementBlockCount(String regionId) {
        ClaimData data = claims.get(regionId);
        if (data != null && data.blockCount > 0) {
            data.blockCount--;
        }
    }

    public ConcurrentHashMap<String, ClaimData> claimMap() {
        return claims;
    }

    public static final class ClaimData {
        final UUID owner;
        volatile int blockCount;
        volatile boolean raidActive;
        volatile boolean decaying;

        ClaimData(UUID owner, int blockCount, boolean raidActive, boolean decaying) {
            this.owner = owner;
            this.blockCount = blockCount;
            this.raidActive = raidActive;
            this.decaying = decaying;
        }

        public UUID owner() { return owner; }
    }
}
