/**
 * Business logic for song library operations.
 * Provides a simplified interface between UI and SongDAO.
 * Handles CSV imports, search, and play count management.
 */
package service;

import dao.SongDAO;
import model.Recommendation;
import model.Song;
import util.Importer;

import java.util.List;

public class SongService {

    private final SongDAO dao = new SongDAO();
    private int userId = 1;

    /**
     * Creates a service for the default user (userId=1).
     */
    public SongService() {}

    /**
     * Creates a service for a specific user.
     * 
     * @param userId User whose library to manage
     */
    public SongService(int userId) {
        this.userId = userId;
    }

    /**
     * Retrieves all songs in the current user's library.
     * 
     * @return List of songs with complete metadata
     */
    public List<Song> getAllSongs() {
        return dao.getAllForUser(userId);
    }

    /**
     * Searches songs by track name (case-insensitive partial match).
     * 
     * @param query Search query
     * @return List of matching songs
     */
    public List<Song> search(String query) {
        return dao.search(query, userId);
    }

    /**
     * Updates the play count for a song in a specific user's library.
     * 
     * @param songId Song to update
     * @param userId User's library
     * @param count  New play count
     */
    public void setPlayCount(int songId, int userId, int count) {
        dao.updatePlayCount(songId, userId, count);
    }

    /**
     * Updates the play count for a song in the current user's library.
     * 
     * @param songId Song to update
     * @param count  New play count
     */
    public void setPlayCount(int songId, int count) {
        setPlayCount(songId, this.userId, count);
    }

    /**
     * Updates a song's metadata for a specific user.
     * 
     * @param song   Song with updated information
     * @param userId User's library
     */
    public void updateSong(Song song, int userId) {
        dao.updateSong(song, userId);
    }

    /**
     * Updates a song's metadata for the current user.
     * 
     * @param song Song with updated information
     */
    public void updateSong(Song song) {
        updateSong(song, this.userId);
    }

    /**
     * Deletes a song from the database completely.
     * 
     * @param songId Song to delete
     */
    public void deleteSong(int songId) {
        dao.delete(songId);
    }

    /**
     * Imports songs from a CSV file (Exportify or cleaned format).
     * Auto-detects format and calculates mood from audio features if available.
     * 
     * @param filePath Path to CSV file
     * @param userId   User whose library to import into
     * @return Number of songs imported
     */
    public int importFromCSV(String filePath, int userId) {
        List<Song> songs = Importer.importFromCSV(filePath);
        if (songs.isEmpty()) return 0;
        dao.insertAll(songs, userId);
        return songs.size();
    }

    /**
     * Adds a recommendation to the current user's library as a new song.
     * 
     * @param rec Recommendation to add to library
     */
    public void addFromRecommendation(Recommendation rec) {
        Song song = new Song(
            rec.getTrackName(),
            rec.getAlbumName(),
            rec.getArtists(),
            rec.getLength(),
            rec.getGenres(),
            rec.getMood(),
            rec.getLink()
        );
        dao.insert(song, userId);
    }
}