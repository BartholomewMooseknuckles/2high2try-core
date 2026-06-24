package com.twohigh.api.detection;

import org.bukkit.Location;

import java.util.List;
import java.util.Optional;

public interface DetectionApi {

    SignalHandle registerSignalSource(Location location, double strength, String sourceType);

    void removeSignalSource(SignalHandle handle);

    void updateSignalStrength(SignalHandle handle, double newStrength);

    Optional<SignalReading> getStrongestSignal(Location from, double maxRange);

    List<SignalReading> getSignalsInRange(Location from, double maxRange);
}
