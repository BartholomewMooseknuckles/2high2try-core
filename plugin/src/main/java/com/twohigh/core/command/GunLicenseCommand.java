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

public final class GunLicenseCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;

    public GunLicenseCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            boolean has = plugin.lawEnforcement().licenseManager().hasLicense(player.getUniqueId());
            player.sendMessage(has ? "§aYou have a gun license." : "§7You do not have a gun license.");
            return true;
        }

        String team = plugin.jobRegistry().getPlayerTeam(player.getUniqueId());
        if (!"police".equals(team) && !"government".equals(team)) {
            player.sendMessage("§cOnly law enforcement can grant or revoke gun licenses.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /license grant <player> or /license revoke <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        if (args[0].equalsIgnoreCase("grant")) {
            if (plugin.lawEnforcement().licenseManager().grant(
                    player.getUniqueId(), target.getUniqueId())) {
                player.sendMessage("§aGun license granted to §e" + target.getName() + "§a.");
                target.sendMessage("§aYou have been granted a gun license!");
            } else {
                player.sendMessage("§cThat player already has a gun license.");
            }
        } else if (args[0].equalsIgnoreCase("revoke")) {
            if (plugin.lawEnforcement().licenseManager().revoke(
                    player.getUniqueId(), target.getUniqueId())) {
                player.sendMessage("§aGun license revoked from §e" + target.getName() + "§a.");
                target.sendMessage("§cYour gun license has been revoked!");
            } else {
                player.sendMessage("§cThat player doesn't have a gun license.");
            }
        } else {
            player.sendMessage("§cUsage: /license grant <player> or /license revoke <player>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("grant", "revoke"), args[0]);
        }
        if (args.length == 2) {
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
