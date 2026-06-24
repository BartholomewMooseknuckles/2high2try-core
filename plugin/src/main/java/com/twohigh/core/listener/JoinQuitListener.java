package com.twohigh.core.listener;

import com.twohigh.core.TwoHigh2TryCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinQuitListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public JoinQuitListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        plugin.cashManager().loadPlayer(uuid);
        if (plugin.jobRegistry() != null) {
            plugin.jobRegistry().loadPlayer(uuid);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        plugin.cashManager().unloadPlayer(uuid);
        if (plugin.jobRegistry() != null) {
            plugin.jobRegistry().unloadPlayer(uuid);
        }
    }
}
