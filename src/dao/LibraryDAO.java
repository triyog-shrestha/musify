// LibraryDAO.java
// Database operations for Library table and user song statistics

package dao;

import db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryDAO extends BaseDAO {

    /**
     * Add a song to user's library
     * @param userId User ID
     * @param songId Song ID
     * @param playCount Initial play count
     */
    public void addSongToLibrary(int userId, int songId, int playCount) throws SQLException {
        String sql = "INSERT INTO Library (userId, songId, playCount) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE playCount = ?";
        executeUpdate(sql, userId, songId, playCount, playCount);
    }

    /**
     * Update play count for a song in user's library
     * @param userId User ID
     * @param songId Song ID
     * @param playCount New play count
     */
    public void updatePlayCount(int userId, int songId, int playCount) throws SQLException {
        String sql = "UPDATE Library SET playCount = ? WHERE userId = ? AND songId = ?";
        executeUpdate(sql, playCount, userId, songId);
    }

    /**
     * Get play count for a song by user
     * @param userId User ID
     * @param songId Song ID
     * @return Play count
     */
    public int getPlayCount(int userId, int songId) throws SQLException {
        String sql = "SELECT playCount FROM Library WHERE userId = ? AND songId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, songId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("playCount");
            }
            return 0;
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Get all songs in user's library
     * @param userId User ID
     * @return List of song IDs
     */
    public List<Integer> getUserLibrarySongs(int userId) throws SQLException {
        List<Integer> songIds = new ArrayList<>();
        String sql = "SELECT songId FROM Library WHERE userId = ? ORDER BY songId";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                songIds.add(rs.getInt("songId"));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return songIds;
    }

    /**
     * Remove song from user's library
     * @param userId User ID
     * @param songId Song ID
     */
    public void removeSongFromLibrary(int userId, int songId) throws SQLException {
        String sql = "DELETE FROM Library WHERE userId = ? AND songId = ?";
        executeUpdate(sql, userId, songId);
    }

    /**
     * Check if song is in user's library
     * @param userId User ID
     * @param songId Song ID
     * @return true if song is in library
     */
    public boolean isSongInLibrary(int userId, int songId) throws SQLException {
        return getPlayCount(userId, songId) > 0 || hasSongInLibrary(userId, songId);
    }

    /**
     * Helper method to check library membership
     */
    private boolean hasSongInLibrary(int userId, int songId) throws SQLException {
        String sql = "SELECT 1 FROM Library WHERE userId = ? AND songId = ? LIMIT 1";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, songId);
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Get total play count for user across all songs
     * @param userId User ID
     * @return Total play count
     */
    public int getTotalPlaysForUser(int userId) throws SQLException {
        String sql = "SELECT SUM(playCount) as total FROM Library WHERE userId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Clear user's library
     * @param userId User ID
     */
    public void clearLibrary(int userId) throws SQLException {
        String sql = "DELETE FROM Library WHERE userId = ?";
        executeUpdate(sql, userId);
    }
}
