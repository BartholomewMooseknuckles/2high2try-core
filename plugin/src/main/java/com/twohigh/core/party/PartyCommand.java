package com.twohigh.core.party;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PartyCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of(
            "create", "invite", "accept", "leave", "kick", "disband",
            "list", "ff", "role", "deposit", "withdraw", "bank");

    private final TwoHigh2TryCore plugin;

    public PartyCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlayers only.");
            return true;
        }
        PartyManager pm = plugin.partyManager();
        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> {
                if (pm.getPlayerParty(player.getUniqueId()).isPresent()) {
                    player.sendMessage("§cYou're already in a party. Leave first.");
                    return true;
                }
                Party party = pm.createParty(player.getUniqueId());
                if (party == null) {
                    player.sendMessage("§cCouldn't create party.");
                } else {
                    player.sendMessage("§6[PARTY] §7Party created! Invite players with §e/party invite <player>§7.");
                }
            }
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party invite <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage("§cYou can't invite yourself.");
                    return true;
                }
                if (pm.invite(player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage("§6[PARTY] §7Invited §e" + target.getName() + "§7. Expires in 60s.");
                    target.sendMessage("§6[PARTY] §e" + player.getName()
                            + " §7invited you to their party. §e/party accept §7to join.");
                } else {
                    player.sendMessage("§cCouldn't invite. Check: you have a party, they don't, "
                            + "party isn't full, and you have invite permissions.");
                }
            }
            case "accept" -> {
                if (!pm.hasPendingInvite(player.getUniqueId())) {
                    player.sendMessage("§cNo pending party invite.");
                    return true;
                }
                if (pm.acceptInvite(player.getUniqueId())) {
                    player.sendMessage("§6[PARTY] §7You joined the party!");
                    Optional<Party> party = pm.getPlayerParty(player.getUniqueId());
                    party.ifPresent(p -> pm.messageParty(p.id(),
                            "§6[PARTY] §e" + player.getName() + " §7joined the party."));
                } else {
                    player.sendMessage("§cCouldn't join — party may be full or disbanded.");
                }
            }
            case "leave" -> {
                Optional<Party> partyOpt = pm.getPlayerParty(player.getUniqueId());
                if (partyOpt.isEmpty()) {
                    player.sendMessage("§cYou're not in a party.");
                    return true;
                }
                UUID partyId = partyOpt.get().id();
                pm.leave(player.getUniqueId());
                player.sendMessage("§6[PARTY] §7You left the party.");
                pm.messageParty(partyId, "§6[PARTY] §e" + player.getName() + " §7left the party.");
            }
            case "kick" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party kick <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                if (pm.kick(player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage("§6[PARTY] §7Kicked §e" + target.getName() + "§7.");
                    target.sendMessage("§6[PARTY] §7You were kicked from the party.");
                    pm.getPlayerParty(player.getUniqueId()).ifPresent(p ->
                            pm.messageParty(p.id(), "§6[PARTY] §e" + target.getName() + " §7was kicked."));
                } else {
                    player.sendMessage("§cCouldn't kick. Check permissions and membership.");
                }
            }
            case "disband" -> {
                if (pm.disband(player.getUniqueId())) {
                    player.sendMessage("§6[PARTY] §7Party disbanded.");
                } else {
                    player.sendMessage("§cYou must be the party leader to disband.");
                }
            }
            case "list" -> {
                Optional<Party> partyOpt = pm.getPlayerParty(player.getUniqueId());
                if (partyOpt.isEmpty()) {
                    player.sendMessage("§cYou're not in a party.");
                    return true;
                }
                Party party = partyOpt.get();
                player.sendMessage("§6§l[PARTY] §7Members (" + party.size() + "/"
                        + plugin.coreConfig().partyMaxSize() + "):");
                for (UUID member : party.members()) {
                    PartyRole role = party.getRole(member);
                    String name = Bukkit.getOfflinePlayer(member).getName();
                    boolean online = Bukkit.getPlayer(member) != null;
                    player.sendMessage("  §7" + roleTag(role) + " §f" + (name != null ? name : member)
                            + (online ? " §a(online)" : " §8(offline)"));
                }
                player.sendMessage("  §7Bank: §a$" + String.format("%.2f", party.bankBalance()));
                player.sendMessage("  §7Friendly fire: " + (pm.isFriendlyFireEnabled(party.id())
                        ? "§cON" : "§aOFF"));
            }
            case "ff" -> {
                if (pm.toggleFriendlyFire(player.getUniqueId())) {
                    Optional<Party> partyOpt = pm.getPlayerParty(player.getUniqueId());
                    partyOpt.ifPresent(p -> {
                        boolean ff = pm.isFriendlyFireEnabled(p.id());
                        pm.messageParty(p.id(), "§6[PARTY] §7Friendly fire: "
                                + (ff ? "§cON" : "§aOFF"));
                    });
                } else {
                    player.sendMessage("§cCouldn't toggle FF. You must be the leader, "
                            + "and the server must allow party_choice mode.");
                }
            }
            case "role" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /party role <player> <leader|officer|member>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                PartyRole newRole;
                try {
                    newRole = PartyRole.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cValid roles: leader, officer, member");
                    return true;
                }
                if (pm.setRole(player.getUniqueId(), target.getUniqueId(), newRole)) {
                    player.sendMessage("§6[PARTY] §7Set §e" + target.getName()
                            + " §7to §f" + newRole.name().toLowerCase() + "§7.");
                    target.sendMessage("§6[PARTY] §7Your role was set to §f"
                            + newRole.name().toLowerCase() + "§7.");
                } else {
                    player.sendMessage("§cCouldn't set role. You must be the leader.");
                }
            }
            case "deposit" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party deposit <amount>");
                    return true;
                }
                double amount;
                try {
                    amount = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid amount.");
                    return true;
                }
                if (pm.depositToBank(player.getUniqueId(), amount)) {
                    player.sendMessage("§6[PARTY] §7Deposited §a$" + String.format("%.2f", amount)
                            + " §7into the party bank.");
                    pm.getPlayerParty(player.getUniqueId()).ifPresent(p ->
                            pm.messageParty(p.id(), "§6[PARTY] §e" + player.getName()
                                    + " §7deposited §a$" + String.format("%.2f", amount) + "§7."));
                } else {
                    player.sendMessage("§cCouldn't deposit. Check your cash and party membership.");
                }
            }
            case "withdraw" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party withdraw <amount>");
                    return true;
                }
                double amount;
                try {
                    amount = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid amount.");
                    return true;
                }
                if (pm.withdrawFromBank(player.getUniqueId(), amount)) {
                    player.sendMessage("§6[PARTY] §7Withdrew §a$" + String.format("%.2f", amount)
                            + " §7from the party bank.");
                    pm.getPlayerParty(player.getUniqueId()).ifPresent(p ->
                            pm.messageParty(p.id(), "§6[PARTY] §e" + player.getName()
                                    + " §7withdrew §a$" + String.format("%.2f", amount) + "§7."));
                } else {
                    player.sendMessage("§cCouldn't withdraw. Check permissions and balance.");
                }
            }
            case "bank" -> {
                Optional<Party> partyOpt = pm.getPlayerParty(player.getUniqueId());
                if (partyOpt.isEmpty()) {
                    player.sendMessage("§cYou're not in a party.");
                    return true;
                }
                player.sendMessage("§6[PARTY] §7Bank balance: §a$"
                        + String.format("%.2f", partyOpt.get().bankBalance()));
            }
            default -> showHelp(player);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if ("invite".equals(sub) || "kick".equals(sub) || "role".equals(sub)) {
                return null;
            }
        }
        if (args.length == 3 && "role".equals(args[0].toLowerCase())) {
            return List.of("leader", "officer", "member").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase())).toList();
        }
        return List.of();
    }

    private void showHelp(Player player) {
        player.sendMessage("§6§l[PARTY] §7Commands:");
        player.sendMessage("  §e/party create §7— Create a new party");
        player.sendMessage("  §e/party invite <player> §7— Invite a player");
        player.sendMessage("  §e/party accept §7— Accept a pending invite");
        player.sendMessage("  §e/party leave §7— Leave your party");
        player.sendMessage("  §e/party kick <player> §7— Kick a member");
        player.sendMessage("  §e/party disband §7— Disband the party");
        player.sendMessage("  §e/party list §7— Show members");
        player.sendMessage("  §e/party ff §7— Toggle friendly fire");
        player.sendMessage("  §e/party role <player> <role> §7— Set member role");
        player.sendMessage("  §e/party deposit <amount> §7— Deposit to party bank");
        player.sendMessage("  §e/party withdraw <amount> §7— Withdraw from party bank");
        player.sendMessage("  §e/party bank §7— Check party bank balance");
    }

    private String roleTag(PartyRole role) {
        return switch (role) {
            case LEADER -> "§6[Leader]";
            case OFFICER -> "§b[Officer]";
            case MEMBER -> "§7[Member]";
        };
    }
}
