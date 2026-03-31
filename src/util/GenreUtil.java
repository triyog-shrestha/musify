/**
 * Utility for parsing and normalizing genre strings from various formats.
 * Handles multiple separators (|, /, ;, ,) and standardizes to lowercase with trimmed whitespace.
 */
package util;

import java.util.ArrayList;
import java.util.List;

public class GenreUtil {

    /**
     * Splits and normalizes a multi-genre string into clean, lowercase tokens.
     * 
     * Supported input formats:
     * - "Hip Hop / R&B; Pop" → ["hip hop", "r&b", "pop"]
     * - "rock|jazz|blues" → ["rock", "jazz", "blues"]
     * - "Electronic, House, Techno" → ["electronic", "house", "techno"]
     * 
     * @param raw The raw genre string (can contain |, /, ;, or , as separators)
     * @return List of normalized genre names (empty list if input is null/blank)
     */
    public static List<String> splitGenres(String raw) {
        List<String> tokens = new ArrayList<>();
        if (raw == null || raw.isBlank()) return tokens;

        // Normalize all separators to pipe character
        String normalized = raw.toLowerCase()
                .replace('/', '|')
                .replace(';', '|')
                .replace(",", "|");

        // Split and clean each genre
        for (String part : normalized.split("\\|")) {
            String g = part.trim().replaceAll("\\s+", " ");  // Collapse multiple spaces
            if (!g.isEmpty()) tokens.add(g);
        }
        return tokens;
    }
}
