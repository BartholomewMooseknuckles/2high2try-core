package com.twohigh.core.command;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.party.Party;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PartyAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("disband", "info");

    private final TwoHigh2TryCore plugin;

    public PartyAdminCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /partyadmin <disband|info> <player>");
            return true;
        }

        String sub = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or not online.");
            return true;
        }

        switch (sub) {
            case "disband" -> {
                Optional<Party> partyOpt = plugin.partyManager().getPlayerParty(target.getUniqueId());
                if (partyOpt.isEmpty()) {
                    sender.sendMessage("§c" + target.getName() + " is not in a party.");
                    return true;
                }
                Party party = partyOpt.get();
                UUID leaderId = party.leader();
                if (leaderId != null) {
                    plugin.partyManager().disband(leaderId);
                }
                sender.sendMessage("§a[PARTY] Force-disbanded §f" + target.getName() + "§a's party.");
            }
            case "info" -> {
                Optional<Party> partyOpt = plugin.partyManager().getPlayerParty(target.getUniqueId());
                if (partyOpt.isEmpty()) {
                    sender.sendMessage("§c" + target.getName() + " is not in a party.");
                    return true;
                }
                Party party = partyOpt.get();
                sender.sendMessage("§6[PARTY INFO] §f" + target.getName() + "§7's party:");
                sender.sendMessage("  §7ID: §f" + party.id());
                sender.sendMessage("  §7Size: §f" + party.size());
                sender.sendMessage("  §7Bank: §a$" + String.format("%.2f", party.bankBalance()));
                sender.sendMessage("  §7FF: " + (plugin.partyManager().isFriendlyFireEnabled(party.id())
                        ? "§cON" : "§aOFF"));
                sender.sendMessage("  §7Members:");
                for (UUID member : party.members()) {
                    String name = Bukkit.getOfflinePlayer(member).getName();
                    String role = party.getRole(member).name().toLowerCase();
                    boolean online = Bukkit.getPlayer(member) != null;
                    sender.sendMessage("    §7" + role + ": §f" + (name != null ? name : member)
                            + (online ? " §a(online)" : " §8(offline)"));
                }
            }
            default -> sender.sendMessage("§cUsage: /partyadmin <disband|info> <player>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) return null;
        return List.of();
    }
}
