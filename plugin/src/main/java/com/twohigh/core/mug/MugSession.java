package com.twohigh.core.mug;

import java.util.UUID;

public final class MugSession {

    private final UUID mugger;
    private final UUID victim;
    private final double amount;
    private final long startTime;
    private volatile boolean resolved;

    public MugSession(UUID mugger, UUID victim, double amount) {
        this.mugger = mugger;
        this.victim = victim;
        this.amount = amount;
        this.startTime = System.currentTimeMillis();
        this.resolved = false;
    }

    public UUID mugger() { return mugger; }
    public UUID victim() { return victim; }
    public double amount() { return amount; }
    public long startTime() { return startTime; }
    public boolean resolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public boolean isExpired(long timeoutMs) {
        return System.currentTimeMillis() - startTime > timeoutMs;
    }
}
