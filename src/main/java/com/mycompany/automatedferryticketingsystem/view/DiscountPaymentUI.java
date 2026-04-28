package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.dao.VesselDAO; 
import com.mycompany.automatedferryticketingsystem.model.Ticket;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * DiscountPaymentUI Class
 * Kini nga screen maoy tig-proseso sa bayad ug tig-compute sa mga discounts.
 * [INHERITANCE] - Nag-EXTEND kini sa JFrame aron makahimo og Window.
 */
public class DiscountPaymentUI extends JFrame {
    
    // [ENCAPSULATION] - Ang paggamit sa 'private' variables 
    // aron dili direktang ma-manipulate ang data sa ticket ug components.
    
    private Ticket ticket;
    private VesselDAO dao; 
    private JComboBox<String> cbPassengerType;
    private JTextField txtIDNumber;
    private JLabel lblBaseFare, lblDiscount, lblTotal, lblErrorMsg;
    private JButton btnGcash, btnCash, btnComplete, btnValidateID;
    private String selectedMethod = "";

    private final String SYSTEM_TITLE = "AUTOMATED FERRY TICKETING SYSTEM";
    private final Color ACCENT_BLUE = new Color(0, 120, 215);
    private final Color HOVER_BLUE = new Color(50, 150, 255);
    private final Color DARK_BG = new Color(38, 40, 45);
    private final Color CARD_BG = new Color(45, 48, 55);
    private final Color ERROR_RED = new Color(255, 80, 80);

