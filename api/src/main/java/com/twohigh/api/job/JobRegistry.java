package com.twohigh.api.job;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface JobRegistry {

    void registerJob(JobDefinition job);

    void unregisterJob(String jobId);

    Optional<JobDefinition> getJob(String jobId);

    Collection<JobDefinition> allJobs();

    Optional<String> getPlayerJob(UUID player);

    boolean setPlayerJob(UUID player, String jobId);

    boolean clearPlayerJob(UUID player);

    int getPlayersInJob(String jobId);

    int getAvailableSlots(String jobId);

    String getPlayerTeam(UUID player);
}
