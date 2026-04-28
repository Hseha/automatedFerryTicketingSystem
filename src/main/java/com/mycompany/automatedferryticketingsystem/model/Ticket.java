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
    private String pierNo; 
    private int capacity; // --- ADDED CAPACITY ---
    
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
    private String paymentMethod; 

    // Updated Constructor to include pierNo and capacity
    public Ticket(int tripId, String vesselName, String route, String vesselType, String departureTime, String eta, double baseFare, String pierNo, int capacity) {
        this.tripId = tripId;
        this.vesselName = vesselName;
        this.route = route;
        this.vesselType = vesselType;
        this.departureTime = departureTime;
        this.eta = eta;
        this.baseFare = baseFare;
        this.pierNo = pierNo;
        this.capacity = capacity; // Initialize capacity
    }

    // Default constructor for flexibility
    public Ticket() {}

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

    // --- CAPACITY GETTER & SETTER (Fixes Compilation Error) ---
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    // --- PIER NO GETTER & SETTER ---
    public String getPierNo() { 
        return (pierNo == null || pierNo.trim().isEmpty()) ? "TBD" : pierNo; 
    }
    
    public void setPierNo(String pierNo) { this.pierNo = pierNo; }

    // --- OTHER GETTERS & SETTERS ---
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getTripId() { return tripId; }
    public String getRoute() { return route; }
    
    public void setRoute(String route) { this.route = route; }
    public void setVesselName(String name) { this.vesselName = name; }
    public void setDepartureTime(String time) { this.departureTime = time; }
    public void setBaseFare(double fare) { this.baseFare = fare; }

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