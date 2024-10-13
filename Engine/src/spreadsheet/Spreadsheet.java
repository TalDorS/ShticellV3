package spreadsheet;

import cells.Cell;
import exceptions.CircularReferenceException;
import exceptions.InvalidColumnException;

import java.io.Serializable;
import java.util.*;

// This class is for creating the spreadsheet and containing its data
public class Spreadsheet implements Serializable {
    private final Map<String, Cell> cells;
    private int rows;
    private int columns;
    private int columnWidth;
    private int rowHeight;
    private String name;
    private int versionNumber;
    private static final String EMPTY_STRING = "";

    // Default Constructor
    public Spreadsheet() {
        this.cells = new HashMap<>();
        this.versionNumber = 1;
    }

    // Deep copy constructor
    public Spreadsheet(Spreadsheet original) {
        this.cells = new HashMap<>();
        // Copy map
        for (Map.Entry<String, Cell> entry : original.cells.entrySet()) {
            this.cells.put(entry.getKey(), new Cell(entry.getValue()));
        }
        this.rows = original.rows;
        this.columns = original.columns;
        this.columnWidth = original.columnWidth;
        this.rowHeight = original.rowHeight;
        this.name = original.name;
        this.versionNumber = original.versionNumber;
    }
    // Parameterized Constructor for creating a Spreadsheet directly
    public Spreadsheet(String name, int rows, int columns,
                       int columnWidth, int rowHeight, int versionNumber) {
        this.cells = new HashMap<>(); // Initialize cells map
        this.rows = rows;
        this.columns = columns;
        this.columnWidth = columnWidth;
        this.rowHeight = rowHeight;
        this.name = name != null ? name : EMPTY_STRING; // Prevent null name
        this.versionNumber = versionNumber;
    }

    // Create a new cell in the spreadsheet or return an existing cell if it already exists
    public Cell getOrCreateCell(String cellId) {
        // Check if the cell exists
        Cell cell = cells.get(cellId);

        // If the cell doesn't exist, create it and add it to the map
        if (cell == null) {
            cell = new Cell();
            cells.put(cellId, cell);
        }

        return cell;
    }

    // Add a cell to the spreadsheet
    public void addCell(String cellId, Cell cell) { cells.put(cellId, cell); }

    // Get a cell from the spreadsheet by its ID
    public Cell getCellById(String cellId) {
        return cells.get(cellId);
    }

    // Get the value of a cell in the spreadsheet by its ID
    public Map<String, Cell> getCells() {
        return cells;
    }

    // Get the value of a cell in the spreadsheet by its ID
    public int getVersionNumber() {
        return versionNumber;
    }

    // Set the version number of the spreadsheet
    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    // Get the value of a cell in the spreadsheet by its ID
    public void setName(String name) {
        this.name = name;
    }

    // Get the value of a cell in the spreadsheet by its ID
    public void setRows(int rows) {
        this.rows = rows;
    }

    // Get the value of a cell in the spreadsheet by its ID
    public void setColumns(int columns) {
        this.columns = columns;
    }

