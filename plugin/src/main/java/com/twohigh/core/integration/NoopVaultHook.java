package com.twohigh.core.integration;

import org.bukkit.OfflinePlayer;

public final class NoopVaultHook implements VaultHook {

    @Override public boolean isAvailable() { return false; }
    @Override public boolean deposit(OfflinePlayer p, double a) { return false; }
    @Override public boolean withdraw(OfflinePlayer p, double a) { return false; }
    @Override public double getBalance(OfflinePlayer p) { return 0; }
    @Override public boolean has(OfflinePlayer p, double a) { return false; }
    @Override public String format(double a) { return String.format("$%.2f", a); }
    @Override public String providerName() { return "none"; }
}
