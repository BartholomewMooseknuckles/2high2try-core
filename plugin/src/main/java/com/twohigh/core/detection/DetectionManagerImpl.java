package com.twohigh.core.detection;

import com.twohigh.api.detection.DetectionApi;
import com.twohigh.api.detection.SignalHandle;
import com.twohigh.api.detection.SignalReading;
import com.twohigh.api.detection.SignalTier;
import com.twohigh.core.config.CoreConfig;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DetectionManagerImpl implements DetectionApi {

    private final ConcurrentHashMap<UUID, SignalSource> sources = new ConcurrentHashMap<>();
    private final CoreConfig config;

    public DetectionManagerImpl(CoreConfig config) {
        this.config = config;
    }

    @Override
    public SignalHandle registerSignalSource(Location location, double strength, String sourceType) {
        UUID id = UUID.randomUUID();
        sources.put(id, new SignalSource(id, location, strength, sourceType));
        return new SignalHandle(id);
    }

    @Override
    public void removeSignalSource(SignalHandle handle) {
        sources.remove(handle.id());
    }

    @Override
    public void updateSignalStrength(SignalHandle handle, double newStrength) {
        SignalSource source = sources.get(handle.id());
        if (source != null) {
            source.setStrength(newStrength);
        }
    }

    @Override
    public Optional<SignalReading> getStrongestSignal(Location from, double maxRange) {
        return getSignalsInRange(from, maxRange).stream()
                .max(Comparator.comparingDouble(SignalReading::effectiveStrength));
    }

    @Override
    public List<SignalReading> getSignalsInRange(Location from, double maxRange) {
        World world = from.getWorld();
        if (world == null) return List.of();

        double maxRangeSquared = maxRange * maxRange;
        double exponent = config.dogDistanceFalloffExponent();
        double mergeRadius = config.dogGrowSiteMergeRadius();
        double mergeRadiusSq = mergeRadius * mergeRadius;

        List<SignalSource> inRange = new ArrayList<>();
        for (SignalSource source : sources.values()) {
            if (!source.location().getWorld().equals(world)) continue;
            if (source.location().distanceSquared(from) <= maxRangeSquared) {
                inRange.add(source);
            }
        }

        List<MergedSignal> merged = mergeNearby(inRange, mergeRadiusSq);

        List<SignalReading> readings = new ArrayList<>();
        for (MergedSignal ms : merged) {
            double distance = Math.max(1.0, from.distance(ms.center));
            double effective = ms.totalStrength / Math.pow(distance, exponent);
            SignalTier tier = classifyTier(effective);
            readings.add(new SignalReading(ms.center, effective, tier));
        }
        return readings;
    }

    private List<MergedSignal> mergeNearby(List<SignalSource> sources, double mergeRadiusSq) {
        boolean[] consumed = new boolean[sources.size()];
        List<MergedSignal> result = new ArrayList<>();

        for (int i = 0; i < sources.size(); i++) {
            if (consumed[i]) continue;
            SignalSource base = sources.get(i);
            double totalStrength = base.strength();
            double cx = base.location().getX();
            double cy = base.location().getY();
            double cz = base.location().getZ();
            int count = 1;
            consumed[i] = true;

            for (int j = i + 1; j < sources.size(); j++) {
                if (consumed[j]) continue;
                SignalSource other = sources.get(j);
                if (base.location().distanceSquared(other.location()) <= mergeRadiusSq) {
                    totalStrength += other.strength();
                    cx += other.location().getX();
                    cy += other.location().getY();
                    cz += other.location().getZ();
                    count++;
                    consumed[j] = true;
                }
            }

            Location center = new Location(base.location().getWorld(),
                    cx / count, cy / count, cz / count);
            result.add(new MergedSignal(center, totalStrength));
        }
        return result;
    }

    private SignalTier classifyTier(double effective) {
        if (effective >= 10.0) return SignalTier.FERAL;
        if (effective >= 5.0) return SignalTier.ALERT;
        if (effective >= 2.0) return SignalTier.INTERESTED;
        return SignalTier.FAINT;
    }

    public int sourceCount() {
        return sources.size();
    }

    private record MergedSignal(Location center, double totalStrength) {}
}
