package com.twohigh.core.command;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.mug.MugSession;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class PayCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;

    public PayCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /pay <player> <amount>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage("§cYou can't pay yourself.");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount.");
            return true;
        }
        if (amount <= 0) {
            player.sendMessage("§cAmount must be positive.");
            return true;
        }

        Optional<MugSession> mugOpt = plugin.mugManager().getSessionByVictim(player.getUniqueId());
        if (mugOpt.isPresent()) {
            MugSession session = mugOpt.get();
            if (session.mugger().equals(target.getUniqueId()) && amount >= session.amount()) {
                plugin.mugManager().resolveMug(session.mugger(), true);
                return true;
            }
        }

        if (!plugin.economyService().hasCash(player, amount)) {
            if (!plugin.economyService().hasBankBalance(player, amount)) {
                player.sendMessage("§cYou don't have enough money.");
                return true;
            }
            if (!plugin.economyService().withdrawBank(player, amount)) {
                player.sendMessage("§cBank withdrawal failed.");
                return true;
            }
        } else {
            if (!plugin.economyService().withdrawCash(player, amount)) {
                player.sendMessage("§cPayment failed.");
                return true;
            }
        }
        plugin.economyService().depositCash(target, amount);
        String formatted = plugin.economyService().format(amount);
        player.sendMessage("§aPaid " + formatted + " to " + target.getName() + ".");
        target.sendMessage("§aReceived " + formatted + " from " + player.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return null;
        }
        return Collections.emptyList();
    }
}
