// DatabaseConnection.java
// Manages MySQL database connections with connection pooling.
// Provides static methods to get connections and handles initialization.

package db;

import java.sql.*;
import java.util.Properties;
import java.io.*;

public class DatabaseConnection {
    
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String PROPERTIES_FILE = "src/config/db.properties";
    
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    
    // Static initializer to load configuration
    static {
        try {
            loadConfiguration();
        } catch (IOException e) {
            System.err.println("Failed to load database configuration: " + e.getMessage());
        }
    }
    
    /**
     * Loads database configuration from db.properties file
     */
    private static void loadConfiguration() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            props.load(fis);
            DB_URL = props.getProperty("db.url");
            DB_USER = props.getProperty("db.user");
            DB_PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            System.err.println("Configuration file not found at: " + PROPERTIES_FILE);
            throw e;
        }
    }
    
    /**
     * Gets a new database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DB_DRIVER);
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Make sure mysql-connector-java is in classpath.", e);
        }
    }
    
    /**
     * Tests the database connection
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Database connection successful!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Closes a connection safely
     * @param conn Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Closes a prepared statement safely
     * @param stmt Statement to close
     */
    public static void closePreparedStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing prepared statement: " + e.getMessage());
            }
        }
    }
    
    /**
     * Closes a result set safely
     * @param rs ResultSet to close
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error closing result set: " + e.getMessage());
            }
        }
    }
}
