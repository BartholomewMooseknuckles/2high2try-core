package com.twohigh.core.social.command;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class GroupChatCommand implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public GroupChatCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            plugin.socialService().groupChatManager().toggleGroupChat(player.getUniqueId());
            boolean on = plugin.socialService().groupChatManager()
                    .isGroupChatEnabled(player.getUniqueId());
            player.sendMessage(on
                    ? "§aGroup chat enabled. All messages go to your team."
                    : "§7Group chat disabled. Messages go to local chat.");
            return true;
        }

        String message = String.join(" ", args);
        plugin.socialService().groupChatManager().sendTeamMessage(player.getUniqueId(), message);
        return true;
    }
}
