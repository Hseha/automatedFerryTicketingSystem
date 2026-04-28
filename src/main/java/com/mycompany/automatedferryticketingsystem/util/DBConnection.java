package com.mycompany.automatedferryticketingsystem.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * LOGIC EXPLANATION:
 * Kini ang silbing hearth sa system; kini ang nagdumala sa trapiko sa data.
 * It uses the Singleton-like pattern para usa ra ka connection pool ang i-share.
 * Bali, efficient ang performance kay dili sige'g create og bag-ong connection.
 */

// // Encapsulation: Access to the dataSource is controlled via static methods.
public class DBConnection {
    
    // // Encapsulation: Private static field para dili ma-access directly outside.
    private static HikariDataSource dataSource = null;
    
    static {
        try {
            // // Abstraction: Getting credentials from System properties instead of hardcoding.
            String url = System.getProperty("DB_URL");
            String user = System.getProperty("DB_USER");
            String pass = System.getProperty("DB_PASS");
            
            if (url == null || user == null || pass == null) {
                System.err.println("CRITICAL: Database properties missing!");
            } else {
                // // Composition: DBConnection "Has-A" HikariConfig to set up the engine.
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(url);
                config.setUsername(user);
                config.setPassword(pass);
                config.setDriverClassName("org.mariadb.jdbc.Driver");
                
                config.setMaximumPoolSize(20);      
                config.setMinimumIdle(5);           
                config.setConnectionTimeout(30000); 
                config.setPoolName("FerryTicketPool");
                
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                
                // // Composition: Initializing the dataSource object.
                dataSource = new HikariDataSource(config);
            }
        } catch (Exception e) {
            System.err.println("DATABASE INIT ERROR: " + e.getMessage());
            dataSource = null; 
        }
    }

    /**
     * // Abstraction: The rest of the app just calls this to get a connection.
     * Wala na sila kabalo giunsa pag-manage ang pool sa sulod.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Pool not initialized. Check DB properties.");
        }
        return dataSource.getConnection();
    }
    
    // // Encapsulation: Public getter to safely provide the dataSource.
    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
