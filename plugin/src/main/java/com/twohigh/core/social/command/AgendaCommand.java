package com.twohigh.core.social.command;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class AgendaCommand implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public AgendaCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        String team = plugin.jobRegistry().getPlayerTeam(player.getUniqueId());

        if (args.length == 0) {
            Optional<String> agenda = plugin.socialService().agendaManager().getAgenda(team);
            if (agenda.isPresent()) {
                player.sendMessage("§6§lTeam Agenda §8(" + team + ")");
                player.sendMessage("§7" + agenda.get());
            } else {
                player.sendMessage("§7No agenda set for your team.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /agenda set <message>");
                return true;
            }
            String text = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            plugin.socialService().agendaManager().setAgenda(player.getUniqueId(), team, text);
            player.sendMessage("§aAgenda set for team §e" + team + "§a.");
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            plugin.socialService().agendaManager().clearAgenda(team);
            player.sendMessage("§7Agenda cleared.");
            return true;
        }

        player.sendMessage("§cUsage: /agenda [set <message>|clear]");
        return true;
    }
}
