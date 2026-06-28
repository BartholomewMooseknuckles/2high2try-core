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

    // Law enforcement — wanted
    CompletableFuture<Void> saveWanted(UUID player, String officerUuid, String reason);
    CompletableFuture<Void> removeWanted(UUID player);
    CompletableFuture<Map<UUID, String[]>> loadAllWanted();

    // Law enforcement — jail
    CompletableFuture<Void> saveJailPosition(String name, String world, double x, double y, double z, float yaw);
    CompletableFuture<Void> saveArrest(UUID player, long releaseAt);
    CompletableFuture<Void> removeArrest(UUID player);
    CompletableFuture<Map<UUID, Long>> loadActiveArrests();

    // Law enforcement — licenses
    CompletableFuture<Void> saveLicense(UUID player);
    CompletableFuture<Void> removeLicense(UUID player);
    CompletableFuture<java.util.Set<UUID>> loadAllLicenses();

    // Money printers
    CompletableFuture<Void> savePrinter(UUID id, UUID owner, String world, int x, int y, int z, double accumulated, long placedAt);
    CompletableFuture<Void> updatePrinterAccumulated(UUID id, double accumulated);
    CompletableFuture<Void> removePrinter(UUID id);
    CompletableFuture<java.util.List<com.twohigh.core.printer.MoneyPrinter>> loadAllPrinters();

    // Party bank
    CompletableFuture<Void> savePartyBank(UUID partyId, double balance);
    CompletableFuture<Double> loadPartyBank(UUID partyId);
    CompletableFuture<Void> removePartyBank(UUID partyId);
}
