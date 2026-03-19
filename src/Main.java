// Main.java
// Entry point. Runs the terminal menu for the core features:
// 1. Import songs from CSV
// 2. Update play counts by searching
// 3. Stats dashboard
// 4. Recommendations

import dao.SongDAO;
import model.Recommendation;
import model.Song;
import service.RecommendationService;
import service.SongService;
import service.StatsService;
import util.Store;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static SongService           songService = new SongService();
    static StatsService          statsService = new StatsService();
    static RecommendationService recService  = new RecommendationService();

    public static void main(String[] args) {
        Store.init();

        while (true) {
            System.out.println("\n1. Import songs from CSV");
            System.out.println("2. Update play count");
            System.out.println("3. View library");
            System.out.println("4. Stats");
            System.out.println("5. Recommendations");
            System.out.println("6. Delete a song");
            System.out.println("0. Exit");
            System.out.print("\n> ");

            switch (sc.nextLine().trim()) {
                case "1" -> importSongs();
                case "2" -> updatePlayCount();
                case "3" -> viewLibrary();
                case "4" -> showStats();
                case "5" -> showRecommendations();
                case "6" -> deleteSong();
                case "0" -> { System.out.println("Goodbye!"); return; }
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // 1. Import
    // -------------------------------------------------------------------------
    static void importSongs() {
        System.out.print("Paste the full path to your CSV file:\n> ");
        String path = sc.nextLine().trim().replace("\"", "");
        int count = songService.importSongs(path);
        System.out.println("Imported " + count + " songs.");
    }

    // -------------------------------------------------------------------------
    // 2. Update play count
    // -------------------------------------------------------------------------
    static void updatePlayCount() {
        System.out.print("Search song name:\n> ");
        String query = sc.nextLine().trim();

        List<Song> results = songService.search(query);
        if (results.isEmpty()) {
            System.out.println("No songs found.");
            return;
        }

        printSongs(results);

        System.out.print("Enter Song ID:\n> ");
        int id = Store.parseInt(sc.nextLine());
        if (id <= 0) {
            System.out.println("Invalid ID.");
            return;
        }

        // find the song from results to get current play count
        Song selected = null;
        for (Song s : results) {
            if (s.getSongId() == id) {
                selected = s;
                break;
            }
        }

        if (selected == null) {
            System.out.println("That ID is not in the search results.");
            return;
        }

        System.out.println("Current play count: " + selected.getPlayCount());
        System.out.print("How many plays to add:\n> ");
        int toAdd = Store.parseInt(sc.nextLine());

        if (toAdd < 0) {
            System.out.println("Cannot add a negative number.");
            return;
        }

        int newCount = selected.getPlayCount() + toAdd;
        songService.setPlayCount(id, newCount);
        System.out.println("Updated. " + selected.getTrackName() + " — "
                + selected.getPlayCount() + " + " + toAdd + " = " + newCount + " plays.");
    }

    // -------------------------------------------------------------------------
    // 3. View library
    // -------------------------------------------------------------------------
    static void viewLibrary() {
        List<Song> songs = songService.getAllSongs();
        if (songs.isEmpty()) {
            System.out.println("Library is empty. Import songs first.");
            return;
        }
        System.out.println("\n--- Library (" + songs.size() + " songs) ---");
        printSongs(songs);
    }

    // -------------------------------------------------------------------------
    // 4. Stats
    // -------------------------------------------------------------------------
    static void showStats() {
        System.out.println("\n======= STATS =======");

        System.out.println("\n-- Top Songs --");
        List<Song> topSongs = statsService.getTopSongs();
        topSongs.stream().limit(5).forEach(s ->
                System.out.printf("  %-35s %d plays%n", s.getTrackName(), s.getPlayCount()));

        System.out.println("\n-- Top Artists --");
        List<Map.Entry<String, Integer>> topArtists = statsService.getTopArtists();
        topArtists.stream().limit(5).forEach(e ->
                System.out.printf("  %-25s %d plays%n", e.getKey(), e.getValue()));

        System.out.println("\n-- Top Genres --");
        List<Map.Entry<String, Integer>> topGenres = statsService.getTopGenres();
        topGenres.stream().limit(5).forEach(e ->
                System.out.printf("  %-20s %d plays%n", e.getKey(), e.getValue()));

        System.out.println("\n-- Overview --");
        System.out.println("  Total plays           : " + statsService.getTotalPlays());
        System.out.println("  Total minutes listened: " + statsService.getTotalMinutes());
        System.out.println("  Top mood              : " + statsService.getTopMood());
    }

    // -------------------------------------------------------------------------
    // 5. Recommendations
    // -------------------------------------------------------------------------
    static void showRecommendations() {
        System.out.println("\n1. By your top genre");
        System.out.println("2. By your top mood");
        System.out.println("3. Filter by genre manually");
        System.out.println("4. Show all");
        System.out.print("> ");

        List<Recommendation> recs;
        switch (sc.nextLine().trim()) {
            case "1" -> recs = recService.byTopGenre();
            case "2" -> recs = recService.byTopMood();
            case "3" -> {
                System.out.print("Genre:\n> ");
                recs = recService.byGenre(sc.nextLine().trim());
            }
            default  -> recs = recService.getAll();
        }

        if (recs.isEmpty()) {
            System.out.println("No recommendations found.");
            return;
        }

        System.out.println("\n--- Recommendations ---");
        recs.stream().limit(10).forEach(System.out::println);
    }

    // -------------------------------------------------------------------------
    // 6. Delete
    // -------------------------------------------------------------------------
    static void deleteSong() {
        System.out.print("Search song to delete:\n> ");
        List<Song> results = songService.search(sc.nextLine().trim());
        if (results.isEmpty()) { System.out.println("Not found."); return; }
        printSongs(results);
        System.out.print("Enter Song ID to delete:\n> ");
        songService.deleteSong(Store.parseInt(sc.nextLine()));
        System.out.println("Deleted.");
    }

    // -------------------------------------------------------------------------
    // Helper: print song table
    // -------------------------------------------------------------------------
    static void printSongs(List<Song> songs) {
        System.out.printf("%n%-5s %-35s %-20s %-6s %-12s %s%n",
                "ID", "Track", "Artists", "Time", "Mood", "Plays");
        System.out.println("-".repeat(90));
        for (Song s : songs) {
            System.out.printf("%-5d %-35s %-20s %-6s %-12s %d%n",
                    s.getSongId(),
                    truncate(s.getTrackName(), 34),
                    truncate(s.getArtists().replace("|", ", "), 19),
                    s.getLength(),
                    s.getMood(),
                    s.getPlayCount());
        }
    }

    static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}