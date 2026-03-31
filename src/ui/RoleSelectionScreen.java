/**
 * Initial role selection screen shown on application launch.
 * Allows users to choose between Listener and Admin login portals.
 * Features two card-style buttons with hover effects.
 */
package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

public class RoleSelectionScreen {

    /**
     * Creates and returns the JavaFX scene for role selection.
     * 
     * @return Configured Scene object
     */
    public Scene getScene() {
        VBox root = new VBox(40);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(80));
        root.setStyle("-fx-background-color: " + Theme.BG_DARK + ";");

        // Logo
        Label logo = new Label("MUSIFY");
        logo.setStyle(
                "-fx-text-fill: " + Theme.ACCENT + ";" +
                        "-fx-font-size: 48px;" +
                        "-fx-font-weight: bold;");

        Label tagline = new Label("Your personal music analytics system");
        tagline.setStyle(
                "-fx-text-fill: " + Theme.TEXT_MUTED + ";" +
                        "-fx-font-size: 16px;");

        Label question = new Label("How do you want to enter the system?");
        question.setStyle(
                "-fx-text-fill: " + Theme.TEXT_PRIMARY + ";" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;");
        question.setPadding(new Insets(20, 0, 0, 0));

        // Role selection cards
        HBox cards = new HBox(30);
        cards.setAlignment(Pos.CENTER);

        VBox userCard = createRoleCard(
                "LISTENER",
                "Track your music library\nAnalyze your listening habits\nDiscover new recommendations",
                Theme.ACCENT,
                () -> AppContext.primaryStage.setScene(new UserLoginScreen().getScene())
        );

        VBox adminCard = createRoleCard(
                "ADMIN",
                "Manage users\nView system statistics\nManage recommendations",
                Theme.TEXT_MUTED,
                () -> AppContext.primaryStage.setScene(new AdminLoginScreen().getScene())
        );

        cards.getChildren().addAll(userCard, adminCard);

        root.getChildren().addAll(logo, tagline, question, cards);

        return new Scene(root, 1100, 720);
    }

    /**
     * Creates a clickable role selection card with hover effects.
     * 
     * @param title       Role name (e.g., "LISTENER", "ADMIN")
     * @param description Features list for this role
     * @param accentColor Color to use for title and border
     * @param onSelect    Action to run when card is clicked
     * @return Configured VBox card
     */
    private VBox createRoleCard(String title, String description, String accentColor, Runnable onSelect) {
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 50, 40, 50));
        card.setStyle(Theme.CARD + 
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;" +
                "-fx-border-width: 2px;");
        card.setMinWidth(280);
        card.setMinHeight(250);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: " + accentColor + ";" +
                        "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle(
                "-fx-text-fill: " + Theme.TEXT_MUTED + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-alignment: center;");
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(240);

        Button selectBtn = new Button("Select");
        selectBtn.setStyle(Theme.BTN_PRIMARY);
        selectBtn.setMinWidth(140);
        selectBtn.setMinHeight(40);
        Theme.hoverPrimary(selectBtn);
        selectBtn.setOnAction(e -> onSelect.run());

        card.getChildren().addAll(titleLabel, descLabel, selectBtn);

        // Hover effect for card
        card.setOnMouseEntered(e -> 
            card.setStyle(Theme.CARD + 
                "-fx-cursor: hand;" +
                "-fx-border-color: " + accentColor + ";" +
                "-fx-border-width: 2px;")
        );
        card.setOnMouseExited(e -> 
            card.setStyle(Theme.CARD + 
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;" +
                "-fx-border-width: 2px;")
        );
        card.setOnMouseClicked(e -> onSelect.run());

        return card;
    }
}
