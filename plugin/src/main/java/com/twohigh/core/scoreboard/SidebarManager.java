package com.twohigh.core.scoreboard;

import com.twohigh.api.job.JobDefinition;
import com.twohigh.api.scoreboard.ScoreboardApi;
import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SidebarManager implements ScoreboardApi {

    private final TwoHigh2TryCore plugin;
    private final Set<UUID> enabled = ConcurrentHashMap.newKeySet();
    private BukkitTask updateTask;

    public SidebarManager(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    public void startUpdating() {
        updateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::refreshAll, 20L, 100L);
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    @Override
    public void showSidebar(UUID player) {
        enabled.add(player);
        Player p = Bukkit.getPlayer(player);
        if (p != null) updatePlayer(p);
    }

    @Override
    public void hideSidebar(UUID player) {
        enabled.remove(player);
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    @Override
    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (enabled.contains(p.getUniqueId())) {
                updatePlayer(p);
            }
        }
    }

    public void autoEnable(UUID player) {
        enabled.add(player);
    }

    private void updatePlayer(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("darkrp", Criteria.DUMMY, "§6§l2HIGH2TRY");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        UUID uuid = player.getUniqueId();

        Optional<String> jobId = plugin.jobRegistry().getPlayerJob(uuid);
        String jobName = "None";
        if (jobId.isPresent()) {
            Optional<JobDefinition> def = plugin.jobRegistry().getJob(jobId.get());
            jobName = def.map(JobDefinition::displayName).orElse(jobId.get());
        }

        double cash = plugin.cashManager().getCash(uuid);
        double kd = plugin.statsTracker().getKD(uuid);
        int kills = plugin.statsTracker().getKills(uuid);
        int deaths = plugin.statsTracker().getDeaths(uuid);

        obj.getScore("§7Job: §f" + jobName).setScore(4);
        obj.getScore("§7Cash: §a$" + String.format("%.0f", cash)).setScore(3);
        obj.getScore("§7K/D: §f" + String.format("%.2f", kd)
                + " §8(" + kills + "/" + deaths + ")").setScore(2);
        obj.getScore("§8----------------").setScore(1);

        if (plugin.lawEnforcement().arrestManager().isJailed(uuid)) {
            long remaining = plugin.lawEnforcement().arrestManager().getRemainingJailTimeMs(uuid) / 1000;
            obj.getScore("§c§lJAILED §7" + remaining + "s").setScore(0);
        } else if (plugin.lawEnforcement().lockdownManager().isActive()) {
            obj.getScore("§c§lLOCKDOWN ACTIVE").setScore(0);
        }

        player.setScoreboard(board);
    }
}
