package com.twohigh.core.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class CashTokenListener implements Listener {

    private final NamespacedKey cashKey;

    public CashTokenListener(JavaPlugin plugin) {
        this.cashKey = new NamespacedKey(plugin, "cash_drop");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() == InventoryType.PLAYER) return;
        if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (isCashToken(cursor) && event.getRawSlot() < event.getView().getTopInventory().getSize()) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof org.bukkit.entity.Player p) {
                p.sendMessage("§cCash tokens cannot be placed in containers.");
            }
            return;
        }

        if (event.isShiftClick() && isCashToken(current)
                && event.getRawSlot() >= event.getView().getTopInventory().getSize()
                && event.getView().getTopInventory().getType() != InventoryType.PLAYER) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof org.bukkit.entity.Player p) {
                p.sendMessage("§cCash tokens cannot be placed in containers.");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (isCashToken(event.getItem())) {
            event.setCancelled(true);
        }
    }

    private boolean isCashToken(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(cashKey, PersistentDataType.DOUBLE);
    }
}
