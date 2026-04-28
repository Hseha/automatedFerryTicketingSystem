package com.mycompany.automatedferryticketingsystem.controller;

import com.mycompany.automatedferryticketingsystem.dao.VesselDAO;
import com.mycompany.automatedferryticketingsystem.model.Trip;
import com.mycompany.automatedferryticketingsystem.model.Ticket;
import com.mycompany.automatedferryticketingsystem.view.IdentifyFerryUI;
import com.mycompany.automatedferryticketingsystem.view.PassengerInfoUI;
import com.zaxxer.hikari.HikariDataSource;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.awt.Color;
import java.awt.event.ActionEvent;

/**
 * [LOGIC OVERVIEW]
 * Kini ang 'Brain' sa imong system. Ang Controller ang nag-handle sa button clicks, 
 * pag-load sa data gikan sa DAO, ug pag-update sa hitsura sa imong UI.
 * [OOP CONCEPT: ABSTRACTION] - Ang Controller nag-tago sa komplikadong database 
 * operations ug nag-provide lang og simple nga interface para sa UI communication.
 */
public class VesselController {
    // [OOP CONCEPT: ENCAPSULATION] - Ang paggamit sa 'private' modifiers aron 
    // ma-bundle ang data ug methods sa usa ka unit ug protektahan kini gikan sa external interference.
    private final VesselDAO dao;
    private final IdentifyFerryUI view;
    private final HikariDataSource dataSource;
    private List<Trip> currentTrips; 

    // [OOP CONCEPT: COMPOSITION] - Ang Controller "naggamit" og uban nga objects (DAO, View).
    // Kini usa ka porma sa "Has-A" relationship imbes nga Inheritance.
    public VesselController(IdentifyFerryUI view, VesselDAO dao, HikariDataSource dataSource) {
        this.view = view;
        this.dao = dao;
        this.dataSource = dataSource; 
        initController(); 
    }

    // Logic: I-bind ang listeners sa UI components inig start sa app.
    private void initController() {
        if (view != null && view.getBtnProceed() != null) {
            // [OOP CONCEPT: INTERFACE & POLYMORPHISM] - Ang 'ActionListener' kay usa ka interface.
            // Ang paggamit sa Lambda (ActionEvent e) nagpakita og polymorphism diin ang 
            // 'actionPerformed' method gi-implementar base sa kinahanglanon sa button.
            view.getBtnProceed().addActionListener((ActionEvent e) -> {
                handleProceedAction(); // Triggered kung i-click ang 'Proceed' button.
            });
        }
        loadVesselData(""); // Automatic load sa data inig start.
    }

    // --- 1. SYSTEM SYNCHRONIZATION (Admin Actions) ---

    /**
     * LOGIC: Pag-handle sa status change (e.g., Grounded, Available).
     * Nag-check ni kung naay 'Reason' kay requirement na sa system before maka-update.
     */
    public void handleAdminStatusUpdate(int vesselId, int tripId, String status, String threat, String reason, boolean isGlobal) {
        if (reason == null || reason.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Reason is required to update status.", "Input Required", JOptionPane.ERROR_MESSAGE);
            return; 
        }

        boolean success;
        // Logic: Decide whether to restore (Available/Boarding) or apply safety protocols.
        boolean isOperational = status.equalsIgnoreCase("Available") || status.equalsIgnoreCase("Boarding");

        if (isOperational) {
            success = dao.restoreToOperation(vesselId, tripId);
        } else {
            success = dao.executeAdminAction(vesselId, tripId, status, threat, reason.trim(), isGlobal);
        }
        
        if (success) {
            loadVesselData(""); // Sync the table para updated ang makita sa user.
            JOptionPane.showMessageDialog(view, "System status synchronized successfully.");
        } else {
            JOptionPane.showMessageDialog(view, "Database error updating status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * LOGIC: Critical System Wipe.
     * Naay 'Confirm Dialog' para dili aksidente nga ma-delete ang tanang records.
     */
    public boolean performSystemReset() {
        int confirm = JOptionPane.showConfirmDialog(null, 
            "Are you sure? This deletes ALL passenger records and tickets.", 
            "CRITICAL RESET", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION && dao.resetSystemData()) {
            loadVesselData(""); // Refresh table after reset.
            return true;
        }
        return false;
    }

    // --- 2. TRIP DATA LOADING (Background Processing) ---

    /**
     * LOGIC: Multi-threading with SwingWorker.
     * Importante ni para dili mo-'Freeze' ang imong UI samtang nag-fetch og data gikan sa database.
     * [OOP CONCEPT: INHERITANCE] - Ang 'SwingWorker' gi-extend (subclassed) isip 
     * anonymous class aron ma-inherit ang iyang background threading capabilities.
     */
    public void loadVesselData(String searchTerm) {
        if (view != null && view.getBtnProceed() != null) {
            view.getBtnProceed().setEnabled(false); // Disable button samtang nag-load.
        }
        
        SwingWorker<List<Trip>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Trip> doInBackground() {
                return dao.getAllTrips(); // I-call ang DAO sa background thread.
            }
            @Override
            protected void done() {
                try {
                    currentTrips = get(); 
                    updateTable(currentTrips); // Refresh table content.
                    if (view != null) {
                        // Logic: Visual feedback kung online/offline ang data.
                        view.getLblStatus().setText(currentTrips.isEmpty() ? "● Offline" : "● System Online");
                        view.getLblStatus().setForeground(currentTrips.isEmpty() ? Color.GRAY : new Color(0, 204, 102));
                    }
                } catch (Exception e) { 
                    e.printStackTrace(); 
                } finally {
                    if (view != null && view.getBtnProceed() != null) view.getBtnProceed().setEnabled(true);
                }
            }
        };
        worker.execute(); 
    }

    // Logic: Pag-populate sa JTable gamit ang data gikan sa database.
    private void updateTable(List<Trip> trips) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0); // Limpyohan una ang karaan nga rows.

        for (Trip t : trips) {
            // Debug check para makita sa terminal kung sakto ba ang calculations.
            System.out.println("[SYSTEM CHECK] Vessel: " + t.getVesselName() + 
                               " | Seats Remaining: " + t.getSeatsAvailable());

            model.addRow(new Object[]{
                t.getTripId(),
                t.getVesselName(),
                t.getRoute(),
                t.getVesselType(),
                t.getVesselStatus(),
                formatToTimeOnly(t.getEtd()), // Format: HH:mm ra ang ipakita.
                formatToTimeOnly(t.getEta()),
                t.getPierNo(),
                t.getBaseFare(),
                t.getCapacity(),
                t.getSeatsAvailable(), // Importante: Monitoring sa slots.
                t.getTripStatus() 
            });
        }
    }

