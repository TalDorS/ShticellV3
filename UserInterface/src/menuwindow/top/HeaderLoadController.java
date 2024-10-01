package menuwindow.top;

import exceptions.engineexceptions.SpreadsheetLoadingException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import menuwindow.MenuWindowController;
import menuwindow.center.AvailableSheetTableController;

import java.io.File;
import java.util.Objects;

import static utils.AlertUtils.showAlert;

public class HeaderLoadController {
    private MenuWindowController mainController;
    private String previousFilePath = "";

    @FXML
    private Button loadFileButton;

    @FXML
    private TextField loadedFilePath;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label nameLabel;

    public void setMainController(MenuWindowController MenuWindowController) {
        this.mainController = MenuWindowController;
    }

    public void setUserName(String username) {
        nameLabel.setText(username);
    }

    @FXML
    public void initialize() {
        loadFileButton.setOnAction(event -> handleLoadFileButtonAction());
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
                && Objects.equals(loadedFilePath, that.loadedFilePath)
                && Objects.equals(progressIndicator, that.progressIndicator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, loadFileButton, loadedFilePath, progressIndicator);
    }



}
