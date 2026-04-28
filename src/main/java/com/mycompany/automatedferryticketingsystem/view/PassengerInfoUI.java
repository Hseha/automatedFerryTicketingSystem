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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * [LOGIC OVERVIEW]
 * 1. DATA PASSING: Gidawat ang Ticket object gikan sa previous screen para mapadayon ang booking.
 * 2. LIVE FILTERING: Gigamit ang DocumentFilters para i-block ang invalid characters samtang nag-type pa ang user.
 * 3. VALIDATION: Gi-check ang consistency sa data (e.g., PH mobile number format) sa dili pa mo-proceed.
 * 4. UX DESIGN: Modern dark theme nga naay real-time hover effects sa buttons.
 */
public class PassengerInfoUI extends JFrame {
    // Member variables para sa global access sa data ug DAO.
    private Ticket ticket;
    private VesselDAO dao; 
    
    // UI Branding Colors (Constants para dali ra i-maintain/usbon).
    private final Color ACCENT_BLUE = new Color(0, 120, 215);
    private final Color HOVER_BLUE = new Color(50, 150, 255);
    private final Color DARK_BG = new Color(28, 30, 35);
    private final Color CARD_BG = new Color(45, 48, 55);
    private final Color STATUS_GREEN = new Color(46, 204, 113);
    private final String SYSTEM_TITLE = "AUTOMATED FERRY TICKETING SYSTEM";

    // Paggahin og memory para sa input components.
    private JTextField txtFullName, txtContact, txtAddress; 
    private JSpinner spnrAge;
    private JButton btnBack, btnValidate;
    private JLabel lblStatus;

