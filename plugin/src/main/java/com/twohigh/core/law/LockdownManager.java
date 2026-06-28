package com.twohigh.core.law;

import com.twohigh.api.law.LockdownApi;
import com.twohigh.api.law.event.LockdownEvent;
import com.twohigh.core.config.CoreConfig;
import com.twohigh.core.job.JobRegistryImpl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public final class LockdownManager implements LockdownApi {

    private final CoreConfig config;
    private final JobRegistryImpl jobRegistry;
    private final JavaPlugin plugin;
    private volatile boolean active;
    private BukkitTask effectTask;

    public LockdownManager(CoreConfig config, JobRegistryImpl jobRegistry, JavaPlugin plugin) {
        this.config = config;
        this.jobRegistry = jobRegistry;
        this.plugin = plugin;
    }

    @Override
    public boolean start(UUID issuer) {
        if (active) return false;
        active = true;

        effectTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            int level = config.lockdownSlownessLevel();
            for (Player p : Bukkit.getOnlinePlayers()) {
                String team = jobRegistry.getPlayerTeam(p.getUniqueId());
                if (!"police".equals(team) && !"government".equals(team)) {
                    p.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOWNESS, 60, level, false, false, true));
                }
            }
        }, 0L, 40L);

        Bukkit.broadcastMessage("§c§l[LOCKDOWN] The city is under lockdown! Stay indoors.");
        Bukkit.getPluginManager().callEvent(new LockdownEvent(issuer, true));
        return true;
    }

    @Override
    public boolean end(UUID issuer) {
        if (!active) return false;
        active = false;

        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.SLOWNESS);
        }

        Bukkit.broadcastMessage("§a§l[LOCKDOWN] The lockdown has been lifted.");
        Bukkit.getPluginManager().callEvent(new LockdownEvent(issuer, false));
        return true;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void shutdown() {
        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }
        active = false;
    }
}
