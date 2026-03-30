// AdminRecsScreen.java
// Admin screen to manage recommendations - Import CSV, view, update, delete recommendations.

package ui;

import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Recommendation;
import model.User;
import service.RecommendationService;
import dao.RecommendationDAO;

import java.util.List;

public class AdminRecsScreen {

    private final User user;
    private final RecommendationService recService = new RecommendationService();
    private final RecommendationDAO recDAO = new RecommendationDAO();

    public AdminRecsScreen(User user) {
        this.user = user;
    }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.SCREEN_BG);
        root.setLeft(new Sidebar("Recommendations", user));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_DARK + ";");

        VBox content = new VBox(28);
        content.setPadding(new Insets(40));

        Label title = new Label("Recommendations Library");
        title.setStyle(Theme.LABEL_TITLE);
        Label sub = new Label("Import and manage recommendation songs");
        sub.setStyle(Theme.LABEL_SUBTITLE);

        // Import section
        HBox importRow = new HBox(12);
        importRow.setAlignment(Pos.CENTER_LEFT);
        Button importBtn = new Button("Import CSV");
        importBtn.setStyle(Theme.BTN_PRIMARY);
        importBtn.setMinHeight(40);
        Theme.hoverPrimary(importBtn);
        Label importResult = new Label("");
        importRow.getChildren().addAll(importBtn, importResult);

        // Summary
        List<Recommendation> allRecs = recDAO.getAll();
        Label countLabel = new Label("Total recommendations: " + allRecs.size());
        countLabel.setStyle(Theme.LABEL_SUBTITLE);

        // Recommendations table
        TableView<Recommendation> table = buildTable();
        table.getItems().addAll(allRecs);

        importBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Import Recommendations from CSV");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            java.io.File file = fileChooser.showOpenDialog(AppContext.primaryStage);
            if (file != null) {
                try {
                    int count = recService.importFromCSV(file.getAbsolutePath());
                    importResult.setStyle(Theme.LABEL_SUCCESS);
                    importResult.setText("Imported " + count + " recommendations!");
                    // Refresh table
                    table.getItems().setAll(recDAO.getAll());
                    countLabel.setText("Total recommendations: " + recDAO.getAll().size());
                } catch (Exception ex) {
                    importResult.setStyle(Theme.LABEL_ERROR);
                    importResult.setText("Import failed: " + ex.getMessage());
                }
            }
        });

        content.getChildren().addAll(title, sub, importRow, countLabel, table);
        scroll.setContent(content);
        root.setCenter(scroll);
        return new Scene(root, 1100, 720);
    }

    private TableView<Recommendation> buildTable() {
        TableView<Recommendation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxHeight(450);
        HomeScreen.styleTable(table);

        TableColumn<Recommendation, String> nameCol = new TableColumn<>("Song");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrackName()));
        nameCol.setStyle("-fx-text-fill: white;");

        TableColumn<Recommendation, String> artistCol = new TableColumn<>("Artists");
        artistCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getArtists().replace("|", ", ")));
        artistCol.setStyle("-fx-text-fill: white;");

        TableColumn<Recommendation, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlbumName()));
        albumCol.setMaxWidth(150);
        albumCol.setStyle("-fx-text-fill: white;");

        TableColumn<Recommendation, String> genreCol = new TableColumn<>("Genres");
        genreCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenres().replace("|", ", ")));
        genreCol.setMaxWidth(120);
        genreCol.setStyle("-fx-text-fill: white;");

        TableColumn<Recommendation, String> moodCol = new TableColumn<>("Mood");
        moodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMood()));
        moodCol.setMaxWidth(90);
        moodCol.setStyle("-fx-text-fill: white;");

        TableColumn<Recommendation, Void> updateCol = new TableColumn<>("Edit");
        updateCol.setMaxWidth(70);
        updateCol.setCellFactory(col -> new TableCell<>() {
            final Button edit = new Button("Edit");
            {
                edit.setStyle(Theme.BTN_GHOST);
                edit.setOnAction(e -> {
                    Recommendation rec = getTableView().getItems().get(getIndex());
                    Recommendation updated = showEditDialog(rec);
                    if (updated != null) {
                        try {
                            recDAO.update(updated);
                            getTableView().getItems().setAll(recDAO.getAll());
                        } catch (Exception ex) {
                            showError("Update failed: " + ex.getMessage());
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : edit);
            }
        });

        TableColumn<Recommendation, Void> delCol = new TableColumn<>("Delete");
        delCol.setMaxWidth(80);
        delCol.setCellFactory(col -> new TableCell<>() {
            final Button del = new Button("Delete");
            {
                del.setStyle(Theme.BTN_DANGER);
                del.setOnAction(e -> {
                    Recommendation rec = getTableView().getItems().get(getIndex());
                    recDAO.delete(rec.getRecId());
                    getTableView().getItems().remove(rec);
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : del);
            }
        });

        table.getColumns().addAll(nameCol, artistCol, albumCol, genreCol, moodCol, updateCol, delCol);
        return table;
    }

    private Recommendation showEditDialog(Recommendation rec) {
        Dialog<Recommendation> dialog = new Dialog<>();
        dialog.setTitle("Edit Recommendation");
        dialog.setHeaderText("Update recommendation details");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField trackField = new TextField(rec.getTrackName());
        TextField artistField = new TextField(rec.getArtists());
        TextField albumField = new TextField(rec.getAlbumName());
        TextField lengthField = new TextField(rec.getLength());
        TextField genreField = new TextField(rec.getGenres());

        // Mood dropdown
        ComboBox<String> moodCombo = new ComboBox<>();
        moodCombo.getItems().addAll("RELAXED", "HAPPY", "MELANCHOLIC", "ENERGETIC", "FOCUSED");
        moodCombo.setValue(rec.getMood().toUpperCase());
        moodCombo.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        TextField linkField = new TextField(rec.getLink());

        grid.addRow(0, new Label("Track"), trackField);
        grid.addRow(1, new Label("Artists"), artistField);
        grid.addRow(2, new Label("Album"), albumField);
        grid.addRow(3, new Label("Length"), lengthField);
        grid.addRow(4, new Label("Genres"), genreField);
        grid.addRow(5, new Label("Mood"), moodCombo);
        grid.addRow(6, new Label("Spotify URL"), linkField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            return new Recommendation(
                    rec.getRecId(),
                    trackField.getText().trim(),
                    albumField.getText().trim(),
                    artistField.getText().trim(),
                    lengthField.getText().trim(),
                    genreField.getText().trim(),
                    moodCombo.getValue(),
                    linkField.getText().trim()
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
