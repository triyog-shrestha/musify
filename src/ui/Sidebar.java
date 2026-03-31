/**
 * Reusable left navigation sidebar for main application screens.
 * Displays user information, navigation menu items, and logout button.
 * Routes differently for admins vs regular users (admins skip Library screen).
 */
package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import model.Admin;
import model.User;


public class Sidebar extends VBox {

    /**
     * Creates a sidebar with navigation for the given user.
     * 
     * @param active Currently active screen name (will be highlighted)
     * @param user   Current logged-in user (Admin or regular User)
     */
    public Sidebar(String active, User user) {
        setStyle(Theme.SIDEBAR);
        setMinWidth(200);
        setMaxWidth(200);
        setPadding(new Insets(24, 12, 24, 12));
        setSpacing(4);

        // logo
        Label logo = new Label("MUSIFY");
        logo.setStyle(
                "-fx-text-fill: " + Theme.ACCENT + ";" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 0 8 20 8;");

        // user info
        Label username = new Label(user.getUsername());
        username.setStyle(
                "-fx-text-fill: " + Theme.TEXT_PRIMARY + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 0 8 2 8;");
        Label role = new Label(user instanceof Admin ? "Admin" : "User");
        role.setStyle(
                "-fx-text-fill: " + (user instanceof Admin ? Theme.ACCENT : Theme.TEXT_MUTED) + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 0 8 16 8;");

        // divider
        Region div = new Region();
        div.setStyle("-fx-background-color: " + Theme.BORDER + ";");
        div.setMinHeight(1);
        div.setMaxHeight(1);
        VBox.setMargin(div, new Insets(0, 8, 12, 8));

        getChildren().addAll(logo, username, role, div);

        // Different nav items for admin vs listener
        boolean isAdmin = user instanceof Admin;
        String[] items = isAdmin 
            ? new String[]{ "Home", "Recommendations", "Stats", "Profile" }
            : new String[]{ "Home", "Library", "Stats", "Recommendations", "Profile" };
            
        for (String item : items) {
            Button btn = navButton(item, item.equals(active));
            btn.setOnAction(e -> navigate(item, user));
            getChildren().add(btn);
        }

        // spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        // logout button
        Button logout = new Button("Logout");
        logout.setStyle(Theme.BTN_GHOST);
        logout.setMaxWidth(Double.MAX_VALUE);
        Theme.hoverGhost(logout);
        logout.setOnAction(e -> {
            AppContext.primaryStage.setScene(new RoleSelectionScreen().getScene());
        });
        getChildren().add(logout);
    }

    /**
     * Creates a navigation button with appropriate styling.
     * Active buttons are highlighted, inactive buttons have hover effects.
     */
    private Button navButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setStyle(active ? Theme.NAV_ITEM_ACTIVE : Theme.NAV_ITEM);
        btn.setMaxWidth(Double.MAX_VALUE);
        if (!active) {
            btn.setOnMouseEntered(ev -> btn.setStyle(Theme.NAV_ITEM_ACTIVE));
            btn.setOnMouseExited(ev  -> btn.setStyle(Theme.NAV_ITEM));
        }
        return btn;
    }

    /**
     * Navigates to the appropriate screen based on the menu item selected.
     * Routes differ for admins and regular users.
     */
    private void navigate(String item, User user) {
        boolean isAdmin = user instanceof Admin;
        
        switch (item) {
            case "Home" -> {
                if (isAdmin) {
                    AppContext.primaryStage.setScene(new AdminHomeScreen(user).getScene());
                } else {
                    AppContext.primaryStage.setScene(new HomeScreen(user).getScene());
                }
            }
            case "Library" -> {
                // Only for listeners
                AppContext.primaryStage.setScene(new LibraryScreen(user).getScene());
            }
            case "Stats" -> {
                if (isAdmin) {
                    AppContext.primaryStage.setScene(new AdminStatsScreen(user).getScene());
                } else {
                    AppContext.primaryStage.setScene(new StatsScreen(user).getScene());
                }
            }
            case "Recommendations" -> {
                if (isAdmin) {
                    AppContext.primaryStage.setScene(new AdminRecsScreen(user).getScene());
                } else {
                    AppContext.primaryStage.setScene(new RecsScreen(user).getScene());
                }
            }
            case "Profile" -> {
                if (isAdmin) {
                    AppContext.primaryStage.setScene(new AdminProfileScreen(user).getScene());
                } else {
                    AppContext.primaryStage.setScene(new ProfileScreen(user).getScene());
                }
            }
        }
    }
}