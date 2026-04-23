package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.dao.AdminDAO;
import com.zaxxer.hikari.HikariDataSource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminLoginUI extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnBack;
    private HikariDataSource dataSource;
    
    private final Color FIGMA_BG = new Color(30, 30, 30);
    private final Color FIGMA_GREEN = new Color(0, 163, 65);
    private final Color FIELD_BG = Color.BLACK;

    public AdminLoginUI(HikariDataSource dataSource) {
        this.dataSource = dataSource;
        
        setTitle("STAFF PORTAL");
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(FIGMA_BG);
        add(mainPanel);

        // --- HEADER ---
        JLabel lblHeader = new JLabel(" STAFF PORTAL", SwingConstants.CENTER); 
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(FIGMA_GREEN);
        lblHeader.setBounds(0, 50, 450, 40);
        mainPanel.add(lblHeader);

        // --- USERNAME ---
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(FIGMA_GREEN);
        lblUser.setBounds(75, 130, 100, 20);
        mainPanel.add(lblUser);

        txtUsername = new JTextField();
        styleField(txtUsername);
        txtUsername.setBounds(75, 155, 300, 45);
        mainPanel.add(txtUsername);

        // --- PASSWORD ---
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(FIGMA_GREEN);
        lblPass.setBounds(75, 220, 100, 20);
        mainPanel.add(lblPass);

        txtPassword = new JPasswordField();
        styleField(txtPassword);
        txtPassword.setBounds(75, 245, 300, 45);
        mainPanel.add(txtPassword);

        // --- LOGIN BUTTON ---
        btnLogin = new JButton("LOGIN");
        btnLogin.setBounds(75, 340, 300, 55);
        btnLogin.setBackground(FIGMA_GREEN);
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnLogin);

        // --- BACK ---
        btnBack = new JButton("< Back to Kiosk");
        btnBack.setBounds(150, 420, 150, 30);
        btnBack.setForeground(FIGMA_GREEN);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnBack);
        
        // --- FOOTER ---
        JLabel lblFooter = new JLabel("SSH-2.0-OpenSSH_8.9             [ENCRYPTED]");
        lblFooter.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lblFooter.setForeground(new Color(0, 100, 40)); 
        lblFooter.setBounds(75, 520, 350, 20);
        mainPanel.add(lblFooter);

        setupEvents();
    }

    private void styleField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(FIGMA_GREEN);
        field.setCaretColor(FIGMA_GREEN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }

    private void setupEvents() {
        // Hover effect for the login button
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogin.setBackground(new Color(0, 200, 80)); }
            public void mouseExited(MouseEvent e) { btnLogin.setBackground(FIGMA_GREEN); }
        });

        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Entry Denied: Username and Password required.", 
                    "System Validation", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            AdminDAO adminDAO = new AdminDAO(dataSource);
            
            if (adminDAO.authenticate(user, pass)) {
                // SUCCESS MOCKUP
                showSuccessMockup(user);
                
                // Redirect back to Kiosk for now since Dashboard is pending
                new IdentifyFerryUI(dataSource).setVisible(true);
                this.dispose();
            } else {
                // FAILURE
                JOptionPane.showMessageDialog(this, 
                    "CRITICAL ERROR: Invalid Credentials.\nAccess attempt logged.", 
                    "Authentication Failed", 
                    JOptionPane.ERROR_MESSAGE);
                txtPassword.setText(""); 
            }
        });

        btnBack.addActionListener(e -> {
            new IdentifyFerryUI(dataSource).setVisible(true); 
            this.dispose();
        });
    }

    private void showSuccessMockup(String user) {
        // Temporary UI tweak for terminal look in the Dialog
        UIManager.put("OptionPane.background", Color.BLACK);
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("OptionPane.messageForeground", FIGMA_GREEN);

        JOptionPane.showMessageDialog(this, 
            "--- AUTHENTICATION SUCCESSFUL ---\n\n" +
            "ID: " + user.toUpperCase() + "\n" +
            "PERMISSIONS: ALL_ACCESS\n" +
            "ENCRYPTION: AES-256 ACTIVE\n\n" +
            "Redirecting to Admin Kiosk...", 
            "SSH Access Granted", 
            JOptionPane.INFORMATION_MESSAGE);
            
        // Reset UIManager to default for other screens
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageForeground", null);
    }
}