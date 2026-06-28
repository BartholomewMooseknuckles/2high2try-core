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
import java.util.stream.Collectors;

public final class WarrantCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;

    public WarrantCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        String team = plugin.jobRegistry().getPlayerTeam(player.getUniqueId());
        if (!"police".equals(team) && !"government".equals(team)) {
            player.sendMessage("§cOnly law enforcement can issue warrants.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /warrant <player> [reason] or /warrant revoke <player>");
            return true;
        }

        if (args[0].equalsIgnoreCase("revoke") && args.length >= 2) {
            revokeWarrant(player, args[1]);
        } else {
            String reason = args.length > 1
                    ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                    : "Suspected criminal activity";
            issueWarrant(player, args[0], reason);
        }
        return true;
    }

    private void issueWarrant(Player officer, String targetName, String reason) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            officer.sendMessage("§cPlayer not found.");
            return;
        }
        if (plugin.lawEnforcement().warrantManager().issue(
                officer.getUniqueId(), target.getUniqueId(), reason)) {
            officer.sendMessage("§aWarrant issued for §e" + target.getName()
                    + "§a. Reason: §7" + reason);
            target.sendMessage("§c§lA warrant has been issued for your property!");
        }
    }

    private void revokeWarrant(Player officer, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            officer.sendMessage("§cPlayer not found.");
            return;
        }
        if (plugin.lawEnforcement().warrantManager().revoke(target.getUniqueId())) {
            officer.sendMessage("§aWarrant revoked for §e" + target.getName() + "§a.");
        } else {
            officer.sendMessage("§cNo active warrant for that player.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("revoke"));
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
            return filter(options, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("revoke")) {
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
