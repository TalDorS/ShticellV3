package menuwindow.top;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import menuwindow.MenuWindowController;

import java.io.File;
import java.util.Objects;

import static utils.AlertUtils.showAlert;

public class HeaderLoadController {
    private MenuWindowController mainController;
    private String clearFilePath = "";

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

    public String getUserName() {
        return nameLabel.getText();
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
                    // Perform spreadsheet loading on the background thread
                try {

                    simulateLoading(); // Simulate loading process
                    loadSpreadsheet(filePath);
                    isSuccess = true;

                } catch (Exception e) {
                    updateMessage(e.getMessage());
                    cancel();
                }
                return null;
            }

            private void simulateLoading() throws InterruptedException {
                for (int i = 0; i <= 100; i++) {
                    if (isCancelled()) {
                        System.out.println("Loading was cancelled.");
                        return;
                    }
                    Thread.sleep(20); // Simulate work
                    updateProgress(i, 100);
                }
            }

            private void loadSpreadsheet(String filePath)  {
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
                    progressIndicator.setVisible(false);
                    loadedFilePath.setText(clearFilePath);
                });
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", getMessage());
                    progressIndicator.setVisible(false);
                    loadedFilePath.setText(clearFilePath);
                });
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Failed to load spreadsheet: \n" + getMessage());
                    progressIndicator.setVisible(false);
                    loadedFilePath.setText(clearFilePath);
                });
            }
        };
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeaderLoadController that = (HeaderLoadController) o;
        return Objects.equals(mainController, that.mainController) && Objects.equals(clearFilePath, that.clearFilePath)
                && Objects.equals(loadFileButton, that.loadFileButton) && Objects.equals(loadedFilePath, that.loadedFilePath)
                && Objects.equals(progressIndicator, that.progressIndicator) && Objects.equals(nameLabel, that.nameLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, clearFilePath, loadFileButton, loadedFilePath, progressIndicator, nameLabel);
    }
}
