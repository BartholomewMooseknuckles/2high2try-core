package com.twohigh.core.social;

import com.twohigh.api.job.JobDefinition;
import com.twohigh.api.social.ChatApi;
import com.twohigh.core.config.CoreConfig;
import com.twohigh.core.job.JobRegistryImpl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class ChatManager implements ChatApi {

    private final JobRegistryImpl jobRegistry;
    private final CoreConfig config;

    public ChatManager(JobRegistryImpl jobRegistry, CoreConfig config) {
        this.jobRegistry = jobRegistry;
        this.config = config;
    }

    @Override public int localRadius() { return config.chatLocalRadius(); }
    @Override public int whisperRadius() { return config.chatWhisperRadius(); }
    @Override public int yellRadius() { return config.chatYellRadius(); }

    @Override
    public void sendLocalMessage(UUID sender, String message) {
        sendProximityMessage(sender, message, config.chatLocalRadius(),
                "§7[Local] ", "§f");
    }

    @Override
    public void sendWhisperMessage(UUID sender, String message) {
        sendProximityMessage(sender, message, config.chatWhisperRadius(),
                "§8[Whisper] ", "§7§o");
    }

    @Override
    public void sendYellMessage(UUID sender, String message) {
        sendProximityMessage(sender, message, config.chatYellRadius(),
                "§c[YELL] ", "§f");
    }

    @Override
    public void sendOocMessage(UUID sender, String message) {
        Player senderPlayer = Bukkit.getPlayer(sender);
        if (senderPlayer == null) return;

        String formatted = "§3[OOC] §f" + senderPlayer.getName() + "§7: §f" + message;
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(formatted);
        }
    }

    private void sendProximityMessage(UUID sender, String message, int radius,
                                      String typePrefix, String messageColor) {
        Player senderPlayer = Bukkit.getPlayer(sender);
        if (senderPlayer == null) return;

        String jobPrefix = buildJobPrefix(sender);
        String formatted = typePrefix + jobPrefix + "§f" + senderPlayer.getName()
                + "§7: " + messageColor + message;

        Location senderLoc = senderPlayer.getLocation();
        long radiusSq = (long) radius * radius;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().equals(senderLoc.getWorld())) continue;
            if (p.getLocation().distanceSquared(senderLoc) <= radiusSq) {
                p.sendMessage(formatted);
            }
        }
    }

    private String buildJobPrefix(UUID player) {
        if (!config.chatShowJobTitle()) return "";

        Optional<String> jobId = jobRegistry.getPlayerJob(player);
        if (jobId.isEmpty()) return "§8[§7Citizen§8] ";

        JobDefinition job = jobRegistry.getJob(jobId.get());
        if (job == null) return "§8[§7Citizen§8] ";

        String color = job.chatColor() != null ? "§" + job.chatColor() : "§e";
        return "§8[" + color + job.displayName() + "§8] ";
    }
}
