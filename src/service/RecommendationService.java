// RecommendationService.java
// Logic for fetching and filtering recommendations.
// Songs already in the user's library are excluded using the Spotify URL as identifier.
// Recommendations only appear after user imports songs AND has play counts > 0.

package service;

import dao.RecommendationDAO;
import dao.SongDAO;
import model.Recommendation;
import model.Song;
import util.Importer;

import java.util.*;

public class RecommendationService {

    private final RecommendationDAO dao         = new RecommendationDAO();
    private final SongDAO           songDAO;
    private final StatsService      statsService;
    private final int               userId;

    public RecommendationService() {
        this.userId = 1;
        this.songDAO = new SongDAO();
        this.statsService = new StatsService();
    }

    public RecommendationService(int userId) {
        this.userId = userId;
        this.songDAO = new SongDAO();
        this.statsService = new StatsService(userId);
    }

    // Import recommendations from CSV file
    public int importFromCSV(String filePath) {
        List<Song> songs = Importer.importFromCSV(filePath);
        if (songs.isEmpty()) return 0;
        
        List<Recommendation> recs = new ArrayList<>();
        for (Song song : songs) {
            Recommendation rec = new Recommendation(
                song.getTrackName(),
                song.getAlbumName(),
                song.getArtists(),
                song.getLength(),
                song.getGenres(),
                song.getMood(),
                song.getLink()
            );
            recs.add(rec);
        }
        
        return dao.insertBatch(recs);
    }

    /**
     * Checks if the user has imported songs AND has at least one play count > 0.
     * Recommendations should only be shown when this returns true.
     */
    public boolean hasListeningHistory() {
        List<Song> userSongs = songDAO.getAllForUser(userId);
        if (userSongs.isEmpty()) return false;
        
        for (Song song : userSongs) {
            if (song.getPlayCount() > 0) return true;
        }
        return false;
    }

    /**
     * Builds a set of all Spotify links already in the user's library.
     * Used to filter out songs the user already has.
     */
    private Set<String> getLibraryLinks() {
        Set<String> links = new HashSet<>();
        for (Song song : songDAO.getAllForUser(userId)) {
            String link = song.getLink().trim().toLowerCase();
            if (!link.isEmpty()) links.add(link);
        }
        return links;
    }

    /**
     * Removes recommendations whose Spotify URL already exists in the user's library.
     */
    private List<Recommendation> excludeLibrarySongs(List<Recommendation> recs) {
        Set<String> libraryLinks = getLibraryLinks();
        List<Recommendation> filtered = new ArrayList<>();
        for (Recommendation rec : recs) {
            String recLink = rec.getLink().trim().toLowerCase();
            if (!libraryLinks.contains(recLink)) {
                filtered.add(rec);
            }
        }
        return filtered;
    }

    /**
     * All recommendations excluding songs already in user's library.
     * Returns empty list if user has no listening history.
     */
    public List<Recommendation> getAll() {
        if (!hasListeningHistory()) return List.of();
        return excludeLibrarySongs(dao.getAll());
    }

    /**
     * Recommendations matching user's top genre based on play counts.
     * Returns empty list if user has no listening history.
     */
    public List<Recommendation> byTopGenre() {
        if (!hasListeningHistory()) return List.of();
        
        List<Map.Entry<String, Integer>> genres = statsService.getTopGenres();
        if (genres.isEmpty()) return getAll();
        
        String topGenre = genres.get(0).getKey();
        List<Recommendation> results = excludeLibrarySongs(dao.filterByGenre(topGenre));
        return results.isEmpty() ? getAll() : results;
    }

