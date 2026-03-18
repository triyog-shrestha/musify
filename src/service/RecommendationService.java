// RecommendationService.java
// Logic for fetching and filtering recommendations.

package service;

import dao.RecommendationDAO;
import model.Recommendation;

import java.util.List;

public class RecommendationService {

    private final RecommendationDAO dao          = new RecommendationDAO();
    private final StatsService      statsService = new StatsService();

    // All recommendations
    public List<Recommendation> getAll() {
        return dao.getAll();
    }

    // Recommendations matching user's top genre
    public List<Recommendation> byTopGenre() {
        List<java.util.Map.Entry<String, Integer>> genres = statsService.getTopGenres();
        if (genres.isEmpty()) return getAll();
        String topGenre = genres.get(0).getKey();
        List<Recommendation> results = dao.filterByGenre(topGenre);
        return results.isEmpty() ? getAll() : results;
    }

    // Recommendations matching user's top mood
    public List<Recommendation> byTopMood() {
        String topMood = statsService.getTopMood();
        List<Recommendation> results = dao.filterByMood(topMood);
        return results.isEmpty() ? getAll() : results;
    }

    // Manual genre filter
    public List<Recommendation> byGenre(String genre) {
        return dao.filterByGenre(genre);
    }
}