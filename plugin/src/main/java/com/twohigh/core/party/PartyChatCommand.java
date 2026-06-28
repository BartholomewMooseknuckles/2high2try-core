package com.twohigh.core.party;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class PartyChatCommand implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public PartyChatCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlayers only.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§cUsage: /p <message>");
            return true;
        }

        PartyManager pm = plugin.partyManager();
        Optional<Party> partyOpt = pm.getPlayerParty(player.getUniqueId());
        if (partyOpt.isEmpty()) {
            player.sendMessage("§cYou're not in a party.");
            return true;
        }

        String message = String.join(" ", args);
        pm.messageParty(partyOpt.get().id(),
                "§d[Party] §f" + player.getName() + "§7: §f" + message);
        return true;
    }
}
