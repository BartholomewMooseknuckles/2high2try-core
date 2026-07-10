package com.twohigh.core.command;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.raid.ActiveRaid;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class RaidAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("end", "endall", "clearcooldown");

    private final TwoHigh2TryCore plugin;

    public RaidAdminCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /raidadmin <end|endall|clearcooldown> [args]");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "end" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cUse /raidadmin endall from console, or specify a region.");
                    return true;
                }
                Optional<ActiveRaid> raidOpt = plugin.raidManager().getRaidAtLocation(player.getLocation());
                if (raidOpt.isEmpty()) {
                    sender.sendMessage("§cNo active raid at your location.");
                    return true;
                }
                plugin.raidManager().endRaid(raidOpt.get().claimRegionId());
                sender.sendMessage("§a[RAID] Force-ended the raid at your location.");
            }
            case "endall" -> {
                plugin.raidManager().shutdown();
                sender.sendMessage("§a[RAID] Force-ended all active raids.");
            }
            case "clearcooldown" -> {
                plugin.raidManager().cooldowns().clearAll();
                sender.sendMessage("§a[RAID] Cleared all raid cooldowns (base + raider).");
            }
            default -> sender.sendMessage("§cUsage: /raidadmin <end|endall|clearcooldown>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
