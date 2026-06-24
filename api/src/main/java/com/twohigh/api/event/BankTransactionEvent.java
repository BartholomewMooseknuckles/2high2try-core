package com.twohigh.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BankTransactionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum Type { DEPOSIT, WITHDRAW, CASH_TO_BANK, BANK_TO_CASH }

    private final Player player;
    private final Type type;
    private final double amount;

    public BankTransactionEvent(Player player, Type type, double amount) {
        this.player = player;
        this.type = type;
        this.amount = amount;
    }

    public Player getPlayer() { return player; }
    public Type getType() { return type; }
    public double getAmount() { return amount; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
