package com.twohigh.api.entity;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public record EntityDefinition(
        String id,
        String displayName,
        Material blockMaterial,
        double price,
        int maxPerPlayer,
        Set<String> allowedJobs,
        Plugin owningPlugin,
        double signalStrength
) {
    public boolean isJobRestricted() {
        return allowedJobs != null && !allowedJobs.isEmpty();
    }
}
