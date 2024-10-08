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
import java.util.Locale;

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

    private GridWindowController mainController;
    private double originalValue;
    private String cellId;

    public void setMainController(GridWindowController gridWindowController) {
        this.mainController = gridWindowController;
    }
//    public void openDynamicAnalysisDialog(String cellId) {
//        this.cellId = cellId;
//
//        // Fetch the cell asynchronously
//        gridWindowController.fetchCellById(cellId, new GridWindowController.CellCallback() {
//            @Override
//            public void onCellReceived(Cell cell) {
//                if (cell == null || !(cell.getEffectiveValue() instanceof Number)) {
//                    showError("Dynamic Analysis can only be performed on cells with numeric values.");
//                    return;
//                }
//                if (cell.getExpression() instanceof FunctionExpression) {
//                    showError("Dynamic Analysis cannot be performed on numbers made out of functions.");
//                    return;
//                }
//
//                originalValue = Double.parseDouble(cell.getEffectiveValue().toString());
//
//                // Set listeners for dynamic behavior
//                setListeners();
//
//                // Show the dialog
//                createAndShowDialog();
//            }
//        });
//    }


    public void openDynamicAnalysisDialog(String cellId) throws IOException {
        this.cellId = cellId;
        CellDTO cell = mainController.getCellDTOById(cellId);

        if (cell == null || !(cell.getEffectiveValue() instanceof Number)) {
            showError("Dynamic Analysis can only be performed on cells with numeric values.");
            return;
        }
        String originalValue = cell.getOriginalValue();
        Expression parseResult = mainController.parseExpression(originalValue);

        if (parseResult instanceof FunctionExpression) {
            showError("Dynamic Analysis cannot be performed on numbers made out of functions.");
            return;
        }

        this.originalValue = Double.parseDouble(cell.getEffectiveValue().toString());

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

    // Sets listener for the slider value change
    private void setSliderListener() {
        valueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Perform dynamic analysis whenever the slider value changes
            double tempValue = newValue.doubleValue();
            performDynamicAnalysis(tempValue);
        });
    }

    // Method to create and display the dynamic analysis dialog
    private void createAndShowDialog() {
        // Create a new dialog window
        Dialog<Void> dialog = new Dialog<>();

        // Set the dialog title and header text
        dialog.setTitle("Dynamic Analysis for " + cellId);
        dialog.setHeaderText("Analyze changes dynamically for cell: " + cellId);

        // Set the dialog content to the parent container of minField (e.g., a VBox)
        dialog.getDialogPane().setContent(minField.getParent()); // Assuming minField is in a VBox or another parent

        // Add a 'Close' button to the dialog
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Define the action to take when the dialog is closed
        dialog.setOnCloseRequest(event -> revertChangesToOriginalValue());

        // Display the dialog and wait for it to be closed
        dialog.showAndWait();
    }

    // Method to revert any changes made during the dynamic analysis to the original value
    private void revertChangesToOriginalValue() {
        // Revert the cell value back to its original value
        performDynamicAnalysis(originalValue);

        // Update the dependent cells with the original value
        mainController.updateDependentCellsForDynamicAnalysis(cellId, originalValue);
    }

    // Method to perform dynamic analysis by temporarily updating the cell value
    private void performDynamicAnalysis(double tempValue) {
        // Retrieve the StringProperty of the target cell
        StringProperty cellProperty = mainController.getCellProperty(cellId);

        if (cellProperty != null) {
            // Format the number with thousands separators and up to two decimal places
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            numberFormat.setMinimumFractionDigits(0);  // No minimum decimal digits
            numberFormat.setMaximumFractionDigits(2);  // Maximum 2 decimal digits

            // Format the temporary value according to the number format
            String formattedValue = numberFormat.format(tempValue);

            // Update the cell's property to display the formatted value
            cellProperty.set(formattedValue);

            // Update dependent cells dynamically based on the temporary value
            mainController.updateDependentCellsForDynamicAnalysis(cellId, tempValue);
        }
    }
}
