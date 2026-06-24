package com.twohigh.core.data.mysql;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MysqlMigrations {

    private static final String[][] MIGRATIONS = {
            // v1: foundation tables
            {
                    """
                    CREATE TABLE IF NOT EXISTS core_schema_version (
                        version INT NOT NULL
                    )
                    """,
                    """
                    CREATE TABLE IF NOT EXISTS player_cash (
                        player_uuid CHAR(36) NOT NULL PRIMARY KEY,
                        cash DOUBLE NOT NULL DEFAULT 0.0,
                        last_updated BIGINT NOT NULL
                    )
                    """,
                    """
                    CREATE TABLE IF NOT EXISTS player_jobs (
                        player_uuid CHAR(36) NOT NULL PRIMARY KEY,
                        job_id VARCHAR(64) NOT NULL,
                        joined_at BIGINT NOT NULL,
                        INDEX idx_job_id (job_id)
                    )
                    """,
                    """
                    CREATE TABLE IF NOT EXISTS combat_tags (
                        player_uuid CHAR(36) NOT NULL PRIMARY KEY,
                        tagged_at BIGINT NOT NULL,
                        expires_at BIGINT NOT NULL
                    )
                    """,
                    """
                    CREATE TABLE IF NOT EXISTS signal_sources (
                        id CHAR(36) NOT NULL PRIMARY KEY,
                        source_type VARCHAR(64) NOT NULL,
                        world VARCHAR(64) NOT NULL,
                        x DOUBLE NOT NULL,
                        y DOUBLE NOT NULL,
                        z DOUBLE NOT NULL,
                        strength DOUBLE NOT NULL,
                        registered_by VARCHAR(128) NOT NULL,
                        INDEX idx_signal_world (world)
                    )
                    """
            }
    };

    private MysqlMigrations() {}

    public static void run(HikariDataSource pool, Logger logger) {
        try (Connection conn = pool.getConnection()) {
            int current = currentVersion(conn);
            for (int v = current; v < MIGRATIONS.length; v++) {
                logger.info("Running migration v" + (v + 1) + "...");
                for (String sql : MIGRATIONS[v]) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sql.trim());
                    }
                }
                setVersion(conn, v + 1);
            }
            if (current < MIGRATIONS.length) {
                logger.info("Database schema is now at v" + MIGRATIONS.length + ".");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database migration failed", e);
            throw new RuntimeException("Migration failed", e);
        }
    }

    private static int currentVersion(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS core_schema_version (
                        version INT NOT NULL
                    )
                    """);
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT version FROM core_schema_version");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("version") : 0;
        }
    }

    private static void setVersion(Connection conn, int version) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM core_schema_version");
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO core_schema_version (version) VALUES (?)")) {
            ps.setInt(1, version);
            ps.executeUpdate();
        }
    }
}
