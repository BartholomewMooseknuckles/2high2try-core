package com.twohigh.core.listener;

import com.twohigh.api.event.CashDropEvent;
import com.twohigh.core.TwoHigh2TryCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class DeathListener implements Listener {

    private final TwoHigh2TryCore plugin;
    private final NamespacedKey CASH_KEY;

    public DeathListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
        this.CASH_KEY = new NamespacedKey(plugin, "cash_drop");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        double cash = plugin.economyService().getCash(player);
        if (cash <= 0) return;

        CashDropEvent dropEvent = new CashDropEvent(player, cash);
        Bukkit.getPluginManager().callEvent(dropEvent);
        if (dropEvent.isCancelled()) return;

        double dropAmount = dropEvent.getAmount();
        plugin.economyService().clearCash(player.getUniqueId());

        ItemStack cashItem = new ItemStack(Material.EMERALD);
        ItemMeta meta = cashItem.getItemMeta();
        meta.displayName(net.kyori.adventure.text.Component.text(
                "§a§l" + plugin.economyService().format(dropAmount) + " Cash"));
        meta.lore(List.of(
                net.kyori.adventure.text.Component.text("§7Dropped by " + player.getName()),
                net.kyori.adventure.text.Component.text("§7Pick up to collect")
        ));
        meta.getPersistentDataContainer().set(CASH_KEY, PersistentDataType.DOUBLE, dropAmount);
        cashItem.setItemMeta(meta);

        player.getWorld().dropItemNaturally(player.getLocation(), cashItem);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Double cashAmount = pdc.get(CASH_KEY, PersistentDataType.DOUBLE);
        if (cashAmount == null) return;

        event.setCancelled(true);
        event.getItem().remove();

        Player player = event.getPlayer();
        plugin.economyService().depositCash(player, cashAmount);
        player.sendMessage("§aPicked up " + plugin.economyService().format(cashAmount) + " cash.");
    }
}
