package com.twohigh.core.command;

import com.twohigh.api.event.JobChangeEvent;
import com.twohigh.api.job.JobDefinition;
import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.defaults.DefaultJobs;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class JobAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("set", "clear");

    private final TwoHigh2TryCore plugin;

    public JobAdminCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /jobadmin <set|clear> <player> [job_id]");
            return true;
        }

        String sub = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or not online.");
            return true;
        }

        switch (sub) {
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /jobadmin set <player> <job_id>");
                    return true;
                }
                String jobId = args[2].toLowerCase();
                Optional<JobDefinition> jobOpt = plugin.jobRegistry().getJob(jobId);
                if (jobOpt.isEmpty()) {
                    sender.sendMessage("§cJob '" + jobId + "' not found. Use /job list to see available jobs.");
                    return true;
                }
                forceSetJob(target, jobId);
                JobDefinition job = jobOpt.get();
                sender.sendMessage("§a[JOB] Force-set §f" + target.getName() + "§a to §f"
                        + job.displayName() + " §7(" + jobId + ")§a. Bypassed slot/prereq checks.");
                target.sendMessage("§e[JOB] An admin set your job to §f" + job.displayName() + "§e.");
            }
            case "clear" -> {
                Optional<String> currentJob = plugin.jobRegistry().getPlayerJob(target.getUniqueId());
                if (currentJob.isEmpty()) {
                    sender.sendMessage("§c" + target.getName() + " doesn't have a job.");
                    return true;
                }
                plugin.jobRegistry().clearPlayerJob(target.getUniqueId());
                forceSetJob(target, DefaultJobs.CITIZEN);
                sender.sendMessage("§a[JOB] Cleared §f" + target.getName() + "§a's job. Reset to Citizen.");
                target.sendMessage("§e[JOB] An admin reset your job to Citizen.");
            }
            default -> sender.sendMessage("§cUsage: /jobadmin <set|clear> <player> [job_id]");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) return null;
        if (args.length == 3 && "set".equals(args[0].toLowerCase())) {
            String prefix = args[2].toLowerCase();
            return plugin.jobRegistry().allJobs().stream()
                    .map(JobDefinition::id)
                    .filter(id -> id.startsWith(prefix))
                    .toList();
        }
        return List.of();
    }

    private void forceSetJob(Player player, String jobId) {
        String old = plugin.jobRegistry().getPlayerJob(player.getUniqueId()).orElse(null);
        plugin.jobRegistry().playerJobMap().put(player.getUniqueId(), jobId);
        plugin.storage().savePlayerJob(player.getUniqueId(), jobId);
        Bukkit.getPluginManager().callEvent(new JobChangeEvent(player.getUniqueId(), old, jobId));
    }
}
