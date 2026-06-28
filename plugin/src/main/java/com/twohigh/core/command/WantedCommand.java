package com.twohigh.core.command;

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

public final class WantedCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;

    public WantedCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            listWanted(player);
            return true;
        }

        String team = plugin.jobRegistry().getPlayerTeam(player.getUniqueId());
        if (!"police".equals(team) && !"government".equals(team)) {
            player.sendMessage("§cOnly law enforcement can use this command.");
            return true;
        }

        if (args[0].equalsIgnoreCase("remove") && args.length >= 2) {
            removeWanted(player, args[1]);
        } else if (args.length >= 2) {
            String reason = args.length > 1
                    ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                    : "No reason given";
            setWanted(player, args[0], reason);
        } else {
            player.sendMessage("§cUsage: /wanted <player> <reason> or /wanted remove <player>");
        }
        return true;
    }

    private void listWanted(Player player) {
        var wanted = plugin.lawEnforcement().wantedManager().allWanted();
        if (wanted.isEmpty()) {
            player.sendMessage("§7No wanted players.");
            return;
        }
        player.sendMessage("§6§lWanted Players");
        for (UUID uuid : wanted) {
            Player p = Bukkit.getPlayer(uuid);
            String name = p != null ? p.getName() : uuid.toString().substring(0, 8);
            String reason = plugin.lawEnforcement().wantedManager().getWantedReason(uuid);
            player.sendMessage(" §c" + name + " §8- §7" + (reason != null ? reason : "Unknown"));
        }
    }

    private void setWanted(Player officer, String targetName, String reason) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            officer.sendMessage("§cPlayer not found.");
            return;
        }
        if (target.getUniqueId().equals(officer.getUniqueId())) {
            officer.sendMessage("§cYou can't want yourself.");
            return;
        }
        if (plugin.lawEnforcement().wantedManager().setWanted(
                officer.getUniqueId(), target.getUniqueId(), reason)) {
            Bukkit.broadcastMessage("§c§l[WANTED] §e" + target.getName()
                    + " §cis now wanted! Reason: §7" + reason);
        } else {
            officer.sendMessage("§cThat player is already wanted.");
        }
    }

    private void removeWanted(Player officer, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            officer.sendMessage("§cPlayer not found.");
            return;
        }
        if (plugin.lawEnforcement().wantedManager().removeWanted(target.getUniqueId())) {
            officer.sendMessage("§a" + target.getName() + " is no longer wanted.");
        } else {
            officer.sendMessage("§cThat player is not wanted.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("remove"));
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
            return filter(options, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return filter(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).collect(Collectors.toList()), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
