package com.mycompany.automatedferryticketingsystem.model;

/**
 * [ENCAPSULATION] - Kini nga class nag-bundle sa data gikan sa 'trips' ug 'vessels' tables.
 * Pinaagi sa paggamit og 'private' fields, masiguro nato nga ang internal state 
 * sa matag trip dili basta-basta ma-usab sa gawas nga classes.
 */
public class Trip {
    
    // Database Keys: Relational identifiers para sa MariaDB logic.
    private int tripId;
    private int vesselId;
    
    // Info Fields: Display attributes para sa kiosk UI.
    private String vesselName;
    private String vesselType;
    private String route;
    private double baseFare;
    private String etd; 
    private String eta; 
    
    // Operational States: Logic para sa status monitoring.
    private String operationalStatus; 
    private String displayStatus;      
    private String pierNo; 
    private String statusReason;
    
    // [STATE MANAGEMENT] - Mga counters para sa booking validation.
    private int capacity;       
    private int seatsTaken;
    private int seatsAvailable; 

    // Constructor: Gigamit sa DAO para i-map ang ResultSet ngadto sa Java Object.
    public Trip(int tripId, int vesselId, String vesselName, String vesselType, String route, 
                double baseFare, String etd, String eta, String operationalStatus, 
                String displayStatus, String pierNo, String statusReason, int capacity, int seatsAvailable) {
        this.tripId = tripId;
        this.vesselId = vesselId;
        this.vesselName = vesselName;
        this.vesselType = vesselType;
        this.route = route;
        this.baseFare = baseFare;
        this.etd = etd;
        this.eta = eta;
        this.operationalStatus = operationalStatus;
        this.displayStatus = displayStatus;
        this.pierNo = pierNo; 
        this.statusReason = statusReason;
        this.capacity = capacity;
        this.seatsAvailable = seatsAvailable; 
    }

    public Trip() {}

    /**
     * [ABSTRACTION] - Gi-hide niini ang logic sa pag-decide kung pwede ba maka-book.
     * Ang UI mo-call lang og 'canBook()' ug dili na kinahanglan mahibalo sa 
     * status conditions (Cancelled, Grounded, etc.).
     */
    public boolean canBook() {
        String trip = getTripStatus();
        String vessel = getVesselStatus();
        if (trip.equalsIgnoreCase("Cancelled")) return false;
        if (vessel.equalsIgnoreCase("Grounded") || vessel.equalsIgnoreCase("Maintenance")) return false;
        return seatsAvailable > 0; 
    }

    public boolean isDelayed() {
        return getTripStatus().equalsIgnoreCase("Delayed");
    }

    // --- [ENCAPSULATION: GETTERS & SETTERS] ---

    public int getTripId() { return tripId; }
    public int getVesselId() { return vesselId; } 
    
    public String getVesselName() { return vesselName; }
    public String getVesselType() { return vesselType; }
    public String getRoute() { return route; }
    public double getBaseFare() { return baseFare; }
    public String getEtd() { return etd; }
    public String getEta() { return eta; }
    public int getSeatsTaken() { return seatsTaken; }
    
    public int getCapacity() { return capacity; }

    public int getSeatsAvailable() { return seatsAvailable; }

    public String getStatusReason() { 
        return (statusReason == null || statusReason.isEmpty()) ? "N/A" : statusReason; 
    }

    public String getPierNo() { 
        return (pierNo == null || pierNo.trim().isEmpty()) ? "TBD" : pierNo; 
    }

    public String getVesselStatus() { 
        return (operationalStatus == null || operationalStatus.isEmpty()) ? "In Service" : operationalStatus; 
    }

    public String getTripStatus() { 
        return (displayStatus == null || displayStatus.isEmpty()) ? "Available" : displayStatus; 
    }

    // SETTERS
    public void setTripId(int id) { this.tripId = id; }
    public void setVesselId(int vesselId) { this.vesselId = vesselId; }
    public void setVesselName(String name) { this.vesselName = name; }
    public void setVesselType(String type) { this.vesselType = type; }
    public void setRoute(String route) { this.route = route; }
    public void setEtd(String etd) { this.etd = etd; }
    public void setEta(String eta) { this.eta = eta; }
    public void setBaseFare(double fare) { this.baseFare = fare; }
    public void setPierNo(String pierNo) { this.pierNo = pierNo; } 
    public void setStatusReason(String reason) { this.statusReason = reason; }
    public void setSeatsTaken(int taken) { this.seatsTaken = taken; }
    public void setSeatsAvailable(int available) { this.seatsAvailable = available; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setVesselStatus(String status) { this.operationalStatus = status; }
    public void setOperationalStatus(String status) { this.operationalStatus = status; }
    public void setTripStatus(String status) { this.displayStatus = status; }
}