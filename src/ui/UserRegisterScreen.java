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
        // Left branding panel
        VBox left = Theme.brandingPanel("User Registration\n\nCreate your account and start\ntracking your music taste.");

        // Right form panel
        VBox right = new VBox(16);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(60, 80, 60, 80));
        right.setStyle("-fx-background-color: " + Theme.BG_DARK + ";");

        Label title = new Label("Create user account");
        title.setStyle(Theme.LABEL_TITLE);
        Label subtitle = new Label("Join Musify and discover your listening habits");
        subtitle.setStyle(Theme.LABEL_SUBTITLE);

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passField = new PasswordField();
        Label errorLabel = new Label("");
        errorLabel.setStyle(Theme.LABEL_ERROR);
        errorLabel.setWrapText(true);

        Button registerBtn = Theme.primaryButton("Create Account");
        registerBtn.setOnAction(e -> {
            try {
                User user = authService.register(nameField.getText().trim(), 
                    emailField.getText().trim(), passField.getText());
                AppContext.primaryStage.setScene(new HomeScreen(user).getScene());
            } catch (AuthException ex) {
                errorLabel.setText(ex.getMessage());
            } catch (Exception ex) {
                errorLabel.setText("Unexpected error while creating account.");
            }
        });

        VBox form = new VBox(12,
            Theme.formField("USERNAME", nameField, "Choose a username"),
            Theme.formField("EMAIL", emailField, "you@example.com"),
            Theme.formField("PASSWORD", passField, "Choose a password (min 8 characters)"),
            errorLabel, registerBtn,
            Theme.linkLabel("Already have an account? Log in", 
                () -> AppContext.primaryStage.setScene(new UserLoginScreen().getScene())),
            Theme.backLink("Back to role selection", 
                () -> AppContext.primaryStage.setScene(new RoleSelectionScreen().getScene()))
        );
        form.setMaxWidth(360);

        right.getChildren().addAll(title, subtitle, form);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox root = new HBox(left, right);
        root.setStyle("-fx-background-color: " + Theme.BG_DARK + ";");
        return new Scene(root, 1100, 720);
    }
}
