// MySQLSongDAO.java
// Database operations for Song table using JDBC
// Supports songs with artists and genres in many-to-many relationships

package dao;

import db.DatabaseConnection;
import model.Song;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLSongDAO extends BaseDAO {

    /**
     * Insert a new song into the database
     * @param song Song object to insert
     */
    public void insert(Song song) throws SQLException {
        String sql = "INSERT INTO Song (trackName, length, mood, spotifyUrl, albumId) VALUES (?, ?, ?, ?, ?)";
        int generatedId = executeInsertWithId(sql,
                song.getTrackName(),
                song.getLength(),
                song.getMood(),
                song.getLink(),
                null  // albumId
        );
        song.setSongId(generatedId);
    }

    /**
     * Insert multiple songs
     * @param songs List of songs to insert
     */
    public void insertAll(List<Song> songs) throws SQLException {
        for (Song song : songs) {
            insert(song);
        }
    }

    /**
     * Get all songs from the database
     * @return List of all songs
     */
    public List<Song> getAll() throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT songId, trackName, length, mood, spotifyUrl, albumId FROM Song";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                songs.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return songs;
    }

    /**
     * Search songs by track name
     * @param query Search query
     * @return List of matching songs
     */
    public List<Song> search(String query) throws SQLException {
        List<Song> results = new ArrayList<>();
        String sql = "SELECT songId, trackName, length, mood, spotifyUrl, albumId FROM Song WHERE trackName LIKE ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + query + "%");
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
     * Get a song by ID
     * @param songId Song ID to retrieve
     * @return Song if found, null otherwise
     */
    public Song getSongById(int songId) throws SQLException {
        String sql = "SELECT songId, trackName, length, mood, spotifyUrl, albumId FROM Song WHERE songId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, songId);
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
     * Delete a song by ID
     * @param songId Song ID to delete
     */
    public void delete(int songId) throws SQLException {
        String sql = "DELETE FROM Song WHERE songId = ?";
        executeUpdate(sql, songId);
    }

    /**
     * Get songs by artist ID
     * @param artistId Artist ID to search for
     * @return List of songs by the artist
     */
    public List<Song> getSongsByArtist(int artistId) throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.songId, s.trackName, s.length, s.mood, s.spotifyUrl, s.albumId " +
                     "FROM Song s JOIN Song_Artist sa ON s.songId = sa.songId " +
                     "WHERE sa.artistId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, artistId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                songs.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return songs;
    }

    /**
     * Get songs by genre ID
     * @param genreId Genre ID to search for
     * @return List of songs in the genre
     */
    public List<Song> getSongsByGenre(int genreId) throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.songId, s.trackName, s.length, s.mood, s.spotifyUrl, s.albumId " +
                     "FROM Song s JOIN Song_Genre sg ON s.songId = sg.songId " +
                     "WHERE sg.genreId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, genreId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                songs.add(fromResultSet(rs));
            }
        } finally {
            DatabaseConnection.closeResultSet(rs);
            DatabaseConnection.closePreparedStatement(stmt);
            DatabaseConnection.closeConnection(conn);
        }
        return songs;
    }

    /**
     * Get artists for a song
     * @param songId Song ID
     * @return Pipe-separated artist names
     */
    public String getArtistsForSong(int songId) throws SQLException {
        String sql = "SELECT a.artistName FROM Artist a " +
                     "JOIN Song_Artist sa ON a.artistId = sa.artistId " +
                     "WHERE sa.songId = ?";
        List<String> artists = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, songId);
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
     * Get genres for a song
     * @param songId Song ID
     * @return Pipe-separated genre names
     */
    public String getGenresForSong(int songId) throws SQLException {
        String sql = "SELECT g.genreName FROM Genre g " +
                     "JOIN Song_Genre sg ON g.genreId = sg.genreId " +
                     "WHERE sg.songId = ?";
        List<String> genres = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, songId);
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
     * Update play count for a song
     * @param songId Song ID
     * @param userId User ID
     * @param playCount New play count
     */
    public void updatePlayCount(int songId, int userId, int playCount) throws SQLException {
        String sql = "INSERT INTO Library (userId, songId, playCount) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE playCount = ?";
        executeUpdate(sql, userId, songId, playCount, playCount);
    }

    /**
     * Get play count for a song by a user
     * @param songId Song ID
     * @param userId User ID
     * @return Play count
     */
    public int getPlayCount(int songId, int userId) throws SQLException {
        String sql = "SELECT playCount FROM Library WHERE songId = ? AND userId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, songId);
            stmt.setInt(2, userId);
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
     * Helper method to convert ResultSet row to Song object
     * @param rs ResultSet positioned at the row to convert
     * @return Song object
     */
    private Song fromResultSet(ResultSet rs) throws SQLException {
        int songId = rs.getInt("songId");
        String trackName = rs.getString("trackName");
        String length = rs.getString("length");
        String mood = rs.getString("mood");
        String spotifyUrl = rs.getString("spotifyUrl");

        Song song = new Song(trackName, "", "", length, "", mood, spotifyUrl);
        song.setSongId(songId);
        
        // Get artists and genres
        try {
            song = new Song(songId, trackName, "", getArtistsForSong(songId),
                           length, getGenresForSong(songId), mood, spotifyUrl, 0);
        } catch (SQLException e) {
            // Fall back to basic song if relationships can't be loaded
        }
        
        return song;
    }
}
