// ArtistDAO.java
// Database operations for Artist table

package dao;

import model.Artist;
import db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtistDAO extends BaseDAO {

    /**
     * Create a new artist
     * @param artist Artist to create
     */
    public void createArtist(Artist artist) throws SQLException {
        String sql = "INSERT INTO Artist (artistName) VALUES (?)";
        int generatedId = executeInsertWithId(sql, artist.getArtistName());
        artist.setArtistId(generatedId);
    }

    /**
     * Get artist by ID
     * @param artistId Artist ID
     * @return Artist if found, null otherwise
     */
    public Artist getArtistById(int artistId) throws SQLException {
        String sql = "SELECT artistId, artistName FROM Artist WHERE artistId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, artistId);
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
     * Get artist by name
     * @param artistName Artist name
     * @return Artist if found, null otherwise
     */
    public Artist getArtistByName(String artistName) throws SQLException {
        String sql = "SELECT artistId, artistName FROM Artist WHERE artistName = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, artistName);
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
     * Get all artists
     * @return List of all artists
     */
    public List<Artist> getAllArtists() throws SQLException {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT artistId, artistName FROM Artist";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                artists.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return artists;
    }

    /**
     * Update artist
     * @param artist Artist with updated data
     */
    public void updateArtist(Artist artist) throws SQLException {
        String sql = "UPDATE Artist SET artistName = ? WHERE artistId = ?";
        executeUpdate(sql, artist.getArtistName(), artist.getArtistId());
    }

    /**
     * Delete artist
     * @param artistId Artist ID to delete
     */
    public void deleteArtist(int artistId) throws SQLException {
        String sql = "DELETE FROM Artist WHERE artistId = ?";
        executeUpdate(sql, artistId);
    }

    /**
     * Helper to convert ResultSet to Artist object
     */
    private Artist fromResultSet(ResultSet rs) throws SQLException {
        return new Artist(rs.getInt("artistId"), rs.getString("artistName"));
    }
}
