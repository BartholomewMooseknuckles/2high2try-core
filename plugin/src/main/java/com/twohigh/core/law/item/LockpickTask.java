package com.twohigh.core.law.item;

import com.twohigh.core.defaults.DefaultJobs;
import com.twohigh.core.job.JobRegistryImpl;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LockpickTask {

    private static final Map<UUID, BukkitRunnable> activePicks = new ConcurrentHashMap<>();

    private LockpickTask() {}

    public static boolean isActive(UUID player) {
        return activePicks.containsKey(player);
    }

    public static void cancel(UUID player) {
        BukkitRunnable task = activePicks.remove(player);
        if (task != null) task.cancel();
    }

    public static void start(Player player, Block block, JavaPlugin plugin, JobRegistryImpl jobRegistry) {
        if (isActive(player.getUniqueId())) {
            player.sendMessage("§cYou're already picking a lock!");
            return;
        }

        String jobId = jobRegistry.getPlayerJob(player.getUniqueId()).orElse("");
        int totalTicks = DefaultJobs.THIEF.equals(jobId) ? 200 : 300;
        int totalSeconds = totalTicks / 20;

        Location startLoc = player.getLocation().clone();
        Location blockLoc = block.getLocation().clone();

        player.sendMessage("§8Picking lock... §7Don't move! §8(" + totalSeconds + "s)");

        BukkitRunnable task = new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                elapsed++;

                if (!player.isOnline() || player.isDead()) {
                    fail("Lockpick cancelled.");
                    return;
                }

                if (player.getLocation().distanceSquared(startLoc) > 1.0) {
                    fail("§cYou moved! Lockpick failed.");
                    return;
                }

                if (elapsed % 20 == 0) {
                    int remaining = (totalTicks - elapsed) / 20;
                    float progress = (float) elapsed / totalTicks;
                    int bars = (int) (progress * 20);
                    StringBuilder bar = new StringBuilder("§8[");
                    for (int i = 0; i < 20; i++) {
                        bar.append(i < bars ? "§a|" : "§7|");
                    }
                    bar.append("§8] §7").append(remaining).append("s");
                    player.sendActionBar(bar.toString());
                }

                if (elapsed >= totalTicks) {
                    activePicks.remove(player.getUniqueId());
                    cancel();
                    player.sendMessage("§aLock picked! The container is open.");
                    player.openInventory(
                            ((org.bukkit.block.Container) block.getState()).getInventory());
                }
            }

            private void fail(String message) {
                activePicks.remove(player.getUniqueId());
                cancel();
                player.sendMessage(message);
            }
        };

        activePicks.put(player.getUniqueId(), task);
        task.runTaskTimer(plugin, 0L, 1L);
    }
}
