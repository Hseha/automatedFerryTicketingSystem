package com.mycompany.automatedferryticketingsystem.dao;

import com.mycompany.automatedferryticketingsystem.model.Ticket;
import com.zaxxer.hikari.HikariDataSource; // Import the connection pool
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TicketDAO {
    // Field to hold the data source
    private final HikariDataSource dataSource;

    // MANDATORY CONSTRUCTOR: This fixes the FinalTicketUI error
    public TicketDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean saveTicket(Ticket ticket) {
        if (ticket.getTransactionId() == null || ticket.getTransactionId().isEmpty()) {
            ticket.setTransactionId("F-TXN-" + (System.currentTimeMillis() % 1000000));
        }

        if (ticket.getSeatNumber() == null || ticket.getSeatNumber().isEmpty()) {
            int nextSeat = getNextSeatNumber(ticket.getTripId());
            ticket.setSeatNumber("S-" + nextSeat);
        }

        String sql = "INSERT INTO tickets (transaction_id, passenger_name, contact_number, " +
                     "address, age, category, id_number, seat_number, trip_id, " +
                     "base_fare, final_fare, payment_method, payment_status) " + 
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Use the dataSource to get a connection from the pool
        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

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

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database Error [TicketDAO]: " + e.getMessage());
            return false;
        }
    }

    public int getNextSeatNumber(int tripId) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE trip_id = ?";
        // Use the dataSource here as well
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Seat Calc Error: " + e.getMessage());
        }
        return 1;
    }
}