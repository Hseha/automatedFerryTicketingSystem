package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;

/**
 * [LOGIC OVERVIEW]
 * 1. DATABASE POOLING: Gigamit ang HikariCP para paspas ug efficient ang database connections.
 * 2. SYSTEM READINESS: Gi-check ang connection status sa background thread para dili mo-freeze ang UI.
 * 3. INTERACTIVE UI: Click-anywhere logic para sa transition ngadto sa main ticketing flow.
 * 4. RESOURCE MANAGEMENT: Naay "Graceful Shutdown" logic para ma-close og tarong ang connection pool.
 */
public class WelcomeScreen extends JFrame {
    private boolean isSystemOnline = false; // Flag para i-check kung ready na ba ang system.
    private HikariDataSource dataSource; 
    private final JLabel statusLabel = new JLabel(" ● Initializing...");

    public WelcomeScreen() {
        // Step 1: Initialize ang Connection Pool sa pagsugod palang.
        initConnectionPool();

        // Step 2: Main Window Configuration.
        setTitle("Automated Ferry Ticketing System | Welcome");
        setSize(1000, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // I-center ang window sa screen.
        setResizable(false);

        // Step 3: Shutdown Hook. Logic para masiguro nga ma-close ang database connections 
        // inig exit sa user para malikayan ang memory leaks.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (dataSource != null) {
                    System.out.println("[System] Closing database pool...");
                    dataSource.close();
                }
            }
        });

        // UI CONSTRUCTION: Custom painting para sa modern charcoal gradient look.
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Logic: Pag-apply sa gradient background gikan sa light charcoal padung dark.
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 44, 52), 0, getHeight(), new Color(20, 22, 26));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        // Branding Labels: Main title ug subtitles sa system.
        JLabel welcomeLabel = new JLabel("AUTOMATED FERRY TICKETING");
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 36)); 
        welcomeLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(welcomeLabel, gbc);

        JLabel subLabel = new JLabel("Safe. Efficient. Reliable.");
        subLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        subLabel.setForeground(new Color(110, 118, 129));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 50, 0);
        mainPanel.add(subLabel, gbc);

        // Instruction Text: Naghatag og visual cue sa user kung unsaon pag-start.
        JLabel startLabel = new JLabel("CLICK ANYWHERE TO BEGIN");
        startLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        startLabel.setForeground(new Color(150, 150, 150));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 80, 0);
        mainPanel.add(startLabel, gbc);

        // Bottom Status: Real-time feedback para sa connection health.
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(statusLabel, gbc);

        // --- INTERACTION LOGIC ---
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Logic: Kung ONLINE na ang database, mo-proceed sa next screen.
                // Kung OFFLINE, mo-retry og connect.
                if (isSystemOnline) {
                    VesselDAO dao = new VesselDAO(dataSource); 
                    new IdentifyFerryUI(dao).setVisible(true);
                    dispose(); // Close ang welcome screen.
                } else {
                    handleConnectionRetry();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Visual Logic: Highlight effect inig hover sa mouse.
                startLabel.setForeground(new Color(52, 152, 219));
                startLabel.setText("> CLICK ANYWHERE TO BEGIN <");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Reset visual effect inig gawas sa mouse.
                startLabel.setForeground(new Color(150, 150, 150));
                startLabel.setText("CLICK ANYWHERE TO BEGIN");
            }
        });

        add(mainPanel);
        checkStatus(); // Logic: Start checking the DB connection health immediately.
    }

    /**
     * DATABASE LOGIC: Setup sa HikariCP connection pool parameters.
     */
    private void initConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Logic: Priority ang System Properties, but naay hardcoded fallback for local dev.
            String url = System.getProperty("DB_URL", "jdbc:mariadb://localhost:3306/ferry_db");
            String user = System.getProperty("DB_USER", "Michael");
            String pass = System.getProperty("DB_PASS", "your_password");

            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(pass);
            
            // Performance Tuning: Setup pool limits ug timeout para dili mag-hang ang system.
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(3000); 
            config.setIdleTimeout(60000);
            config.setPoolName("FerrySystemPool");
            
            this.dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("Database Pool Initialization Failed: " + e.getMessage());
        }
    }

    /**
     * THREADING LOGIC: Mo-check sa connection gamit ang background thread.
     * Importante ni para dili mo-freeze ang UI samtang naghulat sa response sa database.
     */
    private void checkStatus() {
        new Thread(() -> {
            if (dataSource == null) {
                updateStatusUI(" ● SYSTEM OFFLINE (CONFIGURATION ERROR)", new Color(231, 76, 60), false);
                return;
            }

            try (Connection conn = dataSource.getConnection()) {
                // Logic: Inig makakuha og valid connection, i-set ang system to ONLINE.
                if (conn != null && !conn.isClosed()) {
                    updateStatusUI(" ● SYSTEM ONLINE | DATABASE CONNECTED", new Color(46, 204, 113), true);
                }
            } catch (Exception ex) {
                // Logic: Handle errors sama sa wrong password o offline nga server.
                updateStatusUI(" ● SYSTEM OFFLINE (CONNECTION FAILED)", new Color(231, 76, 60), false);
            }
        }).start();
    }

    /**
     * UI THREAD LOGIC: Gigamit ang SwingUtilities.invokeLater para safe i-update 
     * ang components gikan sa background thread.
     */
    private void updateStatusUI(String text, Color color, boolean online) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
            statusLabel.setForeground(color);
            this.isSystemOnline = online;
        });
    }

    /**
     * RETRY LOGIC: I-reset ang status ug i-re-run ang connection check.
     */
    private void handleConnectionRetry() {
        statusLabel.setText(" ● RETRYING CONNECTION...");
        statusLabel.setForeground(Color.YELLOW);
        checkStatus();
    }

    /**
     * MAIN ENTRY POINT: Setup sa Look and Feel para nindot ang display sa Linux/Ubuntu.
     */
    public static void main(String[] args) {
        try {
            // Logic: Gamit og Nimbus L&F para modern tan-awon sa tanan OS.
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Fallback sa default system skin kung dili available ang Nimbus.
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        }
        
        // Logic: Siguraduhon nga ang UI ma-launch sa Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> new WelcomeScreen().setVisible(true));
    }
}