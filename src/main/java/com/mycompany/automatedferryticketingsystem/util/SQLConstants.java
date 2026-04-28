package com.mycompany.automatedferryticketingsystem.util;

/**
 * [LOGIC OVERVIEW]
 * 1. CENTRALIZED QUERIES: Gitigom tanan SQL dinhi para dali ra i-maintain ug dili magkalat sa DAO.
 * 2. SEAT LOGIC: Naggamit og subqueries para ma-compute ang availability in real-time.
 * 3. COUPLING: Kini nga mga queries direktang nag-mapa sa 'Trip', 'Vessel', ug 'Ticket' model classes.
 */
public class SQLConstants {

    // --- 1. TRIP MANAGEMENT ---
    
    /**
     * DQL: SELECT
     * LOGIC: Pag-compute sa available seats pinaagi sa pag-subtract sa ticket count gikan sa capacity.
     * CONNECTION: Gigamit sa VesselDAO para i-populate ang Trip.setSeatsAvailable().
     */
    public static final String FETCH_TRIPS_WITH_AVAILABILITY = 
        "SELECT t.*, v.vessel_name, v.vessel_type, v.capacity, v.status AS vessel_status, " +
        "(v.capacity - IFNULL((SELECT COUNT(*) FROM tickets WHERE trip_id = t.trip_id), 0)) AS seats_available " +
        "FROM trips t JOIN vessels v ON t.vessel_id = v.vessel_id " +
        "WHERE t.is_archived = ? ORDER BY t.etd ASC";

    /**
     * DML: INSERT
     * LOGIC: Pag-record og bag-ong biyahe ngadto sa database.
     * CONNECTION: Gigamit sa AdminDashboard inig save og bag-ong schedule.
     */
    public static final String INSERT_TRIP = 
        "INSERT INTO trips (vessel_id, route, etd, eta, base_fare, trip_status, pier_no) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /**
     * DML: UPDATE
     * LOGIC: Pag-usab sa detalye sa biyahe sama sa ruta o oras.
     * CONNECTION: Gigamit sa Update function sa VesselController.
     */
    public static final String UPDATE_TRIP_DETAILS = 
        "UPDATE trips SET vessel_id = ?, route = ?, etd = ?, eta = ?, base_fare = ?, pier_no = ? " +
        "WHERE trip_id = ?";

    /**
     * DML: UPDATE
     * LOGIC: I-hide ang trip sa UI (Soft Delete) apan pabilin sa database for history.
     * CONNECTION: Gigamit sa Archive feature sa Admin panel.
     */
    public static final String ARCHIVE_TRIP = "UPDATE trips SET is_archived = 1 WHERE trip_id = ?";
    
    /**
     * DML: DELETE
     * LOGIC: Permanenteng pagtangtang sa record.
     * CONNECTION: Kinahanglan i-delete una ang tickets (FK constraint) bago ang trip.
     */
    public static final String DELETE_TRIP = "DELETE FROM trips WHERE trip_id = ?";
    public static final String DELETE_TICKETS = "DELETE FROM tickets WHERE trip_id = ?";

    // --- 2. STATUS & SAFETY ---

    /**
     * DML: UPDATE
     * LOGIC: Pag-restore sa barko ngadto sa normal operation status.
     * CONNECTION: Mo-update sa 'status' field sa Vessel model class.
     */
    public static final String RESTORE_VESSEL_STATUS = 
        "UPDATE vessels SET status = 'In Service', threat_level = 'Low' WHERE vessel_id = ?";

    /**
     * DML: UPDATE
     * LOGIC: Pag-reset sa trip status gikan sa abnormal states (Cancelled/Delayed).
     * CONNECTION: Gigamit sa Trip Management module sa Admin.
     */
    public static final String RESTORE_TRIP_STATUS = 
        "UPDATE trips SET trip_status = 'Available', status_reason = '' WHERE trip_id = ? AND is_archived = 0";

    /**
     * DML: UPDATE
     * LOGIC: Pag-set sa physical condition sa barko (e.g. Maintenance).
     * CONNECTION: Gigamit sa Vessel Monitoring UI.
     */
    public static final String UPDATE_VESSEL_CONDITION = 
        "UPDATE vessels SET status = ? WHERE vessel_id = ?";

    /**
     * DML: UPDATE
     * LOGIC: Batch update sa tanang biyahe nga apektado sa barko nga na-grounded o na-cancel.
     * CONNECTION: Mo-update sa 'displayStatus' sa Trip model.
     */
    public static final String UPDATE_TRIP_STATUS_WITH_REASON = 
        "UPDATE trips SET trip_status = ?, status_reason = ? WHERE vessel_id = ? AND is_archived = 0";

    // --- 3. VESSEL DATA ---

    /**
     * DQL: SELECT
     * LOGIC: Pagpangita sa vessel_id base sa pangalan sa barko.
     * CONNECTION: Gigamit sa validation logic sa DAO.
     */
    public static final String FIND_VESSEL = "SELECT vessel_id FROM vessels WHERE vessel_name = ? LIMIT 1";

    /**
     * DML: INSERT
     * LOGIC: Pag-dugang og bag-ong barko sa system registry.
     * CONNECTION: Gigamit sa Vessel Registration form.
     */
    public static final String INSERT_VESSEL = 
        "INSERT INTO vessels (vessel_name, vessel_type, capacity, status, threat_level) VALUES (?, ?, ?, 'In Service', 'Low')";

    /**
     * DQL: SELECT
     * LOGIC: Pag-kuha sa tibuok listahan sa mga barko para sa table display.
     * CONNECTION: Mo-populate sa List<Vessel> model objects.
     */
    public static final String FETCH_ALL_VESSELS = 
        "SELECT vessel_id, vessel_name, vessel_type, capacity, status, threat_level FROM vessels";

    // --- 4. LOGGING & REASONS ---

    /**
     * DML: UPSERT (INSERT + UPDATE)
     * LOGIC: Pag-track sa frequency sa mga rason sa delay/cancellation para sa analytics.
     * CONNECTION: Gigamit para sa top reasons chart sa Admin Dashboard.
     */
    public static final String LOG_REASON_UPSERT = 
        "INSERT INTO status_reasons (category, reason_text, frequency) VALUES (?, ?, 1) " + 
        "ON DUPLICATE KEY UPDATE frequency = frequency + 1";
}