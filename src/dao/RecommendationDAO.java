// RecommendationDAO.java
// All read operations for recommendations.csv.

package dao;

import model.Recommendation;
import util.Store;

import java.util.*;

public class RecommendationDAO {

    // Get all recommendations
    public List<Recommendation> getAll() {
        List<Recommendation> recs = new ArrayList<>();
        for (String[] row : Store.readAll(Store.RECS_FILE)) {
            if (row.length >= 8) recs.add(fromRow(row));
        }
        return recs;
    }

    // Filter by genre (case-insensitive, checks all pipe-separated genres)
    public List<Recommendation> filterByGenre(String genre) {
        List<Recommendation> results = new ArrayList<>();
        for (Recommendation rec : getAll()) {
            for (String g : rec.getGenres().split("\\|")) {
                if (g.trim().equalsIgnoreCase(genre)) {
                    results.add(rec);
                    break;
                }
            }
        }
        return results;
    }

    // Filter by mood
    public List<Recommendation> filterByMood(String mood) {
        List<Recommendation> results = new ArrayList<>();
        for (Recommendation rec : getAll()) {
            if (rec.getMood().equalsIgnoreCase(mood)) {
                results.add(rec);
            }
        }
        return results;
    }

    // CSV row → Recommendation object
    private Recommendation fromRow(String[] r) {
        return new Recommendation(
                Store.parseInt(r[0]), // recId
                r[1],                 // trackName
                r[2],                 // albumName
                r[3],                 // artists
                r[4],                 // length
                r[5],                 // genres
                r[6],                 // mood
                r[7]                  // link
        );
    }
}