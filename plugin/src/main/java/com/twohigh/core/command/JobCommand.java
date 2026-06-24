package com.twohigh.core.command;

import com.twohigh.api.job.JobDefinition;
import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.job.JobRegistryImpl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class JobCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;

    public JobCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            showCurrentJob(player);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "list" -> { listJobs(player); yield true; }
            case "join" -> { joinJob(player, args); yield true; }
            case "quit", "leave" -> { quitJob(player); yield true; }
            default -> { showUsage(player); yield true; }
        };
    }

    private void showCurrentJob(Player player) {
        JobRegistryImpl registry = plugin.jobRegistry();
        Optional<String> jobId = registry.getPlayerJob(player.getUniqueId());
        if (jobId.isEmpty()) {
            player.sendMessage("§7You don't have a job. Use §e/job list§7 to see available jobs.");
            return;
        }
        Optional<JobDefinition> job = registry.getJob(jobId.get());
        if (job.isEmpty()) {
            player.sendMessage("§7You don't have a job. Use §e/job list§7 to see available jobs.");
            return;
        }
        JobDefinition j = job.get();
        player.sendMessage("§6§lYour Job");
        player.sendMessage("§7Title: §f" + j.displayName());
        player.sendMessage("§7Type: " + (j.legal() ? "§aLegal" : "§cIllegal"));
        if (j.salary() > 0) {
            long intervalMin = j.salaryIntervalMs() / 60_000;
            player.sendMessage("§7Salary: §a$" + String.format("%.2f", j.salary())
                    + " §7every §f" + intervalMin + "m");
        }
    }

    private void listJobs(Player player) {
        Collection<JobDefinition> all = plugin.jobRegistry().allJobs();
        if (all.isEmpty()) {
            player.sendMessage("§7No jobs are available.");
            return;
        }
        player.sendMessage("§6§lAvailable Jobs");
        for (JobDefinition j : all) {
            String type = j.legal() ? "§a[Legal]" : "§c[Illegal]";
            String salary = j.salary() > 0
                    ? " §7- §a$" + String.format("%.2f", j.salary()) + "/cycle"
                    : "";
            player.sendMessage(" §e" + j.displayName() + " " + type + salary
                    + " §8(/job join " + j.id() + ")");
        }
    }

    private void joinJob(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /job join <job_id>");
            return;
        }
        String jobId = args[1].toLowerCase();
        JobRegistryImpl registry = plugin.jobRegistry();

        Optional<JobDefinition> opt = registry.getJob(jobId);
        if (opt.isEmpty()) {
            player.sendMessage("§cJob '" + jobId + "' does not exist. Use §e/job list§c.");
            return;
        }

        JobDefinition job = opt.get();
        if (job.permission() != null && !job.permission().isEmpty()
                && !player.hasPermission(job.permission())) {
            player.sendMessage("§cYou don't have permission for this job.");
            return;
        }

        Optional<String> current = registry.getPlayerJob(player.getUniqueId());
        if (current.isPresent() && current.get().equals(jobId)) {
            player.sendMessage("§cYou already have this job.");
            return;
        }

        if (registry.setPlayerJob(player.getUniqueId(), jobId)) {
            player.sendMessage("§aYou are now a §e" + job.displayName() + "§a!");
        } else {
            player.sendMessage("§cFailed to join job.");
        }
    }

    private void quitJob(Player player) {
        if (plugin.jobRegistry().clearPlayerJob(player.getUniqueId())) {
            player.sendMessage("§aYou quit your job.");
        } else {
            player.sendMessage("§cYou don't have a job to quit.");
        }
    }

    private void showUsage(Player player) {
        player.sendMessage("§6Usage: §e/job [list|join <id>|quit]");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("list", "join", "quit"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            List<String> ids = plugin.jobRegistry().allJobs().stream()
                    .map(JobDefinition::id)
                    .collect(Collectors.toList());
            return filter(ids, args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
