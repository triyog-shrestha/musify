// HomeScreen.java
// Landing screen after login.
// Lets the user update play counts by searching songs.

package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;
import model.Song;
import model.User;
import service.SongService;

import java.util.List;

public class HomeScreen {

    private final User        user;
    private final SongService songService;

    public HomeScreen(User user) { 
        this.user = user; 
        this.songService = new SongService(user.getUserId());
    }

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
        Label sub = new Label("Search and update play counts for your songs");
        sub.setStyle(Theme.LABEL_SUBTITLE);

        // search bar
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
        Button showAllBtn = new Button("Show All");
        showAllBtn.setStyle(Theme.BTN_GHOST);
        showAllBtn.setMinHeight(40);
        Theme.hoverGhost(showAllBtn);
        searchRow.getChildren().addAll(searchField, searchBtn, showAllBtn);

        // results table (smaller to fit without scrolling)
        TableView<Song> table = buildSongTable();
        table.setMaxHeight(280);
        HomeScreen.styleTable(table);

        Label statusLabel = new Label("");
        statusLabel.setStyle(Theme.LABEL_SUBTITLE);

        HBox updateRow = new HBox(12);
        updateRow.setAlignment(Pos.CENTER_LEFT);

        Label selectedLabel = new Label("Selected: none");
        selectedLabel.setStyle(Theme.LABEL_SUBTITLE);

        Label addLabel = new Label("Plays to add:");
        addLabel.setStyle(Theme.LABEL_SUBTITLE);
        TextField playsField = new TextField();
        playsField.setPromptText("e.g. 5");
        playsField.setStyle(Theme.FIELD);
        playsField.setPrefWidth(80);
        Theme.focusField(playsField);

        Button updateBtn = new Button("Update Play Count");
        updateBtn.setStyle(Theme.BTN_PRIMARY);
        updateBtn.setMinHeight(40);
        Theme.hoverPrimary(updateBtn);

        updateRow.getChildren().addAll(selectedLabel, addLabel, playsField, updateBtn);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                selectedLabel.setText("Selected: none");
            } else {
                selectedLabel.setText("Selected: " + selected.getTrackName());
            }
        });

        // Live search as user types
        searchField.textProperty().addListener((obs, old, text) -> {
            String q = text == null ? "" : text.trim();
            if (q.isEmpty()) {
                table.getItems().clear();
                statusLabel.setText("");
            } else {
                List<Song> results = songService.search(q);
                table.getItems().setAll(results);
                if (results.isEmpty()) {
                    statusLabel.setStyle(Theme.LABEL_ERROR);
                    statusLabel.setText("No songs found matching '" + q + "'");
                } else {
                    statusLabel.setStyle(Theme.LABEL_SUCCESS);
                    statusLabel.setText("Found " + results.size() + " song(s)");
                }
            }
        });

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) return;
            List<Song> results = songService.search(q);
            table.getItems().setAll(results);
            if (results.isEmpty()) {
                statusLabel.setStyle(Theme.LABEL_ERROR);
                statusLabel.setText("No songs found matching '" + q + "'");
            } else {
                statusLabel.setStyle(Theme.LABEL_SUCCESS);
                statusLabel.setText("Found " + results.size() + " song(s)");
            }
        });

        showAllBtn.setOnAction(e -> {
            List<Song> allSongs = songService.getAllSongs();
            table.getItems().setAll(allSongs);
            searchField.clear();
            statusLabel.setStyle(Theme.LABEL_SUCCESS);
            statusLabel.setText("Showing all " + allSongs.size() + " songs");
        });

        searchField.setOnAction(e -> searchBtn.fire());

        updateBtn.setOnAction(e -> {
            int toAdd = parseInt(playsField.getText());
            Song selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { 
                statusLabel.setStyle(Theme.LABEL_ERROR); 
                statusLabel.setText("Please select a song from the table first."); 
                return; 
            }
            if (toAdd <= 0) { 
                statusLabel.setStyle(Theme.LABEL_ERROR); 
                statusLabel.setText("Please enter a positive number of plays to add."); 
                return; 
            }
            int newCount = selected.getPlayCount() + toAdd;
            songService.setPlayCount(selected.getSongId(), user.getUserId(), newCount);
            statusLabel.setStyle(Theme.LABEL_SUCCESS);
            statusLabel.setText("✓ Updated " + selected.getTrackName() + ": " + selected.getPlayCount() + " + " + toAdd + " = " + newCount + " plays");
            
            // Refresh the table
            String currentSearch = searchField.getText().trim();
            if (currentSearch.isEmpty()) {
                table.getItems().setAll(songService.getAllSongs());
            } else {
                table.getItems().setAll(songService.search(currentSearch));
            }
            table.getSelectionModel().clearSelection();
            selectedLabel.setText("Selected: none");
            playsField.clear();
        });

        content.getChildren().addAll(title, sub, searchRow, statusLabel, table, updateRow);
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

        // Clickable track name column that opens Spotify link
        TableColumn<Song, String> nameCol = new TableColumn<>("Track Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTrackName()));
        nameCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    javafx.scene.control.Hyperlink link = new javafx.scene.control.Hyperlink(item);
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

        TableColumn<Song, String> artistCol = new TableColumn<>("Artists");
        artistCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getArtists().replace("|", ", ")));
        artistCol.setStyle("-fx-text-fill: white;");

        TableColumn<Song, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getAlbumName()));
        albumCol.setStyle("-fx-text-fill: white;");

        TableColumn<Song, String> lengthCol = new TableColumn<>("Length");
        lengthCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getLength()));
        lengthCol.setMaxWidth(70);
        lengthCol.setStyle("-fx-text-fill: white;");

        TableColumn<Song, String> moodCol = new TableColumn<>("Mood");
        moodCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMood()));
        moodCol.setMaxWidth(90);
        moodCol.setStyle("-fx-text-fill: white;");

        TableColumn<Song, Integer> playsCol = new TableColumn<>("Plays");
        playsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getPlayCount()).asObject());
        playsCol.setMaxWidth(65);
        playsCol.setStyle("-fx-text-fill: white;");

        table.getColumns().addAll(nameCol, artistCol, albumCol, lengthCol, moodCol, playsCol);
        styleTable(table);
        
        return table;
    }

    static <T> void styleTable(TableView<T> table) {
        String css = Theme.TABLE +
                "-fx-control-inner-background: " + Theme.BG_CARD + ";";
        table.setStyle(css);
        
        table.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setStyle("-fx-text-fill: white;");
            row.hoverProperty().addListener((obs, old, isHover) -> {
                if (row.isEmpty()) {
                    row.setStyle("-fx-text-fill: white;");
                } else if (isHover) {
                    row.setStyle("-fx-background-color: " + Theme.BG_ELEVATED + "; -fx-text-fill: white;");
                } else {
                    row.setStyle("-fx-text-fill: white;");
                }
            });
            return row;
        });
        
        // Style headers after table is rendered
        table.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    // Style header labels
                    table.lookupAll(".column-header .label").forEach(node -> {
                        ((Label) node).setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    });
                    // Style header backgrounds with darker color
                    table.lookupAll(".column-header").forEach(node -> {
                        node.setStyle("-fx-background-color: #1a1a2e;");
                    });
                    table.lookupAll(".column-header-background").forEach(node -> {
                        node.setStyle("-fx-background-color: #1a1a2e;");
                    });
                });
            }
        });
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