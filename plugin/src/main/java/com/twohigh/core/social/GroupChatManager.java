package com.twohigh.core.social;

import com.twohigh.api.social.GroupChatApi;
import com.twohigh.core.job.JobRegistryImpl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GroupChatManager implements GroupChatApi {

    private final JobRegistryImpl jobRegistry;
    private final Set<UUID> toggled = ConcurrentHashMap.newKeySet();

    public GroupChatManager(JobRegistryImpl jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    @Override
    public void sendTeamMessage(UUID sender, String message) {
        Player senderPlayer = Bukkit.getPlayer(sender);
        if (senderPlayer == null) return;

        String team = jobRegistry.getPlayerTeam(sender);
        String jobId = jobRegistry.getPlayerJob(sender).orElse("citizen");
        String prefix = "§8[§" + teamColor(team) + team.toUpperCase() + "§8] §f"
                + senderPlayer.getName() + "§7: §f";

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (jobRegistry.getPlayerTeam(p.getUniqueId()).equals(team)) {
                p.sendMessage(prefix + message);
            }
        }
    }

    @Override
    public boolean isGroupChatEnabled(UUID player) {
        return toggled.contains(player);
    }

    @Override
    public void toggleGroupChat(UUID player) {
        if (!toggled.remove(player)) {
            toggled.add(player);
        }
    }

    private String teamColor(String team) {
        return switch (team) {
            case "police" -> "9";
            case "government" -> "6";
            case "criminal" -> "c";
            default -> "7";
        };
    }
}
