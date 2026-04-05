package ui;

import dao.UserDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Admin;
import service.AuthService;

public class AdminRegisterScreen {

    private final AuthService authService = new AuthService();
    private final UserDAO userDAO = new UserDAO();

    public Scene getScene() {
        // Left branding panel
        VBox left = Theme.brandingPanel("Admin Registration\n\nCreate an admin account\nwith elevated privileges.");

        // Right form panel
        VBox right = new VBox(16);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(60, 80, 60, 80));
        right.setStyle("-fx-background-color: " + Theme.BG_DARK + ";");

        Label title = new Label("Create admin account");
        title.setStyle(Theme.LABEL_TITLE);
        Label subtitle = new Label("Register with admin privileges");
        subtitle.setStyle(Theme.LABEL_SUBTITLE);

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passField = new PasswordField();
        Label errorLabel = new Label("");
        errorLabel.setStyle(Theme.LABEL_ERROR);
        errorLabel.setWrapText(true);

        Button registerBtn = Theme.primaryButton("Create Admin Account");
        registerBtn.setOnAction(e -> {
            String username = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass = passField.getText();
            
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
                Admin admin = new Admin(username, email, authService.hashPassword(pass));
                userDAO.createUser(admin);
                AppContext.primaryStage.setScene(new AdminHomeScreen(admin).getScene());
            } catch (Exception ex) {
                errorLabel.setText("Error creating admin account: " + ex.getMessage());
            }
        });

        VBox form = new VBox(12,
            Theme.formField("ADMIN USERNAME", nameField, "Choose an admin username"),
            Theme.formField("ADMIN EMAIL", emailField, "Choose an admin email"),
            Theme.formField("ADMIN PASSWORD", passField, "Choose a strong password (min 8 characters)"),
            errorLabel, registerBtn,
            Theme.linkLabel("Already have an admin account? Log in", 
                () -> AppContext.primaryStage.setScene(new AdminLoginScreen().getScene())),
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
