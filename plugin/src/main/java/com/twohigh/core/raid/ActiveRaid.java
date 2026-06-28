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
    private volatile long advertEndTime;
    private volatile long activeStartTime;
    private volatile Long abandonTimerStartedAt;

    private final Set<UUID> attackers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> defenders = ConcurrentHashMap.newKeySet();
    private final Set<UUID> notifiedWalkIns = ConcurrentHashMap.newKeySet();

    private volatile boolean legit;
    private volatile int blocksBroken;
    private volatile double lootValue;

    public ActiveRaid(String claimRegionId, UUID raider, Location center,
                      int bubbleRadius, long advertEndTime) {
        this.claimRegionId = claimRegionId;
        this.raider = raider;
        this.center = center;
        this.bubbleRadius = bubbleRadius;
        this.startTime = System.currentTimeMillis();
        this.state = RaidState.ADVERTISED;
        this.advertEndTime = advertEndTime;
        this.attackers.add(raider);
    }

    public String claimRegionId() { return claimRegionId; }
    public UUID raider() { return raider; }
    public Location center() { return center; }
    public int bubbleRadius() { return bubbleRadius; }
    public long startTime() { return startTime; }
    public RaidState state() { return state; }
    public void setState(RaidState state) { this.state = state; }
    public long advertEndTime() { return advertEndTime; }
    public long activeStartTime() { return activeStartTime; }

    public void markActive() {
        this.activeStartTime = System.currentTimeMillis();
        this.state = RaidState.ACTIVE;
    }

    public boolean isInBubble(Location loc) {
        if (!loc.getWorld().equals(center.getWorld())) return false;
        return loc.distanceSquared(center) <= (double) bubbleRadius * bubbleRadius;
    }

    public long elapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    public long activeElapsedSeconds() {
        if (activeStartTime == 0) return 0;
        return (System.currentTimeMillis() - activeStartTime) / 1000;
    }

    // --- Attackers / Defenders / Walk-ins ---

    public Set<UUID> attackers() { return Collections.unmodifiableSet(attackers); }
    public Set<UUID> defenders() { return Collections.unmodifiableSet(defenders); }

    public boolean isAttacker(UUID uuid) { return attackers.contains(uuid); }
    public boolean isDefender(UUID uuid) { return defenders.contains(uuid); }
    public boolean isInvolved(UUID uuid) { return attackers.contains(uuid) || defenders.contains(uuid); }

    public void addAttacker(UUID uuid) {
        attackers.add(uuid);
        notifiedWalkIns.remove(uuid);
    }

    public void addDefender(UUID uuid) {
        defenders.add(uuid);
        notifiedWalkIns.remove(uuid);
    }

    public boolean hasBeenNotified(UUID uuid) { return notifiedWalkIns.contains(uuid); }
    public void markNotified(UUID uuid) { notifiedWalkIns.add(uuid); }

    // --- Abandon timer ---

    public void startAbandonTimer() {
        if (abandonTimerStartedAt == null) {
            abandonTimerStartedAt = System.currentTimeMillis();
        }
    }

    public void clearAbandonTimer() {
        abandonTimerStartedAt = null;
    }

    public Long abandonTimerStartedAt() { return abandonTimerStartedAt; }

    public long abandonElapsedSeconds() {
        if (abandonTimerStartedAt == null) return 0;
        return (System.currentTimeMillis() - abandonTimerStartedAt) / 1000;
    }

    // --- Phase 2: Legitimacy + destruction ---

    public boolean isLegit() { return legit; }
    public void markLegit() { this.legit = true; }

    public int blocksBroken() { return blocksBroken; }
    public void incrementBlocksBroken() { blocksBroken++; }

    public double lootValue() { return lootValue; }
    public void addLootValue(double value) { this.lootValue += value; }
}
