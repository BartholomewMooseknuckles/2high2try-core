package com.twohigh.core.social;

import com.twohigh.api.job.JobDefinition;
import com.twohigh.api.social.DemoteApi;
import com.twohigh.api.social.event.PlayerDemotedEvent;
import com.twohigh.core.defaults.DefaultJobs;
import com.twohigh.core.job.JobRegistryImpl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DemoteManager implements DemoteApi {

    private final JobRegistryImpl jobRegistry;
    private final JavaPlugin plugin;

    private final ConcurrentHashMap<String, Set<UUID>> demoteBans = new ConcurrentHashMap<>();

    private volatile UUID demoteTarget;
    private final Set<UUID> yesVotes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> noVotes = ConcurrentHashMap.newKeySet();

    public DemoteManager(JobRegistryImpl jobRegistry, JavaPlugin plugin) {
        this.jobRegistry = jobRegistry;
        this.plugin = plugin;
    }

    @Override
    public boolean startDemoteVote(UUID initiator, UUID target) {
        if (isDemoteVoteActive()) return false;
        if (target.equals(initiator)) return false;

        String initiatorTeam = jobRegistry.getPlayerTeam(initiator);
        String targetTeam = jobRegistry.getPlayerTeam(target);
        if (!initiatorTeam.equals(targetTeam)) return false;

        this.demoteTarget = target;
        yesVotes.clear();
        noVotes.clear();
        yesVotes.add(initiator);

        Player p = Bukkit.getPlayer(target);
        String name = p != null ? p.getName() : target.toString().substring(0, 8);

        Bukkit.broadcastMessage("§c§l[DEMOTE] §7Vote to demote §e" + name
                + "§7. Use §e/demote yes §7or §e/demote no§7. (30s)");

        plugin.getServer().getScheduler().runTaskLater(plugin, this::resolveDemote, 20L * 30);
        return true;
    }

    @Override
    public boolean castDemoteVote(UUID voter, boolean approve) {
        if (!isDemoteVoteActive()) return false;
        if (yesVotes.contains(voter) || noVotes.contains(voter)) return false;

        if (approve) yesVotes.add(voter); else noVotes.add(voter);
        return true;
    }

    @Override
    public boolean isDemoteVoteActive() {
        return demoteTarget != null;
    }

    @Override
    public boolean isDemoteBanned(UUID player, String demoteGroup) {
        if (demoteGroup == null) return false;
        Set<UUID> banned = demoteBans.get(demoteGroup);
        return banned != null && banned.contains(player);
    }

    private void resolveDemote() {
        if (!isDemoteVoteActive()) return;

        int yes = yesVotes.size();
        int no = noVotes.size();
        boolean passed = yes > no;

        UUID target = this.demoteTarget;
        this.demoteTarget = null;
        yesVotes.clear();
        noVotes.clear();

        Player p = Bukkit.getPlayer(target);
        String name = p != null ? p.getName() : target.toString().substring(0, 8);

        if (passed) {
            Optional<String> jobOpt = jobRegistry.getPlayerJob(target);
            String oldJobId = jobOpt.orElse(null);

            if (oldJobId != null) {
                Optional<JobDefinition> def = jobRegistry.getJob(oldJobId);
                String demoteGroup = def.map(JobDefinition::demoteGroup).orElse(null);

                if (demoteGroup != null) {
                    demoteBans.computeIfAbsent(demoteGroup, k -> ConcurrentHashMap.newKeySet())
                            .add(target);
                }

                Bukkit.getPluginManager().callEvent(
                        new PlayerDemotedEvent(target, oldJobId, demoteGroup));
            }

            jobRegistry.setPlayerJob(target, DefaultJobs.CITIZEN);
            Bukkit.broadcastMessage("§c§l[DEMOTE] §e" + name
                    + " §chas been demoted! (" + yes + "/" + no + ")");
        } else {
            Bukkit.broadcastMessage("§a§l[DEMOTE] §e" + name
                    + " §asurvived the vote. (" + yes + "/" + no + ")");
        }
    }
}
