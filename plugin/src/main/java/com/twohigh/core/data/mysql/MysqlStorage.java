package com.twohigh.core.data.mysql;

import com.twohigh.core.data.CoreStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MysqlStorage implements CoreStorage {

    private final Logger logger;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private HikariDataSource pool;
    private ExecutorService worker;

    public MysqlStorage(Logger logger, String host, int port, String database,
                        String username, String password) {
        this.logger = logger;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public void init() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000);
        config.setPoolName("2high2try-pool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");

        this.pool = new HikariDataSource(config);
        this.worker = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "2high2try-db");
            t.setDaemon(true);
            return t;
        });

        MysqlMigrations.run(pool, logger);
    }

    @Override
    public void shutdown() {
        if (worker != null) {
            worker.shutdown();
            worker = null;
        }
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

    @Override
    public CompletableFuture<Double> loadCash(UUID player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT cash FROM player_cash WHERE player_uuid = ?")) {
                ps.setString(1, player.toString());
                ResultSet rs = ps.executeQuery();
                return rs.next() ? rs.getDouble("cash") : 0.0;
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load cash for " + player, e);
                return 0.0;
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> saveCash(UUID player, double amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO player_cash (player_uuid, cash, last_updated) VALUES (?, ?, ?) "
                                 + "ON DUPLICATE KEY UPDATE cash = VALUES(cash), last_updated = VALUES(last_updated)")) {
                ps.setString(1, player.toString());
                ps.setDouble(2, amount);
                ps.setLong(3, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to save cash for " + player, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Map<UUID, Double>> loadAllCash() {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Double> map = new HashMap<>();
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT player_uuid, cash FROM player_cash");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(UUID.fromString(rs.getString("player_uuid")), rs.getDouble("cash"));
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load all cash", e);
            }
            return map;
        }, worker);
    }

    @Override
    public CompletableFuture<String> loadPlayerJob(UUID player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT job_id FROM player_jobs WHERE player_uuid = ?")) {
                ps.setString(1, player.toString());
                ResultSet rs = ps.executeQuery();
                return rs.next() ? rs.getString("job_id") : null;
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load job for " + player, e);
                return null;
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> savePlayerJob(UUID player, String jobId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO player_jobs (player_uuid, job_id, joined_at) VALUES (?, ?, ?) "
                                 + "ON DUPLICATE KEY UPDATE job_id = VALUES(job_id), joined_at = VALUES(joined_at)")) {
                ps.setString(1, player.toString());
                ps.setString(2, jobId);
                ps.setLong(3, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to save job for " + player, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> clearPlayerJob(UUID player) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM player_jobs WHERE player_uuid = ?")) {
                ps.setString(1, player.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to clear job for " + player, e);
            }
        }, worker);
    }
}
