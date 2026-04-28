package com.mycompany.automatedferryticketingsystem.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * CORE LOGIC: Database Connection Pool (HikariCP).
 * Kini ang "kasing-kasing" sa system; kini ang nagdumala sa trapiko sa data.
 */
public class DBConnection {
    
    // Static datasource para usa ra ka pool ang i-share sa tibuok application.
    private static HikariDataSource dataSource = null;
    
    static {
        try {
            // 1. Dynamic Credential Fetching:
            // Kuhaon ang login info gikan sa JVM flags aron dili ma-hardcode (secure approach).
            String url = System.getProperty("DB_URL");
            String user = System.getProperty("DB_USER");
            String pass = System.getProperty("DB_PASS");
            
            // 2. Fatal Error Validation:
            // Siguroon nga naay credentials; if wala, dili mo-proceed ang app.
            if (url == null || user == null || pass == null) {
                System.err.println("CRITICAL: Database properties missing!");
            } else {
                // 3. HikariCP Engine Configuration:
                // Setup sa high-speed connection engine para sa MariaDB/MySQL.
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(url);
                config.setUsername(user);
                config.setPassword(pass);
                config.setDriverClassName("org.mariadb.jdbc.Driver");
                
                // 4. Performance Tuning:
                // Max 20 concurrent connections; dili mo-crash maski daghan ang dungan mo-login.
                config.setMaximumPoolSize(20);      
                config.setMinimumIdle(5);           // Standby connections para paspas ang response.
                config.setConnectionTimeout(30000); // 30s max wait time before timeout.
                config.setPoolName("FerryTicketPool");
                
                // 5. Query Optimization:
                // I-cache ang prepared statements para dili sige'g re-compile ang SQL logic.
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                
                dataSource = new HikariDataSource(config);
            }
        } catch (Exception e) {
            // Error Handling: Catch-all if naay failure sa initialization.
            System.err.println("DATABASE INIT ERROR: " + e.getMessage());
            dataSource = null; 
        }
    }

    /**
     * CONNECTION GATEWAY:
     * Tawagon kini sa mga DAO aron maka-access sa database.
     * Efficient ni kay recycled connections ang ihatag, dili sige'g open/close.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Pool not initialized. Check DB properties.");
        }
        return dataSource.getConnection();
    }
    
    // Helper to pass the pool to views/controllers if needed
    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}