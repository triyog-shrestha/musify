// SongDAO.java
// All read/write operations for songs.csv.
// This is the only class that directly accesses songs.csv.

package dao;

import model.Song;
import util.Store;

import java.util.*;

public class SongDAO {

    // Save a new song — assigns the generated ID back to the object
    public void insert(Song song) {
        int id = Store.nextId(Store.SONGS_FILE);
        song.setSongId(id);
        Store.append(Store.SONGS_FILE, toRow(song));
    }

    // Save a list of songs
    public void insertAll(List<Song> songs) {
        for (Song song : songs) insert(song);
    }

    // Get every song in the library
    public List<Song> getAll() {
        List<Song> songs = new ArrayList<>();
        for (String[] row : Store.readAll(Store.SONGS_FILE)) {
            if (row.length >= 9) songs.add(fromRow(row));
        }
        return songs;
    }

    // Search songs by track name (case-insensitive)
    public List<Song> search(String query) {
        List<Song> results = new ArrayList<>();
        for (Song song : getAll()) {
            if (song.getTrackName().toLowerCase().contains(query.toLowerCase())) {
                results.add(song);
            }
        }
        return results;
    }

    // Update play count for one song by ID
    public void updatePlayCount(int songId, int newCount) {
        List<String[]> all = Store.readAll(Store.SONGS_FILE);
        List<String> updated = new ArrayList<>();
        for (String[] row : all) {
            if (Store.parseInt(row[0]) == songId) {
                Song s = fromRow(row);
                s.setPlayCount(newCount);
                updated.add(toRow(s));
            } else {
                updated.add(toRow(fromRow(row)));
            }
        }
        Store.overwrite(Store.SONGS_FILE, updated);
    }

    // Delete a song by ID
    public void delete(int songId) {
        List<String[]> all = Store.readAll(Store.SONGS_FILE);
        List<String> kept = new ArrayList<>();
        for (String[] row : all) {
            if (Store.parseInt(row[0]) != songId) {
                kept.add(toRow(fromRow(row)));
            }
        }
        Store.overwrite(Store.SONGS_FILE, kept);
    }

    // -------------------------------------------------------------------------
    // CSV row helpers
    // -------------------------------------------------------------------------

    // Song object → one CSV row string
    private String toRow(Song s) {
        return s.getSongId() + "," +
                Store.safe(s.getTrackName()) + "," +
                Store.safe(s.getAlbumName()) + "," +
                Store.safe(s.getArtists()) + "," +
                s.getLength() + "," +
                Store.safe(s.getGenres()) + "," +
                s.getMood() + "," +
                Store.safe(s.getLink()) + "," +
                s.getPlayCount();
    }

    // CSV row → Song object
    private Song fromRow(String[] r) {
        return new Song(
                Store.parseInt(r[0]),  // songId
                r[1],                  // trackName
                r[2],                  // albumName
                r[3],                  // artists
                r[4],                  // length
                r[5],                  // genres
                r[6],                  // mood
                r[7],                  // link
                Store.parseInt(r[8])   // playCount
        );
    }
}