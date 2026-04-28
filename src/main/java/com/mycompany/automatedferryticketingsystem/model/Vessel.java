package com.mycompany.automatedferryticketingsystem.model;

/**
 * CORE LOGIC: Data Modeling (POJO).
 * Kini nagsilbing "container" sa vessel information para dali i-pasa-pasa sa code.
 */
public class Vessel {
    
    // 1. Relational Logic:
    // tripId is used as a foreign key reference para mahibal-an kung unsa nga schedule kini nga barko.
    private int tripId; 
    
    // 2. Physical & Operational Attributes:
    // Mao ni ang mga columns sa imong database table.
    private int vesselId; 
    private String vesselName;
    private String route;
    private String vesselType;
    private String status;         // Vessel condition (e.g., In Service, Maintenance)
    private String departureTime;
    private int capacity;
    private int remainingSeats;    // Dynamic count para sa booking logic
    private String tripStatus;     // Operational state (e.g., Available, Delayed, Cancelled)

    public Vessel() {
        // Default constructor para sa flexibility sa object creation.
    }

    // --- ACCESSOR LOGIC (Getters & Setters) ---
    // Gigamit ni para sa Encapsulation: controlled access sa mga private variables.

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getVesselId() { return vesselId; }
    public void setVesselId(int vesselId) { this.vesselId = vesselId; }

    public String getVesselName() { return vesselName; }
    public void setVesselName(String vesselName) { this.vesselName = vesselName; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getVesselType() { return vesselType; }
    public void setVesselType(String vesselType) { this.vesselType = vesselType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getRemainingSeats() { return remainingSeats; }
    public void setRemainingSeats(int remainingSeats) { this.remainingSeats = remainingSeats; }

    public String getTripStatus() { return tripStatus; }
    public void setTripStatus(String tripStatus) { this.tripStatus = tripStatus; }
}