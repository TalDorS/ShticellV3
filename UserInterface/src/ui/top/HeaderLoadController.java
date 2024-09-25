package ui.top;

import exceptions.engineexceptions.SpreadsheetLoadingException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import app.AppController;
import ui.grid.MainGridAreaController;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static utils.AlertUtils.showAlert;
import static utils.CommonResourcesPaths.MAIN_GRID_AREA_FXML;

public class HeaderLoadController {
    private AppController mainController;
    private String previousFilePath = "";

    @FXML
    private Button loadFileButton;
    @FXML
    private MenuButton colorDisplay;
    @FXML
    private MenuButton animationDisplay;
    @FXML
    private TextField loadedFilePath;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        loadFileButton.setOnAction(event -> handleLoadFileButtonAction());
        colorDisplay.setOnShowing(event -> handleColorDisplay());
        animationDisplay.setOnShowing(event -> handleAnimationDisplay());
    }

    private void handleColorDisplay() {
        colorDisplay.getItems().clear();
        for (Skin skin : Skin.values()) {
            MenuItem menuItem = new MenuItem(skin.getDisplayName());
            menuItem.setOnAction(event -> handleSkinChange(skin));
            colorDisplay.getItems().add(menuItem);
        }
    }

    private void handleSkinChange(Skin skin) {
        mainController.setSkin(skin.name().toLowerCase());
        colorDisplay.setText(skin.getDisplayName());
    }

    private void handleAnimationDisplay() {
        animationDisplay.getItems().clear();
        for (Animation animation : Animation.values()) {
            MenuItem menuItem = new MenuItem(animation.getDisplayName());
            menuItem.setOnAction(event -> handleAnimationChange(animation));
            animationDisplay.getItems().add(menuItem);
        }
    }
    private void handleAnimationChange(Animation animation) {
        mainController.setAnimation(animation.getIdentifier());
        animationDisplay.setText(animation.getDisplayName());
    }


    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    // Handle the Load File button action
    private void handleLoadFileButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));

        // Ensure the button's scene is available
        Window stage = getStage();
        if (stage == null) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Cannot get the window from button's scene.");
            return;
        }

        // Show file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) return; // User canceled file selection

        String filePath = selectedFile.getAbsolutePath();
        previousFilePath = loadedFilePath.getText(); // Save the current file path
        loadedFilePath.setText(filePath);

        // Show progress indicator
        progressIndicator.setVisible(true);

        // Create and start a Task for loading
        Task<Void> loadTask = createLoadTask(filePath);
        progressIndicator.progressProperty().bind(loadTask.progressProperty());

        new Thread(loadTask).start();
    }

    // Get the window from the button's scene
    private Window getStage() {
        return loadFileButton.getScene() != null ? loadFileButton.getScene().getWindow() : null;
    }

    private Task<Void> createLoadTask(String filePath) {
        return new Task<Void>() {
            private boolean isSuccess = false;

            @Override
            protected Void call() {
                try {
                    simulateLoading(); // Simulate loading process

                    // Perform spreadsheet loading on the background thread
                    try {
                        loadSpreadsheet(filePath);
                        isSuccess = true;
                    } catch (IllegalArgumentException e) {
                        updateMessage("Invalid file path:\n " + e.getMessage());
                        cancel(); // Attempt to cancel
                    } catch (SpreadsheetLoadingException e) {
                        updateMessage("Error loading spreadsheet:\n " + e.getMessage());
                        cancel(); // Attempt to cancel
                    } catch (Exception e) {
                        updateMessage("Unexpected error:\n " + e.getMessage());
                        cancel(); // Attempt to cancel
                    }

                } catch (Exception e) {
                    updateMessage("Unexpected error: \n" + e.getMessage());
                    cancel();
                }
                return null;
            }

            private void simulateLoading() throws InterruptedException {
                for (int i = 0; i <= 100; i++) {
                    if (isCancelled()) {
                        return;
                    }
                    Thread.sleep(20); // Simulate work
                    updateProgress(i, 100);
                }
            }

            private void loadSpreadsheet(String filePath) throws Exception {
                if (mainController != null) {
                    try {
                        mainController.loadSpreadsheet(filePath);
                    } catch (Exception e) {
                        throw e; // Rethrow exceptions to be caught in the outer try-catch block
                    }
                } else {
                    throw new IllegalStateException("Main Controller is not initialized.");
                }
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(() -> {
                    if (isSuccess) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Spreadsheet loaded successfully.");
                    }
                    progressIndicator.setVisible(false);
                });
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", getMessage());
                    progressIndicator.setVisible(false);
                    loadedFilePath.setText(previousFilePath);
                });
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Failed to load spreadsheet: \n" + getMessage());
                    progressIndicator.setVisible(false);
                    loadedFilePath.setText(previousFilePath);
                });
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeaderLoadController that = (HeaderLoadController) o;
        return Objects.equals(mainController, that.mainController) && Objects.equals(loadFileButton, that.loadFileButton)
                && Objects.equals(colorDisplay, that.colorDisplay) && Objects.equals(loadedFilePath, that.loadedFilePath)
                && Objects.equals(progressIndicator, that.progressIndicator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, loadFileButton, colorDisplay, loadedFilePath, progressIndicator);
    }
}
