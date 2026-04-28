package com.mycompany.automatedferryticketingsystem.util;

/**
 * [LOGIC OVERVIEW]
 * 1. CENTRALIZED QUERIES: Gitigom tanan SQL dinhi para dali ra i-maintain ug dili magkalit sa DAO.
 * 2. SEAT LOGIC: Naggamit og subqueries para ma-compute ang availability in real-time.
 * 3. UPSERT LOGIC: Gigamit ang 'ON DUPLICATE KEY UPDATE' para sa automatic frequency tracking.
 */
public class SQLConstants {

    // --- 1. TRIP MANAGEMENT (Logic para sa pag-manage sa mga biyahe) ---
    
    /**
     * LOGIC: Calculation of available seats.
     * Naggamit og 'IFNULL' para masiguro nga kung bag-o pa ang trip (0 tickets), 
     * dili 'null' ang mogawas kundi '0' para sakto ang subtraction gikan sa capacity.
     */
    public static final String FETCH_TRIPS_WITH_AVAILABILITY = 
        "SELECT t.*, v.vessel_name, v.vessel_type, v.capacity, v.status AS vessel_status, " +
        "(v.capacity - IFNULL((SELECT COUNT(*) FROM tickets WHERE trip_id = t.trip_id), 0)) AS seats_available " +
        "FROM trips t JOIN vessels v ON t.vessel_id = v.vessel_id " +
        "WHERE t.is_archived = ? ORDER BY t.etd ASC";

    public static final String INSERT_TRIP = 
        "INSERT INTO trips (vessel_id, route, etd, eta, base_fare, trip_status, pier_no) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_TRIP_DETAILS = 
        "UPDATE trips SET vessel_id = ?, route = ?, etd = ?, eta = ?, base_fare = ?, pier_no = ? " +
        "WHERE trip_id = ?";

    // Logic: 'Soft Delete' - I-hide lang ang trip sa view pero naa gihapon sa DB for history.
    public static final String ARCHIVE_TRIP = "UPDATE trips SET is_archived = 1 WHERE trip_id = ?";
    
    // Logic: 'Hard Delete' - Tangtangon gyud ang record. Kinahanglan i-delete una ang tickets.
    public static final String DELETE_TRIP = "DELETE FROM trips WHERE trip_id = ?";
    public static final String DELETE_TICKETS = "DELETE FROM tickets WHERE trip_id = ?";

    // --- 2. STATUS & SAFETY (Logic para sa maintenance ug weather protocols) ---

    // Logic: I-reset ang vessel status balik sa normal operation.
    public static final String RESTORE_VESSEL_STATUS = 
        "UPDATE vessels SET status = 'In Service', threat_level = 'Low' WHERE vessel_id = ?";

    // Logic: I-restore ang trip accessibility basta wala pa kini na-archive.
    public static final String RESTORE_TRIP_STATUS = 
        "UPDATE trips SET trip_status = 'Available', status_reason = '' WHERE trip_id = ? AND is_archived = 0";

    public static final String UPDATE_VESSEL_CONDITION = 
        "UPDATE vessels SET status = ? WHERE vessel_id = ?";

    // Logic: I-update ang trip status (e.g., Cancelled) dungan sa pag-input sa rason (e.g., Bad Weather).
    public static final String UPDATE_TRIP_STATUS_WITH_REASON = 
        "UPDATE trips SET trip_status = ?, status_reason = ? WHERE vessel_id = ? AND is_archived = 0";

    // --- 3. VESSEL DATA (Logic para sa barko) ---

    public static final String FIND_VESSEL = "SELECT vessel_id FROM vessels WHERE vessel_name = ? LIMIT 1";

    public static final String INSERT_VESSEL = 
        "INSERT INTO vessels (vessel_name, vessel_type, capacity, status, threat_level) VALUES (?, ?, ?, 'In Service', 'Low')";

    public static final String FETCH_ALL_VESSELS = 
        "SELECT vessel_id, vessel_name, vessel_type, capacity, status, threat_level FROM vessels";

    // --- 4. LOGGING & REASONS (Logic para sa Admin analytics) ---

    /**
     * LOGIC: Smart Frequency Tracking.
     * Kung ang rason (e.g., 'Maintenance') naa na sa database, i-increment lang ang 'frequency' count.
     * Kung wala pa, i-insert kini as new record. Mapuslan ni para sa top reasons chart.
     */
    public static final String LOG_REASON_UPSERT = 
        "INSERT INTO status_reasons (category, reason_text, frequency) VALUES (?, ?, 1) " + 
        "ON DUPLICATE KEY UPDATE frequency = frequency + 1";
}