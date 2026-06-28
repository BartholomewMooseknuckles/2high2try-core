package com.twohigh.core.party;

import com.twohigh.api.party.PartyApi;
import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.config.CoreConfig;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PartyManager implements PartyApi {

    private final TwoHigh2TryCore plugin;
    private final CoreConfig config;
    private final Map<UUID, Party> parties = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerPartyMap = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();

    public PartyManager(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
        this.config = plugin.coreConfig();
    }

    public Party createParty(UUID leader) {
        if (playerPartyMap.containsKey(leader)) return null;
        Party party = new Party(UUID.randomUUID(), leader);
        parties.put(party.id(), party);
        playerPartyMap.put(leader, party.id());
        return party;
    }

    public boolean invite(UUID inviter, UUID target) {
        UUID partyId = playerPartyMap.get(inviter);
        if (partyId == null) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;

        PartyRole role = party.getRole(inviter);
        if (role == null || !role.canInvite()) return false;
        if (party.size() >= config.partyMaxSize()) return false;
        if (playerPartyMap.containsKey(target)) return false;

        pendingInvites.put(target, partyId);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (partyId.equals(pendingInvites.get(target))) {
                pendingInvites.remove(target);
            }
        }, 20L * 60);

        return true;
    }

    public boolean acceptInvite(UUID player) {
        UUID partyId = pendingInvites.remove(player);
        if (partyId == null) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;
        if (party.size() >= config.partyMaxSize()) return false;

        party.addMember(player, PartyRole.MEMBER);
        playerPartyMap.put(player, partyId);
        return true;
    }

    public boolean leave(UUID player) {
        UUID partyId = playerPartyMap.remove(player);
        if (partyId == null) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;

        boolean wasLeader = party.isLeader(player);
        party.removeMember(player);

        if (party.size() == 0) {
            disbandInternal(partyId);
        } else if (wasLeader) {
            UUID newLeader = party.members().iterator().next();
            party.setRole(newLeader, PartyRole.LEADER);
            messageParty(partyId, "§6[PARTY] §7Leadership transferred to §e"
                    + playerName(newLeader) + "§7.");
        }
        return true;
    }

    public boolean kick(UUID kicker, UUID target) {
        UUID partyId = playerPartyMap.get(kicker);
        if (partyId == null) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;
        if (!party.isMember(target)) return false;

        PartyRole kickerRole = party.getRole(kicker);
        if (kickerRole == null || !kickerRole.canKick()) return false;
        if (party.isLeader(target)) return false;

        party.removeMember(target);
        playerPartyMap.remove(target);
        return true;
    }

    public boolean disband(UUID player) {
        UUID partyId = playerPartyMap.get(player);
        if (partyId == null) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;
        if (!party.isLeader(player)) return false;

        double bankBalance = party.bankBalance();
        if (bankBalance > 0) {
            int memberCount = party.size();
            double share = bankBalance / memberCount;
            for (UUID member : party.members()) {
                plugin.cashManager().deposit(member, share);
                Player p = Bukkit.getPlayer(member);
                if (p != null) {
                    p.sendMessage("§6[PARTY] §7Party disbanded. You received §a$"
                            + String.format("%.2f", share) + " §7from the party bank.");
                }
            }
        }

        messageParty(partyId, "§6[PARTY] §7The party has been disbanded.");
        disbandInternal(partyId);
        return true;
    }

    public boolean setRole(UUID setter, UUID target, PartyRole newRole) {
        UUID partyId = playerPartyMap.get(setter);
        if (partyId == null) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;
        if (!party.isMember(target)) return false;

        PartyRole setterRole = party.getRole(setter);
        if (setterRole == null || !setterRole.canSetRole()) return false;
        if (newRole == PartyRole.LEADER) {
            party.setRole(setter, PartyRole.OFFICER);
            party.setRole(target, PartyRole.LEADER);
        } else {
            party.setRole(target, newRole);
        }
        return true;
    }

    public boolean toggleFriendlyFire(UUID player) {
        UUID partyId = playerPartyMap.get(player);
        if (partyId == null) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;

        PartyRole role = party.getRole(player);
        if (role == null || !role.canToggleFriendlyFire()) return false;

        String mode = config.partyFriendlyFireMode();
        if ("always_off".equals(mode) || "always_on".equals(mode)) return false;

        party.setFriendlyFire(!party.friendlyFire());
        return true;
    }

    public boolean depositToBank(UUID player, double amount) {
        UUID partyId = playerPartyMap.get(player);
        if (partyId == null) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;
        if (amount <= 0) return false;

        double cash = plugin.cashManager().getCash(player);
        if (cash < amount) return false;

        plugin.cashManager().withdraw(player, amount);
        party.depositBank(amount);
        savePartyBank(partyId, party);
        return true;
    }

    public boolean withdrawFromBank(UUID player, double amount) {
        UUID partyId = playerPartyMap.get(player);
        if (partyId == null) return false;
        Party party = parties.get(partyId);
        if (party == null) return false;

        PartyRole role = party.getRole(player);
        if (role == null || !role.canWithdraw()) return false;
        if (!party.withdrawBank(amount)) return false;

        plugin.cashManager().deposit(player, amount);
        savePartyBank(partyId, party);
        return true;
    }

    public void messageParty(UUID partyId, String message) {
        Party party = parties.get(partyId);
        if (party == null) return;
        for (UUID member : party.members()) {
            Player p = Bukkit.getPlayer(member);
            if (p != null) p.sendMessage(message);
        }
    }

    public Optional<Party> getParty(UUID partyId) {
        return Optional.ofNullable(parties.get(partyId));
    }

    public Optional<Party> getPlayerParty(UUID player) {
        UUID partyId = playerPartyMap.get(player);
        return partyId == null ? Optional.empty() : Optional.ofNullable(parties.get(partyId));
    }

    public boolean hasPendingInvite(UUID player) {
        return pendingInvites.containsKey(player);
    }

    // --- PartyApi ---

    @Override
    public Optional<UUID> getPartyId(UUID player) {
        return Optional.ofNullable(playerPartyMap.get(player));
    }

    @Override
    public boolean areInSameParty(UUID a, UUID b) {
        UUID partyA = playerPartyMap.get(a);
        return partyA != null && partyA.equals(playerPartyMap.get(b));
    }

    @Override
    public boolean isFriendlyFireEnabled(UUID partyId) {
        String mode = config.partyFriendlyFireMode();
        if ("always_on".equals(mode)) return true;
        if ("always_off".equals(mode)) return false;
        Party party = parties.get(partyId);
        return party != null && party.friendlyFire();
    }

    @Override
    public Set<UUID> getMembers(UUID partyId) {
        Party party = parties.get(partyId);
        return party != null ? party.members() : Collections.emptySet();
    }

    @Override
    public Optional<String> getPartyLeaderName(UUID partyId) {
        Party party = parties.get(partyId);
        if (party == null) return Optional.empty();
        UUID leader = party.leader();
        return leader == null ? Optional.empty() : Optional.of(playerName(leader));
    }

    @Override
    public double getPartyBankBalance(UUID partyId) {
        Party party = parties.get(partyId);
        return party != null ? party.bankBalance() : 0;
    }

    private void disbandInternal(UUID partyId) {
        Party party = parties.remove(partyId);
        if (party != null) {
            for (UUID member : party.members()) {
                playerPartyMap.remove(member);
            }
        }
        plugin.storage().removePartyBank(partyId);
    }

    private void savePartyBank(UUID partyId, Party party) {
        plugin.storage().savePartyBank(partyId, party.bankBalance());
    }

    private String playerName(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getName() : uuid.toString().substring(0, 8);
    }
}
