package gridwindow.leftside.sortdialog;

import gridwindow.GridWindowController;
import dto.SpreadsheetDTO;
import exceptions.engineexceptions.InvalidCellIdFormatException;
import exceptions.engineexceptions.InvalidColumnException;
import exceptions.engineexceptions.InvalidRowException;
import exceptions.uiexceptions.CellNotFoundException;
import exceptions.uiexceptions.InvalidInputFormatException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import spreadsheet.Spreadsheet;
import gridwindow.grid.MainGridAreaController;
import utils.AlertUtils;

import java.awt.ScrollPane;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static utils.AlertUtils.showAlert;
import static utils.CommonResourcesPaths.MAIN_GRID_AREA_CSS;
import static utils.CommonResourcesPaths.MAIN_GRID_AREA_FXML;

public class SortDialogController {
    private GridWindowController mainController;

    @FXML
    private TextField selectedTableArea;

    @FXML
    private Button chooseRangeButton;

    @FXML
    private MenuButton sortByDropMenu;

    @FXML
    private Button sortButton;

    @FXML
    private Label chooseToProceedLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox sortColumnsContainer;
    @FXML
    private Button addAnotherColumnButton;

    private List<String> sortColumns = new ArrayList<>();
    private boolean lastColumnSet; //to track if we can add a new menu button
    private boolean isNewRangeSelected; //to track if a new range is selected

