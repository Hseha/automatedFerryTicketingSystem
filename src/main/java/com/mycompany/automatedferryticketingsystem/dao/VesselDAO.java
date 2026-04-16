package com.mycompany.automatedferryticketingsystem.dao;

import com.mycompany.automatedferryticketingsystem.model.Vessel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DATABASE ACCESS LAYER: 
 * Kini nga class mao ang nag-handle sa tanang transactions sa MariaDB. 
 * Gi-patch na kini para masiguro nga dili dali ma-hack ang atong queries.
 */
public class VesselDAO {

    /**
     * SEARCH LOGIC: Gidisenyo kini para sa "Real-time Seat Tracking". 
     * Imbis nga static data ang ipakita, ang system ang mo-calculate sa nabilin nga seats 
     * pinaagi sa pag-subtract sa 'Capacity' sa 'Tickets' nga nahalin na.
     */
    public List<Vessel> searchVessels(String searchTerm) {
        List<Vessel> vessels = new ArrayList<>();
        
        // Ang paggamit og '?' (placeholder) kay usa ka security best practice. 
        // Kini ang mopugong sa "SQL Injection" kay ang driver na ang mo-sanitize 
        // sa input aron dili kini mahimong parte sa SQL command.
        String sql = "SELECT v.vessel_id, v.vessel_name, " +
                     "(v.capacity - (SELECT COUNT(*) FROM tickets t WHERE t.vessel_id = v.vessel_id)) AS remaining, " +
                     "v.status FROM vessels v " +
                     "WHERE v.status = 'In Service' AND v.vessel_name LIKE ?";

        // Gigamit nato ang 'Try-with-resources' para sa Connection ug PreparedStatement. 
        // Importante kini sa atong Arch Linux setup aron ang RAM ma-release dayon 
        // inig human sa query, bisan pa og naay error nga mahitabo.
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Dari nato gi-bind ang 'searchTerm' sa placeholder. 
            // Ang wildcards (%) nagtugot sa system nga makit-an ang barko bisan 
            // part lang sa ngalan ang i-type sa user sa Search Bar.
            ps.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = ps.executeQuery()) {
                // Samtang naay makit-an nga matching rows sa database, 
                // atong i-transform ang SQL data ngadto sa Java Objects (Vessel).
                while (rs.next()) {
                    Vessel v = new Vessel();
                    v.setVesselId(rs.getInt("vessel_id"));
                    v.setVesselName(rs.getString("vessel_name"));
                    v.setCapacity(rs.getInt("remaining")); // Gi-store nato ang 'Remaining' math result.
                    v.setStatus(rs.getString("status"));
                    vessels.add(v);
                }
            }
        } catch (SQLException e) {
            // Kini magtabang kanato sa debugging pinaagi sa pag-print 
            // sa error message kung naay problema sa connection o sa SQL syntax.
            System.err.println("DAO Search Error: " + e.getMessage());
        }
        return vessels;
    }

    /**
     * Kini nga method mag implement og 'Code Reuse'. 
     * Imbis maghimo og laing logic para makuha ang tanang vessels, 
     * atong gigamit ang 'searchVessels' nga naay empty string aron simple ang flow.
     */
    public List<Vessel> getAvailableVessels() {
        return searchVessels("");
    }
}
