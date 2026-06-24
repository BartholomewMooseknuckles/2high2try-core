package com.twohigh.api.pvp;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PvPApi {

    boolean canAttack(Player attacker, Player defender);

    boolean isPvPEnabled(Location location);

    boolean isInRaidBubble(Location location);
}
