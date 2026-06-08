package com.homedecor.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the JDBC database connection.
 *
 * ── R6: JDBC ─────────────────────────────────────────────────────────────
 * JDBC (Java Database Connectivity) is the standard Java API for talking
 * to relational databases. It lets you send SQL queries from Java code.
 *
 * ── R9: Singleton Design Pattern ─────────────────────────────────────────
 * We only ever want ONE database connection object in our app.
 * The Singleton pattern guarantees that — only one instance can exist.
 *
 * How Singleton works:
 *   1. Constructor is private → nobody outside can call 'new DatabaseConnection()'
 *   2. A static field holds the one and only instance
 *   3. getInstance() creates it on first call, then returns the same one forever
 *
 * ── Configuration ─────────────────────────────────────────────────────────
 * To use this, you need MySQL running locally and a database called 'homedecor'.
 * SQL to create it:
 *   CREATE DATABASE homedecor;
 *   CREATE USER 'homedecor_user'@'localhost' IDENTIFIED BY 'password123';
 *   GRANT ALL PRIVILEGES ON homedecor.* TO 'homedecor_user'@'localhost';
 */
public class DatabaseConnection {

    // ── Singleton: the one and only instance ──────────────────────────────────
    private static DatabaseConnection instance;

    // ── The actual JDBC connection object ────────────────────────────────────
    private Connection connection;

    // ── Database credentials (in a real app, put these in a config file!) ────
    private static final String URL      = "jdbc:mysql://localhost:3306/homedecor";
    private static final String USER     = "homedecor_user";
    private static final String PASSWORD = "password123";

    // ── Private constructor — prevents external instantiation ─────────────────
    /**
     * Opens the JDBC connection when the singleton is first created.
     * Private so only getInstance() can call it.
     */
    private DatabaseConnection() {
        try {
            // DriverManager.getConnection() opens a physical TCP connection
            // to the MySQL server and returns a Connection object.
            // This Connection is reused for all queries.
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connection established to: " + URL);
        } catch (SQLException e) {
            // Wrap in RuntimeException so callers don't need to catch SQLException
            // in every method just to get a connection.
            throw new RuntimeException("[DB] Failed to connect to database: " + e.getMessage(), e);
        }
    }

    // ── Singleton accessor ────────────────────────────────────────────────────
    /**
     * Returns the single shared instance.
     * Creates it on the first call; returns the same one on every subsequent call.
     *
     * 'synchronized' makes this thread-safe — if two threads call getInstance()
     * at exactly the same time, only one will create the connection.
     *
     * @return the shared DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns the raw JDBC Connection used to create Statements and run queries.
     *
     * Usage:
     *   Connection conn = DatabaseConnection.getInstance().getConnection();
     *   PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products");
     *
     * @return the active JDBC Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes the database connection cleanly.
     * Call this when your application shuts down.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
