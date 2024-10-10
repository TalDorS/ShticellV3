package dto;

import java.util.Map;
import java.util.stream.Collectors;

// This class is for creating the spreadsheet data transfer object
public class SpreadsheetDTO {
    private final String name;
    private final int rows;
    private final int columns;
    private final int columnWidth;
    private final int rowHeight;
    private final int versionNumber;
    private final Map<String, CellDTO> cells;

    public SpreadsheetDTO(String name, int rows, int columns, int columnWidth, int rowHeight, int versionNumber, Map<String, CellDTO> cells) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.columnWidth = columnWidth;
        this.rowHeight = rowHeight;
        this.versionNumber = versionNumber;
        this.cells = cells;
    }
    public String getName() {
        return name;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public Map<String, CellDTO> getCells() {
        return cells;
    }

    public CellDTO getCellById(String cellId) {
        return cells.get(cellId);
    }

    public int getVersionNumber() { return versionNumber;}
}
