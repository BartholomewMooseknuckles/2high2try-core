package com.twohigh.core.listener;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.pvp.CombatTagManager;
import com.twohigh.core.pvp.PvPManager;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PvPListener implements Listener {

    private final PvPManager pvpManager;
    private final CombatTagManager combatTagManager;

    public PvPListener(TwoHigh2TryCore plugin) {
        this.pvpManager = plugin.pvpManager();
        this.combatTagManager = plugin.combatTagManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = resolvePlayerAttacker(event);
        if (attacker == null) return;
        if (!(event.getEntity() instanceof Player defender)) return;

        if (!pvpManager.canAttack(attacker, defender)) {
            event.setCancelled(true);
            attacker.sendMessage("§cPvP is disabled here.");
            return;
        }

        combatTagManager.tagPlayer(attacker);
        combatTagManager.tagPlayer(defender);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (combatTagManager.isInCombat(player)) {
            player.setHealth(0);
        }
        combatTagManager.removeTag(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        combatTagManager.removeTag(event.getEntity().getUniqueId());
    }

    private Player resolvePlayerAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) return p;
        if (event.getDamager() instanceof Projectile proj
                && proj.getShooter() instanceof Player p) return p;
        return null;
    }
}
