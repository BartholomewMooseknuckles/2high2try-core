package com.twohigh.core.raid;

import org.bukkit.Location;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActiveRaid {

    private final String claimRegionId;
    private final UUID raider;
    private final Location center;
    private final int bubbleRadius;
    private final long startTime;
    private volatile RaidState state;
    private final Set<UUID> defenders = ConcurrentHashMap.newKeySet();
    private volatile long advertEndTime;

    public ActiveRaid(String claimRegionId, UUID raider, Location center,
                      int bubbleRadius, long advertEndTime) {
        this.claimRegionId = claimRegionId;
        this.raider = raider;
        this.center = center;
        this.bubbleRadius = bubbleRadius;
        this.startTime = System.currentTimeMillis();
        this.state = RaidState.ADVERTISED;
        this.advertEndTime = advertEndTime;
    }

    public String claimRegionId() { return claimRegionId; }
    public UUID raider() { return raider; }
    public Location center() { return center; }
    public int bubbleRadius() { return bubbleRadius; }
    public long startTime() { return startTime; }
    public RaidState state() { return state; }
    public void setState(RaidState state) { this.state = state; }
    public Set<UUID> defenders() { return Collections.unmodifiableSet(defenders); }
    public void addDefender(UUID uuid) { defenders.add(uuid); }
    public long advertEndTime() { return advertEndTime; }

    public boolean isInBubble(Location loc) {
        if (!loc.getWorld().equals(center.getWorld())) return false;
        return loc.distanceSquared(center) <= (double) bubbleRadius * bubbleRadius;
    }

    public long elapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}
