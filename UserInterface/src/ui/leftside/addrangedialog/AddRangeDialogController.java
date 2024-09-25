package ui.leftside.addrangedialog;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import app.AppController;
import utils.AlertUtils;

public class AddRangeDialogController {
    private AppController mainController;

    @FXML
    private TextField rangeNameField;

    @FXML
    private TextField rangeInputField; // Combined input for the cell range

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleCreateRangeButton() {
        String rangeName = rangeNameField.getText().trim();
        String rangeInput = rangeInputField.getText().trim();

        if (rangeName.isEmpty() || rangeInput.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled out.");
            return;
        }

        // Validate and parse the range input
        String[] cells = parseRangeInput(rangeInput);
        if (cells == null) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "Invalid range format. Please use the format: A3..V9.");
            return;
        }

        String firstCell = cells[0];
        String lastCell = cells[1];

        try {
            // Pass the range creation request to the AppController
            mainController.addNewRange(rangeName, firstCell, lastCell);
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to create range: " + e.getMessage());
        }
    }

    // Parses the input range string in the format "<top-left-cell>..<bottom-right-cell>"
    private String[] parseRangeInput(String rangeInput) {
        // Ensure the input contains the ".." separator
        if (!rangeInput.contains("..")) {
            return null;
        }

        String[] cells = rangeInput.split("\\.\\.");

        // Ensure there are exactly two cells provided
        if (cells.length != 2) {
            return null;
        }

        String firstCell = cells[0].trim().toUpperCase();
        String lastCell = cells[1].trim().toUpperCase();

        // Validate cell formats using a simple regex (like "A1", "B2", etc.)
        if (!firstCell.matches("[A-Z]+\\d+") || !lastCell.matches("[A-Z]+\\d+")) {
            return null;
        }

        return new String[] {firstCell, lastCell};
    }
}
