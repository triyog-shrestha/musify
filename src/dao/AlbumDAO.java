// AlbumDAO.java
// Database operations for Album table

package dao;

import model.Album;
import db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO extends BaseDAO {

    /**
     * Create a new album
     * @param album Album to create
     */
    public void createAlbum(Album album) throws SQLException {
        String sql = "INSERT INTO Album (albumName) VALUES (?)";
        int generatedId = executeInsertWithId(sql, album.getAlbumName());
        album.setAlbumId(generatedId);
    }

    /**
     * Get album by ID
     * @param albumId Album ID
     * @return Album if found, null otherwise
     */
    public Album getAlbumById(int albumId) throws SQLException {
        String sql = "SELECT albumId, albumName FROM Album WHERE albumId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, albumId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Get album by name
     * @param albumName Album name
     * @return Album if found, null otherwise
     */
    public Album getAlbumByName(String albumName) throws SQLException {
        String sql = "SELECT albumId, albumName FROM Album WHERE albumName = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, albumName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Get all albums
     * @return List of all albums
     */
    public List<Album> getAllAlbums() throws SQLException {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT albumId, albumName FROM Album";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                albums.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return albums;
    }

    /**
     * Update album
     * @param album Album with updated data
     */
    public void updateAlbum(Album album) throws SQLException {
        String sql = "UPDATE Album SET albumName = ? WHERE albumId = ?";
        executeUpdate(sql, album.getAlbumName(), album.getAlbumId());
    }

    /**
     * Delete album
     * @param albumId Album ID to delete
     */
    public void deleteAlbum(int albumId) throws SQLException {
        String sql = "DELETE FROM Album WHERE albumId = ?";
        executeUpdate(sql, albumId);
    }

    /**
     * Helper to convert ResultSet to Album object
     */
    private Album fromResultSet(ResultSet rs) throws SQLException {
        return new Album(rs.getInt("albumId"), rs.getString("albumName"));
    }
}
