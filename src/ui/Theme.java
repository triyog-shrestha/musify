// Theme.java
// Shared colours, font sizes and reusable style strings for all screens.
// All screens use this so the look stays consistent across the app.
// Using Spotify's official brand colors.

package ui;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;

import java.awt.Desktop;
import java.net.URI;

public class Theme {

    // Spotify brand colours
    public static final String BG_DARK      = "#121212";   // Spotify black background
    public static final String BG_CARD      = "#181818";   // Spotify card background
    public static final String BG_ELEVATED  = "#282828";   // Spotify elevated surface
    public static final String ACCENT       = "#1DB954";   // Spotify green
    public static final String ACCENT_HOVER = "#1ED760";   // Spotify green hover
    public static final String TEXT_PRIMARY = "#FFFFFF";   // White text
    public static final String TEXT_MUTED   = "#B3B3B3";   // Gray text
    public static final String TEXT_DIM     = "#535353";   // Dimmed text
    public static final String BORDER       = "#282828";   // Subtle border
    public static final String DANGER       = "#E91429";   // Spotify red/error
    public static final String SUCCESS      = "#1DB954";   // Spotify green

    // base screen background
    public static final String SCREEN_BG =
            "-fx-background-color: " + BG_DARK + ";";

    // card style
    public static final String CARD =
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0;";

    // elevated card
    public static final String CARD_ELEVATED =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0;";

    // primary button (Spotify green)
    public static final String BTN_PRIMARY =
            "-fx-background-color: " + ACCENT + ";" +
                    "-fx-text-fill: #000000;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 20;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 32;";

    public static final String BTN_PRIMARY_HOVER =
            "-fx-background-color: " + ACCENT_HOVER + ";" +
                    "-fx-text-fill: #000000;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 20;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 32;";

    // ghost button (outline)
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

    // danger button
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

    // text field
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

    public static final String FIELD_FOCUS =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 4;" +
                    "-fx-border-color: " + TEXT_PRIMARY + ";" +
                    "-fx-border-radius: 4;" +
                    "-fx-border-width: 1;" +
                    "-fx-padding: 11 13;";

    // label styles
    public static final String LABEL_TITLE =
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 28px;" +
                    "-fx-font-weight: bold;";

    public static final String LABEL_SUBTITLE =
            "-fx-text-fill: " + TEXT_MUTED + ";" +
                    "-fx-font-size: 13px;";

    public static final String LABEL_SECTION =
            "-fx-text-fill: " + TEXT_MUTED + ";" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-transform: uppercase;";

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

    // sidebar
    public static final String SIDEBAR =
            "-fx-background-color: #000000;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-width: 0;";

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

    // table
    public static final String TABLE =
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-table-cell-border-color: transparent;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-selection-bar: " + BG_ELEVATED + ";" +
                    "-fx-selection-bar-non-focused: " + BG_ELEVATED + ";";

    // stat card
    public static final String STAT_CARD =
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0;" +
                    "-fx-padding: 20;";

    // hover helpers
    public static void hoverPrimary(javafx.scene.control.Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle(BTN_PRIMARY_HOVER));
        btn.setOnMouseExited(e  -> btn.setStyle(BTN_PRIMARY));
    }

    public static void hoverGhost(javafx.scene.control.Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle(BTN_GHOST_HOVER));
        btn.setOnMouseExited(e  -> btn.setStyle(BTN_GHOST));
    }

    public static void focusField(javafx.scene.control.TextInputControl field) {
        field.focusedProperty().addListener((obs, old, focused) -> {
            field.setStyle(focused ? FIELD_FOCUS : FIELD);
        });
    }

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
                        // Keep UI responsive if OS/browser integration fails.
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
}