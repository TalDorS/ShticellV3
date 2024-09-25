package api;
import exceptions.engineexceptions.*;
import engineimpl.EngineImpl;
import dto.*;
import versions.Version;
import spreadsheet.Spreadsheet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//This engine interface is the logical engine for the program
//It contains the main logic methods to run the SheetSpread
public interface Engine {
    void loadSpreadsheet(String filePath) throws InvalidExpressionException, SpreadsheetLoadingException, CellUpdateException, CircularReferenceException, RangeProcessException;
    void updateCellValue(String cellId, String newValue) throws InvalidExpressionException, CircularReferenceException, CellUpdateException;
    Spreadsheet getCurrentSpreadsheet();
    Spreadsheet getSpreadsheetByVersion(int versionNumber);
    Map<Integer, Version> getVersions();
    EngineDTO getEngineData();
    void clearVersions();
    int getCurrentVersion();
    void addRange(String rangeName, String firstCell, String lastCell) throws Exception;
    void removeRange(String rangeName) throws Exception;
    Map<String, Range> getAllRanges();
    boolean isRangeInUse(String rangeName);
    Range getRange(String rangeName);
    Map<String, String> sortSpreadsheet(Spreadsheet spreadsheet, String range, List<String> columnsToSortBy) throws InvalidColumnException;
    int getColumnIndex(String columnName);
    String getColumnName(int index);
    List<String[][]> filterTableMultipleColumns(String tableArea, Map<String, List<String>> selectedColumnValues);
    List<String> getRangeValues(String rangeName);
    Expression parseExpression (String input) throws InvalidExpressionException;
    void checkForCircularReferences(String cellId, Expression newExpression) throws CircularReferenceException;
}
