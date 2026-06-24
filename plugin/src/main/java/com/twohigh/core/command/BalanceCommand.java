package com.twohigh.core.command;

import com.twohigh.core.TwoHigh2TryCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BalanceCommand implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public BalanceCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        double cash = plugin.economyService().getCash(player);
        double bank = plugin.economyService().getBankBalance(player);
        String fmt = plugin.economyService().format(0).replace("0", "");

        player.sendMessage("");
        player.sendMessage("§6§l--- Your Balance ---");
        player.sendMessage("§e  Cash on hand: §a" + plugin.economyService().format(cash));
        player.sendMessage("§e  Bank balance: §a" + plugin.economyService().format(bank));
        player.sendMessage("§7  Cash is lootable on death. Bank is sacred.");
        player.sendMessage("");
        return true;
    }
}
