/**
 * Global application context for shared state across screens.
 * Stores the primary JavaFX Stage reference, allowing any screen
 * to navigate to another without passing the stage around.
 */
package ui;

import javafx.stage.Stage;

public class AppContext {
    
    /**
     * The main application window. Set once at startup in Main.java.
     * Any screen can use this to switch scenes: AppContext.primaryStage.setScene(...)
     */
    public static Stage primaryStage;
}
