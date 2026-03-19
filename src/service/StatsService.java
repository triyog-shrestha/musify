// StatsService.java
// Calculates all stats from the song library.
// Multi-genre songs: each genre gets the full play count added to it.

package service;

import dao.SongDAO;
import model.Song;

import java.util.*;

public class StatsService {

    private final SongDAO dao = new SongDAO();

    // -------------------------------------------------------------------------
    // Top Artists — sorted by total play count
    // -------------------------------------------------------------------------
    public List<Map.Entry<String, Integer>> getTopArtists() {
        Map<String, Integer> counts = new HashMap<>();
        for (Song song : dao.getAll()) {
            if (song.getPlayCount() == 0) continue;
            for (String artist : song.getArtists().split("\\|")) {
                String a = artist.trim();
                if (!a.isEmpty()) {
                    counts.merge(a, song.getPlayCount(), Integer::sum);
                }
            }
        }
        return sortedDesc(counts);
    }

    // -------------------------------------------------------------------------
    // Top Songs — sorted by play count
    // -------------------------------------------------------------------------
    public List<Song> getTopSongs() {
        List<Song> songs = new ArrayList<>(dao.getAll());
        songs.sort((a, b) -> b.getPlayCount() - a.getPlayCount());
        return songs;
    }

    // -------------------------------------------------------------------------
    // Top Genres — each genre gets the FULL play count of the song
    // e.g. a song with genres "rap|r&b" and 5 plays adds 5 to rap AND 5 to r&b
    // -------------------------------------------------------------------------
    public List<Map.Entry<String, Integer>> getTopGenres() {
        Map<String, Integer> counts = new HashMap<>();
        for (Song song : dao.getAll()) {

            // skip songs with no plays — they shouldn't affect genre rankings
            if (song.getPlayCount() == 0) continue;

            String genres = song.getGenres().trim();
            if (genres.isEmpty()) continue;

            // split by pipe and count each genre separately
            for (String genre : genres.split("\\|")) {
                String g = genre.trim().toLowerCase(); // normalize case
                if (!g.isEmpty()) {
                    counts.merge(g, song.getPlayCount(), Integer::sum);
                }
            }
        }
        return sortedDesc(counts);
    }

    // -------------------------------------------------------------------------
    // Top Mood — mood with the most total plays
    // -------------------------------------------------------------------------
    public String getTopMood() {
        Map<String, Integer> counts = new HashMap<>();
        for (Song song : dao.getAll()) {
            if (song.getPlayCount() == 0) continue;
            String mood = song.getMood().trim();
            if (!mood.isEmpty()) {
                counts.merge(mood, song.getPlayCount(), Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("RELAXED");
    }

    // -------------------------------------------------------------------------
    // Total minutes listened and total plays
    // -------------------------------------------------------------------------
    public int getTotalPlays() {
        int total = 0;
        for (Song song : dao.getAll()) total += song.getPlayCount();
        return total;
    }

    public long getTotalMinutes() {
        long totalSeconds = 0;
        for (Song song : dao.getAll()) {
            if (song.getPlayCount() == 0) continue;
            totalSeconds += parseSeconds(song.getLength()) * song.getPlayCount();
        }
        return totalSeconds / 60;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    // Sort a map by value descending, return as list of entries
    private List<Map.Entry<String, Integer>> sortedDesc(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list;
    }

    // Parse "mm:ss" to total seconds
    private long parseSeconds(String length) {
        try {
            String[] parts = length.split(":");
            return Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}
