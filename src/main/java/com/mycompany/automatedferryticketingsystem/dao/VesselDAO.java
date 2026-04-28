package com.mycompany.automatedferryticketingsystem.dao;

import com.mycompany.automatedferryticketingsystem.model.Trip;
import com.mycompany.automatedferryticketingsystem.util.SQLConstants;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [LOGIC OVERVIEW]
 * 1. DATA ACCESS OBJECT (DAO): Kini ang bridge sa imong Java app ug sa MySQL database.
 * 2. TRANSACTION SAFETY: Gigamit ang 'rollback' logic para masiguro nga dili ma-corrupt 
 * ang data kung naay error sa tunga-tunga sa process.
 * 3. REAL-TIME CALCULATION: Ang seats availability gi-compute dynamically, 
 * dili lang basta-basta hardcoded value sa database.
 */
public class VesselDAO {

    private final HikariDataSource dataSource; // Managed connection pool for performance.

    public VesselDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HikariDataSource getDataSource() {
        return this.dataSource;
    }

    // --- 1. TRIP CRUD OPERATIONS (Create, Read, Update, Delete) ---

    /**
     * LOGIC: Inig add og trip, i-check sa system kung existing na ba ang Vessel name. 
     * If wala pa, i-create una ang Vessel record before i-insert ang Trip details.
     */
    public boolean addTrip(Trip t) {
        // Step 1: Clean the name para walay matching errors tungod sa extra spaces.
        String cleanName = t.getVesselName().trim();
        int vId = getOrCreateVessel(cleanName, t.getVesselType(), t.getCapacity());
        
        if (vId == -1) return false; // Fail safe kung naay error sa vessel creation.
        t.setVesselId(vId);

        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(SQLConstants.INSERT_TRIP)) {
            // Step 2: Mapping object data ngadto sa SQL parameters.
            ps.setInt(1, vId);
            ps.setString(2, t.getRoute());
            ps.setString(3, t.getEtd());
            ps.setString(4, t.getEta());
            ps.setDouble(5, t.getBaseFare());
            ps.setString(6, "Available"); // Default status inig create.
            ps.setString(7, t.getPierNo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace();
            return false; 
        }
    }

    /**
     * LOGIC: Dynamic Update.
     * I-update ang trip details samtang gi-ensure nga ang vessel capacity 
     * ug type naka-sync gihapon sa data.
     */
    public boolean updateTrip(Trip t) {
        int vId = getOrCreateVessel(t.getVesselName().trim(), t.getVesselType(), t.getCapacity());
        if (vId == -1) return false;
        
        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(SQLConstants.UPDATE_TRIP_DETAILS)) {
            ps.setInt(1, vId);
            ps.setString(2, t.getRoute());
            ps.setString(3, t.getEtd());
            ps.setString(4, t.getEta());
            ps.setDouble(5, t.getBaseFare());
            ps.setString(6, t.getPierNo());
            ps.setInt(7, t.getTripId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace();
            return false; 
        }
    }

