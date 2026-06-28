package com.twohigh.core.data.mysql;

import com.twohigh.core.data.CoreStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import com.twohigh.core.printer.MoneyPrinter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    // --- Law enforcement: wanted ---

    @Override
    public CompletableFuture<Void> saveWanted(UUID player, String officerUuid, String reason) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO wanted_players (player_uuid, officer_uuid, reason, created_at) VALUES (?, ?, ?, ?) "
                                 + "ON DUPLICATE KEY UPDATE officer_uuid = VALUES(officer_uuid), reason = VALUES(reason)")) {
                ps.setString(1, player.toString());
                ps.setString(2, officerUuid);
                ps.setString(3, reason);
                ps.setLong(4, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to save wanted for " + player, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> removeWanted(UUID player) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM wanted_players WHERE player_uuid = ?")) {
                ps.setString(1, player.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to remove wanted for " + player, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Map<UUID, String[]>> loadAllWanted() {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, String[]> map = new HashMap<>();
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT player_uuid, officer_uuid, reason FROM wanted_players");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(UUID.fromString(rs.getString("player_uuid")),
                            new String[]{rs.getString("officer_uuid"), rs.getString("reason")});
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load wanted players", e);
            }
            return map;
        }, worker);
    }

    // --- Law enforcement: jail ---

    @Override
    public CompletableFuture<Void> saveJailPosition(String name, String world,
                                                     double x, double y, double z, float yaw) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO jail_positions (name, world, x, y, z, yaw) VALUES (?, ?, ?, ?, ?, ?) "
                                 + "ON DUPLICATE KEY UPDATE world = VALUES(world), x = VALUES(x), "
                                 + "y = VALUES(y), z = VALUES(z), yaw = VALUES(yaw)")) {
                ps.setString(1, name);
                ps.setString(2, world);
                ps.setDouble(3, x);
                ps.setDouble(4, y);
                ps.setDouble(5, z);
                ps.setFloat(6, yaw);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to save jail position " + name, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> saveArrest(UUID player, long releaseAt) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO active_arrests (player_uuid, release_at) VALUES (?, ?) "
                                 + "ON DUPLICATE KEY UPDATE release_at = VALUES(release_at)")) {
                ps.setString(1, player.toString());
                ps.setLong(2, releaseAt);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to save arrest for " + player, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> removeArrest(UUID player) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM active_arrests WHERE player_uuid = ?")) {
                ps.setString(1, player.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to remove arrest for " + player, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Map<UUID, Long>> loadActiveArrests() {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Long> map = new HashMap<>();
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT player_uuid, release_at FROM active_arrests WHERE release_at > ?")) {
                ps.setLong(1, System.currentTimeMillis());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(UUID.fromString(rs.getString("player_uuid")), rs.getLong("release_at"));
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load active arrests", e);
            }
            return map;
        }, worker);
    }

    // --- Law enforcement: licenses ---

    @Override
    public CompletableFuture<Void> saveLicense(UUID player) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT IGNORE INTO gun_licenses (player_uuid, granted_at) VALUES (?, ?)")) {
                ps.setString(1, player.toString());
                ps.setLong(2, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to save license for " + player, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> removeLicense(UUID player) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM gun_licenses WHERE player_uuid = ?")) {
                ps.setString(1, player.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to remove license for " + player, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Set<UUID>> loadAllLicenses() {
        return CompletableFuture.supplyAsync(() -> {
            Set<UUID> set = new HashSet<>();
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT player_uuid FROM gun_licenses");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(UUID.fromString(rs.getString("player_uuid")));
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load licenses", e);
            }
            return set;
        }, worker);
    }

    // --- Money printers ---

    @Override
    public CompletableFuture<Void> savePrinter(UUID id, UUID owner, String world,
                                                int x, int y, int z, double accumulated, long placedAt) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO money_printers (id, owner_uuid, world, x, y, z, accumulated, placed_at) "
                                 + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                                 + "accumulated = VALUES(accumulated)")) {
                ps.setString(1, id.toString());
                ps.setString(2, owner.toString());
                ps.setString(3, world);
                ps.setInt(4, x);
                ps.setInt(5, y);
                ps.setInt(6, z);
                ps.setDouble(7, accumulated);
                ps.setLong(8, placedAt);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to save printer " + id, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> updatePrinterAccumulated(UUID id, double accumulated) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE money_printers SET accumulated = ? WHERE id = ?")) {
                ps.setDouble(1, accumulated);
                ps.setString(2, id.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to update printer " + id, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> removePrinter(UUID id) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM money_printers WHERE id = ?")) {
                ps.setString(1, id.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to remove printer " + id, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<List<MoneyPrinter>> loadAllPrinters() {
        return CompletableFuture.supplyAsync(() -> {
            List<MoneyPrinter> list = new ArrayList<>();
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT id, owner_uuid, world, x, y, z, accumulated, placed_at FROM money_printers");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    World w = Bukkit.getWorld(rs.getString("world"));
                    if (w == null) continue;
                    Location loc = new Location(w, rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
                    list.add(new MoneyPrinter(
                            UUID.fromString(rs.getString("id")),
                            UUID.fromString(rs.getString("owner_uuid")),
                            loc,
                            rs.getDouble("accumulated"),
                            rs.getLong("placed_at")));
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load printers", e);
            }
            return list;
        }, worker);
    }

    // --- Party bank ---

    @Override
    public CompletableFuture<Void> savePartyBank(UUID partyId, double balance) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO party_banks (party_id, balance) VALUES (?, ?) "
                                 + "ON DUPLICATE KEY UPDATE balance = VALUES(balance)")) {
                ps.setString(1, partyId.toString());
                ps.setDouble(2, balance);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to save party bank " + partyId, e);
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Double> loadPartyBank(UUID partyId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT balance FROM party_banks WHERE party_id = ?")) {
                ps.setString(1, partyId.toString());
                ResultSet rs = ps.executeQuery();
                return rs.next() ? rs.getDouble("balance") : 0.0;
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load party bank " + partyId, e);
                return 0.0;
            }
        }, worker);
    }

    @Override
    public CompletableFuture<Void> removePartyBank(UUID partyId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM party_banks WHERE party_id = ?")) {
                ps.setString(1, partyId.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to remove party bank " + partyId, e);
            }
        }, worker);
    }
}
