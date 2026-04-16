package com.mycompany.automatedferryticketingsystem.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Logic: Kuhaon ang data gikan sa JVM arguments (-DDB_URL, etc.) para dili ma-expose ang password sa code.
    private static final String URL = System.getProperty("DB_URL");
    private static final String USER = System.getProperty("DB_USER");
    private static final String PASS = System.getProperty("DB_PASS");

    static {
        try {
            // Driver Load: I-prepare ang MariaDB connection engine kausa ra inig start sa app.
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver Init Failed: " + e.getMessage());
        }
    }

    /**
     * Thread-Safety: Ang 'synchronized' nagsiguro nga dili mag-crash ang app kung dungan mangayo og connection ang UI.
     */
    public static synchronized Connection getConnection() throws SQLException {

        // Fail-fast: Mo-stop ang execution kung wala ma-set ang -D flags sa JVM settings.
        if (URL == null || USER == null || PASS == null) {
            throw new SQLException("Missing Properties: Palihug i-set ang -DDB_URL, -DDB_USER, ug -DDB_PASS.");
        }

        return DriverManager.getConnection(URL, USER, PASS);
    }
}
