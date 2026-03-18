// Importer.java
// Reads your raw Exportify CSV and converts rows into Song objects.
// Handles ms to mm:ss conversion and genre/artist splitting.

package util;

import model.Song;

import java.io.*;
import java.util.*;

public class Importer {

    // Expected raw Exportify columns:
    // 0:row  1:userId  2:spotifyUri  3:trackName  4:albumName  5:discNumber
    // 6:releaseDate  7:durationMs  8:popularity  9:explicit  10:addedBy
    // 11:addedDate  12+:genres...  last:0

    public static List<Song> importFromCSV(String filePath) {
        List<Song> songs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    String[] d = Store.split(line);
                    if (d.length < 11) continue;

                    String trackName = d[0].trim();  // Track name
                    String albumName = d[1].trim();  // Album name
                    String artists   = d[2].trim().replace(";", "|"); // Artists
                    String length    = d[5].trim();  // Length (already mm:ss)
                    String genres    = d[8].trim().replace(";", "|"); // Genres
                    String mood      = d[9].trim();  // Mood
                    String link      = d[10].trim(); // Link

                    if (trackName.isEmpty()) continue;
                    songs.add(new Song(trackName, albumName, artists, length, genres, mood, link));

                } catch (Exception e) {
                    System.out.println("Skipping row: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        return songs;
    }

    // Converts milliseconds string to mm:ss format
    private static String msToTime(String msStr) {
        try {
            long ms   = Long.parseLong(msStr);
            long secs = ms / 1000;
            return secs / 60 + ":" + String.format("%02d", secs % 60);
        } catch (NumberFormatException e) {
            return "0:00";
        }
    }
}