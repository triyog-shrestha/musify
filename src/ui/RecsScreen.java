// RecsScreen.java
// Displays song recommendations filtered by top genre, mood or manual input.
// Songs already in the user's library are excluded.

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

import java.util.List;

public class RecsScreen {

    private final User                  user;
    private final RecommendationService recService = new RecommendationService();

    public RecsScreen(User user) { this.user = user; }

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

        // filter buttons row
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        Button byGenreBtn  = filterBtn("By Top Genre");
        Button byMoodBtn   = filterBtn("By Top Mood");
        Button manualBtn   = filterBtn("Manual Filter");
        Button allBtn      = filterBtn("Show All");

        TextField genreField = new TextField();
        genreField.setPromptText("Enter genre...");
        genreField.setStyle(Theme.FIELD);
        genreField.setPrefWidth(180);
        genreField.setVisible(false);
        Theme.focusField(genreField);

        Button applyBtn = new Button("Apply");
        applyBtn.setStyle(Theme.BTN_PRIMARY);
        applyBtn.setVisible(false);
        Theme.hoverPrimary(applyBtn);

        filterRow.getChildren().addAll(byGenreBtn, byMoodBtn, manualBtn, allBtn, genreField, applyBtn);

        Label statusLabel = new Label("");
        statusLabel.setStyle(Theme.LABEL_SUBTITLE);

        // table
        TableView<Recommendation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        HomeScreen.styleTable(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Recommendation, String> nameCol = new TableColumn<>("Track Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrackName()));

        TableColumn<Recommendation, String> artistCol = new TableColumn<>("Artists");
        artistCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getArtists().replace("|", ", ")));

        TableColumn<Recommendation, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlbumName()));

        TableColumn<Recommendation, String> genreCol = new TableColumn<>("Genres");
        genreCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenres().replace("|", ", ")));

        TableColumn<Recommendation, String> moodCol = new TableColumn<>("Mood");
        moodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMood()));
        moodCol.setMaxWidth(100);

        TableColumn<Recommendation, String> linkCol = new TableColumn<>("Link");
        linkCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLink()));

        table.getColumns().addAll(nameCol, artistCol, albumCol, genreCol, moodCol, linkCol);

        // load all on start
        loadRecs(table, recService.getAll(), statusLabel, "Showing all recommendations");

        byGenreBtn.setOnAction(e -> {
            genreField.setVisible(false); applyBtn.setVisible(false);
            loadRecs(table, recService.byTopGenre(), statusLabel, "Filtered by your top genre");
        });
        byMoodBtn.setOnAction(e -> {
            genreField.setVisible(false); applyBtn.setVisible(false);
            loadRecs(table, recService.byTopMood(), statusLabel, "Filtered by your top mood");
        });
        manualBtn.setOnAction(e -> {
            genreField.setVisible(true); applyBtn.setVisible(true);
        });
        allBtn.setOnAction(e -> {
            genreField.setVisible(false); applyBtn.setVisible(false);
            loadRecs(table, recService.getAll(), statusLabel, "Showing all recommendations");
        });
        applyBtn.setOnAction(e -> {
            String g = genreField.getText().trim();
            if (!g.isEmpty()) loadRecs(table, recService.byGenre(g), statusLabel, "Filtered by: " + g);
        });

        content.getChildren().addAll(title, sub, filterRow, statusLabel, table);
        root.setCenter(content);
        return new Scene(root, 1100, 720);
    }

    private void loadRecs(TableView<Recommendation> table,
                          List<Recommendation> recs,
                          Label statusLabel, String message) {
        table.getItems().setAll(recs);
        statusLabel.setText(message + " — " + recs.size() + " results");
    }

    private Button filterBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(Theme.BTN_GHOST);
        Theme.hoverGhost(btn);
        return btn;
    }
}