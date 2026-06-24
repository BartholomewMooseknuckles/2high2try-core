package com.twohigh.api.economy;

import org.bukkit.OfflinePlayer;

public interface EconomyApi {

    double getCash(OfflinePlayer player);

    boolean depositCash(OfflinePlayer player, double amount);

    boolean withdrawCash(OfflinePlayer player, double amount);

    boolean hasCash(OfflinePlayer player, double amount);

    double getBankBalance(OfflinePlayer player);

    boolean depositBank(OfflinePlayer player, double amount);

    boolean withdrawBank(OfflinePlayer player, double amount);

    boolean hasBankBalance(OfflinePlayer player, double amount);

    boolean transferCashToBank(OfflinePlayer player, double amount);

    boolean transferBankToCash(OfflinePlayer player, double amount);

    String format(double amount);
}
