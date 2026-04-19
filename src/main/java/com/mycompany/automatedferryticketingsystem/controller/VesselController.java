package com.mycompany.automatedferryticketingsystem.controller;

import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.mycompany.automatedferryticketingsystem.model.Vessel;
import com.mycompany.automatedferryticketingsystem.model.Ticket;
import com.mycompany.automatedferryticketingsystem.view.IdentifyFerryUI;
import com.mycompany.automatedferryticketingsystem.view.PassengerInfoUI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.awt.Color;
import java.awt.event.ActionEvent;

public class VesselController {
    private VesselDAO dao;
    private IdentifyFerryUI view;

    public VesselController(IdentifyFerryUI view, VesselDAO dao) {
        this.view = view;
        this.dao = dao;
        initController(); 
    }

    private void initController() {
        view.getBtnProceed().addActionListener((ActionEvent e) -> {
            handleProceedAction();
        });
    }

    private void handleProceedAction() {
        int selectedRow = view.getFerryTable().getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a voyage to proceed.");
            return;
        }

        // 1. EXTRACT DATA: Correct mapping based on updateTable()
        String vesselName = view.getTableModel().getValueAt(selectedRow, 0).toString();
        String route = view.getTableModel().getValueAt(selectedRow, 1).toString();
        String vesselType = view.getTableModel().getValueAt(selectedRow, 2).toString();
        String status = view.getTableModel().getValueAt(selectedRow, 3).toString();
        String departureTime = view.getTableModel().getValueAt(selectedRow, 4).toString();
        String remaining = view.getTableModel().getValueAt(selectedRow, 6).toString();

        // Operational Checks
        if (status.equalsIgnoreCase("Maintenance") || status.equalsIgnoreCase("Critical Repair")) {
            JOptionPane.showMessageDialog(view, "VESSEL UNAVAILABLE", "Operational Alert", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (remaining.equals("FULL")) {
            JOptionPane.showMessageDialog(view, "VOYAGE FULL", "Booking Limit Reached", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 2. PREPARE TICKET: Providing exactly 7 arguments for your new constructor
        int tempId = 0;
        double baseFare = 250.00; 
        String eta = "2 Hours Voyage";

        Ticket ticket = new Ticket(tempId, vesselName, route, vesselType, departureTime, eta, baseFare);

        // 3. NAVIGATION
        new PassengerInfoUI(ticket).setVisible(true);
        view.dispose();
    }

    public void loadVesselData(String searchTerm) {
        view.getBtnProceed().setText("FETCHING...");
        view.getBtnProceed().setEnabled(false);
        
        SwingWorker<List<Vessel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vessel> doInBackground() throws Exception {
                return dao.getAvailableVessels(searchTerm);
            }

            @Override
            protected void done() {
                try {
                    List<Vessel> vessels = get(); 
                    updateTable(vessels);
                    view.getLblStatus().setText(vessels.isEmpty() ? "● No vessels found" : "● Online");
                    view.getLblStatus().setForeground(vessels.isEmpty() ? Color.GRAY : new Color(0, 204, 102));
                } catch (Exception e) {
                    view.getLblStatus().setText("● Database Error");
                    view.getLblStatus().setForeground(Color.RED);
                } finally {
                    view.getBtnProceed().setText("PROCEED");
                    view.getBtnProceed().setEnabled(true);
                }
            }
        };
        worker.execute(); 
    }

    private void updateTable(List<Vessel> vessels) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0); 

        for (Vessel v : vessels) {
            int remaining = v.getRemainingSeats();
            String remainingDisplay = (remaining <= 0) ? "FULL" : String.valueOf(remaining);

            model.addRow(new Object[]{
                v.getVesselName(), v.getRoute(), v.getVesselType(), v.getStatus(),
                v.getDepartureTime(), v.getCapacity(), remainingDisplay, v.getTripStatus()
            });
        }
    }
}