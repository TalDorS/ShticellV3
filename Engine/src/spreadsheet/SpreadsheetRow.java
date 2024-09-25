package spreadsheet;

import cells.Cell;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Represents a row in a spreadsheet
// Mainly created for the sorting functionality
public class SpreadsheetRow {
    private int rowNumber;
    private final Map<String, Cell> cells;

    public SpreadsheetRow(int rowNumber) {
        this.rowNumber = rowNumber;
        this.cells = new HashMap<>();
    }

    public void addCell(String column, Cell cell) {
        cells.put(column, cell);
    }

    // Method to get a cell using a combined column-row key (key= cellId)
    public Cell getCell(String column, int row) {
        String key = column + row;  // Example: "C3"
        return cells.get(key);
    }

    public boolean hasCell(String column) {
        return cells.containsKey(column);
    }

    public Map<String, Cell> getCells() {
        return cells;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    // Compare values in the specified column of the row
    public int compareCellValue(SpreadsheetRow otherRow, String column) {
        Cell thisCell = this.getCell(column, this.rowNumber);
        Cell otherCell = otherRow.getCell(column, otherRow.getRowNumber());

        if (thisCell == null || otherCell == null) {
            throw new IllegalArgumentException("Column " + column + " does not exist in one of the rows.");
        }

        // Extract effective values from cells
        Object value1 = thisCell.getEffectiveValue();
        Object value2 = otherCell.getEffectiveValue();

        // Handle null values
        if (value1 == "" && value2 == "") return 0;
        if (value1 == "") return -1;
        if (value2 == "") return 1;

        // Handle numerical values
        if (value1 instanceof Number && value2 instanceof Number) {
            return Double.compare(((Number) value1).doubleValue(), ((Number) value2).doubleValue());
        }

        // Handle string values
        if (value1 instanceof String && value2 instanceof String) {
            return ((String) value1).compareTo((String) value2);
        }

        // Handle cases where values are of different types
        throw new IllegalArgumentException("Cannot compare values of different types.");
    }

    public void setCells(Map<String, Cell> cells) {
        this.cells.clear();
        this.cells.putAll(cells);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpreadsheetRow that = (SpreadsheetRow) o;
        return Objects.equals(cells, that.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cells);
    }
}
