// RecsScreen.java
// Displays song recommendations filtered by top genre, mood or manual input.
// Songs already in the user's library are excluded.
// Recommendations only appear after user imports songs AND has play counts > 0.

package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Recommendation;
import model.User;
import service.RecommendationService;
import service.SongService;
import service.StatsService;
import dao.RecommendationDAO;

import java.util.List;
import java.util.Map;

public class RecsScreen {

    private final User                  user;
    private final RecommendationService recService;
    private final SongService           songService;
    private final StatsService          statsService;
    private final RecommendationDAO     recDAO = new RecommendationDAO();

    public RecsScreen(User user) {
        this.user = user;
        this.recService = new RecommendationService(user.getUserId());
        this.songService = new SongService(user.getUserId());
        this.statsService = new StatsService(user.getUserId());
    }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.SCREEN_BG);
        root.setLeft(new Sidebar("Recommendations", user));

        VBox content = new VBox(24);
        content.setPadding(new Insets(40));

        Label title = new Label("Recommendations");
        title.setStyle(Theme.LABEL_TITLE);
        Label sub = new Label("Discover new songs based on your listening habits");
        sub.setStyle(Theme.LABEL_SUBTITLE);

        // Check if user has listening history
        if (!recService.hasListeningHistory()) {
            VBox noHistoryBox = new VBox(16);
            noHistoryBox.setAlignment(Pos.CENTER);
            noHistoryBox.setPadding(new Insets(60));
            
            Label noHistoryTitle = new Label("No Listening History Yet");
            noHistoryTitle.setStyle(Theme.LABEL_TITLE);
            
            Label noHistoryMsg = new Label(
                "To get personalized recommendations, you need to:\n\n" +
                "1. Import your Spotify playlist from the Library screen\n" +
                "2. Update play counts for your songs\n\n" +
                "Once you have some listening data, we'll recommend songs\n" +
                "based on your favorite genres and mood preferences."
            );
            noHistoryMsg.setStyle(Theme.LABEL_SUBTITLE + "-fx-text-alignment: center;");
            noHistoryMsg.setWrapText(true);
            
            Button goToLibrary = new Button("Go to Library");
            goToLibrary.setStyle(Theme.BTN_PRIMARY);
            Theme.hoverPrimary(goToLibrary);
            goToLibrary.setOnAction(e -> {
                AppContext.primaryStage.setScene(new LibraryScreen(user).getScene());
            });
            
            noHistoryBox.getChildren().addAll(noHistoryTitle, noHistoryMsg, goToLibrary);
            content.getChildren().addAll(title, sub, noHistoryBox);
            root.setCenter(content);
            return new Scene(root, 1100, 720);
        }

        // filter buttons row
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        Button byGenreBtn  = filterBtn("By Top Genre");
        Button byMoodBtn   = filterBtn("By Mood Match");
        Button personalBtn = filterBtn("Personalized");
        Button manualBtn   = filterBtn("Manual Filter");

        // Manual filter controls
        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.setPromptText("Genre...");
        genreCombo.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        genreCombo.setPrefWidth(150);
        genreCombo.setVisible(false);
        genreCombo.getItems().add("Any");
        genreCombo.getItems().addAll(recDAO.getAllGenres());

        ComboBox<String> moodCombo = new ComboBox<>();
        moodCombo.setPromptText("Mood...");
        moodCombo.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        moodCombo.setPrefWidth(130);
        moodCombo.setVisible(false);
        moodCombo.getItems().addAll("Any", "ENERGETIC", "HAPPY", "FOCUSED", "RELAXED", "MELANCHOLIC");

        Button applyBtn = new Button("Apply");
        applyBtn.setStyle(Theme.BTN_PRIMARY);
        applyBtn.setVisible(false);
        Theme.hoverPrimary(applyBtn);

        filterRow.getChildren().addAll(byGenreBtn, byMoodBtn, personalBtn, manualBtn, genreCombo, moodCombo, applyBtn);

        Label statusLabel = new Label("");
        statusLabel.setStyle(Theme.LABEL_SUBTITLE);

        // table for all views
        TableView<Recommendation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        HomeScreen.styleTable(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Clickable track name column
        TableColumn<Recommendation, String> nameCol = new TableColumn<>("Track Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrackName()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Hyperlink link = new Hyperlink(item);
                    link.setStyle("-fx-text-fill: " + Theme.ACCENT + "; -fx-font-weight: bold;");
                    link.setOnAction(e -> {
                        Recommendation rec = getTableView().getItems().get(getIndex());
                        String url = rec.getLink();
                        if (url != null && !url.isBlank()) {
                            try {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    setGraphic(link);
                }
            }
        });

        TableColumn<Recommendation, String> artistCol = new TableColumn<>("Artists");
        artistCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getArtists().replace("|", ", ")));

        TableColumn<Recommendation, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlbumName()));

        TableColumn<Recommendation, String> genreCol = new TableColumn<>("Genres");
        genreCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenres().replace("|", ", ")));

        TableColumn<Recommendation, String> moodCol = new TableColumn<>("Mood");
        moodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMood()));
        moodCol.setMaxWidth(100);

        // Add to Library button column
        TableColumn<Recommendation, Void> addCol = new TableColumn<>("Action");
        addCol.setMaxWidth(120);
        addCol.setMinWidth(100);
        addCol.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("+ Add");
            {
                addBtn.setStyle("-fx-background-color: #1DB954; -fx-text-fill: #000000; -fx-background-radius: 20; -fx-padding: 6 16; -fx-font-weight: bold;");
                addBtn.setOnMouseEntered(e -> addBtn.setStyle("-fx-background-color: #1ED760; -fx-text-fill: #000000; -fx-background-radius: 20; -fx-padding: 6 16; -fx-font-weight: bold;"));
                addBtn.setOnMouseExited(e -> addBtn.setStyle("-fx-background-color: #1DB954; -fx-text-fill: #000000; -fx-background-radius: 20; -fx-padding: 6 16; -fx-font-weight: bold;"));
                addBtn.setOnAction(e -> {
                    Recommendation rec = getTableView().getItems().get(getIndex());
                    songService.addFromRecommendation(rec);
                    
                    // Remove from table since it's now in library
                    getTableView().getItems().remove(rec);
                    statusLabel.setText("✓ Added \"" + rec.getTrackName() + "\" to your library");
                    statusLabel.setStyle(Theme.LABEL_SUCCESS);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : addBtn);
            }
        });

        table.getColumns().addAll(nameCol, artistCol, albumCol, genreCol, moodCol, addCol);

        // load personalized recommendations on start
        List<Recommendation> personalizedRecs = recService.getPersonalized();
        loadRecs(table, personalizedRecs, statusLabel, "Personalized recommendations");

        byGenreBtn.setOnAction(e -> {
            genreCombo.setVisible(false); moodCombo.setVisible(false); applyBtn.setVisible(false);
            List<Recommendation> recs = recService.byTopGenre();
            String topGenre = topGenreLabel();
            loadRecs(table, recs, statusLabel, "Top Genre: " + topGenre);
        });
        
        byMoodBtn.setOnAction(e -> {
            genreCombo.setVisible(false); moodCombo.setVisible(false); applyBtn.setVisible(false);
            List<Recommendation> recs = recService.byTopMood();
            double avgScore = statsService.getAverageMoodScore();
            loadRecs(table, recs, statusLabel, "Mood Match (avg score: " + String.format("%.2f", avgScore) + ")");
        });
        
        personalBtn.setOnAction(e -> {
            genreCombo.setVisible(false); moodCombo.setVisible(false); applyBtn.setVisible(false);
            loadRecs(table, recService.getPersonalized(), statusLabel, "Personalized recommendations (genre + mood)");
        });
        
        manualBtn.setOnAction(e -> {
            genreCombo.setVisible(true); 
            moodCombo.setVisible(true); 
            applyBtn.setVisible(true);
        });
        
        applyBtn.setOnAction(e -> {
            String selectedGenre = genreCombo.getValue();
            String selectedMood = moodCombo.getValue();
            
            List<Recommendation> results = recService.getAll();
            
            // Filter by genre if not "Any"
            if (selectedGenre != null && !selectedGenre.isEmpty() && !selectedGenre.equals("Any")) {
                results = recService.byGenre(selectedGenre);
            }
            
            // Filter by mood if not "Any"
            if (selectedMood != null && !selectedMood.isEmpty() && !selectedMood.equals("Any")) {
                final String mood = selectedMood;
                results = results.stream()
                    .filter(r -> r.getMood().equalsIgnoreCase(mood))
                    .toList();
            }
            
            String filterDesc = buildFilterDescription(selectedGenre, selectedMood);
            loadRecs(table, results, statusLabel, filterDesc);
        });

        content.getChildren().addAll(title, sub, filterRow, statusLabel, table);
        root.setCenter(content);
        return new Scene(root, 1100, 720);
    }

    private String buildFilterDescription(String genre, String mood) {
        boolean anyGenre = genre == null || genre.isEmpty() || genre.equals("Any");
        boolean anyMood = mood == null || mood.isEmpty() || mood.equals("Any");
        
        if (anyGenre && anyMood) {
            return "Showing all recommendations";
        } else if (anyGenre) {
            return "Filtered by mood: " + mood;
        } else if (anyMood) {
            return "Filtered by genre: " + genre;
        } else {
            return "Filtered by genre: " + genre + " & mood: " + mood;
        }
    }

    private void loadRecs(TableView<Recommendation> table,
                          List<Recommendation> recs,
                          Label statusLabel, String message) {
        table.getItems().setAll(recs);
        statusLabel.setText(message + " — " + recs.size() + " results");
        statusLabel.setStyle(Theme.LABEL_SUBTITLE);
    }

    private Button filterBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(Theme.BTN_GHOST);
        Theme.hoverGhost(btn);
        return btn;
    }

    private String topGenreLabel() {
        List<Map.Entry<String, Integer>> genres = statsService.getTopGenres();
        return genres.isEmpty() ? "N/A" : genres.get(0).getKey();
    }
}