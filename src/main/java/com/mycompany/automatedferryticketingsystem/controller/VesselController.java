package com.mycompany.automatedferryticketingsystem.controller;

import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.mycompany.automatedferryticketingsystem.model.Vessel;
import com.mycompany.automatedferryticketingsystem.view.IdentifyFerryUI;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.awt.Color;

public class VesselController {
    private VesselDAO dao;
    private IdentifyFerryUI view;

    public VesselController(IdentifyFerryUI view, VesselDAO dao) {
        this.view = view;
        this.dao = dao;
    }

    // Main Logic: Fetch data from DB based on search keywords
    public void loadVesselData(String searchTerm) {
        // UI Feedback: Disable button para dili mag-double click ang user
        view.getBtnProceed().setText("FETCHING...");
        view.getBtnProceed().setEnabled(false);
        view.getLblStatus().setText("● System Status: Updating...");
        view.getLblStatus().setForeground(Color.ORANGE);

        // SwingWorker: Para dili mo-freeze ang window samtang nag-loading
        SwingWorker<List<Vessel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vessel> doInBackground() throws Exception {
                // Run SQL query in the background
                return dao.searchVessels(searchTerm);
            }

            @Override
            protected void done() {
                try {
                    // Refresh table with real data from DB
                    List<Vessel> vessels = get(); 
                    updateTable(vessels);
                    
                    view.getLblStatus().setText("● System Status: Online");
                    view.getLblStatus().setForeground(new Color(0, 204, 102));
                } catch (Exception e) {
                    // Show error if connection fails
                    view.getLblStatus().setText("● System Status: Error");
                    view.getLblStatus().setForeground(Color.RED);
                } finally {
                    // Re-enable button after loading
                    view.getBtnProceed().setText("PROCEED");
                    view.getBtnProceed().setEnabled(true);
                }
            }
        };
        worker.execute(); 
    }

    // Table Logic: Insert data objects into JTable rows
    private void updateTable(List<Vessel> vessels) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0); // Limpyohon ang table columns

        for (Vessel v : vessels) {
            // Dynamic Data: Tanan info gikan na sa Database (No hardcoding)
            model.addRow(new Object[]{
                v.getVesselName(),      
                v.getRoute(),           
                v.getVesselType(),      
                v.getStatus(),          
                v.getDepartureTime(),   
                v.getCapacity() + " Max", 
                v.getRemainingSeats() + " Left" 
            });
        }
    }
}
