package com.mycompany.automatedferryticketingsystem.view;

import javax.swing.*;
import java.awt.*;

/**
 * [INHERITANCE] - Kini nga class naggamit sa JDialog aron mahimong usa ka 
 * 'Modal Dialog'. Ang user kinahanglan mo-interact ani una bago makabalik sa main app.
 * * [ABSTRACTION] - Gi-hide niini ang logic sa session termination. 
 * Ang UI mo-provide lang og simple choice (Yes/No) sa user.
 */
public class LogoutUI extends JDialog {
    
    // [ENCAPSULATION] - Private field para sa security sa state. 
    // Ang 'isConfirmed()' getter lang ang pamaagi para mahibal-an ang choice sa user.
    private boolean confirmed = false; 

    public LogoutUI(Frame parent) {
        super(parent, true); // Logic: 'true' means modal interaction.
        initUI();
    }

    private void initUI() {
        /**
         * [TERMINAL AESTHETIC]
         * Naggamit og Monospaced fonts ug Neon Green colors para 
         * mo-bagay sa imong Linux-style terminal theme.
         */
        setUndecorated(true); 
        setSize(400, 250);
        setLocationRelativeTo(getOwner()); 

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(10, 10, 10)); 
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 100), 2)); 

        JLabel titleLabel = new JLabel(">_ TERMINATE SESSION?", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(0, 255, 100));
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        buttonPanel.setOpaque(false); 
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));

        JButton btnYes = new JButton("YES, LOGOUT");
        btnYes.setBackground(new Color(0, 255, 100));
        btnYes.setForeground(Color.BLACK); 
        btnYes.setFont(new Font("Monospaced", Font.BOLD, 14));
        btnYes.setFocusPainted(false);
        btnYes.setBorderPainted(false);
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnNo = new JButton("NO, RETURN");
        btnNo.setBackground(new Color(10, 10, 10));
        btnNo.setForeground(new Color(0, 255, 100));
        btnNo.setFont(new Font("Monospaced", Font.BOLD, 14));
        btnNo.setFocusPainted(false);
        btnNo.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 100), 1));
        btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel footerLabel = new JLabel("Session ID: [ENCRYPTED]", SwingConstants.CENTER);
        footerLabel.setForeground(new Color(0, 100, 40)); 
        footerLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // [ACTION LISTENERS]
        btnYes.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        btnNo.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(btnYes);
        buttonPanel.add(btnNo);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * [ENCAPSULATION - GETTER]
     * Kini nagtugot sa Controller sa pag-access sa value sa 'confirmed' 
     * flag nga dili direktang gina-modify ang variable.
     */
    public boolean isConfirmed() {
        return confirmed;
    }
}