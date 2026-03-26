// BaseDAO.java
// Abstract base class for all DAOs providing common database operations.
// Handles connection management and basic CRUD utilities.

package dao;

import db.DatabaseConnection;
import java.sql.*;

public abstract class BaseDAO {
    
    /**
     * Executes an INSERT, UPDATE, or DELETE query
     * @param sql SQL query with ? placeholders
     * @param params Parameters to bind to the query
     * @return Number of affected rows
     */
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            bindParameters(stmt, params);
            return stmt.executeUpdate();
        } finally {
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }
    
    /**
     * Executes an INSERT query and returns the generated ID
     * @param sql SQL INSERT query with ? placeholders
     * @param params Parameters to bind to the query
     * @return Generated auto-increment ID
     */
    protected int executeInsertWithId(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            bindParameters(stmt, params);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }
    
    /**
     * Executes a SELECT query with single-row result
     * Subclasses should override to parse result set
     * @param sql SQL query with ? placeholders
     * @param params Parameters to bind to the query
     * @return ResultSet for subclass to parse
     */
    protected ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        bindParameters(stmt, params);
        return stmt.executeQuery();
    }
    
    /**
     * Helper method to bind parameters to prepared statement
     * @param stmt PreparedStatement to bind to
     * @param params Parameters to bind
     */
    protected void bindParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                stmt.setNull(i + 1, Types.VARCHAR);
            } else if (params[i] instanceof String) {
                stmt.setString(i + 1, (String) params[i]);
            } else if (params[i] instanceof Integer) {
                stmt.setInt(i + 1, (Integer) params[i]);
            } else if (params[i] instanceof Double) {
                stmt.setDouble(i + 1, (Double) params[i]);
            } else if (params[i] instanceof Boolean) {
                stmt.setBoolean(i + 1, (Boolean) params[i]);
            } else if (params[i] instanceof Long) {
                stmt.setLong(i + 1, (Long) params[i]);
            } else if (params[i] instanceof java.time.LocalDateTime) {
                stmt.setString(i + 1, params[i].toString());
            } else {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }
}
