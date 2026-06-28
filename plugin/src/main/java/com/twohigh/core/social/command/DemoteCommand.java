package com.twohigh.core.social.command;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class DemoteCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;

    public DemoteCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /demote <player> or /demote yes|no");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "yes" -> {
                if (plugin.socialService().demoteManager().castDemoteVote(player.getUniqueId(), true)) {
                    player.sendMessage("§aVoted to §cdemote§a.");
                } else {
                    player.sendMessage("§cNo active demote vote or you already voted.");
                }
            }
            case "no" -> {
                if (plugin.socialService().demoteManager().castDemoteVote(player.getUniqueId(), false)) {
                    player.sendMessage("§aVoted to §akeep§a.");
                } else {
                    player.sendMessage("§cNo active demote vote or you already voted.");
                }
            }
            default -> {
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                if (plugin.socialService().demoteManager().startDemoteVote(
                        player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage("§aDemote vote started for §e" + target.getName() + "§a.");
                } else {
                    player.sendMessage("§cCannot start demote vote. (Already active, same team required, or can't demote yourself)");
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("yes", "no"));
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
            return filter(options, args[0]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
