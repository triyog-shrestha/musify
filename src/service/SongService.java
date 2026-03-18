// SongService.java
// Business logic for songs. Sits between Main and SongDAO.

package service;

import dao.SongDAO;
import model.Song;
import util.Importer;

import java.util.List;

public class SongService {

    private final SongDAO dao = new SongDAO();

    // Import songs from raw Exportify CSV
    public int importSongs(String filePath) {
        List<Song> songs = Importer.importFromCSV(filePath);
        dao.insertAll(songs);
        return songs.size();
    }

    // Get all songs
    public List<Song> getAllSongs() {
        return dao.getAll();
    }

    // Search songs by name
    public List<Song> search(String query) {
        return dao.search(query);
    }

    // Set play count for a song
    public void setPlayCount(int songId, int count) {
        dao.updatePlayCount(songId, count);
    }

    // Delete a song
    public void deleteSong(int songId) {
        dao.delete(songId);
    }
}