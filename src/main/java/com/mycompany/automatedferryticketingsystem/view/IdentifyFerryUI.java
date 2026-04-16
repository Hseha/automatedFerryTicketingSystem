package com.mycompany.automatedferryticketingsystem.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IdentifyFerryUI extends JFrame {
    private JTable ferryTable;
    private DefaultTableModel tableModel;
    private JButton btnProceed;
    private JButton btnAdmin;
    private JLabel lblStatus;

    public IdentifyFerryUI() {
        // --- 1. WINDOW SETUP ---
        setTitle("IDENTIFY FERRY");
        setSize(900, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(236, 236, 236));

        // --- 2. NORTH PANEL: Header ---
        JPanel northPanel = new JPanel();
        northPanel.setBackground(new Color(45, 45, 48));
        northPanel.setPreferredSize(new Dimension(900, 80));
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 20)); 

        JLabel lblTitle = new JLabel("IDENTIFY FERRY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE); 
        northPanel.add(lblTitle);

        // --- 3. CENTER PANEL: Table ---
        String[] columns = {"Vessel Name", "Route", "Vessel Type", "Status", "Departure", "Total", "Remaining"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        ferryTable = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(ferryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); 
        scrollPane.getViewport().setBackground(new Color(236, 236, 236));

        // --- 4. SOUTH PANEL: Footer ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(236, 236, 236));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        // Admin Button with Light Blue Hover Effect
        btnAdmin = new JButton("ADMIN LOGIN");
        btnAdmin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdmin.setForeground(new Color(100, 100, 100)); // Default Gray
        btnAdmin.setContentAreaFilled(false);
        btnAdmin.setBorderPainted(false);
        btnAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnAdmin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Change to Light Blue on hover
                btnAdmin.setForeground(new Color(100, 180, 255)); 
            }
            @Override
            public void mouseExited(MouseEvent e) {
                // Back to Gray when mouse leaves
                btnAdmin.setForeground(new Color(100, 100, 100));
            }
        });

        lblStatus = new JLabel("● System Status: Online");
        lblStatus.setForeground(new Color(0, 153, 76));
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        // Proceed Button with English Validation
        btnProceed = new JButton("PROCEED");
        btnProceed.setPreferredSize(new Dimension(150, 40));
        btnProceed.setBackground(new Color(70, 70, 70)); 
        btnProceed.setForeground(Color.LIGHT_GRAY);
        btnProceed.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnProceed.setFocusPainted(false);

        btnProceed.addActionListener(e -> {
            if (ferryTable.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a ferry from the table to proceed.", 
                    "Selection Required", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                System.out.println("Vessel selected. Moving to next stage...");
            }
        });

        footer.add(btnAdmin, BorderLayout.WEST);
        footer.add(lblStatus, BorderLayout.CENTER);
        footer.add(btnProceed, BorderLayout.EAST);

        // --- 5. ASSEMBLY ---
        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);  
        add(scrollPane, BorderLayout.CENTER); 
        add(footer, BorderLayout.SOUTH);      
    }

    private void styleTable() {
        ferryTable.setRowHeight(35); 
        ferryTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JTableHeader h = ferryTable.getTableHeader();
        h.setPreferredSize(new Dimension(0, 35)); 
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setReorderingAllowed(false);
    }

    // --- GETTERS ---
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnProceed() { return btnProceed; }
    public JButton getBtnAdmin() { return btnAdmin; }
    public JLabel getLblStatus() { return lblStatus; }
}