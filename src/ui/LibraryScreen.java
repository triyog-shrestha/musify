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
import util.Store;

import java.util.List;

public class LibraryScreen {

    private final User        user;
    private final SongService songService = new SongService();

    public LibraryScreen(User user) { this.user = user; }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.SCREEN_BG);
        root.setLeft(new Sidebar("Library", user));

        VBox content = new VBox(24);
        content.setPadding(new Insets(40));

        Label title = new Label("Song Library");
        title.setStyle(Theme.LABEL_TITLE);

        // summary row
        List<Song> all = songService.getAllSongs();
        HBox summary = new HBox(16);
        summary.getChildren().addAll(
                statPill("Total Songs", String.valueOf(all.size())),
                statPill("Unique Artists", String.valueOf(
                        all.stream().flatMap(s -> java.util.Arrays.stream(s.getArtists().split("\\|")))
                                .map(String::trim).distinct().count()))
        );

        // search bar
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
        searchRow.getChildren().addAll(searchField, searchBtn, clearBtn);

        Label statusLabel = new Label("");
        statusLabel.setStyle(Theme.LABEL_ERROR);

        // table
        TableView<Song> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        HomeScreen.styleTable(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Song, Integer> idCol     = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getSongId()).asObject());
        idCol.setMaxWidth(55);

        TableColumn<Song, String> nameCol    = new TableColumn<>("Track Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrackName()));

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

        TableColumn<Song, Void> deleteCol    = new TableColumn<>("Action");
        deleteCol.setMaxWidth(80);
        deleteCol.setCellFactory(col -> new TableCell<>() {
            final Button del = new Button("Delete");
            { del.setStyle(Theme.BTN_DANGER);
                del.setOnAction(e -> {
                    Song s = getTableView().getItems().get(getIndex());
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

        table.getColumns().addAll(idCol, nameCol, artistCol, albumCol, lengthCol, moodCol, playsCol, deleteCol);
        table.getItems().addAll(all);

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) return;
            table.getItems().setAll(songService.search(q));
        });
        clearBtn.setOnAction(e -> {
            searchField.clear();
            table.getItems().setAll(songService.getAllSongs());
        });
        searchField.setOnAction(e -> searchBtn.fire());

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
}