    public DiscountPaymentUI(VesselDAO dao, Ticket ticket) {
        this.dao = dao;
        this.ticket = ticket;
        
        setTitle(SYSTEM_TITLE); 
        setMinimumSize(new Dimension(900, 850));
        setSize(1000, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(DARK_BG);
        setLayout(new BorderLayout());

        // --- HEADER ---
        JLabel lblHeader = new JLabel("REVIEW & PAYMENT", SwingConstants.CENTER);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- MAIN CENTER PANEL ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 50, 10, 50);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        gbc.gridy = 0; gbc.weighty = 0.2;
        centerPanel.add(createPassengerSummaryPanel(), gbc);

        gbc.gridy = 1; gbc.weighty = 0.25;
        centerPanel.add(createDiscountPanel(), gbc);

        gbc.gridy = 2; gbc.weighty = 0.2;
        centerPanel.add(createPaymentSummary(), gbc);

        gbc.gridy = 3; gbc.weighty = 0.25;
        centerPanel.add(createPaymentMethods(), gbc);

        add(centerPanel, BorderLayout.CENTER);

        // --- FOOTER ---
        btnComplete = new JButton("COMPLETE PURCHASE");
        styleMainButton(btnComplete, new Color(60, 60, 64));
        btnComplete.setEnabled(false); 
        
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 30, 50));
        footerPanel.add(btnComplete, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setupFinalActions();
        updateCalculations();
    }

    /**
     * Logic para sa pag-validate sa ID ug paghuman sa transaction.
     * [POLYMORPHISM] - Ang ActionListener naggamit og Lambda expressions
     * aron mo-handle sa user interactions.
     */
    private void setupFinalActions() {
        btnValidateID.addActionListener(e -> {
            String id = txtIDNumber.getText().trim();
            String idPattern = "^[a-zA-Z0-9-]+$"; 

            if (!id.isEmpty() && !id.equals("Enter ID number") && id.matches(idPattern)) {
                ticket.setIdNumber(id);
                JOptionPane.showMessageDialog(this, "ID Validated Successfully.");
                checkPurchaseEligibility();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "INVALID ID: Palihog pagsulod og saktong ID.\nBawal ang symbols sama sa !@#$%^&*.", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnComplete.addActionListener(e -> {
            String txID = "F-TXN-" + (System.currentTimeMillis() % 1000000);
            ticket.setTransactionId(txID);
            
            if(!txtIDNumber.getText().equals("Enter ID number")) {
                ticket.setIdNumber(txtIDNumber.getText().trim());
            }

            // [OOP CONCEPT: OBJECT INSTANTIATION] - Pag-create og bag-ong screen object.
            new FinalTicketUI(this.dao, ticket).setVisible(true);
            this.dispose();
        });
    }

    private JPanel createPassengerSummaryPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3, 15, 0));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), 
            " Passenger Verification ", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));

        p.add(createSummaryItem("PASSENGER", ticket.getPassengerName()));
        p.add(createSummaryItem("CONTACT", ticket.getContactNumber()));
        p.add(createSummaryItem("ADDRESS", ticket.getAddress())); 

        return p;
    }

    private JPanel createSummaryItem(String title, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblTitle.setForeground(Color.LIGHT_GRAY);
        JLabel lblVal = new JLabel(value != null ? value : "N/A");
        lblVal.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblVal.setForeground(Color.WHITE);
        p.add(lblTitle); p.add(lblVal);
        return p;
    }

    private JPanel createDiscountPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), 
            " Discount Selection ", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        cbPassengerType = new JComboBox<>(new String[]{"Regular", "Student", "Senior Citizen", "PWD"});
        cbPassengerType.addActionListener(e -> updateCalculations());

        txtIDNumber = new JTextField("Enter ID number");
        txtIDNumber.setForeground(Color.GRAY);
        txtIDNumber.setPreferredSize(new Dimension(0, 40));
        
        applyIdLimitFilter(txtIDNumber, 15); 

        lblErrorMsg = new JLabel(" "); 
        lblErrorMsg.setForeground(ERROR_RED);

        // [OOP CONCEPT: INTERFACE/INNER CLASS] - Naggamit og FocusAdapter aron 
        // mo-override sa specific focus events.
        txtIDNumber.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtIDNumber.getText().equals("Enter ID number")) {
                    txtIDNumber.setText("");
                    txtIDNumber.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtIDNumber.getText().isEmpty()) {
                    txtIDNumber.setForeground(Color.GRAY);
                    txtIDNumber.setText("Enter ID number");
                }
                checkPurchaseEligibility();
            }
        });

        btnValidateID = new JButton("VALIDATE ID");
        styleActionBtn(btnValidateID);

        gbc.gridy = 0; 
        JLabel lblType = new JLabel("Category Selection");
        lblType.setForeground(Color.LIGHT_GRAY);
        p.add(lblType, gbc);

        gbc.gridy = 1; p.add(cbPassengerType, gbc);

        gbc.gridy = 2; 
        JLabel lblId = new JLabel("ID Number (Required for Discounts)");
        lblId.setForeground(Color.LIGHT_GRAY);
        p.add(lblId, gbc);
        
        JPanel idGroup = new JPanel(new BorderLayout(10, 0));
        idGroup.setOpaque(false);
        idGroup.add(txtIDNumber, BorderLayout.CENTER);
        idGroup.add(btnValidateID, BorderLayout.EAST);
        gbc.gridy = 3; p.add(idGroup, gbc);
        gbc.gridy = 4; p.add(lblErrorMsg, gbc);

        return p;
    }

    private void applyIdLimitFilter(JTextField field, int limit) {
        
        // [POLYMORPHISM (Method Overriding)] - Gi-override ang 'replace' 
        // method aron limitahan ang gidaghanon sa characters.
        
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                int currentLength = fb.getDocument().getLength();
                if ((currentLength + text.length() - length) <= limit) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep(); 
                }
            }
        });
    }

    /**
     * [ ABSTRACTION] - Gi-hide niini ang math logic. 
     * Ang ubang components motawag lang ani aron ma-refresh ang total.
     */
    private void updateCalculations() {
        String type = (String) cbPassengerType.getSelectedItem();
        boolean isRegular = type.equals("Regular");
        
        txtIDNumber.setEnabled(!isRegular);
        btnValidateID.setEnabled(!isRegular);
        
        resetPaymentSelection();

        double discount = isRegular ? 0 : ticket.getBaseFare() * 0.20;
        double finalTotal = ticket.getBaseFare() - discount;

        lblDiscount.setText("₱" + String.format("%.2f", discount));
        lblTotal.setText("₱" + String.format("%.2f", finalTotal));
        
        ticket.setCategory(type);
        ticket.setFinalFare(finalTotal);
        
        checkPurchaseEligibility();
    }

    private void handleCashPayment() {
        double total = ticket.getFinalFare();
        double MAX_REALISTIC_CASH = 10000.00;

        String input = JOptionPane.showInputDialog(this, 
            "Total Amount: ₱" + String.format("%.2f", total) + "\n\nEnter Amount Received:", 
            "Cash Payment", JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.isEmpty()) {
            try {
                double received = Double.parseDouble(input);
                if (received < total) {
                    showPaymentError("Kulang imong gibayad!");
                } else if (received > MAX_REALISTIC_CASH) {
                    showPaymentError("SOBRA RA KAAYO.");
                } else {
                    double change = received - total;
                    JOptionPane.showMessageDialog(this, "Sukli: ₱" + String.format("%.2f", change));
                    ticket.setPaymentMethod("Cash");
                    checkPurchaseEligibility();
                }
            } catch (NumberFormatException e) {
                showPaymentError("Dili valid nga numero.");
            }
        } else {
            resetPaymentSelection();
        }
    }

    private void handleGCashPayment() {
        double total = ticket.getFinalFare();
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.add(new JLabel("Amount to Pay: ₱" + String.format("%.2f", total)));
        panel.add(new JLabel("Enter 13-digit GCash Ref No:"));
        JTextField refField = new JTextField();
        panel.add(refField);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "GCash Payment Verification", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String refNo = refField.getText().trim();
            if (refNo.matches("\\d{13}")) { 
                JOptionPane.showMessageDialog(this, "Reference Number Verified!");
                ticket.setPaymentMethod("GCash (Ref: " + refNo + ")");
                checkPurchaseEligibility();
            } else {
                showPaymentError("Invalid Reference Number!");
            }
        } else {
            resetPaymentSelection();
        }
    }

    private void selectPayment(String method, JButton btn) {
        selectedMethod = method;
        btnGcash.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btnCash.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btn.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 3));

        if (method.equals("Cash")) handleCashPayment();
        else if (method.equals("GCash")) handleGCashPayment();
    }

    private void checkPurchaseEligibility() {
        String type = (String) cbPassengerType.getSelectedItem();
        String idText = txtIDNumber.getText().trim();
        
        boolean isRegular = type.equals("Regular");
        boolean hasId = !idText.isEmpty() && !idText.equals("Enter ID number");
        boolean idRequirementMet = isRegular || hasId;

        btnGcash.setEnabled(idRequirementMet);
        btnCash.setEnabled(idRequirementMet);
        
        if (!idRequirementMet) {
            lblErrorMsg.setText("* Kinahanglan og ID Number para sa " + type + " discount.");
            btnGcash.setBackground(new Color(200, 200, 200));
            btnCash.setBackground(new Color(200, 200, 200));
        } else {
            lblErrorMsg.setText(" ");
            btnGcash.setBackground(Color.WHITE);
            btnCash.setBackground(Color.WHITE);
        }

        boolean isMethodSelected = !selectedMethod.isEmpty();
        if (idRequirementMet && isMethodSelected) {
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

    private void showPaymentError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Payment Error", JOptionPane.ERROR_MESSAGE);
        resetPaymentSelection();
    }

    private void resetPaymentSelection() {
        selectedMethod = "";
        btnGcash.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btnCash.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        checkPurchaseEligibility();
    }

    // --- STYLING METHODS (ENCAPSULATED LOGIC) ---
    private void styleMainButton(JButton btn, Color base) {
        btn.setPreferredSize(new Dimension(0, 60));
        btn.setBackground(base);
        btn.setForeground(Color.GRAY);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorderPainted(false);
        // [OOP CONCEPT: POLYMORPHISM] - Anonymous inner class for hover effects.
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(btn.isEnabled()) btn.setBackground(HOVER_BLUE); }
            public void mouseExited(MouseEvent e) { if(btn.isEnabled()) btn.setBackground(ACCENT_BLUE); }
        });
    }

    private void stylePaymentBtn(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    private void styleActionBtn(JButton btn) {
        btn.setBackground(new Color(60, 60, 65));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }
}