package com.mycompany.automatedferryticketingsystem.model;

public class Vessel {
    private int vesselId; 
    private String vesselName;
    private String route;
    private String vesselType;
    private String status;
    private String departureTime;
    private int capacity;
    private int remainingSeats;
    private String tripStatus; // This matches the 8th column in your UI

    public Vessel() {}

    // Getters and Setters
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

    // Crucial for the Controller to populate the 8th column
    public String getTripStatus() { return tripStatus; }
    public void setTripStatus(String tripStatus) { this.tripStatus = tripStatus; }
}