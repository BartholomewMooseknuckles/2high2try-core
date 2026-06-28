package com.twohigh.core.scoreboard;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ScoreboardCommand implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public ScoreboardCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        SidebarManager sidebar = plugin.sidebarManager();
        if (sidebar == null) return true;

        // Toggle
        if (args.length > 0 && args[0].equalsIgnoreCase("off")) {
            sidebar.hideSidebar(player.getUniqueId());
            player.sendMessage("§7Sidebar hidden.");
        } else {
            sidebar.showSidebar(player.getUniqueId());
            player.sendMessage("§aSidebar shown.");
        }
        return true;
    }
}
