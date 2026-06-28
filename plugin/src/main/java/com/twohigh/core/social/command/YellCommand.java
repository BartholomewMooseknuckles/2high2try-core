package com.twohigh.core.social.command;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class YellCommand implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public YellCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /yell <message>");
            return true;
        }

        String message = String.join(" ", args);
        plugin.socialService().chatManager().sendYellMessage(player.getUniqueId(), message);
        return true;
    }
}
