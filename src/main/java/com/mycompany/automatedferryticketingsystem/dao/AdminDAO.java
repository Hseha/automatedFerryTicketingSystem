package com.mycompany.automatedferryticketingsystem.dao;

import com.zaxxer.hikari.HikariDataSource;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class AdminDAO {
    private final HikariDataSource dataSource;

    /**
     * Constructor that accepts the project's connection pool.
     * Reusing connections helps stay within the 512MB RAM limit.
     */
    public AdminDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Authenticates a staff member using BCrypt hashing.
     * @param username The entered username
     * @param plainPassword The raw password from the JPasswordField
     * @return true if credentials match, false otherwise
     */
    public boolean authenticate(String username, String plainPassword) {
        String sql = "SELECT password_hash FROM admins WHERE username = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashed = rs.getString("password_hash");
                    
                    // BCrypt.checkpw handles the salt and comparison logic
                    return BCrypt.checkpw(plainPassword, hashed);
                }
            }
        } catch (SQLException e) {
            // Logs error for system analysis without crashing the UI
            System.err.println("Authentication error: " + e.getMessage());
        }
        return false;
    }
}