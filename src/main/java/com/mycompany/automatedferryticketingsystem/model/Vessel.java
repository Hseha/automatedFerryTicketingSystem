package com.mycompany.automatedferryticketingsystem.model;

/**
 * MODEL LAYER:
 * Kini ang "Blueprint" o sudlanan sa data sa barko gikan sa database.
 * This class holds all the information for a single vessel row.
 */
public class Vessel {
    // --- CORE FIELDS: Basic Database Data ---
    private int vesselId;
    private String vesselName;
    private int capacity;
    private String status; // e.g., 'In Service' or 'Maintenance'
    
    // --- DYNAMIC FIELDS: Needed for the Table UI ---
    // Gidugang kini para dili na hardcoded ang route ug time sa screen.
    private String route;
    private String vesselType; // e.g., 'Fastcraft' or 'Roro'
    private String departureTime;
    private int remainingSeats; // Resulta sa (Capacity - Tickets Sold)

    public Vessel() {} // Kinahanglanon para makahimo og empty object ang DAO

    // --- CORE GETTERS AND SETTERS ---
    // Methods para makuha (get) o ma-set (set) ang basic data.

    public int getVesselId() { return vesselId; }
    public void setVesselId(int vesselId) { this.vesselId = vesselId; }

    public String getVesselName() { return vesselName; }
    public void setVesselName(String vesselName) { this.vesselName = vesselName; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // --- DYNAMIC UI GETTERS AND SETTERS ---
    // Mao kini ang gamiton sa VesselController para i-populate ang JTable.

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getVesselType() { return vesselType; }
    public void setVesselType(String vesselType) { this.vesselType = vesselType; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public int getRemainingSeats() { return remainingSeats; }
    public void setRemainingSeats(int remainingSeats) { this.remainingSeats = remainingSeats; }
}
