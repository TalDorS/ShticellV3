package gridwindow.leftside.filterdialog;

import cells.Cell;
import exceptions.engineexceptions.FileNotFoundException;
import exceptions.engineexceptions.UserNotFoundException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import gridwindow.GridWindowController;
import spreadsheet.Spreadsheet;
import javafx.scene.layout.HBox;

import java.util.*;
import java.util.stream.Collectors;

import static utils.AlertUtils.showError;

public class FilterDialogController {
    @FXML
    private TextField tableAreaField;

    @FXML
    private VBox columnsCheckBoxContainer; // Use VBox for checkboxes

    @FXML
    private Button selectValuesButton;

    private GridWindowController mainController;
    private Map<String, List<String>> selectedColumnValues = new HashMap<>();
    private Set<String> selectedColumns = new HashSet<>();

    @FXML
    public void initialize() {
        // Set the default behavior for the tableAreaField
        tableAreaField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (getValidatedTableArea() != null) {
                    try {
                        updateRelevantColumns();
                    } catch (UserNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    // Enable and show selectValuesButton
                    selectValuesButton.setDisable(false);
                    selectValuesButton.setOpacity(1.0);
                } else {
                    clearColumns();
                    // Disable and hide selectValuesButton
                    selectValuesButton.setDisable(true);
                    selectValuesButton.setOpacity(0.3);

                }
            } catch (UserNotFoundException e) {
                throw new RuntimeException(e);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void setMainController(GridWindowController mainController) {
        this.mainController = mainController;
    }

    private void clearColumns() {
        columnsCheckBoxContainer.getChildren().clear();
        selectedColumns.clear();
        selectedColumnValues.clear();
    }

    private void updateRelevantColumns() throws UserNotFoundException, FileNotFoundException {
        // Retrieve and validate the table area input from the user
        String tableArea = getValidatedTableArea();
        if (tableArea == null) return; // Exit if validation fails

        // Extract start and end columns from the table area
        String[] columns = extractStartAndEndColumns(tableArea);
        if (columns == null) return; // Exit if extraction fails

        String startColumn = columns[0].toUpperCase();
        String endColumn = columns[1].toUpperCase();

        // Retrieve the indices for the start and end columns
        int startColumnIndex = mainController.getColumnIndex(startColumn);
        int endColumnIndex = mainController.getColumnIndex(endColumn);

        updateMenuButtonWithRelevantColumns(startColumnIndex, endColumnIndex);
    }

    private String getValidatedTableArea() throws UserNotFoundException, FileNotFoundException {
        String tableArea = tableAreaField.getText().trim().toUpperCase();

        if (tableArea.isEmpty()) {
            return null;
        }

        if (!tableArea.contains("..")) {
            return null;
        }

        String[] areaParts = tableArea.split("\\.\\.");
        if (areaParts.length != 2) {
            return null;
        }

        // Check if both cells are valid
        String topLeftCell = areaParts[0].trim();
        String bottomRightCell = areaParts[1].trim();

        if (!isValidCell(topLeftCell) || !isValidCell(bottomRightCell)) {
            return null;
        }

        // Ensure that the top-left cell is not "greater than" the bottom-right cell
        if (!isValidCellRange(topLeftCell, bottomRightCell)) {
            return null;
        }

        return tableArea;
    }

    private String[] extractStartAndEndColumns(String tableArea) {
        String[] areaParts = tableArea.split("\\.\\.");
        String topLeftCell = areaParts[0].trim();
        String bottomRightCell = areaParts[1].trim();
        String startColumn = topLeftCell.replaceAll("\\d", "");
        String endColumn = bottomRightCell.replaceAll("\\d", "");

        return new String[]{startColumn, endColumn};
    }

    private boolean isValidCell(String cellReference) {
        if (!cellReference.matches("[A-Z]+\\d+")) {
            return false;
        }

        return isCellWithinBounds(cellReference);
    }

    private boolean isValidCellRange(String topLeftCell, String bottomRightCell) throws UserNotFoundException, FileNotFoundException {
        int startRow = Integer.parseInt(topLeftCell.replaceAll("\\D", ""));
        int endRow = Integer.parseInt(bottomRightCell.replaceAll("\\D", ""));
        String startColumn = topLeftCell.replaceAll("\\d", "");
        String endColumn = bottomRightCell.replaceAll("\\d", "");

        int startColumnIndex = mainController.getColumnIndex(startColumn);
        int endColumnIndex = mainController.getColumnIndex(endColumn);

        return (startColumnIndex < endColumnIndex) || (startColumnIndex == endColumnIndex && startRow <= endRow);
    }

    private void updateMenuButtonWithRelevantColumns(int startColumnIndex, int endColumnIndex) throws UserNotFoundException, FileNotFoundException {
        // Clear previous checkboxes
        columnsCheckBoxContainer.getChildren().clear();
        selectedColumns.clear();
        selectedColumnValues.clear();

        // Add new CheckBoxes for the relevant columns
        for (int i = startColumnIndex; i <= endColumnIndex; i++) {
            String columnName = mainController.getColumnName(i);
            CheckBox checkBox = new CheckBox(columnName);

            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    selectedColumns.add(columnName);
                    selectedColumnValues.put(columnName, new ArrayList<>());
                } else {
                    selectedColumns.remove(columnName);
                    selectedColumnValues.remove(columnName);
                }
            });

            columnsCheckBoxContainer.getChildren().add(checkBox);
        }

