package com.twohigh.core.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Marks an inventory as an F4 menu and maps slots to click actions.
 * The listener identifies our GUIs by this holder — never by title.
 */
public final class F4MenuHolder implements InventoryHolder {

    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();
    private Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setAction(int slot, Consumer<Player> action) {
        actions.put(slot, action);
    }

    public Consumer<Player> action(int slot) {
        return actions.get(slot);
    }
}
