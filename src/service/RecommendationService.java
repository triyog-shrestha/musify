// RecommendationService.java
// Logic for fetching and filtering recommendations.
// Songs already in the user's library are excluded using the Spotify URL as identifier.

package service;

import dao.RecommendationDAO;
import dao.SongDAO;
import model.Recommendation;
import model.Song;

import java.util.*;

public class RecommendationService {

    private final RecommendationDAO dao         = new RecommendationDAO();
    private final SongDAO           songDAO     = new SongDAO();
    private final StatsService      statsService = new StatsService();

    // Builds a set of all Spotify links already in the user's library.
    // Used to filter out songs the user already has.
    private Set<String> getLibraryLinks() {
        Set<String> links = new HashSet<>();
        for (Song song : songDAO.getAll()) {
            String link = song.getLink().trim().toLowerCase();
            if (!link.isEmpty()) links.add(link);
        }
        return links;
    }

    // Removes recommendations whose link already exists in the library
    private List<Recommendation> exclude(List<Recommendation> recs) {
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

    // All recommendations excluding songs already in library
    public List<Recommendation> getAll() {
        return exclude(dao.getAll());
    }

    // Recommendations matching user's top genre, excluding library songs
    public List<Recommendation> byTopGenre() {
        List<Map.Entry<String, Integer>> genres = statsService.getTopGenres();
        if (genres.isEmpty()) return getAll();
        String topGenre = genres.get(0).getKey();
        List<Recommendation> results = exclude(dao.filterByGenre(topGenre));
        return results.isEmpty() ? getAll() : results;
    }

    // Recommendations matching user's top mood, excluding library songs
    public List<Recommendation> byTopMood() {
        String topMood = statsService.getTopMood();
        List<Recommendation> results = exclude(dao.filterByMood(topMood));
        return results.isEmpty() ? getAll() : results;
    }

    // Manual genre filter, excluding library songs
    public List<Recommendation> byGenre(String genre) {
        return exclude(dao.filterByGenre(genre));
    }
}