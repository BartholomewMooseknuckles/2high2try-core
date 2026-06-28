package com.twohigh.core.cheque;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ChequeCommand implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public ChequeCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /cheque <amount>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage("§cAmount must be positive.");
            return true;
        }

        double cash = plugin.cashManager().getCash(player.getUniqueId());
        if (cash < amount) {
            player.sendMessage("§cInsufficient cash. You have §a$" + String.format("%.2f", cash));
            return true;
        }

        plugin.cashManager().withdraw(player.getUniqueId(), amount);
        ItemStack cheque = plugin.chequeManager().createCheque(player.getUniqueId(), amount);
        player.getInventory().addItem(cheque);
        player.sendMessage("§aCheque created for §a$" + String.format("%.2f", amount) + "§a.");
        return true;
    }
}
