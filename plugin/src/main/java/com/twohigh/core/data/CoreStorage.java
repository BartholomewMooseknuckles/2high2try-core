package com.twohigh.core.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CoreStorage {

    void init();

    void shutdown();

    CompletableFuture<Double> loadCash(UUID player);

    CompletableFuture<Void> saveCash(UUID player, double amount);

    CompletableFuture<Map<UUID, Double>> loadAllCash();

    CompletableFuture<String> loadPlayerJob(UUID player);

    CompletableFuture<Void> savePlayerJob(UUID player, String jobId);

    CompletableFuture<Void> clearPlayerJob(UUID player);
}
