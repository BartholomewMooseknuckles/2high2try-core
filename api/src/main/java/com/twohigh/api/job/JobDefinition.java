package com.twohigh.api.job;

import org.bukkit.plugin.Plugin;

public record JobDefinition(
        String id,
        String displayName,
        boolean legal,
        double salary,
        long salaryIntervalMs,
        String permission,
        Plugin owningPlugin
) {}
