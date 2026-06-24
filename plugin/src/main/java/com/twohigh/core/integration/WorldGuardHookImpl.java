package com.twohigh.core.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public final class WorldGuardHookImpl implements WorldGuardHook {

    private static final String REGION_PREFIX = "2h2t_claim_";

    private final Logger logger;

    private WorldGuardHookImpl(Logger logger) {
        this.logger = logger;
    }

    public static Optional<WorldGuardHook> tryCreate(Logger logger) {
        try {
            WorldGuard.getInstance();
            return Optional.of(new WorldGuardHookImpl(logger));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isInRegion(Location location, String regionId) {
        RegionManager rm = getRegionManager(location.getWorld());
        if (rm == null) return false;
        ProtectedRegion region = rm.getRegion(regionId);
        if (region == null) return false;
        return region.contains(BukkitAdapter.asBlockVector(location));
    }

    @Override
    public Optional<String> findCoreRegionAt(Location location) {
        RegionManager rm = getRegionManager(location.getWorld());
        if (rm == null) return Optional.empty();

        BlockVector3 bv = BukkitAdapter.asBlockVector(location);
        for (ProtectedRegion region : rm.getRegions().values()) {
            if (region.getId().startsWith(REGION_PREFIX) && region.contains(bv)) {
                return Optional.of(region.getId());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean createCoreRegion(String regionId, UUID owner, Location corner1, Location corner2) {
        World world = corner1.getWorld();
        RegionManager rm = getRegionManager(world);
        if (rm == null) return false;

        BlockVector3 min = BlockVector3.at(
                Math.min(corner1.getBlockX(), corner2.getBlockX()),
                Math.min(corner1.getBlockY(), corner2.getBlockY()),
                Math.min(corner1.getBlockZ(), corner2.getBlockZ()));
        BlockVector3 max = BlockVector3.at(
                Math.max(corner1.getBlockX(), corner2.getBlockX()),
                Math.max(corner1.getBlockY(), corner2.getBlockY()),
                Math.max(corner1.getBlockZ(), corner2.getBlockZ()));

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, min, max);
        region.getOwners().addPlayer(owner);
        rm.addRegion(region);

        try { rm.save(); } catch (Exception e) {
            logger.warning("Failed to save WorldGuard region: " + regionId);
        }
        return true;
    }

    @Override
    public boolean removeCoreRegion(String regionId, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;
        RegionManager rm = getRegionManager(world);
        if (rm == null) return false;

        rm.removeRegion(regionId);
        try { rm.save(); } catch (Exception e) {
            logger.warning("Failed to save WorldGuard after removing region: " + regionId);
        }
        return true;
    }

    @Override
    public boolean setFlag(String regionId, String worldName, String flag, String value) {
        return false;
    }

    private RegionManager getRegionManager(World world) {
        if (world == null) return null;
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
    }

    public static String regionPrefix() {
        return REGION_PREFIX;
    }
}
