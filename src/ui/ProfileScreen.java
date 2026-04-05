// ProfileScreen.java
// Displays user details and allows updating username, email and password.

package ui;

import exception.AuthException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.User;
import service.AuthService;

public class ProfileScreen {

    private final User        user;
    private final AuthService authService = new AuthService();

    public ProfileScreen(User user) { this.user = user; }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.SCREEN_BG);
        root.setLeft(new Sidebar("Profile", user));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_DARK + ";");

        VBox content = new VBox(28);
        content.setPadding(new Insets(40));
        content.setMaxWidth(700);

        Label title = new Label("Profile");
        title.setStyle(Theme.LABEL_TITLE);

        // avatar + info card
        VBox infoCard = new VBox(16);
        infoCard.setStyle(Theme.CARD);
        infoCard.setPadding(new Insets(28));

        // avatar circle
        HBox avatarRow = new HBox(20);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label(user.getUsername().substring(0, 1).toUpperCase());
        avatar.setStyle(
                "-fx-background-color: " + Theme.ACCENT + ";" +
                        "-fx-text-fill: #0d0d0d;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 50;" +
                        "-fx-min-width: 60;" +
                        "-fx-min-height: 60;" +
                        "-fx-max-width: 60;" +
                        "-fx-max-height: 60;" +
                        "-fx-alignment: center;");

        VBox userInfo = new VBox(4);
        Label nameDisplay = new Label(user.getUsername());
        nameDisplay.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label emailDisplay = new Label(user.getEmail());
        emailDisplay.setStyle(Theme.LABEL_SUBTITLE);
        
        // Show role label for admins
        boolean isAdmin = user instanceof model.Admin;
        if (isAdmin) {
            Label roleDisplay = new Label("Administrator");
            roleDisplay.setStyle("-fx-text-fill: " + Theme.ACCENT + "; -fx-font-size: 12px; -fx-font-weight: bold;");
            userInfo.getChildren().addAll(nameDisplay, emailDisplay, roleDisplay);
        } else {
            userInfo.getChildren().addAll(nameDisplay, emailDisplay);
        }
        
        Label joinedDisplay = new Label("Member since " + user.getCreatedAt().toLocalDate().toString());
        joinedDisplay.setStyle(Theme.LABEL_SUBTITLE);
        userInfo.getChildren().add(joinedDisplay);
        avatarRow.getChildren().addAll(avatar, userInfo);
        infoCard.getChildren().add(avatarRow);

        // edit profile card
        VBox editCard = new VBox(16);
        editCard.setStyle(Theme.CARD);
        editCard.setPadding(new Insets(28));

        Label editTitle = new Label("Edit Profile");
        editTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label nameLabel = new Label("USERNAME");
        nameLabel.setStyle(Theme.LABEL_SECTION);
        TextField nameField = new TextField(user.getUsername());
        nameField.setStyle(Theme.FIELD);
        nameField.setMaxWidth(400);
        Theme.focusField(nameField);

        Label emailLabel = new Label("EMAIL");
        emailLabel.setStyle(Theme.LABEL_SECTION);
        TextField emailField = new TextField(user.getEmail());
        emailField.setStyle(Theme.FIELD);
        emailField.setMaxWidth(400);
        Theme.focusField(emailField);

        Label editResult = new Label("");
        editResult.setStyle(Theme.LABEL_SUCCESS);

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle(Theme.BTN_PRIMARY);
        saveBtn.setMinHeight(40);
        Theme.hoverPrimary(saveBtn);

        saveBtn.setOnAction(e -> {
            String newName  = nameField.getText().trim();
            String newEmail = emailField.getText().trim();
            try {
                authService.updateProfile(user, newName, newEmail);
                editResult.setStyle(Theme.LABEL_SUCCESS);
                editResult.setText("Profile updated successfully.");
                nameDisplay.setText(user.getUsername());
                emailDisplay.setText(user.getEmail());
                avatar.setText(user.getUsername().substring(0, 1).toUpperCase());
            } catch (AuthException ex) {
                editResult.setStyle(Theme.LABEL_ERROR);
                editResult.setText(ex.getMessage());
            }
        });

        editCard.getChildren().addAll(editTitle, nameLabel, nameField, emailLabel, emailField, editResult, saveBtn);

        // change password card
        VBox passCard = new VBox(16);
        passCard.setStyle(Theme.CARD);
        passCard.setPadding(new Insets(28));

        Label passTitle = new Label("Change Password");
        passTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label oldLabel = new Label("CURRENT PASSWORD");
        oldLabel.setStyle(Theme.LABEL_SECTION);
        PasswordField oldField = new PasswordField();
        oldField.setStyle(Theme.FIELD);
        oldField.setMaxWidth(400);
        Theme.focusField(oldField);

        Label newLabel = new Label("NEW PASSWORD");
        newLabel.setStyle(Theme.LABEL_SECTION);
        PasswordField newField = new PasswordField();
        newField.setStyle(Theme.FIELD);
        newField.setMaxWidth(400);
        Theme.focusField(newField);

        Label passResult = new Label("");
        Button changeBtn = new Button("Change Password");
        changeBtn.setStyle(Theme.BTN_PRIMARY);
        changeBtn.setMinHeight(40);
        Theme.hoverPrimary(changeBtn);

        changeBtn.setOnAction(e -> {
            try {
                authService.changePassword(user.getUserId(),
                        oldField.getText(), newField.getText());
                passResult.setStyle(Theme.LABEL_SUCCESS);
                passResult.setText("Password changed successfully.");
                oldField.clear(); newField.clear();
            } catch (AuthException ex) {
                passResult.setStyle(Theme.LABEL_ERROR);
                passResult.setText(ex.getMessage());
            }
        });

        passCard.getChildren().addAll(passTitle, oldLabel, oldField, newLabel, newField, passResult, changeBtn);

        content.getChildren().addAll(title, infoCard, editCard, passCard);
        scroll.setContent(content);
        root.setCenter(scroll);
        return new Scene(root, 1100, 720);
    }
}