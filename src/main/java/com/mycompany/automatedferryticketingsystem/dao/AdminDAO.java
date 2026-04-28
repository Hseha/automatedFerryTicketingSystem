package com.mycompany.automatedferryticketingsystem.dao;

import com.zaxxer.hikari.HikariDataSource;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

/**
 * [ABSTRACTION] - Kini nga class nagsilbing bridge aron itago ang komplikadong 
 * SQL queries. Ang UI motawag lang og authenticate() nga wala na mahibalo 
 * giunsa pag-verify ang password sa luyo.
 */
public class AdminDAO {
    
    // [ENCAPSULATION] - Gi-private ang dataSource aron masiguro nga ang 
    // database connection dili ma-manipulate gikan sa gawas niini nga class.
    private final HikariDataSource dataSource;

    public AdminDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * [SECURITY LOGIC]
     * Sugdan sa pag-check sa username, dayon i-verify ang encrypted password.
     */
    public boolean authenticate(String username, String plainPassword) {
        
        // Defensive Check: 
        // I-filter daan ang null o empty inputs para dili mausik ang resources sa DB.
        if (username == null || plainPassword == null || username.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT password_hash FROM admins WHERE username = ?";
        
        // Resource Management (Try-with-resources):
        // Siguroon nga ma-close ang Connection ug PreparedStatement para dili ma-leak ang memory.
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // I-bind ang username sa query aron malikayan ang SQL Injection attacks.
            pstmt.setString(1, username.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashed = rs.getString("password_hash");
                    
                    /**
                     * [BCRYPT LOGIC]
                     * Dili nato i-compare ang password directly (plain text). 
                     * Gamiton ang BCrypt para i-check kon ang gi-input nga password 
                     * ni-match ba sa encrypted string sa database.
                     */
                    return BCrypt.checkpw(plainPassword, hashed);
                }
            }
        } catch (SQLException e) {
            System.err.println("DATABASE ERROR during Admin Auth: " + e.getMessage());
        }
        
        return false;
    }
}