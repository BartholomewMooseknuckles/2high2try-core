package com.twohigh.core.leaderboard;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.economy.CashManager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LeaderboardManager {

    private final TwoHigh2TryCore plugin;

    public LeaderboardManager(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    public List<LeaderboardEntry> getTopCash(int limit) {
        CashManager cash = plugin.cashManager();
        List<LeaderboardEntry> entries = new ArrayList<>();

        for (Map.Entry<UUID, Double> entry : cash.allCash().entrySet()) {
            String name = resolvePlayerName(entry.getKey());
            entries.add(new LeaderboardEntry(entry.getKey(), name, entry.getValue()));
        }

        entries.sort(Comparator.comparingDouble(LeaderboardEntry::amount).reversed());
        return entries.size() > limit ? entries.subList(0, limit) : entries;
    }

    public void showLeaderboard(Player player, int count) {
        List<LeaderboardEntry> top = getTopCash(count);
        player.sendMessage("§6§l--- Cash Leaderboard ---");
        for (int i = 0; i < top.size(); i++) {
            LeaderboardEntry entry = top.get(i);
            player.sendMessage("§e#" + (i + 1) + " §f" + entry.name()
                    + " §7— §a$" + String.format("%.2f", entry.amount()));
        }
        if (top.isEmpty()) {
            player.sendMessage("§7No data yet.");
        }
    }

    private String resolvePlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        String name = offline.getName();
        return name != null ? name : uuid.toString().substring(0, 8);
    }

    public record LeaderboardEntry(UUID uuid, String name, double amount) {}
}
