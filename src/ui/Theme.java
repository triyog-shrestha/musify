/**
 * Theme constants and styling utilities for consistent UI appearance.
 * Provides Spotify-inspired color palette and reusable style strings.
 * All screens use these constants for visual consistency.
 */
package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.awt.Desktop;
import java.net.URI;

public class Theme {

    // Spotify brand color palette
    public static final String BG_DARK      = "#121212";   // Main background
    public static final String BG_CARD      = "#181818";   // Card background
    public static final String BG_ELEVATED  = "#282828";   // Spotify elevated surface
    public static final String ACCENT       = "#1DB954";   // Spotify green
    public static final String ACCENT_HOVER = "#1ED760";   // Spotify green hover
    public static final String TEXT_PRIMARY = "#FFFFFF";   // White text
    public static final String TEXT_MUTED   = "#B3B3B3";   // Gray text
    public static final String TEXT_DIM     = "#535353";   // Dimmed text
    public static final String BORDER       = "#282828";   // Subtle border
    public static final String DANGER       = "#E91429";   // Spotify red/error
    public static final String SUCCESS      = "#1DB954";   // Spotify green

    // Base screen background style
    public static final String SCREEN_BG =
            "-fx-background-color: " + BG_DARK + ";";

    // Card container style
    public static final String CARD =
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0;";

    // Elevated card with lighter background
    public static final String CARD_ELEVATED =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0;";

    // Primary action button (Spotify green)
    public static final String BTN_PRIMARY =
            "-fx-background-color: " + ACCENT + ";" +
                    "-fx-text-fill: #000000;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 20;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 32;";

    // Primary button hover state
    public static final String BTN_PRIMARY_HOVER =
            "-fx-background-color: " + ACCENT_HOVER + ";" +
                    "-fx-text-fill: #000000;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 20;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 32;";

    // Outlined ghost button
    public static final String BTN_GHOST =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-border-color: " + TEXT_MUTED + ";" +
                    "-fx-border-radius: 20;" +
                    "-fx-background-radius: 20;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 24;";

    // Ghost button hover state
    public static final String BTN_GHOST_HOVER =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-border-color: " + TEXT_PRIMARY + ";" +
                    "-fx-border-radius: 20;" +
                    "-fx-background-radius: 20;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 24;";

    // Danger/delete button
    public static final String BTN_DANGER =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + DANGER + ";" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-border-color: " + DANGER + ";" +
                    "-fx-border-radius: 20;" +
                    "-fx-background-radius: 20;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 6 14;";

    // Input field style
    public static final String FIELD =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-prompt-text-fill: " + TEXT_DIM + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 4;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 4;" +
                    "-fx-border-width: 0;" +
                    "-fx-padding: 12 14;";

    // Input field focus state
    public static final String FIELD_FOCUS =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 4;" +
                    "-fx-border-color: " + TEXT_PRIMARY + ";" +
                    "-fx-border-radius: 4;" +
                    "-fx-border-width: 1;" +
                    "-fx-padding: 11 13;";

    // Large page title
    public static final String LABEL_TITLE =
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 28px;" +
                    "-fx-font-weight: bold;";

    // Subtitle/description text
    public static final String LABEL_SUBTITLE =
            "-fx-text-fill: " + TEXT_MUTED + ";" +
                    "-fx-font-size: 13px;";

    public static final String LABEL_SECTION =
            "-fx-text-fill: " + TEXT_MUTED + ";" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-transform: uppercase;";

    // Regular content text
    public static final String LABEL_VALUE =
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 14px;";

    public static final String LABEL_ERROR =
            "-fx-text-fill: " + DANGER + ";" +
                    "-fx-font-size: 12px;";

    public static final String LABEL_SUCCESS =
            "-fx-text-fill: " + SUCCESS + ";" +
                    "-fx-font-size: 12px;";

    public static final String LABEL_ACCENT =
            "-fx-text-fill: " + ACCENT + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-cursor: hand;";

    // Sidebar panel style
    public static final String SIDEBAR =
            "-fx-background-color: #000000;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-width: 0;";

    // Navigation menu item (inactive)
    public static final String NAV_ITEM =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + TEXT_MUTED + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-alignment: center-left;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 12 20;" +
                    "-fx-background-radius: 4;" +
                    "-fx-border-radius: 4;";

