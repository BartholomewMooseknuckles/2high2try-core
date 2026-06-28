package com.twohigh.core.pvp;

import com.twohigh.api.pvp.PvPApi;
import com.twohigh.core.config.CoreConfig;
import com.twohigh.core.party.PartyManager;
import com.twohigh.core.raid.RaidManager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PvPManager implements PvPApi {

    private final CoreConfig config;
    private final CombatTagManager combatTagManager;
    private RaidManager raidManager;
    private PartyManager partyManager;

    private final Set<String> mugPvPPairs = ConcurrentHashMap.newKeySet();

    public PvPManager(CoreConfig config, CombatTagManager combatTagManager) {
        this.config = config;
        this.combatTagManager = combatTagManager;
    }

    public void setRaidManager(RaidManager raidManager) {
        this.raidManager = raidManager;
    }

    public void setPartyManager(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public boolean canAttack(Player attacker, Player defender) {
        if (attacker.equals(defender)) return false;

        if (partyManager != null
                && partyManager.areInSameParty(attacker.getUniqueId(), defender.getUniqueId())) {
            java.util.Optional<java.util.UUID> partyId = partyManager.getPartyId(attacker.getUniqueId());
            if (partyId.isPresent() && !partyManager.isFriendlyFireEnabled(partyId.get())) {
                return false;
            }
        }

        if (isMugPvPActive(attacker.getUniqueId(), defender.getUniqueId())) {
            return true;
        }

        if (raidManager != null && raidManager.isLocationInActiveRaidBubble(attacker.getLocation())) {
            return true;
        }

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
        return raidManager != null && raidManager.isLocationInActiveRaidBubble(location);
    }

    public void addMugPvPPair(UUID a, UUID b) {
        mugPvPPairs.add(pairKey(a, b));
    }

    public void removeMugPvPPair(UUID a, UUID b) {
        mugPvPPairs.remove(pairKey(a, b));
    }

    public boolean isMugPvPActive(UUID a, UUID b) {
        return mugPvPPairs.contains(pairKey(a, b));
    }

    private String pairKey(UUID a, UUID b) {
        return a.compareTo(b) < 0 ? a + ":" + b : b + ":" + a;
    }
}
