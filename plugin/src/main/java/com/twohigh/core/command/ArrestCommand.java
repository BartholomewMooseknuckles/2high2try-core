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

public final class ArrestCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;
    private final boolean unarrest;

    public ArrestCommand(TwoHigh2TryCore plugin, boolean unarrest) {
        this.plugin = plugin;
        this.unarrest = unarrest;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        String team = plugin.jobRegistry().getPlayerTeam(player.getUniqueId());
        if (!"police".equals(team)) {
            player.sendMessage("§cOnly police can " + (unarrest ? "un" : "") + "arrest players.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /" + (unarrest ? "unarrest" : "arrest") + " <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou can't " + (unarrest ? "un" : "") + "arrest yourself.");
            return true;
        }

        if (unarrest) {
            doUnarrest(player, target);
        } else {
            doArrest(player, target);
        }
        return true;
    }

    private void doArrest(Player officer, Player target) {
        if (!plugin.lawEnforcement().wantedManager().isWanted(target.getUniqueId())) {
            officer.sendMessage("§c" + target.getName() + " is not wanted.");
            return;
        }
        if (!plugin.lawEnforcement().arrestManager().jailManager.hasPositions()) {
            officer.sendMessage("§cNo jail positions set! Ask an admin to use §e/setjail§c.");
            return;
        }
        if (plugin.lawEnforcement().arrestManager().arrest(
                officer.getUniqueId(), target.getUniqueId())) {
            officer.sendMessage("§a" + target.getName() + " has been arrested!");
            Bukkit.broadcastMessage("§9§l[POLICE] §e" + target.getName()
                    + " §7has been arrested by §e" + officer.getName() + "§7.");
        } else {
            officer.sendMessage("§cFailed to arrest " + target.getName() + ".");
        }
    }

    private void doUnarrest(Player officer, Player target) {
        if (plugin.lawEnforcement().arrestManager().unarrest(
                officer.getUniqueId(), target.getUniqueId())) {
            officer.sendMessage("§a" + target.getName() + " has been released from jail.");
        } else {
            officer.sendMessage("§c" + target.getName() + " is not in jail.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).collect(Collectors.toList()), args[0]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
