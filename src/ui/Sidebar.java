// Sidebar.java
// Reusable left navigation bar used by all main screens.
// Takes the active screen name and the current user to show the username.

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

        // nav items
        String[] items = { "Home", "Library", "Stats", "Recommendations", "Profile" };
        for (String item : items) {
            Button btn = navButton(item, item.equals(active));
            btn.setOnAction(e -> navigate(item, user));
            getChildren().add(btn);
        }

        if (user instanceof Admin) {
            Region div2 = new Region();
            div2.setStyle("-fx-background-color: " + Theme.BORDER + ";");
            div2.setMinHeight(1);
            div2.setMaxHeight(1);
            VBox.setMargin(div2, new Insets(8, 8, 8, 8));
            Button adminBtn = navButton("Admin Panel", "Admin Panel".equals(active));
            adminBtn.setOnAction(e -> navigate("Admin Panel", user));
            getChildren().addAll(div2, adminBtn);
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
            Main.primaryStage.setScene(new LoginScreen().getScene());
        });
        getChildren().add(logout);
    }

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

    private void navigate(String item, User user) {
        switch (item) {
            case "Home"            -> Main.primaryStage.setScene(new HomeScreen(user).getScene());
            case "Library"         -> Main.primaryStage.setScene(new LibraryScreen(user).getScene());
            case "Stats"           -> Main.primaryStage.setScene(new StatsScreen(user).getScene());
            case "Recommendations" -> Main.primaryStage.setScene(new RecsScreen(user).getScene());
            case "Profile"         -> Main.primaryStage.setScene(new ProfileScreen(user).getScene());
            case "Admin Panel"     -> Main.primaryStage.setScene(new AdminScreen(user).getScene());
        }
    }
}