    public void setMainController(GridWindowController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {

        // Disable all buttons except the "Choose Range" button
        disableAllButtonsButChooseRange(true);
        // Any time the user clicks on the text field, disable all buttons except "Choose Range"
        selectedTableArea.setOnMouseClicked(event -> disableAllButtonsButChooseRange(true));
        // Handle the "Choose Range" button click
        chooseRangeButton.setOnAction(event -> handleChooseRangeButton(sortByDropMenu));
        // Handle the "Sort" button click
        sortButton.setOnAction(event -> handleSortButton());
        // Initialize sortByDropMenu to handle its dropdown menu being shown
        sortByDropMenu.setOnShowing(event -> handleSortByDropMenu((MenuButton) event.getSource()));

        lastColumnSet = false; // For tracking if the last column was set and there are no more columns to sort by
        isNewRangeSelected = false; // For tracking if a new range is selected so we can remove the columns that were added before
    }

    @FXML
    private void handleChooseRangeButton(MenuButton menuButton) {
        try {
            // Clear previous sort columns and reset UI
            resetStateAndUI();

            enableAllButtonsButChooseRange();

            // Add new menu items based on the updated range
            addSortColumnMenuItems(menuButton, sortColumns);

        } catch (InvalidCellIdFormatException | InvalidColumnException | CellNotFoundException | InvalidRowException | InvalidInputFormatException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void disableAllButtonsButChooseRange(boolean disable) {
        // Disable the buttons you want
        sortButton.setDisable(disable);
        cancelButton.setDisable(disable);
        addAnotherColumnButton.setDisable(disable);
        chooseRangeButton.setDisable(!disable);
        chooseToProceedLabel.setDisable(!disable);
        // Optionally, disable menu items in the drop-down as well
        sortByDropMenu.setDisable(disable);
    }

    private void enableAllButtonsButChooseRange() {
        disableAllButtonsButChooseRange(false);
    }

    private void resetStateAndUI() {
        // Clear sort columns and reset UI components
        sortColumns.clear();
        sortByDropMenu.getItems().clear();
        lastColumnSet = false;
        isNewRangeSelected = true; // Indicate that a new range is selected
        addAnotherColumnButton.setDisable(false); // Re-enable the button if necessary

        // Remove only dynamically added sort column rows
        sortColumnsContainer.getChildren().removeIf(node -> {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                // Check if the HBox has a "delete" button and is not the "addAnotherColumnButton"
                return hbox.getChildren().stream().anyMatch(child -> child instanceof Button && ((Button) child).getText().equals("X"));
            }
            return false;
        });
    }

    @FXML
    private void handleSortByDropMenu(MenuButton menuButton) {
        if(isNewRangeSelected) {
            try {
                addSortColumnMenuItems(menuButton, sortColumns);  // Original sortColumns used for the first menu
            } catch (InvalidCellIdFormatException | InvalidColumnException | CellNotFoundException |
                     InvalidRowException | InvalidInputFormatException e) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            } catch (Exception e) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        else{
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "Please select a range first");
        }
    }

    private void addSortColumnMenuItems(MenuButton menuButton, List<String> selectedColumns) throws Exception {
        String selectedArea = selectedTableArea.getText().trim();

        if (selectedArea.isEmpty()) {
            throw new IllegalArgumentException("Cell range cannot be empty.");
        }

        // Split the input (expecting format A1..B1)
        String[] cells = selectedArea.split("\\.\\.");
        if (cells.length != 2) {
            throw new InvalidInputFormatException(selectedArea,"Please enter a valid range in the format A1..B1.");
        }
        String firstCell = cells[0];
        String lastCell = cells[1];

        // Validate the user input (ensure cells are valid)
        if (isCellValid(firstCell, mainController.getCurrentSpreadsheet()) && isCellValid(lastCell, mainController.getCurrentSpreadsheet())) {
            char firstColumn = firstCell.charAt(0);
            char lastColumn = lastCell.charAt(0);

            firstColumn = Character.toUpperCase(firstColumn);
            lastColumn = Character.toUpperCase(lastColumn);

            if (firstColumn <= lastColumn) {
                menuButton.getItems().clear();

                // Populate the menu items with all columns between first and last
                for (char column = firstColumn; column <= lastColumn; column++) {
                    String columnName = "Column " + column;
                    MenuItem menuItem = new MenuItem(columnName);
                    menuItem.setOnAction(event -> {
                        // On selection, check if the column is already selected in any other drop-down
                        if (!sortColumns.contains(columnName)) {
                            String previousSelection = menuButton.getText();
                            if (!previousSelection.equals("Sort By")) {
                                sortColumns.remove(previousSelection);
                            }
                            menuButton.setText(columnName);
                            selectedColumns.add(columnName); // Add to the selected columns of this menu
                            lastColumnSet = true;
                        } else {
                            AlertUtils.showAlert(Alert.AlertType.WARNING, "Duplicate Column", "This column is already selected.");
                        }
                    });
                    menuButton.getItems().add(menuItem);
                }
            } else {
                throw new InvalidInputFormatException(selectedArea,"The first cell must come before the last cell.");
            }
        } else {
            throw new Exception("Invalid Cells: One of the selected cells isnt in the format- A1.");
        }
    }

    private void addSortColumn(String column) {
        sortColumns.add(column);
        sortByDropMenu.setText(column);
    }

    private void checkAndUpdateAddButtonState() throws InvalidInputFormatException {
        // Get the selected area from the text field
        String selectedArea = selectedTableArea.getText().trim();

        // Validate input
        if (selectedArea.isEmpty()) {
            throw new IllegalArgumentException("Cell range cannot be empty.");
        }

        // Split the input (expecting format A1..B1)
        String[] cells = selectedArea.split("\\.\\.");
        if (cells.length != 2) {
            throw new InvalidInputFormatException(selectedArea,"Please enter a valid range in the format A1..B1.");
        }

        // Extract first and last cells
        String firstCell = cells[0].trim();
        String lastCell = cells[1].trim();

        char firstColumn = Character.toUpperCase(firstCell.charAt(0));
        char lastColumn = Character.toUpperCase(lastCell.charAt(0));

        if (firstColumn > lastColumn) {
            throw new IllegalArgumentException("Invalid column range: first column cannot be greater than last column.");
        }
        // Determine the range of columns
        List<String> allPossibleColumns = new ArrayList<>();
        for (char column = firstColumn; column <= lastColumn; column++) {
            String columnName = "Column " + column;
            if (!sortColumns.contains(columnName)) {
                allPossibleColumns.add(columnName);
            }
        }

        // Enable or disable the add button based on available columns
        addAnotherColumnButton.setDisable(allPossibleColumns.isEmpty());
    }

    @FXML
    private void handleAddAnotherColumnButton() throws Exception {

        if(isNewRangeSelected || selectedTableArea.getText().isEmpty()) {
            try {
                checkAndUpdateAddButtonState();
            } catch (IllegalArgumentException e) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                return;
            }

            // If no columns are available, prevent adding more
            if (addAnotherColumnButton.isDisable()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Columns Available");
                alert.setHeaderText("No Columns to Add");
                alert.setContentText("All columns within the specified range have been added. You cannot add more.");
                alert.showAndWait();
                return;
            }

            if (!lastColumnSet) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Required");
                alert.setHeaderText("No Column Selected");
                alert.setContentText("Please select a column in the current drop-down menu before adding another.");
                alert.showAndWait();
                return;
            }

            if (sortColumns.size() == 5) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Limit exceeded");
                alert.setHeaderText("Can't Add more columns");
                alert.setContentText("You can't add more than 5 columns to sort by");
                alert.showAndWait();
                addAnotherColumnButton.setDisable(true);
                return;
            }

