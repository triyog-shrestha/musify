/**
 * Data Access Object for song operations in the normalized database schema.
 * Manages songs, their relationships to artists/genres/albums, and user library linkage.
 * 
 * Database design uses normalization to avoid duplication:
 * - Song table: Core song data
 * - Artist/Genre/Album tables: Unique entities  
 * - Junction tables: Many-to-many relationships
 * - Library table: User-song linkage with play counts
 */
package dao;

import model.Song;
import util.Database;
import util.GenreUtil;

import java.sql.*;
import java.util.*;

public class SongDAO {

    /**
     * Inserts a song into the database for the default user (userId=1).
     * 
     * @param song Song object to insert
     */
    public void insert(Song song) {
        insert(song, 1);
    }

    /**
     * Inserts a song into the database and links it to a specific user's library.
     * 
     * Process:
     * 1. Check if song already exists by Spotify URL (avoid duplicates)
     * 2. If exists, just link to user's library
     * 3. If new, create album, song, and relationships
     * 4. Link to user's library with play count
     * 
     * All operations are wrapped in a transaction for atomicity.
     * 
     * @param song   Song object to insert
     * @param userId User who owns this song
     */
    public void insert(Song song, int userId) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Check if song already exists by Spotify URL
                int existingSongId = findSongBySpotifyUrl(conn, song.getLink());
                
                if (existingSongId > 0) {
                    // Song already exists, just link to user's library if not already linked
                    linkToLibraryIfNotExists(conn, userId, existingSongId, song.getPlayCount());
                    song.setSongId(existingSongId);
                    conn.commit();
                    return;
                }
                
                // 2. Ensure Album exists
                int albumId = ensureAlbum(conn, song.getAlbumName());
                
