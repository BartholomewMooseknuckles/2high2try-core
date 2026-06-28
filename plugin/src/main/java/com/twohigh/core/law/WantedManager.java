package com.twohigh.core.law;

import com.twohigh.api.law.WantedApi;
import com.twohigh.api.law.event.PlayerWantedEvent;
import com.twohigh.core.data.CoreStorage;

import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WantedManager implements WantedApi {

    private final ConcurrentHashMap<UUID, String[]> wantedPlayers = new ConcurrentHashMap<>();
    private final CoreStorage storage;

    public WantedManager(CoreStorage storage) {
        this.storage = storage;
    }

    public void loadFromDatabase() {
        storage.loadAllWanted().thenAccept(map -> {
            wantedPlayers.clear();
            wantedPlayers.putAll(map);
        });
    }

    @Override
    public boolean setWanted(UUID officer, UUID target, String reason) {
        if (wantedPlayers.containsKey(target)) return false;
        wantedPlayers.put(target, new String[]{officer.toString(), reason});
        storage.saveWanted(target, officer.toString(), reason);
        Bukkit.getPluginManager().callEvent(new PlayerWantedEvent(target, officer, reason, true));
        return true;
    }

    @Override
    public boolean removeWanted(UUID target) {
        String[] entry = wantedPlayers.remove(target);
        if (entry == null) return false;
        storage.removeWanted(target);
        UUID officer = UUID.fromString(entry[0]);
        Bukkit.getPluginManager().callEvent(new PlayerWantedEvent(target, officer, entry[1], false));
        return true;
    }

    @Override
    public boolean isWanted(UUID target) {
        return wantedPlayers.containsKey(target);
    }

    @Override
    public Collection<UUID> allWanted() {
        return Collections.unmodifiableSet(wantedPlayers.keySet());
    }

    public String getWantedReason(UUID target) {
        String[] entry = wantedPlayers.get(target);
        return entry != null ? entry[1] : null;
    }
}
