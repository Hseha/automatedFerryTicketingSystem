package com.mycompany.automatedferryticketingsystem.model;

/**
 * [ENCAPSULATION] - Kini nga class nag-bundle sa tanang attributes sa usa ka Ticket 
 * ngadto sa usa ka unit. Gigamit ang 'private' fields aron maprotektahan ang data 
 * gikan sa direktang external modification.
 */
public class Ticket {
    
    // [STATE MANAGEMENT] - Mga data fields nga nagrepresentar sa ticket status.
    private int tripId;
    private String route;
    private String vesselName;
    private String vesselType;    
    private String departureTime; 
    private String eta;            
    private double baseFare;
    private String pierNo; 
    private int capacity; 
    
    private String transactionId;
    private String passengerName;
    private int age;
    private String contactNumber; 
    private String address;        
    private String category; 
    private String idNumber; 
    
    private double finalFare;
    private String seatNumber;
    private String paymentStatus = "Pending";
    private String paymentMethod; 

    // Constructor: Gigamit para sa pag-initialize sa mga importanteng barko ug trip details.
    public Ticket(int tripId, String vesselName, String route, String vesselType, String departureTime, String eta, double baseFare, String pierNo, int capacity) {
        this.tripId = tripId;
        this.vesselName = vesselName;
        this.route = route;
        this.vesselType = vesselType;
        this.departureTime = departureTime;
        this.eta = eta;
        this.baseFare = baseFare;
        this.pierNo = pierNo;
        this.capacity = capacity; 
    }

    public Ticket() {}

    /**
     * [ABSTRACTION] - Ang user (o ubang classes) dili na kinahanglan mahibalo sa complex 
     * math sa diskwento. I-call lang ang 'applyDiscount()' ug ang system na ang bahala.
     */
    public void applyDiscount() {
        if (category != null && !category.equalsIgnoreCase("Regular")) {
            this.finalFare = this.baseFare * 0.80; // Default 20% discount (PWD/Senior/Student)
        } else {
            this.finalFare = this.baseFare;
        }
    }

    public String getTravelTime() { 
        String type = (vesselType != null) ? vesselType : "Standard";
        String arrival = (eta != null) ? eta : "TBA";
        return type + " (ETA: " + arrival + ")"; 
    }

    // --- [ENCAPSULATION: GETTERS & SETTERS] ---
    // Kini ang controlled access points para sa atong private fields.

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getPierNo() { 
        return (pierNo == null || pierNo.trim().isEmpty()) ? "TBD" : pierNo; 
    }
    public void setPierNo(String pierNo) { this.pierNo = pierNo; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getTripId() { return tripId; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public void setVesselName(String name) { this.vesselName = name; }
    public String getVesselName() { return vesselName; }

    public void setDepartureTime(String time) { this.departureTime = time; }
    public String getDepartureTime() { return departureTime; }

    public void setBaseFare(double fare) { this.baseFare = fare; }
    public double getBaseFare() { return baseFare; }

    public String getVesselType() { return vesselType; }
    public String getEta() { return eta; }

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
        applyDiscount(); // Automatic computation inig set sa category.
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