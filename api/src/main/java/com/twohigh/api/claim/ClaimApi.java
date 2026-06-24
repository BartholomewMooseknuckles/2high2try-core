package com.twohigh.api.claim;

import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

public interface ClaimApi {

    boolean isInClaim(Location location);

    Optional<UUID> getClaimOwner(Location location);

    boolean isRaidActive(Location location);

    Optional<ClaimInfo> getClaimAt(Location location);

    int getBlockCount(String regionId);
}