                // 3. Insert into Song table
                String songSql = "INSERT INTO Song(songName, tracklength, mood, spotifyUrl, albumId) VALUES(?,?,?,?,?)";
                int songId;
                try (PreparedStatement ps = conn.prepareStatement(songSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, song.getTrackName());
                    ps.setTime(2, toSqlTime(song.getLength()));
                    ps.setString(3, song.getMood());
                    ps.setString(4, song.getLink());
                    ps.setInt(5, albumId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            songId = keys.getInt(1);
                            song.setSongId(songId);
                        } else {
                            throw new SQLException("Failed to get song ID");
                        }
                    }
                }
                
                // 4. Link to Library (user's collection)
                linkToLibraryIfNotExists(conn, userId, songId, song.getPlayCount());
                
                // 5. Link artists
                for (String artistName : song.getArtists().split("\\|")) {
                    String name = artistName.trim();
                    if (!name.isEmpty()) {
                        int artistId = ensureArtist(conn, name);
                        linkSongArtist(conn, songId, artistId);
                    }
                }
                
                // 6. Link genres
                for (String genreName : GenreUtil.splitGenres(song.getGenres())) {
                    if (!genreName.isEmpty()) {
                        int genreId = ensureGenre(conn, genreName);
                        linkSongGenre(conn, songId, genreId);
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
            throw new RuntimeException("Could not insert song.", e);
        }
    }

    /**
     * Inserts multiple songs into the database for the default user.
     * 
     * @param songs List of songs to insert
     */
    public void insertAll(List<Song> songs) {
        insertAll(songs, 1);
    }

    /**
     * Inserts multiple songs into the database for a specific user.
     * 
     * @param songs  List of songs to insert
     * @param userId User who owns these songs
     */
    public void insertAll(List<Song> songs, int userId) {
        if (songs == null || songs.isEmpty()) return;
        for (Song song : songs) {
            insert(song, userId);
        }
    }

    /**
     * Retrieves all songs for the default user with joined artist/album/genre data.
     * 
     * @return List of songs with all metadata
     */
    public List<Song> getAll() {
        return getAllForUser(1);
    }

    /**
     * Retrieves all songs for a specific user with joined metadata.
     * Uses GROUP_CONCAT to aggregate multiple artists and genres.
     * 
     * @param userId User whose songs to retrieve
     * @return List of songs from user's library
     */
    public List<Song> getAllForUser(int userId) {
        List<Song> songs = new ArrayList<>();
        String sql = """
            SELECT s.songId,
                   s.songName,
                   COALESCE(al.albumName, '') AS albumName,
                   COALESCE(GROUP_CONCAT(DISTINCT a.artistName SEPARATOR '|'), '') AS artists,
                   s.tracklength,
                   COALESCE(GROUP_CONCAT(DISTINCT g.genreName SEPARATOR '|'), '') AS genres,
                   COALESCE(s.mood, 'RELAXED') AS mood,
                   COALESCE(s.spotifyUrl, '') AS link,
                   COALESCE(l.playCount, 0) AS playCount
            FROM Library l
            JOIN Song s ON s.songId = l.songId
            LEFT JOIN Album al ON al.albumId = s.albumId
            LEFT JOIN Song_Artist sa ON sa.songId = s.songId
            LEFT JOIN Artist a ON a.artistId = sa.artistId
            LEFT JOIN Song_Genre sg ON sg.songId = s.songId
            LEFT JOIN Genre g ON g.genreId = sg.genreId
            WHERE l.userId = ?
            GROUP BY s.songId, s.songName, al.albumName, s.tracklength, s.mood, s.spotifyUrl, l.playCount
            ORDER BY s.songId
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) songs.add(fromRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch songs.", e);
        }
        return songs;
    }

    /**
     * Searches songs by track name (case-insensitive, partial match) for default user.
     * 
     * @param query Search query
     * @return List of matching songs
     */
    public List<Song> search(String query) {
        return search(query, 1);
    }

    /**
     * Searches songs by track name for a specific user.
     * 
     * @param query  Search query (partial match, case-insensitive)
     * @param userId User whose library to search
     * @return List of matching songs
     */
    public List<Song> search(String query, int userId) {
        List<Song> results = new ArrayList<>();
        String sql = """
            SELECT s.songId,
                   s.songName,
                   COALESCE(al.albumName, '') AS albumName,
                   COALESCE(GROUP_CONCAT(DISTINCT a.artistName SEPARATOR '|'), '') AS artists,
                   s.tracklength,
                   COALESCE(GROUP_CONCAT(DISTINCT g.genreName SEPARATOR '|'), '') AS genres,
                   COALESCE(s.mood, 'RELAXED') AS mood,
                   COALESCE(s.spotifyUrl, '') AS link,
                   COALESCE(l.playCount, 0) AS playCount
            FROM Library l
            JOIN Song s ON s.songId = l.songId
            LEFT JOIN Album al ON al.albumId = s.albumId
            LEFT JOIN Song_Artist sa ON sa.songId = s.songId
            LEFT JOIN Artist a ON a.artistId = sa.artistId
            LEFT JOIN Song_Genre sg ON sg.songId = s.songId
            LEFT JOIN Genre g ON g.genreId = sg.genreId
            WHERE l.userId = ? AND LOWER(s.songName) LIKE ?
            GROUP BY s.songId, s.songName, al.albumName, s.tracklength, s.mood, s.spotifyUrl, l.playCount
            ORDER BY s.songName
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, "%" + query.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(fromRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not search songs.", e);
        }
        return results;
    }

    /**
     * Updates the play count for a song in the default user's library.
     * 
     * @param songId   Song to update
     * @param newCount New play count value
     */
    public void updatePlayCount(int songId, int newCount) {
        updatePlayCount(songId, 1, newCount);
    }

    /**
     * Updates the play count for a song in a specific user's library.
     * If the song isn't in their library yet, adds it with the given play count.
     * 
     * @param songId   Song to update
     * @param userId   User whose library to update
     * @param newCount New play count value
     */
    public void updatePlayCount(int songId, int userId, int newCount) {
        String sql = "UPDATE Library SET playCount=? WHERE songId=? AND userId=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newCount);
            ps.setInt(2, songId);
            ps.setInt(3, userId);
            int rows = ps.executeUpdate();
            
            // If no rows updated, insert new library entry
            if (rows == 0) {
                String insertSql = "INSERT INTO Library(userId, songId, playCount) VALUES(?,?,?)";
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    ins.setInt(1, userId);
                    ins.setInt(2, songId);
                    ins.setInt(3, newCount);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update play count.", e);
        }
    }

    /**
     * Updates a song's metadata for the default user.
     * 
     * @param song Song with updated information
     */
    public void updateSong(Song song) {
        updateSong(song, 1);
    }

    /**
     * Updates a song's complete metadata including artists and genres.
     * Rebuilds all relationships in a transaction to maintain consistency.
     * 
     * @param song   Song with updated information
     * @param userId User whose library contains this song
     */
    public void updateSong(Song song, int userId) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update Album
                int albumId = ensureAlbum(conn, song.getAlbumName());
                
                // Update Song table
                String sql = "UPDATE Song SET songName=?, tracklength=?, mood=?, spotifyUrl=?, albumId=? WHERE songId=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, song.getTrackName());
                    ps.setTime(2, toSqlTime(song.getLength()));
                    ps.setString(3, song.getMood());
                    ps.setString(4, song.getLink());
                    ps.setInt(5, albumId);
                    ps.setInt(6, song.getSongId());
                    ps.executeUpdate();
                }
                
                // Update play count in Library
                String libSql = "UPDATE Library SET playCount=? WHERE songId=? AND userId=?";
                try (PreparedStatement ps = conn.prepareStatement(libSql)) {
                    ps.setInt(1, song.getPlayCount());
                    ps.setInt(2, song.getSongId());
                    ps.setInt(3, userId);
                    ps.executeUpdate();
                }
                
                // Rebuild artist links
                rebuildSongArtists(conn, song.getSongId(), song.getArtists());
                
                // Rebuild genre links
                rebuildSongGenres(conn, song.getSongId(), song.getGenres());
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update song.", e);
        }
    }

    /**
     * Deletes a song from the database completely.
     * Cascade delete will remove all associated relationships.
     * 
     * @param songId Song to delete
     */
    public void delete(int songId) {
        // Cascade delete handles junction tables
        String sql = "DELETE FROM Song WHERE songId=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, songId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete song.", e);
        }
    }

    /**
     * Ensures an album exists in the database, creating it if needed.
     * Returns the albumId either way.
     */
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
    
    /**
     * Ensures an artist exists in the database, creating it if needed.
     * Returns the artistId either way.
     */
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
    
    /**
     * Ensures a genre exists in the database, creating it if needed.
     * Returns the genreId either way.
     */
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
    
    /**
     * Creates a link between a song and an artist in the Song_Artist junction table.
     * Uses INSERT IGNORE to prevent duplicate entries.
     */
    private void linkSongArtist(Connection conn, int songId, int artistId) throws SQLException {
        String sql = "INSERT IGNORE INTO Song_Artist(songId, artistId) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, songId);
            ps.setInt(2, artistId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Creates a link between a song and a genre in the Song_Genre junction table.
     * Uses INSERT IGNORE to prevent duplicate entries.
     */
    private void linkSongGenre(Connection conn, int songId, int genreId) throws SQLException {
        String sql = "INSERT IGNORE INTO Song_Genre(songId, genreId) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, songId);
            ps.setInt(2, genreId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Finds a song by its Spotify URL to detect duplicates.
     * 
     * @return songId if found, -1 otherwise
     */
    private int findSongBySpotifyUrl(Connection conn, String spotifyUrl) throws SQLException {
        if (spotifyUrl == null || spotifyUrl.isBlank()) return -1;
        
        String sql = "SELECT songId FROM Song WHERE spotifyUrl = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, spotifyUrl);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }
    
    /**
     * Links a song to a user's library if not already linked.
     * Avoids duplicate library entries.
     */
    private void linkToLibraryIfNotExists(Connection conn, int userId, int songId, int playCount) throws SQLException {
        String checkSql = "SELECT 1 FROM Library WHERE userId = ? AND songId = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, songId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Already linked, do nothing
                    return;
                }
            }
        }
        
        String insertSql = "INSERT INTO Library(userId, songId, playCount) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, songId);
            ps.setInt(3, playCount);
            ps.executeUpdate();
        }
    }
    
    /**
     * Removes all artist links for a song and recreates them.
     * Used when updating a song's artist list.
     */
    private void rebuildSongArtists(Connection conn, int songId, String artists) throws SQLException {
        // Remove old links
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Song_Artist WHERE songId=?")) {
            ps.setInt(1, songId);
            ps.executeUpdate();
        }
        // Add new links
        for (String artistName : artists.split("\\|")) {
            String name = artistName.trim();
            if (!name.isEmpty()) {
                int artistId = ensureArtist(conn, name);
                linkSongArtist(conn, songId, artistId);
            }
        }
    }
    
    /**
     * Removes all genre links for a song and recreates them.
     * Used when updating a song's genre list.
     */
    private void rebuildSongGenres(Connection conn, int songId, String genres) throws SQLException {
        // Remove old links
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Song_Genre WHERE songId=?")) {
            ps.setInt(1, songId);
            ps.executeUpdate();
        }
        // Add new links
        for (String genreName : GenreUtil.splitGenres(genres)) {
            if (!genreName.isEmpty()) {
                int genreId = ensureGenre(conn, genreName);
                linkSongGenre(conn, songId, genreId);
            }
        }
    }
    
    /**
     * Converts mm:ss format to SQL TIME (HH:mm:ss).
     */
    private Time toSqlTime(String mmss) {
        if (mmss == null || mmss.isBlank()) return Time.valueOf("00:00:00");
        String[] parts = mmss.split(":");
        if (parts.length == 2) {
            return Time.valueOf("00:" + parts[0] + ":" + parts[1]);
        }
        return Time.valueOf("00:00:00");
    }
    
    /**
     * Converts SQL TIME format (HH:mm:ss) back to mm:ss display format.
     */
    private String timeToMmSs(String sqlTime) {
        if (sqlTime == null || sqlTime.length() < 5) return "0:00";
        // Handle both "HH:mm:ss" and "mm:ss" formats
        if (sqlTime.length() >= 8) {
            return sqlTime.substring(3, 8); // "00:03:45" -> "03:45"
        }
        return sqlTime;
    }

    /**
     * Converts a database result row to a Song object with all metadata.
     */
    private Song fromRow(ResultSet rs) throws SQLException {
        String tracklength = rs.getString("tracklength");
        String lengthFormatted = timeToMmSs(tracklength);
        String artists = rs.getString("artists");
        int totalArtists = (artists == null || artists.isEmpty()) ? 0 : artists.split("\\|").length;
        
        return new Song(
                rs.getInt("songId"),
                rs.getString("songName"),
                rs.getString("albumName"),
                artists != null ? artists : "",
                totalArtists,
                lengthFormatted,
                rs.getString("genres") != null ? rs.getString("genres") : "",
                rs.getString("mood"),
                rs.getString("link"),
                rs.getInt("playCount")
        );
    }
}