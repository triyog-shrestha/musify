// LibraryScreen.java
// Displays all songs in a searchable table with edit and delete options.

package ui;

import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Song;
import model.User;
import service.SongService;

import java.util.List;

public class LibraryScreen {

    private final User        user;
    private final SongService songService;

    public LibraryScreen(User user) { 
        this.user = user; 
        this.songService = new SongService(user.getUserId());
    }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.SCREEN_BG);
        root.setLeft(new Sidebar("Library", user));

        VBox content = new VBox(24);
        content.setPadding(new Insets(40));

        Label title = new Label("Song Library");
        title.setStyle(Theme.LABEL_TITLE);

        // summary row with stats
        List<Song> all = songService.getAllSongs();
        
        long uniqueAlbums = all.stream()
            .map(s -> s.getAlbumName().trim())
            .filter(a -> !a.isEmpty())
            .distinct()
            .count();
        
        HBox summary = new HBox(16);
        summary.getChildren().addAll(
                statPill("Total Songs", String.valueOf(all.size())),
                statPill("Unique Artists", String.valueOf(
                        all.stream().flatMap(s -> java.util.Arrays.stream(s.getArtists().split("\\|")))
                                .map(String::trim).distinct().count())),
                statPill("Unique Albums", String.valueOf(uniqueAlbums))
        );

        // search bar with import button
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search songs...");
        searchField.setStyle(Theme.FIELD);
        searchField.setPrefWidth(320);
        Theme.focusField(searchField);
        Button searchBtn = new Button("Search");
        searchBtn.setStyle(Theme.BTN_GHOST);
        Theme.hoverGhost(searchBtn);
        Button clearBtn = new Button("Show All");
        clearBtn.setStyle(Theme.BTN_GHOST);
        Theme.hoverGhost(clearBtn);
        Button importBtn = new Button("Import CSV");
        importBtn.setStyle(Theme.BTN_PRIMARY);
        importBtn.setMinHeight(40);
        Theme.hoverPrimary(importBtn);
        searchRow.getChildren().addAll(searchField, searchBtn, clearBtn, importBtn);

        Label statusLabel = new Label("");
        statusLabel.setStyle(Theme.LABEL_ERROR);

        // table
        TableView<Song> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        HomeScreen.styleTable(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Clickable track name column that opens Spotify link
        TableColumn<Song, String> nameCol = new TableColumn<>("Track Name");
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
                        Song song = getTableView().getItems().get(getIndex());
                        String url = song.getLink();
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

        TableColumn<Song, String> artistCol  = new TableColumn<>("Artists");
        artistCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getArtists().replace("|", ", ")));

        TableColumn<Song, String> albumCol   = new TableColumn<>("Album");
        albumCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlbumName()));

        TableColumn<Song, String> lengthCol  = new TableColumn<>("Length");
        lengthCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLength()));
        lengthCol.setMaxWidth(70);

        TableColumn<Song, String> moodCol    = new TableColumn<>("Mood");
        moodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMood()));
        moodCol.setMaxWidth(90);

        TableColumn<Song, Integer> playsCol  = new TableColumn<>("Plays");
        playsCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getPlayCount()).asObject());
        playsCol.setMaxWidth(65);

        TableColumn<Song, Void> updateCol = new TableColumn<>("Edit");
        updateCol.setMinWidth(80);
        updateCol.setMaxWidth(90);
        updateCol.setCellFactory(col -> new TableCell<>() {
            final Button edit = new Button("Edit");
            {
                edit.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 12px; -fx-border-color: " + Theme.TEXT_MUTED + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 4 12; -fx-cursor: hand;");
                edit.setOnMouseEntered(e -> edit.setStyle("-fx-background-color: " + Theme.BG_ELEVATED + "; -fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 12px; -fx-border-color: " + Theme.TEXT_PRIMARY + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 4 12; -fx-cursor: hand;"));
                edit.setOnMouseExited(e -> edit.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 12px; -fx-border-color: " + Theme.TEXT_MUTED + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 4 12; -fx-cursor: hand;"));
                edit.setOnAction(e -> {
                    Song s = getTableView().getItems().get(getIndex());
                    Song edited = showEditDialog(s);
                    if (edited != null) {
                        songService.updateSong(edited, user.getUserId());
                        getTableView().getItems().set(getIndex(), edited);
                        statusLabel.setStyle(Theme.LABEL_SUCCESS);
                        statusLabel.setText("Updated: " + edited.getTrackName());
                    }
                });
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : edit);
            }
        });

        TableColumn<Song, Void> deleteCol    = new TableColumn<>("Action");
        deleteCol.setMaxWidth(80);
        deleteCol.setCellFactory(col -> new TableCell<>() {
            final Button del = new Button("Delete");
            { del.setStyle(Theme.BTN_DANGER);
                del.setOnAction(e -> {
                    Song s = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete song '" + s.getTrackName() + "'?", ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText("Confirm delete");
                    confirm.showAndWait();
                    if (confirm.getResult() != ButtonType.YES) return;
                    songService.deleteSong(s.getSongId());
                    getTableView().getItems().remove(s);
                    statusLabel.setStyle(Theme.LABEL_SUCCESS);
                    statusLabel.setText("Deleted: " + s.getTrackName());
                }); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : del);
            }
        });

        table.getColumns().addAll(nameCol, artistCol, albumCol, lengthCol, moodCol, playsCol, updateCol, deleteCol);
        table.getItems().addAll(all);

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) return;
            table.getItems().setAll(songService.search(q));
        });
        searchField.textProperty().addListener((obs, old, text) -> {
            String q = text == null ? "" : text.trim();
            table.getItems().setAll(q.isEmpty() ? songService.getAllSongs() : songService.search(q));
        });
        clearBtn.setOnAction(e -> {
            searchField.clear();
            table.getItems().setAll(songService.getAllSongs());
        });
        searchField.setOnAction(e -> searchBtn.fire());
        
        importBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Import Songs from CSV");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            java.io.File file = fileChooser.showOpenDialog(AppContext.primaryStage);
            if (file != null) {
                try {
                    int count = songService.importFromCSV(file.getAbsolutePath(), user.getUserId());
                    statusLabel.setStyle(Theme.LABEL_SUCCESS);
                    statusLabel.setText("Successfully imported " + count + " songs to your library!");
                    table.getItems().setAll(songService.getAllSongs());
                    
                    // Update summary
                    List<Song> updated = songService.getAllSongs();
                    long updatedUniqueAlbums = updated.stream()
                        .map(s -> s.getAlbumName().trim())
                        .filter(a -> !a.isEmpty())
                        .distinct()
                        .count();
                    
                    summary.getChildren().setAll(
                        statPill("Total Songs", String.valueOf(updated.size())),
                        statPill("Unique Artists", String.valueOf(
                            updated.stream().flatMap(s -> java.util.Arrays.stream(s.getArtists().split("\\|")))
                                .map(String::trim).distinct().count())),
                        statPill("Unique Albums", String.valueOf(updatedUniqueAlbums))
                    );
                } catch (Exception ex) {
                    statusLabel.setStyle(Theme.LABEL_ERROR);
                    statusLabel.setText("Import failed: " + ex.getMessage());
                }
            }
        });

        content.getChildren().addAll(title, summary, searchRow, statusLabel, table);
        root.setCenter(content);
        return new Scene(root, 1100, 720);
    }

    private HBox statPill(String label, String value) {
        HBox pill = new HBox(10);
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setPadding(new Insets(12, 20, 12, 20));
        pill.setStyle(Theme.CARD_ELEVATED);
        Label l = new Label(label + ":");
        l.setStyle(Theme.LABEL_SUBTITLE);
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: " + Theme.ACCENT + "; -fx-font-size: 15px; -fx-font-weight: bold;");
        pill.getChildren().addAll(l, v);
        return pill;
    }

    private Song showEditDialog(Song song) {
        Dialog<Song> dialog = new Dialog<>();
        dialog.setTitle("Update Song");
        dialog.setHeaderText("Edit song details");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField trackField = new TextField(song.getTrackName());
        TextField artistField = new TextField(song.getArtists());
        TextField albumField = new TextField(song.getAlbumName());
        TextField lengthField = new TextField(song.getLength());
        TextField genreField = new TextField(song.getGenres());
        
        // Mood dropdown with predefined values
        ComboBox<String> moodCombo = new ComboBox<>();
        moodCombo.getItems().addAll("RELAXED", "HAPPY", "MELANCHOLIC", "ENERGETIC", "FOCUSED");
        moodCombo.setValue(song.getMood().toUpperCase());
        moodCombo.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        
        TextField linkField = new TextField(song.getLink());
        TextField playsField = new TextField(String.valueOf(song.getPlayCount()));

        grid.addRow(0, new Label("Track"), trackField);
        grid.addRow(1, new Label("Artists"), artistField);
        grid.addRow(2, new Label("Album"), albumField);
        grid.addRow(3, new Label("Length"), lengthField);
        grid.addRow(4, new Label("Genres"), genreField);
        grid.addRow(5, new Label("Mood"), moodCombo);
        grid.addRow(6, new Label("Spotify URL"), linkField);
        grid.addRow(7, new Label("Play Count"), playsField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            int plays = parseInt(playsField.getText());
            if (plays < 0) plays = 0;
            return new Song(
                    song.getSongId(),
                    trackField.getText().trim(),
                    albumField.getText().trim(),
                    artistField.getText().trim(),
                    lengthField.getText().trim(),
                    genreField.getText().trim(),
                    moodCombo.getValue(),
                    linkField.getText().trim(),
                    plays
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    // Helper method to parse integer safely
    private static int parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}