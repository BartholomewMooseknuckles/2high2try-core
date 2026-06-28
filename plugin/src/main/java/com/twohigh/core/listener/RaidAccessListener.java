package com.twohigh.core.listener;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.raid.ActiveRaid;

import org.bukkit.block.Container;
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) return;
        if (!plugin.claimManager().isRaidActive(event.getBlock().getLocation())) return;

        Optional<ActiveRaid> raidOpt = plugin.raidManager()
                .getActiveRaidAtLocation(event.getBlock().getLocation());
        if (raidOpt.isEmpty()) return;

        ActiveRaid raid = raidOpt.get();
        if (raid.isInvolved(event.getPlayer().getUniqueId())) {
            event.setCancelled(false);
            raid.incrementBlocksBroken();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled()) return;
        if (!plugin.claimManager().isRaidActive(event.getBlock().getLocation())) return;

        Optional<ActiveRaid> raidOpt = plugin.raidManager()
                .getActiveRaidAtLocation(event.getBlock().getLocation());
        if (raidOpt.isEmpty()) return;

        if (raidOpt.get().isInvolved(event.getPlayer().getUniqueId())) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.isCancelled()) return;
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Container)) return;
        if (!plugin.claimManager().isRaidActive(event.getClickedBlock().getLocation())) return;

        Optional<ActiveRaid> raidOpt = plugin.raidManager()
                .getActiveRaidAtLocation(event.getClickedBlock().getLocation());
        if (raidOpt.isEmpty()) return;

        if (raidOpt.get().isInvolved(event.getPlayer().getUniqueId())) {
            event.setCancelled(false);
        }
    }
}
