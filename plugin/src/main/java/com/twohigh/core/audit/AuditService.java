package com.twohigh.core.audit;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public final class AuditService {

    private final Logger logger;
    private final ConcurrentLinkedQueue<AuditEntry> recentEntries = new ConcurrentLinkedQueue<>();
    private static final int MAX_RECENT = 500;

    public AuditService(Logger logger) {
        this.logger = logger;
    }

    public void log(String action, UUID player, String detail) {
        AuditEntry entry = new AuditEntry(System.currentTimeMillis(), action, player, detail);
        recentEntries.add(entry);
        while (recentEntries.size() > MAX_RECENT) {
            recentEntries.poll();
        }
        logger.info("[AUDIT] " + action + " | " + player + " | " + detail);
    }

    public void logEconomy(UUID player, String type, double amount, String context) {
        log("ECONOMY", player, type + " $" + String.format("%.2f", amount) + " — " + context);
    }

    public void logRaid(UUID player, String regionId, String action) {
        log("RAID", player, action + " on " + regionId);
    }

    public void logMug(UUID mugger, UUID victim, double amount, String result) {
        log("MUG", mugger, "mugged " + victim + " for $"
                + String.format("%.2f", amount) + " — " + result);
    }

    public ConcurrentLinkedQueue<AuditEntry> recentEntries() {
        return recentEntries;
    }

    public record AuditEntry(long timestamp, String action, UUID player, String detail) {}
}
