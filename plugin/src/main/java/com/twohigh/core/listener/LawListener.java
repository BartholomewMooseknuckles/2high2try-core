package com.twohigh.core.listener;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.law.ArrestManager;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

public final class LawListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public LawListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;

        UUID uuid = event.getPlayer().getUniqueId();
        ArrestManager arrests = plugin.lawEnforcement().arrestManager();
        if (!arrests.isJailed(uuid)) return;

        Location jail = arrests.jailManager.randomJailLocation();
        if (jail == null) return;

        Location to = event.getTo();
        if (to.distanceSquared(jail) > 100) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou are in jail! "
                    + (arrests.getRemainingJailTimeMs(uuid) / 1000) + "s remaining.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        ArrestManager arrests = plugin.lawEnforcement().arrestManager();
        if (!arrests.isJailed(uuid)) return;

        Location jail = arrests.jailManager.randomJailLocation();
        if (jail != null) {
            event.setRespawnLocation(jail);
        }
    }
}
