package com.twohigh.core.listener;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.detection.DrugDogItem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DrugDogListener implements Listener {

    private final TwoHigh2TryCore plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public DrugDogListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!DrugDogItem.isDrugDog(event.getItem())) return;

        UUID uuid = event.getPlayer().getUniqueId();
        long now = System.currentTimeMillis();
        long treatInterval = plugin.coreConfig().dogTreatIntervalSeconds() * 1000L;

        Long last = cooldowns.get(uuid);
        if (last != null && now - last < treatInterval) {
            long remaining = (treatInterval - (now - last)) / 1000;
            event.getPlayer().sendMessage("§7The dog needs a break. " + remaining + "s cooldown.");
            return;
        }

        cooldowns.put(uuid, now);
        DrugDogItem.sniff(event.getPlayer(), plugin);
    }
}
