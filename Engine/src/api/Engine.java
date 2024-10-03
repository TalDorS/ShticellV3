package api;
import exceptions.engineexceptions.*;
import engineimpl.EngineImpl;
import dto.*;
import javafx.util.Pair;
import versions.Version;
import spreadsheet.Spreadsheet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import user.User;
//This engine interface is the logical engine for the program
//It contains the main logic methods to run the SheetSpread
public interface Engine {
    Pair<String, Boolean> loadSpreadsheet(String userName, String filePath) throws Exception;
   void updateCellValue(String userName, String filePath, String cellId, String newValue) throws InvalidExpressionException, CircularReferenceException, CellUpdateException, SpreadsheetLoadingException, FileNotFoundException, UserNotFoundException;
   Spreadsheet getCurrentSpreadsheet(String userName, String fileName);
    Spreadsheet getSpreadsheetByVersion(String userName, String fileName, int versionNumber) throws IndexOutOfBoundsException, FileNotFoundException,UserNotFoundException;
//    Map<Integer, Version> getVersions();
    EngineDTO getEngineData(String userName, String fileName);
//   //void clearVersions();
    int getCurrentVersion(String userName, String fileName);
    void addRange(String userName, String fileName,String rangeName, String firstCell, String lastCell) throws Exception;
    void removeRange(String userName, String fileName, String rangeName) throws Exception;
    Map<String, Range> getAllRanges(String userName, String fileName) throws FileNotFoundException, UserNotFoundException;
//    boolean isRangeInUse(String rangeName);
//    Range getRange(String rangeName);
    Map<String, String> sortSpreadsheet(String userName, String fileName,Spreadsheet spreadsheet, String range, List<String> columnsToSortBy) throws InvalidColumnException, FileNotFoundException, UserNotFoundException;
    int getColumnIndex(String userName, String fileName, String columnName)  throws FileNotFoundException, UserNotFoundException;
    String getColumnName(String userName, String fileName, int index)throws FileNotFoundException, UserNotFoundException;
   List<String[][]> filterTableMultipleColumns(String userName, String fileName, String tableArea, Map<String, List<String>> selectedColumnValues) throws FileNotFoundException, UserNotFoundException;
//    List<String> getRangeValues(String rangeName);
    Expression parseExpression (String userName, String fileName, String input) throws InvalidExpressionException, FileNotFoundException, UserNotFoundException;
    void checkForCircularReferences(String userName, String fileName, String cellId, Expression newExpression) throws CircularReferenceException, FileNotFoundException, UserNotFoundException;
   public SpreadsheetDTO convertSpreadsheetToDTO(Spreadsheet spreadsheet);
    }
