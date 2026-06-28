package com.twohigh.api.scoreboard;

import java.util.UUID;

public interface PlayerStatsApi {

    int getKills(UUID player);

    int getDeaths(UUID player);

    double getKD(UUID player);

    void addKill(UUID player);

    void addDeath(UUID player);
}