    // --- 3. BOOKING LOGIC (The Transition) ---

    /**
     * LOGIC: Validation before Proceeding.
     * 1. Check if naay napili nga row.
     * 2. Check if Full na ba ang barko (Seats <= 0).
     * 3. Check if Available/Boarding ba ang status (Dili pwede mo-book kung 'Cancelled').
     */
    private void handleProceedAction() {
        int selectedRow = view.getFerryTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a voyage to proceed.");
            return;
        }

        Trip selectedTrip = currentTrips.get(selectedRow);
        
        // Final seat check: Mo-block sa booking kung puno na ang barko.
        if (selectedTrip.getSeatsAvailable() <= 0) { 
            JOptionPane.showMessageDialog(view, "VOYAGE FULL: No seats remaining for this trip.", "Booking Limit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Logic: Kung operational, i-pass ang data sa sunod nga window (PassengerInfoUI).
        if (selectedTrip.getTripStatus().equalsIgnoreCase("Available") || selectedTrip.getTripStatus().equalsIgnoreCase("Boarding")) {
            // [OOP CONCEPT: OBJECT INSTANTIATION] - Pag-create og bag-ong 'Ticket' object 
            // aron ma-transfer ang data gikan sa Model (Trip) ngadto sa sunod nga View.
            Ticket ticket = new Ticket(
                selectedTrip.getTripId(), selectedTrip.getVesselName(), selectedTrip.getRoute(),
                selectedTrip.getVesselType(), selectedTrip.getEtd(), selectedTrip.getEta(),
                selectedTrip.getBaseFare(), selectedTrip.getPierNo(), selectedTrip.getCapacity() 
            );

            new PassengerInfoUI(this.dao, ticket).setVisible(true);
            view.dispose(); // Close current window para clean ang transition.
        } else {
            // Feedback kung ngano dili maka-book (e.g. Cancelled because of weather).
            String reason = (selectedTrip.getStatusReason() == null || selectedTrip.getStatusReason().isEmpty()) 
                            ? "Operational adjustment." : selectedTrip.getStatusReason();
            JOptionPane.showMessageDialog(view, "VOYAGE INACCESSIBLE\nStatus: " + selectedTrip.getTripStatus() + "\nReason: " + reason);
        }
    }

    // Logic: Utility function para limpyo tan-awon ang time (HH:mm) sa table.
    private String formatToTimeOnly(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) return "00:00";
        try {
            if (dateTime.contains(" ")) {
                String[] parts = dateTime.split("\\s+"); // I-split ang Date ug Time.
                return (parts.length >= 2) ? parts[1] : parts[0];
            }
            return dateTime;
        } catch (Exception e) { return dateTime; }
    }
}