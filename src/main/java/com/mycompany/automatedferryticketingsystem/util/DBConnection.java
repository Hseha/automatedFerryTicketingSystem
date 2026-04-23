package com.mycompany.automatedferryticketingsystem.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    
    // The pool that manages database connections
    private static HikariDataSource dataSource = null;
    
    static {
        try {
            // 1. Get credentials from NetBeans settings (-D flags)
            // Kuhaon ang login info para dili ma-hardcode sa code.
            String url = System.getProperty("DB_URL");
            String user = System.getProperty("DB_USER");
            String pass = System.getProperty("DB_PASS");
            
            // 2. Safety Check: Stop if flags are missing
            if (url == null || user == null || pass == null) {
                System.err.println("FATAL: Database properties not set!");
            } else {
                // 3. Setup HikariCP (High-speed connection pool)
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(url);
                config.setUsername(user);
                config.setPassword(pass);
                config.setDriverClassName("org.mariadb.jdbc.Driver");
                
                // Settings to make the system fast and stable
                config.setMaximumPoolSize(20);      // Max 20 concurrent users
                config.setMinimumIdle(5);           // 5 connections ready anytime
                config.setConnectionTimeout(30000); // 30s wait time
                config.setPoolName("FerryTicketPool");
                
                // Speed optimizations for MariaDB
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                
                dataSource = new HikariDataSource(config);
            }
        } catch (Exception e) {
            System.err.println("HikariCP Init Failed: " + e.getMessage());
            dataSource = null; 
        }
    }

    // Call this in your DAO to get a connection
    // Tawagon kini para makasulod sa database.
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not connected.");
        }
        return dataSource.getConnection();
    }
}