            // Create an independent list for each new MenuButton
            HBox sortColumnRow = new HBox(10);
            sortColumnRow.setAlignment(Pos.CENTER_LEFT);

            Label newSortByLabel = new Label("Then by");

            MenuButton newSortByMenuButton = new MenuButton("Sort By");
            newSortByMenuButton.setPrefWidth(105);

            // Handle selection of menu items for the new MenuButton
            newSortByMenuButton.setOnShowing(event -> {
                try {
                    addSortColumnMenuItems(newSortByMenuButton, sortColumns); // Use current global sortColumns

                } catch (Exception e) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            });

            Button deleteButton = new Button("X");
            deleteButton.setPrefWidth(30);
            deleteButton.setOnAction(event -> {
                sortColumnsContainer.getChildren().remove(sortColumnRow);
                sortColumns.remove(newSortByMenuButton.getText()); // Remove the deleted column from the global selection list
                // If this was the last button added, allow adding new buttons again
                lastColumnSet = true;

                // Check if all buttons were removed, reset lastColumnSet if needed
                if (sortColumnsContainer.getChildren().size() == 1) { // Only "addAnotherColumnButton" remains
                    lastColumnSet = false;
                }
                // Update the state of the add button after removal
                try {
                    checkAndUpdateAddButtonState();
                } catch (IllegalArgumentException e) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                    return;
                } catch (InvalidInputFormatException e) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                    return;            }
            });

            sortColumnRow.getChildren().addAll(newSortByLabel, newSortByMenuButton, deleteButton);

            int insertIndex = sortColumnsContainer.getChildren().indexOf(addAnotherColumnButton);
            sortColumnsContainer.getChildren().add(insertIndex, sortColumnRow);
            lastColumnSet = false;
        }
        else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "Please select a range first");
        }
    }

    @FXML
    private void handleSortButton() {
        try {
            String selectedRange = selectedTableArea.getText().trim();
            // Ensure that a range is selected and columns are chosen
            if (sortColumns.isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "No Columns Selected", "Please select at least one column to sort by.");
                return;
            }

            // Collect the columns in the correct order
            List<String> orderedSortColumns = new ArrayList<>();

            // Add the primary column from the main sortByDropMenu
            String primaryColumn = sortByDropMenu.getText();
            if (!primaryColumn.equals("Sort By")) {
                orderedSortColumns.add(primaryColumn);
            }

            // Add columns from the dynamically added menu buttons in sortColumnsContainer
            for (Node node : sortColumnsContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    for (Node child : hbox.getChildren()) {
                        if (child instanceof MenuButton) {
                            MenuButton menuButton = (MenuButton) child;
                            String selectedColumn = menuButton.getText();
                            if (!selectedColumn.equals("Sort By")) {
                                orderedSortColumns.add(selectedColumn);
                            }
                        }
                    }
                }
            }

            // Check if at least one valid column is selected
            if (orderedSortColumns.isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "No Valid Columns", "Please select valid columns to sort.");
                return;
            }
            // Send the selected range and columns to the main controller
            mainController.handleSortRequest(selectedRange, new ArrayList<>(sortColumns));
            // Optionally close the dialog or show a success message

        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while sorting: " + e.getMessage());
        }
    }

    public void showSortedResultsPopup(SpreadsheetDTO sortedSpreadsheet, Map<String,String> idMapping) {
        try {
            // Load the FXML file for the grid layout
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_GRID_AREA_FXML));
            Parent root = loader.load(); // Load the FXML to get the root node (Parent)

            // Get the controller instance
            MainGridAreaController gridController = loader.getController();
            gridController.setMainController(mainController);

            // Set the engine and initialize the grid for the correct version
            gridController.start(sortedSpreadsheet, true);

            gridController.setCellStyles(
                    mainController.getCellBackgroundColors(),
                    mainController.getCellTextColors(),
                    mainController.getCellAlignments(),
                    idMapping
            );

            Stage popupStage = new Stage();
            popupStage.setTitle("Sorted Spreadsheet Results");

            // Set the scene with the loaded root (which contains the GridPane)
            Scene popupScene = new Scene(root);

            // Load and apply the CSS
            String css = getClass().getResource(MAIN_GRID_AREA_CSS).toExternalForm();
            popupScene.getStylesheets().add(css);
            popupStage.setScene(popupScene);

            // Make the popup modal
            popupStage.initModality(Modality.APPLICATION_MODAL);

            popupStage.showAndWait(); // Show the popup and wait for it to be closed

        } catch (IOException e) {
            e.printStackTrace(); // Log the exception for debugging
        }
    }

    @FXML
    private void handleCancelButton() {
        // Close the sort popup without making changes
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    // Validate the cell ID format, column, row, and existence in the spreadsheet
    private boolean isCellValid(String cellId, Spreadsheet spreadsheet)
            throws InvalidCellIdFormatException, InvalidColumnException, InvalidRowException, CellNotFoundException {

        // Check if spreadsheet was loaded
        if (spreadsheet == null) {
            throw new RuntimeException("Couldn't find cell because no spreadsheet was found. Please load a spreadsheet and try again.");
        }

        // Convert the cell ID to uppercase to handle case insensitivity
        String tempCellId = cellId;
        cellId = cellId.toUpperCase();


        // Validate the cell ID format using uppercase
        if (!cellId.matches("^[A-Z]+[0-9]+$")) {
            throw new InvalidCellIdFormatException(tempCellId);
        }

        // Extract column letters and row number from the cell ID
        String columnPart = cellId.replaceAll("[0-9]", "");
        String rowPart = cellId.replaceAll("[A-Z]", "");

        // Check if the column part exceeds the allowed length (e.g., "AA" for more than 26 columns)
        if (columnPart.length() > 1) {
            throw new InvalidCellIdFormatException(tempCellId);
        }

        char column = columnPart.charAt(0);
        int row = Integer.parseInt(rowPart);

        // Determine the maximum valid column and row based on the spreadsheet size
        int maxColumnIndex = spreadsheet.getColumns() - 1; // 0-indexed
        char maxColumn = (char) ('A' + maxColumnIndex);
        int maxRow = spreadsheet.getRows();

        // Validate the column part
        if (column < 'A' || column > maxColumn) {
            throw new InvalidColumnException(cellId, column, maxColumn);
        }

        // Validate the row part
        if (row < 1 || row > maxRow) {
            throw new InvalidRowException(cellId, row, maxRow);
        }

        return true;
    }

}
