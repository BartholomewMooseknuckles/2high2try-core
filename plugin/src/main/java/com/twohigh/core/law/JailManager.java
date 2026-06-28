package com.twohigh.core.law;

import com.twohigh.core.data.CoreStorage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public final class JailManager {

    private final CopyOnWriteArrayList<JailPosition> positions = new CopyOnWriteArrayList<>();
    private final CoreStorage storage;
    private final Logger logger;

    public JailManager(CoreStorage storage, Logger logger) {
        this.storage = storage;
        this.logger = logger;
    }

    public void addPosition(String name, Location loc) {
        positions.removeIf(p -> p.name().equals(name));
        positions.add(new JailPosition(name, loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw()));
        storage.saveJailPosition(name, loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw());
        logger.info("Jail position set: " + name);
    }

    public Location randomJailLocation() {
        if (positions.isEmpty()) return null;
        JailPosition pos = positions.get(ThreadLocalRandom.current().nextInt(positions.size()));
        World world = Bukkit.getWorld(pos.world());
        if (world == null) return null;
        return new Location(world, pos.x(), pos.y(), pos.z(), pos.yaw(), 0f);
    }

    public boolean hasPositions() {
        return !positions.isEmpty();
    }

    public List<String> positionNames() {
        List<String> names = new ArrayList<>();
        for (JailPosition p : positions) names.add(p.name());
        return names;
    }

    public record JailPosition(String name, String world, double x, double y, double z, float yaw) {}
}
