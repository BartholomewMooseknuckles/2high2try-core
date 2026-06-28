package com.twohigh.core.listener;

import com.twohigh.core.TwoHigh2TryCore;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public final class ChatListener implements Listener {

    private final TwoHigh2TryCore plugin;

    public ChatListener(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String message = event.getMessage();

        if (plugin.socialService().groupChatManager().isGroupChatEnabled(uuid)) {
            plugin.socialService().groupChatManager().sendTeamMessage(uuid, message);
            return;
        }

        if (message.startsWith("//")) {
            String oocMsg = message.substring(2).trim();
            if (!oocMsg.isEmpty()) {
                plugin.socialService().chatManager().sendOocMessage(uuid, oocMsg);
            }
            return;
        }

        if (message.startsWith("!")) {
            String yellMsg = message.substring(1).trim();
            if (!yellMsg.isEmpty()) {
                plugin.socialService().chatManager().sendYellMessage(uuid, yellMsg);
            }
            return;
        }

        if (message.startsWith(".")) {
            String whisperMsg = message.substring(1).trim();
            if (!whisperMsg.isEmpty()) {
                plugin.socialService().chatManager().sendWhisperMessage(uuid, whisperMsg);
            }
            return;
        }

        plugin.socialService().chatManager().sendLocalMessage(uuid, message);
    }
}
