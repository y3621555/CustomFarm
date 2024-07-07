package com.johnson.customfarm;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


public class Database {
    private static final String URL = "jdbc:sqlite:plugins/CustomFarm/farming_mining.db";

    public Database() {
        // 確保目錄存在
        File dbFile = new File("plugins/CustomFarm");
        if (!dbFile.exists()) {
            dbFile.mkdirs();
        }

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

    private void initializePlayer(UUID uuid) {
        addPlayer(uuid);
    }

    public int getMiningSkill(UUID uuid) {
        initializePlayer(uuid);
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
        initializePlayer(uuid);
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
        initializePlayer(uuid);
        String sql = "UPDATE player_skills SET mining_skill = mining_skill + 1 WHERE uuid = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void decreaseMiningSkill(UUID uuid) {
        initializePlayer(uuid);
        String sql = "UPDATE player_skills SET mining_skill = mining_skill - 1 WHERE uuid = ? AND mining_skill > 0";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void increaseFarmingSkill(UUID uuid) {
        initializePlayer(uuid);
        String sql = "UPDATE player_skills SET farming_skill = farming_skill + 1 WHERE uuid = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void decreaseFarmingSkill(UUID uuid) {
        initializePlayer(uuid);
        String sql = "UPDATE player_skills SET farming_skill = farming_skill - 1 WHERE uuid = ? AND farming_skill > 0";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map.Entry<UUID, Integer>> getTopMiningSkills(int limit) {
        String sql = "SELECT uuid, mining_skill FROM player_skills ORDER BY mining_skill DESC LIMIT ?";
        List<Map.Entry<UUID, Integer>> results = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                int miningSkill = rs.getInt("mining_skill");
                results.add(new AbstractMap.SimpleEntry<>(uuid, miningSkill));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<Map.Entry<UUID, Integer>> getTopFarmingSkills(int limit) {
        String sql = "SELECT uuid, farming_skill FROM player_skills ORDER BY farming_skill DESC LIMIT ?";
        List<Map.Entry<UUID, Integer>> results = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                int farmingSkill = rs.getInt("farming_skill");
                results.add(new AbstractMap.SimpleEntry<>(uuid, farmingSkill));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}
