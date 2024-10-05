package gridwindow.grid;

import dto.CellDTO;
import dto.SpreadsheetDTO;
import gridwindow.api.SpreadsheetController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import cells.Cell;

import javafx.scene.control.MenuItem;
import gridwindow.GridWindowController;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainGridAreaController implements SpreadsheetController {

    private GridWindowController mainController;
    private boolean isPopup = false; // Default is false, indicating it’s not a popup

    @FXML
    private GridPane spreadsheetGrid;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private ColorPicker backgroundColorPicker; // For selecting cell background color

    @FXML
    private ColorPicker textColorPicker; // For selecting cell text color

    private Map<String, StringProperty> cellProperties; // Map to store properties for each cell ID
    private final Map<Integer, Label> columnHeaders = new HashMap<>(); // Store column header labels
    private final Map<Integer, Label> rowHeaders = new HashMap<>(); // Store row header labels
    private Map<String, String> cellBackgroundColors = new HashMap<>();
    private Map<String, String> cellTextColors = new HashMap<>();
    private Map<String, String> cellAlignments = new HashMap<>();
    private final Map<String, String> originalColors = new HashMap<>();
    private final Map<String, TextField> textFieldMap = new HashMap<>();


    @Override
    public void start(SpreadsheetDTO spreadsheet, boolean isPopup) {
        this.isPopup = isPopup;
        this.cellProperties = new HashMap<>(); // Initialize the map for cell properties

        initializeSpreadsheet(spreadsheet); // Initialize the grid with the current spreadsheet
    }

    @Override
    public void initializeSpreadsheet(SpreadsheetDTO spreadsheet) {
        int rowCount = spreadsheet.getRows();
        int columnCount = spreadsheet.getColumns();
        int columnWidth = spreadsheet.getColumnWidth();
        int rowHeight = spreadsheet.getRowHeight();

        createColumnHeaders(columnCount, columnWidth);
        createRowHeaders(rowCount, rowHeight);
        createCells(spreadsheet, rowCount, columnCount, columnWidth, rowHeight);

        configureScrollPane();
    }

    public void clearGrid() {
        spreadsheetGrid.getChildren().clear(); // Assuming you are using a GridPane for displaying the cells
    }

    public void setCellStyles(Map<String, String> cellBackgroundColors, Map<String, String> cellTextColors, Map<String, String> cellAlignments, Map<String, String> idMapping) {
        this.cellBackgroundColors = cellBackgroundColors;
        this.cellTextColors = cellTextColors;
        this.cellAlignments = cellAlignments;

        // Create new maps for updated styles, initially copying existing ones for unmapped cells
        Map<String, String> updatedBackgroundColors = new HashMap<>(cellBackgroundColors);
        Map<String, String> updatedTextColors = new HashMap<>(cellTextColors);
        Map<String, String> updatedAlignments = new HashMap<>(cellAlignments);

        // Update the styles for the mapped cells
        for (Map.Entry<String, String> mapping : idMapping.entrySet()) {
            String oldCellId = mapping.getKey();
            String newCellId = mapping.getValue();

            // Update background colors for mapped cells
            if (cellBackgroundColors.containsKey(oldCellId)) {
                updatedBackgroundColors.put(newCellId, cellBackgroundColors.get(oldCellId));
                updatedBackgroundColors.remove(oldCellId); // Remove old ID entry
            }

            // Update text colors for mapped cells
            if (cellTextColors.containsKey(oldCellId)) {
                updatedTextColors.put(newCellId, cellTextColors.get(oldCellId));
                updatedTextColors.remove(oldCellId); // Remove old ID entry
            }

            // Update alignments for mapped cells
            if (cellAlignments.containsKey(oldCellId)) {
                updatedAlignments.put(newCellId, cellAlignments.get(oldCellId));
            }
        }

        // Update the styles with the new cell IDs
        this.cellBackgroundColors = updatedBackgroundColors;
        this.cellTextColors = updatedTextColors;
        this.cellAlignments = updatedAlignments;

        // Apply styles to each cell based on the updated maps
        for (Map.Entry<String, TextField> entry : textFieldMap.entrySet()) {
            applyCombinedStyle(entry.getValue());
        }
        for (String cellId : cellBackgroundColors.keySet()) {
            applyCombinedStyle(textFieldMap.get(cellId));
        }
        for (String cellId : cellTextColors.keySet()) {
            applyCombinedStyle(textFieldMap.get(cellId));
        }
        for (String cellId : cellAlignments.keySet()) {
            applyCombinedStyle(textFieldMap.get(cellId));
        }
    }

    public Map<String, String> getCellBackgroundColors() {
        return cellBackgroundColors;
    }

    public Map<String, String> getCellTextColors() {
        return cellTextColors;
    }

    public Map<String, String> getCellAlignments() {
        return cellAlignments;
    }

    public Map<String, TextField> getTextFieldMap() {
        return textFieldMap;
    }

    @Override
    public void createColumnHeaders(int columnCount, int columnWidth) {
        for (int col = 0; col <= columnCount; col++) {
            final int currentCol = col;
            String columnName = col == 0 ? "" : getColumnName(col - 1);
            Label header = new Label(columnName);
            header.getStyleClass().add("column-header");
            header.setPrefWidth(columnWidth);
            header.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHalignment(header, javafx.geometry.HPos.CENTER);

            if (col == 0)
                continue;

            // Create context menu for column header
            ContextMenu contextMenu = new ContextMenu();
            MenuItem alignLeft = new MenuItem("Align Left");
            MenuItem alignCenter = new MenuItem("Align Center");
            MenuItem alignRight = new MenuItem("Align Right");

            // Add alignment options
            alignLeft.setOnAction(event -> setColumnAlignment(currentCol, "LEFT"));
            alignCenter.setOnAction(event -> setColumnAlignment(currentCol, "CENTER"));
            alignRight.setOnAction(event -> setColumnAlignment(currentCol, "RIGHT"));

            // Create Spinner for adjusting column width
            Spinner<Integer> widthSpinner = new Spinner<>(1, 500, (int) columnWidth); // Minimum value is 1, maximum 500, initial is columnWidth
            widthSpinner.setPrefWidth(60); // Set preferred width of the spinner
            widthSpinner.setEditable(true);
            widthSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                header.setPrefWidth(newValue); // Update column width dynamically
            });

            // Create menu item for adjusting column width
            CustomMenuItem widthItem = new CustomMenuItem(widthSpinner, false);
            widthItem.setText("Adjust Column Width");

            contextMenu.getItems().addAll(alignLeft, alignCenter, alignRight, widthItem); // Add all items to context menu
            header.setContextMenu(contextMenu);

            spreadsheetGrid.add(header, col, 0);
            columnHeaders.put(col, header);
        }
    }

    @Override
    public void createRowHeaders(int rowCount, int rowHeight) {
        for (int row = 1; row <= rowCount; row++) {
            String rowNumber = String.format("%02d", row);
            final int rowLambda = row;
            Label header = new Label(rowNumber);
            header.getStyleClass().add("row-header");
            header.setPrefWidth(60);
            header.setPrefHeight(rowHeight);
            header.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHalignment(header, javafx.geometry.HPos.CENTER);

            // Add right-click context menu to change row height
            ContextMenu contextMenu = new ContextMenu();

            // Create a spinner for adjusting the row height
            Spinner<Integer> heightSpinner = new Spinner<>(10, 500, rowHeight); // Min 10, Max 500, Initial rowHeight
            heightSpinner.setPrefWidth(70); // Set width of the spinner
            heightSpinner.setEditable(true);

            // When the spinner value changes, update the row height
            heightSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                setRowHeight(rowLambda, newValue); // Set the new row height
            });

            // Add the spinner to a custom menu item
            CustomMenuItem heightMenuItem = new CustomMenuItem(heightSpinner, false);
            heightMenuItem.setText("Adjust Row Height");

            // Add the custom menu item to the context menu
            contextMenu.getItems().add(heightMenuItem);

            // Set the context menu on the row header (right-click to show)
            header.setContextMenu(contextMenu);

            spreadsheetGrid.add(header, 0, row);
            rowHeaders.put(row, header);
        }
    }

    @Override
    public void createCells(SpreadsheetDTO spreadsheet, int rowCount, int columnCount, int columnWidth, int rowHeight) {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        for (int row = 1; row <= rowCount; row++) {
            for (int col = 1; col <= columnCount; col++) {
                String cellId = getColumnName(col - 1) + row;
                CellDTO cell = spreadsheet.getCellById(cellId);

                StringProperty cellProperty = cellProperties.computeIfAbsent(cellId, key -> {
                    String cellEffectiveValue = "";
                    if (cell != null && cell.getEffectiveValue() != null) {
                        Object value = cell.getEffectiveValue();
                        if (value instanceof Number) {
                            cellEffectiveValue = numberFormat.format(((Number) value).doubleValue());
                        } else if (value instanceof Boolean){
                            cellEffectiveValue = value.toString().toUpperCase();
                        } else {
                            cellEffectiveValue = value.toString();
                        }
                    }

                    return new SimpleStringProperty(cellEffectiveValue);
                });

                TextField textField = new TextField();
                textField.setId(cellId);
                textField.setPrefWidth(columnWidth);
                textField.setPrefHeight(rowHeight);
                textField.textProperty().bind(cellProperty);
                textField.setStyle("-fx-alignment: center;");
                textField.setEditable(false);
                textField.getStyleClass().add("text-field");

                textField.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        handleCellClick(cellId);
                    }
                });

                textField.setOnMouseExited(event -> resetCellColors());

                ContextMenu contextMenu = createCellContextMenu(textField);
                textField.setContextMenu(contextMenu);
                textFieldMap.put(cellId, textField);
                cellAlignments.put(cellId, "-fx-alignment: center;");
                spreadsheetGrid.add(textField, col, row);
            }
        }
    }

    // Method to update the height of all cells in the specified row
    private void setRowHeight(int row, int newHeight) {
        for (Node node : spreadsheetGrid.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            if (nodeRow != null && nodeRow == row) {
                if (node instanceof TextField) {
                    ((TextField) node).setPrefHeight(newHeight); // Update the height of each cell in the row
                }
            }
        }
    }
