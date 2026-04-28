package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.mycompany.automatedferryticketingsystem.model.Trip;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

/**
 * CORE LOGIC: Central hub sa Admin.
 * Handles navigation using CardLayout (Manage, Vessel, Sales views).
 */
public class FerryAdminDashboard extends JFrame {
    private final VesselDAO vesselDao;
    private JTable tripTable;
    private DefaultTableModel tableModel, statusTableModel, salesTableModel;
    private JLabel lblTotalRevenue;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton activeSidebarButton = null;
    
    private final Color SIDEBAR_ACTIVE_COLOR = new Color(135, 206, 250); 
    private final Color SIDEBAR_DEFAULT_BG = new Color(60, 60, 60);
    private final Color DASHBOARD_DARK = new Color(35, 35, 35);

    public FerryAdminDashboard(VesselDAO vesselDao) {
        this.vesselDao = vesselDao;
        initUI();
        refreshTripData(); // Automatic data fetch in background on start
    }

    // UI Logic: Sets up the main window structure and navigation
    private void initUI() {
        setTitle("AUTOMATED FERRY TICKETING SYSTEM - ADMIN");
        setSize(1300, 750); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header: Logic for Logout and Title display
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 20, 20));
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel lblTitle = new JLabel("ADMIN DASHBOARD CONTROL");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton btnLogout = new JButton("LOGOUT");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            LogoutUI logoutDialog = new LogoutUI(this);
            logoutDialog.setVisible(true);
            if (logoutDialog.isConfirmed()) {
                this.dispose();
                new IdentifyFerryUI(vesselDao).setVisible(true);
            }
        });

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);

        // Sidebar: Container for navigation buttons
        JPanel sidebar = new JPanel();
        sidebar.setBackground(DASHBOARD_DARK);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));

        sidebar.add(createSidebarButton("Manage Trips", SIDEBAR_ACTIVE_COLOR, Color.BLACK, "MANAGE"));
        sidebar.add(createSidebarButton("Vessel Status", SIDEBAR_DEFAULT_BG, Color.WHITE, "VESSEL"));
        sidebar.add(createSidebarButton("Sales Reports", SIDEBAR_DEFAULT_BG, Color.WHITE, "SALES"));

        // Card Panel: Logic for switching between different admin panels
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(DASHBOARD_DARK);
        cardPanel.add(createManageTripsPanel(), "MANAGE");
        cardPanel.add(createVesselStatusPanel(), "VESSEL");
        cardPanel.add(createSalesReportsPanel(), "SALES");

        add(header, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }

    // Manage Panel Logic: UI for adding, updating, and deleting ferry trips
    private JPanel createManageTripsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(210, 215, 225));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionPanel.setOpaque(false);

        JButton btnAdd = createActionButton("ADD NEW", Color.WHITE, Color.BLACK);
        JButton btnSave = createActionButton("SAVE NEW", Color.WHITE, Color.BLACK);
        JButton btnUpdate = createActionButton("UPDATE", Color.WHITE, Color.BLACK);
        JButton btnDelete = createActionButton("DELETE", Color.WHITE, new Color(180, 0, 0));
        JButton btnArchive = createActionButton("ARCHIVE", Color.WHITE, new Color(80, 80, 80));
        
        // Add Row Logic: Inserts a temporary "PENDING" row in the table model
        btnAdd.addActionListener(e -> {
            tableModel.insertRow(0, new Object[]{"PENDING", "ENTER NAME", "ROUTE", "Fastcraft", "In Service", "00:00", "00:00", "TBD", "0.00", "300", "300", "Available"});
            tripTable.setRowSelectionInterval(0, 0);
        });
        
        btnSave.addActionListener(e -> handleSaveNewTrip());
        btnUpdate.addActionListener(e -> handleUpdateTrip());
        btnDelete.addActionListener(e -> handleDeletePermanent());
        btnArchive.addActionListener(e -> handleArchiveTrip());

        actionPanel.add(btnAdd); actionPanel.add(btnSave); actionPanel.add(btnUpdate); 
        actionPanel.add(btnDelete); actionPanel.add(btnArchive);

        String[] cols = {"ID", "Vessel Name", "Route", "Vessel Type", "Status", "ETD", "ETA", "Pier", "Price", "Capacity", "Remaining", "Trip Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c != 0 && c != 4 && c != 10 && c != 11; }
        };

        tripTable = new JTable(tableModel);
        tripTable.setRowHeight(40);
        tripTable.getColumnModel().getColumn(0).setMinWidth(0);
        tripTable.getColumnModel().getColumn(0).setMaxWidth(0);

        panel.add(actionPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tripTable), BorderLayout.CENTER);
        return panel;
    }

    // Sales Panel Logic: Displays total revenue and transaction history
    private JPanel createSalesReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(210, 215, 225));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        lblTotalRevenue = new JLabel("TOTAL REVENUE: ₱0.00");
        lblTotalRevenue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        JButton btnViewSales = createActionButton("VIEW ANALYTICS", Color.WHITE, new Color(0, 102, 204));
        btnViewSales.addActionListener(e -> openSalesAnalyticsDialog());

        headerPanel.add(lblTotalRevenue, BorderLayout.WEST);
        headerPanel.add(btnViewSales, BorderLayout.EAST);

        String[] columns = {"ID", "Passenger", "Route", "Vessel", "Timestamp", "Fare"};
        salesTableModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTable(salesTableModel)), BorderLayout.CENTER);
        return panel;
    }

    // Analytics Logic: Draws dynamic bar charts based on Database revenue
    private void openSalesAnalyticsDialog() {
        JDialog dialog = new JDialog(this, "Sales Analytics", true);
        dialog.setSize(1000, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(15, 15));

        Map<String, Double> revenueMap = vesselDao.getRevenuePerTrip();
        double maxRev = revenueMap.values().stream().max(Double::compare).orElse(1.0);

        JPanel chartArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int startX = 80, base = getHeight() - 50;
                for (Map.Entry<String, Double> entry : revenueMap.entrySet()) {
                    int h = (int) (entry.getValue() / maxRev * (getHeight() - 150));
                    g2.setColor(new Color(30, 144, 255));
                    g2.fillRect(startX, base - h, 50, h);
                    g2.setColor(Color.BLACK);
                    g2.drawString(entry.getKey(), startX - 10, base + 20);
                    startX += 100;
                }
            }
        };
        dialog.add(chartArea, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    /**
     * SWING WORKER (SALES DATA):
     * doInBackground: Runs SQL queries (Non-blocking).
     * done: Updates UI components like salesTableModel in the EDT.
     */
    private void refreshSalesData() {
        new SwingWorker<Void, Void>() {
            List<Object[]> manifesto;
            double revenue;

            @Override protected Void doInBackground() {
                manifesto = vesselDao.getPassengerManifesto();
                revenue = vesselDao.getTotalSales();
                return null;
            }

            @Override protected void done() {
                salesTableModel.setRowCount(0);
                for (Object[] row : manifesto) salesTableModel.addRow(row);
                lblTotalRevenue.setText("TOTAL REVENUE: ₱" + String.format("%.2f", revenue));
            }
        }.execute();
    }

    /**
     * SWING WORKER (TRIP DATA):
     * Logic: Fetches trip list from DB in background to keep GUI responsive.
     * Updates tripTable and statusTableModel after task completion.
     */
    public void refreshTripData() {
        new SwingWorker<List<Trip>, Void>() {
            @Override protected List<Trip> doInBackground() { return vesselDao.getAllTrips(); }
            @Override protected void done() {
                try {
                    List<Trip> list = get(); 
                    tableModel.setRowCount(0);
                    if (statusTableModel != null) statusTableModel.setRowCount(0);
                    for (Trip t : list) {
                        tableModel.addRow(new Object[]{ t.getTripId(), t.getVesselName(), t.getRoute(), t.getVesselType(), t.getVesselStatus(), t.getEtd(), t.getEta(), t.getPierNo(), t.getBaseFare(), t.getCapacity(), t.getSeatsAvailable(), t.getTripStatus() });
                        if (statusTableModel != null) statusTableModel.addRow(new Object[]{ t.getVesselId(), t.getVesselName(), t.getVesselStatus(), t.getTripStatus(), "UPDATE" });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // CRUD Methods: Logic to sync UI table changes with the Database
    private void handleSaveNewTrip() {
        if (tripTable.isEditing()) tripTable.getCellEditor().stopCellEditing();
        int row = tripTable.getSelectedRow();
        if (row != -1 && "PENDING".equals(tableModel.getValueAt(row, 0))) {
            if (vesselDao.addTrip(mapRowToTrip(row))) refreshTripData();
        }
    }

    private void handleUpdateTrip() {
        if (tripTable.isEditing()) tripTable.getCellEditor().stopCellEditing();
        int row = tripTable.getSelectedRow();
        if (row != -1 && !tableModel.getValueAt(row, 0).equals("PENDING")) {
            Trip t = mapRowToTrip(row);
            t.setTripId((int) tableModel.getValueAt(row, 0));
            if (vesselDao.updateTrip(t)) refreshTripData();
        }
    }

    // Mapper Logic: Reads specific table cells and creates a Trip Object
    private Trip mapRowToTrip(int row) {
        Trip t = new Trip();
        t.setVesselName(getVal(row, 1, "Unknown"));
        t.setRoute(getVal(row, 2, "Undefined"));
        t.setBaseFare(Double.parseDouble(getVal(row, 8, "0.0")));
        int cap = Integer.parseInt(getVal(row, 9, "300"));
        t.setCapacity(cap); t.setSeatsAvailable(cap);
        return t;
    }

    private String getVal(int r, int c, String d) {
        Object o = tableModel.getValueAt(r, c);
        return (o == null || o.toString().isEmpty()) ? d : o.toString();
    }

    // Status Panel Logic: Monitoring of vessel physical and operational states
    private JPanel createVesselStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(210, 215, 225));
        statusTableModel = new DefaultTableModel(new String[]{"ID", "Vessel", "Condition", "Trip Status", "Control"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable st = new JTable(statusTableModel);
        st.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (st.getSelectedColumn() == 4) openStatusUpdateDialog(st.getSelectedRow());
            }
        });
        panel.add(new JScrollPane(st), BorderLayout.CENTER);
        return panel;
    }

    // Admin Action Logic: Updates multiple database tables in one workflow
    private void openStatusUpdateDialog(int row) {
        int vId = (int) statusTableModel.getValueAt(row, 0);
        JComboBox<String> vC = new JComboBox<>(new String[]{"In Service", "Maintenance", "Grounded"});
        JComboBox<String> tS = new JComboBox<>(new String[]{"Available", "Cancelled", "Delayed"});
        
        Object[] msg = { "Vessel Condition:", vC, "Trip Operational Status:", tS };
        if (JOptionPane.showConfirmDialog(this, msg, "System Control", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String threat = (vC.getSelectedItem().equals("Grounded")) ? "High" : "Low";
            if (vesselDao.executeAdminAction(vId, -1, (String)tS.getSelectedItem(), threat, "Admin Update", threat.equals("High"))) {
                refreshTripData();
            }
        }
    }

    // Delete Logic: Permanent data removal from Database via DAO
    private void handleDeletePermanent() {
        int row = tripTable.getSelectedRow();
        if (row != -1) {
            Object id = tableModel.getValueAt(row, 0);
            if (!id.equals("PENDING") && vesselDao.deleteTripPermanently((int)id)) refreshTripData();
            else tableModel.removeRow(row);
        }
    }

    // Archive Logic: Soft-delete that hides trips from public users
    private void handleArchiveTrip() {
        int row = tripTable.getSelectedRow();
        if (row != -1 && !tableModel.getValueAt(row, 0).equals("PENDING")) {
            if (vesselDao.archiveTrip((int)tableModel.getValueAt(row, 0))) refreshTripData();
        }
    }

    // Sidebar Factory Logic: Handles visual highlighting and view swapping
    private JButton createSidebarButton(String text, Color bg, Color fg, String cardName) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(180, 50));
        btn.setBackground(bg); btn.setForeground(fg);
        btn.addActionListener(e -> {
            if (activeSidebarButton != null) { 
                activeSidebarButton.setBackground(SIDEBAR_DEFAULT_BG); 
                activeSidebarButton.setForeground(Color.WHITE); 
            }
            activeSidebarButton = btn; 
            btn.setBackground(SIDEBAR_ACTIVE_COLOR); 
            btn.setForeground(Color.BLACK);
            cardLayout.show(cardPanel, cardName);
            if (cardName.equals("SALES")) refreshSalesData();
        });
        return btn;
    }

    // Button Factory: Consistent styling for Action buttons
    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(130, 35));
        btn.setBackground(bg); btn.setForeground(fg);
        return btn;
    }
}