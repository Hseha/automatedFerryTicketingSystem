package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.model.Ticket;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PassengerInfoUI extends JFrame {
    private Ticket ticket;
    
 
    private final Color ACCENT_BLUE = new Color(0, 120, 215);
    private final Color HOVER_BLUE = new Color(50, 150, 255);
    private final Color DARK_BG = new Color(28, 30, 35);
    private final Color CARD_BG = new Color(45, 48, 55);
    private final Color STATUS_GREEN = new Color(46, 204, 113);
    private final Color WARNING_RED = new Color(231, 76, 60);
    private final String SYSTEM_TITLE = "AUTOMATED FERRY TICKETING SYSTEM";

    private JTextField txtFullName, txtContact;
    private JSpinner spnrAge;
    private JButton btnBack, btnValidate;
    private JLabel lblStatus;

    public PassengerInfoUI(Ticket ticket) {
        this.ticket = ticket;
        setTitle(SYSTEM_TITLE);
        setMinimumSize(new Dimension(1000, 850));
        setSize(1000, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(DARK_BG);
        setLayout(new BorderLayout());

        // --- HEADER ---
        JLabel lblHeader = new JLabel("TRIP & PASSENGER INFORMATION", SwingConstants.CENTER);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- CENTER PANEL (Split Layout) ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Side: Inputs
        gbc.gridx = 0; gbc.weightx = 0.5;
        centerPanel.add(createPassengerPanel(), gbc);

        // Right Side: Summary Card
        gbc.gridx = 1; gbc.weightx = 0.5;
        centerPanel.add(createTripSummaryPanel(), gbc);

        add(centerPanel, BorderLayout.CENTER);

        // --- FOOTER WITH DYNAMIC STATUS ---
        add(createModernFooter(), BorderLayout.SOUTH);

        setupActions();
    }

    private JPanel createModernFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 50, 40, 50));

        btnBack = new JButton("BACK");
        btnValidate = new JButton("VALIDATE INFORMATION");
        
        lblStatus = new JLabel();
        updateSystemStatus(); // Dynamic Health Check

        styleButton(btnBack, new Color(60, 63, 65));
        styleButton(btnValidate, ACCENT_BLUE);

        GridBagConstraints g = new GridBagConstraints();
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;

        // Back Button
        g.gridx = 0; p.add(btnBack, g);
        
        // Middle Status (Online)
        g.weightx = 0; 
        g.gridx = 1; 
        g.insets = new Insets(0, 40, 0, 40);
        p.add(lblStatus, g);
        
        // Validate Button
        g.weightx = 1.0;
        g.gridx = 2; 
        g.insets = new Insets(0, 0, 0, 0);
        p.add(btnValidate, g);

        return p;
    }

    private void updateSystemStatus() {
        // Checks if data is present to determine if system is "Online"
        if (ticket != null && ticket.getRoute() != null) {
            lblStatus.setText("SYSTEM STATUS: ONLINE");
            lblStatus.setForeground(STATUS_GREEN);
        } else {
            lblStatus.setText("SYSTEM STATUS: OFFLINE");
            lblStatus.setForeground(WARNING_RED);
        }
        lblStatus.setFont(new Font("Monospaced", Font.BOLD, 13));
    }

    private void setupActions() {
        // --- VALIDATE BUTTON: Moves to Discount & Payment ---
        btnValidate.addActionListener(e -> {
            String name = txtFullName.getText().trim();
            String contact = txtContact.getText().trim();

            if (name.isEmpty() || contact.isEmpty()) {
                // Pop-up Error if name is missing
                JOptionPane.showMessageDialog(this, 
                    "ERROR: Please enter personal info first!", 
                    "System Validation", 
                    JOptionPane.ERROR_MESSAGE);
            } else {
                // Save data and transition
                ticket.setPassengerName(name);
                
                // Switch to DiscountPaymentUI
                new DiscountPaymentUI(ticket).setVisible(true);
                this.dispose(); 
            }
        });

        // --- BACK BUTTON: Returns to Identify Ferry (Trip Selection) ---
        btnBack.addActionListener(e -> {
            new IdentifyFerryUI().setVisible(true); 
            this.dispose(); 
        });
    }

    private JPanel createPassengerPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)), 
            " Passenger Details ", TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 14), Color.WHITE));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(15, 25, 5, 25);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.gridx = 0;

        txtFullName = new JTextField();
        txtContact = new JTextField();
        spnrAge = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        
        styleInput(txtFullName);
        styleInput(txtContact);
        
        g.gridy = 0; p.add(createLabel("FULL NAME"), g);
        g.gridy = 1; p.add(txtFullName, g);
        g.gridy = 2; p.add(createLabel("AGE"), g);
        g.gridy = 3; p.add(spnrAge, g);
        g.gridy = 4; p.add(createLabel("CONTACT NUMBER"), g);
        g.gridy = 5; p.add(txtContact, g);

        return p;
    }

    private JPanel createTripSummaryPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)), 
            " Trip Catalogue ", TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 14), Color.WHITE));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        card.add(createSummaryLabel("SELECTED ROUTE", ticket.getRoute(), Color.GRAY));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(createSummaryLabel("DEPARTURE TIME", ticket.getDepartureTime(), Color.GRAY));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(createSummaryLabel("VESSEL", ticket.getVesselName(), Color.GRAY));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(createSummaryLabel("TRAVEL TIME", ticket.getTravelTime(), Color.GRAY));
        card.add(Box.createRigidArea(new Dimension(0, 30)));
        
        JLabel lblPrice = new JLabel("₱" + String.format("%.2f", ticket.getBaseFare()), SwingConstants.RIGHT);
        lblPrice.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblPrice.setForeground(new Color(39, 174, 96));
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblPrice);

        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setPreferredSize(new Dimension(250, 55));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(HOVER_BLUE); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
    }

    private void styleInput(JTextField f) {
        f.setPreferredSize(new Dimension(0, 45));
        f.setBackground(new Color(60, 63, 65));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 16));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(180, 180, 180));
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    private JPanel createSummaryLabel(String title, String val, Color tCol) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(tCol);
        JLabel v = new JLabel(val);
        v.setFont(new Font("SansSerif", Font.BOLD, 19));
        v.setForeground(Color.BLACK);
        p.add(t); p.add(v);
        return p;
    }
}