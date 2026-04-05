package ui;

import exception.AuthException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Admin;
import model.User;
import service.AuthService;

public class AdminLoginScreen {

    private final AuthService authService = new AuthService();

    public Scene getScene() {
        // Left branding panel
        VBox left = Theme.brandingPanel("Admin Portal\n\nManage users, view statistics,\nand curate recommendations.");
        HBox stats = new HBox(20, Theme.statBlock("Manage", "Users"), 
                              Theme.statBlock("View", "Statistics"), 
                              Theme.statBlock("Curate", "Recommendations"));
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(40, 0, 0, 0));
        left.getChildren().add(stats);

        // Right form panel
        VBox right = new VBox(16);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(60, 80, 60, 80));
        right.setStyle("-fx-background-color: " + Theme.BG_DARK + ";");

        Label title = new Label("Admin Access");
        title.setStyle(Theme.LABEL_TITLE);
        Label subtitle = new Label("Log in with your admin credentials");
        subtitle.setStyle(Theme.LABEL_SUBTITLE);

        TextField emailField = new TextField();
        PasswordField passField = new PasswordField();
        Label errorLabel = new Label("");
        errorLabel.setStyle(Theme.LABEL_ERROR);
        errorLabel.setWrapText(true);

        Button loginBtn = Theme.primaryButton("Log In");
        loginBtn.setOnAction(e -> {
            try {
                User user = authService.login(emailField.getText().trim(), passField.getText());
                if (!(user instanceof Admin)) {
                    errorLabel.setText("This is not an admin account. Use the user login.");
                    return;
                }
                AppContext.primaryStage.setScene(new AdminHomeScreen(user).getScene());
            } catch (AuthException ex) {
                errorLabel.setText(ex.getMessage());
            } catch (Exception ex) {
                errorLabel.setText("Unexpected error while logging in.");
            }
        });
        passField.setOnAction(e -> loginBtn.fire());

        VBox form = new VBox(12,
            Theme.formField("ADMIN EMAIL", emailField, "Enter admin email"),
            Theme.formField("ADMIN PASSWORD", passField, "Enter admin password"),
            errorLabel, loginBtn,
            Theme.linkLabel("Create another admin account", 
                () -> AppContext.primaryStage.setScene(new AdminRegisterScreen().getScene())),
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
