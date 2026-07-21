package com.twohigh.testbot;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Write-through SQLite persistence so the campaign survives bot restarts. */
public final class TestStore implements AutoCloseable {

    public record MessageRef(long messageId, long threadId) {}
    public record Note(String author, String content, long createdAt) {}

    private final Connection conn;

    public TestStore(File dataDir) throws SQLException {
        File db = new File(dataDir, "testbot-data.db");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
        try (Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS messages(
                        test_num INTEGER PRIMARY KEY,
                        message_id INTEGER NOT NULL,
                        thread_id INTEGER NOT NULL)""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS votes(
                        test_num INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        vote TEXT NOT NULL,
                        PRIMARY KEY(test_num, user_id))""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS retest(
                        test_num INTEGER PRIMARY KEY,
                        flagged INTEGER NOT NULL)""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS notes(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        test_num INTEGER NOT NULL,
                        author TEXT NOT NULL,
                        content TEXT NOT NULL,
                        created_at INTEGER NOT NULL)""");
        }
    }

    public synchronized void saveMessage(int testNum, long messageId, long threadId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO messages(test_num, message_id, thread_id) VALUES (?,?,?)")) {
            ps.setInt(1, testNum);
            ps.setLong(2, messageId);
            ps.setLong(3, threadId);
            ps.executeUpdate();
        }
    }

    public synchronized Map<Integer, MessageRef> loadMessages() throws SQLException {
        Map<Integer, MessageRef> map = new HashMap<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT test_num, message_id, thread_id FROM messages")) {
            while (rs.next()) {
                map.put(rs.getInt(1), new MessageRef(rs.getLong(2), rs.getLong(3)));
            }
        }
        return map;
    }

    public synchronized void setVote(int testNum, long userId, String vote) throws SQLException {
        if (vote == null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM votes WHERE test_num = ? AND user_id = ?")) {
                ps.setInt(1, testNum);
                ps.setLong(2, userId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT OR REPLACE INTO votes(test_num, user_id, vote) VALUES (?,?,?)")) {
                ps.setInt(1, testNum);
                ps.setLong(2, userId);
                ps.setString(3, vote);
                ps.executeUpdate();
            }
        }
    }

    /** vote -> count of users, for one test. */
    public synchronized Map<String, Integer> voteCounts(int testNum) throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT vote, COUNT(*) FROM votes WHERE test_num = ? GROUP BY vote")) {
            ps.setInt(1, testNum);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                counts.put(rs.getString(1), rs.getInt(2));
            }
        }
        return counts;
    }

    public synchronized void setRetest(int testNum, boolean flagged) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO retest(test_num, flagged) VALUES (?,?)")) {
            ps.setInt(1, testNum);
            ps.setInt(2, flagged ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public synchronized boolean isRetest(int testNum) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT flagged FROM retest WHERE test_num = ?")) {
            ps.setInt(1, testNum);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) == 1;
        }
    }

    public synchronized void addNote(int testNum, String author, String content) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO notes(test_num, author, content, created_at) VALUES (?,?,?,?)")) {
            ps.setInt(1, testNum);
            ps.setString(2, author);
            ps.setString(3, content);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    public synchronized List<Note> notes(int testNum) throws SQLException {
        List<Note> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT author, content, created_at FROM notes WHERE test_num = ? ORDER BY id")) {
            ps.setInt(1, testNum);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Note(rs.getString(1), rs.getString(2), rs.getLong(3)));
            }
        }
        return list;
    }

    public synchronized void clearAll() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM messages");
            st.execute("DELETE FROM votes");
            st.execute("DELETE FROM retest");
            st.execute("DELETE FROM notes");
        }
    }

    @Override
    public synchronized void close() throws SQLException {
        conn.close();
    }
}
