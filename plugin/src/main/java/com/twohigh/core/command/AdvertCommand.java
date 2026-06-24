package com.twohigh.core.command;

import com.twohigh.api.claim.ClaimInfo;
import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.raid.ActiveRaid;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AdvertCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;

    public AdvertCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§6Usage: §e/advert <raid|defend>");
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "raid" -> { handleRaid(player); yield true; }
            case "defend" -> { handleDefend(player); yield true; }
            default -> {
                player.sendMessage("§6Usage: §e/advert <raid|defend>");
                yield true;
            }
        };
    }

    private void handleRaid(Player player) {
        Optional<ClaimInfo> claimOpt = plugin.claimManager().getClaimAt(player.getLocation());
        if (claimOpt.isEmpty()) {
            player.sendMessage("§cYou must be near a claimed base to start a raid.");
            return;
        }

        ClaimInfo claim = claimOpt.get();
        if (claim.owner().equals(player.getUniqueId())) {
            player.sendMessage("§cYou can't raid your own base.");
            return;
        }

        double cost = plugin.coreConfig().raidAdvertCost();
        if (!plugin.economyService().hasCash(player, cost)) {
            player.sendMessage("§cYou need $" + String.format("%.2f", cost) + " cash to start a raid.");
            return;
        }

        plugin.economyService().withdrawCash(player, cost);

        String result = plugin.raidManager().startRaid(player, claim.regionId(), player.getLocation());
        if (result == null) {
            plugin.economyService().depositCash(player, cost);
        }
    }

    private void handleDefend(Player player) {
        Optional<ActiveRaid> raidOpt = plugin.raidManager().getRaidAtLocation(player.getLocation());
        if (raidOpt.isEmpty()) {
            player.sendMessage("§cNo active raid near you.");
            return;
        }

        ActiveRaid raid = raidOpt.get();
        raid.addDefender(player.getUniqueId());
        player.sendMessage("§aYou are now defending this base!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("raid", "defend"), args[0]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
