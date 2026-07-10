package com.twohigh.core.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.function.Consumer;

public final class F4MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof F4MenuHolder holder)) return;

        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getView().getTopInventory().getSize()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Consumer<Player> action = holder.action(rawSlot);
        if (action != null) {
            action.accept(player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof F4MenuHolder) {
            event.setCancelled(true);
        }
    }
}
