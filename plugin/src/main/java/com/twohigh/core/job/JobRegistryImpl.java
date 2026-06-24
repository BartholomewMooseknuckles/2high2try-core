package com.twohigh.core.job;

import com.twohigh.api.event.JobChangeEvent;
import com.twohigh.api.job.JobDefinition;
import com.twohigh.api.job.JobRegistry;
import com.twohigh.core.data.CoreStorage;

import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class JobRegistryImpl implements JobRegistry {

    private final ConcurrentHashMap<String, JobDefinition> jobs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> playerJobs = new ConcurrentHashMap<>();
    private final CoreStorage storage;
    private final Logger logger;

    public JobRegistryImpl(CoreStorage storage, Logger logger) {
        this.storage = storage;
        this.logger = logger;
    }

    @Override
    public void registerJob(JobDefinition job) {
        if (jobs.putIfAbsent(job.id(), job) != null) {
            logger.warning("Job '" + job.id() + "' is already registered — ignoring duplicate from "
                    + job.owningPlugin().getName());
            return;
        }
        logger.info("Registered job: " + job.displayName() + " (" + job.id()
                + ") from " + job.owningPlugin().getName());
    }

    @Override
    public void unregisterJob(String jobId) {
        JobDefinition removed = jobs.remove(jobId);
        if (removed != null) {
            logger.info("Unregistered job: " + removed.displayName() + " (" + jobId + ")");
        }
    }

    @Override
    public Optional<JobDefinition> getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    @Override
    public Collection<JobDefinition> allJobs() {
        return Collections.unmodifiableCollection(jobs.values());
    }

    @Override
    public Optional<String> getPlayerJob(UUID player) {
        return Optional.ofNullable(playerJobs.get(player));
    }

    @Override
    public boolean setPlayerJob(UUID player, String jobId) {
        JobDefinition job = jobs.get(jobId);
        if (job == null) return false;

        String oldJob = playerJobs.put(player, jobId);
        storage.savePlayerJob(player, jobId);
        Bukkit.getPluginManager().callEvent(new JobChangeEvent(player, oldJob, jobId));
        return true;
    }

    @Override
    public boolean clearPlayerJob(UUID player) {
        String old = playerJobs.remove(player);
        if (old == null) return false;

        storage.clearPlayerJob(player);
        Bukkit.getPluginManager().callEvent(new JobChangeEvent(player, old, null));
        return true;
    }

    public void loadPlayer(UUID player) {
        storage.loadPlayerJob(player).thenAccept(jobId -> {
            if (jobId != null && jobs.containsKey(jobId)) {
                playerJobs.put(player, jobId);
            }
        });
    }

    public void unloadPlayer(UUID player) {
        playerJobs.remove(player);
    }

    public void unregisterAllFromPlugin(String pluginName) {
        jobs.values().removeIf(j -> j.owningPlugin().getName().equals(pluginName));
    }

    public ConcurrentHashMap<UUID, String> playerJobMap() {
        return playerJobs;
    }
}
