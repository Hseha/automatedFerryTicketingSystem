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
 * * [ABSTRACTION] - Kini nga class nag-tago sa tanang SQL queries aron ang UI motawag 
 * na lang og simple nga methods sama sa addTrip() o getAllTrips().
 */
public class VesselDAO {

    // [ENCAPSULATION] - Gi-private ang dataSource aron dili ma-usab ang connection pool sa gawas.
    private final HikariDataSource dataSource; 

    public VesselDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HikariDataSource getDataSource() {
        return this.dataSource;
    }

    // --- 1. TRIP CRUD OPERATIONS (Create, Read, Update, Delete) ---

    public boolean addTrip(Trip t) {
        String cleanName = t.getVesselName().trim();
        int vId = getOrCreateVessel(cleanName, t.getVesselType(), t.getCapacity());
        
        if (vId == -1) return false; 
        t.setVesselId(vId);

        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(SQLConstants.INSERT_TRIP)) {
            ps.setInt(1, vId);
            ps.setString(2, t.getRoute());
            ps.setString(3, t.getEtd());
            ps.setString(4, t.getEta());
            ps.setDouble(5, t.getBaseFare());
            ps.setString(6, "Available"); 
            ps.setString(7, t.getPierNo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace();
            return false; 
        }
    }

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
     * [ABSTRACTION] - Gi-hide niini ang komplikadong SQL JOIN logic. 
     * Ang UI mo-request lang og List<Trip>, wala na sila'y labot sa math sa 'actual_remaining'.
     */
    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        String sql = "SELECT t.*, v.vessel_name, v.vessel_type, v.capacity, v.status AS vessel_status, " +
                     "(v.capacity - (SELECT COUNT(*) FROM tickets WHERE trip_id = t.trip_id)) AS actual_remaining " +
                     "FROM trips t JOIN vessels v ON t.vessel_id = v.vessel_id " +
                     "WHERE t.is_archived = ? ORDER BY t.etd ASC";

        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 0); 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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

    public boolean archiveTrip(int tripId) {
        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(SQLConstants.ARCHIVE_TRIP)) {
            ps.setInt(1, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /**
     * [ATOMIC TRANSACTION] - Logic diin i-delete una ang tickets una ang trip. 
     * [ABSTRACTION] - Gi-hide ang complexity sa database referential integrity.
     */
    public boolean deleteTripPermanently(int tripId) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); 
            try (PreparedStatement ps1 = conn.prepareStatement(SQLConstants.DELETE_TICKETS); 
                 PreparedStatement ps2 = conn.prepareStatement(SQLConstants.DELETE_TRIP)) {
                
                ps1.setInt(1, tripId); 
                ps1.executeUpdate(); 

                ps2.setInt(1, tripId);
                int affected = ps2.executeUpdate();
                
                conn.commit(); 
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback(); 
                return false;
            }
        } catch (SQLException e) { return false; }
    }

    // --- 2. STATUS & SAFETY MANAGEMENT ---

    public boolean executeAdminAction(int vesselId, int tripId, String newStatus, String threat, String reason, boolean isGlobal) {
        String vesselStatus = "In Service";
        if (reason.toLowerCase().contains("maintenance") || reason.toLowerCase().contains("repair")) {
            vesselStatus = "Maintenance";
        } else if (threat.equalsIgnoreCase("High") || threat.equalsIgnoreCase("Critical")) {
            vesselStatus = "Grounded";
        }
        return applySafetyProtocol(vesselId, vesselStatus, newStatus, reason);
    }

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
     * [ABSTRACTION] - Kini nga method nag-handle sa 'Upsert' logic (Update or Insert). 
     * Ang system maoy mag-decide kung kailangan ba i-create ang vessel o i-update ra.
     */
    private int getOrCreateVessel(String name, String type, int cap) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(SQLConstants.FIND_VESSEL)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) { 
                    if (rs.next()) {
                        int vId = rs.getInt(1);
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