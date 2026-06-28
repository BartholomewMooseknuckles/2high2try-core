package com.twohigh.api.cheque;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface ChequeApi {

    ItemStack createCheque(UUID writer, double amount);

    double getChequeAmount(ItemStack item);

    boolean isCheque(ItemStack item);

    boolean redeem(Player player, ItemStack item);
}
