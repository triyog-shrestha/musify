// RecommendationDAO.java
// Database operations for Recommendation table using JDBC with MySQL

package dao;

import db.DatabaseConnection;
import model.Recommendation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationDAO extends BaseDAO {

    /**
     * Get all recommendations
     * @return List of all recommendations
     */
    public List<Recommendation> getAll() throws SQLException {
        List<Recommendation> recommendations = new ArrayList<>();
        String sql = "SELECT recId, recSongName, recTrackLength, recMood, spotifyUrl FROM Recommendation";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                recommendations.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return recommendations;
    }

    /**
     * Filter recommendations by genre
     * @param genre Genre to filter by
     * @return List of recommendations in that genre
     */
    public List<Recommendation> filterByGenre(String genre) throws SQLException {
        List<Recommendation> results = new ArrayList<>();
        String sql = "SELECT DISTINCT r.recId, r.recSongName, r.recTrackLength, r.recMood, r.spotifyUrl " +
                     "FROM Recommendation r " +
                     "JOIN Recommendation_Genre rg ON r.recId = rg.recId " +
                     "JOIN Genre g ON rg.genreId = g.genreId " +
                     "WHERE LOWER(g.genreName) = LOWER(?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, genre);
            rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return results;
    }

    /**
     * Filter recommendations by mood
     * @param mood Mood to filter by
     * @return List of recommendations with that mood
     */
    public List<Recommendation> filterByMood(String mood) throws SQLException {
        List<Recommendation> results = new ArrayList<>();
        String sql = "SELECT recId, recSongName, recTrackLength, recMood, spotifyUrl FROM Recommendation " +
                     "WHERE LOWER(recMood) = LOWER(?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mood);
            rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return results;
    }

    /**
     * Get a recommendation by ID
     * @param recId Recommendation ID
     * @return Recommendation if found, null otherwise
     */
    public Recommendation getRecommendationById(int recId) throws SQLException {
        String sql = "SELECT recId, recSongName, recTrackLength, recMood, spotifyUrl FROM Recommendation " +
                     "WHERE recId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, recId);
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
     * Create a new recommendation
     * @param rec Recommendation object
     */
    public void createRecommendation(Recommendation rec) throws SQLException {
        String sql = "INSERT INTO Recommendation (recSongName, recTrackLength, recMood, spotifyUrl, albumId) " +
                     "VALUES (?, ?, ?, ?, ?)";
        int generatedId = executeInsertWithId(sql,
                rec.getTrackName(),
                rec.getLength(),
                rec.getMood(),
                rec.getLink(),
                null
        );
        rec.setSongId(generatedId);
    }

    /**
     * Delete a recommendation
     * @param recId Recommendation ID
     */
    public void deleteRecommendation(int recId) throws SQLException {
        String sql = "DELETE FROM Recommendation WHERE recId = ?";
        executeUpdate(sql, recId);
    }

    /**
     * Update a recommendation
     * @param rec Recommendation with updated data
     */
    public void updateRecommendation(Recommendation rec) throws SQLException {
        String sql = "UPDATE Recommendation SET recSongName = ?, recTrackLength = ?, " +
                     "recMood = ?, spotifyUrl = ? WHERE recId = ?";
        executeUpdate(sql,
                rec.getTrackName(),
                rec.getLength(),
                rec.getMood(),
                rec.getLink(),
                rec.getSongId()
        );
    }

    /**
     * Get artists for a recommendation
     * @param recId Recommendation ID
     * @return Pipe-separated artist names
     */
    private String getArtistsForRecommendation(int recId) throws SQLException {
        String sql = "SELECT a.artistName FROM Artist a " +
                     "JOIN Recommendation_Artist ra ON a.artistId = ra.artistId " +
                     "WHERE ra.recId = ?";
        List<String> artists = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, recId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                artists.add(rs.getString("artistName"));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return String.join("|", artists);
    }

    /**
     * Get genres for a recommendation
     * @param recId Recommendation ID
     * @return Pipe-separated genre names
     */
    private String getGenresForRecommendation(int recId) throws SQLException {
        String sql = "SELECT g.genreName FROM Genre g " +
                     "JOIN Recommendation_Genre rg ON g.genreId = rg.genreId " +
                     "WHERE rg.recId = ?";
        List<String> genres = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, recId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                genres.add(rs.getString("genreName"));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return String.join("|", genres);
    }

    /**
     * Helper to convert ResultSet to Recommendation object
     */
    private Recommendation fromResultSet(ResultSet rs) throws SQLException {
        int recId = rs.getInt("recId");
        String recSongName = rs.getString("recSongName");
        String recTrackLength = rs.getString("recTrackLength");
        String recMood = rs.getString("recMood");
        String spotifyUrl = rs.getString("spotifyUrl");

        String artists = getArtistsForRecommendation(recId);
        String genres = getGenresForRecommendation(recId);

        Recommendation rec = new Recommendation(recSongName, "", artists, recTrackLength, 
                                               genres, recMood, spotifyUrl);
        rec.setSongId(recId);
        return rec;
    }
}
