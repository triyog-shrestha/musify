// LoginScreen.java
// First screen the user sees. Collects email and password.
// Routes to HomeScreen for regular users or AdminScreen for admins.

package ui;

import exception.AuthException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import model.Admin;
import model.User;
import service.AuthService;

public class LoginScreen {

    private final AuthService authService = new AuthService();

    public Scene getScene() {

        // left panel — branding
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

        Label tagline = new Label("Your personal music analytics system.\nTrack, analyse and discover.");
        tagline.setStyle(
                "-fx-text-fill: " + Theme.TEXT_MUTED + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-alignment: center;");
        tagline.setTextAlignment(TextAlignment.CENTER);
        tagline.setWrapText(true);

        // decorative stat blocks
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(40, 0, 0, 0));
        stats.getChildren().addAll(
                statBlock("Import", "Spotify CSV"),
                statBlock("Track", "Play counts"),
                statBlock("Discover", "New songs")
        );

        left.getChildren().addAll(logo, tagline, stats);

        // right panel — login form
        VBox right = new VBox(16);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(60, 80, 60, 80));
        right.setStyle("-fx-background-color: " + Theme.BG_DARK + ";");

        Label title = new Label("Welcome back");
        title.setStyle(Theme.LABEL_TITLE);

        Label subtitle = new Label("Sign in to your account");
        subtitle.setStyle(Theme.LABEL_SUBTITLE);

        VBox form = new VBox(12);
        form.setMaxWidth(360);

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
        passField.setPromptText("Enter your password");
        passField.setStyle(Theme.FIELD);
        passField.setMaxWidth(Double.MAX_VALUE);
        Theme.focusField(passField);

        Label errorLabel = new Label("");
        errorLabel.setStyle(Theme.LABEL_ERROR);
        errorLabel.setWrapText(true);

        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle(Theme.BTN_PRIMARY);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setMinHeight(42);
        Theme.hoverPrimary(loginBtn);

        Label registerLink = new Label("Don't have an account? Register here");
        registerLink.setStyle(Theme.LABEL_ACCENT);
        registerLink.setOnMouseClicked(e ->
                Main.primaryStage.setScene(new RegisterScreen().getScene()));

        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String pass  = passField.getText();
            try {
                User user = authService.login(email, pass);
                if (user instanceof Admin) {
                    Main.primaryStage.setScene(new AdminScreen(user).getScene());
                } else {
                    Main.primaryStage.setScene(new HomeScreen(user).getScene());
                }
            } catch (AuthException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        // allow enter key to submit
        passField.setOnAction(e -> loginBtn.fire());

        form.getChildren().addAll(
                emailLabel, emailField,
                passLabel, passField,
                errorLabel,
                loginBtn,
                registerLink
        );

        right.getChildren().addAll(title, subtitle, form);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox root = new HBox(left, right);
        root.setStyle("-fx-background-color: " + Theme.BG_DARK + ";");

        return new Scene(root, 1100, 720);
    }

    private VBox statBlock(String title, String sub) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setStyle(Theme.CARD_ELEVATED);
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + Theme.ACCENT + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label s = new Label(sub);
        s.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px;");
        box.getChildren().addAll(t, s);
        return box;
    }
}