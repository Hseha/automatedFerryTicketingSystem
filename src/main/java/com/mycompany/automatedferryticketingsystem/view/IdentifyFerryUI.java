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
 * Mao ni ang main interface para sa mga pasahero para makapili sila og byahe.
 * (This is the main passenger interface for selecting a voyage.)
 */
public class IdentifyFerryUI extends JFrame {
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

        // Pag-set sa window properties sama sa title ug size.
        // (Setting window properties like title and size.)
        setTitle("AUTOMATED FERRY TICKETING SYSTEM");
        setSize(1200, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center sa screen.
        getContentPane().setBackground(new Color(240, 240, 240));
        setLayout(new BorderLayout());

        // Setup para sa secret shortcut para sa admin login.
        setupSecretAdminAccess();

        // --- NORTH PANEL ---
        // Kini ang top section sa UI para sa title ug search bar.
        // (The top section of the UI for the title and search bar.)
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
        // Pag-setup sa table columns para sa mga detalye sa barko.
        // (Setting up table columns for vessel details.)
        String[] cols = {"ID", "Vessel Name", "Route", "Vessel Type", "Status", "ETD", "ETA", "Pier", "Price", "Capacity", "Remaining", "Trip Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; } // Dili ma-edit ang table cells.
        };
        ferryTable = new JTable(tableModel);
        ferryTable.setRowHeight(45);
        ferryTable.setShowGrid(false);
        ferryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Taguon ang ID column para dili makita sa user.
        // (Hiding the ID column so it's not visible to the user.)
        ferryTable.getColumnModel().getColumn(0).setMinWidth(0);
        ferryTable.getColumnModel().getColumn(0).setMaxWidth(0);

        // --- RENDERERS ---
        // Custom designs para sa status, time, ug remaining seats columns.
        // (Custom designs for status, time, and remaining seats columns.)
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
        // Ang footer section diin makita ang online status ug Proceed button.
        // (The footer section where online status and Proceed button are located.)
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

        // I-connect ang Controller para sa logic handles.
        // (Connect the Controller for handling logic.)
        this.controller = new VesselController(this, this.dao, this.dataSource);
    }

    /**
     * Logic para sa search placeholder text.
     * (Logic for the search placeholder text.)
     */
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
     * Secret key combination (CTRL + SHIFT + A) para maka-access ang admin.
     * (Secret key combination for admin access.)
     */
    private void setupSecretAdminAccess() {
        Action adminAction = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                new AdminLoginUI(dao).setVisible(true);
                dispose(); // Isira ang karon nga window.
            }
        };
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control shift A"), "openAdmin");
        this.getRootPane().getActionMap().put("openAdmin", adminAction);
    }

    /**
     * TimeRenderer Class
     * Para nindot tan-awon ang oras sa byahe (ETD/ETA).
     * (To style the voyage times ETD/ETA.)
     */
    class TimeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            l.setForeground(new Color(0, 153, 76)); // Green color para sa time.
            l.setFont(new Font("Segoe UI", Font.BOLD, 14));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            return l;
        }
    }

    /**
     * StatusRenderer Class
     * Logic para usbon ang text color base sa status sa barko.
     * (Logic to change text color based on vessel status.)
     */
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            String val = (v != null) ? String.valueOf(v) : "";
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Logic: I-green ang "Available" o "In Service".
            // (Logic: Color "Available" or "In Service" green.)
            if (val.equalsIgnoreCase("Available") || val.equalsIgnoreCase("Boarding") || val.equalsIgnoreCase("In Service")) {
                l.setForeground(new Color(0, 180, 100)); 
                if (val.equalsIgnoreCase("Available")) {
                    l.setText("Boarding"); // Usbon ang text para mas formal.
                }
            } else {
                l.setForeground(new Color(220, 53, 69)); // Red kung naay issue o delay.
            }
            return l;
        }
    }

    /**
     * RemainingRenderer Class
     * Logic para i-check kung puno na ba ang barko.
     * (Logic to check if the vessel is already full.)
     */
    class RemainingRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            try {
                int count = Integer.parseInt(String.valueOf(v));
                if (count <= 0) { 
                    l.setText("FULL"); // Kung zero na ang seats, i-mark og FULL.
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

    // --- GETTERS ---
    // Gigamit ni para ma-access ang components gikan sa Controller.
    // (Used to access components from the Controller.)
    public JLabel getLblStatus() { return lblStatus; }
    public JTable getFerryTable() { return ferryTable; }
    public JButton getBtnProceed() { return prcdbtn; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtSearch() { return txtSearch; }
}