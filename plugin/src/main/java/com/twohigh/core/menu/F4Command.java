package com.twohigh.core.menu;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class F4Command implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public F4Command(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlayers only.");
            return true;
        }
        plugin.f4MenuManager().open(player);
        return true;
    }
}
