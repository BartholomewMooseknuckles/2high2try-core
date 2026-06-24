package com.twohigh.core.integration;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;
import java.util.logging.Logger;

public final class VaultHookImpl implements VaultHook {

    private final Economy economy;

    private VaultHookImpl(Economy economy) {
        this.economy = economy;
    }

    public static Optional<VaultHook> tryCreate(Server server, Logger logger) {
        RegisteredServiceProvider<Economy> rsp =
                server.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return Optional.empty();
        Economy eco = rsp.getProvider();
        logger.info("Hooked Vault economy (provider: " + eco.getName() + ").");
        return Optional.of(new VaultHookImpl(eco));
    }

    @Override public boolean isAvailable() { return true; }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean withdraw(OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return economy.has(player, amount);
    }

    @Override
    public String format(double amount) {
        return economy.format(amount);
    }

    @Override
    public String providerName() {
        return economy.getName();
    }
}
