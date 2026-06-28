package com.twohigh.core.law;

import com.twohigh.api.law.ArrestApi;
import com.twohigh.api.law.event.PlayerArrestedEvent;
import com.twohigh.core.config.CoreConfig;
import com.twohigh.core.data.CoreStorage;
import com.twohigh.core.defaults.DefaultJobs;
import com.twohigh.core.job.JobRegistryImpl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ArrestManager implements ArrestApi {

    private final ConcurrentHashMap<UUID, Long> jailedPlayers = new ConcurrentHashMap<>();
    private final CoreStorage storage;
    private final CoreConfig config;
    public final JailManager jailManager;
    private final WantedManager wantedManager;
    private final JobRegistryImpl jobRegistry;
    private final JavaPlugin plugin;

    public ArrestManager(CoreStorage storage, CoreConfig config, JailManager jailManager,
                         WantedManager wantedManager, JobRegistryImpl jobRegistry, JavaPlugin plugin) {
        this.storage = storage;
        this.config = config;
        this.jailManager = jailManager;
        this.wantedManager = wantedManager;
        this.jobRegistry = jobRegistry;
        this.plugin = plugin;
    }

    public void loadFromDatabase() {
        storage.loadActiveArrests().thenAccept(map -> {
            jailedPlayers.clear();
            jailedPlayers.putAll(map);
        });
    }

    public void startReleaseChecker() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Map.Entry<UUID, Long> entry : jailedPlayers.entrySet()) {
                if (now >= entry.getValue()) {
                    release(entry.getKey());
                }
            }
        }, 20L, 20L);
    }

    @Override
    public boolean arrest(UUID officer, UUID target) {
        if (!wantedManager.isWanted(target)) return false;
        if (isJailed(target)) return false;
        if (!jailManager.hasPositions()) return false;

        long releaseAt = System.currentTimeMillis() + config.jailTimeMs();
        jailedPlayers.put(target, releaseAt);
        storage.saveArrest(target, releaseAt);
        wantedManager.removeWanted(target);

        jobRegistry.setPlayerJob(target, DefaultJobs.CITIZEN);

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            Location jail = jailManager.randomJailLocation();
            if (jail != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    targetPlayer.teleport(jail);
                    targetPlayer.sendMessage("§c§lYou have been arrested! Jail time: "
                            + (config.jailTimeSeconds()) + "s");
                });
            }
        }

        Bukkit.getPluginManager().callEvent(new PlayerArrestedEvent(target, officer, releaseAt));
        return true;
    }

    @Override
    public boolean unarrest(UUID officer, UUID target) {
        if (!isJailed(target)) return false;
        release(target);
        return true;
    }

    @Override
    public boolean isJailed(UUID player) {
        Long releaseAt = jailedPlayers.get(player);
        if (releaseAt == null) return false;
        if (System.currentTimeMillis() >= releaseAt) {
            release(player);
            return false;
        }
        return true;
    }

    @Override
    public long getRemainingJailTimeMs(UUID player) {
        Long releaseAt = jailedPlayers.get(player);
        if (releaseAt == null) return 0;
        long remaining = releaseAt - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    private void release(UUID player) {
        jailedPlayers.remove(player);
        storage.removeArrest(player);
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                p.sendMessage("§aYou have been released from jail.");
                Location spawn = p.getWorld().getSpawnLocation();
                p.teleport(spawn);
            });
        }
    }
}
