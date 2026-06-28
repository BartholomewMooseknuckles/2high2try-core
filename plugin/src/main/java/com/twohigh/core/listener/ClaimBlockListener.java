package com.twohigh.core.listener;

import com.twohigh.api.claim.ClaimInfo;
import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;

public final class ClaimBlockListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public ClaimBlockListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Optional<ClaimInfo> claim = plugin.claimManager().getClaimAt(event.getBlock().getLocation());
        if (claim.isPresent()) {
            plugin.claimManager().incrementBlockCount(claim.get().regionId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Optional<ClaimInfo> claim = plugin.claimManager().getClaimAt(event.getBlock().getLocation());
        if (claim.isPresent()) {
            plugin.claimManager().decrementBlockCount(claim.get().regionId());
        }
    }
}
