package com.twohigh.core.command;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class EcoAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("give", "take", "set", "reset");

    private final TwoHigh2TryCore plugin;

    public EcoAdminCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /eco <give|take|set|reset> <player> [amount]");
            return true;
        }

        String sub = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or not online.");
            return true;
        }
        UUID uuid = target.getUniqueId();

        switch (sub) {
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /eco give <player> <amount>");
                    return true;
                }
                double amount = parseAmount(sender, args[2]);
                if (amount < 0) return true;
                plugin.cashManager().deposit(uuid, amount);
                sender.sendMessage("§a[ECO] Gave §f" + target.getName() + " §a$"
                        + String.format("%.2f", amount) + "§a. New balance: §f$"
                        + String.format("%.2f", plugin.cashManager().getCash(uuid)));
                target.sendMessage("§a[ECO] You received §f$" + String.format("%.2f", amount)
                        + " §afrom an admin.");
            }
            case "take" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /eco take <player> <amount>");
                    return true;
                }
                double amount = parseAmount(sender, args[2]);
                if (amount < 0) return true;
                double current = plugin.cashManager().getCash(uuid);
                double toTake = Math.min(amount, current);
                if (toTake > 0) {
                    plugin.cashManager().withdraw(uuid, toTake);
                }
                sender.sendMessage("§a[ECO] Took §f$" + String.format("%.2f", toTake)
                        + " §afrom §f" + target.getName() + "§a. New balance: §f$"
                        + String.format("%.2f", plugin.cashManager().getCash(uuid)));
                target.sendMessage("§c[ECO] An admin took §f$" + String.format("%.2f", toTake)
                        + " §cfrom your balance.");
            }
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /eco set <player> <amount>");
                    return true;
                }
                double amount = parseAmount(sender, args[2]);
                if (amount < 0) return true;
                plugin.cashManager().clearCash(uuid);
                if (amount > 0) {
                    plugin.cashManager().deposit(uuid, amount);
                }
                sender.sendMessage("§a[ECO] Set §f" + target.getName() + "§a's cash to §f$"
                        + String.format("%.2f", amount));
                target.sendMessage("§e[ECO] An admin set your cash to §f$"
                        + String.format("%.2f", amount));
            }
            case "reset" -> {
                double had = plugin.cashManager().clearCash(uuid);
                sender.sendMessage("§a[ECO] Reset §f" + target.getName() + "§a's cash to §f$0.00 §a(had §f$"
                        + String.format("%.2f", had) + "§a).");
                target.sendMessage("§c[ECO] An admin reset your cash to $0.00.");
            }
            default -> sender.sendMessage("§cUsage: /eco <give|take|set|reset> <player> [amount]");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) return null;
        return List.of();
    }

    private double parseAmount(CommandSender sender, String str) {
        try {
            double val = Double.parseDouble(str);
            if (val < 0) {
                sender.sendMessage("§cAmount must be positive.");
                return -1;
            }
            return val;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: " + str);
            return -1;
        }
    }
}
