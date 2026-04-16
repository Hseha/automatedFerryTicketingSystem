package com.mycompany.automatedferryticketingsystem.view;

import com.mycompany.automatedferryticketingsystem.model.Ticket;
import javax.swing.*;
import java.awt.*;

public class PassengerInfoUI extends JFrame {
    // Ang "envelope" gikan sa IdentifyFerryUI. It carries the data from the previous screen.
    private Ticket ticket; 

    // UI Components for user input
    private JTextField txtPassengerName, txtIdNumber;
    private JSpinner spnAge;
    private JComboBox<String> cbCategory;
    private JButton btnValidate, btnBack;
    private JLabel lblVessel, lblRoute, lblFare;

    /**
     * Constructor: Kinahanglan og Ticket object para mahibal-an kung unsa nga 
     * barko ang gipili sa user sa unang screen.
     */
    public PassengerInfoUI(Ticket ticket) {
        this.ticket = ticket;
        setupWindow();
        initializeComponents();
        displayTripDetails();
    }

    // Basic window settings to match the dark theme
    private void setupWindow() {
        setTitle("PASSENGER INFORMATION");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(45, 45, 48)); 
        setLayout(new BorderLayout());
    }

    private void initializeComponents() {
        // FORM PANEL: Area diin i-input ang passenger data
        JPanel formPanel = new JPanel(new GridLayout(10, 1, 10, 10));
        formPanel.setBackground(new Color(45, 45, 48));
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE), "PASSENGER DETAILS", 0, 0, null, Color.WHITE));

        // Input fields: Name, Age, and Category
        txtPassengerName = new JTextField();
        spnAge = new JSpinner(new SpinnerNumberModel(18, 0, 120, 1)); // Default 18, Min 0, Max 120
        
        String[] categories = {"Regular", "Student", "Senior Citizen"};
        cbCategory = new JComboBox<>(categories);

        // ID Number Logic: Disabled by default, active lang kung Student/Senior
        txtIdNumber = new JTextField();
        txtIdNumber.setEnabled(false); 

        // SUMMARY PANEL: Ipakita ang details sa barko nga napili sa pikas screen
        JPanel summaryPanel = new JPanel(new GridLayout(5, 1));
        summaryPanel.setBackground(new Color(60, 60, 63));
        
        // Data Extraction: Gamit ang getters gikan sa Ticket model
        lblVessel = new JLabel("Vessel: " + ticket.getVesselName());
        lblRoute = new JLabel("Route: " + ticket.getRoute());
        lblFare = new JLabel("Base Fare: ₱" + ticket.getBaseFare());
        
        // Setting font colors for visibility against the dark background
        lblVessel.setForeground(Color.WHITE);
        lblRoute.setForeground(Color.WHITE);
        lblFare.setForeground(Color.WHITE);

        // (You can add the panels to the frame here)
    }

    /**
     * Logic to refresh the labels with data coming from the Ticket object.
     * Siguradohon nato nga ang UI nagpakita sa sakto nga info gikan sa Database.
     */
    private void displayTripDetails() {
        lblVessel.setText("Vessel: " + ticket.getVesselName());
        lblRoute.setText("Route: " + ticket.getRoute());
        lblFare.setText("Base Fare: ₱" + ticket.getBaseFare());
    }
}
