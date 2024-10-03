package gridwindow;

import api.Engine;
import api.Expression;
import api.Range;
import cells.Cell;
import dto.CellDTO;
import dto.EngineDTO;
import dto.SpreadsheetDTO;
import dto.VersionDTO;
import engineimpl.EngineImpl;
import exceptions.engineexceptions.*;
import expressionimpls.LiteralExpression;
import gridwindow.top.*;
import gridwindow.top.Skin;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import spreadsheet.Spreadsheet;
import gridwindow.grid.MainGridAreaController;
import gridwindow.grid.dynamicanalysisdialog.DynamicAnalysisDialogController;
import gridwindow.leftside.LeftSideController;
import gridwindow.leftside.addrangedialog.AddRangeDialogController;
import gridwindow.leftside.sortdialog.SortDialogController;
import utils.AlertUtils;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

import java.util.List;
import java.util.stream.Collectors;

import static utils.AlertUtils.showAlert;
import static utils.AlertUtils.showError;
import static utils.CommonResourcesPaths.*;

public class GridWindowController {

    private Engine engine; //set by the menu window controller
    private List<FadeTransition> activeFadeTransitions = new ArrayList<>();  // List to store all active transitions
    private List<RotateTransition> activeRotateTransitions = new ArrayList<>();  // List to store all active transitions
    private String filePath; //set by the menu window controller
    private String userName;//set by the menu window controller

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TopGridWindowController topGridWindowComponentController;

    @FXML
    private OptionsBarController optionsBarComponentController;

    @FXML
    private LeftSideController leftSideComponentController;

    @FXML
    private MainGridAreaController mainGridAreaComponentController;

    @FXML
    private DynamicAnalysisDialogController dynamicAnalysisComponentController;

    @FXML
    private SortDialogController sortDialogController;

    @FXML
    private AddRangeDialogController addRangeDialogController;

    @FXML
    public void initialize() {

        if (optionsBarComponentController != null) {
            optionsBarComponentController.setMainController(this);
        }
        if (leftSideComponentController != null) {
            leftSideComponentController.setMainController(this);
        }
        if (mainGridAreaComponentController != null) {
            mainGridAreaComponentController.setMainController(this);
        }
        if(sortDialogController != null){
            sortDialogController.setMainController(this);
        }
        if(addRangeDialogController != null){
            addRangeDialogController.setMainController(this);
        }
        if(dynamicAnalysisComponentController != null){
            dynamicAnalysisComponentController.setMainController(this);
        }
        if(topGridWindowComponentController!=null){
            topGridWindowComponentController.setMainController(this);
        }

    }

    public void setName(String name) {
        if (topGridWindowComponentController != null) {
            topGridWindowComponentController.setUsername(name);
        }
    }

