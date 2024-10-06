package versions;

import api.Expression;
import api.Range;
import cells.Cell;
import enums.PermissionStatus;
import enums.PermissionType;
import exceptions.engineexceptions.*;
import expressionimpls.ExpressionParser;
import expressionimpls.FunctionExpression;
import expressionimpls.RangeExpression;
import expressionimpls.ReferenceExpression;
import filter.SpreadsheetFilter;
import generatedschemafilesv2.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import ranges.RangesManager;
import spreadsheet.Spreadsheet;
import dto.*;
import versions.permissions.PermissionsManager;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;

public class VersionsManager implements Serializable {
    private final Map<Integer, Version> versions;
    private final RangesManager rangesManager;
    private final SpreadsheetFilter spreadsheetFilterer;
    private final PermissionsManager permissionsManager;
    private static final int MAX_ROWS = 50;
    private static final int MAX_COLS = 20;
    private Supplier<Spreadsheet> spreadsheetSupplier = this::getCurrentSpreadsheet;
    private int currentVersionNumber;

//    public VersionsManager(RangesManager rangesManager, SpreadsheetFilter spreadsheetFilterer) {
//        this.rangesManager = rangesManager;
//        this.spreadsheetFilterer = spreadsheetFilterer;
//        this.versions = new HashMap<>();
//        this.currentVersionNumber = 0;
//    }

    public VersionsManager(String username) {
        this.permissionsManager = new PermissionsManager(username);
        this.rangesManager = new RangesManager();
        this.spreadsheetFilterer = new SpreadsheetFilter(this);
        this.versions = new HashMap<>();
        this.currentVersionNumber = 0;
    }

    public void clearVersions() {
        versions.clear();
        currentVersionNumber = 0;
    }


    public SpreadsheetDTO convertSpreadsheetToDTO(Spreadsheet spreadsheet) {
        if (spreadsheet == null) {
            return null;
        }

        return new SpreadsheetDTO(
                spreadsheet.getName(),
                spreadsheet.getRows(),
                spreadsheet.getColumns(),
                spreadsheet.getColumnWidth(),
                spreadsheet.getRowHeight(),
                spreadsheet.getVersionNumber(),
                convertCellsToDTO(spreadsheet.getCells())
        );
    }

    // Method to convert cells to DTO
    private Map<String, CellDTO> convertCellsToDTO(Map<String, Cell> cells) {
        // Create basic CellDTOs without dependencies
        Map<String, CellDTO> cellDTOMap = new HashMap<>();
        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
            Cell cell = entry.getValue();
            CellDTO cellDTO = new CellDTO(
                    cell.getOriginalValue(),
                    cell.getEffectiveValue(),
                    cell.getLastUpdatedVersion(),
                    new ArrayList<>(), // Placeholder for dependsOnThemIds
                    new ArrayList<>()  // Placeholder for dependsOnMeIds
            );
            cellDTOMap.put(entry.getKey(), cellDTO);
        }
        // Second pass: Populate dependencies by adding cell IDs
        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
            String cellId = entry.getKey();
            Cell cell = entry.getValue();
            CellDTO cellDTO = cellDTOMap.get(cellId);

            // Populate dependsOnThemIds
            for (String dependsOnId : cell.getDependsOnThem().keySet()) {
                cellDTO.getDependsOnThemIds().add(dependsOnId);
            }

