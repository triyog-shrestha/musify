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

        // top songs
        VBox topSongsCard = sectionCard("Top Songs");
        List<Song> topSongs = statsService.getTopSongs();
        if (topSongs.stream().allMatch(s -> s.getPlayCount() == 0)) {
            topSongsCard.getChildren().add(emptyLabel("No play counts recorded yet."));
        } else {
            int rank = 1;
            for (Song s : topSongs.stream().filter(x -> x.getPlayCount() > 0).limit(5).toList()) {
                topSongsCard.getChildren().add(rankRow(rank++, s.getTrackName(),
                        s.getArtists().replace("|", ", "), s.getPlayCount() + " plays"));
            }
        }

        // top artists and top genres side by side
        HBox topRow = new HBox(16);
        HBox.setHgrow(topRow, Priority.ALWAYS);

        VBox topArtistsCard = sectionCard("Top Artists");
        List<Map.Entry<String, Integer>> artists = statsService.getTopArtists();
        if (artists.isEmpty()) {
            topArtistsCard.getChildren().add(emptyLabel("No data yet."));
        } else {
            int rank = 1;
            for (Map.Entry<String, Integer> e : artists.stream().limit(5).toList()) {
                topArtistsCard.getChildren().add(rankRow(rank++, e.getKey(), "", e.getValue() + " plays"));
            }
        }
        HBox.setHgrow(topArtistsCard, Priority.ALWAYS);

        VBox topGenresCard = sectionCard("Top Genres");
        List<Map.Entry<String, Integer>> genres = statsService.getTopGenres();
        if (genres.isEmpty()) {
            topGenresCard.getChildren().add(emptyLabel("No data yet."));
        } else {
            int rank = 1;
            for (Map.Entry<String, Integer> e : genres.stream().limit(5).toList()) {
                topGenresCard.getChildren().add(rankRow(rank++, e.getKey(), "", e.getValue() + " plays"));
            }
        }
        HBox.setHgrow(topGenresCard, Priority.ALWAYS);

        topRow.getChildren().addAll(topArtistsCard, topGenresCard);

        // top albums on separate row below
        VBox topAlbumsCard = sectionCard("Top Albums");
        List<Map.Entry<String, Integer>> albums = statsService.getTopAlbums();
        if (albums.isEmpty()) {
            topAlbumsCard.getChildren().add(emptyLabel("No data yet."));
        } else {
            int rank = 1;
            for (Map.Entry<String, Integer> e : albums.stream().limit(5).toList()) {
                topAlbumsCard.getChildren().add(rankRow(rank++, e.getKey(), "", e.getValue() + " plays"));
            }
        }

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

    private VBox sectionCard(String heading) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setStyle(Theme.CARD);
        Label h = new Label(heading);
        h.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");
        card.getChildren().add(h);
        return card;
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