package com.johnson.coustomfarm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;


public class Database {
    private static final String URL = "jdbc:sqlite:plugins/CustomFarm/farming_mining.db";

    public Database() {
        createTables();
    }

    private void createTables() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS player_skills (" +
                    "uuid TEXT PRIMARY KEY," +
                    "mining_skill INTEGER DEFAULT 0," +
                    "farming_skill INTEGER DEFAULT 0)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void addPlayer(UUID uuid) {
        String sql = "INSERT OR IGNORE INTO player_skills(uuid) VALUES(?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMiningSkill(UUID uuid) {
        String sql = "SELECT mining_skill FROM player_skills WHERE uuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("mining_skill");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getFarmingSkill(UUID uuid) {
        String sql = "SELECT farming_skill FROM player_skills WHERE uuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("farming_skill");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void increaseMiningSkill(UUID uuid) {
        String sql = "UPDATE player_skills SET mining_skill = mining_skill + 1 WHERE uuid = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void increaseFarmingSkill(UUID uuid) {
        String sql = "UPDATE player_skills SET farming_skill = farming_skill + 1 WHERE uuid = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
