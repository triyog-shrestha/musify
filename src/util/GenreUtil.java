package util;

import java.util.ArrayList;
import java.util.List;

public class GenreUtil {

    // Splits multi-genre strings into clean individual tokens.
    public static List<String> splitGenres(String raw) {
        List<String> tokens = new ArrayList<>();
        if (raw == null || raw.isBlank()) return tokens;

        String normalized = raw.toLowerCase()
                .replace('/', '|')
                .replace(';', '|')
                .replace(",", "|");

        for (String part : normalized.split("\\|")) {
            String g = part.trim().replaceAll("\\s+", " ");
            if (!g.isEmpty()) tokens.add(g);
        }
        return tokens;
    }
}
