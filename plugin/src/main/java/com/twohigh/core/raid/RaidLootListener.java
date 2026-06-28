package com.twohigh.core.raid;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class RaidLootListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public RaidLootListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getType() == InventoryType.PLAYER) return;

        Location containerLoc = top.getLocation();
        if (containerLoc == null) return;

        Optional<ActiveRaid> raidOpt = plugin.raidManager().getActiveRaidAtLocation(containerLoc);
        if (raidOpt.isEmpty()) return;
        ActiveRaid raid = raidOpt.get();

        boolean takingFromContainer = event.getRawSlot() < top.getSize()
                && event.getCurrentItem() != null
                && event.getCurrentItem().getType() != org.bukkit.Material.AIR;

        if (takingFromContainer) {
            raid.markLegit();
        }
    }
}
