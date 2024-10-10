package filter;

import api.Engine;
import cells.Cell;
import spreadsheet.Spreadsheet;
import versions.VersionsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpreadsheetFilter {
    private final VersionsManager versionsManager;

    public SpreadsheetFilter(VersionsManager versionsManager) {
        this.versionsManager = versionsManager;
    }

    public List<String[][]> filterTableMultipleColumns(String tableArea, Map<String, List<String>> selectedColumnValues) {
        Spreadsheet currentSpreadsheet = versionsManager.getCurrentSpreadsheet();

        if (currentSpreadsheet == null) {
            return new ArrayList<>(); // Return empty list if no spreadsheet is loaded
        }

        // Parse the table area into start and end cells
        String[] areaParts = parseTableArea(tableArea);
        String topLeftCell = areaParts[0];
        String bottomRightCell = areaParts[1];

        // Get the row and column ranges for the specified area
        int[] rowRange = getRowRange(topLeftCell, bottomRightCell);
        int[] columnRange = getColumnRange(topLeftCell, bottomRightCell);

        // Filter rows based on selected column values, including cell IDs
        return getFilteredRowsWithCellIds(currentSpreadsheet, rowRange, columnRange, selectedColumnValues);
    }

    // Parses the table area string (e.g., "A1..D10") and returns the top-left and bottom-right cells
    private String[] parseTableArea(String tableArea) {
        String[] areaParts = tableArea.split("\\.\\.");

        if (areaParts.length != 2) {
            throw new IllegalArgumentException("Invalid table area format. Use <top-left-cell>..<bottom-right-cell> format.");
        }

        return areaParts;
    }

    // Extracts the row range from the start and end cell references
    private int[] getRowRange(String topLeftCell, String bottomRightCell) {
        int startRow = Integer.parseInt(topLeftCell.replaceAll("\\D", ""));
        int endRow = Integer.parseInt(bottomRightCell.replaceAll("\\D", ""));

        return new int[]{startRow, endRow};
    }

    // Extracts the column range from the start and end cell references
    private int[] getColumnRange(String topLeftCell, String bottomRightCell) {
        String startColumn = topLeftCell.replaceAll("\\d", "");
        String endColumn = bottomRightCell.replaceAll("\\d", "");
        int startColumnIndex = versionsManager.getColumnIndex(startColumn);
        int endColumnIndex = versionsManager.getColumnIndex(endColumn);

        return new int[]{startColumnIndex, endColumnIndex};
    }

    // Filters the rows based on the selected columns and their values
    private List<String[][]> getFilteredRowsWithCellIds(Spreadsheet spreadsheet, int[] rowRange, int[] columnRange, Map<String, List<String>> selectedColumnValues) {
        List<String[][]> filteredRows = new ArrayList<>();

        // Iterate through each row within the specified range
        for (int row = rowRange[0]; row <= rowRange[1]; row++) {
            List<String[]> filteredRow = new ArrayList<>();
            boolean includeRow = true;

            // Iterate through each column within the specified range
            for (int col = columnRange[0]; col <= columnRange[1]; col++) {
                String cellId = getColumnName(col) + row; // Construct cell ID like "A1", "B2", etc.
                Cell cell = spreadsheet.getCellById(cellId);
                String cellValue = (cell != null && cell.getEffectiveValue() != null) ? cell.getEffectiveValue().toString() : "";

                // Check if the cell value matches the filter criteria for its column
                String columnName = getColumnName(col);
                if (selectedColumnValues.containsKey(columnName) && !selectedColumnValues.get(columnName).contains(cellValue)) {
                    includeRow = false; // If any cell in the row does not match the filter, skip this row
                    break;
                }

                // Add cellId and cellValue as a two-element array
                filteredRow.add(new String[] { cellId, cellValue });
            }

            // If the row meets all filter criteria, add it to the result
            if (includeRow) {
                filteredRows.add(filteredRow.toArray(new String[0][0])); // Convert list to array
            }
        }

        return filteredRows;
    }

    // Determines if a row should be included in the filtered results
    private boolean shouldIncludeRow(Spreadsheet spreadsheet, int row, int[] columnRange, String[] rowData, Map<String, List<String>> selectedColumnValues) {
        for (int colIndex = columnRange[0]; colIndex <= columnRange[1]; colIndex++) {
            String cellId = versionsManager.getColumnName(colIndex) + row;
            String cellValue = getCellValue(spreadsheet, cellId);
            rowData[colIndex - columnRange[0]] = cellValue; // Store cell value in row data array

            // Check if cell value matches the filter criteria
            if (!isCellValueMatching(columnRange, colIndex, cellValue, selectedColumnValues)) {
                return false; // Exclude row if any cell does not match the filter criteria
            }
        }

        return true; // Include row if all cells match filter criteria
    }

    // Retrieves the value of a cell given its ID
    private String getCellValue(Spreadsheet spreadsheet, String cellId) {
        Cell cell = spreadsheet.getCellById(cellId);

        return (cell != null && cell.getEffectiveValue() != null) ? cell.getEffectiveValue().toString() : "";
    }

    // Checks if a cell value matches the filter criteria
    private boolean isCellValueMatching(int[] columnRange, int colIndex, String cellValue, Map<String, List<String>> selectedColumnValues) {
        String columnName = versionsManager.getColumnName(colIndex);

        if (selectedColumnValues.containsKey(columnName)) {
            List<String> valuesToMatch = selectedColumnValues.get(columnName);
            return valuesToMatch.contains(cellValue); // Return true if cell value matches the selected values
        }

        return true; // Include if column is not in filter criteria
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpreadsheetFilter that = (SpreadsheetFilter) o;
        return Objects.equals(versionsManager, that.versionsManager);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(versionsManager);
    }
}
