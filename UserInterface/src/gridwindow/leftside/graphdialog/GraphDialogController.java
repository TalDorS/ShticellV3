package gridwindow.leftside.graphdialog;

import cells.Cell;
import dto.CellDTO;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import gridwindow.GridWindowController;
import utils.AlertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GraphDialogController {
    private GridWindowController mainController;

    @FXML
    private TextField xRangeTextField; // TextField for X axis range input

    @FXML
    private TextField yRangeTextField; // TextField for Y axis range input

    @FXML
    private RadioButton barGraphRadioButton; // RadioButton for selecting bar graph

    @FXML
    private RadioButton lineGraphRadioButton; // RadioButton for selecting line graph

    @FXML
    private VBox graphContainer; // VBox to display the chart

    @FXML
    private Button createGraphButton; // Button to create the graph

    private final Pattern rangePattern = Pattern.compile("[A-Z]+\\d+\\.\\.[A-Z]+\\d+"); // Pattern to validate input format

    public void setMainController(GridWindowController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        ToggleGroup graphTypeGroup = new ToggleGroup();
        barGraphRadioButton.setToggleGroup(graphTypeGroup);
        lineGraphRadioButton.setToggleGroup(graphTypeGroup);

        createGraphButton.setDisable(true);
        createGraphButton.setOpacity(0.5);

        // Add listeners to both text fields to validate input in real-time
        xRangeTextField.textProperty().addListener(this::handleRangeValidation);
        yRangeTextField.textProperty().addListener(this::handleRangeValidation);
    }

    private void handleRangeValidation(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        boolean isValidX = validateRange(xRangeTextField.getText().toUpperCase());
        boolean isValidY = validateRange(yRangeTextField.getText().toUpperCase());

        // Enable or disable the createGraphButton based on validity
        if (isValidX && isValidY) {
            createGraphButton.setDisable(false);
            createGraphButton.setOpacity(1);
        } else {
            createGraphButton.setDisable(true);
            createGraphButton.setOpacity(0.5);
        }
    }

    // Method to validate that the range is in the same column and contains numbers
    private boolean validateRange(String range) {
        if (range == null || !rangePattern.matcher(range).matches()) {
            return false; // Invalid format
        }

        // Extract the column letters and rows
        String[] parts = range.toUpperCase().split("\\.\\.");
        String startColumn = parts[0].replaceAll("\\d", "");
        String endColumn = parts[1].replaceAll("\\d", "");
        String startRow = parts[0].replaceAll("\\D", "");
        String endRow = parts[1].replaceAll("\\D", "");

        // Ensure columns are the same and rows contain only numbers
        if (!startColumn.equals(endColumn) || !isNumeric(startRow) || !isNumeric(endRow)) {
            return false;
        }

        return true; // Valid range
    }

    // Helper method to check if a string contains only numeric values
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    private void handleCreateGraphButton() {
        // Get and validate user input
        String xRange = xRangeTextField.getText().toUpperCase().trim();
        String yRange = yRangeTextField.getText().toUpperCase().trim();

        if (!validateInputRanges(xRange, yRange)) {
            return; // Exit if input validation fails
        }

        // Extract the rows and columns from input
        String[] xRangeParts = xRange.split("\\.\\.");
        String[] yRangeParts = yRange.split("\\.\\.");

        int xStart = getStartRow(xRangeParts[0]);
        int xEnd = getEndRow(xRangeParts[1]);
        int yStart = getStartRow(yRangeParts[0]);
        int yEnd = getEndRow(yRangeParts[1]);

        // Validate the rows and ranges
        if (!validateRowOrderAndSize(xStart, xEnd, yStart, yEnd)) {
            return; // Exit if row validation fails
        }

        // Extract the column names
        String xColumn = getColumnFromRange(xRangeParts[0]);
        String yColumn = getColumnFromRange(yRangeParts[0]);

        // Retrieve values from the spreadsheet
        List<String> xValuesList = getRangeValues(xColumn, xStart, xEnd);
        List<String> yValuesList = getRangeValues(yColumn, yStart, yEnd);

        // Validate that the retrieved values are non-empty
        if (!validateRetrievedValues(xValuesList, yValuesList)) {
            return; // Exit if values are invalid
        }

        // Convert Y values to double array
        double[] yValues = convertToDoubleArray(yValuesList);
        if (yValues == null) {
            AlertUtils.showError("Y-axis values must be numeric.");
            return; // Exit if conversion fails
        }

        // Handle graph creation based on the selected graph type
        handleGraphCreation(xValuesList, yValues);
    }

    private boolean validateInputRanges(String xRange, String yRange) {
        if (xRange.isEmpty() || yRange.isEmpty()) {
            AlertUtils.showError("Both X and Y ranges must be filled.");
            return false;
        }

        return true;
    }

    private int getStartRow(String range) {
        return Integer.parseInt(range.replaceAll("\\D", ""));
    }

    private int getEndRow(String range) {
        return Integer.parseInt(range.replaceAll("\\D", ""));
    }

    private String getColumnFromRange(String range) {
        return range.replaceAll("\\d", "");
    }

    private boolean validateRowOrderAndSize(int xStart, int xEnd, int yStart, int yEnd) {
        if (xStart > xEnd || yStart > yEnd) {
            AlertUtils.showError("The starting row number must be less than or equal to the ending row number.");
            return false;
        }
        if ((xEnd - xStart) != (yEnd - yStart)) {
            AlertUtils.showError("X and Y ranges must have the same number of rows.");
            return false;
        }
        return true;
    }

    private boolean validateRetrievedValues(List<String> xValuesList, List<String> yValuesList) {
        if (xValuesList.isEmpty() || yValuesList.isEmpty()) {
            AlertUtils.showError("Invalid X or Y range.");
            return false;
        }
        return true;
    }

    private double[] convertToDoubleArray(List<String> values) {
        double[] doubleValues = new double[values.size()];
        try {
            for (int i = 0; i < values.size(); i++) {
                doubleValues[i] = Double.parseDouble(values.get(i));
            }
        } catch (NumberFormatException e) {
            return null; // Return null if parsing fails
        }
        return doubleValues;
    }

    private void handleGraphCreation(List<String> xValuesList, double[] yValues) {
        if (barGraphRadioButton.isSelected()) {
            createBarGraph(xValuesList.toArray(new String[0]), yValues);
        } else if (lineGraphRadioButton.isSelected()) {
            double[] xNumericValues = convertToDoubleArray(xValuesList);
            if (xNumericValues != null) {
                createLineGraph(xNumericValues, yValues);
            } else {
                AlertUtils.showError("X-axis values must be numeric for a line graph.");
            }
        } else {
            AlertUtils.showError("Please select a graph type.");
        }
    }


    private void createBarGraph(String[] xValues, double[] yValues) {
        // Set X-axis as a CategoryAxis for categorical data
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xRangeTextField.getText().toUpperCase());

        // Set Y-axis as a NumberAxis for numerical data
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yRangeTextField.getText().toUpperCase());

        // Create BarChart with correct axis types
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Populate the series with data points
        for (int i = 0; i < xValues.length; i++) {
            series.getData().add(new XYChart.Data<>(xValues[i], yValues[i]));
        }

        // Add series to the bar chart
        barChart.getData().add(series);

        // Clear previous graphs and add the new chart
        graphContainer.getChildren().clear();
        graphContainer.getChildren().add(barChart);
    }

    private void createLineGraph(double[] xValues, double[] yValues) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X Axis");
        yAxis.setLabel("Y Axis");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < xValues.length; i++) {
            series.getData().add(new XYChart.Data<>(xValues[i], yValues[i]));
        }
        lineChart.getData().add(series);
        graphContainer.getChildren().clear();
        graphContainer.getChildren().add(lineChart);
    }

    public List<String> getRangeValues(String column, int startRow, int endRow) {
        List<String> values = new ArrayList<>();

        // Loop through each row from startRow to endRow in the specified column
        for (int row = startRow; row <= endRow; row++) {
            String cellId = column + row; // Construct cell ID (e.g., "A1", "A2", etc.)
            CellDTO cell = mainController.getCurrentSpreadsheetDTO().getCellById(cellId); // Get the cell by its ID

            // Check if the cell is not null and has an effective value
            if (cell != null && cell.getEffectiveValue() != null) {
                Object effectiveValue = cell.getEffectiveValue();
                if (effectiveValue instanceof Double) {
                    Double doubleValue = (Double) effectiveValue;
                    // Check if it's a whole number
                    if (doubleValue == doubleValue.intValue()) {
                        values.add(String.valueOf(doubleValue.intValue()));
                    } else {
                        values.add(doubleValue.toString());
                    }
                } else {
                    values.add(effectiveValue.toString());
                }
            } else {
                values.add(""); // If the cell is empty, add an empty string
            }
        }

        return values;
    }
}