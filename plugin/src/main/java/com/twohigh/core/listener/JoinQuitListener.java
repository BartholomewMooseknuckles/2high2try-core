package com.twohigh.core.listener;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.defaults.DefaultJobs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class JoinQuitListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public JoinQuitListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.cashManager().loadPlayer(uuid);
        if (plugin.jobRegistry() != null) {
            plugin.jobRegistry().loadPlayer(uuid);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.jobRegistry().assignIfAbsent(uuid, DefaultJobs.CITIZEN);
            }, 5L);
        }
        if (plugin.sidebarManager() != null) {
            plugin.sidebarManager().autoEnable(uuid);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.cashManager().unloadPlayer(uuid);
        if (plugin.jobRegistry() != null) {
            plugin.jobRegistry().unloadPlayer(uuid);
        }
    }
}
