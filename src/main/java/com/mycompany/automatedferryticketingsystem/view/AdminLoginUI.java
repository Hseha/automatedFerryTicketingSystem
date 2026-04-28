package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.dao.AdminDAO;
import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.mycompany.automatedferryticketingsystem.controller.VesselController; 
import com.zaxxer.hikari.HikariDataSource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LOGIC EXPLANATION:
 * This class handles the Staff Login window. Gi-inherit niya ang traits sa JFrame 
 * para mahimong window, then gi-encapsulate ang fields para safe ang data. 
 * High-level abstraction gihapon ni kay wala kabalo ang UI giunsa pag-check 
 * ang password sa DB—basta kay valid ang login, mo-proceed ra siya.
 */

// // Inheritance
public class AdminLoginUI extends JFrame {
    
    // // Encapsulation
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnBack;
    
    // // Composition
    private HikariDataSource dataSource;
    private VesselDAO dao; 
    
    private final Color FIGMA_BG = new Color(30, 30, 30);
    private final Color FIGMA_GREEN = new Color(0, 163, 65);
    private final Color FIELD_BG = Color.BLACK;

    public AdminLoginUI(VesselDAO dao) {
        this.dao = dao;
        this.dataSource = (dao != null) ? dao.getDataSource() : null; 
        
        initUI();
        setupEvents();
    }

    /**
     * Ginahimo ani ang layout setup (size, colors, and positioning).
     * Bali, gi-set up ang aesthetics sa portal para nindot tan-awon sa user.
     */
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

    private void styleField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(FIGMA_GREEN);
        field.setCaretColor(FIGMA_GREEN);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }

    /**
     * Logic para sa event handling—basically namantay lang ni kung gi-click 
     * ang button o gi-press ang enter para mo-trigger ang login validation.
     */
    private void setupEvents() {
        // // Polymorphism
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnLogin.doClick();
            }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);

        // // Abstraction
        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password required.");
                return;
            }

            // // Composition
            AdminDAO adminDAO = new AdminDAO(dataSource);
            if (adminDAO.authenticate(user, pass)) {
                showSuccessMockup(user);
                new FerryAdminDashboard(this.dao).setVisible(true); 
                this.dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText(""); 
            }
        });

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
