package com.twohigh.core.entity;

import com.twohigh.api.entity.EntityDefinition;
import com.twohigh.api.entity.PlacedEntity;
import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;

public final class EntityListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public EntityListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<PlacedEntity> placed = plugin.entityRegistry().getAt(block.getLocation());
        if (placed.isEmpty()) return;

        PlacedEntity entity = placed.get();
        Player player = event.getPlayer();

        if (!entity.owner().equals(player.getUniqueId()) && !player.hasPermission("twohigh.admin")) {
            event.setCancelled(true);
            player.sendMessage("§cYou don't own this entity.");
            return;
        }

        event.setCancelled(true);
        plugin.entityRegistry().remove(block.getLocation(), player.getUniqueId());

        Optional<EntityDefinition> def = plugin.entityRegistry().getDefinition(entity.entityId());
        String name = def.map(EntityDefinition::displayName).orElse(entity.entityId());
        player.sendMessage("§7Removed §e" + name + "§7.");
    }
}
