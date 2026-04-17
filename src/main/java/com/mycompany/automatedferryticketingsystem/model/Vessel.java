package com.mycompany.automatedferryticketingsystem.model;

public class Vessel {
    private int vesselId; // Ensure this is int
    private String vesselName;
    private String route;
    private String vesselType;
    private String status;
    private String departureTime;
    private int capacity;
    private int remainingSeats;

    public Vessel() {}

    // THE MISSING METHOD:
    public void setVesselId(int vesselId) {
        this.vesselId = vesselId;
    }

    public int getVesselId() {
        return vesselId;
    }

    // Ensure you also have these for the rest of the DAO to work:
    public void setVesselName(String vesselName) { this.vesselName = vesselName; }
    public void setRoute(String route) { this.route = route; }
    public void setVesselType(String vesselType) { this.vesselType = vesselType; }
    public void setStatus(String status) { this.status = status; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setRemainingSeats(int remainingSeats) { this.remainingSeats = remainingSeats; }
    
    // Getters for the Controller to use:
    public String getVesselName() { return vesselName; }
    public String getRoute() { return route; }
    public String getVesselType() { return vesselType; }
    public String getStatus() { return status; }
    public String getDepartureTime() { return departureTime; }
    public int getCapacity() { return capacity; }
    public int getRemainingSeats() { return remainingSeats; }
}