package com.twohigh.core.raid;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public final class RaidAccessListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public RaidAccessListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) return;
        Location loc = event.getBlock().getLocation();
        Optional<ActiveRaid> raidOpt = plugin.raidManager().getActiveRaidAtLocation(loc);
        if (raidOpt.isEmpty()) return;

        ActiveRaid raid = raidOpt.get();
        if (raid.isInvolved(event.getPlayer().getUniqueId())) {
            event.setCancelled(false);
            if (raid.isAttacker(event.getPlayer().getUniqueId())) {
                raid.incrementBlocksBroken();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled()) return;
        Location loc = event.getBlock().getLocation();
        Optional<ActiveRaid> raidOpt = plugin.raidManager().getActiveRaidAtLocation(loc);
        if (raidOpt.isEmpty()) return;

        ActiveRaid raid = raidOpt.get();
        if (raid.isInvolved(event.getPlayer().getUniqueId())) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.useInteractedBlock().equals(org.bukkit.event.Event.Result.DENY)) return;
        if (event.getClickedBlock() == null) return;

        Location loc = event.getClickedBlock().getLocation();
        Optional<ActiveRaid> raidOpt = plugin.raidManager().getActiveRaidAtLocation(loc);
        if (raidOpt.isEmpty()) return;

        ActiveRaid raid = raidOpt.get();
        if (raid.isInvolved(event.getPlayer().getUniqueId())) {
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.ALLOW);
        }
    }
}
