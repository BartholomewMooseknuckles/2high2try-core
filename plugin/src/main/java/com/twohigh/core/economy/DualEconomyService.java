package com.twohigh.core.economy;

import com.twohigh.api.economy.EconomyApi;
import com.twohigh.api.event.BankTransactionEvent;
import com.twohigh.core.integration.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class DualEconomyService implements EconomyApi {

    private final CashManager cash;
    private final VaultHook vault;

    public DualEconomyService(CashManager cash, VaultHook vault) {
        this.cash = cash;
        this.vault = vault;
    }

    @Override
    public double getCash(OfflinePlayer player) {
        return cash.getCash(player.getUniqueId());
    }

    @Override
    public boolean depositCash(OfflinePlayer player, double amount) {
        return cash.deposit(player.getUniqueId(), amount);
    }

    @Override
    public boolean withdrawCash(OfflinePlayer player, double amount) {
        return cash.withdraw(player.getUniqueId(), amount);
    }

    @Override
    public boolean hasCash(OfflinePlayer player, double amount) {
        return cash.hasCash(player.getUniqueId(), amount);
    }

    @Override
    public double getBankBalance(OfflinePlayer player) {
        return vault.getBalance(player);
    }

    @Override
    public boolean depositBank(OfflinePlayer player, double amount) {
        return vault.deposit(player, amount);
    }

    @Override
    public boolean withdrawBank(OfflinePlayer player, double amount) {
        return vault.withdraw(player, amount);
    }

    @Override
    public boolean hasBankBalance(OfflinePlayer player, double amount) {
        return vault.has(player, amount);
    }

    @Override
    public boolean transferCashToBank(OfflinePlayer player, double amount) {
        if (!cash.withdraw(player.getUniqueId(), amount)) return false;
        if (!vault.deposit(player, amount)) {
            cash.deposit(player.getUniqueId(), amount);
            return false;
        }
        Bukkit.getPluginManager().callEvent(
                new BankTransactionEvent(player.getPlayer(), BankTransactionEvent.Type.CASH_TO_BANK, amount));
        return true;
    }

    @Override
    public boolean transferBankToCash(OfflinePlayer player, double amount) {
        if (!vault.withdraw(player, amount)) return false;
        if (!cash.deposit(player.getUniqueId(), amount)) {
            vault.deposit(player, amount);
            return false;
        }
        Bukkit.getPluginManager().callEvent(
                new BankTransactionEvent(player.getPlayer(), BankTransactionEvent.Type.BANK_TO_CASH, amount));
        return true;
    }

    @Override
    public String format(double amount) {
        return vault.format(amount);
    }

    public double clearCash(java.util.UUID player) {
        return cash.clearCash(player);
    }
}
