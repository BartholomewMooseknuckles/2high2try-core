package com.twohigh.core.pvp;

import com.twohigh.api.pvp.CombatTagApi;
import com.twohigh.core.config.CoreConfig;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTagManager implements CombatTagApi {

    private final ConcurrentHashMap<UUID, Long> tags = new ConcurrentHashMap<>();
    private final CoreConfig config;

    public CombatTagManager(CoreConfig config) {
        this.config = config;
    }

    @Override
    public boolean isInCombat(Player player) {
        Long expiry = tags.get(player.getUniqueId());
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            tags.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    @Override
    public long getCombatTagExpiry(Player player) {
        Long expiry = tags.get(player.getUniqueId());
        return expiry != null ? expiry : 0L;
    }

    @Override
    public void tagPlayer(Player player) {
        long expiry = System.currentTimeMillis() + (config.combatTagSeconds() * 1000L);
        tags.put(player.getUniqueId(), expiry);
    }

    public void removeTag(UUID player) {
        tags.remove(player);
    }

    public int remainingSeconds(Player player) {
        Long expiry = tags.get(player.getUniqueId());
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }
}
