package com.twohigh.core.integration;

import org.bukkit.OfflinePlayer;

public interface VaultHook {

    boolean isAvailable();

    boolean deposit(OfflinePlayer player, double amount);

    boolean withdraw(OfflinePlayer player, double amount);

    double getBalance(OfflinePlayer player);

    boolean has(OfflinePlayer player, double amount);

    String format(double amount);

    String providerName();
}
