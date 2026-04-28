package com.mycompany.automatedferryticketingsystem.model;

/**
 * CORE LOGIC: Trip Entity Model.
 * Mao ni ang mapping sa gi-join nga 'trips' ug 'vessels' tables gikan sa MariaDB.
 * Kini ang nagsilbing "Data Carrier" sa tibuok system.
 */
public class Trip {
    // Database Keys: Kinahanglanon para sa relational queries.
    private int tripId;
    private int vesselId;
    
    // Info Fields: Unsa ang makit-an sa passenger sa kiosk.
    private String vesselName;
    private String vesselType;
    private String route;
    private double baseFare;
    private String etd; // Estimated Time of Departure
    private String eta; // Estimated Time of Arrival
    
    // Operational States: Logic para sa status display.
    private String operationalStatus; 
    private String displayStatus;      
    private String pierNo; 
    private String statusReason;
    
    // Counter Logic: Kinahanglan para sa booking validation.
    private int capacity;       
    private int seatsTaken;
    private int seatsAvailable; 

    // Constructor: Gigamit sa DAO para i-populate ang list of trips gikan sa ResultSet.
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

    // --- IDENTIFICATION LOGIC ---
    public int getTripId() { return tripId; }
    public int getVesselId() { return vesselId; } 
    
    // --- DISPLAY LOGIC ---
    public String getVesselName() { return vesselName; }
    public String getVesselType() { return vesselType; }
    public String getRoute() { return route; }
    public double getBaseFare() { return baseFare; }
    public String getEtd() { return etd; }
    public String getEta() { return eta; }
    public int getSeatsTaken() { return seatsTaken; }
    
    // --- FIXED: ADDED MISSING GETTERS PARA SA COMPILATION ---
    
    /**
     * LOGIC: Pagkuha sa maximum capacity sa barko.
     * Gikinahanglan ni sa VesselDAO ug AdminDashboard.
     */
    public int getCapacity() { 
        return capacity; 
    }

    /**
     * LOGIC: Pagkuha sa nahabilin nga slots.
     * Mao ni ang gamiton sa VesselController para i-block ang booking kon 0 na.
     */
    public int getSeatsAvailable() { 
        return seatsAvailable; 
    }

    /**
     * LOGIC: Pagkuha sa rason ngano na-delay o na-cancel ang trip.
     */
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

    // --- BUSINESS LOGIC (SAFETY CHECKS) ---
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

    // --- SETTERS ---
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