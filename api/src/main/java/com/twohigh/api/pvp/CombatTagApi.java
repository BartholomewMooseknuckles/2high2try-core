package com.twohigh.api.pvp;

import org.bukkit.entity.Player;

public interface CombatTagApi {

    boolean isInCombat(Player player);

    long getCombatTagExpiry(Player player);

    void tagPlayer(Player player);
}
