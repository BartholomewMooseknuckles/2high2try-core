package com.twohigh.core.cheque;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class ChequeListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public ChequeListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!plugin.chequeManager().isCheque(item)) return;

        event.setCancelled(true);

        if (plugin.chequeManager().redeem(player, item)) {
            item.setAmount(item.getAmount() - 1);
        }
    }
}
