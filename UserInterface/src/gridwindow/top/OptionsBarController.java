package gridwindow.top;

import dto.VersionDTO;
import gridwindow.top.interactiveformuladialog.InteractiveFormulaDialogController;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import gridwindow.GridWindowController;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import gridwindow.grid.MainGridAreaController;
import utils.AlertUtils;

import static utils.AlertUtils.showAlert;
import static utils.CommonResourcesPaths.*;


public class OptionsBarController {

    private GridWindowController mainController;

    @FXML
    private TextField selectedCellId;

    @FXML
    private TextField originalCellValue;

    @FXML
    private TextField actionLineInput;

    @FXML
    private Button updateValueButton;

    @FXML
    private TextField lastUpdateCellVersion;

    @FXML
    private MenuButton versionSelectorButton;

    @FXML
    private Label currentVersionLabel;

    @FXML
    private Button interactiveFormulaButton;

    @FXML
    public void initialize() {
        updateValueButton.setOnAction(event -> handleUpdateValueButtonAction());
        interactiveFormulaButton.setOnAction(event -> handleInteractiveFormulaButtonAction());
        versionSelectorButton.setOnShowing(event -> handleVersionSelectorButtonAction());
    }

    public void setMainController(GridWindowController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleUpdateValueButtonAction() {
        String newValue = actionLineInput.getText(); // Get the new value from the input field
        String cellId = selectedCellId.getText(); // Get the ID of the selected cell

        if (mainController != null && !cellId.isEmpty()) {
            mainController.updateCellValue(cellId, newValue, false); // Pass the update request to AppController
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No cell selected or invalid input.");
        }
    }

    public void updateCurrentVersionLabel(int versionNumber) {
        currentVersionLabel.setText("Current Version: " + versionNumber);
    }

    @FXML
    private void handleVersionSelectorButtonAction() {

        List<VersionDTO> versions = mainController.getVersions();
        versionSelectorButton.getItems().clear(); // Clear the previous menu items

        if(versions == null || versions.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No versions available.");
            return;
        }

        for (VersionDTO version : versions) {
            String menuText = "Version " + version.getVersionNumber() + " (" + version.getChangedCellsCount() + " changes)";
            MenuItem versionItem = new MenuItem(menuText);

            // Use a local variable to ensure the correct version is passed
            final int versionNumber = version.getVersionNumber();

            // Add action listener to handle the version selection
            versionItem.setOnAction(e -> {
                // Call the main controller method to display the selected version
                displaySpreadsheetForVersion(versionNumber);
            });

            // Add each version item to the menu button
            versionSelectorButton.getItems().add(versionItem);
        }
    }

    public void displaySpreadsheetForVersion(int versionNumber) {
        try {
            // Load the FXML file for the grid layout
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_GRID_AREA_FXML));
            Parent root = loader.load(); // Load the FXML to get the root node (Parent)

            // Get the controller instance
            MainGridAreaController gridController = loader.getController();
            gridController.setMainController(mainController);

            // Set the engine and initialize the grid for the correct version
            gridController.start(mainController.getSpreadsheetByVersion(versionNumber), true);

            // Create a new stage (window)
            Stage popupStage = new Stage();
            popupStage.setTitle("Spreadsheet Version " + versionNumber);

            // Set the scene with the loaded root (which contains the GridPane)
            Scene popupScene = new Scene(root);
            popupStage.setScene(popupScene);

            String css = getClass().getResource(MAIN_GRID_AREA_CSS).toExternalForm();
            popupScene.getStylesheets().add(css);

            // Make the popup modal
            popupStage.initModality(Modality.APPLICATION_MODAL);

            popupStage.showAndWait(); // Show the popup and wait for it to be closed

        } catch (IOException e) {
        }
    }

    public void updateCellInfo(String cellId, String originalValue, String lastUpdateVersion, String userName) {
        selectedCellId.setText(cellId);
        originalCellValue.setText(originalValue);
        // Only show the last update version and username if lastUpdateVersion and userName are not empty
        if (lastUpdateVersion != null && !lastUpdateVersion.trim().isEmpty() && userName != null && !userName.trim().isEmpty()) {
            lastUpdateCellVersion.setText("Version " + lastUpdateVersion + " (" + userName + ")");
        } else {
            lastUpdateCellVersion.setText("");  // Clear the field if empty
        }
    }

    public void clearActionLineInput() {
        actionLineInput.clear();
    }

    @FXML
    private void handleInteractiveFormulaButtonAction() {
        try {
            // Check if a cell is selected
            if (selectedCellId.getText() == null || selectedCellId.getText().isEmpty()) {
                throw new IllegalStateException("No cell selected. Please select a cell to use the Interactive Formula Builder.");
            }

            // Check if a spreadsheet is loaded
            if (!mainController.isSpreadsheetLoaded()) {
                throw new IllegalStateException("No spreadsheet loaded. Please load a spreadsheet first.");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(INTERACTIVE_FORMULA_DIALOG_FXML));
            Parent root = loader.load();

            // Get the controller from the FXML loader
            InteractiveFormulaDialogController formulaDialogController = loader.getController();
            formulaDialogController.setMainController(mainController);

            // Set this as the main controller for the formula dialog
            formulaDialogController.setParentController(this);

            // Create a new stage for the dialog
            Stage stage = new Stage();
            stage.setTitle("Interactive Formula Builder");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Show the stage and wait for it to be closed
            stage.showAndWait();
        } catch (IOException | IllegalStateException e) {
            AlertUtils.showError("Failed to open interactive formula builder: " + e.getMessage());
        }
    }

    public void applyExpressionToActionLine(String expression) {
        actionLineInput.setText(expression);
    }

    public String getCurrentCellId() {
        return selectedCellId.getText();
    }

    public void disableEditButtons() {
        this.updateValueButton.setDisable(true);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionsBarController that = (OptionsBarController) o;
        return Objects.equals(mainController, that.mainController) && Objects.equals(selectedCellId, that.selectedCellId)
                && Objects.equals(originalCellValue, that.originalCellValue) && Objects.equals(actionLineInput, that.actionLineInput)
                && Objects.equals(updateValueButton, that.updateValueButton) && Objects.equals(lastUpdateCellVersion, that.lastUpdateCellVersion)
                && Objects.equals(versionSelectorButton, that.versionSelectorButton) && Objects.equals(currentVersionLabel, that.currentVersionLabel)
                && Objects.equals(interactiveFormulaButton, that.interactiveFormulaButton);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, selectedCellId, originalCellValue, actionLineInput, updateValueButton,
                lastUpdateCellVersion, versionSelectorButton, currentVersionLabel, interactiveFormulaButton);
    }
}

