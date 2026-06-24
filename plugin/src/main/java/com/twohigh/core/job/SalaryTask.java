package com.twohigh.core.job;

import com.twohigh.api.job.JobDefinition;
import com.twohigh.core.economy.CashManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SalaryTask extends BukkitRunnable {

    private final JobRegistryImpl jobRegistry;
    private final CashManager cashManager;

    public SalaryTask(JobRegistryImpl jobRegistry, CashManager cashManager) {
        this.jobRegistry = jobRegistry;
        this.cashManager = cashManager;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, String> entry : jobRegistry.playerJobMap().entrySet()) {
            UUID uuid = entry.getKey();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            Optional<JobDefinition> opt = jobRegistry.getJob(entry.getValue());
            if (opt.isEmpty()) continue;

            JobDefinition job = opt.get();
            if (job.salary() <= 0) continue;

            cashManager.deposit(uuid, job.salary());
            player.sendMessage("§a[§6Salary§a] +$" + String.format("%.2f", job.salary())
                    + " §7(" + job.displayName() + ")");
        }
    }
}
