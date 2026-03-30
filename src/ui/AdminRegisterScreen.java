// AdminRegisterScreen.java
// Registration screen for admin users

package ui;

import dao.UserDAO;
import exception.AuthException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Admin;
import model.User;
import service.AuthService;

import java.time.LocalDateTime;

public class AdminRegisterScreen {

    private final AuthService authService = new AuthService();
    private final UserDAO userDAO = new UserDAO();

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

        Label tagline = new Label("Admin Registration\n\nCreate an admin account\nwith elevated privileges.");
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

        Label title = new Label("Create admin account");
        title.setStyle(Theme.LABEL_TITLE);

        Label subtitle = new Label("Register with administrator privileges");
        subtitle.setStyle(Theme.LABEL_SUBTITLE);

        VBox form = new VBox(12);
        form.setMaxWidth(360);

        Label nameLabel = new Label("ADMIN USERNAME");
        nameLabel.setStyle(Theme.LABEL_SECTION);
        TextField nameField = new TextField();
        nameField.setPromptText("Choose an admin username");
        nameField.setStyle(Theme.FIELD);
        nameField.setMaxWidth(Double.MAX_VALUE);
        Theme.focusField(nameField);

        Label emailLabel = new Label("ADMIN EMAIL");
        emailLabel.setStyle(Theme.LABEL_SECTION);
        TextField emailField = new TextField();
        emailField.setPromptText("admin@example.com");
        emailField.setStyle(Theme.FIELD);
        emailField.setMaxWidth(Double.MAX_VALUE);
        Theme.focusField(emailField);

        Label passLabel = new Label("ADMIN PASSWORD");
        passLabel.setStyle(Theme.LABEL_SECTION);
        PasswordField passField = new PasswordField();
        passField.setPromptText("Choose a strong password (min 8 characters)");
        passField.setStyle(Theme.FIELD);
        passField.setMaxWidth(Double.MAX_VALUE);
        Theme.focusField(passField);

        Label errorLabel = new Label("");
        errorLabel.setStyle(Theme.LABEL_ERROR);
        errorLabel.setWrapText(true);

        // Warning box
        VBox warningBox = new VBox(6);
        warningBox.setStyle("-fx-background-color: rgba(239, 68, 68, 0.1);" +
                "-fx-border-color: #ef4444;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 6px;" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 12px;");
        Label warningTitle = new Label("⚠ Admin Privileges:");
        warningTitle.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label warningText = new Label("Admin accounts can manage users, view all statistics, and manage recommendations.");
        warningText.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px;");
        warningText.setWrapText(true);
        warningBox.getChildren().addAll(warningTitle, warningText);

        Button registerBtn = new Button("Create Admin Account");
        registerBtn.setStyle(Theme.BTN_PRIMARY);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setMinHeight(42);
        Theme.hoverPrimary(registerBtn);

        Label loginLink = new Label("Already have an admin account? Sign in");
        loginLink.setStyle(Theme.LABEL_ACCENT);
        loginLink.setOnMouseClicked(e ->
                AppContext.primaryStage.setScene(new AdminLoginScreen().getScene()));

        Label backLink = new Label("← Back to role selection");
        backLink.setStyle(Theme.LABEL_SUBTITLE + "-fx-cursor: hand;");
        backLink.setOnMouseClicked(e ->
                AppContext.primaryStage.setScene(new RoleSelectionScreen().getScene()));

        registerBtn.setOnAction(e -> {
            String username = nameField.getText().trim();
            String email    = emailField.getText().trim();
            String pass     = passField.getText();
            
            // Validation
            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("All fields are required.");
                return;
            }
            
            if (!email.contains("@")) {
                errorLabel.setText("Invalid email address.");
                return;
            }
            
            if (pass.length() < 8) {
                errorLabel.setText("Password must be at least 8 characters.");
                return;
            }
            
            try {
                if (userDAO.usernameExists(username)) {
                    errorLabel.setText("Username is already taken.");
                    return;
                }
                if (userDAO.emailExists(email)) {
                    errorLabel.setText("An account with this email already exists.");
                    return;
                }
                
                // Create admin user
                Admin admin = new Admin(username, email, authService.hashPassword(pass));
                userDAO.createUser(admin);
                
                AppContext.primaryStage.setScene(new AdminHomeScreen(admin).getScene());
            } catch (Exception ex) {
                errorLabel.setText("Error creating admin account: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(
                nameLabel, nameField,
                emailLabel, emailField,
                passLabel, passField,
                errorLabel,
                warningBox,
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
