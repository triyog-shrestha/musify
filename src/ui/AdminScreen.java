// AdminScreen.java
// Admin only screen. Shows user management and app wide statistics.

package ui;

import exception.AuthException;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Recommendation;
import model.User;
import service.AdminService;
import service.AuthService;

import java.util.List;

public class AdminScreen {

    private final User         user;
    private final AdminService adminService = new AdminService();
    private final AuthService  authService  = new AuthService();

    public AdminScreen(User user) { this.user = user; }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.SCREEN_BG);
        root.setLeft(new Sidebar("Admin Panel", user));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_DARK + ";");

        VBox content = new VBox(28);
        content.setPadding(new Insets(40));

        Label title = new Label("Admin Panel");
        title.setStyle(Theme.LABEL_TITLE);
        Label sub = new Label("Manage users and view system wide statistics");
        sub.setStyle(Theme.LABEL_SUBTITLE);

        // user management card
        VBox userCard = new VBox(16);
        userCard.setStyle(Theme.CARD);
        userCard.setPadding(new Insets(24));

        Label userTitle = new Label("User Management");
        userTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        // user table
        TableView<User> userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setMaxHeight(250);
        HomeScreen.styleTable(userTable);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getUserId()).asObject());
        idCol.setMaxWidth(55);

        TableColumn<User, String> nameCol = new TableColumn<>("Username");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<User, String> dateCol = new TableColumn<>("Joined");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCreatedAt().toLocalDate().toString()));

        TableColumn<User, Void> delCol = new TableColumn<>("Action");
        delCol.setMaxWidth(80);
        delCol.setCellFactory(col -> new TableCell<>() {
            final Button del = new Button("Delete");
            { del.setStyle(Theme.BTN_DANGER);
                del.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    adminService.deleteUser(u.getUserId());
                    getTableView().getItems().remove(u);
                }); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : del);
            }
        });

        userTable.getColumns().addAll(idCol, nameCol, emailCol, dateCol, delCol);
        userTable.getItems().addAll(adminService.getAllUsers());

        // create user form
        Label createTitle = new Label("Create New User");
        createTitle.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox createRow = new HBox(12);
        createRow.setAlignment(Pos.CENTER_LEFT);

        TextField newName  = new TextField(); newName.setPromptText("Username"); newName.setStyle(Theme.FIELD); newName.setPrefWidth(160); Theme.focusField(newName);
        TextField newEmail = new TextField(); newEmail.setPromptText("Email");    newEmail.setStyle(Theme.FIELD); newEmail.setPrefWidth(200); Theme.focusField(newEmail);
        PasswordField newPass = new PasswordField(); newPass.setPromptText("Password"); newPass.setStyle(Theme.FIELD); newPass.setPrefWidth(160); Theme.focusField(newPass);
        Button createBtn = new Button("Create");
        createBtn.setStyle(Theme.BTN_PRIMARY);
        createBtn.setMinHeight(40);
        Theme.hoverPrimary(createBtn);

        Label createResult = new Label("");

        createRow.getChildren().addAll(newName, newEmail, newPass, createBtn);

        createBtn.setOnAction(e -> {
            try {
                adminService.createUser(newName.getText(), newEmail.getText(), newPass.getText());
                createResult.setStyle(Theme.LABEL_SUCCESS);
                createResult.setText("User created successfully.");
                userTable.getItems().setAll(adminService.getAllUsers());
                newName.clear(); newEmail.clear(); newPass.clear();
            } catch (AuthException ex) {
                createResult.setStyle(Theme.LABEL_ERROR);
                createResult.setText(ex.getMessage());
            }
        });

        userCard.getChildren().addAll(userTitle, userTable, createTitle, createRow, createResult);

        // recommendations management card
        VBox recsCard = new VBox(16);
        recsCard.setStyle(Theme.CARD);
        recsCard.setPadding(new Insets(24));

        Label recsTitle = new Label("Recommendations Management");
        recsTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label recsSub = new Label("Import songs to add to the recommendations pool");
        recsSub.setStyle(Theme.LABEL_SUBTITLE);

        HBox importRow = new HBox(12);
        importRow.setAlignment(Pos.CENTER_LEFT);
        Button importRecsBtn = new Button("Import CSV to Recommendations");
        importRecsBtn.setStyle(Theme.BTN_PRIMARY);
        importRecsBtn.setMinHeight(40);
        Theme.hoverPrimary(importRecsBtn);
        Label importResult = new Label("");
        importRow.getChildren().addAll(importRecsBtn, importResult);

        List<Recommendation> currentRecs = adminService.getAllRecommendations();
        Label recsCount = new Label("Current recommendations in pool: " + currentRecs.size());
        recsCount.setStyle(Theme.LABEL_SUBTITLE);

        importRecsBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Import Recommendations from CSV");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            java.io.File file = fileChooser.showOpenDialog(AppContext.primaryStage);
            if (file != null) {
                try {
                    service.RecommendationService recService = new service.RecommendationService();
                    int count = recService.importFromCSV(file.getAbsolutePath());
                    importResult.setStyle(Theme.LABEL_SUCCESS);
                    importResult.setText("Successfully imported " + count + " recommendations!");
                    
                    // Update count
                    List<Recommendation> updatedRecs = adminService.getAllRecommendations();
                    recsCount.setText("Current recommendations in pool: " + updatedRecs.size());
                } catch (Exception ex) {
                    importResult.setStyle(Theme.LABEL_ERROR);
                    importResult.setText("Import failed: " + ex.getMessage());
                }
            }
        });

        recsCard.getChildren().addAll(recsTitle, recsSub, importRow, recsCount);

        // app stats card
        VBox statsCard = new VBox(16);
        statsCard.setStyle(Theme.CARD);
        statsCard.setPadding(new Insets(24));

        Label statsTitle = new Label("App Statistics");
        statsTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        HBox statsRow = new HBox(16);
        List<User> allUsers = adminService.getAllUsers();
        statsRow.getChildren().addAll(
                statPill("Total Users", String.valueOf(allUsers.size()))
        );

        statsCard.getChildren().addAll(statsTitle, statsRow);

        content.getChildren().addAll(title, sub, userCard, recsCard, statsCard);
        scroll.setContent(content);
        root.setCenter(scroll);
        return new Scene(root, 1100, 720);
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