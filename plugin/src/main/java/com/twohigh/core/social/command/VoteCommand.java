package com.twohigh.core.social.command;

import com.twohigh.api.job.JobDefinition;
import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class VoteCommand implements CommandExecutor, TabCompleter {

    private final TwoHigh2TryCore plugin;

    public VoteCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /vote start <job_id> or /vote yes|no");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /vote start <job_id>");
                    return true;
                }
                String jobId = args[1].toLowerCase();
                Optional<JobDefinition> def = plugin.jobRegistry().getJob(jobId);
                if (def.isEmpty()) {
                    player.sendMessage("§cJob not found.");
                    return true;
                }
                if (!def.get().voteRequired()) {
                    player.sendMessage("§cThat job doesn't require a vote. Use §e/job join " + jobId + "§c.");
                    return true;
                }
                if (plugin.socialService().voteManager().startVote(player.getUniqueId(), jobId)) {
                    player.sendMessage("§aVote started!");
                } else {
                    player.sendMessage("§cA vote is already in progress.");
                }
            }
            case "yes" -> {
                if (plugin.socialService().voteManager().castVote(player.getUniqueId(), true)) {
                    player.sendMessage("§aVoted §2YES§a.");
                } else {
                    player.sendMessage("§cNo active vote or you already voted.");
                }
            }
            case "no" -> {
                if (plugin.socialService().voteManager().castVote(player.getUniqueId(), false)) {
                    player.sendMessage("§aVoted §4NO§a.");
                } else {
                    player.sendMessage("§cNo active vote or you already voted.");
                }
            }
            default -> player.sendMessage("§cUsage: /vote start <job_id> or /vote yes|no");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("start", "yes", "no"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            return filter(plugin.jobRegistry().allJobs().stream()
                    .filter(JobDefinition::voteRequired)
                    .map(JobDefinition::id)
                    .collect(Collectors.toList()), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
