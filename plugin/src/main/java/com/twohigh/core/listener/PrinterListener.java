package com.twohigh.core.listener;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.printer.MoneyPrinterManager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

public final class PrinterListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public PrinterListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!MoneyPrinterManager.isPrinterItem(event.getItemInHand())) return;
        plugin.printerManager().placePrinter(event.getPlayer(), event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.EMERALD_BLOCK) return;
        plugin.printerManager().removePrinter(block.getLocation());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.EMERALD_BLOCK) return;

        if (plugin.printerManager().getPrinter(block.getLocation()).isPresent()) {
            plugin.printerManager().collectPrinter(event.getPlayer(), block.getLocation());
            event.setCancelled(true);
        }
    }
}
