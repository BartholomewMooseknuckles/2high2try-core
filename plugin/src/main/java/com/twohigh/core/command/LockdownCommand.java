package com.twohigh.core.command;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.defaults.DefaultJobs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class LockdownCommand implements CommandExecutor {

    private final TwoHigh2TryCore plugin;

    public LockdownCommand(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Optional<String> job = plugin.jobRegistry().getPlayerJob(player.getUniqueId());
        boolean isMayor = job.isPresent() && job.get().equals(DefaultJobs.MAYOR);
        boolean isChief = job.isPresent() && job.get().equals(DefaultJobs.CHIEF);

        if (!isMayor && !isChief && !player.hasPermission("twohigh.admin")) {
            player.sendMessage("§cOnly the Mayor or Police Chief can initiate a lockdown.");
            return true;
        }

        if (plugin.lawEnforcement().lockdownManager().isActive()) {
            if (plugin.lawEnforcement().lockdownManager().end(player.getUniqueId())) {
                player.sendMessage("§aLockdown ended.");
            }
        } else {
            if (plugin.lawEnforcement().lockdownManager().start(player.getUniqueId())) {
                player.sendMessage("§cLockdown initiated.");
            }
        }
        return true;
    }
}
