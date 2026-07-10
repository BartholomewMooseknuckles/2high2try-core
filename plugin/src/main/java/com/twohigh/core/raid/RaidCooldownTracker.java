package com.twohigh.core.raid;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RaidCooldownTracker {

    private final ConcurrentHashMap<String, Long> baseCooldowns = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> raiderBaseCooldowns = new ConcurrentHashMap<>();

    public boolean isBaseOnCooldown(String regionId) {
        Long until = baseCooldowns.get(regionId);
        return until != null && System.currentTimeMillis() < until;
    }

    public boolean isRaiderOnCooldownForBase(UUID raider, String regionId) {
        String key = raider + ":" + regionId;
        Long until = raiderBaseCooldowns.get(key);
        return until != null && System.currentTimeMillis() < until;
    }

    public void setBaseCooldown(String regionId, long cooldownMs) {
        baseCooldowns.put(regionId, System.currentTimeMillis() + cooldownMs);
    }

    public void setRaiderBaseCooldown(UUID raider, String regionId, long cooldownMs) {
        String key = raider + ":" + regionId;
        raiderBaseCooldowns.put(key, System.currentTimeMillis() + cooldownMs);
    }

    public long baseCooldownRemaining(String regionId) {
        Long until = baseCooldowns.get(regionId);
        if (until == null) return 0;
        return Math.max(0, until - System.currentTimeMillis());
    }

    public long raiderCooldownRemaining(UUID raider, String regionId) {
        String key = raider + ":" + regionId;
        Long until = raiderBaseCooldowns.get(key);
        if (until == null) return 0;
        return Math.max(0, until - System.currentTimeMillis());
    }

    public void clearAll() {
        baseCooldowns.clear();
        raiderBaseCooldowns.clear();
    }
}
