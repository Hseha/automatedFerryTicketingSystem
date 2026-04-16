package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.model.Ticket;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class IdentifyFerryUI extends JFrame {
    // --- UI COMPONENTS: Ang mga gamit sa screen ---
    private JTable ferryTable;            // Ang grid nga display sa barko
    private DefaultTableModel tableModel; // Ang "utok" sa table (handles data)
    private JButton btnProceed;           // Button para sunod screen
    private JButton btnAdmin;             // Button para sa staff
    private JLabel lblStatus;             // Indicator kung online ang system

    public IdentifyFerryUI() {
        // --- 1. WINDOW SETUP: basic configuration ---
        setTitle("IDENTIFY FERRY");
        setSize(900, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);                    // I-tunga ang window sa screen
        getContentPane().setBackground(new Color(236, 236, 236)); // Light gray background

        // --- 2. NORTH PANEL: Header section with Title ---
        JPanel northPanel = new JPanel();
        northPanel.setBackground(new Color(45, 45, 48)); // Dark gray header
        northPanel.setPreferredSize(new Dimension(900, 80));
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 20)); 

        JLabel lblTitle = new JLabel("IDENTIFY FERRY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE); 
        northPanel.add(lblTitle);

        // --- 3. CENTER PANEL: The Table Logic ---
        String[] columns = {"Vessel Name", "Route", "Vessel Type", "Status", "Departure", "Total", "Remaining"};
        
        // tableModel Logic: Gi-lock nato (false) para dili ma-typepan sa user ang table
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        ferryTable = new JTable(tableModel);
        styleTable(); // Patahuron ang rows para limpyo tan-awon

        // ScrollPane: Para maka-scroll kung daghan na ang barko
        JScrollPane scrollPane = new JScrollPane(ferryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); 
        scrollPane.getViewport().setBackground(new Color(236, 236, 236));

        // --- 4. SOUTH PANEL: Footer buttons ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(236, 236, 236));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        // Admin Button: Styled like a clickable link
        btnAdmin = new JButton("ADMIN LOGIN");
        btnAdmin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdmin.setForeground(new Color(100, 100, 100));
        btnAdmin.setContentAreaFilled(false); // No background box
        btnAdmin.setBorderPainted(false);
        btnAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Status Label: Real-time feedback kung online ba ang database
        lblStatus = new JLabel("● System Status: Online");
        lblStatus.setForeground(new Color(0, 153, 76)); // Green color
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        // Proceed Button: Primary action para sa booking
        btnProceed = new JButton("PROCEED");
        btnProceed.setPreferredSize(new Dimension(150, 40));
        btnProceed.setBackground(new Color(70, 70, 70)); 
        btnProceed.setForeground(Color.LIGHT_GRAY);
        btnProceed.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnProceed.setFocusPainted(false);

        footer.add(btnAdmin, BorderLayout.WEST);
        footer.add(lblStatus, BorderLayout.CENTER);
        footer.add(btnProceed, BorderLayout.EAST);

        // --- 5. ASSEMBLY: Pag-combine sa tanang panels ---
        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);  
        add(scrollPane, BorderLayout.CENTER); 
        add(footer, BorderLayout.SOUTH);      
    }

    /**
     * STYLING LOGIC: Gi-set ang height sa rows ug header para modern tan-awon.
     */
    private void styleTable() {
        ferryTable.setRowHeight(35); 
        ferryTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JTableHeader h = ferryTable.getTableHeader();
        h.setPreferredSize(new Dimension(0, 35)); 
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setReorderingAllowed(false); // Disable dragging columns
    }

    // --- GETTERS: Bridges para ang Controller maka-access sa UI ---
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnProceed() { return btnProceed; }
    public JButton getBtnAdmin() { return btnAdmin; }
    public JLabel getLblStatus() { return lblStatus; }
}
