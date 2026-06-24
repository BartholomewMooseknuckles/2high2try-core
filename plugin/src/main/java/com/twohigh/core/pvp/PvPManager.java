package com.twohigh.core.pvp;

import com.twohigh.api.pvp.PvPApi;
import com.twohigh.core.config.CoreConfig;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PvPManager implements PvPApi {

    private final CoreConfig config;
    private final CombatTagManager combatTagManager;
    private final Set<UUID> raidBubblePlayers = ConcurrentHashMap.newKeySet();

    public PvPManager(CoreConfig config, CombatTagManager combatTagManager) {
        this.config = config;
        this.combatTagManager = combatTagManager;
    }

    @Override
    public boolean canAttack(Player attacker, Player defender) {
        if (attacker.equals(defender)) return false;
        return isPvPEnabled(attacker.getLocation());
    }

    @Override
    public boolean isPvPEnabled(Location location) {
        World world = location.getWorld();
        if (world == null) return false;

        return switch (world.getEnvironment()) {
            case NORMAL -> config.overworldPvP();
            case NETHER -> config.netherPvP();
            case THE_END -> config.endPvP();
            default -> false;
        };
    }

    @Override
    public boolean isInRaidBubble(Location location) {
        return false;
    }

    public void addRaidBubblePlayer(UUID player) {
        raidBubblePlayers.add(player);
    }

    public void removeRaidBubblePlayer(UUID player) {
        raidBubblePlayers.remove(player);
    }

    public Set<UUID> raidBubblePlayers() {
        return Collections.unmodifiableSet(raidBubblePlayers);
    }
}
