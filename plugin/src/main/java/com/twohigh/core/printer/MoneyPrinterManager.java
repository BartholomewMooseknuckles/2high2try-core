package com.twohigh.core.printer;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.config.CoreConfig;
import com.twohigh.core.economy.CashManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MoneyPrinterManager {

    private static final NamespacedKey PRINTER_KEY = new NamespacedKey("twohigh", "money_printer");

    private final TwoHigh2TryCore plugin;
    private final ConcurrentHashMap<Location, MoneyPrinter> printers = new ConcurrentHashMap<>();
    private BukkitTask tickTask;

    public MoneyPrinterManager(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
        this.tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L * 60, 20L * 60);
    }

    public static ItemStack createPrinterItem() {
        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Money Printer", NamedTextColor.GREEN));
        meta.lore(List.of(
                Component.text("Place to start printing money", NamedTextColor.GRAY),
                Component.text("Right-click to collect earnings", NamedTextColor.DARK_GRAY)
        ));
        meta.getPersistentDataContainer().set(PRINTER_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isPrinterItem(ItemStack item) {
        if (item == null || item.getType() != Material.EMERALD_BLOCK) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(PRINTER_KEY, PersistentDataType.BYTE);
    }

    public boolean placePrinter(Player player, Location location) {
        if (printers.containsKey(location)) return false;

        MoneyPrinter printer = new MoneyPrinter(UUID.randomUUID(), player.getUniqueId(), location);
        printers.put(location, printer);
        player.sendMessage("§a[PRINTER] Money printer placed! Collect earnings by right-clicking.");
        return true;
    }

    public Optional<Double> collectPrinter(Player player, Location location) {
        MoneyPrinter printer = printers.get(location);
        if (printer == null) return Optional.empty();
        if (!printer.owner().equals(player.getUniqueId())) {
            player.sendMessage("§cThis isn't your printer.");
            return Optional.empty();
        }

        double collected = printer.collectAndReset();
        if (collected > 0) {
            plugin.cashManager().deposit(player.getUniqueId(), collected);
            player.sendMessage("§a[PRINTER] Collected $" + String.format("%.2f", collected) + "!");
        } else {
            player.sendMessage("§7[PRINTER] Nothing to collect yet.");
        }
        return Optional.of(collected);
    }

    public boolean removePrinter(Location location) {
        return printers.remove(location) != null;
    }

    public Optional<MoneyPrinter> getPrinter(Location location) {
        return Optional.ofNullable(printers.get(location));
    }

    private void tick() {
        double yieldPerTick = plugin.coreConfig().printerYieldPerHour() / 60.0;
        for (MoneyPrinter printer : printers.values()) {
            Block block = printer.location().getBlock();
            if (block.getType() != Material.EMERALD_BLOCK) {
                printers.remove(printer.location());
                continue;
            }
            printer.addAccumulated(yieldPerTick);
        }
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    public int activePrinters() {
        return printers.size();
    }
}
