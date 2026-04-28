package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.model.Ticket;
import com.mycompany.automatedferryticketingsystem.dao.TicketDAO;
import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.zaxxer.hikari.HikariDataSource;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [INHERITANCE] - Kini nga class naggamit sa JFrame aron mahimong usa ka 
 * ganap nga Window/UI component.
 * * [ABSTRACTION] - Gi-hide niini ang proseso sa pag-generate sa QR Code 
 * ug ang pag-save sa database. Ang user makakita ra sa "Done" button.
 */
public class FinalTicketUI extends JFrame {
    
    // [ENCAPSULATION] - Gi-private ang data fields aron dili ma-access 
    // o ma-usab sa ubang screens nga walay permiso.
    private Ticket ticket;
    private VesselDAO dao;

    public FinalTicketUI(VesselDAO dao, Ticket ticket) {
        this.dao = dao;
        this.ticket = ticket;
        HikariDataSource ds = dao.getDataSource();

        // 1. DATA SYNC LOGIC
        if (ticket.getSeatNumber() == null || ticket.getSeatNumber().isEmpty() || ticket.getSeatNumber().equals("null")) {
            TicketDAO tempDao = new TicketDAO(ds);
            int nextSeat = tempDao.getNextSeatNumber(ticket.getTripId());
            ticket.setSeatNumber("S-" + nextSeat);
        }

        setTitle("OFFICIAL TICKET - " + ticket.getTransactionId());
        setSize(450, 950); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false); 
        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);

        // --- 1. HEADER SECTION ---
        mainContainer.add(Box.createRigidArea(new Dimension(0, 25)));
        JLabel shipIcon = new JLabel("🚢", SwingConstants.CENTER); 
        shipIcon.setFont(new Font("SansSerif", Font.PLAIN, 40));
        shipIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(shipIcon);

        JLabel lblTitle = new JLabel("OFFICIAL TICKET", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(lblTitle);

        mainContainer.add(createDottedSeparator());

        // --- 2. INFORMATION GROUPS ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(createSectionTitle("PASSENGER SUMMARY"));
        infoPanel.add(createDataRow("Name:", ticket.getPassengerName().toUpperCase()));
        infoPanel.add(createDataRow("Voyage ID:", "VY-" + ticket.getTransactionId().substring(ticket.getTransactionId().length() - 6)));
        infoPanel.add(createDottedSeparator());

        infoPanel.add(createSectionTitle("VESSEL INFORMATION"));
        infoPanel.add(createDataRow("Vessel:", ticket.getVesselName()));
        infoPanel.add(createDataRow("Route:", ticket.getRoute()));
        infoPanel.add(createDataRow("Departure:", ticket.getDepartureTime()));
        infoPanel.add(createDataRow("Pier/Gate:", ticket.getPierNo())); 
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String currentDate = LocalDateTime.now().format(dtf);
        infoPanel.add(createDataRow("Date:", currentDate)); 
        
        infoPanel.add(createDottedSeparator());

        infoPanel.add(createSectionTitle("PAYMENT INFORMATION"));
        infoPanel.add(createDataRow("Total Paid:", "₱" + String.format("%.2f", ticket.getFinalFare())));
        infoPanel.add(createDataRow("Payment Method:", ticket.getPaymentMethod()));
        infoPanel.add(createDataRow("Transaction ID:", ticket.getTransactionId()));

        mainContainer.add(infoPanel);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- 3. ASSIGNED SEAT BLACK BOX ---
        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new BoxLayout(seatPanel, BoxLayout.Y_AXIS));
        seatPanel.setBackground(Color.BLACK);
        seatPanel.setAlignmentX(Component.CENTER_ALIGNMENT); 
        seatPanel.setMaximumSize(new Dimension(450, 150));
        
        JLabel lblSeatTitle = new JLabel("ASSIGNED SEAT");
        lblSeatTitle.setForeground(Color.WHITE);
        lblSeatTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblSeatTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSeatTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));

        JLabel lblSeatNumber = new JLabel(ticket.getSeatNumber());
        lblSeatNumber.setForeground(Color.WHITE);
        lblSeatNumber.setFont(new Font("SansSerif", Font.BOLD, 72));
        lblSeatNumber.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSeatNumber.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        seatPanel.add(lblSeatTitle);
        seatPanel.add(lblSeatNumber);
        mainContainer.add(seatPanel);

        // --- 4. SCAN SECTION ---
        JPanel scanPanel = new JPanel();
        scanPanel.setLayout(new BoxLayout(scanPanel, BoxLayout.Y_AXIS));
        scanPanel.setBackground(new Color(33, 33, 33)); 
        scanPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scanPanel.setMaximumSize(new Dimension(450, 250));
        
        JLabel lblScan = new JLabel("SCAN TO BOARD");
        lblScan.setForeground(Color.LIGHT_GRAY);
        lblScan.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblScan.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblScan.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        scanPanel.add(lblScan);

        // [ABSTRACTION] - Gi-call lang ang method, tago ang complexity sa QR matrix logic.
        JLabel qrLabel = new JLabel(generateQRCode(ticket.getTransactionId(), 150, 150));
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setOpaque(true);
        qrLabel.setBackground(Color.WHITE);
        qrLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 8));
        scanPanel.add(qrLabel);

        JLabel lblQRText = new JLabel("QR Code: BRD-" + ticket.getTransactionId());
        lblQRText.setForeground(Color.WHITE);
        lblQRText.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lblQRText.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblQRText.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        scanPanel.add(lblQRText);

        mainContainer.add(scanPanel);
        mainContainer.add(createDottedSeparator());

        // --- 5. FOOTER BUTTON ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        footerPanel.setBackground(Color.WHITE);
        
        JButton btnDone = new JButton("DONE / BACK TO START");
        btnDone.setPreferredSize(new Dimension(350, 50));
        btnDone.setBackground(Color.BLACK);
        btnDone.setForeground(Color.WHITE);
        btnDone.setFocusPainted(false);
        btnDone.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        btnDone.addActionListener(e -> {
            TicketDAO ticketDao = new TicketDAO(ds);
            // [ABSTRACTION] - Save operation implementation is hidden from the UI.
            if (ticketDao.saveTicket(ticket)) {
                new WelcomeScreen().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Database Save Failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        footerPanel.add(btnDone);
        mainContainer.add(footerPanel);

        add(mainContainer, BorderLayout.CENTER);
    }
    
    // [ENCAPSULATION] - Helper methods are private to protect internal component generation.
    private JPanel createDottedSeparator() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                float[] dash = {3f, 3f};
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, dash, 0f));
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawLine(20, 10, getWidth() - 20, 10);
            }
        };
        panel.setPreferredSize(new Dimension(400, 20));
        panel.setMaximumSize(new Dimension(2000, 20));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JLabel createSectionTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(Color.DARK_GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT); 
        return lbl;
    }

    private JPanel createDataRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(500, 25));
        p.setAlignmentX(Component.LEFT_ALIGNMENT); 
        JLabel lblL = new JLabel(label);
        lblL.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel lblV = new JLabel(value != null ? value : "---");
        lblV.setFont(new Font("SansSerif", Font.BOLD, 12));
        p.add(lblL, BorderLayout.WEST);
        p.add(lblV, BorderLayout.EAST);
        return p;
    }

    private ImageIcon generateQRCode(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return new ImageIcon(bufferedImage);
        } catch (Exception e) {
            return null;
        }
    }
}