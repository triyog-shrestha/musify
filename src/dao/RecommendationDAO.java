// RecommendationDAO.java
// All read/write operations for recommendations using normalized tables.

package dao;

import model.Recommendation;
import util.Database;
import util.GenreUtil;

import java.sql.*;
import java.util.*;

public class RecommendationDAO {

    public int insertBatch(List<Recommendation> recs) {
        if (recs == null || recs.isEmpty()) return 0;

        int inserted = 0;
        Set<String> existingLinks = new HashSet<>();
        for (Recommendation existing : getAll()) {
            String key = existing.getLink().trim().toLowerCase();
            if (!key.isEmpty()) existingLinks.add(key);
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Recommendation rec : recs) {
                    String key = rec.getLink().trim().toLowerCase();
                    if (key.isEmpty() || existingLinks.contains(key)) continue;

                    // Ensure Album exists
                    int albumId = ensureAlbum(conn, rec.getAlbumName());

                    // Insert into Recommendation table
                    String sql = "INSERT INTO Recommendation(songName, tracklength, mood, spotifyUrl, albumId) VALUES(?,?,?,?,?)";
                    int recId;
                    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, rec.getTrackName());
                        ps.setTime(2, toSqlTime(rec.getLength()));
                        ps.setString(3, rec.getMood());
                        ps.setString(4, rec.getLink());
                        ps.setInt(5, albumId);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                recId = keys.getInt(1);
                            } else {
                                continue;
                            }
                        }
                    }

                    // Link artists
                    for (String artistName : rec.getArtists().split("\\|")) {
                        String name = artistName.trim();
                        if (!name.isEmpty()) {
                            int artistId = ensureArtist(conn, name);
                            linkRecArtist(conn, recId, artistId);
                        }
                    }

                    // Link genres
                    for (String genreName : GenreUtil.splitGenres(rec.getGenres())) {
                        if (!genreName.isEmpty()) {
                            int genreId = ensureGenre(conn, genreName);
                            linkRecGenre(conn, recId, genreId);
                        }
                    }

                    existingLinks.add(key);
                    inserted++;
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not insert recommendations.", e);
        }
        return inserted;
    }

    // Get all recommendations with joined data
    public List<Recommendation> getAll() {
        List<Recommendation> recs = new ArrayList<>();
        String sql = """
            SELECT r.recId,
                   r.songName,
                   COALESCE(al.albumName, '') AS albumName,
                   COALESCE(GROUP_CONCAT(DISTINCT a.artistName SEPARATOR '|'), '') AS artists,
                   r.tracklength,
                   COALESCE(GROUP_CONCAT(DISTINCT g.genreName SEPARATOR '|'), '') AS genres,
                   COALESCE(r.mood, 'RELAXED') AS mood,
                   COALESCE(r.spotifyUrl, '') AS link
            FROM Recommendation r
            LEFT JOIN Album al ON al.albumId = r.albumId
            LEFT JOIN Recommendation_Artist ra ON ra.recId = r.recId
            LEFT JOIN Artist a ON a.artistId = ra.artistId
            LEFT JOIN Recommendation_Genre rg ON rg.recId = r.recId
            LEFT JOIN Genre g ON g.genreId = rg.genreId
            GROUP BY r.recId, r.songName, al.albumName, r.tracklength, r.mood, r.spotifyUrl
            ORDER BY r.recId
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) recs.add(fromRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch recommendations.", e);
        }
        return recs;
    }

    // Get all unique genres from the database
    public List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT genreName FROM Genre ORDER BY genreName";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                genres.add(rs.getString("genreName"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch genres.", e);
        }
        return genres;
    }

    // Filter by genre (case-insensitive)
    public List<Recommendation> filterByGenre(String genre) {
        List<Recommendation> results = new ArrayList<>();
        String requested = genre == null ? "" : genre.trim().toLowerCase();
        if (requested.isEmpty()) return results;

        for (Recommendation rec : getAll()) {
            if (GenreUtil.splitGenres(rec.getGenres()).contains(requested)) {
                results.add(rec);
            }
        }
        return results;
    }

    // Filter by mood
    public List<Recommendation> filterByMood(String mood) {
        List<Recommendation> results = new ArrayList<>();
        for (Recommendation rec : getAll()) {
            if (rec.getMood().equalsIgnoreCase(mood)) {
                results.add(rec);
            }
        }
        return results;
    }

    public void delete(int recId) {
        // Cascade delete handles junction tables
        String sql = "DELETE FROM Recommendation WHERE recId=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete recommendation.", e);
        }
    }

    public void update(Recommendation rec) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update album
                int albumId = ensureAlbum(conn, rec.getAlbumName());

                // Update recommendation
                String sql = "UPDATE Recommendation SET songName=?, tracklength=?, mood=?, spotifyUrl=?, albumId=? WHERE recId=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, rec.getTrackName());
                    ps.setTime(2, toSqlTime(rec.getLength()));
                    ps.setString(3, rec.getMood());
                    ps.setString(4, rec.getLink());
                    ps.setInt(5, albumId);
                    ps.setInt(6, rec.getRecId());
                    ps.executeUpdate();
                }

                // Clear old artist/genre links
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Recommendation_Artist WHERE recId=?")) {
                    ps.setInt(1, rec.getRecId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Recommendation_Genre WHERE recId=?")) {
                    ps.setInt(1, rec.getRecId());
                    ps.executeUpdate();
                }

                // Re-link artists
                for (String artistName : rec.getArtists().split("\\|")) {
                    String name = artistName.trim();
                    if (!name.isEmpty()) {
                        int artistId = ensureArtist(conn, name);
                        linkRecArtist(conn, rec.getRecId(), artistId);
                    }
                }

                // Re-link genres
                for (String genreName : GenreUtil.splitGenres(rec.getGenres())) {
                    if (!genreName.isEmpty()) {
                        int genreId = ensureGenre(conn, genreName);
                        linkRecGenre(conn, rec.getRecId(), genreId);
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update recommendation.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private int ensureAlbum(Connection conn, String albumName) throws SQLException {
        if (albumName == null || albumName.isBlank()) albumName = "Unknown Album";

        String selectSql = "SELECT albumId FROM Album WHERE albumName=?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, albumName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        String insertSql = "INSERT INTO Album(albumName) VALUES(?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, albumName);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Could not create album: " + albumName);
    }

    private int ensureArtist(Connection conn, String artistName) throws SQLException {
        if (artistName == null || artistName.isBlank()) artistName = "Unknown Artist";

        String selectSql = "SELECT artistId FROM Artist WHERE artistName=?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        String insertSql = "INSERT INTO Artist(artistName) VALUES(?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, artistName);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Could not create artist: " + artistName);
    }

    private int ensureGenre(Connection conn, String genreName) throws SQLException {
        if (genreName == null || genreName.isBlank()) genreName = "Unknown";

        String selectSql = "SELECT genreId FROM Genre WHERE genreName=?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, genreName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        String insertSql = "INSERT INTO Genre(genreName) VALUES(?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, genreName);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Could not create genre: " + genreName);
    }

    private void linkRecArtist(Connection conn, int recId, int artistId) throws SQLException {
        String sql = "INSERT IGNORE INTO Recommendation_Artist(recId, artistId) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recId);
            ps.setInt(2, artistId);
            ps.executeUpdate();
        }
    }

    private void linkRecGenre(Connection conn, int recId, int genreId) throws SQLException {
        String sql = "INSERT IGNORE INTO Recommendation_Genre(recId, genreId) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recId);
            ps.setInt(2, genreId);
            ps.executeUpdate();
        }
    }

    private Time toSqlTime(String mmss) {
        if (mmss == null || mmss.isBlank()) return Time.valueOf("00:00:00");
        String[] parts = mmss.split(":");
        if (parts.length == 2) {
            return Time.valueOf("00:" + parts[0] + ":" + parts[1]);
        }
        return Time.valueOf("00:00:00");
    }

    private String timeToMmSs(String sqlTime) {
        if (sqlTime == null || sqlTime.length() < 5) return "0:00";
        if (sqlTime.length() >= 8) {
            return sqlTime.substring(3, 8);
        }
        return sqlTime;
    }

    // SQL row → Recommendation object
    private Recommendation fromRow(ResultSet rs) throws SQLException {
        String tracklength = rs.getString("tracklength");
        return new Recommendation(
                rs.getInt("recId"),
                rs.getString("songName"),
                rs.getString("albumName"),
                rs.getString("artists") != null ? rs.getString("artists") : "",
                timeToMmSs(tracklength),
                rs.getString("genres") != null ? rs.getString("genres") : "",
                rs.getString("mood"),
                rs.getString("link")
        );
    }
}