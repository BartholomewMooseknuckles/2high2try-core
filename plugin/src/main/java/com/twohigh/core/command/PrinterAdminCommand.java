package com.twohigh.core.command;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.printer.MoneyPrinterManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class PrinterAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("give", "list", "clearall");

    private final TwoHigh2TryCore plugin;

    public PrinterAdminCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /printeradmin <give|list|clearall> [player]");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "give" -> {
                Player target;
                if (args.length >= 2) {
                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage("§cPlayer not found.");
                        return true;
                    }
                } else if (sender instanceof Player p) {
                    target = p;
                } else {
                    sender.sendMessage("§cUsage: /printeradmin give <player>");
                    return true;
                }
                ItemStack printer = MoneyPrinterManager.createPrinterItem();
                target.getInventory().addItem(printer);
                sender.sendMessage("§a[PRINTER] Gave a money printer to §f" + target.getName() + "§a.");
                if (!target.equals(sender)) {
                    target.sendMessage("§a[PRINTER] You received a money printer from an admin.");
                }
            }
            case "list" -> {
                int count = plugin.printerManager().activePrinters();
                sender.sendMessage("§a[PRINTER] Active printers: §f" + count);
            }
            case "clearall" -> {
                sender.sendMessage("§c[PRINTER] This would remove all printers. "
                        + "Use /printeradmin clearall confirm to proceed.");
                if (args.length >= 2 && "confirm".equalsIgnoreCase(args[1])) {
                    plugin.printerManager().shutdown();
                    sender.sendMessage("§a[PRINTER] All printers cleared and saved.");
                }
            }
            default -> sender.sendMessage("§cUsage: /printeradmin <give|list|clearall>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && "give".equals(args[0].toLowerCase())) {
            return null;
        }
        return List.of();
    }
}
