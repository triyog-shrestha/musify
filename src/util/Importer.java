/**
 * CSV importer for Spotify Exportify format (from exportify.net).
 * Parses raw Exportify files with 24 columns including audio features.
 * Audio features are used to calculate mood automatically.
 */
package util;

import model.Mood;
import model.Song;
import model.Recommendation;

import java.io.*;
import java.util.*;

public class Importer {

    /**
     * Imports songs from a raw Exportify CSV file.
     * 
     * @param filePath Path to Exportify CSV file
     * @return List of Song objects parsed from the file
     */
    public static List<Song> importFromCSV(String filePath) {
        List<Song> songs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                System.out.println("File is empty.");
                return songs;
            }

            String line;
            int row = 0;

            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;

                try {
                    Song song = parseRawRow(line);
                    if (song != null) songs.add(song);
                } catch (Exception e) {
                    System.out.println("Skipping row " + row + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        return songs;
    }

    /**
     * Parses a row from raw Exportify CSV format (24+ columns).
     * Extracts audio features and calculates mood using MoodCalculator.
     */
    private static Song parseRawRow(String line) {
        String[] d = splitCSV(line);
        if (d.length < 23) return null;

        String link      = spotifyLinkFromUri(d[0].trim());
        String trackName = d[1].trim();
        if (trackName.isEmpty()) return null;
        String albumName = d[2].trim();
        String artists   = normalizeArtists(d[3]);
        int totalArtists = countUniqueArtists(artists);
        String length    = msToTime(d[5].trim());
        String genres    = normalizeGenres(d[10]);

        double danceability = parseDouble(d[12]);
        double energy       = parseDouble(d[13]);
        double acousticness = parseDouble(d[18]);
        double valence      = parseDouble(d[21]);
        double tempo        = parseDouble(d[22]);
        int    mode         = parseInt(d[16]);

        Mood mood = MoodCalculator.fromAudioFeatures(
                danceability, energy, acousticness, valence, tempo, mode
        );

        return new Song(trackName, albumName, artists, totalArtists, length, genres, mood.name(), link);
    }

    /**
     * Converts milliseconds to mm:ss format.
     */
    private static String msToTime(String msStr) {
        try {
            long ms   = Long.parseLong(msStr);
            long secs = ms / 1000;
            return secs / 60 + ":" + String.format("%02d", secs % 60);
        } catch (NumberFormatException e) {
            return "0:00";
        }
    }

    /**
     * Converts a Spotify track URI to a full Spotify web URL.
     */
    private static String spotifyLinkFromUri(String trackUri) {
        if (trackUri == null) return "";
        String clean = trackUri.trim();
        if (clean.startsWith("http://") || clean.startsWith("https://")) return clean;
        return "https://open.spotify.com/track/" + clean.replace("spotify:track:", "");
    }

    /**
     * Normalizes artist names from semicolon-separated to pipe-separated format.
     */
    private static String normalizeArtists(String raw) {
        if (raw == null || raw.isBlank()) return "";
        List<String> normalized = new ArrayList<>();
        for (String part : raw.split(";")) {
            String a = part.trim();
            if (!a.isEmpty()) normalized.add(a);
        }
        return String.join("|", normalized);
    }

    /**
     * Counts unique artists from a pipe-separated artist string.
     */
    private static int countUniqueArtists(String normalizedArtists) {
        if (normalizedArtists == null || normalizedArtists.isBlank()) return 0;
        Set<String> unique = new HashSet<>();
        for (String artist : normalizedArtists.split("\\|")) {
            String a = artist.trim().toLowerCase();
            if (!a.isEmpty()) unique.add(a);
        }
        return unique.size();
    }

    /**
     * Normalizes genres using GenreUtil and converts to pipe-separated format.
     */
    private static String normalizeGenres(String rawGenres) {
        List<String> genres = GenreUtil.splitGenres(rawGenres == null ? "" : rawGenres);
        return String.join("|", genres);
    }

    /**
     * Splits a CSV line into fields, properly handling quoted values with embedded commas.
     * Handles escaped quotes (double quotes) within quoted fields.
     */
    private static String[] splitCSV(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // consume escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
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

    private static double parseDouble(String val) {
        try { return Double.parseDouble(val.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private static int parseInt(String val) {
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    /**
     * Imports recommendations from a raw Exportify CSV file.
     * 
     * @param filePath Path to Exportify CSV file
     * @return List of Recommendation objects
     */
    public static List<Recommendation> parseRecommendations(String filePath) {
        List<Recommendation> recs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String headerLine = br.readLine();
            if (headerLine == null) return recs;

            String header = headerLine.toLowerCase();
            boolean isAppRecCsv = header.contains("recid") && header.contains("trackname")
                    && header.contains("albumname") && header.contains("artists");

            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                try {
                    Recommendation rec = isAppRecCsv ? parseAppRec(line) : parseRawRec(line);
                    if (rec != null) recs.add(rec);
                } catch (Exception e) {
                    System.out.println("Skipping row " + row + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return recs;
    }

    /**
     * Parses a raw Exportify row into a Recommendation object.
     */
    private static Recommendation parseRawRec(String line) {
        String[] d = splitCSV(line);
        if (d.length < 23) return null;

        String trackName = d[1].trim();
        if (trackName.isEmpty()) return null;

        String link      = spotifyLinkFromUri(d[0].trim());
        String albumName = d[2].trim();
        String artists   = normalizeArtists(d[3]);
        String length    = msToTime(d[5].trim());
        String genres    = normalizeGenres(d[10]);

        double danceability = parseDouble(d[12]);
        double energy       = parseDouble(d[13]);
        double acousticness = parseDouble(d[18]);
        double valence      = parseDouble(d[21]);
        double tempo        = parseDouble(d[22]);
        int    mode         = parseInt(d[16]);

        Mood mood = MoodCalculator.fromAudioFeatures(
                danceability, energy, acousticness, valence, tempo, mode
        );

        return new Recommendation(trackName, albumName, artists, length, genres, mood.name(), link);
    }

    /**
     * Parses an app-exported recommendation CSV row.
     * Format: recId, trackName, albumName, artists, length, genres, mood, link
     */
    private static Recommendation parseAppRec(String line) {
        String[] d = splitCSV(line);
        if (d.length < 8) return null;

        String trackName = d[1].trim();
        if (trackName.isEmpty()) return null;

        return new Recommendation(trackName, d[2].trim(), normalizeArtists(d[3]),
                d[4].trim(), normalizeGenres(d[5]), d[6].trim(), d[7].trim());
    }
}