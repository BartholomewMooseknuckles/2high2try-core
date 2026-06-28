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
import java.util.Set;
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
        if (cooldowns.isBaseOnCooldown(claimRegionId)) {
            long remaining = cooldowns.baseCooldownRemaining(claimRegionId) / 60_000;
            raider.sendMessage("§cThis base is on raid cooldown. " + remaining + "m remaining.");
            return null;
        }
        if (cooldowns.isRaiderOnCooldownForBase(raider.getUniqueId(), claimRegionId)) {
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

        if (raid.isLegit()) {
            int blockCount = plugin.claimManager().getBlockCount(claimRegionId);
            double destructionPct = blockCount > 0
                    ? (raid.blocksBroken() * 100.0 / blockCount) : 0;
            int cooldownMinutes = Math.min(config.raidCooldownMaxMinutes(),
                    Math.max(0, config.raidCooldownFloorMinutes()
                            + (int) (destructionPct * config.raidCooldownMinutesPerPercent())));

            cooldowns.setBaseCooldown(claimRegionId, cooldownMinutes * 60_000L);
            cooldowns.setRaiderBaseCooldown(raid.raider(), claimRegionId,
                    config.raidSameRaiderCooldownHours() * 3_600_000L);

            Bukkit.broadcastMessage("§a§l[RAID ENDED] §7Destruction: §f"
                    + String.format("%.1f", destructionPct) + "% §7| Loot: §a$"
                    + String.format("%.0f", raid.lootValue())
                    + " §7| Cooldown: §f" + cooldownMinutes + "m");
        } else {
            Bukkit.broadcastMessage("§7§l[RAID ENDED] §7Illegitimate raid — no cooldown applied.");
        }

        BossBar bar = bossBars.remove(claimRegionId);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void addCounter(UUID player, String claimRegionId) {
        ActiveRaid raid = activeRaids.get(claimRegionId);
        if (raid != null) {
            raid.addAttacker(player);
        }
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

    public Optional<ActiveRaid> getActiveRaidAtLocation(Location location) {
        for (ActiveRaid raid : activeRaids.values()) {
            if (raid.state() == RaidState.ACTIVE && raid.isInBubble(location)) {
                return Optional.of(raid);
            }
        }
        return Optional.empty();
    }

    public boolean isLocationInActiveRaidBubble(Location location) {
        return getActiveRaidAtLocation(location).isPresent();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, ActiveRaid> entry : activeRaids.entrySet()) {
            ActiveRaid raid = entry.getValue();
            String regionId = entry.getKey();

            if (raid.state() == RaidState.ADVERTISED && now >= raid.advertEndTime()) {
                raid.markActive();
                Location c = raid.center();
                Bukkit.broadcastMessage("§c§l[RAID] §7Coordinates revealed: §f"
                        + c.getBlockX() + ", " + c.getBlockY() + ", " + c.getBlockZ()
                        + " §7in §f" + c.getWorld().getName());
            }

            if (raid.state() == RaidState.ACTIVE) {
                if (raid.activeElapsedSeconds() >= config.raidDurationSeconds()) {
                    endRaid(regionId);
                    continue;
                }

                boolean anyAttackersInBubble = false;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (raid.isInBubble(online.getLocation()) && raid.isAttacker(online.getUniqueId())) {
                        anyAttackersInBubble = true;
                        break;
                    }
                }

                if (!anyAttackersInBubble) {
                    raid.startAbandonTimer();
                    if (raid.abandonElapsedSeconds() >= config.raidAbandonGraceSeconds()) {
                        Bukkit.broadcastMessage("§7§l[RAID] §7All attackers left — raid abandoned.");
                        endRaid(regionId);
                        continue;
                    }
                } else {
                    raid.clearAbandonTimer();
                }
            }

            // Walk-in detection + boss bar management
            BossBar bar = bossBars.get(regionId);
            for (Player online : Bukkit.getOnlinePlayers()) {
                boolean inBubble = raid.isInBubble(online.getLocation());
                UUID uuid = online.getUniqueId();

                if (bar != null) {
                    if (inBubble) bar.addPlayer(online);
                    else bar.removePlayer(online);
                }

                if (inBubble && raid.state() == RaidState.ACTIVE && !raid.isInvolved(uuid)) {
                    if (plugin.partyManager() != null) {
                        com.twohigh.core.party.PartyManager pm = plugin.partyManager();
                        Optional<UUID> playerParty = pm.getPartyId(uuid);
                        if (playerParty.isPresent()) {
                            Set<UUID> partyMembers = pm.getMembers(playerParty.get());
                            boolean partyAttacker = partyMembers.stream().anyMatch(raid::isAttacker);
                            boolean partyDefender = partyMembers.stream().anyMatch(raid::isDefender);
                            if (partyAttacker && !partyDefender) {
                                raid.addAttacker(uuid);
                                online.sendMessage("§6§l[RAID] §7Auto-joined as §cattacker §7(party member in raid).");
                                continue;
                            } else if (partyDefender && !partyAttacker) {
                                raid.addDefender(uuid);
                                online.sendMessage("§6§l[RAID] §7Auto-joined as §adefender §7(party member in raid).");
                                continue;
                            }
                        }
                    }

                    if (!raid.hasBeenNotified(uuid)) {
                        raid.markNotified(uuid);
                        online.sendMessage("§6§l[RAID] §7You walked into a raid.");
                        online.sendMessage("§7Use §e/advert counter §7to attack, "
                                + "§e/advert defend §7to help, or leave the area.");
                    }
                }
            }

            // Boss bar title with destruction %
            if (bar != null && raid.state() == RaidState.ACTIVE) {
                int blockCount = plugin.claimManager().getBlockCount(regionId);
                if (blockCount > 0) {
                    double pct = raid.blocksBroken() * 100.0 / blockCount;
                    bar.setTitle("§c§lRAID ACTIVE §7— "
                            + String.format("%.1f", pct) + "% destroyed");
                    bar.setProgress(Math.min(1.0, raid.blocksBroken() / (double) Math.max(1, blockCount)));
                } else {
                    bar.setTitle("§c§lRAID ACTIVE §7— " + raid.blocksBroken() + " blocks broken");
                }
                long remaining = config.raidDurationSeconds() - raid.activeElapsedSeconds();
                if (remaining > 0) {
                    long min = remaining / 60;
                    long sec = remaining % 60;
                    bar.setTitle(bar.getTitle() + " §8(" + min + "m " + sec + "s)");
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
