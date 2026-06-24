package com.twohigh.core.economy;

import com.twohigh.core.data.CoreStorage;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class CashManager {

    private final ConcurrentHashMap<UUID, Double> cache = new ConcurrentHashMap<>();
    private final CoreStorage storage;
    private final Logger logger;

    public CashManager(CoreStorage storage, Logger logger) {
        this.storage = storage;
        this.logger = logger;
    }

    public void loadPlayer(UUID player) {
        storage.loadCash(player).thenAccept(cash -> cache.put(player, cash));
    }

    public void unloadPlayer(UUID player) {
        Double cash = cache.remove(player);
        if (cash != null) {
            storage.saveCash(player, cash);
        }
    }

    public double getCash(UUID player) {
        return cache.getOrDefault(player, 0.0);
    }

    public boolean hasCash(UUID player, double amount) {
        return getCash(player) >= amount;
    }

    public boolean deposit(UUID player, double amount) {
        if (amount <= 0) return false;
        cache.compute(player, (k, v) -> (v == null ? 0.0 : v) + amount);
        storage.saveCash(player, getCash(player));
        return true;
    }

    public boolean withdraw(UUID player, double amount) {
        if (amount <= 0) return false;
        Double[] result = new Double[1];
        cache.compute(player, (k, v) -> {
            double current = v == null ? 0.0 : v;
            if (current < amount) {
                result[0] = null;
                return v;
            }
            result[0] = current - amount;
            return current - amount;
        });
        if (result[0] == null) return false;
        storage.saveCash(player, result[0]);
        return true;
    }

    public double clearCash(UUID player) {
        Double previous = cache.put(player, 0.0);
        double amount = previous == null ? 0.0 : previous;
        storage.saveCash(player, 0.0);
        return amount;
    }

    public Map<UUID, Double> allCash() {
        return Collections.unmodifiableMap(cache);
    }

    public void flushAll() {
        cache.forEach((uuid, cash) -> storage.saveCash(uuid, cash));
    }
}
