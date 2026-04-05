// StatsScreen.java
// Displays top songs, artists, genres, mood, total plays and total minutes.

package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Song;
import model.User;
import service.StatsService;

import java.util.List;
import java.util.Map;
import java.util.Locale;

public class StatsScreen {

    private final User         user;
    private final StatsService statsService;

    public StatsScreen(User user) {
        this.user = user;
        this.statsService = new StatsService(user.getUserId());
    }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.SCREEN_BG);
        root.setLeft(new Sidebar("Stats", user));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_DARK + ";");

        VBox content = new VBox(28);
        content.setPadding(new Insets(40));

        Label title = new Label("Stats Dashboard");
        title.setStyle(Theme.LABEL_TITLE);
        Label sub = new Label("Your listening habits at a glance");
        sub.setStyle(Theme.LABEL_SUBTITLE);

        // overview stat cards
        HBox overviewRow = new HBox(16);
        int totalPlays = statsService.getTotalPlays();
        double avgMoodScore = statsService.getAverageMoodScore();
        String avgMoodDisplay = totalPlays == 0 ? "None" : String.format(Locale.US, "%.2f / 1.00", avgMoodScore);
        String topMoodDisplay = totalPlays == 0 ? "None" : statsService.getTopMood();
        
        overviewRow.getChildren().addAll(
                statCard("Total Plays",    String.valueOf(totalPlays)),
                statCard("Minutes Listened", String.valueOf(statsService.getTotalMinutes())),
                statCard("Top Mood",       topMoodDisplay),
                statCard("Avg Mood Score", avgMoodDisplay)
        );

        // top songs (scrollable)
        VBox topSongsCard = scrollableSectionCard("Top Songs");
        VBox topSongsContent = new VBox(8);
        List<Song> topSongs = statsService.getTopSongs();
        List<Song> filteredSongs = topSongs.stream().filter(x -> x.getPlayCount() > 0).toList();
        if (filteredSongs.isEmpty()) {
            topSongsContent.getChildren().add(emptyLabel("No play counts recorded yet."));
        } else {
            int rank = 1;
            for (Song s : filteredSongs) {
                topSongsContent.getChildren().add(rankRow(rank++, s.getTrackName(),
                        s.getArtists().replace("|", ", "), s.getPlayCount() + " plays"));
            }
        }
        addScrollableContent(topSongsCard, topSongsContent, 300);

        // top artists and top genres side by side
        HBox topRow = new HBox(16);
        HBox.setHgrow(topRow, Priority.ALWAYS);

        VBox topArtistsCard = scrollableSectionCard("Top Artists");
        VBox topArtistsContent = new VBox(8);
        List<Map.Entry<String, Integer>> artists = statsService.getTopArtists().stream()
                .filter(e -> e.getValue() > 0).toList();
        if (artists.isEmpty()) {
            topArtistsContent.getChildren().add(emptyLabel("No data yet."));
        } else {
            int rank = 1;
            for (Map.Entry<String, Integer> e : artists) {
                topArtistsContent.getChildren().add(rankRow(rank++, e.getKey(), "", e.getValue() + " plays"));
            }
        }
        addScrollableContent(topArtistsCard, topArtistsContent, 250);
        HBox.setHgrow(topArtistsCard, Priority.ALWAYS);

        VBox topGenresCard = scrollableSectionCard("Top Genres");
        VBox topGenresContent = new VBox(8);
        List<Map.Entry<String, Integer>> genres = statsService.getTopGenres().stream()
                .filter(e -> e.getValue() > 0).toList();
        if (genres.isEmpty()) {
            topGenresContent.getChildren().add(emptyLabel("No data yet."));
        } else {
            int rank = 1;
            for (Map.Entry<String, Integer> e : genres) {
                topGenresContent.getChildren().add(rankRow(rank++, e.getKey(), "", e.getValue() + " plays"));
            }
        }
        addScrollableContent(topGenresCard, topGenresContent, 250);
        HBox.setHgrow(topGenresCard, Priority.ALWAYS);

        topRow.getChildren().addAll(topArtistsCard, topGenresCard);

        // top albums (scrollable)
        VBox topAlbumsCard = scrollableSectionCard("Top Albums");
        VBox topAlbumsContent = new VBox(8);
        List<Map.Entry<String, Integer>> albums = statsService.getTopAlbums().stream()
                .filter(e -> e.getValue() > 0).toList();
        if (albums.isEmpty()) {
            topAlbumsContent.getChildren().add(emptyLabel("No data yet."));
        } else {
            int rank = 1;
            for (Map.Entry<String, Integer> e : albums) {
                topAlbumsContent.getChildren().add(rankRow(rank++, e.getKey(), "", e.getValue() + " plays"));
            }
        }
        addScrollableContent(topAlbumsCard, topAlbumsContent, 250);

        content.getChildren().addAll(title, sub, overviewRow, topSongsCard, topRow, topAlbumsCard);
        scroll.setContent(content);
        root.setCenter(scroll);
        return new Scene(root, 1100, 720);
    }

    private VBox statCard(String label, String value) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle(Theme.STAT_CARD);
        HBox.setHgrow(card, Priority.ALWAYS);
        Label l = new Label(label.toUpperCase());
        l.setStyle(Theme.LABEL_SECTION);
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: " + Theme.ACCENT + "; -fx-font-size: 26px; -fx-font-weight: bold;");
        card.getChildren().addAll(l, v);
        return card;
    }

    private VBox scrollableSectionCard(String heading) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setStyle(Theme.CARD);
        Label h = new Label(heading);
        h.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");
        card.getChildren().add(h);
        return card;
    }

    private void addScrollableContent(VBox card, VBox content, double maxHeight) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(maxHeight);
        scrollPane.setPrefHeight(maxHeight);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        content.setStyle("-fx-background-color: transparent;");
        
        // Apply themed scrollbar styling
        scrollPane.getStylesheets().add("data:text/css," + getThemedScrollbarCSS());
        
        card.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private String getThemedScrollbarCSS() {
        return String.join("",
            ".scroll-pane { -fx-background-color: transparent; }",
            ".scroll-pane > .viewport { -fx-background-color: transparent; }",
            ".scroll-pane .scroll-bar:vertical { -fx-background-color: transparent; -fx-pref-width: 8px; }",
            ".scroll-pane .scroll-bar:vertical .track { -fx-background-color: " + Theme.BG_ELEVATED + "; -fx-background-radius: 4px; }",
            ".scroll-pane .scroll-bar:vertical .thumb { -fx-background-color: " + Theme.TEXT_DIM + "; -fx-background-radius: 4px; }",
            ".scroll-pane .scroll-bar:vertical .thumb:hover { -fx-background-color: " + Theme.TEXT_MUTED + "; }",
            ".scroll-pane .scroll-bar:vertical .increment-button, .scroll-pane .scroll-bar:vertical .decrement-button { -fx-background-color: transparent; -fx-padding: 0; }",
            ".scroll-pane .scroll-bar:vertical .increment-arrow, .scroll-pane .scroll-bar:vertical .decrement-arrow { -fx-shape: \"\"; -fx-padding: 0; }"
        ).replace("#", "%23");
    }

    private HBox rankRow(int rank, String main, String sub, String right) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle("-fx-background-color: " + Theme.BG_ELEVATED + "; -fx-background-radius: 8;");

        Label rankLbl = new Label(String.valueOf(rank));
        rankLbl.setStyle("-fx-text-fill: " + Theme.ACCENT + "; -fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 20;");

        VBox info = new VBox(2);
        Label mainLbl = new Label(main);
        mainLbl.setStyle(Theme.LABEL_VALUE);
        info.getChildren().add(mainLbl);
        if (!sub.isEmpty()) {
            Label subLbl = new Label(sub);
            subLbl.setStyle(Theme.LABEL_SUBTITLE);
            info.getChildren().add(subLbl);
        }
        HBox.setHgrow(info, Priority.ALWAYS);

        Label rightLbl = new Label(right);
        rightLbl.setStyle(Theme.LABEL_SUBTITLE);

        row.getChildren().addAll(rankLbl, info, rightLbl);
        return row;
    }

    private Label emptyLabel(String text) {
        Label l = new Label(text);
        l.setStyle(Theme.LABEL_SUBTITLE);
        return l;
    }
}