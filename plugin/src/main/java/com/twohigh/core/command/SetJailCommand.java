package com.twohigh.core.command;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SetJailCommand implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public SetJailCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("twohigh.admin")) {
            player.sendMessage("§cYou need admin permissions to set jail positions.");
            return true;
        }

        String name = args.length > 0 ? args[0] : "jail";

        plugin.lawEnforcement().arrestManager().jailManager.addPosition(name, player.getLocation());
        player.sendMessage("§aJail position §e'" + name + "'§a set at your location.");
        return true;
    }
}
