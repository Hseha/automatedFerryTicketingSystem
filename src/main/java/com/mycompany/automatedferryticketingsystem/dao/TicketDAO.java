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
 */
public class TicketDAO {
    private final HikariDataSource dataSource;

    public TicketDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Function para i-save ang ticket sa database.
     * (Function to save the ticket to the database.)
     */
    public boolean saveTicket(Ticket ticket) {
        // Logic: Siguraduhon nga naay Transaction ID. Gi-limit sa "TX-" para dili molapas sa VARCHAR(20).
        // (Ensuring there is a Transaction ID. Limited to "TX-" to stay within VARCHAR(20).)
        if (ticket.getTransactionId() == null || ticket.getTransactionId().isEmpty()) {
            ticket.setTransactionId("TX-" + (System.currentTimeMillis() % 1000000000L));
        }

        // Logic: Kung wala pay seat number, mag-auto calculate base sa trip ID.
        // (If no seat number yet, auto-calculate based on the trip ID.)
        if (ticket.getSeatNumber() == null || ticket.getSeatNumber().isEmpty()) {
            int nextSeat = getNextSeatNumber(ticket.getTripId());
            ticket.setSeatNumber("S-" + nextSeat);
        }

        // SQL query nga nag-match gyud sa imong 'tickets' table structure.
        // (SQL query that exactly matches your 'tickets' table structure.)
        String sql = "INSERT INTO tickets (transaction_id, passenger_name, contact_number, " +
                     "address, age, category, id_number, seat_number, trip_id, " +
                     "base_fare, final_fare, payment_method, payment_status) " + 
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Paggamit og try-with-resources para auto-close ang connection.
        // (Using try-with-resources for automatic connection closing.)
        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Pag-set sa mga values sa matag placeholder (?).
            // (Setting the values for each placeholder (?).)
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
            ps.setString(13, "Confirmed"); // Default status inig save.

            int result = ps.executeUpdate();
            
            if (result > 0) {
                // Kon nag-success, mo-print ni sa console.
                // (If successful, this prints to the console.)
                System.out.println(">>> SUCCESS: Ticket " + ticket.getTransactionId() + " saved to MariaDB.");
                return true;
            }
            return false;

        } catch (SQLException e) {
            // Error handling kung naay problema sa database (sama sa duplicate entry).
            // (Error handling if there's a database problem like duplicate entry.)
            System.err.println("!!! DATABASE ERROR [TicketDAO]: " + e.getMessage());
            e.printStackTrace(); 
            return false;
        }
    }

    /**
     * Function para kwentahon kung unsa ang sunod nga seat number sa usa ka byahe.
     * (Function to calculate the next seat number for a specific trip.)
     */
    public int getNextSeatNumber(int tripId) {
        // Mo-count kung pila nay naka-book sa maong trip ID.
        // (Counts how many are already booked for that trip ID.)
        String sql = "SELECT COUNT(*) FROM tickets WHERE trip_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Ang sunod nga seat number kay ang total count + 1.
                    // (The next seat number is the total count + 1.)
                    return rs.getInt(1) + 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Seat Calc Error: " + e.getMessage());
        }
        return 1; // Default sa 1 kung wala pay sulod ang table.
    }
}