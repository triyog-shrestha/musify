// AdminHomeScreen.java
// Admin home screen - View and manage all LISTENER users.

package ui;

import exception.AuthException;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.User;
import service.AdminService;

import java.util.List;
import java.util.stream.Collectors;

public class AdminHomeScreen {

    private final User user;
    private final AdminService adminService = new AdminService();

    public AdminHomeScreen(User user) {
        this.user = user;
    }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.SCREEN_BG);
        root.setLeft(new Sidebar("Home", user));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_DARK + ";");

        VBox content = new VBox(28);
        content.setPadding(new Insets(40));

        Label title = new Label("Admin Dashboard");
        title.setStyle(Theme.LABEL_TITLE);
        Label sub = new Label("Manage listener accounts");
        sub.setStyle(Theme.LABEL_SUBTITLE);

        // User management card
        VBox userCard = new VBox(16);
        userCard.setStyle(Theme.CARD);
        userCard.setPadding(new Insets(24));

        Label userTitle = new Label("Listener Management");
        userTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        // User table (only LISTENER users)
        TableView<User> userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setMaxHeight(300);
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

        TableColumn<User, Void> updateCol = new TableColumn<>("Edit");
        updateCol.setMaxWidth(70);
        updateCol.setCellFactory(col -> new TableCell<>() {
            final Button edit = new Button("Edit");
            {
                edit.setStyle(Theme.BTN_GHOST);
                edit.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    User updated = showEditDialog(u);
                    if (updated != null) {
                        try {
                            adminService.updateUser(updated);
                            userTable.getItems().setAll(getListeners());
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

        TableColumn<User, Void> delCol = new TableColumn<>("Delete");
        delCol.setMaxWidth(80);
        delCol.setCellFactory(col -> new TableCell<>() {
            final Button del = new Button("Delete");
            {
                del.setStyle(Theme.BTN_DANGER);
                del.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText("Delete User");
                    confirm.setContentText("Are you sure you want to delete \"" + u.getUsername() + "\"?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            adminService.deleteUser(u.getUserId());
                            getTableView().getItems().remove(u);
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : del);
            }
        });

        userTable.getColumns().addAll(idCol, nameCol, emailCol, dateCol, updateCol, delCol);
        userTable.getItems().addAll(getListeners());

        // Create user form
        Label createTitle = new Label("Create New Listener");
        createTitle.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox createRow = new HBox(12);
        createRow.setAlignment(Pos.CENTER_LEFT);

        TextField newName = new TextField();
        newName.setPromptText("Username");
        newName.setStyle(Theme.FIELD);
        newName.setPrefWidth(160);
        Theme.focusField(newName);

        TextField newEmail = new TextField();
        newEmail.setPromptText("Email");
        newEmail.setStyle(Theme.FIELD);
        newEmail.setPrefWidth(200);
        Theme.focusField(newEmail);

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("Password");
        newPass.setStyle(Theme.FIELD);
        newPass.setPrefWidth(160);
        Theme.focusField(newPass);

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
                userTable.getItems().setAll(getListeners());
                newName.clear();
                newEmail.clear();
                newPass.clear();
            } catch (AuthException ex) {
                createResult.setStyle(Theme.LABEL_ERROR);
                createResult.setText(ex.getMessage());
            }
        });

        userCard.getChildren().addAll(userTitle, userTable, createTitle, createRow, createResult);

        content.getChildren().addAll(title, sub, userCard);
        scroll.setContent(content);
        root.setCenter(scroll);
        return new Scene(root, 1100, 720);
    }

    private List<User> getListeners() {
        return adminService.getAllUsers().stream()
                .filter(u -> !(u instanceof model.Admin))
                .collect(Collectors.toList());
    }

    private User showEditDialog(User u) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Update user details");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField nameField = new TextField(u.getUsername());
        TextField emailField = new TextField(u.getEmail());

        grid.addRow(0, new Label("Username"), nameField);
        grid.addRow(1, new Label("Email"), emailField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            return new User(
                    u.getUserId(),
                    nameField.getText().trim(),
                    u.getPassword(),
                    emailField.getText().trim(),
                    u.getCreatedAt()
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
