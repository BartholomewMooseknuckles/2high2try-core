package com.twohigh.api.job;

import org.bukkit.plugin.Plugin;

public record JobDefinition(
        String id,
        String displayName,
        boolean legal,
        double salary,
        long salaryIntervalMs,
        String permission,
        Plugin owningPlugin,
        int maxSlots,
        String team,
        boolean voteRequired,
        String prerequisiteJobId,
        String demoteGroup,
        String chatColor
) {

    public JobDefinition(String id, String displayName, boolean legal,
                         double salary, long salaryIntervalMs,
                         String permission, Plugin owningPlugin) {
        this(id, displayName, legal, salary, salaryIntervalMs, permission,
                owningPlugin, -1, "civilian", false, null, null, null);
    }

    public boolean hasSlotLimit() {
        return maxSlots > 0;
    }
}
