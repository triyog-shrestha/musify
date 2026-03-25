// Theme.java
// Shared colours, font sizes and reusable style strings for all screens.
// All screens use this so the look stays consistent across the app.

package ui;

public class Theme {

    // colours
    public static final String BG_DARK      = "#0d0d0d";
    public static final String BG_CARD      = "#161616";
    public static final String BG_ELEVATED  = "#1f1f1f";
    public static final String ACCENT       = "#f5a623";
    public static final String ACCENT_HOVER = "#f7b84b";
    public static final String TEXT_PRIMARY = "#f0ece4";
    public static final String TEXT_MUTED   = "#7a756e";
    public static final String TEXT_DIM     = "#3d3a35";
    public static final String BORDER       = "#2a2722";
    public static final String DANGER       = "#e05252";
    public static final String SUCCESS      = "#52c07a";

    // base screen background
    public static final String SCREEN_BG =
            "-fx-background-color: " + BG_DARK + ";";

    // card style
    public static final String CARD =
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1;";

    // elevated card
    public static final String CARD_ELEVATED =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 10;" +
                    "-fx-border-width: 1;";

    // primary button (amber)
    public static final String BTN_PRIMARY =
            "-fx-background-color: " + ACCENT + ";" +
                    "-fx-text-fill: #0d0d0d;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 24;";

    public static final String BTN_PRIMARY_HOVER =
            "-fx-background-color: " + ACCENT_HOVER + ";" +
                    "-fx-text-fill: #0d0d0d;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 24;";

    // ghost button (outline)
    public static final String BTN_GHOST =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + TEXT_MUTED + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 24;";

    public static final String BTN_GHOST_HOVER =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 10 24;";

    // danger button
    public static final String BTN_DANGER =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + DANGER + ";" +
                    "-fx-font-size: 12px;" +
                    "-fx-border-color: " + DANGER + ";" +
                    "-fx-border-radius: 6;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 6 14;";

    // text field
    public static final String FIELD =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-prompt-text-fill: " + TEXT_DIM + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 1;" +
                    "-fx-padding: 10 14;";

    public static final String FIELD_FOCUS =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: " + ACCENT + ";" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 1;" +
                    "-fx-padding: 10 14;";

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
                    "-fx-font-weight: bold;";

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
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-width: 0 1 0 0;";

    public static final String NAV_ITEM =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + TEXT_MUTED + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-alignment: center-left;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 12 20;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;";

    public static final String NAV_ITEM_ACTIVE =
            "-fx-background-color: " + BG_ELEVATED + ";" +
                    "-fx-text-fill: " + ACCENT + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-alignment: center-left;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 12 20;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;";

    // table
    public static final String TABLE =
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-table-cell-border-color: " + BORDER + ";" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 10;";

    // stat card
    public static final String STAT_CARD =
            "-fx-background-color: " + BG_CARD + ";" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1;" +
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
}