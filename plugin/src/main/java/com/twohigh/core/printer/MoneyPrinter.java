package com.twohigh.core.printer;

import org.bukkit.Location;

import java.util.UUID;

public final class MoneyPrinter {

    private final UUID id;
    private final UUID owner;
    private final Location location;
    private volatile double accumulated;
    private final long placedAt;

    public MoneyPrinter(UUID id, UUID owner, Location location) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.accumulated = 0;
        this.placedAt = System.currentTimeMillis();
    }

    public UUID id() { return id; }
    public UUID owner() { return owner; }
    public Location location() { return location; }
    public double accumulated() { return accumulated; }
    public void addAccumulated(double amount) { this.accumulated += amount; }
    public double collectAndReset() {
        double amt = accumulated;
        accumulated = 0;
        return amt;
    }
    public long placedAt() { return placedAt; }
}
