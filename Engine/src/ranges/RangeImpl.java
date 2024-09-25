package ranges;

import api.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RangeImpl implements Range {
    private final String name;
    private final String startCell;
    private final String endCell;
    private final List<String> cells;

    public RangeImpl(String name, String startCell, String endCell) {
        this.name = name;
        this.startCell = startCell;
        this.endCell = endCell;
        this.cells = calculateCells(startCell, endCell);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getCells() {
        return cells;
    }

    @Override
    public boolean containsCell(String cellId) {
        return cells.contains(cellId);
    }

    @Override
    public String getStartCell() {
        return startCell;
    }

    @Override
    public String getEndCell() {
        return endCell;
    }

    // Helper method to calculate all cell IDs between the start and end cells
    private List<String> calculateCells(String startCell, String endCell) {
        List<String> cellList = new ArrayList<>();
        // Logic to compute all cells within the range goes here
        // Assuming cell IDs are like "A1", "A2", ..., "B1", "B2", ...
        // Example: "A1..A4", "A3..D3", "A3..D4"

        // Compute row and column ranges
        char startColumn = startCell.charAt(0);
        int startRow = Integer.parseInt(startCell.substring(1));
        char endColumn = endCell.charAt(0);
        int endRow = Integer.parseInt(endCell.substring(1));

        for (char col = startColumn; col <= endColumn; col++) {
            for (int row = startRow; row <= endRow; row++) {
                cellList.add(col + String.valueOf(row));
            }
        }

        return cellList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeImpl range = (RangeImpl) o;
        return Objects.equals(name, range.name) && Objects.equals(startCell, range.startCell) && Objects.equals(endCell, range.endCell) && Objects.equals(cells, range.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, startCell, endCell, cells);
    }
}
