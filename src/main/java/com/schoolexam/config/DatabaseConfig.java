package com.schoolexam.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static String dbUrl = "jdbc:sqlite:school_exam_java.db";

    public static void setDbPath(String dbPath) {
        dbUrl = "jdbc:sqlite:" + dbPath;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found", e);
        }
        return DriverManager.getConnection(dbUrl);
    }
}