    /**
     * Constructor: Diri i-setup ang tibuok "dagway" sa window.
     */
    public PassengerInfoUI(VesselDAO dao, Ticket ticket) {
        this.dao = dao; 
        this.ticket = ticket;
        
        // Window Properties: Setting size, centering, ug background color.
        setTitle(SYSTEM_TITLE);
        setMinimumSize(new Dimension(1000, 850));
        setSize(1000, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(DARK_BG);
        setLayout(new BorderLayout());

        // --- HEADER SECTION ---
        // Top label para sa main title sa screen.
        JLabel lblHeader = new JLabel("TRIP & PASSENGER INFORMATION", SwingConstants.CENTER);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- CENTER SECTION (CONTENT) ---
        // JPanel nga naay GridBagLayout para flexible ang pag-arrange sa cards.
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Side: Passenger Details input form.
        gbc.gridx = 0; gbc.weightx = 0.5;
        centerPanel.add(createPassengerPanel(), gbc);

        // Right Side: Summary of the selected trip (Receipt-style view).
        gbc.gridx = 1; gbc.weightx = 0.5;
        centerPanel.add(createTripSummaryPanel(), gbc);

        add(centerPanel, BorderLayout.CENTER);
        add(createModernFooter(), BorderLayout.SOUTH); // Bottom controls.

        setupActions(); // Register logic listeners for interactive elements.
    }

    /**
     * SETUP ACTIONS: Mao ni ang "logic engine" sa window.
     */
    private void setupActions() {
        // Event listener para sa Validate Button.
        btnValidate.addActionListener(e -> {
            // Get text, then remove extra spaces (trim).
            String name = txtFullName.getText().trim();
            String contact = txtContact.getText().trim();
            String address = txtAddress.getText().trim(); 
            int age = (int) spnrAge.getValue();

            // Regex Patterns para sa strict validation checks.
            String namePattern = "^[A-Za-z][A-Za-z\\s.]{1,49}$";
            boolean isGibberish = name.matches(".*(.)\\1\\1\\1.*"); // Checks for spam characters like "aaaa".

            // Logic Tree for Validation:
            if (name.isEmpty() || contact.isEmpty() || address.isEmpty()) {
                showError("ERROR: Please fill up tanan fields!");
            } 
            else if (!name.matches(namePattern) || isGibberish) {
                showError("INVALID NAME: Palihog butangi og tarong nga name.");
            }
            else if (age < 12) {
                // Safety Rule: Passengers below 12 need a guardian.
                JOptionPane.showMessageDialog(this, 
                    "UNACCOMPANIED MINOR\nNeed og guardian kung below 12 years old.", 
                    "Notice", JOptionPane.WARNING_MESSAGE);
            }
            else if (!contact.matches("^09\\d{9}$")) {
                // Logic: Must start with 09 and follow PH mobile format.
                showError("INVALID CONTACT: Dapat magsugod sa '09' ug 11 digits tanan.");
            } 
            else {
                // SUCCESS: Save results to the Ticket object.
                ticket.setPassengerName(name);
                ticket.setAge(age);
                ticket.setContactNumber(contact);
                ticket.setAddress(address); 
                
                // Logic: Pass the object to the next UI (Payment screen).
                new DiscountPaymentUI(this.dao, ticket).setVisible(true);
                this.dispose(); // Close current UI to save RAM.
            }
        });

        // Go back to ferry selection.
        btnBack.addActionListener(e -> {
            new IdentifyFerryUI(this.dao).setVisible(true); 
            this.dispose(); 
        });
    }

    /**
     * PANEL BUILDER: Setup sa UI layout para sa passenger info inputs.
     */
    private JPanel createPassengerPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)), 
            " Passenger Details ", TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 14), Color.WHITE));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 25, 5, 25);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.gridx = 0;

        // Field initialization and styling.
        txtFullName = new JTextField();
        txtContact = new JTextField();
        txtAddress = new JTextField(); 
        spnrAge = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        
        styleInput(txtFullName);
        styleInput(txtContact);
        styleInput(txtAddress); 
        
        // [LIVE FILTERS]: I-restrict ang typing behavior sa user.
        applyStrictNameFilter(txtFullName);
        applyContactNumberFilter(txtContact);
        applyLengthLimit(txtFullName, 50); 
        applyLengthLimit(txtAddress, 100); 
        
        // Add components to the grid in sequence.
        g.gridy = 0; p.add(createLabel("FULL NAME"), g);
        g.gridy = 1; p.add(txtFullName, g);
        g.gridy = 2; p.add(createLabel("AGE"), g);
        g.gridy = 3; p.add(spnrAge, g);
        g.gridy = 4; p.add(createLabel("CONTACT NUMBER"), g);
        g.gridy = 5; p.add(txtContact, g);
        g.gridy = 6; p.add(createLabel("HOME ADDRESS"), g);
        g.gridy = 7; p.add(txtAddress, g);

        return p;
    }

    /**
     * FILTER LOGIC: Restricts input sa Name field para letters ug dots ra.
     */
    private void applyStrictNameFilter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                // If it matches letters/spaces, insert it; otherwise, trigger an alert beep.
                if (text.matches("[A-Za-z\\s.]*")) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep(); 
                }
            }
        });
    }

    /**
     * FILTER LOGIC: Strict length check para dili ma-error sa SQL database limits.
     */
    private void applyLengthLimit(JTextField field, int limit) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
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
     * FILTER LOGIC: Digits-only check para sa contact number inputs.
     */
    private void applyContactNumberFilter(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nextText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                // Allow only numbers and max 11 digits.
                if (nextText.matches("\\d*") && nextText.length() <= 11) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    // Common helper method para sa error dialogs.
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "System Check", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * UI BUILDER: Display summary card containing data from the previous selection.
     */
    private JPanel createTripSummaryPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)), 
            " Trip Catalogue ", TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 14), Color.WHITE));

        // Create a white contrast card.
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Logic: Get data directly from the 'ticket' object which holds the current session details.
        card.add(createSummaryLabel("SELECTED ROUTE", ticket.getRoute(), Color.GRAY));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(createSummaryLabel("DEPARTURE TIME", ticket.getDepartureTime(), Color.GRAY));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(createSummaryLabel("DEPARTURE PIER", ticket.getPierNo(), Color.GRAY));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(createSummaryLabel("VESSEL", ticket.getVesselName(), Color.GRAY));
        card.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Large price text para dali ra makit-an sa user.
        JLabel lblPrice = new JLabel("₱" + String.format("%.2f", ticket.getBaseFare()), SwingConstants.RIGHT);
        lblPrice.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblPrice.setForeground(new Color(39, 174, 96));
        card.add(lblPrice);

        p.add(card, BorderLayout.CENTER);
        return p;
    }

    /**
     * FOOTER BUILDER: Handles navigation buttons and system status display.
     */
    private JPanel createModernFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 50, 40, 50));

        btnBack = new JButton("BACK");
        btnValidate = new JButton("VALIDATE INFORMATION");
        lblStatus = new JLabel("SYSTEM STATUS: ONLINE");
        lblStatus.setForeground(STATUS_GREEN);
        lblStatus.setFont(new Font("Monospaced", Font.BOLD, 13));

        // Method calls para sa visual styling.
        styleButton(btnBack, new Color(60, 63, 65));
        styleButton(btnValidate, ACCENT_BLUE);

        GridBagConstraints g = new GridBagConstraints();
        g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; p.add(btnBack, g);
        g.weightx = 0; g.gridx = 1; g.insets = new Insets(0, 40, 0, 40); p.add(lblStatus, g);
        g.weightx = 1.0; g.gridx = 2; g.insets = new Insets(0, 0, 0, 0); p.add(btnValidate, g);

        return p;
    }

    /**
     * UTILITY: Standardized styling para sa buttons (Hand cursor + Hover logic).
     */
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

    /**
     * UTILITY: Standardized styling para sa input fields.
     */
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

    /**
     * UI HELPER: Shortcut para sa gray text labels.
     */
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(180, 180, 180));
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    /**
     * UI HELPER: Shortcut para sa formatted labels sa Summary Card.
     */
    private JPanel createSummaryLabel(String title, String val, Color tCol) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(tCol);
        JLabel v = new JLabel(val != null ? val : "---");
        v.setFont(new Font("SansSerif", Font.BOLD, 19));
        v.setForeground(Color.BLACK);
        p.add(t); p.add(v);
        return p;
    }
}