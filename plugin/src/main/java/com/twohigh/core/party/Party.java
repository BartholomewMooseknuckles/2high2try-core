package com.twohigh.core.party;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Party {

    private final UUID id;
    private final Map<UUID, PartyRole> members = new ConcurrentHashMap<>();
    private volatile boolean friendlyFire;
    private volatile double bankBalance;

    public Party(UUID id, UUID leader) {
        this.id = id;
        this.members.put(leader, PartyRole.LEADER);
        this.friendlyFire = false;
        this.bankBalance = 0;
    }

    public UUID id() { return id; }

    public Set<UUID> members() {
        return Collections.unmodifiableSet(members.keySet());
    }

    public int size() {
        return members.size();
    }

    public PartyRole getRole(UUID player) {
        return members.get(player);
    }

    public boolean isMember(UUID player) {
        return members.containsKey(player);
    }

    public boolean isLeader(UUID player) {
        return members.get(player) == PartyRole.LEADER;
    }

    public UUID leader() {
        for (Map.Entry<UUID, PartyRole> entry : members.entrySet()) {
            if (entry.getValue() == PartyRole.LEADER) return entry.getKey();
        }
        return null;
    }

    public void addMember(UUID player, PartyRole role) {
        members.put(player, role);
    }

    public void removeMember(UUID player) {
        members.remove(player);
    }

    public void setRole(UUID player, PartyRole role) {
        if (members.containsKey(player)) {
            members.put(player, role);
        }
    }

    public boolean friendlyFire() { return friendlyFire; }
    public void setFriendlyFire(boolean ff) { this.friendlyFire = ff; }

    public double bankBalance() { return bankBalance; }
    public void setBankBalance(double balance) { this.bankBalance = balance; }

    public void depositBank(double amount) {
        this.bankBalance += amount;
    }

    public boolean withdrawBank(double amount) {
        if (bankBalance < amount) return false;
        bankBalance -= amount;
        return true;
    }
}
