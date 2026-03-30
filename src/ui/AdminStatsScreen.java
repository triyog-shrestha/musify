// AdminStatsScreen.java
// Admin statistics screen - Shows app-wide statistics.

package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.User;
import dao.RecommendationDAO;
import service.AdminService;
import util.Database;

import java.sql.*;

public class AdminStatsScreen {

    private final User user;
    private final AdminService adminService = new AdminService();
    private final RecommendationDAO recDAO = new RecommendationDAO();

    public AdminStatsScreen(User user) {
        this.user = user;
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

        Label title = new Label("System Statistics");
        title.setStyle(Theme.LABEL_TITLE);
        Label sub = new Label("App-wide metrics and insights");
        sub.setStyle(Theme.LABEL_SUBTITLE);

        // User stats card
        VBox userCard = new VBox(16);
        userCard.setStyle(Theme.CARD);
        userCard.setPadding(new Insets(24));

        Label userTitle = new Label("User Statistics");
        userTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        int totalUsers = adminService.getAllUsers().size();
        int totalListeners = (int) adminService.getAllUsers().stream()
                .filter(u -> !(u instanceof model.Admin))
                .count();
        int totalAdmins = totalUsers - totalListeners;

        HBox userStats = new HBox(16);
        userStats.getChildren().addAll(
                statPill("Total Users", String.valueOf(totalUsers)),
                statPill("Listeners", String.valueOf(totalListeners)),
                statPill("Admins", String.valueOf(totalAdmins))
        );

        userCard.getChildren().addAll(userTitle, userStats);

        // Content stats card
        VBox contentCard = new VBox(16);
        contentCard.setStyle(Theme.CARD);
        contentCard.setPadding(new Insets(24));

        Label contentTitle = new Label("Content Statistics");
        contentTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        int totalSongs = getTotalSongs();
        int totalArtists = getTotalArtists();
        int totalAlbums = getTotalAlbums();
        int totalRecommendations = recDAO.getAll().size();

        HBox contentStats = new HBox(16);
        contentStats.setAlignment(Pos.CENTER_LEFT);
        contentStats.getChildren().addAll(
                statPill("Total Songs", String.valueOf(totalSongs)),
                statPill("Total Artists", String.valueOf(totalArtists)),
                statPill("Total Albums", String.valueOf(totalAlbums)),
                statPill("Recommendations", String.valueOf(totalRecommendations))
        );

        contentCard.getChildren().addAll(contentTitle, contentStats);

        // Engagement stats card
        VBox engagementCard = new VBox(16);
        engagementCard.setStyle(Theme.CARD);
        engagementCard.setPadding(new Insets(24));

        Label engagementTitle = new Label("Engagement Statistics");
        engagementTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        long totalPlays = getTotalPlays();
        long totalMinutes = getTotalMinutes();

        HBox engagementStats = new HBox(16);
        engagementStats.getChildren().addAll(
                statPill("Total Play Counts", String.valueOf(totalPlays)),
                statPill("Total Minutes Listened", String.valueOf(totalMinutes))
        );

        engagementCard.getChildren().addAll(engagementTitle, engagementStats);

        content.getChildren().addAll(title, sub, userCard, contentCard, engagementCard);
        scroll.setContent(content);
        root.setCenter(scroll);
        return new Scene(root, 1100, 720);
    }

    private int getTotalSongs() {
        String sql = "SELECT COUNT(*) FROM Song";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalArtists() {
        String sql = "SELECT COUNT(*) FROM Artist";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalAlbums() {
        String sql = "SELECT COUNT(*) FROM Album";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long getTotalPlays() {
        String sql = "SELECT COALESCE(SUM(playCount), 0) FROM Library";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long getTotalMinutes() {
        // Calculate total minutes from all plays * song duration
        String sql = """
            SELECT COALESCE(SUM(
                l.playCount * (HOUR(s.tracklength) * 60 + MINUTE(s.tracklength) + SECOND(s.tracklength) / 60.0)
            ), 0) AS totalMinutes
            FROM Library l
            JOIN Song s ON s.songId = l.songId
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return (long) rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private HBox statPill(String label, String value) {
        HBox pill = new HBox(10);
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setPadding(new Insets(14, 24, 14, 24));
        pill.setStyle(Theme.CARD_ELEVATED);
        Label l = new Label(label + ":");
        l.setStyle(Theme.LABEL_SUBTITLE);
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: " + Theme.ACCENT + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        pill.getChildren().addAll(l, v);
        return pill;
    }
}
