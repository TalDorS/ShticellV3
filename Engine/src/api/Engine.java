package api;
import exceptions.engineexceptions.*;
import engineimpl.EngineImpl;
import dto.*;
import versions.Version;
import spreadsheet.Spreadsheet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import user.User;
//This engine interface is the logical engine for the program
//It contains the main logic methods to run the SheetSpread
public interface Engine {
   void loadSpreadsheet(String filePath,String userName) throws InvalidExpressionException, SpreadsheetLoadingException, CellUpdateException, CircularReferenceException, RangeProcessException;
   void updateCellValue(String userName, String filePath, String cellId, String newValue) throws InvalidExpressionException, CircularReferenceException, CellUpdateException, SpreadsheetLoadingException;
   Spreadsheet getCurrentSpreadsheet(String userName, String filePath);
    Spreadsheet getSpreadsheetByVersion(String userName, String filePath, int versionNumber) throws IndexOutOfBoundsException, FileNotFoundException,UserNotFoundException;
//    Map<Integer, Version> getVersions();
    EngineDTO getEngineData(String userName, String filePath);
//   //void clearVersions();
    int getCurrentVersion(String userName, String filePath);
    void addRange(String userName, String filePath,String rangeName, String firstCell, String lastCell) throws Exception;
    void removeRange(String userName, String filePath, String rangeName) throws Exception;
    Map<String, Range> getAllRanges(String userName, String filePath) throws FileNotFoundException, UserNotFoundException;
//    boolean isRangeInUse(String rangeName);
//    Range getRange(String rangeName);
    Map<String, String> sortSpreadsheet(String userName, String filePath,Spreadsheet spreadsheet, String range, List<String> columnsToSortBy) throws InvalidColumnException, FileNotFoundException, UserNotFoundException;
    int getColumnIndex(String userName, String filePath, String columnName)  throws FileNotFoundException, UserNotFoundException;
    String getColumnName(String userName, String filePath, int index)throws FileNotFoundException, UserNotFoundException;
   List<String[][]> filterTableMultipleColumns(String userName, String filePath, String tableArea, Map<String, List<String>> selectedColumnValues) throws FileNotFoundException, UserNotFoundException;
//    List<String> getRangeValues(String rangeName);
    Expression parseExpression (String userName, String filePath, String input) throws InvalidExpressionException, FileNotFoundException, UserNotFoundException;
    void checkForCircularReferences(String userName, String filePath, String cellId, Expression newExpression) throws CircularReferenceException, FileNotFoundException, UserNotFoundException;
   public SpreadsheetDTO convertSpreadsheetToDTO(Spreadsheet spreadsheet);
    }
