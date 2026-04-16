package com.mycompany.automatedferryticketingsystem.model;

public class Ticket {
    // 1. Basic Trip Info: Data gikan sa unang screen (IdentifyFerryUI)
    private int tripId;
    private String route;
    private String vesselName;
    private double baseFare;
    
    // 2. Passenger Info: Data gikan sa ikaduhang screen (PassengerInfoUI)
    private String transactionId;
    private String passengerName;
    private int age;
    private String category; // Regular, Student, or Senior
    private String idNumber; 
    
    // 3. Payment & Logistics: Data nga i-calculate sa system
    private double finalFare;
    private String seatNumber;
    private String paymentStatus = "Pending"; // Default status sa database

    // Constructor: I-initialize ang basic info sa barko pag-create sa object
    public Ticket(int tripId, String route, String vesselName, double baseFare) {
        this.tripId = tripId;
        this.route = route;
        this.vesselName = vesselName;
        this.baseFare = baseFare;
    }

    // Business Logic: Mao ni ang tig-calculate sa 20% discount
    public void applyDiscount() {
        if (category != null && (category.equalsIgnoreCase("Student") || category.equalsIgnoreCase("Senior Citizen"))) {
            this.finalFare = this.baseFare * 0.80; // Minus 20% kung naay ID
        } else {
            this.finalFare = this.baseFare; // Fix price kung regular
        }
    }

    // --- GETTERS AND SETTERS ---
    // Gigamit ni para makakuha (get) o makabutang (set) og data ang Controller ug DAO

    public int getTripId() { return tripId; }
    public String getRoute() { return route; }
    public String getVesselName() { return vesselName; }
    public double getBaseFare() { return baseFare; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public double getFinalFare() { return finalFare; }
    public void setFinalFare(double finalFare) { this.finalFare = finalFare; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
