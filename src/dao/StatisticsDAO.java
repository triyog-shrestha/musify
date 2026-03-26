// StatisticsDAO.java
// Database operations for Statistics table

package dao;

import db.DatabaseConnection;
import java.sql.*;

public class StatisticsDAO extends BaseDAO {

    /**
     * Get or create statistics for a user
     * @param userId User ID
     * @return Statistics array: [totalPlays, totalMinutesListened]
     */
    public double[] getUserStatistics(int userId) throws SQLException {
        String sql = "SELECT totalPlays, totalMinutesListened FROM Statistics WHERE userId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return new double[]{rs.getInt("totalPlays"), rs.getDouble("totalMinutesListened")};
            }
            // Create new statistics entry if doesn't exist
            createUserStatistics(userId);
            return new double[]{0, 0};
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Create initial statistics for a new user
     * @param userId User ID
     */
    public void createUserStatistics(int userId) throws SQLException {
        String sql = "INSERT INTO Statistics (userId, totalPlays, totalMinutesListened) " +
                     "VALUES (?, 0, 0) " +
                     "ON DUPLICATE KEY UPDATE totalPlays = totalPlays";
        executeUpdate(sql, userId);
    }

    /**
     * Update total plays for a user
     * @param userId User ID
     * @param plays Total plays
     */
    public void updateTotalPlays(int userId, int plays) throws SQLException {
        String sql = "UPDATE Statistics SET totalPlays = ? WHERE userId = ?";
        executeUpdate(sql, plays, userId);
    }

    /**
     * Update total minutes listened for a user
     * @param userId User ID
     * @param minutes Total minutes
     */
    public void updateTotalMinutesListened(int userId, double minutes) throws SQLException {
        String sql = "UPDATE Statistics SET totalMinutesListened = ? WHERE userId = ?";
        executeUpdate(sql, minutes, userId);
    }

    /**
     * Increment total plays for a user
     * @param userId User ID
     * @param incrementBy Amount to increment by
     */
    public void incrementPlays(int userId, int incrementBy) throws SQLException {
        String sql = "UPDATE Statistics SET totalPlays = totalPlays + ? WHERE userId = ?";
        executeUpdate(sql, incrementBy, userId);
    }

    /**
     * Increment total minutes listened for a user
     * @param userId User ID
     * @param incrementBy Amount (in minutes) to increment by
     */
    public void incrementMinutesListened(int userId, double incrementBy) throws SQLException {
        String sql = "UPDATE Statistics SET totalMinutesListened = totalMinutesListened + ? WHERE userId = ?";
        executeUpdate(sql, incrementBy, userId);
    }

    /**
     * Get total plays for a user
     * @param userId User ID
     * @return Total plays
     */
    public int getTotalPlays(int userId) throws SQLException {
        double[] stats = getUserStatistics(userId);
        return (int) stats[0];
    }

    /**
     * Get total minutes listened for a user
     * @param userId User ID
     * @return Total minutes
     */
    public double getTotalMinutesListened(int userId) throws SQLException {
        double[] stats = getUserStatistics(userId);
        return stats[1];
    }

    /**
     * Reset user statistics
     * @param userId User ID
     */
    public void resetStatistics(int userId) throws SQLException {
        String sql = "UPDATE Statistics SET totalPlays = 0, totalMinutesListened = 0 WHERE userId = ?";
        executeUpdate(sql, userId);
    }
}
