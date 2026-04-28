package com.mycompany.automatedferryticketingsystem.dao;

import com.zaxxer.hikari.HikariDataSource;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

/**
 * CORE LOGIC: Data Access Object (DAO) for Admin Authentication.
 * Kini ang layer nga direktang nakig-istorya sa 'admins' table sa MariaDB.
 */
public class AdminDAO {
    private final HikariDataSource dataSource;

    // Constructor: Dawaton ang connection pool para maka-execute og SQL queries.
    public AdminDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * AUTHENTICATION LOGIC:
     * Sugdan sa pag-check sa username, dayon i-verify ang encrypted password.
     */
    public boolean authenticate(String username, String plainPassword) {
        
        // 1. Defensive Check: 
        // I-filter daan ang null o empty inputs para dili mausik ang resources sa DB.
        if (username == null || plainPassword == null || username.trim().isEmpty()) {
            return false;
        }

        // SQL Query: Kuhaon lang ang hashed password base sa username.
        String sql = "SELECT password_hash FROM admins WHERE username = ?";
        
        // 2. Resource Management (Try-with-resources):
        // Siguroon nga ma-close ang Connection ug PreparedStatement para dili ma-leak ang memory.
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // I-bind ang username sa query aron malikayan ang SQL Injection attacks.
            pstmt.setString(1, username.trim());
            
            // 3. Execution & Result Handling:
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Kuhaon ang 'scrambled' o hashed password gikan sa database record.
                    String hashed = rs.getString("password_hash");
                    
                    // 4. BCrypt Security Logic:
                    // Dili nato i-compare ang password directly (plain text). 
                    // Gamiton ang BCrypt para i-check kon ang gi-input nga pass ni Michael 
                    // ni-match ba sa encrypted string sa database.
                    return BCrypt.checkpw(plainPassword, hashed);
                }
            }
        } catch (SQLException e) {
            // 5. Error Logging: 
            // Mo-print ni sa terminal kon naay problema sa database (e.g. Table doesn't exist).
            System.err.println("DATABASE ERROR during Admin Auth: " + e.getMessage());
        }
        
        // Default return: If wala makit-i ang user o naay error, 'false' ang i-return (Access Denied).
        return false;
    }
}