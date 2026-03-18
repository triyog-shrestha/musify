// Store.java
// Acts as the database. Reads and writes CSV files.
// All DAOs use this — nothing else touches files directly.

package util;

import java.io.*;
import java.util.*;

public class Store {

    // File paths
    public static final String DATA_DIR      = "data/";
    public static final String SONGS_FILE    = DATA_DIR + "songs.csv";
    public static final String RECS_FILE     = DATA_DIR + "recommendations.csv";

    // Headers
    private static final String SONGS_HEADER = "songId,trackName,albumName,artists,length,genres,mood,link,playCount";
    private static final String RECS_HEADER  = "recId,trackName,albumName,artists,length,genres,mood,link";

    // Called once at startup — creates files if they don't exist
    public static void init() {
        new File(DATA_DIR).mkdirs();
        createIfMissing(SONGS_FILE, SONGS_HEADER);
        createIfMissing(RECS_FILE,  RECS_HEADER);
    }

    private static void createIfMissing(String path, String header) {
        File f = new File(path);
        if (!f.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                bw.write(header);
                bw.newLine();
            } catch (IOException e) {
                System.out.println("Could not create file: " + path);
            }
        }
    }

    // Read all rows from a file, skipping the header
    public static List<String[]> readAll(String filePath) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    rows.add(split(line));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading: " + filePath);
        }
        return rows;
    }

    // Append one row to the end of a file
    public static void append(String filePath, String row) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(row);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error writing: " + filePath);
        }
    }

    // Rewrite entire file — used for update and delete
    public static void overwrite(String filePath, List<String> rows) {
        String header = filePath.equals(SONGS_FILE) ? SONGS_HEADER : RECS_HEADER;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            bw.write(header);
            bw.newLine();
            for (String row : rows) {
                bw.write(row);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error overwriting: " + filePath);
        }
    }

    // Get the next available ID (max existing + 1)
    public static int nextId(String filePath) {
        int max = 0;
        for (String[] row : readAll(filePath)) {
            try {
                int id = Integer.parseInt(row[0].trim());
                if (id > max) max = id;
            } catch (NumberFormatException ignored) {}
        }
        return max + 1;
    }

    // Split a CSV line correctly, handles quoted fields
    public static String[] split(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    // Wrap in quotes if value contains a comma
    public static String safe(String value) {
        if (value == null) return "";
        return value.contains(",") ? "\"" + value + "\"" : value;
    }

    // Safe int parse — returns 0 on failure
    public static int parseInt(String val) {
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}