package com.twohigh.core.law;

import com.twohigh.api.law.WarrantApi;
import com.twohigh.api.law.event.WarrantIssuedEvent;
import com.twohigh.core.config.CoreConfig;

import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WarrantManager implements WarrantApi {

    private final ConcurrentHashMap<UUID, long[]> warrants = new ConcurrentHashMap<>();
    private final CoreConfig config;

    public WarrantManager(CoreConfig config) {
        this.config = config;
    }

    @Override
    public boolean issue(UUID officer, UUID target, String reason) {
        long expiresAt = System.currentTimeMillis() + config.warrantExpireMs();
        warrants.put(target, new long[]{expiresAt});
        Bukkit.getPluginManager().callEvent(new WarrantIssuedEvent(target, officer, reason));
        return true;
    }

    @Override
    public boolean revoke(UUID target) {
        return warrants.remove(target) != null;
    }

    @Override
    public boolean hasWarrant(UUID target) {
        long[] entry = warrants.get(target);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry[0]) {
            warrants.remove(target);
            return false;
        }
        return true;
    }

    public void cleanExpired() {
        long now = System.currentTimeMillis();
        warrants.entrySet().removeIf(e -> now > e.getValue()[0]);
    }
}
