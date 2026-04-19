package com.mycompany.automatedferryticketingsystem.dao;

import com.mycompany.automatedferryticketingsystem.model.Vessel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VesselDAO {
    
    private static final String SQL = 
        "SELECT v.vessel_id, v.vessel_name, t.route, v.vessel_type, v.status, " +
        "t.departure_time, v.capacity, " +
        "(v.capacity - COALESCE(b.booked_seats, 0)) as remaining_seats, " +
        "CASE WHEN t.departure_time < NOW() THEN 'Past' ELSE 'Upcoming' END as trip_status " +
        "FROM vessels v " +
        "JOIN trips t ON v.vessel_id = t.vessel_id " +
        "LEFT JOIN (SELECT trip_id, COUNT(*) as booked_seats FROM bookings " +
        "WHERE booking_status = 'Confirmed' GROUP BY trip_id) b ON t.trip_id = b.trip_id " +
        "WHERE (v.vessel_name LIKE ? OR t.route LIKE ? OR v.vessel_type LIKE ?) " +
        "ORDER BY t.departure_time DESC LIMIT 500";

    public List<Vessel> getAvailableVessels(String query) {
        List<Vessel> vessels = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
             
            String pattern = (query == null || query.trim().isEmpty()) ? "%" : "%" + query.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vessel v = new Vessel();
                    v.setVesselId(rs.getInt("vessel_id"));
                    v.setVesselName(rs.getString("vessel_name"));
                    v.setRoute(rs.getString("route"));
                    v.setVesselType(rs.getString("vessel_type"));
                    v.setStatus(rs.getString("status"));
                    v.setDepartureTime(rs.getString("departure_time"));
                    v.setCapacity(rs.getInt("capacity"));
                    v.setRemainingSeats(rs.getInt("remaining_seats"));
                    v.setTripStatus(rs.getString("trip_status"));
                    vessels.add(v);
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO Error: " + e.getMessage());
        }
        return vessels;
    }
}