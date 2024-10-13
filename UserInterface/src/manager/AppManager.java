package manager;


import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

import loginwindow.LoginController;
import java.io.IOException;

import static utils.CommonResourcesPaths.*;


// This class manages the stages of the application, including the splash screen and the main app.
public class AppManager {

    private Stage primaryStage;

    public AppManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void runApp() {
        showLoginWindow();
    }

    private void showLoginWindow() {
        try {
            // Load the login FXML
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource(LOGIN_FXML));
            Parent loginRoot = loginLoader.load();

            // Get the controller
            LoginController loginController = loginLoader.getController();

            // Create new stage for the login window
            Stage loginStage = new Stage();
            Scene loginScene = new Scene(loginRoot);

            // Login window properties
            loginStage.setTitle("Login - New Client");
            loginStage.setScene(loginScene);
            loginStage.initStyle(StageStyle.DECORATED);
            loginStage.setResizable(false);
            loginStage.show();

            // Hide the primary stage until login is successful
            primaryStage.hide();
        } catch (IOException e) {
        }
    }

    public void closeApp() {
        // Close the primary stage
        if (primaryStage != null) {
            primaryStage.close();  // Closes the primary window
        }

        Platform.exit();  // Shuts down the entire application
        System.exit(0);  // Forces the application to close
    }
}
