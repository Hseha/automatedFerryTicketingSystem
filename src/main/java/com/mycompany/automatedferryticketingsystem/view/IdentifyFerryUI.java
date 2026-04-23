package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.controller.VesselController;
import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.zaxxer.hikari.HikariDataSource;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IdentifyFerryUI extends JFrame {
    private JTable ferryTable;
    private DefaultTableModel tableModel;
    private JButton prcdbtn, btnAdmin;
    private JLabel lblStatus, lblSystemGreeting;
    private JTextField txtSearch;
    private HikariDataSource dataSource;

    public IdentifyFerryUI(HikariDataSource dataSource) {
        this.dataSource = dataSource;

        // --- WINDOW SETUP ---
        setTitle("AUTOMATED FERRY TICKETING SYSTEM");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 240, 240));
        setLayout(new BorderLayout());

        // --- NORTH PANEL (Original Flat Header #2D2D30) ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(45, 45, 48)); 
        northPanel.setPreferredSize(new Dimension(1000, 110));
        northPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("IDENTIFY FERRY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(Color.WHITE);

        lblSystemGreeting = new JLabel("WELCOME, PASSENGER. PLEASE SELECT AN AVAILABLE VOYAGE FROM THE LIST BELOW.");
        lblSystemGreeting.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSystemGreeting.setForeground(new Color(160, 160, 160));

        titlePanel.add(lblTitle);
        titlePanel.add(lblSystemGreeting);

        txtSearch = new JTextField("Search vessel, route, or type...");
        txtSearch.setPreferredSize(new Dimension(280, 35));
        
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        searchWrapper.setOpaque(false);
        searchWrapper.add(txtSearch);

        northPanel.add(titlePanel, BorderLayout.WEST);
        northPanel.add(searchWrapper, BorderLayout.EAST);

        // --- CENTER PANEL (Table Area) ---
        String[] cols = {"ID", "Vessel Name", "Route", "Vessel Type", "Status", "Departure", "Total", "Remaining", "Trip Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ferryTable = new JTable(tableModel);
        ferryTable.setRowHeight(40);
        ferryTable.setShowGrid(false);
        ferryTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Hide ID Column
        ferryTable.getColumnModel().getColumn(0).setMinWidth(0);
        ferryTable.getColumnModel().getColumn(0).setMaxWidth(0);

        // Header Styling
        JTableHeader header = ferryTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 249, 250));
        header.setPreferredSize(new Dimension(0, 45));

        // APPLY CUSTOM RENDERERS FOR DYNAMIC COLORS
        ferryTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer()); 
        ferryTable.getColumnModel().getColumn(7).setCellRenderer(new RemainingRenderer()); 

        JScrollPane sp = new JScrollPane(ferryTable);
        sp.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        sp.getViewport().setBackground(Color.WHITE);

        add(northPanel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        // --- SOUTH PANEL (Original Dark Footer) ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(45, 45, 48)); 
        footer.setPreferredSize(new Dimension(1000, 75));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));

        // Admin Login with Redirect Logic
        btnAdmin = new JButton("ADMIN LOGIN");
        btnAdmin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdmin.setForeground(new Color(150, 150, 150));
        btnAdmin.setContentAreaFilled(false);
        btnAdmin.setBorderPainted(false);
        btnAdmin.setFocusPainted(false);
        btnAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Blue Hover and Redirect
        btnAdmin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnAdmin.setForeground(new Color(100, 200, 255)); }
            public void mouseExited(MouseEvent e) { btnAdmin.setForeground(new Color(150, 150, 150)); }
        });

        btnAdmin.addActionListener(e -> {
            // REDIRECT TO ADMIN LOGIN UI
            new AdminLoginUI(this.dataSource).setVisible(true);
            this.dispose();
        });

        // Center: System Online Status
        lblStatus = new JLabel("● Online");
        lblStatus.setForeground(new Color(0, 204, 102)); 
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        // Proceed Button
        prcdbtn = new JButton("PROCEED");
        prcdbtn.setBackground(new Color(60, 60, 60));
        prcdbtn.setForeground(Color.WHITE);
        prcdbtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        prcdbtn.setPreferredSize(new Dimension(140, 40));
        prcdbtn.setFocusPainted(false);

        footer.add(btnAdmin, BorderLayout.WEST);
        footer.add(lblStatus, BorderLayout.CENTER);
        footer.add(prcdbtn, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        // Controller logic
        VesselDAO dao = new VesselDAO(dataSource);
        new VesselController(this, dao, this.dataSource).loadVesselData("");
    }

    // --- TABLE COLOR LOGIC ---
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            String val = String.valueOf(v);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (val.equalsIgnoreCase("In Service")) l.setForeground(new Color(0, 180, 100));
            else if (val.equalsIgnoreCase("Maintenance") || val.contains("Repair")) l.setForeground(new Color(220, 53, 69));
            else l.setForeground(Color.BLACK);
            return l;
        }
    }

    class RemainingRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            String val = String.valueOf(v);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            
            try {
                if (val.equals("FULL")) {
                    l.setForeground(Color.RED);
                } else {
                    int count = Integer.parseInt(val);
                    if (count >= 100) l.setForeground(new Color(0, 180, 100)); // Plenty
                    else if (count > 0) l.setForeground(Color.ORANGE); // Low
                    else { l.setText("FULL"); l.setForeground(Color.RED); }
                }
            } catch (Exception e) { l.setForeground(Color.BLACK); }
            return l;
        }
    }

    // --- GETTERS (Crucial for VesselController to compile) ---
    public JTable getFerryTable() { return ferryTable; }
    public JButton getBtnProceed() { return prcdbtn; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JLabel getLblStatus() { return lblStatus; }
}