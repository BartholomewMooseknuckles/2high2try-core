package com.twohigh.core.scoreboard;

import com.twohigh.api.scoreboard.PlayerStatsApi;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStatsTracker implements PlayerStatsApi {

    private final ConcurrentHashMap<UUID, int[]> stats = new ConcurrentHashMap<>();

    @Override
    public int getKills(UUID player) {
        return getOrCreate(player)[0];
    }

    @Override
    public int getDeaths(UUID player) {
        return getOrCreate(player)[1];
    }

    @Override
    public double getKD(UUID player) {
        int[] s = getOrCreate(player);
        return s[1] == 0 ? s[0] : (double) s[0] / s[1];
    }

    @Override
    public void addKill(UUID player) {
        getOrCreate(player)[0]++;
    }

    @Override
    public void addDeath(UUID player) {
        getOrCreate(player)[1]++;
    }

    private int[] getOrCreate(UUID player) {
        return stats.computeIfAbsent(player, k -> new int[2]);
    }
}
