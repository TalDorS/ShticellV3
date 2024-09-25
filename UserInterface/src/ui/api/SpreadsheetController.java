package ui.api;

import api.Engine;
import app.AppController;
import spreadsheet.Spreadsheet;

public interface SpreadsheetController {
    void initializeSpreadsheet(Spreadsheet spreadsheet);
    void setEngine(Spreadsheet spreadsheet, boolean isPopup);
    void createColumnHeaders(int columnCount, int columnWidth);
    void createRowHeaders(int rowCount, int rowHeight);
    void createCells( Spreadsheet spreadsheet, int rowCount, int columnCount, int columnWidth, int rowHeight);
    String getColumnName(int index);
    void setMainController(AppController appController);
    }
