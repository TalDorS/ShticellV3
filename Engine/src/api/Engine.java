package api;
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
    String loadSpreadsheet(String userName, String filePath) throws Exception;
    void updateCellValue(String userName, String filePath, String cellId, String newValue) throws InvalidExpressionException, CircularReferenceException, CellUpdateException, SpreadsheetLoadingException, SpreadsheetNotFoundException, UserNotFoundException;
    Spreadsheet getCurrentSpreadsheet(String userName, String fileName);
    Spreadsheet getSpreadsheetByVersion(String userName, String fileName, int versionNumber) throws IndexOutOfBoundsException, SpreadsheetNotFoundException,UserNotFoundException;
    EngineDTO getEngineData(String userName, String fileName);
    int getCurrentVersion(String userName, String fileName);
    void addRange(String userName, String fileName,String rangeName, String firstCell, String lastCell) throws Exception;
    void removeRange(String userName, String fileName, String rangeName) throws Exception;
    //Map<String, Range> getAllRanges(String userName, String fileName) throws SpreadsheetNotFoundException, UserNotFoundException;
    Map<String, String> sortSpreadsheet(String userName, String fileName,Spreadsheet spreadsheet, String range, List<String> columnsToSortBy) throws InvalidColumnException, SpreadsheetNotFoundException, UserNotFoundException;
    int getColumnIndex(String userName, String fileName, String columnName)  throws SpreadsheetNotFoundException, UserNotFoundException;
    String getColumnName(String userName, String fileName, int index)throws SpreadsheetNotFoundException, UserNotFoundException;
    List<String[][]> filterTableMultipleColumns(String userName, String fileName, String tableArea, Map<String, List<String>> selectedColumnValues) throws SpreadsheetNotFoundException, UserNotFoundException;
    Expression parseExpression (String userName, String fileName, String input) throws InvalidExpressionException, SpreadsheetNotFoundException, UserNotFoundException;
    void checkForCircularReferences(String userName, String fileName, String cellId, Expression newExpression) throws CircularReferenceException, SpreadsheetNotFoundException, UserNotFoundException;
    SpreadsheetDTO convertSpreadsheetToDTO(Spreadsheet spreadsheet);
    void addUser(String userName) throws Exception;
    void removeUser(String userName) throws Exception;
    boolean isUserExist(String userName);
    Map<String, VersionsManager> getSpreadsheetsMap();
    PermissionType getUserPermission(String username, String spreadsheetName) throws SpreadsheetNotFoundException;
    PermissionsManagerDTO getPermissionsData(String sheetName);
    void askForPermission(String username, String spreadsheetName, PermissionType permissionType);
    void handlePermissionRequest(String applicantName, String handlerName, String spreadsheetName, PermissionStatus permissionStatus, PermissionType permissionType);
}

