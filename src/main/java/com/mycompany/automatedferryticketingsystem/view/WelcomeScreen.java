package com.mycompany.automatedferryticketingsystem.view;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;

public class WelcomeScreen extends JFrame {
    private boolean isSystemOnline = false;
    private HikariDataSource dataSource; // The connection pool "baton"

    public WelcomeScreen() {
        // 1. Initialize the connection pool first
        initConnectionPool();

        setTitle("Automated Ferry Ticketing System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // --- Custom Panel with Gradient and Anti-Aliasing ---
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(0, 0, new Color(45, 45, 48), 0, getHeight(), new Color(25, 25, 28));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        GridBagConstraints gbc = new GridBagConstraints();

        // Welcome Text
        JLabel welcomeLabel = new JLabel("WELCOME TO AUTOMATED FERRY TICKETING SYSTEM");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28)); 
        welcomeLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(welcomeLabel, gbc);

        // Instruction Text (With Hover Effect)
        JLabel startLabel = new JLabel("Click anywhere to start booking");
        startLabel.setFont(new Font("Segoe UI Semilight", Font.PLAIN, 18));
        startLabel.setForeground(new Color(150, 150, 150));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 60, 0);
        mainPanel.add(startLabel, gbc);

        // System Status
        JLabel statusLabel = new JLabel(" ● Checking System...");
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        statusLabel.setForeground(Color.GRAY);
        gbc.gridy = 2;
        mainPanel.add(statusLabel, gbc);

        // Run the status check using the new pool
        checkStatus(statusLabel);

        // --- Mouse Interaction Feedback ---
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isSystemOnline) {
                    // FIX: Passing the dataSource to IdentifyFerryUI
                    new IdentifyFerryUI(dataSource).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "System is currently OFFLINE. Please check your database connection.", 
                        "Connection Required", JOptionPane.WARNING_MESSAGE);
                    checkStatus(statusLabel);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                startLabel.setForeground(Color.WHITE); 
            }

            @Override
            public void mouseExited(MouseEvent e) {
                startLabel.setForeground(new Color(150, 150, 150)); 
            }
        });

        add(mainPanel);
    }

    private void initConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();
            // Pulling from your Maven VM Arguments (-DDB_URL, etc.)
            config.setJdbcUrl(System.getProperty("DB_URL"));
            config.setUsername(System.getProperty("DB_USER"));
            config.setPassword(System.getProperty("DB_PASS"));
            
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(5000); // 5 seconds timeout
            
            this.dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("Pool Initialization Failed: " + e.getMessage());
        }
    }

    private void checkStatus(JLabel label) {
        SwingUtilities.invokeLater(() -> {
            if (dataSource == null) {
                label.setText(" ● System Offline (Pool Error)");
                label.setForeground(new Color(231, 76, 60));
                isSystemOnline = false;
                return;
            }

            try (Connection conn = dataSource.getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    label.setText(" ● System Online");
                    label.setForeground(new Color(46, 204, 113)); 
                    isSystemOnline = true;
                }
            } catch (Exception ex) {
                label.setText(" ● System Offline (Click to retry)");
                label.setForeground(new Color(231, 76, 60)); 
                isSystemOnline = false;
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new WelcomeScreen().setVisible(true));
    }
}