    // Navigation menu item (active/selected)
    public static final String NAV_ITEM_ACTIVE =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-alignment: center-left;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 12 20;" +
                    "-fx-background-radius: 4;" +
                    "-fx-border-radius: 4;";

    // TableView style
    public static final String TABLE =
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-table-cell-border-color: transparent;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-selection-bar: " + BG_ELEVATED + ";" +
                    "-fx-selection-bar-non-focused: " + BG_ELEVATED + ";";

    // Statistics card container
    public static final String STAT_CARD =
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0;" +
                    "-fx-padding: 20;";

    /**
     * Adds hover effect to primary buttons.
     */
    public static void hoverPrimary(Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle(BTN_PRIMARY_HOVER));
        btn.setOnMouseExited(e  -> btn.setStyle(BTN_PRIMARY));
    }

    /**
     * Adds hover effect to ghost buttons.
     */
    public static void hoverGhost(Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle(BTN_GHOST_HOVER));
        btn.setOnMouseExited(e  -> btn.setStyle(BTN_GHOST));
    }

    /**
     * Adds focus border effect to text input fields.
     */
    public static void focusField(TextInputControl field) {
        field.focusedProperty().addListener((obs, old, focused) -> {
            field.setStyle(focused ? FIELD_FOCUS : FIELD);
        });
    }

    /**
     * Creates a TableView cell that displays Spotify links as clickable hyperlinks.
     * Opens the link in the system's default browser when clicked.
     * 
     * @param <T> The type of the TableView row item
     * @return Configured TableCell for Spotify URLs
     */
    public static <T> TableCell<T, String> spotifyLinkCell() {
        return new TableCell<T, String>() {
            private final Hyperlink link = new Hyperlink("Open");

            {
                link.setStyle("-fx-text-fill: " + ACCENT + "; -fx-font-weight: bold;");
                link.setOnAction(e -> {
                    String url = getItem();
                    if (url == null || url.isBlank()) return;
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ignored) {
                        // Silently fail if browser integration is unavailable
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null || item.isBlank() ? null : link);
                setText(null);
            }
        };
    }

    // ==================== UI FACTORY HELPERS ====================

    /** Creates a styled branding panel (left side of login/register screens) */
    public static VBox brandingPanel(String taglineText) {
        VBox left = new VBox();
        left.setStyle("-fx-background-color: " + BG_CARD + ";");
        left.setMinWidth(420);
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(60));

        Label logo = new Label("MUSIFY");
        logo.setStyle("-fx-text-fill: " + ACCENT + "; -fx-font-size: 42px; -fx-font-weight: bold;");

        Label tagline = new Label(taglineText);
        tagline.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px; -fx-text-alignment: center;");
        tagline.setTextAlignment(TextAlignment.CENTER);
        tagline.setWrapText(true);

        left.getChildren().addAll(logo, tagline);
        return left;
    }

    /** Creates a decorative stat block for branding panels */
    public static VBox statBlock(String title, String sub) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setStyle(CARD_ELEVATED);
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + ACCENT + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label s = new Label(sub);
        s.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        box.getChildren().addAll(t, s);
        return box;
    }

    /** Creates a styled form field with label */
    public static VBox formField(String labelText, TextInputControl field, String placeholder) {
        Label label = new Label(labelText);
        label.setStyle(LABEL_SECTION);
        field.setPromptText(placeholder);
        field.setStyle(FIELD);
        field.setMaxWidth(Double.MAX_VALUE);
        focusField(field);
        VBox container = new VBox(4);
        container.getChildren().addAll(label, field);
        return container;
    }

    /** Creates a styled primary button */
    public static Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(BTN_PRIMARY);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setMinHeight(42);
        hoverPrimary(btn);
        return btn;
    }

    /** Creates a clickable link label */
    public static Label linkLabel(String text, Runnable onClick) {
        Label link = new Label(text);
        link.setStyle(LABEL_ACCENT);
        link.setOnMouseClicked(e -> onClick.run());
        return link;
    }

    /** Creates a back link label */
    public static Label backLink(String text, Runnable onClick) {
        Label link = new Label(text);
        link.setStyle(LABEL_SUBTITLE + "-fx-cursor: hand;");
        link.setOnMouseClicked(e -> onClick.run());
        return link;
    }
}