    public void setSpreadsheetData(String filePath) throws CellUpdateException, InvalidExpressionException,
            SpreadsheetLoadingException, RangeProcessException, CircularReferenceException {
        try{
            engine.loadSpreadsheet(userName, filePath); //
            EngineDTO engineDTO = engine.getEngineData(userName,filePath);
            int currentVersionNumber = engineDTO.getCurrentVersionNumber();
            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();

            // Update the table view with the new data and open the grid window when the view sheet button is pressed
            Platform.runLater(() -> {
                // Clear the grid (if necessary)
                mainGridAreaComponentController.clearGrid();

                // Update the current version label in the options bar
                optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber);

                // Populate the grid with the new spreadsheet data
                mainGridAreaComponentController.start(spreadsheetDTO, false);

                // Refresh any dependent UI elements (ranges, etc.)
                try {
                    leftSideComponentController.refreshRanges();
                } catch (UserNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        }catch (SpreadsheetLoadingException | CellUpdateException | InvalidExpressionException | CircularReferenceException | RangeProcessException e) {
            // Rethrow exceptions to be handled by the calling code or task
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //todo- move to another controller for the sceond screen ??
//    public void loadSpreadsheet(String filePath) throws SpreadsheetLoadingException, CellUpdateException, InvalidExpressionException,
//            CircularReferenceException, RangeProcessException {
//        // Load the spreadsheet and update components on the JavaFX Application Thread
//        try {
//            engine.loadSpreadsheet(filePath);
//            EngineDTO engineDTO = engine.getEngineData();
//            int currentVersionNumber = engineDTO.getCurrentVersionNumber();
//            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();
//
//            // Ensure UI updates are executed on the JavaFX Application Thread
//            Platform.runLater(() -> {
//                mainGridAreaComponentController.clearGrid(); // Add a clearGrid() method to your controller if it doesn't exist
//                optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber); // Update the current version label
//                mainGridAreaComponentController.start(spreadsheetDTO, false);
//                leftSideComponentController.refreshRanges();
//            });
//
//        } catch (SpreadsheetLoadingException | CellUpdateException | InvalidExpressionException | CircularReferenceException | RangeProcessException e) {
//            // Rethrow exceptions to be handled by the calling code or task
//            throw e;
//        }
//    }

    public void highlightRange(String firstCell, String lastCell, boolean isHighlight) {
        // Delegate to the MainGridAreaController
        if (mainGridAreaComponentController != null) {
            mainGridAreaComponentController.highlightRange(firstCell, lastCell, isHighlight);
        }
    }

    // Method that occurs when a cell is clicked in Main Grid Area, the info is then delivered to
    // OptionsBarController to be printed out in the right textFields.
    public void updateSelectedCellInfo(String cellId, String OriginalValue, String lastUpdateVersion) {
        if (optionsBarComponentController != null) {
            optionsBarComponentController.updateCellInfo(cellId, OriginalValue, lastUpdateVersion);
            optionsBarComponentController.clearActionLineInput();
        }
    }

    // Method that occurs when we try to update a cell in Options Bar Controller.
    // It updates the relevant cell with its new info
    public void updateCellValue(String cellId, String newValue) {
        try {

            // Update the value in the engine
            engine.updateCellValue(userName,filePath,cellId, newValue);
            EngineDTO engineDTO = engine.getEngineData(userName, filePath);
            int currentVersionNumber = engineDTO.getCurrentVersionNumber();
            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();
            CellDTO currentCellDTO = spreadsheetDTO.getCellById(cellId);

            // Update the StringProperty for the cell ID
            StringProperty cellProperty = mainGridAreaComponentController.getCellProperty(cellId);

            if (cellProperty != null) {
                cellProperty.set(currentCellDTO.getEffectiveValue().toString());
                updateSelectedCellInfo(cellId, currentCellDTO.getOriginalValue(), Integer.toString(currentCellDTO.getLastUpdatedVersion()));
                optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber); // Update the current version label

                Object effectiveValue = currentCellDTO.getEffectiveValue();
                String effectiveValueString = String.valueOf(effectiveValue);

                if (effectiveValue instanceof Boolean) {
                    effectiveValueString = effectiveValueString.toUpperCase();
                }

                cellProperty.set(effectiveValueString);
                optionsBarComponentController.updateCellInfo(cellId, currentCellDTO.getOriginalValue(), Integer.toString(currentCellDTO.getLastUpdatedVersion()));
            }

            // Update all dependent cells
            updateDependentCells(cellId,false);

        } catch (Exception e) { // Catch any exceptions thrown during the update
            // Show an error alert with the exception message
            showAlert(Alert.AlertType.ERROR, "Error Updating Cell", e.getMessage());
        }
    }

    // Method to update all dependent cells
    private void updateDependentCells(String cellId, Boolean isDynamicAnalysis) {
        try {
            // Retrieve the map of dependent cells from the current cell
            Spreadsheet currentSpreadsheet = engine.getCurrentSpreadsheet(userName, this.filePath);
            Map<String, Cell> dependentCellsMap = currentSpreadsheet.getCellById(cellId).getDependsOnMe();

            if (dependentCellsMap != null) { // Check if there are any dependent cells
                for (Map.Entry<String, Cell> entry : dependentCellsMap.entrySet()) {
                    String dependentCellId = entry.getKey();
                    Cell dependentCell = currentSpreadsheet.getCellById(dependentCellId);
                    StringProperty dependentCellProperty = mainGridAreaComponentController.getCellProperty(dependentCellId);

                    if (dependentCellProperty != null && dependentCell != null) {
                        // If it's part of a dynamic analysis, update effective value
                        if (isDynamicAnalysis) {
                            dependentCell.setEffectiveValue();
                        }

                        Object effectiveValue = dependentCell.getEffectiveValue();
                        String effectiveValueString = String.valueOf(effectiveValue);
                        if (effectiveValue instanceof Boolean) {
                            effectiveValueString = effectiveValueString.toUpperCase();
                        }

                        dependentCellProperty.set(effectiveValueString); // Update the StringProperty for the dependent cell
                    }

                    updateDependentCells(dependentCellId, false);
                }
            }

        } catch (Exception e) {
            // If there's an error in updating dependent cells, display an alert
            showAlert(Alert.AlertType.ERROR, "Error Updating Dependent Cells", "Failed to update dependent cells: " + e.getMessage());
        }
    }

    public void addNewRange(String name, String firstCell, String lastCell) {
        try {
            firstCell = firstCell.toUpperCase();
            lastCell = lastCell.toUpperCase();
            engine.addRange(userName,filePath,name, firstCell, lastCell); // Add range to the backend engine
            leftSideComponentController.addRangeToUI(name, firstCell, lastCell); // Update the UI
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Success", "Range created successfully.");
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Creating Range", e.getMessage());
        }
    }

    public void deleteRange(String rangeName) {
        try {
            // Delete range from the backend engine
            engine.removeRange(userName, filePath, rangeName);
            leftSideComponentController.refreshRanges(); // Refresh the ranges in UI
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Deleting Range", e.getMessage());
        }
    }

    public void handleSortRequest(String range, List<String> columnsToSortBy) {
        try {
            // Assuming you have a method in the engine to sort the spreadsheet
            Spreadsheet sortedSpreadsheet = new Spreadsheet(engine.getCurrentSpreadsheet(userName,filePath));
            Map<String,String> idMapping = engine.sortSpreadsheet(userName, filePath, sortedSpreadsheet, range, columnsToSortBy);

            // Step 2: Convert the sorted spreadsheet (domain model) to a SpreadsheetDTO
            SpreadsheetDTO sortedSpreadsheetDTO = engine.convertSpreadsheetToDTO(sortedSpreadsheet);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(SORT_DIALOG_FXML));
            Parent root = loader.load();
            sortDialogController = loader.getController(); // Get the controller after loading the FXML
            sortDialogController.setMainController(this); // Set the main controller

            // Now create a popup window to display the sorted results
            sortDialogController.showSortedResultsPopup(sortedSpreadsheetDTO, idMapping);

        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Sorting Error", "Failed to sort the spreadsheet: " + e.getMessage());
        }
    }

    // Method to get the current ranges from the backend engine
    public Map<String, String[]> getRanges() throws UserNotFoundException, FileNotFoundException {
        // Fetch the ranges from the backend engine
        Map<String, Range> ranges = engine.getAllRanges(userName, filePath);
        Map<String, String[]> formattedRanges = new HashMap<>();

        // Convert each Range object to a String[] format
        for (Map.Entry<String, Range> entry : ranges.entrySet()) {
            Range range = entry.getValue();
            String[] cells = {range.getStartCell(), range.getEndCell()};
            formattedRanges.put(entry.getKey(), cells);
        }

        return formattedRanges;
    }

    public List<VersionDTO> getVersionsForMenu() {

        EngineDTO engineDTO = engine.getEngineData(userName, this.filePath);
        Map<Integer, VersionDTO> versionMap = engineDTO.getVersions();

        // Convert the map values (VersionDTO) to a list and return it
        return versionMap.values().stream().collect(Collectors.toList());
    }


    public boolean isSpreadsheetLoaded() {
        return engine.getCurrentSpreadsheet(userName,filePath) != null;
    }

    public void setSkin(String theme) {
        Scene scene = scrollPane.getScene();

        if (scene != null) {
            // Clear existing stylesheets
            scene.getStylesheets().clear();
            Skin skin;
            try {
                skin = Skin.valueOf(theme.toUpperCase());
            } catch (IllegalArgumentException e) {
                skin = Skin.DEFAULT; // Fallback to default skin if skin isnt selected
            }

            String[] components = {"TopGridWindow.css", "OptionsBar.css", "LeftSide.css", "MainGridArea.css", "GridWindow.css","Back.css"};
            for (String component : components) {
                String cssPath = String.format("/gridwindow/styles/%s/%s", skin.getDirectoryName(), component);
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
        }
    }

    public void setAnimation(String animation) {
        Animation animationEnum = Animation.valueOf(animation.toUpperCase());
        Scene scene = scrollPane.getScene();

        if (scene != null) {
            Pane rootPane = (Pane) scene.lookup("#borderPane");

            if (rootPane != null) {
                stopAnimations();
                if (animationEnum != Animation.NONE) {
                    Platform.runLater(() -> {
                        switch (animationEnum) {
                            case FADE:
                                applyFadeTransition(rootPane);
                                break;
                            case ROTATE:
                                applyRotateTransition(rootPane);
                                break;
                            default:
                                break;
                        }
                    });
                }
            }
        }
    }


    private void stopAnimations() {
        if(!activeFadeTransitions.isEmpty()) {
            for (FadeTransition fadeTransition : activeFadeTransitions) {
                fadeTransition.stop();  // Stop each active fade transition

                // Restore the node's opacity to its initial value
                Node node = fadeTransition.getNode();
                if (node != null) {
                    node.setOpacity(1.0);  // Reset to fully opaque
                }
            }
            activeFadeTransitions.clear();  // Clear the list after stopping
        }

        // Stop all rotate transitions
        if (!activeRotateTransitions.isEmpty()) {
            for (RotateTransition rotateTransition : activeRotateTransitions) {
                rotateTransition.stop();
                Node node = rotateTransition.getNode();
                if (node != null) {
                    node.setRotate(0);  // Reset rotation
                }
            }
            activeRotateTransitions.clear();
        }
    }

    // Method to apply fade transitions to nodes
    public void applyFadeTransition(Pane rootPane) {
        for (Node node : rootPane.getChildren()) {
            if (node instanceof Label || node instanceof Text || node instanceof Button || node instanceof MenuButton || node instanceof TextField) {
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), node);  // Create a new transition for each node
                fadeTransition.setFromValue(1.0);
                fadeTransition.setToValue(0.1);
                fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
                fadeTransition.setAutoReverse(true);
                fadeTransition.play();

                activeFadeTransitions.add(fadeTransition);  // Keep track of all transitions
            } else if (node instanceof Pane) { // Handle Pane and its children
                applyFadeTransition((Pane) node);  // Recursively apply transitions to child nodes
            } else if (node instanceof SplitPane) { // Handle SplitPane and its items
                SplitPane splitPane = (SplitPane) node;
                for (Node splitPaneItem : splitPane.getItems()) {
                    if (splitPaneItem instanceof Pane) {
                        applyFadeTransition((Pane) splitPaneItem);
                    }
                }
            }
        }
    }

    private void applyRotateTransition(Pane rootPane) {
        for (Node node : rootPane.getChildren()) {
            if (node instanceof Label || node instanceof Text || node instanceof Button || node instanceof MenuButton || node instanceof TextField) {
                RotateTransition rotateTransition = new RotateTransition(Duration.millis(5000), node);
                rotateTransition.setFromAngle(0);
                rotateTransition.setToAngle(360);
                rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
                rotateTransition.setInterpolator(javafx.animation.Interpolator.LINEAR);
                rotateTransition.play();

                activeRotateTransitions.add(rotateTransition);
            } else if (node instanceof Pane) {
                applyRotateTransition((Pane) node);
            } else if (node instanceof SplitPane) {
                SplitPane splitPane = (SplitPane) node;
                for (Node splitPaneItem : splitPane.getItems()) {
                    if (splitPaneItem instanceof Pane) {
                        applyRotateTransition((Pane) splitPaneItem);
                    } else {
                        applyRotateTransition((Pane)splitPaneItem);
                    }
                }
            }
        }
    }

    public Cell getCellById(String cellId) {
        return engine.getCurrentSpreadsheet(userName,filePath).getCellById(cellId);
    }

    public Spreadsheet getSpreadsheetByVersion(int versionNumber) throws UserNotFoundException, FileNotFoundException {
        return engine.getSpreadsheetByVersion(userName, filePath,versionNumber);
    }

    public List<String> getCurrentColumns() throws UserNotFoundException, FileNotFoundException {
        Spreadsheet currentSpreadsheet = engine.getCurrentSpreadsheet(userName,filePath);
        if (currentSpreadsheet == null) {
            return new ArrayList<>(); // Return an empty list if no spreadsheet is loaded
        }

        int columnCount = currentSpreadsheet.getColumns(); // Assuming this returns the total number of columns
        List<String> columnNames = new ArrayList<>();

        for (int i = 0; i < columnCount; i++) {
            columnNames.add(getColumnName(i)); // Convert index to column name
        }

        return columnNames;
    }

    // Helper method to convert a zero-based column index to an Excel-style column name (A, B, C, ..., Z, AA, AB, ...)
    public String getColumnName(int index) throws UserNotFoundException, FileNotFoundException {
        return engine.getColumnName(userName, filePath,index);
    }

    public Spreadsheet getCurrentSpreadsheet() {
        return engine.getCurrentSpreadsheet(userName,filePath);
    }

    public List<String[][]> filterTableMultipleColumns(String tableArea, Map<String, List<String>> selectedColumnValues) throws UserNotFoundException, FileNotFoundException {
        return engine.filterTableMultipleColumns(userName, filePath, tableArea, selectedColumnValues);
    }

    // Helper method to convert a column letter (e.g., "A") to a zero-based index
    public int getColumnIndex(String columnName) throws UserNotFoundException, FileNotFoundException {
        return engine.getColumnIndex(userName, filePath,columnName);
    }

    public void updateDependentCellsForDynamicAnalysis(String cellId, double tempValue) {
        try {
            // Get the current spreadsheet
            Spreadsheet currentSpreadsheet = getCurrentSpreadsheet();

            if (currentSpreadsheet == null) {
                return;
            }

            // Temporarily update the value of the target cell in the spreadsheet's backend
            Cell cell = currentSpreadsheet.getCellById(cellId);

            if (cell != null) {
                cell.setExpression(new LiteralExpression(tempValue));
                cell.setEffectiveValue();
            }

            // Get the sorted list of cells to update using topological sort
            List<String> sortedCells = currentSpreadsheet.topologicalSort();

            // Update each cell in the topological order
            for (String sortedCellId : sortedCells) {
                Cell dependentCell = currentSpreadsheet.getCellById(sortedCellId);
                StringProperty dependentCellProperty = mainGridAreaComponentController.getCellProperty(sortedCellId);

                if (dependentCellProperty != null && dependentCell != null) {
                    dependentCell.setEffectiveValue();
                    Object effectiveValue = dependentCell.getEffectiveValue();
                    String effectiveValueString;

                    // Format the number with thousands separators and up to two decimal places
                    if (effectiveValue instanceof Number) {
                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                        numberFormat.setMinimumFractionDigits(0);  // No minimum decimal digits
                        numberFormat.setMaximumFractionDigits(2);  // Maximum 2 decimal digits
                        effectiveValueString = numberFormat.format(effectiveValue);
                    } else {
                        effectiveValueString = String.valueOf(effectiveValue);
                    }

                    dependentCellProperty.set(effectiveValueString); // Update the StringProperty for the dependent cell
                }
            }

        } catch (Exception e) {
            // Handle any errors during dynamic analysis
            showAlert(Alert.AlertType.ERROR, "Error Performing Dynamic Analysis", e.getMessage());
        }
    }

    // Method to get the StringProperty of a cell by its ID
    public StringProperty getCellProperty(String cellId) {
        return mainGridAreaComponentController.getCellProperty(cellId);
    }

    public void showDynamicAnalysisDialog(String cellId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(DYNAMIC_ANALYSIS_FXML));
            Parent root = loader.load();

            // Get the controller from the FXML loader
            dynamicAnalysisComponentController = loader.getController();

            // Set the main controller
            dynamicAnalysisComponentController.setMainController(this);

            // Pass the cell ID to the dialog
            dynamicAnalysisComponentController.openDynamicAnalysisDialog(cellId);
        } catch (IOException e) {
            showError("Failed to open dynamic analysis dialog: " + e.getMessage());
        }
    }

    public List<String> getRangeNames() throws UserNotFoundException, FileNotFoundException {
        // Fetch all ranges from the backend engine
        Map<String, Range> ranges = engine.getAllRanges(userName, filePath);

        // Extract range names and return them as a list
        return new ArrayList<>(ranges.keySet());
    }

    public Map<String, String> getCellAlignments() {
        return mainGridAreaComponentController.getCellAlignments();
    }

    public Map<String, String> getCellBackgroundColors() {
        return mainGridAreaComponentController.getCellBackgroundColors();
    }
    public Map<String, String> getCellTextColors() {
        return mainGridAreaComponentController.getCellTextColors();
    }

    public Map<String, TextField> getTextFieldMap() {
        return mainGridAreaComponentController.getTextFieldMap();
    }

    public EngineDTO getEngine() {
        return engine.getEngineData(userName, filePath);
    }

    public void checkForCircularReferences(String cellId, Expression newExpression) throws CircularReferenceException, UserNotFoundException, FileNotFoundException {
        engine.checkForCircularReferences(userName, filePath, cellId, newExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridWindowController that = (GridWindowController) o;
        return Objects.equals(engine, that.engine) && Objects.equals(scrollPane, that.scrollPane)
                && Objects.equals(optionsBarComponentController, that.optionsBarComponentController)
                && Objects.equals(leftSideComponentController, that.leftSideComponentController)
                && Objects.equals(mainGridAreaComponentController, that.mainGridAreaComponentController)
                && Objects.equals(sortDialogController, that.sortDialogController)
                && Objects.equals(addRangeDialogController, that.addRangeDialogController)
                && Objects.equals(dynamicAnalysisComponentController, that.dynamicAnalysisComponentController)
                && Objects.equals(topGridWindowComponentController, that.topGridWindowComponentController);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engine, scrollPane,
                optionsBarComponentController, leftSideComponentController,
                mainGridAreaComponentController, sortDialogController, addRangeDialogController,
                dynamicAnalysisComponentController, topGridWindowComponentController);
    }

    public Expression parseExpression (String input) throws InvalidExpressionException, UserNotFoundException, FileNotFoundException {
        return engine.parseExpression(userName, filePath, input);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }


}