package com.mycompany.automatedferryticketingsystem.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WelcomeScreen extends JFrame {
    private boolean isSystemOnline = false; // Track connection status

    public WelcomeScreen() {
        setTitle("Automated Ferry Ticketing System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(45, 45, 48));
        GridBagConstraints gbc = new GridBagConstraints();

        // Welcome Text
        JLabel welcomeLabel = new JLabel("WELCOME TO AUTOMATED FERRY TICKETING SYSTEM");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(welcomeLabel, gbc);

        // Instruction Text
        JLabel startLabel = new JLabel("Click anywhere to start booking");
        startLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        startLabel.setForeground(new Color(180, 180, 180));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 40, 0);
        mainPanel.add(startLabel, gbc);

        // System Status Logic
        JLabel statusLabel = new JLabel(" ● Checking System...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        gbc.gridy = 2;
        mainPanel.add(statusLabel, gbc);

        // Perform Initial Connection Check
        checkStatus(statusLabel);

        // --- THE FIX: Conditional Navigation Guard ---
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isSystemOnline) {
                    new IdentifyFerryUI().setVisible(true);
                    dispose();
                } else {
                    // Feedback for the user: Block transition and try reconnecting
                    JOptionPane.showMessageDialog(null, 
                        "System is currently OFFLINE. Please check your database connection before starting.", 
                        "Connection Required", 
                        JOptionPane.WARNING_MESSAGE);
                    
                    checkStatus(statusLabel); // Attempt to refresh status on click
                }
            }
        });

        add(mainPanel);
    }

    private void checkStatus(JLabel label) {
        SwingUtilities.invokeLater(() -> {
            try (java.sql.Connection conn = com.mycompany.automatedferryticketingsystem.dao.DBConnection.getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    label.setText(" ● System Online");
                    label.setForeground(new Color(0, 255, 127));
                    isSystemOnline = true;
                }
            } catch (Exception ex) {
                label.setText(" ● System Offline (Click to retry)");
                label.setForeground(new Color(255, 51, 51));
                isSystemOnline = false;
                System.err.println("Database check failed: " + ex.getMessage());
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