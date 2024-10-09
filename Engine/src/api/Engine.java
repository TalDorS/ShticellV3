package api;
import cells.Cell;
import enums.PermissionStatus;
import enums.PermissionType;
import exceptions.engineexceptions.*;
import dto.*;
import spreadsheet.Spreadsheet;

import java.util.List;
import java.util.Map;

import versions.VersionsManager;

//This engine interface is the logical engine for the program
//It contains the main logic methods to run the SheetSpread
public interface Engine {
    String loadSpreadsheet(String username, String filePath) throws Exception;
    void updateCellValue(String username, String spreadsheetName, String cellId, String newValue, Boolean isDynamicAnalysis) throws InvalidExpressionException, CircularReferenceException, CellUpdateException, SpreadsheetLoadingException, SpreadsheetNotFoundException, UserNotFoundException;
    Spreadsheet getCurrentSpreadsheet(String username, String spreadsheetName);
    Spreadsheet getSpreadsheetByVersion(String username, String spreadsheetName, int versionNumber) throws IndexOutOfBoundsException, SpreadsheetNotFoundException,UserNotFoundException;
    EngineDTO getEngineData(String username, String spreadsheetName);
    int getCurrentVersion(String username, String spreadsheetName);
    void addRange(String username, String spreadsheetName,String rangeName, String firstCell, String lastCell) throws Exception;
    void removeRange(String username, String spreadsheetName, String rangeName) throws Exception;
    //Map<String, Range> getAllRanges(String username, String spreadsheetName) throws SpreadsheetNotFoundException, UserNotFoundException;
    Map<String, String> sortSpreadsheet(String username, String spreadsheetName,Spreadsheet spreadsheet, String range, List<String> columnsToSortBy) throws InvalidColumnException, SpreadsheetNotFoundException, UserNotFoundException;
    int getColumnIndex(String username, String spreadsheetName, String columnName)  throws SpreadsheetNotFoundException, UserNotFoundException;
    String getColumnName(String username, String spreadsheetName, int index)throws SpreadsheetNotFoundException, UserNotFoundException;
    List<String[][]> filterTableMultipleColumns(String username, String spreadsheetName, String tableArea, Map<String, List<String>> selectedColumnValues) throws SpreadsheetNotFoundException, UserNotFoundException;
    Expression parseExpression (String username, String spreadsheetName, String input) throws InvalidExpressionException, SpreadsheetNotFoundException, UserNotFoundException;
    void checkForCircularReferences(String username, String spreadsheetName, String cellId, Expression newExpression) throws CircularReferenceException, SpreadsheetNotFoundException, UserNotFoundException;
    SpreadsheetDTO convertSpreadsheetToDTO(Spreadsheet spreadsheet);
    void addUser(String username) throws Exception;
    void removeUser(String username) throws Exception;
    boolean isUserExist(String username);
    Map<String, VersionsManager> getSpreadsheetsMap();
    PermissionType getUserPermission(String username, String spreadsheetName) throws SpreadsheetNotFoundException;
    PermissionsManagerDTO getPermissionsData(String spreadsheetName);
    void askForPermission(String username, String spreadsheetName, PermissionType permissionType);
    void handlePermissionRequest(String applicantName, String handlerName, String spreadsheetName, PermissionStatus permissionStatus, PermissionType permissionType);
    Cell getCell(String spreadsheetName, String cellId);
}

