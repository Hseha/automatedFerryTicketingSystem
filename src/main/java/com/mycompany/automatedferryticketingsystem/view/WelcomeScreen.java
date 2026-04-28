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
 * [INHERITANCE] - Kini nga class naggamit sa JFrame aron mahimong main landing page 
 * sa imong application.
 * [ABSTRACTION] - Gi-hide niini ang komplikadong proseso sa pag-setup sa HikariCP 
 * connection pool ug background threading gikan sa user.
 */
public class WelcomeScreen extends JFrame {
    
    // [ENCAPSULATION] - Gi-protektahan ang system state ug data source 
    // aron dili direktang ma-access sa ubang classes.
    private boolean isSystemOnline = false; 
    private HikariDataSource dataSource; 
    private final JLabel statusLabel = new JLabel(" ● Initializing...");

    public WelcomeScreen() {
        // [RESOURCE INITIALIZATION]
        initConnectionPool();

        setTitle("Automated Ferry Ticketing System | Welcome");
        setSize(1000, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setResizable(false);

        // [RESOURCE MANAGEMENT] - Graceful Shutdown Logic.
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
                
                // Visual Logic: Gradient background.
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 44, 52), 0, getHeight(), new Color(20, 22, 26));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

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

        JLabel startLabel = new JLabel("CLICK ANYWHERE TO BEGIN");
        startLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        startLabel.setForeground(new Color(150, 150, 150));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 80, 0);
        mainPanel.add(startLabel, gbc);

        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(statusLabel, gbc);

        // [INTERACTION LOGIC]
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isSystemOnline) {
                    VesselDAO dao = new VesselDAO(dataSource); 
                    new IdentifyFerryUI(dao).setVisible(true);
                    dispose(); 
                } else {
                    handleConnectionRetry();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                startLabel.setForeground(new Color(52, 152, 219));
                startLabel.setText("> CLICK ANYWHERE TO BEGIN <");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                startLabel.setForeground(new Color(150, 150, 150));
                startLabel.setText("CLICK ANYWHERE TO BEGIN");
            }
        });

        add(mainPanel);
        checkStatus(); 
    }

    /**
     * [DATABASE LOGIC] - Setup sa HikariCP.
     * Kini nga method nag-encapsulate sa configuration sa database pooling.
     */
    private void initConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Hardcoded fallback for local development (MariaDB).
            String url = System.getProperty("DB_URL", "jdbc:mariadb://localhost:3306/ferry_db");
            String user = System.getProperty("DB_USER", "Michael");
            String pass = System.getProperty("DB_PASS", "your_password");

            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(pass);
            
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
     * [THREADING LOGIC] 
     * Gigamit ang background thread aron dili mo-freeze ang UI samtang 
     * nag-check sa connection status sa database.
     */
    private void checkStatus() {
        new Thread(() -> {
            if (dataSource == null) {
                updateStatusUI(" ● SYSTEM OFFLINE (CONFIG ERROR)", new Color(231, 76, 60), false);
                return;
            }

            try (Connection conn = dataSource.getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    updateStatusUI(" ● SYSTEM ONLINE | DATABASE CONNECTED", new Color(46, 204, 113), true);
                }
            } catch (Exception ex) {
                updateStatusUI(" ● SYSTEM OFFLINE (CONNECTION FAILED)", new Color(231, 76, 60), false);
            }
        }).start();
    }

    private void updateStatusUI(String text, Color color, boolean online) {
        // SwingUtilities.invokeLater siguroon nga ang UI updates mahitabo sa EDT thread.
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
            statusLabel.setForeground(color);
            this.isSystemOnline = online;
        });
    }

    private void handleConnectionRetry() {
        statusLabel.setText(" ● RETRYING CONNECTION...");
        statusLabel.setForeground(Color.YELLOW);
        checkStatus();
    }

    public static void main(String[] args) {
        try {
            // Logic: Gamit og Nimbus L&F para modern tan-awon sa Linux/Ubuntu.
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        }
        
        SwingUtilities.invokeLater(() -> new WelcomeScreen().setVisible(true));
    }
}