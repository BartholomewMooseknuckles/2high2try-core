package com.twohigh.core.mug;

import com.twohigh.core.TwoHigh2TryCore;
import com.twohigh.core.config.CoreConfig;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MugManager {

    private final TwoHigh2TryCore plugin;
    private final CoreConfig config;
    private final ConcurrentHashMap<UUID, MugSession> activeMugs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public MugManager(TwoHigh2TryCore plugin) {
        this.plugin = plugin;
        this.config = plugin.coreConfig();
    }

    public boolean startMug(Player mugger, Player victim, double amount) {
        UUID muggerId = mugger.getUniqueId();
        UUID victimId = victim.getUniqueId();

        if (amount > config.mugCap()) {
            mugger.sendMessage("§cMax mugging amount is $" + config.mugCap() + ".");
            return false;
        }
        if (amount <= 0) {
            mugger.sendMessage("§cAmount must be positive.");
            return false;
        }

        if (isOnCooldown(muggerId)) {
            long remaining = cooldownRemaining(muggerId) / 1000;
            mugger.sendMessage("§cMugging cooldown: " + remaining + "s remaining.");
            return false;
        }

        if (activeMugs.containsKey(muggerId) || getSessionByVictim(victimId).isPresent()) {
            mugger.sendMessage("§cA mugging is already in progress.");
            return false;
        }

        if (mugger.getLocation().distance(victim.getLocation()) > config.mugMaxDistance()) {
            mugger.sendMessage("§cYou must be within " + config.mugMaxDistance() + " blocks to mug.");
            return false;
        }

        MugSession session = new MugSession(muggerId, victimId, amount);
        activeMugs.put(muggerId, session);

        victim.sendMessage("§c§l[MUG] §e" + mugger.getName() + " §7is mugging you for §a$"
                + String.format("%.2f", amount) + "§7!");
        victim.sendMessage("§7Type §e/pay " + mugger.getName() + " " + String.format("%.0f", amount)
                + " §7to comply, or §cfight back§7!");

        mugger.sendMessage("§c§l[MUG] §7Mugging §e" + victim.getName() + " §7for §a$"
                + String.format("%.2f", amount) + "§7. Waiting for response...");

        plugin.pvpManager().addMugPvPPair(muggerId, victimId);

        int windowTicks = config.mugWindowSeconds() * 20;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MugSession s = activeMugs.get(muggerId);
            if (s != null && !s.resolved()) {
                resolveMug(muggerId, false);
            }
        }, windowTicks);

        return true;
    }

    public void resolveMug(UUID muggerId, boolean complied) {
        MugSession session = activeMugs.remove(muggerId);
        if (session == null || session.resolved()) return;
        session.setResolved(true);

        plugin.pvpManager().removeMugPvPPair(session.mugger(), session.victim());

        Player mugger = Bukkit.getPlayer(session.mugger());
        Player victim = Bukkit.getPlayer(session.victim());

        if (complied && victim != null && mugger != null) {
            if (plugin.economyService().hasCash(victim, session.amount())) {
                plugin.economyService().withdrawCash(victim, session.amount());
                plugin.economyService().depositCash(mugger, session.amount());
                mugger.sendMessage("§a[MUG] §e" + victim.getName() + " §7paid up! +$"
                        + String.format("%.2f", session.amount()));
                victim.sendMessage("§c[MUG] §7You paid §e" + mugger.getName() + " §a$"
                        + String.format("%.2f", session.amount()) + "§7.");
            } else {
                if (mugger != null) mugger.sendMessage("§c[MUG] Victim doesn't have enough cash.");
                if (victim != null) victim.sendMessage("§7[MUG] You don't have enough cash to comply.");
            }
        } else {
            if (mugger != null) mugger.sendMessage("§c[MUG] §7Mugging expired or was refused. Fight!");
            if (victim != null) victim.sendMessage("§c[MUG] §7Mugging expired. Defend yourself!");
        }

        long cooldownMs = config.mugCooldownMinutes() * 60_000L;
        cooldowns.put(session.mugger(), System.currentTimeMillis() + cooldownMs);
    }

    public Optional<MugSession> getSession(UUID muggerId) {
        return Optional.ofNullable(activeMugs.get(muggerId));
    }

    public Optional<MugSession> getSessionByVictim(UUID victimId) {
        for (MugSession session : activeMugs.values()) {
            if (session.victim().equals(victimId)) return Optional.of(session);
        }
        return Optional.empty();
    }

    public boolean isOnCooldown(UUID player) {
        Long until = cooldowns.get(player);
        return until != null && System.currentTimeMillis() < until;
    }

    public long cooldownRemaining(UUID player) {
        Long until = cooldowns.get(player);
        if (until == null) return 0;
        return Math.max(0, until - System.currentTimeMillis());
    }
}
