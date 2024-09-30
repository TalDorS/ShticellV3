package gridwindow.api;

import gridwindow.GridWindowController;
import dto.SpreadsheetDTO;

public interface SpreadsheetController {
    void initializeSpreadsheet(SpreadsheetDTO spreadsheet);
    void start(SpreadsheetDTO spreadsheet, boolean isPopup);
    void createColumnHeaders(int columnCount, int columnWidth);
    void createRowHeaders(int rowCount, int rowHeight);
    void createCells( SpreadsheetDTO spreadsheet, int rowCount, int columnCount, int columnWidth, int rowHeight);
    String getColumnName(int index);
    void setMainController(GridWindowController gridWindowController);
    }
