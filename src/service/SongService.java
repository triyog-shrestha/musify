// SongService.java
// Business logic for songs. Sits between Main and SongDAO.

package service;

import dao.SongDAO;
import model.Recommendation;
import model.Song;
import util.Importer;

import java.util.List;

public class SongService {

    private final SongDAO dao = new SongDAO();
    private int userId = 1;

    public SongService() {}

    public SongService(int userId) {
        this.userId = userId;
    }

    // Get all songs for current user
    public List<Song> getAllSongs() {
        return dao.getAllForUser(userId);
    }

    // Search songs by name
    public List<Song> search(String query) {
        return dao.search(query, userId);
    }

    // Set play count for a song
    public void setPlayCount(int songId, int userId, int count) {
        dao.updatePlayCount(songId, userId, count);
    }

    public void setPlayCount(int songId, int count) {
        setPlayCount(songId, this.userId, count);
    }

    public void updateSong(Song song, int userId) {
        dao.updateSong(song, userId);
    }

    public void updateSong(Song song) {
        updateSong(song, this.userId);
    }

    // Delete a song
    public void deleteSong(int songId) {
        dao.delete(songId);
    }

    // Import songs from CSV file to user's library
    public int importFromCSV(String filePath, int userId) {
        List<Song> songs = Importer.importFromCSV(filePath);
        if (songs.isEmpty()) return 0;
        dao.insertAll(songs, userId);
        return songs.size();
    }

    // Add a song from a recommendation to the user's library
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