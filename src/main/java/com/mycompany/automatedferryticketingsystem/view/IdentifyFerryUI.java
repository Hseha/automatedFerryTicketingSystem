package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.controller.VesselController;
import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.zaxxer.hikari.HikariDataSource;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * IdentifyFerryUI Class
 * [INHERITANCE]: Extends JFrame to become a window component.
 * [COMPOSITION]: Uses VesselDAO and VesselController to handle data and logic.
 */
public class IdentifyFerryUI extends JFrame {
    // [ENCAPSULATION]: Private fields ensure data security.
    private JTable ferryTable;
    private DefaultTableModel tableModel;
    private JButton prcdbtn;
    private JLabel lblStatus, lblSystemGreeting;
    private JTextField txtSearch;
    private HikariDataSource dataSource;
    private VesselDAO dao; 
    private VesselController controller; 

    private static final String SEARCH_HINT = "Search vessel, route, or type...";

    public IdentifyFerryUI(VesselDAO dao) {
        this.dao = dao;
        this.dataSource = dao.getDataSource(); 

        setTitle("AUTOMATED FERRY TICKETING SYSTEM");
        setSize(1200, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        getContentPane().setBackground(new Color(240, 240, 240));
        setLayout(new BorderLayout());

        setupSecretAdminAccess();

        // --- NORTH PANEL ---
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

        txtSearch = new JTextField(SEARCH_HINT);
        txtSearch.setPreferredSize(new Dimension(280, 35));
        txtSearch.setForeground(Color.GRAY);
        setupSearchHintLogic();
        
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        searchWrapper.setOpaque(false);
        searchWrapper.add(txtSearch);

        northPanel.add(titlePanel, BorderLayout.WEST);
        northPanel.add(searchWrapper, BorderLayout.EAST);

        // --- CENTER PANEL (Table) ---
        String[] cols = {"ID", "Vessel Name", "Route", "Vessel Type", "Status", "ETD", "ETA", "Pier", "Price", "Capacity", "Remaining", "Trip Status"};
        
        // [POLYMORPHISM]: Overriding isCellEditable to prevent user modifications to the table grid.
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        ferryTable = new JTable(tableModel);
        ferryTable.setRowHeight(45);
        ferryTable.setShowGrid(false);
        ferryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        ferryTable.getColumnModel().getColumn(0).setMinWidth(0);
        ferryTable.getColumnModel().getColumn(0).setMaxWidth(0);

        // [COMPOSITION]: Assigning custom renderer objects to specific table columns.
        StatusRenderer statusRenderer = new StatusRenderer();
        ferryTable.getColumnModel().getColumn(4).setCellRenderer(statusRenderer); 
        ferryTable.getColumnModel().getColumn(11).setCellRenderer(statusRenderer); 

        TimeRenderer timeRenderer = new TimeRenderer();
        ferryTable.getColumnModel().getColumn(5).setCellRenderer(timeRenderer);     
        ferryTable.getColumnModel().getColumn(6).setCellRenderer(timeRenderer);     
        
        ferryTable.getColumnModel().getColumn(10).setCellRenderer(new RemainingRenderer()); 

        JScrollPane sp = new JScrollPane(ferryTable);
        sp.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        sp.getViewport().setBackground(Color.WHITE);

        add(northPanel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        // --- SOUTH PANEL ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(45, 45, 48)); 
        footer.setPreferredSize(new Dimension(1000, 75));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));

        lblStatus = new JLabel("● Online");
        lblStatus.setForeground(new Color(0, 204, 102)); 
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));

        prcdbtn = new JButton("PROCEED");
        prcdbtn.setBackground(new Color(60, 60, 60));
        prcdbtn.setForeground(Color.WHITE);
        prcdbtn.setFocusPainted(false);
        prcdbtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        footer.add(lblStatus, BorderLayout.CENTER);
        footer.add(prcdbtn, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        this.controller = new VesselController(this, this.dao, this.dataSource);
    }

    private void setupSearchHintLogic() {
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(SEARCH_HINT)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText(SEARCH_HINT);
                }
            }
        });
    }

    /**
     * [ABSTRACTION]: The complex key binding logic is handled by AbstractAction.
     */
    private void setupSecretAdminAccess() {
        Action adminAction = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                new AdminLoginUI(dao).setVisible(true);
                dispose(); 
            }
        };
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control shift A"), "openAdmin");
        this.getRootPane().getActionMap().put("openAdmin", adminAction);
    }

    /**
     * [INHERITANCE]: Inner class inheriting from DefaultTableCellRenderer.
     */
    class TimeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            l.setForeground(new Color(0, 153, 76)); 
            l.setFont(new Font("Segoe UI", Font.BOLD, 14));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            return l;
        }
    }

    /**
     * [POLYMORPHISM]: Logic to change colors based on the data value.
     */
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            String val = (v != null) ? String.valueOf(v) : "";
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (val.equalsIgnoreCase("Available") || val.equalsIgnoreCase("Boarding") || val.equalsIgnoreCase("In Service")) {
                l.setForeground(new Color(0, 180, 100)); 
                if (val.equalsIgnoreCase("Available")) {
                    l.setText("Boarding"); 
                }
            } else {
                l.setForeground(new Color(220, 53, 69)); 
            }
            return l;
        }
    }

    class RemainingRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            try {
                int count = Integer.parseInt(String.valueOf(v));
                if (count <= 0) { 
                    l.setText("FULL"); 
                    l.setForeground(new Color(220, 53, 69)); 
                } else { 
                    l.setForeground(new Color(0, 180, 100)); 
                }
            } catch (Exception e) { 
                l.setForeground(Color.BLACK); 
            }
            l.setHorizontalAlignment(SwingConstants.CENTER);
            return l;
        }
    }

    // [ENCAPSULATION]: Public getters provide controlled access to private components.
    public JLabel getLblStatus() { return lblStatus; }
    public JTable getFerryTable() { return ferryTable; }
    public JButton getBtnProceed() { return prcdbtn; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtSearch() { return txtSearch; }
}