//    private void handleCellClick(String cellId) {
//        if (isPopup) {
//            return;
//        }
//
//        // Fetch the cell asynchronously
//        mainController.fetchCellById(cellId, new GridWindowController.CellCallback() {
//            @Override
//            public void onCellReceived(Cell currentCell) {
//                if (currentCell != null) {
//                    highlightDependencies(currentCell);
//                }
//
//                String lastUpdateVersion = (currentCell != null) ? Integer.toString(currentCell.getLastUpdatedVersion()) : "1";
//                String cellOriginalValue = (currentCell != null) ? String.valueOf(currentCell.getOriginalValue()) : "";
//
//                mainController.updateSelectedCellInfo(cellId, cellOriginalValue, lastUpdateVersion);
//            }
//        });
//    }

    private void handleCellClick(String cellId) {
        if (isPopup) {
            return;
        }

        Cell currentCell = mainController.getCellById(cellId);
        if (currentCell != null) {
            highlightDependencies(currentCell);
        }

        String lastUpdateVersion = (currentCell != null) ? Integer.toString(currentCell.getLastUpdatedVersion()) : "1";
        String cellOriginalValue = (currentCell != null) ? String.valueOf(currentCell.getOriginalValue()) : "";

        mainController.updateSelectedCellInfo(cellId, cellOriginalValue, lastUpdateVersion);
    }

    private void highlightDependencies(Cell currentCell) {
        for (String dependsOnId : currentCell.getDependsOnThem().keySet()) {
            changeCellBackground(dependsOnId, "#ADD8E6");
        }

        for (String dependsOnMeId : currentCell.getDependsOnMe().keySet()) {
            changeCellBackground(dependsOnMeId, "#90EE90");
        }
    }

    private void changeCellBackground(String cellId, String color) {
        TextField cellField = (TextField) getNodeByCellId(cellId);

        if (cellField != null) {
            if (!color.isEmpty()) {
                if (!originalColors.containsKey(cellId)) {
                    originalColors.put(cellId, cellField.getStyle());
                }
                String existingStyle = cellField.getStyle();
                String newStyle = updateStyleWithBackgroundColor(existingStyle, color);
                cellField.setStyle(newStyle);
            } else {
                cellField.setStyle(originalColors.getOrDefault(cellId, ""));
                originalColors.remove(cellId);
            }
        }
    }

    private String updateStyleWithBackgroundColor(String existingStyle, String newColor) {
        if (existingStyle.contains("-fx-background-color")) {
            return existingStyle.replaceAll("-fx-background-color: [^;]+;", "-fx-background-color: " + newColor + ";");
        } else {
            return existingStyle + "-fx-background-color: " + newColor + ";";
        }
    }

    private void resetCellColors() {
        for (Map.Entry<String, String> entry : originalColors.entrySet()) {
            String cellId = entry.getKey();
            String originalColor = entry.getValue();
            TextField cellField = (TextField) getNodeByCellId(cellId);
            if (cellField != null) {
                cellField.setStyle(originalColor);
            }
        }
        originalColors.clear();
    }

    private Node getNodeByCellId(String cellId) {
        for (Node node : spreadsheetGrid.getChildren()) {
            if (node instanceof TextField && cellId.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }

    private void configureScrollPane() {
        scrollPane.setContent(spreadsheetGrid);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    @Override
    public String getColumnName(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private void setColumnAlignment(int col, String alignment) {
        for (int row = 1; row <= rowHeaders.size(); row++) {
            TextField cell = (TextField) getNodeFromGridPane(col, row);

            if (cell != null) {
                switch (alignment) {
                    case "LEFT":
                        cellAlignments.put(cell.getId(), "-fx-alignment: center-left;");
                        break;
                    case "CENTER":
                        cellAlignments.put(cell.getId(), "-fx-alignment: center;");
                        break;
                    case "RIGHT":
                        cellAlignments.put(cell.getId(), "-fx-alignment: center-right;");
                        break;
                }
                applyCombinedStyle(cell);
            }
        }
    }

    private Node getNodeFromGridPane(int col, int row) {
        for (Node node : spreadsheetGrid.getChildren()) {
            Integer nodeCol = GridPane.getColumnIndex(node);
            Integer nodeRow = GridPane.getRowIndex(node);

            if (nodeCol != null && nodeRow != null && nodeCol == col && nodeRow == row) {
                return node;
            }
        }
        return null;
    }

    private ContextMenu createCellContextMenu(TextField cell) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem dynamicAnalysisItem = new MenuItem("Dynamic Analysis");
        dynamicAnalysisItem.setOnAction(e -> mainController.showDynamicAnalysisDialog(cell.getId())); // Use AppController to open dialog

        MenuItem backgroundColorItem = new MenuItem("Change Background Color");
        backgroundColorItem.setOnAction(e -> {
            ColorPicker backgroundColorPicker = new ColorPicker();

            Dialog<javafx.scene.paint.Color> dialog = new Dialog<>();
            dialog.setTitle("Select Background Color");
            dialog.getDialogPane().setContent(backgroundColorPicker);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(button -> button == ButtonType.OK ? backgroundColorPicker.getValue() : null);
            dialog.showAndWait().ifPresent(color -> {
                String hexColor = toHexString(color);
                cellBackgroundColors.put(cell.getId(), "-fx-background-color: " + hexColor + ";");
                applyCombinedStyle(cell);
            });
        });

        MenuItem textColorItem = new MenuItem("Change Text Color");
        textColorItem.setOnAction(e -> {
            ColorPicker textColorPicker = new ColorPicker();

            Dialog<javafx.scene.paint.Color> dialog = new Dialog<>();
            dialog.setTitle("Select Text Color");
            dialog.getDialogPane().setContent(textColorPicker);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(button -> button == ButtonType.OK ? textColorPicker.getValue() : null);
            dialog.showAndWait().ifPresent(color -> {
                String hexColor = toHexString(color);
                cellTextColors.put(cell.getId(), "-fx-text-fill: " + hexColor + ";");
                applyCombinedStyle(cell);
            });
        });

        MenuItem resetStyleItem = new MenuItem("Reset Style");
        resetStyleItem.setOnAction(e -> {
            cellBackgroundColors.remove(cell.getId());
            cellTextColors.remove(cell.getId());
            cellAlignments.put(cell.getId(), "-fx-alignment: center;");
            applyCombinedStyle(cell);
        });

        contextMenu.getItems().addAll(backgroundColorItem, textColorItem, dynamicAnalysisItem, resetStyleItem);

        return contextMenu;
    }

    private void applyCombinedStyle(TextField cell) {
        String backgroundColor = cellBackgroundColors.getOrDefault(cell.getId(), "");
        String textColor = cellTextColors.getOrDefault(cell.getId(), "");
        String alignment = cellAlignments.getOrDefault(cell.getId(), "");

        cell.setStyle(backgroundColor + textColor + alignment);
    }

    private String toHexString(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public void setMainController(GridWindowController gridWindowController) {
        this.mainController = gridWindowController;
    }

    public StringProperty getCellProperty(String cellId) {
        return cellProperties.get(cellId);
    }

    public void highlightRange(String firstCell, String lastCell, boolean isHighlight) {
        String color = isHighlight ? "#ebf207" : "";
        changeRangeBackground(firstCell, lastCell, color);
    }

    private void changeRangeBackground(String firstCell, String lastCell, String color) {
        int startRow = Integer.parseInt(firstCell.replaceAll("\\D", ""));
        int endRow = Integer.parseInt(lastCell.replaceAll("\\D", ""));
        String startColumn = firstCell.replaceAll("\\d", "");
        String endColumn = lastCell.replaceAll("\\d", "");

        for (int row = startRow; row <= endRow; row++) {
            for (char col = startColumn.charAt(0); col <= endColumn.charAt(0); col++) {
                String cellId = col + String.valueOf(row);
                changeCellBackground(cellId, color);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MainGridAreaController that = (MainGridAreaController) o;
        return isPopup == that.isPopup && Objects.equals(mainController, that.mainController)
                && Objects.equals(spreadsheetGrid, that.spreadsheetGrid) && Objects.equals(scrollPane, that.scrollPane)
                && Objects.equals(backgroundColorPicker, that.backgroundColorPicker) && Objects.equals(textColorPicker, that.textColorPicker)
                && Objects.equals(columnHeaders, that.columnHeaders) && Objects.equals(rowHeaders, that.rowHeaders)
                && Objects.equals(cellBackgroundColors, that.cellBackgroundColors) && Objects.equals(cellTextColors, that.cellTextColors)
                && Objects.equals(cellAlignments, that.cellAlignments) && Objects.equals(originalColors, that.originalColors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, isPopup, spreadsheetGrid, scrollPane, backgroundColorPicker, textColorPicker, cellProperties, columnHeaders, rowHeaders, cellBackgroundColors, cellTextColors, cellAlignments, originalColors);
    }

}