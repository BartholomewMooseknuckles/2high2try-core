package com.twohigh.core.cheque;

import com.twohigh.api.cheque.ChequeApi;
import com.twohigh.core.economy.CashManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public final class ChequeManager implements ChequeApi {

    private final NamespacedKey chequeKey;
    private final CashManager cashManager;

    public ChequeManager(JavaPlugin plugin, CashManager cashManager) {
        this.chequeKey = new NamespacedKey(plugin, "cheque_amount");
        this.cashManager = cashManager;
    }

    @Override
    public ItemStack createCheque(UUID writer, double amount) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lCheque §7- §a$" + String.format("%.2f", amount));
        Player p = Bukkit.getPlayer(writer);
        String name = p != null ? p.getName() : "Unknown";
        meta.setLore(List.of(
                "§7Written by: §f" + name,
                "§7Amount: §a$" + String.format("%.2f", amount),
                "",
                "§eRight-click to redeem"
        ));
        meta.getPersistentDataContainer().set(chequeKey, PersistentDataType.DOUBLE, amount);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public double getChequeAmount(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Double amount = item.getItemMeta().getPersistentDataContainer()
                .get(chequeKey, PersistentDataType.DOUBLE);
        return amount != null ? amount : 0;
    }

    @Override
    public boolean isCheque(ItemStack item) {
        return getChequeAmount(item) > 0;
    }

    @Override
    public boolean redeem(Player player, ItemStack item) {
        double amount = getChequeAmount(item);
        if (amount <= 0) return false;

        cashManager.deposit(player.getUniqueId(), amount);
        player.sendMessage("§aCheque redeemed! §a$" + String.format("%.2f", amount)
                + " §aadded to your cash.");
        return true;
    }

    public NamespacedKey key() {
        return chequeKey;
    }
}
