package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.model.Ticket;
import com.zaxxer.hikari.HikariDataSource;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class FinalTicketUI extends JFrame {
    private Ticket ticket;
    private HikariDataSource dataSource;
    
    // Updated Colors to match the high-contrast image
    private final Color TICKET_WHITE = Color.WHITE;
    private final Color APP_BG = new Color(28, 30, 35); 
    private final Color TEXT_MAIN = Color.BLACK; 
    private final Color TEXT_SECONDARY = new Color(130, 130, 130);
    private final Color DARK_SECTION_BG = new Color(15, 15, 15);
    private final Font SANS_SERIF_BOLD_12 = new Font("SansSerif", Font.BOLD, 12);
    private final Font SANS_SERIF_PLAIN_12 = new Font("SansSerif", Font.PLAIN, 12);

    public FinalTicketUI(HikariDataSource dataSource, Ticket ticket) {
        this.dataSource = dataSource;
        this.ticket = ticket;
        
        // Window Setup
        setTitle("OFFICIAL TICKET");
        setSize(480, 950); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(APP_BG);
        setLayout(new BorderLayout());

        // --- MAIN TICKET PANEL (GridBagLayout for alignment) ---
        JPanel ticketPanel = new JPanel(new GridBagLayout());
        ticketPanel.setBackground(TICKET_WHITE);
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // 1. SHIP ICON
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0);
        JLabel lblIcon = new JLabel("🚢", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Serif", Font.PLAIN, 40));
        ticketPanel.add(lblIcon, gbc);

        // 2. OFFICIAL TICKET HEADER
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 20, 0);
        JLabel lblHeader = new JLabel("OFFICIAL TICKET", SwingConstants.CENTER);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        ticketPanel.add(lblHeader, gbc);

        // 3. TOP DASHED DIVIDER
        gbc.gridy = 2; gbc.insets = new Insets(0, -40, 20, -40); // Extend to edges
        ticketPanel.add(createDashedDivider(), gbc);

        // --- DATA SECTION ---
        gbc.insets = new Insets(5, 0, 5, 0); // Reset default insets

        // A. PASSENGER SUMMARY
        gbc.gridy = 3; ticketPanel.add(createSectionHeader("PASSENGER SUMMARY"), gbc);
        gbc.gridy = 4; ticketPanel.add(createDataRow("Name:", ticket.getPassengerName()), gbc);
        String voyId = ticket.getDepartureTime().replace(":", "") + "-" + ticket.getTripId(); // Simple Voyage ID
        gbc.gridy = 5; ticketPanel.add(createDataRow("Voyage ID:", voyId), gbc);
        gbc.gridy = 6; gbc.insets = new Insets(15, 0, 15, 0); ticketPanel.add(createDotDivider(), gbc);

        // B. VESSEL INFORMATION
        gbc.insets = new Insets(5, 0, 5, 0); // Reset
        gbc.gridy = 7; ticketPanel.add(createSectionHeader("VESSEL INFORMATION"), gbc);
        gbc.gridy = 8; ticketPanel.add(createDataRow("Vessel:", ticket.getVesselName()), gbc);
        gbc.gridy = 9; ticketPanel.add(createDataRow("Route:", ticket.getRoute()), gbc);
        gbc.gridy = 10; ticketPanel.add(createDataRow("Departure:", ticket.getDepartureTime()), gbc);
        // Assuming current date as trip date for simplicity
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        gbc.gridy = 11; ticketPanel.add(createDataRow("Date:", today.format(formatter)), gbc);
        gbc.gridy = 12; gbc.insets = new Insets(15, 0, 15, 0); ticketPanel.add(createDotDivider(), gbc);

        // C. PAYMENT INFORMATION
        gbc.insets = new Insets(5, 0, 5, 0); // Reset
        gbc.gridy = 13; ticketPanel.add(createSectionHeader("PAYMENT INFORMATION"), gbc);
        gbc.gridy = 14; ticketPanel.add(createDataRow("Total Paid:", "₱" + String.format("%.2f", ticket.getFinalFare())), gbc);
        gbc.gridy = 15; ticketPanel.add(createDataRow("Payment Method:", ticket.getPaymentMethod()), gbc);
        String txId = (ticket.getTransactionId() != null) ? ticket.getTransactionId() : "TXN-000000";
        gbc.gridy = 16; ticketPanel.add(createDataRow("Transaction ID:", txId), gbc);
        gbc.gridy = 17; gbc.insets = new Insets(15, 0, 15, 0); ticketPanel.add(createDotDivider(), gbc);

        // --- DARK SEAT SECTION ---
        JPanel darkSeatSection = new JPanel();
        darkSeatSection.setLayout(new BoxLayout(darkSeatSection, BoxLayout.Y_AXIS));
        darkSeatSection.setBackground(DARK_SECTION_BG);
        darkSeatSection.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        darkSeatSection.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSeatTitle = new JLabel("ASSIGNED SEAT", SwingConstants.CENTER);
        lblSeatTitle.setForeground(new Color(200, 200, 200));
        lblSeatTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblSeatTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSeatNum = new JLabel(ticket.getSeatNumber(), SwingConstants.CENTER);
        lblSeatNum.setForeground(Color.WHITE);
        lblSeatNum.setFont(new Font("SansSerif", Font.BOLD, 60));
        lblSeatNum.setAlignmentX(Component.CENTER_ALIGNMENT);

        darkSeatSection.add(lblSeatTitle);
        darkSeatSection.add(lblSeatNum);
        gbc.gridy = 18; gbc.insets = new Insets(0, -40, 0, -40); // Extend to edges
        ticketPanel.add(darkSeatSection, gbc);

        // --- BOTTOM SECTION (QR & DONE) ---
        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setBackground(DARK_SECTION_BG); // Dark BG for continuity
        bottomSection.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblScan = new JLabel("SCAN TO BOARD", SwingConstants.CENTER);
        lblScan.setForeground(TEXT_SECONDARY);
        lblScan.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblScan.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblScan.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        bottomSection.add(lblScan);

        // Real QR Code generation
        bottomSection.add(createQRCodePanel(txId));

        JLabel lblQRData = new JLabel("QR Code: " + txId, SwingConstants.CENTER);
        lblQRData.setForeground(TEXT_SECONDARY);
        lblQRData.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblQRData.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblQRData.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        bottomSection.add(lblQRData);

        gbc.gridy = 19; gbc.insets = new Insets(15, -40, 15, -40); ticketPanel.add(createDashedDivider(), gbc);

        // --- BUTTON (Assembly outside main ticket) ---
        JButton btnDone = new JButton("DONE / BACK TO START");
        btnDone.setBackground(Color.BLACK); // Image shows black button
        btnDone.setForeground(Color.WHITE);
        btnDone.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnDone.setPreferredSize(new Dimension(0, 60));
        btnDone.setFocusPainted(false);
        btnDone.setBorderPainted(false);
        btnDone.addActionListener(e -> {
            // Add your DAO save logic here if needed
            new IdentifyFerryUI(this.dataSource).setVisible(true);
            this.dispose();
        });

        // --- FINAL ASSEMBLY ---
        JScrollPane scroll = new JScrollPane(ticketPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Panel to hold the black button over the dark background
        JPanel footerWrapper = new JPanel(new BorderLayout());
        footerWrapper.setBackground(DARK_SECTION_BG);
        footerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));
        footerWrapper.add(btnDone, BorderLayout.CENTER);

        add(scroll, BorderLayout.CENTER);
        add(footerWrapper, BorderLayout.SOUTH);
    }

    // --- HELPER METHODS FOR LAYOUT MATTERY ---

    private JPanel createDataRow(String key, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(400, 20));
        JLabel lblK = new JLabel(key);
        lblK.setFont(SANS_SERIF_PLAIN_12);
        lblK.setForeground(TEXT_MAIN);
        JLabel lblV = new JLabel(value != null ? value : "---", SwingConstants.RIGHT);
        lblV.setFont(SANS_SERIF_BOLD_12);
        lblV.setForeground(TEXT_MAIN);
        p.add(lblK, BorderLayout.WEST);
        p.add(lblV, BorderLayout.EAST);
        return p;
    }

    private JLabel createSectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(TEXT_MAIN);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return l;
    }

    private JLabel createDashedDivider() {
        return new JLabel("- - - - - - - - - - - - - - - - - - - - - - -", SwingConstants.CENTER) {{
            setForeground(new Color(200, 200, 200));
            setFont(new Font("Monospaced", Font.BOLD, 16));
        }};
    }

    private JLabel createDotDivider() {
        return new JLabel(".................................................", SwingConstants.CENTER) {{
            setForeground(new Color(220, 220, 220));
            setFont(new Font("Monospaced", Font.BOLD, 16));
        }};
    }

    private JPanel createQRCodePanel(String data) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE); // Image shows QR on white box
        p.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        p.setMaximumSize(new Dimension(160, 160));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 0); // No margin inside the white box
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 150, 150, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            p.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
        } catch (Exception e) {
            p.add(new JLabel("QR ERROR"), BorderLayout.CENTER);
        }
        return p;
    }
}