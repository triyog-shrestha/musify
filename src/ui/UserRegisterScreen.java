// UserRegisterScreen.java
// Registration screen for regular users

package ui;

import exception.AuthException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.User;
import service.AuthService;

public class UserRegisterScreen {

    private final AuthService authService = new AuthService();

    public Scene getScene() {

        // left branding panel
        VBox left = new VBox();
        left.setStyle("-fx-background-color: " + Theme.BG_CARD + ";");
        left.setMinWidth(420);
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(60));

        Label logo = new Label("MUSIFY");
        logo.setStyle(
                "-fx-text-fill: " + Theme.ACCENT + ";" +
                        "-fx-font-size: 42px;" +
                        "-fx-font-weight: bold;");

        Label tagline = new Label("User Registration\n\nCreate your account and start\ntracking your music taste.");
        tagline.setStyle(
                "-fx-text-fill: " + Theme.TEXT_MUTED + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-alignment: center;");
        tagline.setWrapText(true);

        left.getChildren().addAll(logo, tagline);

        // right form panel
        VBox right = new VBox(16);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(60, 80, 60, 80));
        right.setStyle("-fx-background-color: " + Theme.BG_DARK + ";");

        Label title = new Label("Create user account");
        title.setStyle(Theme.LABEL_TITLE);

        Label subtitle = new Label("Join Musify and discover your listening habits");
        subtitle.setStyle(Theme.LABEL_SUBTITLE);

        VBox form = new VBox(12);
        form.setMaxWidth(360);

        Label nameLabel = new Label("USERNAME");
        nameLabel.setStyle(Theme.LABEL_SECTION);
        TextField nameField = new TextField();
        nameField.setPromptText("Choose a username");
        nameField.setStyle(Theme.FIELD);
        nameField.setMaxWidth(Double.MAX_VALUE);
        Theme.focusField(nameField);

        Label emailLabel = new Label("EMAIL");
        emailLabel.setStyle(Theme.LABEL_SECTION);
        TextField emailField = new TextField();
        emailField.setPromptText("you@example.com");
        emailField.setStyle(Theme.FIELD);
        emailField.setMaxWidth(Double.MAX_VALUE);
        Theme.focusField(emailField);

        Label passLabel = new Label("PASSWORD");
        passLabel.setStyle(Theme.LABEL_SECTION);
        PasswordField passField = new PasswordField();
        passField.setPromptText("Choose a password (min 8 characters)");
        passField.setStyle(Theme.FIELD);
        passField.setMaxWidth(Double.MAX_VALUE);
        Theme.focusField(passField);

        Label errorLabel = new Label("");
        errorLabel.setStyle(Theme.LABEL_ERROR);
        errorLabel.setWrapText(true);

        Button registerBtn = new Button("Create Account");
        registerBtn.setStyle(Theme.BTN_PRIMARY);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setMinHeight(42);
        Theme.hoverPrimary(registerBtn);

        Label loginLink = new Label("Already have an account? Sign in");
        loginLink.setStyle(Theme.LABEL_ACCENT);
        loginLink.setOnMouseClicked(e ->
                AppContext.primaryStage.setScene(new UserLoginScreen().getScene()));

        Label backLink = new Label("← Back to role selection");
        backLink.setStyle(Theme.LABEL_SUBTITLE + "-fx-cursor: hand;");
        backLink.setOnMouseClicked(e ->
                AppContext.primaryStage.setScene(new RoleSelectionScreen().getScene()));

        registerBtn.setOnAction(e -> {
            String username = nameField.getText().trim();
            String email    = emailField.getText().trim();
            String pass     = passField.getText();
            try {
                User user = authService.register(username, email, pass);
                AppContext.primaryStage.setScene(new HomeScreen(user).getScene());
            } catch (AuthException ex) {
                errorLabel.setText(ex.getMessage());
            } catch (Exception ex) {
                errorLabel.setText("Unexpected error while creating account.");
            }
        });

        form.getChildren().addAll(
                nameLabel, nameField,
                emailLabel, emailField,
                passLabel, passField,
                errorLabel,
                registerBtn,
                loginLink,
                backLink
        );

        right.getChildren().addAll(title, subtitle, form);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox root = new HBox(left, right);
        root.setStyle("-fx-background-color: " + Theme.BG_DARK + ";");

        return new Scene(root, 1100, 720);
    }
}