            // Populate dependsOnMeIds
            for (String dependsOnMeId : cell.getDependsOnMe().keySet()) {
                cellDTO.getDependsOnMeIds().add(dependsOnMeId);
            }
        }

        return cellDTOMap;
    }

    private RangeDTO convertRangeToDTO(Range range) {
        if (range == null) {
            return null; // Handle null case if necessary
        }
        return new RangeDTO(
                range.getName(), // Assuming Range has a getName() method
                range.getStartCell(),
                range.getEndCell(),
                range.getCells() // Assuming Range has a getCells() method returning List<String>
        );
    }


    public Spreadsheet getCurrentSpreadsheet() {
        if (currentVersionNumber == 0) {
            return null;
        }

        return versions.get(currentVersionNumber).getSpreadsheet();
    }

    public Spreadsheet getSpreadsheetByVersion(int versionNumber) throws IndexOutOfBoundsException {
        if (versionNumber <= 0 || versionNumber > currentVersionNumber) {
            throw new IndexOutOfBoundsException("The version number is invalid");
        }
        return versions.get(versionNumber).getSpreadsheet();
    }

    public void deleteLatestVersion() {
        if (currentVersionNumber == 0) {
            throw new IllegalStateException("No versions available to delete.");
        }

        // Remove the latest version from the map
        versions.remove(currentVersionNumber);

        // Decrement the currentVersion to point to the previous version
        currentVersionNumber--;

        // If all versions were removed, reset the version number to 0
        if (currentVersionNumber == 0) {
            clearVersions();
        }
    }

    public void saveNewVersion(String cellId, Spreadsheet spreadsheet) {
        int numOfCellsChanged = getNumOfCellsChanged(spreadsheet, cellId);
        Version newVersion = new Version(currentVersionNumber + 1, numOfCellsChanged, spreadsheet);

        versions.put(currentVersionNumber + 1, newVersion);
        currentVersionNumber++;
    }

    public Map<Integer, Version> getVersions() {
        return versions;
    }

    public int getCurrentVersion() {
        return currentVersionNumber;
    }

    public void setCurrentVersionNumber(int currentVersionNumber) {
        this.currentVersionNumber = currentVersionNumber;
    }

    // Helper method to get the number of cells that depend on a cell
    private int getNumOfCellsChanged(Spreadsheet currentSpreadsheet, String cellId) {
        Stack<String> stack = new Stack<>();    // Create a stack for the calculations
        Set<String> visited = new HashSet<>();  // Create a list for those already visited

        stack.push(cellId);    // Add first cell to stack
        visited.add(cellId);   // Add first cell to visited

        int totalNumOfDependants  = 1;     // Including the cell we change

        while (!stack.isEmpty()) {
            String currentCellId = stack.pop();
            Cell currentCell = currentSpreadsheet.getCells().get(currentCellId);
            if (currentCell == null) {
                continue;
            }

            // Iterate over the cells that depend on the current cell
            for (String dependantId : currentCell.getDependsOnMe().keySet()) {
                if (!visited.contains(dependantId)) {   // If we haven't visited this cell yet
                    stack.push(dependantId);            // Add it to the stack for processing
                    visited.add(dependantId);           // Mark it as visited
                    totalNumOfDependants++;             // Increment the count
                }
            }
        }

        return totalNumOfDependants;
    }

    // Method to load a spreadsheet from an XML file
    public void loadSpreadsheet(String filePath) throws
            SpreadsheetLoadingException, CellUpdateException, CircularReferenceException, RangeProcessException {
        // Create a deep copy of the current versions map
        Map<Integer, Version> originalVersions = new HashMap<>();

        // Get Versions Map
        Map<Integer, Version> versions = getVersions();

        // Get current version
        int prevCurrVersion = getCurrentVersion();

        for (Map.Entry<Integer, Version> entry : versions.entrySet()) {
            originalVersions.put(entry.getKey(), entry.getValue().deepCopy());
        }

        // Create a deep copy of the current ranges map
        Map<String, Range> originalRanges = new HashMap<>(rangesManager.getAllRanges());

        try {
            // Unmarshal the STLSheet from the XML file
            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File file = new File(filePath);
            STLSheet stlSheet = (STLSheet) unmarshaller.unmarshal(file);

            // Clear the versions map to prepare for the new data
            clearVersions();
            rangesManager.clearRanges();
            // Validate and load the spreadsheet into the cleared versions map
            validateAndLoadSpreadsheet(stlSheet);

        }  catch (JAXBException e) {
            // Restore the original map if loading fails
            versions.clear();
            this.setCurrentVersionNumber(prevCurrVersion);
            versions.putAll(originalVersions);
            rangesManager.clearRanges();
            rangesManager.getAllRanges().putAll(originalRanges);

            throw new SpreadsheetLoadingException("Failed to parse XML file: " + e.getMessage(), e);
        } catch (SpreadsheetLoadingException | CellUpdateException | CircularReferenceException | RangeProcessException e) {
            // Restore the original map if validation fails
            versions.clear();
            this.setCurrentVersionNumber(prevCurrVersion);
            versions.putAll(originalVersions);
            rangesManager.clearRanges();
            rangesManager.getAllRanges().putAll(originalRanges);

            throw e; // Re-throw the original exception
        }
    }

    private void validateAndLoadSpreadsheet(STLSheet stlSheet) throws
            SpreadsheetLoadingException, CellUpdateException, CircularReferenceException, RangeProcessException {
        // Validate the sheet dimensions
        validateSheetDimensions(stlSheet);

        // Initialize the Spreadsheet object
        Spreadsheet spreadsheet = initializeSpreadsheet(stlSheet);

        // Create a new version for the spreadsheet
        createVersion(spreadsheet);

        // Process ranges
        processRanges(stlSheet);

        // Validate and load cells
        processCells(stlSheet);
    }

    private void validateSheetDimensions(STLSheet stlSheet) throws SpreadsheetLoadingException {
        int rows = stlSheet.getSTLLayout().getRows();
        int columns = stlSheet.getSTLLayout().getColumns();

        if (rows < 1 || rows > MAX_ROWS) {
            throw new SpreadsheetLoadingException("Row count must be between 1 and " + MAX_ROWS);
        }
        if (columns < 1 || columns > MAX_COLS) {
            throw new SpreadsheetLoadingException("Column count must be between 1 and " + MAX_COLS);
        }
    }

    private Spreadsheet initializeSpreadsheet(STLSheet stlSheet) {
        int newVersionNumber = 0;
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setName(stlSheet.getName());
        spreadsheet.setRows(stlSheet.getSTLLayout().getRows());
        spreadsheet.setColumns(stlSheet.getSTLLayout().getColumns());
        spreadsheet.setRowHeight(stlSheet.getSTLLayout().getSTLSize().getRowsHeightUnits());
        spreadsheet.setColumnWidth(stlSheet.getSTLLayout().getSTLSize().getColumnWidthUnits());
        spreadsheet.setVersionNumber(++newVersionNumber);
        this.setCurrentVersionNumber(newVersionNumber);

        return spreadsheet;
    }

    private void createVersion(Spreadsheet spreadsheet) {
        Version newVersion = new Version(spreadsheet.getVersionNumber(), 0, spreadsheet);
        getVersions().put(spreadsheet.getVersionNumber(), newVersion);
    }

    private void processCells(STLSheet stlSheet) throws
            SpreadsheetLoadingException, CellUpdateException, CircularReferenceException {
        List<STLCell> cellList = stlSheet.getSTLCells().getSTLCell();
        int numOfCellsChanged = 0;
        int currentVersion = getCurrentVersion();
        Map<Integer, Version> versions = this.getVersions();

        for (STLCell stlCell : cellList) {
            String cellId = stlCell.getColumn() + stlCell.getRow();
            String cellValue = stlCell.getSTLOriginalValue().strip();

            try {
                validateCellIdWhenLoading(cellId, stlSheet);
                cellId = cellId.toUpperCase();
                Cell cell = getOrCreateCell(cellId, versions.get(currentVersion).getSpreadsheet());
                cell.setOriginalValue(cellValue);
                cell.setLastUpdatedVersion(currentVersion);
                parseAndApplyNewExpressionToCell(cellId, cellValue, cell, versions.get(currentVersion).getSpreadsheet());
                numOfCellsChanged++;
            } catch (InvalidCellIdFormatException | InvalidColumnException | InvalidRowException e) {
                throw new SpreadsheetLoadingException(e.getMessage());
            }
        }
        try {
            versions.get(currentVersion).getSpreadsheet().recalculateEffectiveCellValues(1);
        }catch (CircularReferenceException e) {
            throw new CircularReferenceException("Circular reference detected: " + e.getMessage());
        }


        // Update the number of cells changed in the version
        versions.get(currentVersion).setNumOfCellsChanged(numOfCellsChanged);
    }

    private void processRanges(STLSheet stlSheet) throws RangeProcessException {
        // Assuming STLRanges is part of STLSheet
        STLRanges stlRanges = stlSheet.getSTLRanges(); // Adjust according to actual method of accessing ranges

        if (stlRanges != null) {
            for (STLRange stlRange : stlRanges.getSTLRange()) {
                String rangeName = stlRange.getName();
                STLBoundaries boundaries = stlRange.getSTLBoundaries();

                if (boundaries != null) {
                    String from = boundaries.getFrom();
                    String to = boundaries.getTo();

                    // Add the range to the RangesManager
                    try {
                        // Leverage addRange for validation and addition
                        addRange(rangeName, from, to);
                    } catch (Exception e) {
                        throw new RangeProcessException(e.getMessage());
                    }
                }
            }
        }
    }


    // Method to validate a cell ID based on spreadsheet size
    private void validateCellIdWhenLoading(String cellId, STLSheet sheet) throws
            InvalidColumnException, InvalidRowException, InvalidCellIdFormatException {

        // Check if cellId is null
        if (cellId == null) {

            throw new InvalidCellIdFormatException(cellId);
        }

        // Convert the cell ID to uppercase to handle case insensitivity
        cellId = cellId.toUpperCase();

        // Validate the cell ID format using uppercase
        if (!cellId.matches("^[A-Z]+[0-9]+$")) {
            throw new InvalidCellIdFormatException(cellId);
        }

        // Extract column letters and row number from the cell ID
        String columnPart = cellId.replaceAll("[0-9]", "");
        String rowPart = cellId.replaceAll("[A-Z]", "");

        // Check if there's at least one letter and one digit
        if (columnPart.isEmpty() || rowPart.isEmpty()) {
            throw new InvalidCellIdFormatException(cellId);
        }

        char column = columnPart.charAt(0);
        int row = Integer.parseInt(rowPart);

        // Determine the maximum valid column and row based on the spreadsheet size
        int maxColumnIndex = sheet.getSTLLayout().getColumns() - 1; // 0-indexed
        char maxColumn = (char) ('A' + maxColumnIndex);
        int maxRow = sheet.getSTLLayout().getRows();

        // Validate the column part
        if (column < 'A' || column > maxColumn) {
            throw new InvalidColumnException(cellId, column, maxColumn);
        }

        // Validate the row part
        if (row < 1 || row > maxRow) {
            throw new InvalidRowException(cellId, row, maxRow);
        }
    }

    public void updateCellValue(String cellId, String newValue)
            throws CircularReferenceException, CellUpdateException {
        // Get the current spreadsheet instance
        Spreadsheet currentSpreadsheet = getCurrentSpreadsheet();

        // Change cellId to be uppercased for case-sensitivity
        cellId = cellId.toUpperCase();

        // Strip new value of white spaces
        newValue = newValue.strip();

        // We want to create a new version, so create a new spreadsheet
        currentSpreadsheet = new Spreadsheet(currentSpreadsheet);

        // Ensure that a spreadsheet is loaded, otherwise throw an exception
        validateSpreadsheetLoaded(currentSpreadsheet);

        // Retrieve the cell by its ID, or create a new one if it doesn't exist
        Cell cell = getOrCreateCell(cellId, currentSpreadsheet);

        // Store the current original value
        String currentOriginalValue = cell.getOriginalValue();

        // Determine if the new value is different from the original value in the cell
        boolean valueChanged = !newValue.equals(currentOriginalValue);

        // Set the original value of the cell to the new value provided by the user
        cell.setOriginalValue(newValue);

        // If the new value is empty, clear the cell's content
        if (isNewValueEmpty(newValue)) {
            clearCell(cellId, currentSpreadsheet);
        } else {
            // Otherwise, update the cell with the new value, parsing and recalculating dependencies
            parseAndApplyNewExpressionToCell(cellId, newValue, cell, currentSpreadsheet);
        }

        // If the cell was new or its value changed, create a new version of the spreadsheet
        if (isNewCellOrValueChanged(cell, valueChanged)) {
            // Update the cell's last updated version
            cell.setLastUpdatedVersion(getCurrentVersion() + 1);
            saveNewVersion(cellId, currentSpreadsheet);
            try {
                // Recalculate the entire spreadsheet to update the effective values of all dependent cells
                getCurrentSpreadsheet().recalculateEffectiveCellValues(getCurrentVersion());
            } catch (Exception e) {
                this.deleteLatestVersion();
                throw e;
            }
        }
    }

    // Validate that a spreadsheet is loaded
    private void validateSpreadsheetLoaded(Spreadsheet spreadsheet) throws CellUpdateException {
        if (spreadsheet == null) {
            throw new CellUpdateException("No spreadsheet is currently loaded. Please load a spreadsheet and try again.");
        }
    }

    // Get an existing cell or create a new one if it doesn't exist
    protected Cell getOrCreateCell(String cellId, Spreadsheet spreadsheet) {
        cellId = cellId.toUpperCase();
        Cell cell = spreadsheet.getCellById(cellId);

        if (cell == null) {
            cell = new Cell();
            spreadsheet.addCell(cellId, cell);
        }

        return cell;
    }

    // Check if the new value is empty
    private boolean isNewValueEmpty(String newValue) {
        return newValue == null || newValue.trim().isEmpty();
    }

    // Clear the value of a cell
    private void clearCell(String cellId, Spreadsheet spreadsheet) {
        spreadsheet.clearCellValue(cellId);
    }

    // Update the cell with the new value
    private void parseAndApplyNewExpressionToCell(String cellId, String newValue, Cell cell, Spreadsheet spreadsheet)
            throws CircularReferenceException, CellUpdateException {
        try {
            // Parse the new value into an Expression, which could be a function or a literal
            //Supplier<Spreadsheet> spreadsheetSupplier = this::getCurrentSpreadsheet;
            Expression newExpression = ExpressionParser.parse(newValue, spreadsheetSupplier, rangesManager);

            // Check for any circular references that might be introduced by the new expression
            checkForCircularReferences(cellId, newExpression);

            // Update the cell's expression with the newly parsed expression
            cell.setExpression(newExpression);

            // Update the dependencies of the cell based on the new expression
            // This ensures that the cell correctly tracks which other cells it depends on and which cells depend on it
            updateDependencies(cellId, cell, newExpression, spreadsheet);
        } catch (CircularReferenceException e) {
            // If a circular reference is detected, rethrow the exception to indicate the error
            throw e;
        } catch (Exception e) {
            throw new CellUpdateException("Failed to update cell: " + e.getMessage());
        }
    }

    // Check if the cell is new or its value has changed
    private boolean isNewCellOrValueChanged(Cell cell, boolean valueChanged) {
        return cell.getOriginalValue().isEmpty() || valueChanged;
    }


    // Method to check for circular references in the new expression
    public void checkForCircularReferences(String cellId, Expression newExpression) throws CircularReferenceException {
        if (newExpression instanceof FunctionExpression) {
            List<Expression> arguments = ((FunctionExpression) newExpression).getArguments();

            for (Expression argument : arguments) {
                if (argument instanceof ReferenceExpression) {
                    String referencedCellId = ((ReferenceExpression) argument).getCellId();

                    // Check if the referenced cell is the same as the one being updated
                    if (cellId.equals(referencedCellId) || isDependentOn(cellId, referencedCellId)) {
                        throw new CircularReferenceException("Circular reference detected: " + referencedCellId + " depends on " + cellId);
                    }
                }
            }
        }
    }

    // Method to check if a cell depends on another cell (directly or indirectly)
    private boolean isDependentOn(String cellId, String potentialDependencyId) {
        Cell potentialDependency = getCurrentSpreadsheet().getCellById(potentialDependencyId);

        if (potentialDependency == null) {
            return false;
        }

        // Direct dependency check
        if (potentialDependency.getDependsOnThem().containsKey(cellId)) {
            return true;
        }

        // Indirect dependency check (recursive)
        for (String dependentCellId : potentialDependency.getDependsOnThem().keySet()) {
            if (isDependentOn(cellId, dependentCellId)) {
                return true;
            }
        }

        return false;
    }

    private void updateDependencies(String cellId, Cell cell, Expression newExpression, Spreadsheet spreadsheet) {
        // Clear current dependencies
        for (String dependencyId : cell.getDependsOnThem().keySet()) {
            Cell dependency = spreadsheet.getCellById(dependencyId);

            if (dependency != null) {
                dependency.removeDependsOnMe(cellId);
            }
        }
        cell.getDependsOnThem().clear();

        // Recursively add new dependencies
        addDependencies(cellId, cell, newExpression, spreadsheet);
    }

    private void addDependencies(String cellId, Cell cell, Expression expression, Spreadsheet spreadsheet) {
        if (expression instanceof ReferenceExpression) {
            String dependencyId = ((ReferenceExpression) expression).getCellId();

            // Validate the cell ID before creating a new cell or adding the dependency
            try {
                validateCellId(dependencyId, spreadsheet);
            } catch (Exception e) {
                throw new RuntimeException("Invalid cell ID: " + dependencyId + ". " + e.getMessage());
            }

            // Check if the dependency cell exists
            Cell dependency = spreadsheet.getCellById(dependencyId);

            // If the dependency doesn't exist, create it
            if (dependency == null) {
                dependency = spreadsheet.getOrCreateCell(dependencyId);
            }

            // Add the new dependency
            cell.addDependsOnThem(dependencyId, dependency);
            dependency.addDependsOnMe(cellId, cell);

        } else if (expression instanceof FunctionExpression) {
            List<Expression> args = ((FunctionExpression) expression).getArguments();
            for (Expression arg : args) {
                // Recursively add dependencies for each argument
                addDependencies(cellId, cell, arg, spreadsheet);
            }
        } else if (expression instanceof RangeExpression) {
            // Handle RangeExpression
            RangeExpression rangeExpression = (RangeExpression) expression;
            Range range = rangeExpression.getRange();

            // Check if the range exists
            if (range != null) {
                for (String rangeCellId : range.getCells()) {
                    // Validate the cell ID before creating a new cell or adding the dependency
                    try {
                        validateCellId(rangeCellId, spreadsheet);
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid cell ID in range: " + rangeCellId + ". " + e.getMessage());
                    }

                    // Check if the range cell exists
                    Cell rangeCell = spreadsheet.getCellById(rangeCellId);

                    // If the range cell doesn't exist, create it
                    if (rangeCell == null) {
                        rangeCell = spreadsheet.getOrCreateCell(rangeCellId);
                    }

                    // Add the new dependency for each cell in the range
                    cell.addDependsOnThem(rangeCellId, rangeCell);
                    rangeCell.addDependsOnMe(cellId, cell);
                }
            }
        }
    }

    // Ensure that the cell ID is valid and corresponds to an existing cell within the spreadsheet
    private void validateCellId(String cellId, Spreadsheet spreadsheet) throws InvalidCellIdFormatException, InvalidRowException, InvalidColumnException {
        if (cellId == null || !cellId.matches("^[A-Z]+[0-9]+$")) {
            throw new InvalidCellIdFormatException(cellId);
        }

        int row = Integer.parseInt(cellId.replaceAll("[^0-9]", ""));
        String col = cellId.replaceAll("[^A-Z]", "");

        int colIndex = col.charAt(0) - 'A' + 1;

        if (row < 1 || row > spreadsheet.getRows()) {
            throw new InvalidRowException(cellId, row, spreadsheet.getRows());
        }

        if (colIndex < 1 || colIndex > spreadsheet.getColumns()) {
            throw new InvalidColumnException(cellId, col.charAt(0), (char) ('A' + spreadsheet.getColumns() - 1));
        }
    }

    public Supplier<Spreadsheet> getSpreadsheetSupplier() {
        return spreadsheetSupplier;
    }

    public void setSpreadsheetSupplier(Supplier<Spreadsheet> spreadsheetSupplier) {
        this.spreadsheetSupplier = spreadsheetSupplier;
    }

    public void addRange(String rangeName, String firstCell, String lastCell) throws Exception {
        // Turn cell id's to uppercase
        firstCell = firstCell.toUpperCase();
        lastCell = lastCell.toUpperCase();

        // Validate cell IDs
        validateCellId(firstCell, getCurrentSpreadsheet());
        validateCellId(lastCell, getCurrentSpreadsheet());

        // Check if the first cell is greater than the last cell
        if (isFirstCellGreater(firstCell, lastCell)) {
            throw new Exception("Invalid Range: The first cell is greater than the last cell.");
        }

        // Add the range using the RangesManager
        rangesManager.addRange(rangeName, firstCell, lastCell);
    }


    // Helper method to check if the first cell is greater than the last cell
    private boolean isFirstCellGreater(String firstCell, String lastCell) {
        // Extract column letters and row numbers
        String firstColumn = firstCell.replaceAll("[^A-Z]", "");
        String lastColumn = lastCell.replaceAll("[^A-Z]", "");
        int firstRow = Integer.parseInt(firstCell.replaceAll("[^0-9]", ""));
        int lastRow = Integer.parseInt(lastCell.replaceAll("[^0-9]", ""));

        // Compare column letters
        if (firstColumn.compareTo(lastColumn) > 0) {
            return true; // First cell's column is greater
        } else if (firstColumn.equals(lastColumn)) {
            // If columns are the same, compare row numbers
            return firstRow > lastRow;
        }

        return false; // First cell is not greater
    }

    public Range getRange(String rangeName) {
        return this.rangesManager.getRange(rangeName);
    }

    public void removeRange(String rangeName) throws Exception {
        // Check if the range is currently in use
        if (isRangeInUse(rangeName.toUpperCase())) {
            throw new Exception(
                    "The range '" + rangeName + "' is currently in use by one or more cells. Please update or remove the reference before deleting.");
        }

        // Delete the range using the RangesManager
        rangesManager.deleteRange(rangeName.toUpperCase());
    }

    public Map<String, Range> getAllRanges() {
        // Retrieve all ranges from the RangesManager
        return rangesManager.getAllRanges();
    }

    public List<String> getRangeValues(String rangeName) {
        Range range = getRange(rangeName);
        List<String> values = new ArrayList<>();

        // Fetch all cells within the range
        String startCell = range.getStartCell();
        String endCell = range.getEndCell();

        // Determine the starting and ending rows and columns
        int startRow = Integer.parseInt(startCell.replaceAll("\\D", ""));
        int endRow = Integer.parseInt(endCell.replaceAll("\\D", ""));
        int startColumnIndex = getColumnIndex(startCell.replaceAll("\\d", ""));
        int endColumnIndex = getColumnIndex(endCell.replaceAll("\\d", ""));

        // Loop through each cell in the range and extract values
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startColumnIndex; col <= endColumnIndex; col++) {
                String cellId = getColumnName(col) + row;
                Cell cell = getCurrentSpreadsheet().getCellById(cellId);

                if (cell != null && cell.getEffectiveValue() != null) {
                    // Add all values (numeric or non-numeric) to the list
                    values.add(cell.getEffectiveValue().toString());
                }
            }
        }

        return values;
    }

    public boolean isRangeInUse(String rangeName) {
        // Retrieve the current spreadsheet
        Spreadsheet currentSpreadsheet = getCurrentSpreadsheet();

        if (currentSpreadsheet == null) {
            return false;
        }

        // Iterate through all cells in the spreadsheet
        for (Cell cell : currentSpreadsheet.getCells().values()) {
            // Check if the cell's expression uses the specified range
            if (cell.getExpression() instanceof FunctionExpression) {
                FunctionExpression functionExpression = (FunctionExpression) cell.getExpression();

                for (Expression argument : functionExpression.getArguments()) {
                    if (argument instanceof RangeExpression) {
                        RangeExpression rangeExpression = (RangeExpression) argument;

                        if (rangeName.equalsIgnoreCase(rangeExpression.getRangeName())) {
                            return true; // Range is in use
                        }
                    }
                }
            }
        }
        return false; // Range is not in use
    }

    public  Map<String, String> sortSpreadsheet(Spreadsheet spreadsheet, String range, List<String> columnsToSortBy) throws InvalidColumnException {

        if (spreadsheet == null) {
            throw new IllegalStateException("No spreadsheet loaded to sort.");
        }

        // Perform sorting on the specified range using the columns to sort by
        // Create a map of old cell IDs to new cell IDs after sorting
        Map<String,String> idMapping = spreadsheet.sort(range, columnsToSortBy);

        return idMapping; // Return the mapping of old cell IDs to new cell IDs
    }


    // Helper method to convert a column letter (e.g., "A") to a zero-based index
    public int getColumnIndex(String columnName) {
        int index = 0;
        for (char c : columnName.toCharArray()) {
            index = index * 26 + (c - 'A' + 1);
        }
        return index - 1; // Zero-based index
    }


    // Helper method to convert a zero-based column index to an Excel-style column name (A, B, C, ..., Z, AA, AB, ...)
    public String getColumnName(int index) {
        StringBuilder columnName = new StringBuilder();
        while (index >= 0) {
            columnName.insert(0, (char) ('A' + (index % 26)));
            index = (index / 26) - 1;
        }
        return columnName.toString();
    }

    public List<String[][]> filterTableMultipleColumns(String tableArea, Map<String, List<String>> selectedColumnValues) {
        return this.spreadsheetFilterer.filterTableMultipleColumns(tableArea, selectedColumnValues);
    }

    public Expression parseExpression (String input) throws InvalidExpressionException {
        return ExpressionParser.parse(input, spreadsheetSupplier, rangesManager);
    }

    public PermissionsManager getPermissionsManager() {
        return this.permissionsManager;
    }

    public String getUploaderName() {
        return permissionsManager.getOwner();
    }

    public PermissionType getUserPermission(String username) {
        return permissionsManager.getUserPermission(username);
    }

    public void askForPermission(String username, PermissionType permissionType) {
        this.permissionsManager.askForPermission(username, permissionType);
    }

    public void handlePermissionRequest(String applicantName, String handlerName, PermissionStatus permissionStatus, PermissionType permissionType) {
        this.permissionsManager.handlePermissionRequest(applicantName, handlerName, permissionStatus, permissionType);
    }

    public int getRows() {
        return getCurrentSpreadsheet().getRows();
    }

    public int getCols() {
        return getCurrentSpreadsheet().getColumns();
    }

    public String getSpreadsheetName() {
        return getCurrentSpreadsheet().getName();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionsManager that = (VersionsManager) o;
        return currentVersionNumber == that.currentVersionNumber && Objects.equals(versions, that.versions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versions, currentVersionNumber);
    }
}
