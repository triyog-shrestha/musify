// StatsService.java
// Calculates all stats from the song library using normalized tables.
// Multi-genre songs: each genre gets the full play count added to it.

package service;

import dao.SongDAO;
import model.Song;
import util.Database;
import util.GenreUtil;

import java.sql.*;
import java.util.*;

public class StatsService {

    private final SongDAO dao = new SongDAO();
    private final int userId;

    public StatsService() {
        this.userId = -1;
    }

    public StatsService(int userId) {
        this.userId = userId;
    }

    // -------------------------------------------------------------------------
    // Top Artists — sorted by total play count
    // -------------------------------------------------------------------------
    public List<Map.Entry<String, Integer>> getTopArtists() {
        if (userId > 0) {
            String sql = """
                    SELECT a.artistName, COALESCE(SUM(l.playCount), 0) AS total
                    FROM Library l
                    JOIN Song_Artist sa ON sa.songId = l.songId
                    JOIN Artist a ON a.artistId = sa.artistId
                    WHERE l.userId = ?
                    GROUP BY a.artistId, a.artistName
                    ORDER BY total DESC
                    """;
            List<Map.Entry<String, Integer>> rows = queryRankMap(sql);
            if (!rows.isEmpty()) return rows;
        }

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
    // Top Albums — sorted by total play count
    // -------------------------------------------------------------------------
    public List<Map.Entry<String, Integer>> getTopAlbums() {
        if (userId > 0) {
            String sql = """
                    SELECT al.albumName, COALESCE(SUM(l.playCount), 0) AS total
                    FROM Library l
                    JOIN Song s ON s.songId = l.songId
                    JOIN Album al ON al.albumId = s.albumId
                    WHERE l.userId = ?
                    GROUP BY al.albumId, al.albumName
                    ORDER BY total DESC
                    """;
            List<Map.Entry<String, Integer>> rows = queryRankMap(sql);
            if (!rows.isEmpty()) return rows;
        }

        Map<String, Integer> counts = new HashMap<>();
        for (Song song : dao.getAll()) {
            if (song.getPlayCount() == 0) continue;
            String album = song.getAlbumName().trim();
            if (!album.isEmpty()) {
                counts.merge(album, song.getPlayCount(), Integer::sum);
            }
        }
        return sortedDesc(counts);
    }

    // -------------------------------------------------------------------------
    // Top Songs — sorted by play count
    // -------------------------------------------------------------------------
    public List<Song> getTopSongs() {
        if (userId > 0) {
            String sql = """
                    SELECT s.songId,
                           s.songName,
                           COALESCE(al.albumName, '') AS albumName,
                           COALESCE(GROUP_CONCAT(DISTINCT a.artistName SEPARATOR '|'), '') AS artists,
                           s.tracklength,
                           COALESCE(GROUP_CONCAT(DISTINCT g.genreName SEPARATOR '|'), '') AS genres,
                           COALESCE(s.mood, 'RELAXED') AS mood,
                           COALESCE(s.spotifyUrl, '') AS link,
                           l.playCount
                    FROM Library l
                    JOIN Song s ON s.songId = l.songId
                    LEFT JOIN Album al ON al.albumId = s.albumId
                    LEFT JOIN Song_Artist sa ON sa.songId = s.songId
                    LEFT JOIN Artist a ON a.artistId = sa.artistId
                    LEFT JOIN Song_Genre sg ON sg.songId = s.songId
                    LEFT JOIN Genre g ON g.genreId = sg.genreId
                    WHERE l.userId = ?
                    GROUP BY s.songId, s.songName, al.albumName, s.tracklength, s.mood, s.spotifyUrl, l.playCount
                    ORDER BY l.playCount DESC
                    """;

            List<Song> songs = new ArrayList<>();
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String artists = rs.getString("artists");
                        int totalArtists = (artists == null || artists.isEmpty()) ? 0 : artists.split("\\|").length;
                        songs.add(new Song(
                                rs.getInt("songId"),
                                rs.getString("songName"),
                                rs.getString("albumName"),
                                artists != null ? artists : "",
                                totalArtists,
                                timeToMmSs(rs.getString("tracklength")),
                                rs.getString("genres") != null ? rs.getString("genres") : "",
                                rs.getString("mood"),
                                rs.getString("link"),
                                rs.getInt("playCount")
                        ));
                    }
                }
            } catch (SQLException ignored) {
                // fallback below
            }
            if (!songs.isEmpty()) return songs;
        }

        List<Song> songs = new ArrayList<>(dao.getAll());
        songs.sort((a, b) -> b.getPlayCount() - a.getPlayCount());
        return songs;
    }

    // -------------------------------------------------------------------------
    // Top Genres — each genre gets the FULL play count of the song
    // e.g. a song with genres "rap|r&b" and 5 plays adds 5 to rap AND 5 to r&b
    // -------------------------------------------------------------------------
    public List<Map.Entry<String, Integer>> getTopGenres() {
        if (userId > 0) {
            String sql = """
                    SELECT g.genreName, COALESCE(SUM(l.playCount), 0) AS total
                    FROM Library l
                    JOIN Song_Genre sg ON sg.songId = l.songId
                    JOIN Genre g ON g.genreId = sg.genreId
                    WHERE l.userId = ?
                    GROUP BY g.genreId, g.genreName
                    ORDER BY total DESC
                    """;
            List<Map.Entry<String, Integer>> rows = queryRankMap(sql);
            if (!rows.isEmpty()) return rows;
        }

        Map<String, Integer> counts = new HashMap<>();
        for (Song song : dao.getAll()) {
            if (song.getPlayCount() == 0) continue;

            String genres = song.getGenres().trim();
            if (genres.isEmpty()) continue;

            for (String g : GenreUtil.splitGenres(genres)) {
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
        if (userId > 0) {
            String sql = """
                    SELECT s.mood, COALESCE(SUM(l.playCount), 0) AS total
                    FROM Library l
                    JOIN Song s ON s.songId = l.songId
                    WHERE l.userId = ?
                    GROUP BY s.mood
                    ORDER BY total DESC
                    LIMIT 1
                    """;
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String mood = rs.getString(1);
                        if (mood != null && !mood.isBlank()) return mood;
                    }
                }
            } catch (SQLException ignored) {
                // fallback below
            }
        }

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
        if (userId > 0) {
            String sql = "SELECT COALESCE(SUM(playCount),0) FROM Library WHERE userId=?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException ignored) {
                // fallback below
            }
        }

        int total = 0;
        for (Song song : dao.getAll()) total += song.getPlayCount();
        return total;
    }

    public long getTotalMinutes() {
        if (userId > 0) {
            String sql = """
                    SELECT COALESCE(SUM(TIME_TO_SEC(s.tracklength) * l.playCount),0)
                    FROM Library l
                    JOIN Song s ON s.songId = l.songId
                    WHERE l.userId=?
                    """;
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong(1) / 60;
                }
            } catch (SQLException ignored) {
                // fallback below
            }
        }

        long totalSeconds = 0;
        for (Song song : dao.getAll()) {
            if (song.getPlayCount() == 0) continue;
            totalSeconds += parseSeconds(song.getLength()) * song.getPlayCount();
        }
        return totalSeconds / 60;
    }

    public double getAverageMoodScore() {
        if (userId > 0) {
            String sql = """
                    SELECT COALESCE(SUM(
                        CASE UPPER(COALESCE(s.mood, 'RELAXED'))
                            WHEN 'ENERGETIC' THEN 1.0
                            WHEN 'HAPPY' THEN 0.8
                            WHEN 'FOCUSED' THEN 0.6
                            WHEN 'RELAXED' THEN 0.4
                            WHEN 'MELANCHOLIC' THEN 0.2
                            ELSE 0.5
                        END * l.playCount
                    ) / NULLIF(SUM(l.playCount), 0), 0)
                    FROM Library l
                    JOIN Song s ON s.songId = l.songId
                    WHERE l.userId=?
                    """;
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble(1);
                }
            } catch (SQLException ignored) {
                // fallback below
            }
        }

        double weighted = 0;
        int totalPlays = 0;

        for (Song song : dao.getAll()) {
            int plays = song.getPlayCount();
            if (plays <= 0) continue;
            weighted += moodScore(song.getMood()) * plays;
            totalPlays += plays;
        }

        if (totalPlays == 0) return 0.0;
        return weighted / totalPlays;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<Map.Entry<String, Integer>> sortedDesc(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list;
    }

    private long parseSeconds(String length) {
        try {
            String[] parts = length.split(":");
            return Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    private String timeToMmSs(String sqlTime) {
        if (sqlTime == null || sqlTime.length() < 5) return "0:00";
        if (sqlTime.length() >= 8) {
            return sqlTime.substring(3, 8);
        }
        return sqlTime;
    }

    private double moodScore(String mood) {
        if (mood == null) return 0;
        return switch (mood.trim().toUpperCase()) {
            case "ENERGETIC" -> 1.0;
            case "HAPPY" -> 0.8;
            case "FOCUSED" -> 0.6;
            case "RELAXED" -> 0.4;
            case "MELANCHOLIC" -> 0.2;
            default -> 0.5;
        };
    }

    private List<Map.Entry<String, Integer>> queryRankMap(String sql) {
        List<Map.Entry<String, Integer>> results = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString(1);
                    int value = rs.getInt(2);
                    if (key != null && !key.isBlank()) {
                        results.add(Map.entry(key, value));
                    }
                }
            }
        } catch (SQLException ignored) {
            return List.of();
        }
        return results;
    }
}