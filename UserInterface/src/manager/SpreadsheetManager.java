package manager;

import app.AppController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import ui.top.Skin;

import java.io.IOException;
import java.util.Objects;

import static utils.CommonResourcesPaths.*;


// This class manages the stages of the application, including the splash screen and the main app.
public class SpreadsheetManager {

    private Stage primaryStage;

    public SpreadsheetManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void runApp() {
        //showWelcomeScreen(); //fixme-it shows the loading app screen
        showMainApp();
    }

    private void showWelcomeScreen() {
        try {
            // Load the welcome screen FXML
            FXMLLoader welcomeLoader = new FXMLLoader(getClass().getResource(WELCOME_FXML));
            Parent welcomeRoot = welcomeLoader.load();

            // Get the controller from the FXML loader
            SpreadsheetManagerController welcomeController = welcomeLoader.getController();

            // Create a new Stage for the splash screen (undecorated)
            Stage splashStage = new Stage();
            Scene welcomeScene = new Scene(welcomeRoot);
            splashStage.setScene(welcomeScene);
            splashStage.initStyle(StageStyle.UNDECORATED); // Removes the window decorations
            splashStage.show();

            // Simulate progress for the progress bar
            simulateLoading(welcomeController, splashStage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void simulateLoading(SpreadsheetManagerController welcomeController, Stage splashStage) {
        // Simulate progress for 1.5 seconds, then close the splash screen and open the main app
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, event -> welcomeController.setProgress(0)),
                new KeyFrame(Duration.seconds(0.2), event -> welcomeController.setProgress(0.25)),
                new KeyFrame(Duration.seconds(0.4), event -> welcomeController.setProgress(0.5)),
                new KeyFrame(Duration.seconds(0.6), event -> welcomeController.setProgress(0.75)),
                new KeyFrame(Duration.seconds(0.8), event -> welcomeController.setProgress(1))
        );

        timeline.setOnFinished(event -> {
            // Once the progress is complete, close the splash screen and load the main app
            splashStage.close();
            showMainApp();
        });

        timeline.play();
    }

    private void showMainApp() {
        try {
            // Load the main app FXML and set up the AppController
            FXMLLoader appLoader = new FXMLLoader(getClass().getResource(APP_FXML));
            Parent root = appLoader.load();
            AppController appController = appLoader.getController();

            // Set up the scene and stage for the main application
            Scene scene = new Scene(root);
            appController.setSkin(Skin.DEFAULT.getDirectoryName());

            primaryStage.setTitle("Main Scene");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
