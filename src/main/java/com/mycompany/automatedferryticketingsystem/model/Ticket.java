package com.mycompany.automatedferryticketingsystem.model;

public class Ticket {
    // 1. Trip Info
    private int tripId;
    private String route;
    private String vesselName;
    private String vesselType;    
    private String departureTime; 
    private String eta;           
    private double baseFare;
    
    // 2. Passenger Info
    private String transactionId;
    private String passengerName;
    private int age;
    private String contactNumber; 
    private String address;       
    private String category; 
    private String idNumber; 
    
    // 3. System Calculations & Payment
    private double finalFare;
    private String seatNumber;
    private String paymentStatus = "Pending";
    private String paymentMethod; // --- ADDED THIS ---

    // Constructor
    public Ticket(int tripId, String vesselName, String route, String vesselType, String departureTime, String eta, double baseFare) {
        this.tripId = tripId;
        this.vesselName = vesselName;
        this.route = route;
        this.vesselType = vesselType;
        this.departureTime = departureTime;
        this.eta = eta;
        this.baseFare = baseFare;
    }

    // Business Logic
    public void applyDiscount() {
        if (category != null && !category.equalsIgnoreCase("Regular")) {
            this.finalFare = this.baseFare * 0.80; 
        } else {
            this.finalFare = this.baseFare;
        }
    }

    public String getTravelTime() { 
        String type = (vesselType != null) ? vesselType : "Standard";
        String arrival = (eta != null) ? eta : "TBA";
        return type + " (ETA: " + arrival + ")"; 
    }

    // --- GETTERS & SETTERS ---
    
    // Payment Method Getter/Setter (Fixes Maven Error)
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getTripId() { return tripId; }
    public String getRoute() { return route; }
    public String getVesselName() { return vesselName; }
    public String getVesselType() { return vesselType; }
    public String getDepartureTime() { return departureTime; }
    public String getEta() { return eta; }
    public double getBaseFare() { return baseFare; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String name) { this.passengerName = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contact) { this.contactNumber = contact; }
    
    public String getAddress() { return address; }
    public void setAddress(String addr) { this.address = addr; }

    public void setCategory(String category) { 
        this.category = category; 
        applyDiscount(); 
    }
    public String getCategory() { return category; }

    public void setFinalFare(double fare) { this.finalFare = fare; }
    public double getFinalFare() { return finalFare; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String id) { this.transactionId = id; }
    
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seat) { this.seatNumber = seat; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
}