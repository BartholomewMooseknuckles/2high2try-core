package com.twohigh.core.social;

import com.twohigh.api.social.VoteApi;
import com.twohigh.api.social.event.VoteResolvedEvent;
import com.twohigh.api.social.event.VoteStartEvent;
import com.twohigh.core.defaults.DefaultJobs;
import com.twohigh.core.job.JobRegistryImpl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VoteManager implements VoteApi {

    private final JobRegistryImpl jobRegistry;
    private final JavaPlugin plugin;

    private volatile UUID nominee;
    private volatile String voteJobId;
    private final Set<UUID> yesVotes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> noVotes = ConcurrentHashMap.newKeySet();

    public VoteManager(JobRegistryImpl jobRegistry, JavaPlugin plugin) {
        this.jobRegistry = jobRegistry;
        this.plugin = plugin;
    }

    @Override
    public boolean startVote(UUID nominee, String jobId) {
        if (isVoteActive()) return false;

        this.nominee = nominee;
        this.voteJobId = jobId;
        yesVotes.clear();
        noVotes.clear();

        Player p = Bukkit.getPlayer(nominee);
        String name = p != null ? p.getName() : nominee.toString().substring(0, 8);

        Bukkit.broadcastMessage("§6§l[VOTE] §e" + name + " §7is running for §e"
                + jobId + "§7! Use §e/vote yes §7or §e/vote no§7. (60s)");

        Bukkit.getPluginManager().callEvent(new VoteStartEvent(nominee, jobId));

        plugin.getServer().getScheduler().runTaskLater(plugin, this::resolve, 20L * 60);
        return true;
    }

    @Override
    public boolean castVote(UUID voter, boolean approve) {
        if (!isVoteActive()) return false;
        if (yesVotes.contains(voter) || noVotes.contains(voter)) return false;

        if (approve) yesVotes.add(voter); else noVotes.add(voter);
        return true;
    }

    @Override
    public boolean isVoteActive() {
        return nominee != null;
    }

    @Override
    public String getActiveJobId() {
        return voteJobId;
    }

    @Override
    public UUID getNominee() {
        return nominee;
    }

    private void resolve() {
        if (!isVoteActive()) return;

        int yes = yesVotes.size();
        int no = noVotes.size();
        boolean passed = yes > no;

        UUID nom = this.nominee;
        String jid = this.voteJobId;

        this.nominee = null;
        this.voteJobId = null;
        yesVotes.clear();
        noVotes.clear();

        Player p = Bukkit.getPlayer(nom);
        String name = p != null ? p.getName() : nom.toString().substring(0, 8);

        if (passed) {
            jobRegistry.setPlayerJob(nom, jid);
            Bukkit.broadcastMessage("§a§l[VOTE] §e" + name + " §ahas been elected as §e"
                    + jid + "§a! (" + yes + " yes / " + no + " no)");
        } else {
            Bukkit.broadcastMessage("§c§l[VOTE] §e" + name + " §cwas not elected. ("
                    + yes + " yes / " + no + " no)");
        }

        Bukkit.getPluginManager().callEvent(new VoteResolvedEvent(nom, jid, passed, yes, no));
    }
}
