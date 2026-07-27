package com.schoolexam.dao;

import com.schoolexam.config.DatabaseConfig;
import com.schoolexam.model.LlmConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LlmConfigDao {

    public List<LlmConfig> findAll() {
        List<LlmConfig> list = new ArrayList<>();
        String sql = "SELECT * FROM llm_configs ORDER BY id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapConfig(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public LlmConfig findByProviderKey(String key) {
        String sql = "SELECT * FROM llm_configs WHERE provider_key = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapConfig(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LlmConfig getDefaultConfig() {
        String sql = "SELECT * FROM llm_configs WHERE is_default = 1 LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return mapConfig(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Fallback to first active
        List<LlmConfig> all = findAll();
        return all.isEmpty() ? null : all.get(0);
    }

    public boolean setDefaultProvider(String providerKey) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement resetStmt = conn.createStatement()) {
                resetStmt.executeUpdate("UPDATE llm_configs SET is_default = 0");
            }
            try (PreparedStatement setStmt = conn.prepareStatement("UPDATE llm_configs SET is_default = 1 WHERE provider_key = ?")) {
                setStmt.setString(1, providerKey);
                int updated = setStmt.executeUpdate();
                conn.commit();
                return updated > 0;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateApiKey(String providerKey, String apiKey) {
        String sql = "UPDATE llm_configs SET api_key = ?, updated_at = CURRENT_TIMESTAMP WHERE provider_key = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, apiKey);
            ps.setString(2, providerKey);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private LlmConfig mapConfig(ResultSet rs) throws SQLException {
        LlmConfig cfg = new LlmConfig();
        cfg.setId(rs.getLong("id"));
        cfg.setProviderKey(rs.getString("provider_key"));
        cfg.setProviderName(rs.getString("provider_name"));
        cfg.setApiKey(rs.getString("api_key"));
        cfg.setApiEndpoint(rs.getString("api_endpoint"));
        cfg.setIsDefault(rs.getInt("is_default") == 1);
        cfg.setIsActive(rs.getInt("is_active") == 1);
        return cfg;
    }
}
