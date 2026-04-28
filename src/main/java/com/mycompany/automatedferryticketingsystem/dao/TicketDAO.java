package com.mycompany.automatedferryticketingsystem.dao;

import com.mycompany.automatedferryticketingsystem.model.Ticket;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TicketDAO Class
 * Mao ni ang responsable sa pag-save sa mga ticket details ngadto sa MariaDB database.
 * (This is responsible for saving ticket details into the MariaDB database.)
 * 
 * LOGIC EXPLANATION:
 * Gi-abstract ani nga class ang SQL complexity para ang ubang classes mo-tawag na lang 
 * sa save function without worrying about database connections or queries.
 */
public class TicketDAO {
    
    // // Encapsulation: Private field para controlled ang access sa dataSource.
    private final HikariDataSource dataSource;

    // // Composition: Ang TicketDAO "Has-A" HikariDataSource para maka-connect sa DB.
    public TicketDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Function para i-save ang ticket sa database.
     * (Function to save the ticket to the database.)
     * 
     * // Abstraction: Ang complexity sa SQL INSERT process kay gitago sulod ani nga method.
     */
    public boolean saveTicket(Ticket ticket) {
        // Logic: Siguraduhon nga naay Transaction ID. Gi-limit sa "TX-" para dili molapas sa VARCHAR(20).
        if (ticket.getTransactionId() == null || ticket.getTransactionId().isEmpty()) {
            ticket.setTransactionId("TX-" + (System.currentTimeMillis() % 1000000000L));
        }

        // Logic: Kung wala pay seat number, mag-auto calculate base sa trip ID.
        if (ticket.getSeatNumber() == null || ticket.getSeatNumber().isEmpty()) {
            int nextSeat = getNextSeatNumber(ticket.getTripId());
            ticket.setSeatNumber("S-" + nextSeat);
        }

        // SQL query logic
        String sql = "INSERT INTO tickets (transaction_id, passenger_name, contact_number, " +
                     "address, age, category, id_number, seat_number, trip_id, " +
                     "base_fare, final_fare, payment_method, payment_status) " + 
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // // Abstraction: Using try-with-resources to automatically handle connection closing.
        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // // Encapsulation: Getting data from the Ticket object using its getter methods.
            ps.setString(1, ticket.getTransactionId());
            ps.setString(2, ticket.getPassengerName());
            ps.setString(3, ticket.getContactNumber());
            ps.setString(4, ticket.getAddress());
            ps.setInt(5, ticket.getAge());
            ps.setString(6, ticket.getCategory());
            ps.setString(7, ticket.getIdNumber());
            ps.setString(8, ticket.getSeatNumber());
            ps.setInt(9, ticket.getTripId()); 
            ps.setDouble(10, ticket.getBaseFare());
            ps.setDouble(11, ticket.getFinalFare());
            ps.setString(12, ticket.getPaymentMethod()); 
            ps.setString(13, "Confirmed"); 

            int result = ps.executeUpdate();
            
            if (result > 0) {
                System.out.println(">>> SUCCESS: Ticket " + ticket.getTransactionId() + " saved to MariaDB.");
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("!!! DATABASE ERROR [TicketDAO]: " + e.getMessage());
            e.printStackTrace(); 
            return false;
        }
    }

    /**
     * Function para kwentahon kung unsa ang sunod nga seat number sa usa ka byahe.
     */
    public int getNextSeatNumber(int tripId) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE trip_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) + 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Seat Calc Error: " + e.getMessage());
        }
        return 1; 
    }
}