    /**
     * LOGIC: Fetcher with Dynamic Join. 
     * Gi-calculate ang 'actual_remaining' slots pinaagi sa pag-minus sa 
     * total tickets sold gikan sa maximum vessel capacity sa SQL level pa lang.
     */
    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        // SQL Logic: Complex join sa 'trips' ug 'vessels' tables.
        String sql = "SELECT t.*, v.vessel_name, v.vessel_type, v.capacity, v.status AS vessel_status, " +
                     "(v.capacity - (SELECT COUNT(*) FROM tickets WHERE trip_id = t.trip_id)) AS actual_remaining " +
                     "FROM trips t JOIN vessels v ON t.vessel_id = v.vessel_id " +
                     "WHERE t.is_archived = ? ORDER BY t.etd ASC";

        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 0); // Logic: 0 means 'not archived' (active trips ra ang ipakita).
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mapping row data gikan sa database ngadto sa Trip model objects.
                    trips.add(new Trip(
                        rs.getInt("trip_id"), 
                        rs.getInt("vessel_id"), 
                        rs.getString("vessel_name"),
                        rs.getString("vessel_type"), 
                        rs.getString("route"), 
                        rs.getDouble("base_fare"),
                        rs.getString("etd"), 
                        rs.getString("eta"), 
                        rs.getString("vessel_status"), 
                        rs.getString("trip_status"), 
                        rs.getString("pier_no"), 
                        rs.getString("status_reason"),
                        rs.getInt("capacity"), 
                        rs.getInt("actual_remaining")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return trips;
    }

    /**
     * LOGIC: Soft Delete (Archive).
     * Dili i-delete ang data, kundi butangan ra og 'archived' flag para naay record sa history.
     */
    public boolean archiveTrip(int tripId) {
        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(SQLConstants.ARCHIVE_TRIP)) {
            ps.setInt(1, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /**
     * LOGIC: Atomic Transaction (Hard Delete). 
     * Dili pwede ma-delete ang Trip kung naay existing Tickets tungod sa Foreign Key constraints. 
     * Ang logic kay 'Delete Tickets first, then Trip' within a single database commit.
     */
    public boolean deleteTripPermanently(int tripId) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); // Start manually controlled transaction.
            try (PreparedStatement ps1 = conn.prepareStatement(SQLConstants.DELETE_TICKETS); 
                 PreparedStatement ps2 = conn.prepareStatement(SQLConstants.DELETE_TRIP)) {
                
                ps1.setInt(1, tripId); 
                ps1.executeUpdate(); // Limpyohan una ang dependent tickets.

                ps2.setInt(1, tripId);
                int affected = ps2.executeUpdate();
                
                conn.commit(); // I-finalize ang changes kung successful tanan steps.
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback(); // I-undo ang tanan kung naay error (Data Integrity).
                return false;
            }
        } catch (SQLException e) { return false; }
    }

    // --- 2. STATUS & SAFETY MANAGEMENT ---

    /**
     * LOGIC: Automated Classification base sa context. 
     * Ang system mo-detect sa keywords (e.g., 'maintenance') para i-ground 
     * ang vessel status automatically.
     */
    public boolean executeAdminAction(int vesselId, int tripId, String newStatus, String threat, String reason, boolean isGlobal) {
        String vesselStatus = "In Service";
        // Business Rule Logic:
        if (reason.toLowerCase().contains("maintenance") || reason.toLowerCase().contains("repair")) {
            vesselStatus = "Maintenance";
        } else if (threat.equalsIgnoreCase("High") || threat.equalsIgnoreCase("Critical")) {
            vesselStatus = "Grounded";
        }
        return applySafetyProtocol(vesselId, vesselStatus, newStatus, reason);
    }

    /**
     * LOGIC: Upsert Log and Status Sync.
     * I-update ang condition sa vessel ug i-log ang reason sa 'status_reasons' 
     * table para sa analytics and history tracking.
     */
    public boolean applySafetyProtocol(int vesselId, String vesselStatus, String tripStatus, String reason) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); 
            try (PreparedStatement psV = conn.prepareStatement(SQLConstants.UPDATE_VESSEL_CONDITION);
                 PreparedStatement psT = conn.prepareStatement(SQLConstants.UPDATE_TRIP_STATUS_WITH_REASON);
                 PreparedStatement psLog = conn.prepareStatement(SQLConstants.LOG_REASON_UPSERT)) {
                
                psV.setString(1, vesselStatus);
                psV.setInt(2, vesselId);
                psV.executeUpdate();

                psT.setString(1, tripStatus);
                psT.setString(2, reason);
                psT.setInt(3, vesselId);
                psT.executeUpdate();

                // Logic: I-upsert ang frequency sa status reason para dali ra ma-suggest sunod.
                psLog.setString(1, vesselStatus); 
                psLog.setString(2, reason);      
                psLog.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) { return false; }
    }

    // Logic: Simple batch update para ibalik sa normal status ang vessel ug trip.
    public boolean restoreToOperation(int vesselId, int tripId) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psV = conn.prepareStatement(SQLConstants.RESTORE_VESSEL_STATUS);
                 PreparedStatement psT = conn.prepareStatement(SQLConstants.RESTORE_TRIP_STATUS)) {
                psV.setInt(1, vesselId);
                psV.executeUpdate();
                psT.setInt(1, tripId);
                psT.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) { return false; }
    }

    // Logic: Fetcher para sa "Autocomplete" feature sa admin reasons field.
    public List<String> getSuggestedReasons() {
        List<String> reasons = new ArrayList<>();
        String sql = "SELECT DISTINCT reason_text FROM status_reasons ORDER BY frequency DESC LIMIT 10";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reasons.add(rs.getString(1));
            }
        } catch (SQLException e) {
            reasons.add("Scheduled Maintenance");
            reasons.add("Adverse Weather Conditions");
        }
        return reasons;
    }

    // --- 3. SALES & ANALYTICS ---

    // Logic: Summation query para sa financial charts.
    public Map<String, Double> getRevenuePerTrip() {
        Map<String, Double> revenueMap = new HashMap<>();
        String sql = "SELECT v.vessel_name, SUM(t.final_fare) FROM tickets t " +
                     "JOIN trips tr ON t.trip_id = tr.trip_id " +
                     "JOIN vessels v ON tr.vessel_id = v.vessel_id GROUP BY v.vessel_name";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                revenueMap.put(rs.getString(1), rs.getDouble(2));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return revenueMap;
    }

    // Logic: Detailed manifest list filtered by tripId.
    public List<Object[]> getPassengersByTrip(int tripId) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT passenger_name, category, seat_number, transaction_id " +
                     "FROM tickets WHERE trip_id = ? ORDER BY passenger_name ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(new Object[]{ rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4) });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    // Logic: Master manifest fetcher nga nag-join og tulo ka tables.
    public List<Object[]> getPassengerManifesto() {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT t.transaction_id, t.passenger_name, tr.route, v.vessel_name, t.seat_number, t.final_fare " +
                     "FROM tickets t JOIN trips tr ON t.trip_id = tr.trip_id " + 
                     "JOIN vessels v ON tr.vessel_id = v.vessel_id ORDER BY t.transaction_id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getDouble(6)
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    // Logic: Simple scalar aggregate function para makuha ang total gross sales.
    public double getTotalSales() {
        String sql = "SELECT SUM(final_fare) FROM tickets";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    // --- 4. CORE UTILITIES ---

    /**
     * LOGIC: Smart Vessel Management.
     * 1. Check if vessel exists by name.
     * 2. If exists, i-update ang specs (capacity/type) para synchronize sa admin edits.
     * 3. If new, i-insert sa database and return the unique generated ID (PK).
     */
    private int getOrCreateVessel(String name, String type, int cap) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(SQLConstants.FIND_VESSEL)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) { 
                    if (rs.next()) {
                        int vId = rs.getInt(1);
                        // Logic: Sync data in case nausab ang specs sa admin form.
                        String updateSql = "UPDATE vessels SET capacity = ?, vessel_type = ? WHERE vessel_id = ?";
                        try (PreparedStatement psU = conn.prepareStatement(updateSql)) {
                            psU.setInt(1, cap);
                            psU.setString(2, type);
                            psU.setInt(3, vId);
                            psU.executeUpdate();
                        }
                        return vId; 
                    } 
                }
            }
            // Creation logic for new vessels.
            try (PreparedStatement psI = conn.prepareStatement(SQLConstants.INSERT_VESSEL, Statement.RETURN_GENERATED_KEYS)) {
                psI.setString(1, name); 
                psI.setString(2, type); 
                psI.setInt(3, cap);
                psI.executeUpdate();
                try (ResultSet rsI = psI.getGeneratedKeys()) { 
                    if (rsI.next()) return rsI.getInt(1); 
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    // Logic: Testing utility para i-wipe ang tickets table during system resets/demos.
    public boolean resetSystemData() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM tickets");
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) { return false; }
    }
}