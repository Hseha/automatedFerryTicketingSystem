package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.model.Ticket;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DiscountPaymentUI extends JFrame {
    private Ticket ticket;
    private JComboBox<String> cbPassengerType;
    private JTextField txtIDNumber;
    private JLabel lblBaseFare, lblDiscount, lblTotal, lblErrorMsg;
    private JButton btnGcash, btnCash, btnComplete;
    private String selectedMethod = "";

    private final String SYSTEM_TITLE = "AUTOMATED FERRY TICKETING SYSTEM";
    
    private final Color ACCENT_BLUE = new Color(0, 120, 215);
    private final Color HOVER_BLUE = new Color(50, 150, 255);
    private final Color DARK_BG = new Color(38, 40, 45);
    private final Color ERROR_RED = new Color(255, 80, 80);

    public DiscountPaymentUI(Ticket ticket) {
        this.ticket = ticket;
        setTitle(SYSTEM_TITLE); 
        setMinimumSize(new Dimension(850, 750));
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(DARK_BG);
        setLayout(new BorderLayout());

        JLabel lblHeader = new JLabel("DISCOUNT & PAYMENT", SwingConstants.CENTER);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(lblHeader, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 50, 10, 50);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        gbc.gridy = 0; gbc.weighty = 0.35;
        centerPanel.add(createDiscountPanel(), gbc);

        gbc.gridy = 1; gbc.weighty = 0.25;
        centerPanel.add(createPaymentSummary(), gbc);

        gbc.gridy = 2; gbc.weighty = 0.4;
        centerPanel.add(createPaymentMethods(), gbc);

        add(centerPanel, BorderLayout.CENTER);

        btnComplete = new JButton("COMPLETE PURCHASE");
        styleMainButton(btnComplete, new Color(60, 60, 64));
        btnComplete.setEnabled(false); 
        
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 30, 50));
        footerPanel.add(btnComplete, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        updateCalculations();
    }

    private JPanel createDiscountPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), 
            "Discount Validation", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        cbPassengerType = new JComboBox<>(new String[]{"Regular", "Student", "Senior Citizen", "PWD"});
        cbPassengerType.addActionListener(e -> updateCalculations());

        txtIDNumber = new JTextField("Enter ID number");
        txtIDNumber.setForeground(Color.GRAY);
        txtIDNumber.setPreferredSize(new Dimension(0, 40));
        
        lblErrorMsg = new JLabel(" "); 
        lblErrorMsg.setForeground(ERROR_RED);
        lblErrorMsg.setFont(new Font("SansSerif", Font.ITALIC, 12));

        txtIDNumber.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtIDNumber.getText().equals("Enter ID number")) {
                    txtIDNumber.setText("");
                    txtIDNumber.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtIDNumber.getText().isEmpty()) {
                    txtIDNumber.setForeground(Color.GRAY);
                    txtIDNumber.setText("Enter ID number");
                }
                checkPurchaseEligibility();
            }
        });

        txtIDNumber.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkPurchaseEligibility(); }
            public void removeUpdate(DocumentEvent e) { checkPurchaseEligibility(); }
            public void changedUpdate(DocumentEvent e) { checkPurchaseEligibility(); }
        });

        JButton btnValidateID = new JButton("VALIDATE ID");
        styleActionBtn(btnValidateID);

        gbc.gridy = 0; p.add(new JLabel("Passenger Type") {{ setForeground(Color.LIGHT_GRAY); }}, gbc);
        gbc.gridy = 1; p.add(cbPassengerType, gbc);
        gbc.gridy = 2; p.add(new JLabel("ID Number") {{ setForeground(Color.LIGHT_GRAY); }}, gbc);
        
        JPanel idGroup = new JPanel(new BorderLayout(10, 0));
        idGroup.setOpaque(false);
        idGroup.add(txtIDNumber, BorderLayout.CENTER);
        idGroup.add(btnValidateID, BorderLayout.EAST);
        gbc.gridy = 3; p.add(idGroup, gbc);
        
        gbc.gridy = 4; p.add(lblErrorMsg, gbc);

        return p;
    }

    private void checkPurchaseEligibility() {
        String idText = txtIDNumber.getText().trim();
        boolean isIdEntered = !idText.isEmpty() && !idText.equals("Enter ID number");
        boolean isMethodSelected = !selectedMethod.isEmpty();

        if (!isIdEntered && isMethodSelected) {
            lblErrorMsg.setText("Warning: ID Number is required for selected category.");
        } else {
            lblErrorMsg.setText(" ");
        }

        if (isIdEntered && isMethodSelected) {
            btnComplete.setEnabled(true);
            btnComplete.setBackground(ACCENT_BLUE);
            btnComplete.setForeground(Color.WHITE);
        } else {
            btnComplete.setEnabled(false);
            btnComplete.setBackground(new Color(60, 60, 64));
            btnComplete.setForeground(Color.GRAY);
        }
    }

    private JPanel createPaymentSummary() {
        JPanel p = new JPanel(new GridLayout(3, 2, 0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        lblBaseFare = new JLabel("₱" + String.format("%.2f", ticket.getBaseFare()));
        lblDiscount = new JLabel("₱0.00");
        lblDiscount.setForeground(Color.RED);
        lblTotal = new JLabel("₱0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTotal.setForeground(new Color(0, 150, 50));
        p.add(new JLabel("Base Fare:")); p.add(lblBaseFare);
        p.add(new JLabel("Discount Amount:")); p.add(lblDiscount);
        p.add(new JLabel("Final Total:")); p.add(lblTotal);
        return p;
    }

    private JPanel createPaymentMethods() {
        JPanel p = new JPanel(new GridLayout(1, 2, 20, 0));
        p.setOpaque(false);
        btnGcash = new JButton("GCash");
        btnCash = new JButton("Cash");
        stylePaymentBtn(btnGcash);
        stylePaymentBtn(btnCash);
        btnGcash.addActionListener(e -> selectPayment("GCash", btnGcash));
        btnCash.addActionListener(e -> selectPayment("Cash", btnCash));
        p.add(btnGcash); p.add(btnCash);
        return p;
    }

    private void updateCalculations() {
        String type = (String) cbPassengerType.getSelectedItem();
        double discount = type.equals("Regular") ? 0 : ticket.getBaseFare() * 0.20;
        double finalTotal = ticket.getBaseFare() - discount;
        lblDiscount.setText("₱" + String.format("%.2f", discount));
        lblTotal.setText("₱" + String.format("%.2f", finalTotal));
        ticket.setCategory(type);
        ticket.setFinalFare(finalTotal);
        checkPurchaseEligibility();
    }

    private void selectPayment(String method, JButton btn) {
        selectedMethod = method;
        btnGcash.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btnCash.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btn.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 3));
        checkPurchaseEligibility();
    }

    private void styleMainButton(JButton btn, Color base) {
        btn.setPreferredSize(new Dimension(0, 60));
        btn.setBackground(base);
        btn.setForeground(Color.GRAY);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorderPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(btn.isEnabled()) btn.setBackground(HOVER_BLUE); }
            public void mouseExited(MouseEvent e) { if(btn.isEnabled()) btn.setBackground(ACCENT_BLUE); }
        });
    }

    private void stylePaymentBtn(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(!selectedMethod.equals(btn.getText())) btn.setBackground(new Color(240, 248, 255)); }
            public void mouseExited(MouseEvent e) { if(!selectedMethod.equals(btn.getText())) btn.setBackground(Color.WHITE); }
        });
    }

    private void styleActionBtn(JButton btn) {
        btn.setBackground(new Color(60, 60, 65));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }
}