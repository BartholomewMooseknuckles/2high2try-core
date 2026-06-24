package com.twohigh.api.detection;

import org.bukkit.Location;

public record SignalReading(
        Location direction,
        double effectiveStrength,
        SignalTier tier
) {}
