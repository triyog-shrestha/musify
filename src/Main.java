/**
 * Main.java
 * 
 * JavaFX application entry point for Musify - a personal music analytics system.
 * This class initializes the database, configures the main window, and launches
 * the application starting with the role selection screen.
 * 
 * Architecture:
 * - Uses JavaFX Application class as the entry point
 * - Stores primary stage globally for screen navigation across the app
 * - Initializes MySQL database connection and creates required tables on startup

 */

import javafx.application.Application;
import javafx.stage.Stage;
import ui.AppContext;
import ui.RoleSelectionScreen;
import util.Database;
import dao.UserDAO;

public class Main extends Application {

    /**
     * Shared primary stage reference allowing any screen to switch scenes.
     * This enables navigation between screens from anywhere in the application.
     */
    public static Stage primaryStage;

    /**
     * JavaFX application start method - called after the JavaFX runtime is initialized.
     * Sets up the database, configures the main window, and displays the initial screen.
     * 
     * @param stage The primary stage provided by JavaFX runtime
     */
    @Override
    public void start(Stage stage) {
        // Store stage reference for global access
        primaryStage = stage;
        AppContext.primaryStage = stage;

        // Initialize database - creates tables if they don't exist
        Database.init();
        new UserDAO().init();

        // Configure main window properties
        stage.setTitle("Musify");
        stage.setWidth(1100);
        stage.setHeight(720);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setResizable(true);

        // Launch application with role selection (Listener vs Admin)
        stage.setScene(new RoleSelectionScreen().getScene());
        stage.show();
    }

    /**
     * Application entry point - launches the JavaFX application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}