package com.twohigh.core.scoreboard;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class StatsCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;

    public StatsCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player target = player;
        if (args.length > 0) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return true;
            }
        }

        UUID uuid = target.getUniqueId();
        int kills = plugin.statsTracker().getKills(uuid);
        int deaths = plugin.statsTracker().getDeaths(uuid);
        double kd = plugin.statsTracker().getKD(uuid);

        player.sendMessage("§6§lStats: §f" + target.getName());
        player.sendMessage("§7Kills: §f" + kills);
        player.sendMessage("§7Deaths: §f" + deaths);
        player.sendMessage("§7K/D: §f" + String.format("%.2f", kd));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return List.of();
    }
}
