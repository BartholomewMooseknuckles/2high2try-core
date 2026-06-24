package com.twohigh.core.detection;

import org.bukkit.Location;

import java.util.UUID;

public final class SignalSource {

    private final UUID id;
    private final Location location;
    private volatile double strength;
    private final String sourceType;

    public SignalSource(UUID id, Location location, double strength, String sourceType) {
        this.id = id;
        this.location = location;
        this.strength = strength;
        this.sourceType = sourceType;
    }

    public UUID id() { return id; }
    public Location location() { return location; }
    public double strength() { return strength; }
    public void setStrength(double strength) { this.strength = strength; }
    public String sourceType() { return sourceType; }
}
