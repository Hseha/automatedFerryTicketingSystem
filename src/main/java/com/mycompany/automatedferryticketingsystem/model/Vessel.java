package com.mycompany.automatedferryticketingsystem.model;

/**
 * [ENCAPSULATION] - Kini nga class nagsilbing "Data Container" para sa Vessel 
 * information. Gigamit ang 'private' fields aron maprotektahan ang integridad 
 * sa data samtang gina-pasa kini sa lain-laing layers sa system (DAO, View, Controller).
 */
public class Vessel {
    
    // [RELATIONAL LOGIC] - Foreign key reference para sa scheduling.
    private int tripId; 
    
    // [STATE MANAGEMENT] - Mga attributes nga nagrepresentar sa physical ug 
    // operational status sa barko gikan sa database.
    private int vesselId; 
    private String vesselName;
    private String route;
    private String vesselType;
    private String status;         // Vessel condition (e.g., In Service, Maintenance)
    private String departureTime;
    private int capacity;
    private int remainingSeats;    // Dynamic count para sa booking validation
    private String tripStatus;     // Operational state (e.g., Available, Delayed)

    /**
     * [POJO DESIGN] - Default constructor para sa framework flexibility 
     * ug manual object instantiation.
     */
    public Vessel() {
    }

    // --- [ENCAPSULATION: ACCESSOR LOGIC] ---
    // Gigamit ni para sa controlled access (Read/Write) sa mga private variables.

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