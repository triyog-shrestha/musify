// Main.java
// JavaFX entry point. Replaces the terminal Main.java entirely.
// Initialises the app, sets up the window and shows LoginScreen first.

import javafx.application.Application;
import javafx.stage.Stage;
import ui.AppContext;
import ui.RoleSelectionScreen;
import util.Database;
import dao.UserDAO;

public class Main extends Application {

    // shared stage so any screen can switch scenes from anywhere
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        AppContext.primaryStage = stage;

        // reset and initialize database tables on startup
        Database.init();
        new UserDAO().init();

        // window settings
        stage.setTitle("Musify");
        stage.setWidth(1100);
        stage.setHeight(720);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setResizable(true);

        // start at the role selection screen
        stage.setScene(new RoleSelectionScreen().getScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}