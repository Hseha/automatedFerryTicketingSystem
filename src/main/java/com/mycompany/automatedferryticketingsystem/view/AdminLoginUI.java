package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.dao.AdminDAO;
import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.mycompany.automatedferryticketingsystem.controller.VesselController; 
import com.zaxxer.hikari.HikariDataSource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * CORE LOGIC: Staff Login portal.
 * Handles authentication and navigation between the public Kiosk and Admin Dashboard.
 */
public class AdminLoginUI extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnBack;
    private HikariDataSource dataSource;
    private VesselDAO dao; 
    
    private final Color FIGMA_BG = new Color(30, 30, 30);
    private final Color FIGMA_GREEN = new Color(0, 163, 65);
    private final Color FIELD_BG = Color.BLACK;

    public AdminLoginUI(VesselDAO dao) {
        this.dao = dao;
        // Data Integrity: Ensure dataSource exists for database calls
        this.dataSource = (dao != null) ? dao.getDataSource() : null; 
        
        initUI();
        setupEvents();
    }

    // UI Logic: Manual layout positioning to match a specific "Figma-style" dark theme
    private void initUI() {
        setTitle("STAFF PORTAL");
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(FIGMA_BG);
        add(mainPanel);

        JLabel lblHeader = new JLabel("STAFF PORTAL", SwingConstants.CENTER); 
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(FIGMA_GREEN);
        lblHeader.setBounds(0, 50, 450, 40);
        mainPanel.add(lblHeader);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(FIGMA_GREEN);
        lblUser.setBounds(75, 130, 100, 20);
        mainPanel.add(lblUser);

        txtUsername = new JTextField();
        styleField(txtUsername);
        txtUsername.setBounds(75, 155, 300, 45);
        mainPanel.add(txtUsername);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(FIGMA_GREEN);
        lblPass.setBounds(75, 220, 100, 20);
        mainPanel.add(lblPass);

        txtPassword = new JPasswordField();
        styleField(txtPassword);
        txtPassword.setBounds(75, 245, 300, 45);
        mainPanel.add(txtPassword);

        btnLogin = new JButton("LOGIN");
        btnLogin.setBounds(75, 340, 300, 55);
        btnLogin.setBackground(FIGMA_GREEN);
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnLogin);

        btnBack = new JButton("< Back to Kiosk");
        btnBack.setBounds(150, 420, 150, 30);
        btnBack.setForeground(FIGMA_GREEN);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnBack);
    }

    // Styling Logic: Applies consistent dark-mode aesthetic to input fields
    private void styleField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(FIGMA_GREEN);
        field.setCaretColor(FIGMA_GREEN);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }

    private void setupEvents() {
        // Accessibility Logic: Triggers login when user presses Enter key
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnLogin.doClick();
            }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);

        // Authentication Logic: Validates credentials via AdminDAO
        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password required.");
                return;
            }

            AdminDAO adminDAO = new AdminDAO(dataSource);
            if (adminDAO.authenticate(user, pass)) {
                showSuccessMockup(user);
                new FerryAdminDashboard(this.dao).setVisible(true); // Open Admin Hub
                this.dispose(); // Close login screen
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText(""); 
            }
        });

        // Navigation Logic: Re-initializes the public view and its controller
        btnBack.addActionListener(e -> {
            IdentifyFerryUI kioskView = new IdentifyFerryUI(this.dao);
            new VesselController(kioskView, this.dao, this.dataSource);
            kioskView.setVisible(true);
            this.dispose();
        });
    }

    private void showSuccessMockup(String user) {
        JOptionPane.showMessageDialog(this, "ACCESS GRANTED\nID: " + user.toUpperCase());
    }
}