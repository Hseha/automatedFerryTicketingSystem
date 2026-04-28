package com.mycompany.automatedferryticketingsystem.util;

/**
 * [LOGIC OVERVIEW]
 * 1. CENTRALIZED QUERIES: Gitigom tanan SQL dinhi para dali ra i-maintain ug dili magkalat sa DAO.
 * 2. SEAT LOGIC: Naggamit og subqueries para ma-compute ang availability in real-time.
 * 3. COUPLING: Kini nga mga queries direktang nag-mapa sa 'Trip', 'Vessel', ug 'Ticket' model classes.
 */
public class SQLConstants {

    // --- 1. TRIP MANAGEMENT (Logic para sa pag-manage sa mga biyahe) ---
    
    /**
     * LOGIC: Calculation of available seats.
     * [CONNECTION]: Gigamit sa VesselDAO para i-populate ang 'Trip' object.
     * Ang 'seats_available' nga column mo-mapa sa Trip.setSeatsAvailable().
     */
    public static final String FETCH_TRIPS_WITH_AVAILABILITY = 
        "SELECT t.*, v.vessel_name, v.vessel_type, v.capacity, v.status AS vessel_status, " +
        "(v.capacity - IFNULL((SELECT COUNT(*) FROM tickets WHERE trip_id = t.trip_id), 0)) AS seats_available " +
        "FROM trips t JOIN vessels v ON t.vessel_id = v.vessel_id " +
        "WHERE t.is_archived = ? ORDER BY t.etd ASC";

    /**
     * [CONNECTION]: Gigamit sa AdminDashboard/DAO inig create og bag-ong schedule.
     * Ang data gikan sa UI i-pasa sa database pinaagi ani.
     */
    public static final String INSERT_TRIP = 
        "INSERT INTO trips (vessel_id, route, etd, eta, base_fare, trip_status, pier_no) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /**
     * [CONNECTION]: Gigamit para sa 'Edit' function sa imong system schedule.
     */
    public static final String UPDATE_TRIP_DETAILS = 
        "UPDATE trips SET vessel_id = ?, route = ?, etd = ?, eta = ?, base_fare = ?, pier_no = ? " +
        "WHERE trip_id = ?";

    /**
     * LOGIC: 'Soft Delete' - I-hide lang ang trip sa view pero naa gihapon sa DB.
     * [CONNECTION]: Importante ni para dili mawala ang records sa reports.
     */
    public static final String ARCHIVE_TRIP = "UPDATE trips SET is_archived = 1 WHERE trip_id = ?";
    
    /**
     * LOGIC: 'Hard Delete' - Full cleanup.
     * [CONNECTION]: Kinahanglan i-call una ang DELETE_TICKETS tungod sa Foreign Key constraints.
     */
    public static final String DELETE_TRIP = "DELETE FROM trips WHERE trip_id = ?";
    public static final String DELETE_TICKETS = "DELETE FROM tickets WHERE trip_id = ?";

    // --- 2. STATUS & SAFETY (Logic para sa maintenance ug weather protocols) ---

    /**
     * [CONNECTION]: Mo-update sa 'status' field sa 'Vessel' model class.
     */
    public static final String RESTORE_VESSEL_STATUS = 
        "UPDATE vessels SET status = 'In Service', threat_level = 'Low' WHERE vessel_id = ?";

    /**
     * [CONNECTION]: Gigamit inig human sa maintenance o kung nindot na ang panahon.
     */
    public static final String RESTORE_TRIP_STATUS = 
        "UPDATE trips SET trip_status = 'Available', status_reason = '' WHERE trip_id = ? AND is_archived = 0";

    public static final String UPDATE_VESSEL_CONDITION = 
        "UPDATE vessels SET status = ? WHERE vessel_id = ?";

    /**
     * LOGIC: Global cancellation logic.
     * [CONNECTION]: Mo-update sa 'displayStatus' ug 'statusReason' sa 'Trip' object.
     */
    public static final String UPDATE_TRIP_STATUS_WITH_REASON = 
        "UPDATE trips SET trip_status = ?, status_reason = ? WHERE vessel_id = ? AND is_archived = 0";

    // --- 3. VESSEL DATA (Logic para sa barko) ---

    public static final String FIND_VESSEL = "SELECT vessel_id FROM vessels WHERE vessel_name = ? LIMIT 1";

    /**
     * [CONNECTION]: Gigamit sa pag-add og bag-ong barko sa 'vessels' table.
     */
    public static final String INSERT_VESSEL = 
        "INSERT INTO vessels (vessel_name, vessel_type, capacity, status, threat_level) VALUES (?, ?, ?, 'In Service', 'Low')";

    /**
     * [CONNECTION]: Mo-populate sa List<Vessel> para sa imong JTable o ComboBoxes.
     */
    public static final String FETCH_ALL_VESSELS = 
        "SELECT vessel_id, vessel_name, vessel_type, capacity, status, threat_level FROM vessels";

    // --- 4. LOGGING & REASONS (Logic para sa Admin analytics) ---

    /**
     * LOGIC: Smart Frequency Tracking.
     * [CONNECTION]: Wala ni'y direct model class, apan gigamit ni para sa 
     * 'Top Reasons' dashboard analytics para sa Admin.
     */
    public static final String LOG_REASON_UPSERT = 
        "INSERT INTO status_reasons (category, reason_text, frequency) VALUES (?, ?, 1) " + 
        "ON DUPLICATE KEY UPDATE frequency = frequency + 1";
}