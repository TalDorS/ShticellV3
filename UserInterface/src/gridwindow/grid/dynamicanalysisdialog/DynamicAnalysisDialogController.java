package gridwindow.grid.dynamicanalysisdialog;

import api.Expression;
import dto.CellDTO;
import gridwindow.GridWindowController;
import expressionimpls.FunctionExpression;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import cells.Cell;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static utils.AlertUtils.showError;

public class DynamicAnalysisDialogController {
    @FXML
    private TextField minField;

    @FXML
    private TextField maxField;

    @FXML
    private TextField stepField;

    @FXML
    private Slider valueSlider;

    @FXML
    private TextField cellIdField;  // New field to input cell ID

    @FXML
    private Button addCellButton;   // Button to add cell

    @FXML
    private ListView<String> selectedCellsListView;  // List to display selected cells

    private GridWindowController mainController;
    private List<String> selectedCells = new ArrayList<>();  // List to store multiple selected cell IDs
    private List<Double> originalValues = new ArrayList<>();  // List to store the original values of each cell

    @FXML
    private void handleAddCell() {
        String cellId = cellIdField.getText().trim();

        if (!cellId.isEmpty()) {
            try {
                // Add the cell to the analysis list
                addCellToAnalysis(cellId.toUpperCase());
                cellIdField.clear();  // Clear the input field after adding
            } catch (IOException e) {
                showError("Failed to add the cell to the analysis: " + e.getMessage());
            }
        } else {
            showError("Please enter a valid cell ID.");
        }
    }

    public void setMainController(GridWindowController gridWindowController) {
        this.mainController = gridWindowController;
    }

    // Method to add a cell to the dynamic analysis list
    public void addCellToAnalysis(String cellId) throws IOException {
        try {
            // Normalize cellId by removing leading zeros from the numeric part (e.g., B04 -> B4)
            cellId = cellId.replaceAll("([A-Z]+)(0*)(\\d+)", "$1$3");

            // Get CellDTO and save its original value before changes are made
            CellDTO cell = mainController.getCellDTOById(cellId);

            if (cell == null) {
                throw new IOException(cellId + " is empty or does not exist");
            }

            // Check if cell original value is a number
            Double.parseDouble(cell.getOriginalValue());

            originalValues.add(Double.parseDouble(cell.getEffectiveValue().toString()));
            selectedCells.add(cellId);

            // Update the ListView to show the selected cells
            selectedCellsListView.getItems().add(cellId);
        } catch (NumberFormatException e) {
            showError("Failed to add the cell to the analysis: Cell must be of number value and not created by a function");
        } catch (Exception e) {
            showError("Failed to add the cell to the analysis: " + e.getMessage());
        }
    }

    public void openDynamicAnalysisDialog() throws IOException {
        // Set listeners for dynamic behavior
        setListeners();

        // Show the dialog
        createAndShowDialog();
    }

    // Method to set all listeners for dynamic analysis controls
    private void setListeners() {
        setMinFieldListener();
        setMaxFieldListener();
        setStepFieldListener();
        setSliderListener();
    }

    // Sets listener for the minimum value field
    private void setMinFieldListener() {
        minField.textProperty().addListener((observable, oldValue, newValue) -> {
            // If the input is empty or a minus sign, reset to default minimum
            if (newValue.isEmpty() || newValue.equals("-")) {
                valueSlider.setMin(0);
            } else {
                try {
                    // Parse the new value and set it as the slider's minimum
                    double min = Double.parseDouble(newValue);
                    valueSlider.setMin(min);
                } catch (NumberFormatException e) {
                    // Revert to the old value if parsing fails
                    minField.setText(oldValue);
                }
            }
        });
    }

    // Sets listener for the maximum value field
    private void setMaxFieldListener() {
        maxField.textProperty().addListener((observable, oldValue, newValue) -> {
            // If the input is empty or a minus sign, reset to default maximum
            if (newValue.isEmpty() || newValue.equals("-")) {
                valueSlider.setMax(0);
            } else {
                try {
                    // Parse the new value and set it as the slider's maximum
                    double max = Double.parseDouble(newValue);
                    valueSlider.setMax(max);
                } catch (NumberFormatException e) {
                    // Revert to the old value if parsing fails
                    maxField.setText(oldValue);
                }
            }
        });
    }

    // Sets listener for the step size field
    private void setStepFieldListener() {
        stepField.textProperty().addListener((observable, oldValue, newValue) -> {
            // If the input is empty, reset to default step size
            if (newValue.isEmpty()) {
                valueSlider.setBlockIncrement(1);
            } else {
                try {
                    // Parse the new step size and set it for the slider
                    double step = Double.parseDouble(newValue);
                    if (step > 0) {
                        valueSlider.setBlockIncrement(step);
                        valueSlider.setMajorTickUnit(step);
                    } else {
                        // Revert to the old value if step size is not positive
                        stepField.setText(oldValue);
                    }
                } catch (NumberFormatException e) {
                    // Revert to the old value if parsing fails
                    stepField.setText(oldValue);
                }
            }
        });
    }

    // Sets listener for the slider value change to update multiple cells
    private void setSliderListener() {
        valueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double step = valueSlider.getMajorTickUnit();
            double min = valueSlider.getMin();
            double currentValue = newValue.doubleValue();

            // Calculate the difference between the current value and the min value
            double differenceFromMin = currentValue - min;

            // Check if the difference is a multiple of the step size
            if (Math.abs(differenceFromMin % step) < 1e-6) {  // Small tolerance for floating-point precision
                double tempValue = currentValue;

                // Update all selected cells with the slider value
                for (String cellId : selectedCells) {
                    mainController.updateCellValue(cellId, Double.toString(tempValue), true);
                }
            }
        });
    }

    // Method to create and display the dynamic analysis dialog
    private void createAndShowDialog() {
        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle("Dynamic Analysis for Multiple Cells");
        dialog.setHeaderText("Analyze changes dynamically for multiple cells.");

        dialog.getDialogPane().setContent(minField.getParent());  // Assuming minField is in a parent like VBox

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setOnCloseRequest(event -> revertChangesToOriginalValue());

        dialog.showAndWait();
    }

    // Method to revert any changes made during the dynamic analysis to the original value
    private void revertChangesToOriginalValue() {
        for (int i = 0; i < selectedCells.size(); i++) {
            String cellId = selectedCells.get(i);
            double originalValue = originalValues.get(i);
            String valueToRevert;

            if (originalValue % 1 == 0) {
                valueToRevert = Integer.toString((int) originalValue);
            } else {
                valueToRevert = Double.toString(originalValue);
            }

            mainController.updateCellValue(cellId, valueToRevert, true);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicAnalysisDialogController that = (DynamicAnalysisDialogController) o;
        return Objects.equals(minField, that.minField) && Objects.equals(maxField, that.maxField)
                && Objects.equals(stepField, that.stepField) && Objects.equals(valueSlider, that.valueSlider)
                && Objects.equals(cellIdField, that.cellIdField) && Objects.equals(addCellButton, that.addCellButton)
                && Objects.equals(selectedCellsListView, that.selectedCellsListView) && Objects.equals(mainController, that.mainController)
                && Objects.equals(selectedCells, that.selectedCells) && Objects.equals(originalValues, that.originalValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minField, maxField, stepField, valueSlider, cellIdField, addCellButton, selectedCellsListView, mainController, selectedCells, originalValues);
    }
}