    /**
     * Recommendations matching user's average mood score.
     * Uses the user's weighted average mood score to find recommendations with similar moods.
     * Returns empty list if user has no listening history.
     */
    public List<Recommendation> byTopMood() {
        if (!hasListeningHistory()) return List.of();
        
        double avgMoodScore = statsService.getAverageMoodScore();
        List<Recommendation> allRecs = excludeLibrarySongs(dao.getAll());
        
        if (allRecs.isEmpty()) return List.of();
        
        // Score each recommendation based on how close its mood is to user's average
        List<ScoredRecommendation> scored = new ArrayList<>();
        for (Recommendation rec : allRecs) {
            double recMoodScore = moodScore(rec.getMood());
            double distance = Math.abs(recMoodScore - avgMoodScore);
            scored.add(new ScoredRecommendation(rec, distance));
        }
        
        // Sort by closest mood match (smallest distance first)
        scored.sort(Comparator.comparingDouble(s -> s.distance));
        
        // Return recommendations with mood score within 0.2 of user's average
        List<Recommendation> results = new ArrayList<>();
        for (ScoredRecommendation s : scored) {
            if (s.distance <= 0.2) {
                results.add(s.rec);
            }
        }
        
        // If no close matches, return top recommendations sorted by mood similarity
        if (results.isEmpty()) {
            for (ScoredRecommendation s : scored) {
                results.add(s.rec);
                if (results.size() >= 20) break; // Limit to top 20
            }
        }
        
        return results;
    }

    /**
     * Combined recommendations: matches both top genre AND similar mood score.
     * Provides the most personalized recommendations.
     * Returns empty list if user has no listening history.
     */
    public List<Recommendation> getPersonalized() {
        if (!hasListeningHistory()) return List.of();
        
        List<Map.Entry<String, Integer>> genres = statsService.getTopGenres();
        double avgMoodScore = statsService.getAverageMoodScore();
        
        List<Recommendation> allRecs = excludeLibrarySongs(dao.getAll());
        if (allRecs.isEmpty()) return List.of();
        
        // Get top genres (up to 3)
        Set<String> topGenres = new HashSet<>();
        for (int i = 0; i < Math.min(3, genres.size()); i++) {
            topGenres.add(genres.get(i).getKey().toLowerCase());
        }
        
        // Score recommendations based on genre match and mood similarity
        List<ScoredRecommendation> scored = new ArrayList<>();
        for (Recommendation rec : allRecs) {
            double score = 0;
            
            // Genre match bonus (higher is better)
            Set<String> recGenres = new HashSet<>();
            for (String g : rec.getGenres().toLowerCase().split("\\|")) {
                recGenres.add(g.trim());
            }
            for (String topGenre : topGenres) {
                if (recGenres.contains(topGenre)) {
                    score += 1.0;
                }
            }
            
            // Mood similarity (smaller distance = higher score)
            double recMoodScore = moodScore(rec.getMood());
            double moodDistance = Math.abs(recMoodScore - avgMoodScore);
            score += (1.0 - moodDistance); // Closer mood = higher score
            
            scored.add(new ScoredRecommendation(rec, -score)); // Negative so we can sort ascending
        }
        
        // Sort by score (highest first, so ascending on negative)
        scored.sort(Comparator.comparingDouble(s -> s.distance));
        
        List<Recommendation> results = new ArrayList<>();
        for (ScoredRecommendation s : scored) {
            results.add(s.rec);
        }
        
        return results;
    }

    /**
     * Manual genre filter, excluding songs already in user's library.
     */
    public List<Recommendation> byGenre(String genre) {
        return excludeLibrarySongs(dao.filterByGenre(genre));
    }

    /**
     * Maps mood string to numeric score (0.0 - 1.0).
     */
    private double moodScore(String mood) {
        if (mood == null) return 0.5;
        return switch (mood.trim().toUpperCase()) {
            case "ENERGETIC" -> 1.0;
            case "HAPPY" -> 0.8;
            case "FOCUSED" -> 0.6;
            case "RELAXED" -> 0.4;
            case "MELANCHOLIC" -> 0.2;
            default -> 0.5;
        };
    }

    /**
     * Helper class for scoring recommendations.
     */
    private static class ScoredRecommendation {
        final Recommendation rec;
        final double distance;
        
        ScoredRecommendation(Recommendation rec, double distance) {
            this.rec = rec;
            this.distance = distance;
        }
    }
}