// Importer.java
// Takes a raw Exportify CSV file and converts it directly into Song objects.
// The user just points at the file from exportify.net — no manual cleaning needed.
// Also accepts already-cleaned files as a fallback.
//
// Raw Exportify columns:
// 0:TrackURI  1:TrackName  2:AlbumName  3:ArtistName(s)  4:ReleaseDate
// 5:Duration(ms)  6:Popularity  7:Explicit  8:AddedBy  9:AddedAt
// 10:Genres  11:RecordLabel  12:Danceability  13:Energy  14:Key
// 15:Loudness  16:Mode  17:Speechiness  18:Acousticness  19:Instrumentalness
// 20:Liveness  21:Valence  22:Tempo  23:TimeSignature

package util;

import model.Mood;
import model.Song;
import model.Recommendation;

import java.io.*;
import java.util.*;

public class Importer {

    public static List<Song> importFromCSV(String filePath) {
        List<Song> songs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                System.out.println("File is empty.");
                return songs;
            }

            // detect format by checking the header
            String header = headerLine.toLowerCase();
            boolean isRaw = header.contains("duration (ms)") || header.contains("danceability");

            System.out.println("Detected format: " + (isRaw ? "raw Exportify" : "cleaned CSV"));

            String line;
            int row = 0;

            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;

                try {
                    Song song = isRaw ? parseRawRow(line) : parseCleanedRow(line);
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

    // -------------------------------------------------------------------------
    // Raw Exportify row parser
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // Already-cleaned row parser (fallback)
    // Columns: 0:trackName 1:albumName 2:artists 3:totalArtists
    //          4:releaseDate 5:length 6:popularity 7:addedDate
    //          8:genres 9:mood 10:link
    // -------------------------------------------------------------------------
    private static Song parseCleanedRow(String line) {
        String[] d = splitCSV(line);
        if (d.length < 11) return null;

        String trackName = d[0].trim();
        if (trackName.isEmpty()) return null;

        String artists = normalizeArtists(d[2]);
        int totalArtists = d[3].isBlank() ? countUniqueArtists(artists) : parseInt(d[3]);
        String genres = normalizeGenres(d[8]);

        return new Song(trackName, d[1].trim(), artists,
                totalArtists, d[5].trim(), genres, d[9].trim(), d[10].trim());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String msToTime(String msStr) {
        try {
            long ms   = Long.parseLong(msStr);
            long secs = ms / 1000;
            return secs / 60 + ":" + String.format("%02d", secs % 60);
        } catch (NumberFormatException e) {
            return "0:00";
        }
    }

    private static String spotifyLinkFromUri(String trackUri) {
        if (trackUri == null) return "";
        String clean = trackUri.trim();
        if (clean.startsWith("http://") || clean.startsWith("https://")) return clean;
        return "https://open.spotify.com/track/" + clean.replace("spotify:track:", "");
    }

    private static String normalizeArtists(String raw) {
        if (raw == null || raw.isBlank()) return "";
        List<String> normalized = new ArrayList<>();
        for (String part : raw.split(";")) {
            String a = part.trim();
            if (!a.isEmpty()) normalized.add(a);
        }
        return String.join("|", normalized);
    }

    private static int countUniqueArtists(String normalizedArtists) {
        if (normalizedArtists == null || normalizedArtists.isBlank()) return 0;
        Set<String> unique = new HashSet<>();
        for (String artist : normalizedArtists.split("\\|")) {
            String a = artist.trim().toLowerCase();
            if (!a.isEmpty()) unique.add(a);
        }
        return unique.size();
    }

    private static String normalizeGenres(String rawGenres) {
        List<String> genres = GenreUtil.splitGenres(rawGenres == null ? "" : rawGenres);
        return String.join("|", genres);
    }

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

    // add this method to Importer.java

    public static List<Recommendation> parseRecommendations(String filePath) {
        List<Recommendation> recs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String headerLine = br.readLine();
            if (headerLine == null) return recs;

            String header = headerLine.toLowerCase();
            boolean isRaw = header.contains("duration (ms)") || header.contains("danceability");
            boolean isAppRecCsv = header.contains("recid") && header.contains("trackname")
                    && header.contains("albumname") && header.contains("artists");

            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                try {
                    Recommendation rec;
                    if (isRaw) {
                        rec = parseRawRec(line);
                    } else if (isAppRecCsv) {
                        rec = parseAppRec(line);
                    } else {
                        rec = parseCleanedRec(line);
                    }
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

    private static Recommendation parseCleanedRec(String line) {
        String[] d = splitCSV(line);
        if (d.length < 11) return null;

        String trackName = d[0].trim();
        if (trackName.isEmpty()) return null;

        return new Recommendation(trackName, d[1].trim(), normalizeArtists(d[2]),
                d[5].trim(), normalizeGenres(d[8]), d[9].trim(), d[10].trim());
    }

    private static Recommendation parseAppRec(String line) {
        String[] d = splitCSV(line);
        if (d.length < 8) return null;

        String trackName = d[1].trim();
        if (trackName.isEmpty()) return null;

        return new Recommendation(trackName, d[2].trim(), normalizeArtists(d[3]),
                d[4].trim(), normalizeGenres(d[5]), d[6].trim(), d[7].trim());
    }
}