    // Get the value of a cell in the spreadsheet by its ID
    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
    }

    // Get the value of a cell in the spreadsheet by its ID
    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    // Recalculate the effective value of each cell in the spreadsheet according to the topological sort and checks for circular references
    public void recalculateEffectiveCellValues(int currentVersion) throws CircularReferenceException {
        List<String> sortedCells = topologicalSort();

        for (String cellId : sortedCells) {
            Cell cell = cells.get(cellId);

            Object oldValue = cell.getEffectiveValue();  // Store the old value

            try {
                cell.setEffectiveValue();
                // If the value has changed, update the last updated version
                if (!cell.getEffectiveValue().equals(oldValue)) {
                    cell.setLastUpdatedVersion(currentVersion);
                }
            } catch (Exception e) {
                throw new CircularReferenceException("Error when recalculating spreadsheet: " + e.getMessage());
            }
        }
    }

    // Perform a topological sort on the cells to determine the order in which they should be recalculated
    public List<String> topologicalSort() throws CircularReferenceException {
        Map<String, Integer> inDegree = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        List<String> sortedCells = new ArrayList<>();

        // Initialize in-degree of each cell based on "DependsOnThem"
        for (String cellId : cells.keySet()) {
            inDegree.put(cellId, 0);
        }

        // Calculate in-degrees based on "DependsOnMe"
        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
            String cellId = entry.getKey();
            Cell cell = entry.getValue();

            for (String dependency : cell.getDependsOnMe().keySet()) {
                inDegree.put(dependency, inDegree.get(dependency) + 1);
            }
        }

        // Enqueue cells with zero in-degree
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        // Perform the topological sort
        while (!queue.isEmpty()) {
            String cellId = queue.poll();
            sortedCells.add(cellId);

            Cell cell = cells.get(cellId);

            for (String dependentCellId : cell.getDependsOnMe().keySet()) {
                inDegree.put(dependentCellId, inDegree.get(dependentCellId) - 1);
                if (inDegree.get(dependentCellId) == 0) {
                    queue.add(dependentCellId);
                }
            }
        }

        // If the number of sorted cells does not match the number of cells, a circular reference exists
        if (sortedCells.size() != cells.size()) {
            throw new CircularReferenceException("Circular reference detected in the spreadsheet.");
        }

        return sortedCells;
    }

    // Clear the expression of a cell, effectively resetting it
    public void clearCellValue(String cellId) {
        Cell cell = cells.get(cellId);
        if (cell != null) {
            cell.setExpression(null);  // Clear the expression, effectively resetting the cell
            cells.put(cellId, cell);   // Update the map with the cleared cell
        }
    }

    // This method sorts the rows of the spreadsheet based on the specified columns
    // It throws an InvalidColumnException if any of the specified columns contain non-numerical data
    public  Map<String,String> sort(String range, List<String> columnsToSortBy) throws InvalidColumnException {
        // Extract columns and rows from the specified range
        int[] rows = extractRows(range);

        // Extract rows within the specified range
        List<SpreadsheetRow> rowsToSort = getRowsInRange(rows[0], rows[1]);

        // Validate that all columns to sort by are numerical
        validateNumericalColumns(rowsToSort, columnsToSortBy, range);

        // Sort rows based on the specified columns
        rowsToSort.sort((row1, row2) -> {
            for (String column : columnsToSortBy) {
                String cleanColumn = stripColumnPrefix(column);
                int comparison = row1.compareCellValue(row2, cleanColumn);
                if (comparison != 0) {
                    return comparison; // Return comparison result if different
                }
            }
            return 0; // Rows are equal if all specified columns are equal
        });

        // Update the spreadsheet with sorted rows
      return updateSpreadsheetWithSortedRows(rowsToSort, rows[0], rows[1]);
    }

    // Validate that specified columns contain only numerical data within the specified range
    private void validateNumericalColumns(List<SpreadsheetRow> rowsToSort, List<String> columnsToSortBy, String range) throws InvalidColumnException {
        // Extract start and end columns from the range
        String[] columnsRange = extractStartAndEndColumns(range);
        String startColumn = columnsRange[0];
        String endColumn = columnsRange[1];

        startColumn = startColumn.toUpperCase();
        endColumn = endColumn.toUpperCase();

        // Convert column letters to indices for comparison
        int startColumnIndex = convertColumnLetterToIndex(startColumn);
        int endColumnIndex = convertColumnLetterToIndex(endColumn);

        // Extract start and end rows from the range
        int startRow = extractStartRow(range);
        int endRow = extractEndRow(range);

        // Iterate through the specified columns
        for (String column : columnsToSortBy) {
            String cleanColumn = stripColumnPrefix(column);             // Strip "Column" prefix
            int columnIndex = convertColumnLetterToIndex(cleanColumn); // Convert column letter to index

            // Ensure the column is within the specified range
            if (columnIndex < startColumnIndex || columnIndex > endColumnIndex) {
                continue; // Skip columns outside the range
            }

            // Check each row in the specified range
            for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
                SpreadsheetRow row = getRow(rowIndex); // Method to get the row by index
                if (row == null) {
                    continue; // Skip if row is not present
                }

                Cell cell = row.getCell(cleanColumn, rowIndex); // Method to get the cell by column and row
                if (cell == null) {
                    throw new InvalidColumnException(cleanColumn);
                }

                Object effectiveValue = cell.getEffectiveValue(); // Get the effective value of the cell
                if (!(effectiveValue instanceof Number)) {
                    throw new InvalidColumnException(cleanColumn);
                }

            }
        }
    }

    // Helper method to convert column letter to index
    private int convertColumnLetterToIndex(String columnLetter) {
        int columnIndex = 0;
        for (char ch : columnLetter.toCharArray()) {
            columnIndex = columnIndex * 26 + (ch - 'A' + 1);
        }
        return columnIndex;
    }

    // Helper method to strip "Column" prefix and extract column letter
    private String stripColumnPrefix(String column) {
        if (column.startsWith("Column")) {
            return column.substring("Column".length()).trim(); // Remove "Column" prefix
        }
        return column.trim(); // Return as is if no prefix
    }

    // Extract the starting and ending columns from the given range
    private String[] extractStartAndEndColumns(String tableArea) {
        String[] areaParts = tableArea.split("\\.\\.");
        String topLeftCell = areaParts[0].trim();
        String bottomRightCell = areaParts[1].trim();
        String startColumn = topLeftCell.replaceAll("\\d", "");
        String endColumn = bottomRightCell.replaceAll("\\d", "");

        return new String[]{startColumn, endColumn};
    }

    // Helper method to extract start row from the range (assuming format is like "A3..V9")
    private int extractStartRow(String range) {
        String[] rangeParts = range.split("\\.\\.");
        String topLeftCell = rangeParts[0].trim();
        return Integer.parseInt(topLeftCell.replaceAll("\\D", "")); // Extract row number
    }

    // Helper method to extract end row from the range
    private int extractEndRow(String range) {
        String[] rangeParts = range.split("\\.\\.");
        String bottomRightCell = rangeParts[1].trim();
        return Integer.parseInt(bottomRightCell.replaceAll("\\D", "")); // Extract row number
    }

    // Extract the rows from the given range
    private int[] extractRows(String tableArea) {
        String[] areaParts = tableArea.split("\\.\\.");
        String topLeftCell = areaParts[0].trim();
        String bottomRightCell = areaParts[1].trim();

        // Extract row numbers
        int startRow = Integer.parseInt(topLeftCell.replaceAll("\\D", ""));
        int endRow = Integer.parseInt(bottomRightCell.replaceAll("\\D", ""));

        return new int[]{startRow, endRow};
    }

    // Get the rows within the specified range
    private List<SpreadsheetRow> getRowsInRange(int startRow, int endRow) {
        List<SpreadsheetRow> rowsInRange = new ArrayList<>();

        // Assuming you have a method to get all rows in the spreadsheet
        for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
            SpreadsheetRow row = getRow(rowIndex); // Method to get a row from the spreadsheet at the specified index
            if (row != null) {
                rowsInRange.add(row); // Add the row to the list
            }
        }

        return rowsInRange;
    }

    // Update the spreadsheet with the sorted rows
    private  Map<String, String> updateSpreadsheetWithSortedRows(List<SpreadsheetRow> sortedRows, int startRow, int endRow) {
        // Remove old rows from the spreadsheet
        removeOldRows(startRow, endRow);
        Map<String, String> cellIdMapping = new HashMap<>();

        for (int i = 0; i < sortedRows.size(); i++) { // Add the sorted rows to the spreadsheet
            SpreadsheetRow row = sortedRows.get(i);
            int newRowIndex = startRow + i;  // Update the row index as rows are moved
            // Update the row and get the ID mapping for this row
            Map<String, String> rowIdMapping = setRow(row, newRowIndex);

            // Merge the row-level ID mappings into the overall mapping
            cellIdMapping.putAll(rowIdMapping);
        }
        return cellIdMapping;
    }

    // Remove old rows to be changed after sort from the spreadsheet
    private void removeOldRows(int startRow, int endRow) {
        // Remove all cells that belong to the rows in the specified range
        List<String> cellsToRemove = new ArrayList<>();

        for (String cellId : cells.keySet()) { // Iterate through all cells in the spreadsheet
            int rowIndex = extractRowNumberFromCellId(cellId);
            if (rowIndex >= startRow && rowIndex <= endRow) {
                cellsToRemove.add(cellId);  // Mark cells for removal
            }
        }

        // Remove the old cells
        for (String cellId : cellsToRemove) {
            cells.remove(cellId);
        }
    }

    // Update the row number and cell IDs of the cells in the provided row
    public Map<String,String> setRow(SpreadsheetRow row, int newRowNumber) {
        Map<String, Cell> updatedCells = new HashMap<>();
        Map<String,String> idMapping = new HashMap<>();

        // Iterate through the cells in the provided row
        for (Map.Entry<String, Cell> entry : row.getCells().entrySet()) {
            String oldCellId = entry.getKey();
            Cell cell = entry.getValue();

            // Extract the column from the oldCellId (assuming it's something like "C3" where "C" is the column and "3" is the row)
            String column = extractColumnFromCellId(oldCellId);

            // Generate the new cell ID based on the new row number
            String newCellId = column + newRowNumber;

            idMapping.put(oldCellId,newCellId);

            // Put the cell in the updated map with the new ID
            updatedCells.put(newCellId, cell);
        }

        // Update the row number of the row and replace cells with the updated ones
        row.setRowNumber(newRowNumber);  // Assuming you have a setter for the row number
        row.setCells(updatedCells);      // Assuming you have a setter for cells

        // Update the spreadsheet's internal cell map with the new cell IDs
        for (Map.Entry<String, Cell> updatedEntry : updatedCells.entrySet()) {
            this.cells.put(updatedEntry.getKey(), updatedEntry.getValue());
        }
        return idMapping;
    }

    // Get a row from the spreadsheet by its index
    public SpreadsheetRow getRow(int rowIndex) {
        SpreadsheetRow row = new SpreadsheetRow(rowIndex); // Create a new SpreadsheetRow object for the specified row index

        // Iterate through all cells in the spreadsheet
        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
            String cellId = entry.getKey();
            Cell cell = entry.getValue();

            // Extract row number from cell ID
            int cellRowIndex = extractRowNumberFromCellId(cellId);
            if (cellRowIndex == rowIndex) {
                row.addCell(cellId, cell); // Add cell to the row
            }
        }

        return row;
    }

    // Extract the column part from the cell ID
    private String extractColumnFromCellId(String cellId) {
        // Extract all leading alphabetic characters (the column part)
        StringBuilder column = new StringBuilder();

        for (char c : cellId.toCharArray()) {
            if (Character.isLetter(c)) {
                column.append(c);
            } else {
                break; // Stop once you encounter a digit (which indicates the row part)
            }
        }

        return column.toString(); // Return the column portion (e.g., "C" from "C3")
    }

    // Extract the row number from the cell ID
    private int extractRowNumberFromCellId(String cellId) {
        // Extract digits from the cellId
        String rowNumberStr = cellId.replaceAll("[^0-9]", "");
        return Integer.parseInt(rowNumberStr);
    }

    public String getName() {   return name; }

    public int getRows() { return rows; }

    public int getColumns() { return columns; }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spreadsheet that = (Spreadsheet) o;
        return rows == that.rows && columns == that.columns && columnWidth == that.columnWidth && versionNumber == that.versionNumber && Objects.equals(cells, that.cells) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cells, rows, columns, columnWidth, name, versionNumber);
    }

}
