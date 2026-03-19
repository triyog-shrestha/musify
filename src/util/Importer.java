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

import model.Song;

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
            boolean isRaw = headerLine.toLowerCase().contains("duration (ms)")
                    || headerLine.toLowerCase().contains("danceability");

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

        String link      = "https://open.spotify.com/track/" + d[0].trim().replace("spotify:track:", "");
        String trackName = d[1].trim();
        if (trackName.isEmpty()) return null;
        String albumName = d[2].trim();
        String artists   = d[3].trim().replace(";", "|");
        String length    = msToTime(d[5].trim());
        String genres    = d[10].trim();

        double danceability = parseDouble(d[12]);
        double energy       = parseDouble(d[13]);
        double acousticness = parseDouble(d[18]);
        double valence      = parseDouble(d[21]);
        double tempo        = parseDouble(d[22]);
        int    mode         = parseInt(d[16]);

        String mood = calculateMood(danceability, energy, acousticness, valence, tempo, mode);

        return new Song(trackName, albumName, artists, length, genres, mood, link);
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

        return new Song(trackName, d[1].trim(), d[2].trim(),
                d[5].trim(), d[8].trim(), d[9].trim(), d[10].trim());
    }

    // -------------------------------------------------------------------------
    // Mood calculator — same logic as CleanCsvFr.java
    // -------------------------------------------------------------------------
    private static String calculateMood(double danceability, double energy,
                                        double acousticness, double valence,
                                        double tempo, int mode) {
        double tempoNorm = (tempo - 60) / (200 - 60);
        tempoNorm = Math.max(0, Math.min(1, tempoNorm));

        if (energy > 0.75 && tempoNorm > 0.65)           return "ENERGETIC";
        if (valence > 0.65 && energy > 0.5)               return "HAPPY";
        if (valence < 0.4  && mode == 0)                  return "MELANCHOLIC";
        if (energy < 0.45  && acousticness > 0.5)         return "RELAXED";
        if (energy >= 0.4  && energy <= 0.65
                && danceability < 0.5)         return "FOCUSED";
        return "RELAXED";
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

    private static String[] splitCSV(String line) {
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

    private static double parseDouble(String val) {
        try { return Double.parseDouble(val.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private static int parseInt(String val) {
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}