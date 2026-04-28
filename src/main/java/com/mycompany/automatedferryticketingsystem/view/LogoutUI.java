package com.mycompany.automatedferryticketingsystem.view;

import javax.swing.*;
import java.awt.*;

/**
 * [LOGIC OVERVIEW]
 * 1. MODAL DIALOG: Kini nga window (JDialog) mo-pause sa main app para dili maka-click 
 * sa lain ang user hangtod dili siya mopili og 'Yes' o 'No'.
 * 2. TERMINAL AESTHETIC: Naggamit og Monospaced fonts ug Neon Green colors para 
 * mo-bagay sa imong Linux-style terminal theme.
 */
public class LogoutUI extends JDialog {
    private boolean confirmed = false; // Flag para mahibal-an sa Controller ang choice sa user.

    public LogoutUI(Frame parent) {
        super(parent, true); // Logic: 'true' means modal. Ang user kinahanglan mo-interact ani una.
        initUI();
    }

    private void initUI() {
        // Logic: 'undecorated' removes the minimize/maximize/close buttons sa Windows/Linux 
        // para makuha ang custom 'Pop-up' look.
        setUndecorated(true); 
        setSize(400, 250);
        setLocationRelativeTo(getOwner()); // I-center ang dialog sa tunga sa parent window.

        // Main Container Panel - Ang 'panapton' sa imong UI.
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(10, 10, 10)); // Deep Black background.
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 100), 2)); // Neon Green border.

        // Header: Ang title text nga naggamit og 'Monospaced' para sa coder vibe.
        JLabel titleLabel = new JLabel(">_ TERMINATE SESSION?", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(0, 255, 100));
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

        // Button Container - Naggamit og GridLayout para pantay ang duha ka buttons.
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        buttonPanel.setOpaque(false); // Transparent background para makita ang mainPanel black color.
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));

        // YES Button: Highlighted color para dali ra makita ang action.
        JButton btnYes = new JButton("YES, LOGOUT");
        btnYes.setBackground(new Color(0, 255, 100));
        btnYes.setForeground(Color.BLACK); // Contrast: Black text on Green background.
        btnYes.setFont(new Font("Monospaced", Font.BOLD, 14));
        btnYes.setFocusPainted(false);
        btnYes.setBorderPainted(false);
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // NO Button: Outlined style para visually secondary action ra siya.
        JButton btnNo = new JButton("NO, RETURN");
        btnNo.setBackground(new Color(10, 10, 10));
        btnNo.setForeground(new Color(0, 255, 100));
        btnNo.setFont(new Font("Monospaced", Font.BOLD, 14));
        btnNo.setFocusPainted(false);
        btnNo.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 100), 1));
        btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Footer Text: Small details para mapalig-on ang "System/Terminal" theme.
        JLabel footerLabel = new JLabel("Session ID: [ENCRYPTED]", SwingConstants.CENTER);
        footerLabel.setForeground(new Color(0, 100, 40)); // Darker green para dili distracting.
        footerLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // [LOGIC: ACTION LISTENERS]
        
        // Inig click sa Yes, i-set ang flag to true ug i-close ang dialog.
        btnYes.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        // Inig click sa No, false ang flag ug i-close ang dialog (return to app).
        btnNo.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        // Assemble: Pag-combine sa tanang elements ngadto sa main window.
        buttonPanel.add(btnYes);
        buttonPanel.add(btnNo);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * LOGIC: Getter method.
     * Importante ni para ang naggamit nga Controller (e.g. AdminController) 
     * makahibalo kung mo-proceed ba sa logout or dili.
     */
    public boolean isConfirmed() {
        return confirmed;
    }
}