        // Make the container visible
        columnsCheckBoxContainer.setVisible(true);
    }


    @FXML
    private void handleSelectValues() {
        if (selectedColumns.isEmpty()) {
            showError("Select at least one column for filtering first.");
            return;
        }

        // For each selected column, fetch unique values from the main controller
        for (String column : selectedColumns) {
            List<String> uniqueValues = getUniqueValuesForColumn(column);

            // Allow the user to select multiple values for each column
            showValueSelectionDialog(column, uniqueValues);
        }
    }

    private void showValueSelectionDialog(String column, List<String> uniqueValues) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Column: " + column);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        ListView<CheckBox> listView = new ListView<>();
        ObservableList<CheckBox> checkBoxList = FXCollections.observableArrayList();

        // Create checkboxes for each unique value
        for (String value : uniqueValues) {
            CheckBox checkBox = new CheckBox(value);
            checkBox.setSelected(selectedColumnValues.getOrDefault(column, new ArrayList<>()).contains(value));
            checkBoxList.add(checkBox);
        }

        listView.setItems(checkBoxList);

        Button okButton = new Button("OK");
        okButton.setOnAction(e -> {
            List<String> selectedValues = checkBoxList.stream()
                    .filter(CheckBox::isSelected)
                    .map(CheckBox::getText)
                    .collect(Collectors.toList());

            // Check if no values are selected
            if (selectedValues.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select at least one value.");
                alert.showAndWait();
            } else {
                // If values are selected, update the selected values map and close the dialog
                selectedColumnValues.put(column, selectedValues);
                dialogStage.close();
            }
        });

        // HBox to align the button to the right
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().add(okButton);
        buttonBox.setPadding(new Insets(10));

        VBox vbox = new VBox(listView, buttonBox);
        vbox.setSpacing(10);
        Scene scene = new Scene(vbox, 300, 400);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    @FXML
    private void handleApplyFilter() throws UserNotFoundException, FileNotFoundException {
        // Parse the table area
        String tableArea = tableAreaField.getText().trim().toUpperCase();

        if (tableArea.isEmpty()) {
            showError("Table area is required. Please enter a table area and try again.");
            return;
        }

        // Ensure at least one column is selected and has values
        if (selectedColumnValues.isEmpty() || selectedColumnValues.values().stream().allMatch(List::isEmpty)) {
            showError("Select at least one column and its corresponding values to filter.");
            return;
        }

        // Perform filtering based on the selected area, columns, and values
        List<String[][]> filteredRows = mainController.filterTableMultipleColumns(tableArea, selectedColumnValues);

        // Show the filtered rows in a new pop-up window
        showFilteredResultsGrid(filteredRows);
    }

    private void showFilteredResultsGrid(List<String[][]> filteredRows) throws UserNotFoundException, FileNotFoundException {
        Stage stage = new Stage();
        stage.setTitle("Filtered Results");
        stage.initModality(Modality.APPLICATION_MODAL);

        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true); // Make grid lines visible
        gridPane.setHgap(2); // Horizontal gap between cells
        gridPane.setVgap(2); // Vertical gap between cells
        gridPane.setStyle("-fx-background-color: white;");

        // Get the real column names from the main grid
        List<String> columnNames = mainController.getCurrentColumns();

        int requiredColumns = filteredRows.isEmpty() ? 0 : filteredRows.get(0).length;

        // Add column headers
        addColumnHeadersToGridPane(gridPane, columnNames, requiredColumns);

        // Add row headers and data cells
        addRowHeadersAndDataCellsToGridPane(gridPane, filteredRows);

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        VBox vbox = new VBox(scrollPane);
        Scene scene = new Scene(vbox, 800, 600); // Adjusted the size for a more consistent appearance
        stage.setScene(scene);
        stage.show();
    }

    private void addColumnHeadersToGridPane(GridPane gridPane, List<String> columnNames, int requiredColumns) {
        for (int i = 0; i < requiredColumns; i++) {
            TextField header = new TextField(columnNames.get(i));
            header.setStyle("-fx-font-weight: bold; -fx-background-color: #d3d3d3; -fx-padding: 5;");
            header.setAlignment(Pos.CENTER);
            header.setMaxWidth(Double.MAX_VALUE);
            header.setMaxHeight(Double.MAX_VALUE);
            header.setEditable(false);
            GridPane.setHalignment(header, HPos.CENTER);
            gridPane.add(header, i + 1, 0); // Adding headers to the first row
        }
    }

    private void addRowHeadersAndDataCellsToGridPane(GridPane gridPane, List<String[][]> filteredRows) {
        // Get the relevant style from the main controller
        Map<String, TextField> textFieldMap = mainController.getTextFieldMap();

        for (int rowIndex = 0; rowIndex < filteredRows.size(); rowIndex++) {
            String[][] rowData = filteredRows.get(rowIndex);

            TextField rowHeader = new TextField(Integer.toString(rowIndex + 1));
            rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #d3d3d3; -fx-padding: 5;");
            rowHeader.setAlignment(Pos.CENTER);
            rowHeader.setMaxWidth(Double.MAX_VALUE);
            rowHeader.setMaxHeight(Double.MAX_VALUE);
            rowHeader.setEditable(false);
            GridPane.setHalignment(rowHeader, HPos.CENTER);
            gridPane.add(rowHeader, 0, rowIndex + 1); // Adding row headers to the first column

            for (int colIndex = 0; colIndex < rowData.length; colIndex++) {
                String cellId = rowData[colIndex][0]; // Get the cell ID
                String cellValue = rowData[colIndex][1]; // Get the cell value

                // Retrieve the corresponding TextField from the main grid
                TextField originalTextField = textFieldMap.get(cellId);

                // Create a new textField for the filtered cell
                TextField newCell = new TextField(cellValue);
                newCell.setStyle(originalTextField.getStyle());
                newCell.setPrefWidth(originalTextField.getPrefWidth());
                rowHeader.setPrefWidth(originalTextField.getPrefWidth());
                newCell.setPrefHeight(originalTextField.getPrefHeight());
                newCell.setEditable(false);

                gridPane.add(newCell, colIndex + 1, rowIndex + 1); // Populate data in cells
            }
        }
    }

    // Checks if a given cell reference (e.g., "A1") is within the bounds of the current spreadsheet
    public boolean isCellWithinBounds(String cellId) {
        // Ensure that a spreadsheet is loaded
        Spreadsheet currentSpreadsheet = mainController.getCurrentSpreadsheet();
        if (currentSpreadsheet == null) {
            return false; // No spreadsheet is loaded
        }

        // Validate the format of the cell ID (e.g., "A1")
        if (!cellId.matches("[A-Z]+\\d+")) {
            return false; // Invalid format
        }

        // Extract the column and row from the cell ID
        String columnPart = cellId.replaceAll("\\d", ""); // Get the letter(s) part
        String rowPart = cellId.replaceAll("\\D", ""); // Get the digit(s) part

        try {
            int row = Integer.parseInt(rowPart); // Convert row part to integer

            // Get the maximum number of columns and rows from the current spreadsheet
            int maxColumns = currentSpreadsheet.getColumns(); // Total columns in the spreadsheet
            int maxRows = currentSpreadsheet.getRows(); // Total rows in the spreadsheet

            // Convert column part to an index (e.g., "A" -> 0, "B" -> 1, ..., "AA" -> 26, etc.)
            int columnIndex = mainController.getColumnIndex(columnPart);

            // Check if the row and column are within valid spreadsheet boundaries
            return columnIndex >= 0 && columnIndex < maxColumns && row > 0 && row <= maxRows;

        } catch (NumberFormatException e) {
            // If row parsing fails, it's an invalid cell ID
            return false;
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getUniqueValuesForColumn(String column) {
        Spreadsheet currentSpreadsheet = mainController.getCurrentSpreadsheet();
        if (currentSpreadsheet == null) {
            return new ArrayList<>(); // Return an empty list if no spreadsheet is loaded
        }

        // Initialize a set to store unique values
        Set<String> uniqueValues = new HashSet<>();

        // Get all rows and retrieve values for the specified column
        for (int row = 1; row <= currentSpreadsheet.getRows(); row++) {
            String cellId = column + row; // Create cell ID like "A1", "B2", etc.
            Cell cell = currentSpreadsheet.getCellById(cellId);
            if (cell != null && cell.getEffectiveValue() != null) {
                uniqueValues.add(cell.getEffectiveValue().toString());
            }
        }

        return new ArrayList<>(uniqueValues); // Return the unique values as a list
    }

    @FXML
    private void handleClose() {
        // Close the filter dialog
        Stage stage = (Stage) tableAreaField.getScene().getWindow();
        stage.close();
    }
}
