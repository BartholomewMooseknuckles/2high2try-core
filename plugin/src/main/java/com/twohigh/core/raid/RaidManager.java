package com.twohigh.core.raid;

import com.twohigh.api.claim.ClaimInfo;
import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.config.CoreConfig;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RaidManager {

    private final TwoHigh2TryCore plugin;
    private final CoreConfig config;
    private final RaidCooldownTracker cooldowns = new RaidCooldownTracker();
    private final ConcurrentHashMap<String, ActiveRaid> activeRaids = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BossBar> bossBars = new ConcurrentHashMap<>();
    private BukkitTask tickTask;

    public RaidManager(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
        this.config = plugin.coreConfig();

        this.tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public String startRaid(Player raider, String claimRegionId, Location center) {
        long baseCooldownMs = config.raidPerBaseCooldownMinutes() * 60_000L;
        long raiderCooldownMs = config.raidSameRaiderCooldownHours() * 3_600_000L;

        if (cooldowns.isBaseOnCooldown(claimRegionId, baseCooldownMs)) {
            long remaining = cooldowns.baseCooldownRemaining(claimRegionId) / 60_000;
            raider.sendMessage("§cThis base is on raid cooldown. " + remaining + "m remaining.");
            return null;
        }
        if (cooldowns.isRaiderOnCooldownForBase(raider.getUniqueId(), claimRegionId, raiderCooldownMs)) {
            long remaining = cooldowns.raiderCooldownRemaining(raider.getUniqueId(), claimRegionId) / 3_600_000;
            raider.sendMessage("§cYou already raided this base recently. " + remaining + "h remaining.");
            return null;
        }
        if (activeRaids.containsKey(claimRegionId)) {
            raider.sendMessage("§cA raid is already active on this claim.");
            return null;
        }

        long advertEndTime = System.currentTimeMillis() + (config.raidCoordPublishDelay() * 1000L);
        ActiveRaid raid = new ActiveRaid(claimRegionId, raider.getUniqueId(), center,
                config.raidBubbleRadius(), advertEndTime);
        activeRaids.put(claimRegionId, raid);

        plugin.claimManager().setRaidActive(claimRegionId, true);

        BossBar bar = Bukkit.createBossBar("§c§lRAID §7— Preparing...",
                BarColor.RED, BarStyle.SEGMENTED_10);
        bar.setProgress(1.0);
        bossBars.put(claimRegionId, bar);

        Bukkit.broadcastMessage("§c§l[RAID] §e" + raider.getName()
                + " §7is raiding a base! Coordinates in §f"
                + config.raidCoordPublishDelay() + "s§7...");

        return claimRegionId;
    }

    public void endRaid(String claimRegionId) {
        ActiveRaid raid = activeRaids.remove(claimRegionId);
        if (raid == null) return;

        raid.setState(RaidState.ENDED);
        plugin.claimManager().setRaidActive(claimRegionId, false);

        long baseCooldownMs = config.raidPerBaseCooldownMinutes() * 60_000L;
        long raiderCooldownMs = config.raidSameRaiderCooldownHours() * 3_600_000L;
        cooldowns.setBaseCooldown(claimRegionId, baseCooldownMs);
        cooldowns.setRaiderBaseCooldown(raid.raider(), claimRegionId, raiderCooldownMs);

        BossBar bar = bossBars.remove(claimRegionId);
        if (bar != null) {
            bar.removeAll();
        }

        Bukkit.broadcastMessage("§a§l[RAID ENDED] §7The raid on a base has concluded.");
    }

    public Optional<ActiveRaid> getRaid(String claimRegionId) {
        return Optional.ofNullable(activeRaids.get(claimRegionId));
    }

    public Optional<ActiveRaid> getRaidAtLocation(Location location) {
        for (ActiveRaid raid : activeRaids.values()) {
            if (raid.isInBubble(location)) {
                return Optional.of(raid);
            }
        }
        return Optional.empty();
    }

    public boolean isLocationInAnyRaidBubble(Location location) {
        return getRaidAtLocation(location).isPresent();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, ActiveRaid> entry : activeRaids.entrySet()) {
            ActiveRaid raid = entry.getValue();

            if (raid.state() == RaidState.ADVERTISED && now >= raid.advertEndTime()) {
                raid.setState(RaidState.ACTIVE);
                Location c = raid.center();
                Bukkit.broadcastMessage("§c§l[RAID] §7Coordinates revealed: §f"
                        + c.getBlockX() + ", " + c.getBlockY() + ", " + c.getBlockZ()
                        + " §7in §f" + c.getWorld().getName());

                BossBar bar = bossBars.get(entry.getKey());
                if (bar != null) {
                    bar.setTitle("§c§lRAID ACTIVE §7— PvP enabled in bubble");
                }
            }

            BossBar bar = bossBars.get(entry.getKey());
            if (bar != null) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (raid.isInBubble(online.getLocation())) {
                        bar.addPlayer(online);
                    } else {
                        bar.removePlayer(online);
                    }
                }
            }
        }
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        for (String regionId : activeRaids.keySet()) {
            endRaid(regionId);
        }
        bossBars.values().forEach(BossBar::removeAll);
        bossBars.clear();
    }

    public RaidCooldownTracker cooldowns() { return cooldowns; }
}
