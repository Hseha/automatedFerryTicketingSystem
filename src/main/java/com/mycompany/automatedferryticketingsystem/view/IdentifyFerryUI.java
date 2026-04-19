package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.controller.VesselController; 
import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;           
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class IdentifyFerryUI extends JFrame {
    private JTable ferryTable;
    private DefaultTableModel tableModel;
    private JButton prcdbtn, btnAdmin; 
    private JLabel lblStatus, lblSystemGreeting;
    private JTextField txtSearch;

    public IdentifyFerryUI() {
        // --- WINDOW SETUP ---
        setTitle("AUTOMATED FERRY TICKETING SYSTEM");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(236, 236, 236));
        setLayout(new BorderLayout());

        // --- NORTH PANEL: Responsive Header ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(45, 45, 48)); 
        northPanel.setPreferredSize(new Dimension(1000, 100));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 0));

        JLabel lblTitle = new JLabel("IDENTIFY FERRY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);

        lblSystemGreeting = new JLabel("WELCOME, PASSENGER. PLEASE SELECT AN AVAILABLE VOYAGE FROM THE LIST BELOW.");
        lblSystemGreeting.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSystemGreeting.setForeground(new Color(180, 180, 180));

        titlePanel.add(lblTitle);
        titlePanel.add(lblSystemGreeting);

        // Styled Search Field
        txtSearch = new JTextField("Search vessel, route, or type...", 20);
        txtSearch.setPreferredSize(new Dimension(250, 35));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        
        JPanel searchBoxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 35));
        searchBoxPanel.setOpaque(false);
        searchBoxPanel.add(txtSearch);

        northPanel.add(titlePanel, BorderLayout.WEST);
        northPanel.add(searchBoxPanel, BorderLayout.EAST);

        // --- CENTER PANEL: Responsive Table Wrapper ---
        String[] cols = {"Vessel Name", "Route", "Vessel Type", "Status", "Departure", "Total", "Remaining", "Trip Status"};
        tableModel = new DefaultTableModel(cols, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        ferryTable = new JTable(tableModel);
        styleTable();
        applyTableRenderers();

        JScrollPane sp = new JScrollPane(ferryTable);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        sp.getViewport().setBackground(new Color(236, 236, 236));

        // Using a wrapper keeps the table from looking "too wide" on full screen
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.add(sp, BorderLayout.CENTER);

        // --- SOUTH PANEL: Footer ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(45, 45, 48)); 
        footer.setPreferredSize(new Dimension(1000, 60));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        // ADMIN LOGIN
        btnAdmin = new JButton("ADMIN LOGIN");
        btnAdmin.setForeground(Color.GRAY);
        btnAdmin.setContentAreaFilled(false);
        btnAdmin.setBorderPainted(false);
        btnAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Admin Hover Effect
        btnAdmin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnAdmin.setForeground(new Color(100, 180, 255)); }
            public void mouseExited(MouseEvent e) { btnAdmin.setForeground(Color.GRAY); }
        });

        lblStatus = new JLabel("● System Status: Online");
        lblStatus.setForeground(new Color(0, 255, 127)); 

        // PROCEED BUTTON (Updated with Hover logic)
        prcdbtn = new JButton("PROCEED");
        styleProceedButton(prcdbtn, new Color(60, 60, 60));

        footer.add(btnAdmin, BorderLayout.WEST);
        footer.add(lblStatus, BorderLayout.CENTER);
        footer.add(prcdbtn, BorderLayout.EAST);

        add(northPanel, BorderLayout.NORTH);
        add(tableWrapper, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        // --- INITIALIZE ---
        VesselDAO dao = new VesselDAO();
        VesselController controller = new VesselController(this, dao);
        controller.loadVesselData("");

        setupEvents(controller);
    }

    private void styleProceedButton(JButton btn, Color baseColor) {
        btn.setPreferredSize(new Dimension(130, 35));
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0, 122, 204)); // Bright Blue Hover
                btn.setBorder(BorderFactory.createLineBorder(new Color(100, 180, 255)));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseColor);
                btn.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
            }
            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(new Color(0, 90, 158));
            }
        });
    }

    private void setupEvents(VesselController controller) {
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { controller.loadVesselData(txtSearch.getText()); }
        });
        
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(txtSearch.getText().equals("Search vessel, route, or type...")) txtSearch.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                if(txtSearch.getText().isEmpty()) txtSearch.setText("Search vessel, route, or type...");
            }
        });
    }

    private void styleTable() {
        ferryTable.setRowHeight(45); // Taller rows for readability
        ferryTable.setShowVerticalLines(false);
        ferryTable.setGridColor(new Color(230, 230, 230));
        ferryTable.setSelectionBackground(new Color(220, 235, 250));
        ferryTable.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = ferryTable.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(245, 245, 245));
    }

    private void applyTableRenderers() {
        // Shared logic for Zebra Striping
        DefaultTableCellRenderer globalRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                c.setForeground(new Color(50, 50, 50)); 
                return c;
            }
        };

        for (int i = 0; i < ferryTable.getColumnCount(); i++) {
            ferryTable.getColumnModel().getColumn(i).setCellRenderer(globalRenderer);
        }

        // Status Column logic remains consistent for Service/Maintenance
        ferryTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String valStr = (value != null) ? value.toString() : "";
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                
                if (valStr.equalsIgnoreCase("In Service")) {
                    c.setForeground(new Color(0, 153, 76)); 
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (valStr.equalsIgnoreCase("Maintenance") || valStr.equalsIgnoreCase("Critical Repair")) {
                    c.setForeground(Color.RED); 
                }
                return c;
            }
        });
    }

    // --- GETTERS ---
    public JTable getFerryTable() { return ferryTable; } 
    public JButton getBtnProceed() { return prcdbtn; } 
    public JLabel getLblStatus() { return lblStatus; }
    public JButton getBtnAdmin() { return btnAdmin; }
    public DefaultTableModel getTableModel() { return tableModel; }
}