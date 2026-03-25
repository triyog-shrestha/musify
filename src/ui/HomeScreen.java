// HomeScreen.java
// Landing screen after login.
// Lets the user import a CSV file and update play counts by searching songs.

package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Song;
import model.User;
import service.SongService;
import util.Store;

import java.util.List;

public class HomeScreen {

    private final User        user;
    private final SongService songService = new SongService();

    public HomeScreen(User user) { this.user = user; }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.SCREEN_BG);
        root.setLeft(new Sidebar("Home", user));

        VBox content = new VBox(28);
        content.setPadding(new Insets(40));
        content.setStyle(Theme.SCREEN_BG);

        // page title
        Label title = new Label("Home");
        title.setStyle(Theme.LABEL_TITLE);
        Label sub = new Label("Import your Spotify playlist or update play counts");
        sub.setStyle(Theme.LABEL_SUBTITLE);

        // import card
        VBox importCard = new VBox(16);
        importCard.setStyle(Theme.CARD);
        importCard.setPadding(new Insets(24));

        Label importTitle = new Label("Import Playlist");
        importTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label importSub = new Label("Paste the full path to your Exportify CSV file below");
        importSub.setStyle(Theme.LABEL_SUBTITLE);

        HBox importRow = new HBox(12);
        importRow.setAlignment(Pos.CENTER_LEFT);
        TextField pathField = new TextField();
        pathField.setPromptText("C:\\Users\\...\\playlist.csv");
        pathField.setStyle(Theme.FIELD);
        pathField.setPrefWidth(500);
        Theme.focusField(pathField);

        Button importBtn = new Button("Import");
        importBtn.setStyle(Theme.BTN_PRIMARY);
        importBtn.setMinHeight(40);
        Theme.hoverPrimary(importBtn);

        Label importResult = new Label("");
        importResult.setStyle(Theme.LABEL_SUCCESS);

        importBtn.setOnAction(e -> {
            String path = pathField.getText().trim().replace("\"", "");
            if (path.isEmpty()) {
                importResult.setStyle(Theme.LABEL_ERROR);
                importResult.setText("Please enter a file path.");
                return;
            }
            int count = songService.importSongs(path);
            if (count > 0) {
                importResult.setStyle(Theme.LABEL_SUCCESS);
                importResult.setText("Successfully imported " + count + " songs.");
            } else {
                importResult.setStyle(Theme.LABEL_ERROR);
                importResult.setText("No songs imported. Check the file path and format.");
            }
        });

        importRow.getChildren().addAll(pathField, importBtn);
        importCard.getChildren().addAll(importTitle, importSub, importRow, importResult);

        // update play count card
        VBox playCard = new VBox(16);
        playCard.setStyle(Theme.CARD);
        playCard.setPadding(new Insets(24));

        Label playTitle = new Label("Update Play Count");
        playTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label playSub = new Label("Search a song and add to its play count");
        playSub.setStyle(Theme.LABEL_SUBTITLE);

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search song name...");
        searchField.setStyle(Theme.FIELD);
        searchField.setPrefWidth(360);
        Theme.focusField(searchField);
        Button searchBtn = new Button("Search");
        searchBtn.setStyle(Theme.BTN_GHOST);
        searchBtn.setMinHeight(40);
        Theme.hoverGhost(searchBtn);
        searchRow.getChildren().addAll(searchField, searchBtn);

        // results table
        TableView<Song> table = buildSongTable();
        table.setMaxHeight(200);
        table.setVisible(false);

        HBox updateRow = new HBox(12);
        updateRow.setAlignment(Pos.CENTER_LEFT);
        updateRow.setVisible(false);

        Label idLabel = new Label("Song ID:");
        idLabel.setStyle(Theme.LABEL_SUBTITLE);
        TextField idField = new TextField();
        idField.setPromptText("ID");
        idField.setStyle(Theme.FIELD);
        idField.setPrefWidth(80);
        Theme.focusField(idField);

        Label addLabel = new Label("Plays to add:");
        addLabel.setStyle(Theme.LABEL_SUBTITLE);
        TextField playsField = new TextField();
        playsField.setPromptText("e.g. 5");
        playsField.setStyle(Theme.FIELD);
        playsField.setPrefWidth(80);
        Theme.focusField(playsField);

        Button updateBtn = new Button("Update");
        updateBtn.setStyle(Theme.BTN_PRIMARY);
        updateBtn.setMinHeight(40);
        Theme.hoverPrimary(updateBtn);
        Label updateResult = new Label("");

        updateRow.getChildren().addAll(idLabel, idField, addLabel, playsField, updateBtn);

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) return;
            List<Song> results = songService.search(q);
            table.getItems().setAll(results);
            table.setVisible(!results.isEmpty());
            updateRow.setVisible(!results.isEmpty());
            if (results.isEmpty()) {
                updateResult.setStyle(Theme.LABEL_ERROR);
                updateResult.setText("No songs found.");
            } else {
                updateResult.setText("");
            }
        });

        updateBtn.setOnAction(e -> {
            int id    = Store.parseInt(idField.getText());
            int toAdd = Store.parseInt(playsField.getText());
            if (id <= 0) { updateResult.setStyle(Theme.LABEL_ERROR); updateResult.setText("Invalid ID."); return; }
            if (toAdd < 0) { updateResult.setStyle(Theme.LABEL_ERROR); updateResult.setText("Cannot add negative plays."); return; }
            Song selected = table.getItems().stream()
                    .filter(s -> s.getSongId() == id).findFirst().orElse(null);
            if (selected == null) { updateResult.setStyle(Theme.LABEL_ERROR); updateResult.setText("ID not in results."); return; }
            int newCount = selected.getPlayCount() + toAdd;
            songService.setPlayCount(id, newCount);
            updateResult.setStyle(Theme.LABEL_SUCCESS);
            updateResult.setText(selected.getTrackName() + " — " + selected.getPlayCount() + " + " + toAdd + " = " + newCount + " plays.");
            List<Song> refreshed = songService.search(searchField.getText().trim());
            table.getItems().setAll(refreshed);
        });

        playCard.getChildren().addAll(playTitle, playSub, searchRow, table, updateRow, updateResult);

        content.getChildren().addAll(title, sub, importCard, playCard);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_DARK + ";");

        root.setCenter(scroll);
        return new Scene(root, 1100, 720);
    }

    private TableView<Song> buildSongTable() {
        TableView<Song> table = new TableView<>();
        table.setStyle(Theme.TABLE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Song, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getSongId()).asObject());
        idCol.setMaxWidth(60);

        TableColumn<Song, String> nameCol = new TableColumn<>("Track");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTrackName()));

        TableColumn<Song, String> artistCol = new TableColumn<>("Artists");
        artistCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getArtists().replace("|", ", ")));

        TableColumn<Song, Integer> playsCol = new TableColumn<>("Plays");
        playsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getPlayCount()).asObject());
        playsCol.setMaxWidth(80);

        table.getColumns().addAll(idCol, nameCol, artistCol, playsCol);
        styleTable(table);
        return table;
    }

    static void styleTable(TableView<?> table) {
        table.setStyle(
                "-fx-background-color: " + Theme.BG_CARD + ";" +
                        "-fx-border-color: " + Theme.BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-table-cell-border-color: " + Theme.BORDER + ";");
    }
}