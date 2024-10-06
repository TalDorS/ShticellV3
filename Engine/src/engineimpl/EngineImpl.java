package engineimpl;
import api.Engine;
import api.Expression;
import api.Range;
import dto.*;
import dto.VersionDTO;
import enums.PermissionStatus;
import enums.PermissionType;
import exceptions.engineexceptions.*;
import cells.Cell;
import spreadsheet.Spreadsheet;
import user.User;
import user.UserManager;
import versions.Version;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Map;

import versions.VersionsManager;
import versions.permissions.PermissionsManager;

//This class implements the api.Engine interface and manages the spreadsheet
// First version of the spreadsheet will be index 1
public class EngineImpl implements Engine {
    private final Map<String, VersionsManager> spreadsheetsMap; // Map of spreadsheet name to it's version manager
    private final UserManager userManager;

    public EngineImpl() {
        this.spreadsheetsMap = new HashMap<>();
        this.userManager = new UserManager();
    }

    @Override
    public synchronized void addUser(String userName) throws Exception {
        userManager.addUser(userName);
    }

    @Override
    public synchronized boolean isUserExist(String userName) {
        return userManager.isUserExists(userName);
    }

    @Override
    public String loadSpreadsheet(String userName, String filePath) throws Exception {
        // Load spreadsheet to get the spreadsheet name
        VersionsManager versionsManager = new VersionsManager(userName);
        versionsManager.loadSpreadsheet(filePath);
        String spreadsheetName = versionsManager.getCurrentSpreadsheet().getName(); // Get the file name after loading

        // Check if the spreadsheet name already exists for any user in the system
        if (spreadsheetsMap.containsKey(spreadsheetName)) {
            // File already exists for another user, throw exception
            throw new SpreadsheetLoadingException("The file name '" + spreadsheetName + "' already exists for another user.");
        }

        // If the file does not exist for any user, add it for the current user
        spreadsheetsMap.put(spreadsheetName, versionsManager);

        return spreadsheetName; // Return that the file was newly loaded
    }


//    @Override
//    public void clearVersions() {
//        this.versionsManager.clearVersions();
//    }

    @Override
    // Method to get the engine data using dto
    public EngineDTO getEngineData(String userName, String fileName) {
        if (!userManager.isUserExists(userName)) {
            // If user doesn't exist, remove them from clientFilesVersions
            removeUser(userName);  // Method to clean up invalid users

            return new EngineDTO(Collections.emptyMap(),  0); // Return empty DTO or handle appropriately
        }

        User user = userManager.getUser(userName);

        Map<Integer, VersionDTO> versionDTOMap = new HashMap<>();

        if (spreadsheetsMap != null) {
            VersionsManager versionsManager = spreadsheetsMap.get(fileName);

            if (versionsManager != null) {
                // Get the current version of the spreadsheet
                Spreadsheet spreadsheet = versionsManager.getCurrentSpreadsheet(); // Assume this method exists

                // Convert the spreadsheet to DTO
                SpreadsheetDTO spreadsheetDTO = convertSpreadsheetToDTO(spreadsheet);
                // Get all versions
                Map<Integer, Version> versions = versionsManager.getVersions(); // Ensure getVersions() exists in VersionsManager
                // Retrieve ranges from RangesManager
                Map<String, Range> ranges = versionsManager.getAllRanges();
                // Convert each version to a VersionDTO and add it to the map
                for (Map.Entry<Integer, Version> entry : versions.entrySet()) {
                    Version version = entry.getValue();

                    // Collecting ranges as DTOs
                    List<RangeDTO> rangeDTOList = ranges.values().stream()
                            .map(this::convertRangeToDTO)
                            .collect(Collectors.toList());

                    versionDTOMap.put(entry.getKey(), new VersionDTO(
                            version.getVersionNumber(),
                            version.getChangedCellsCount(),
                            convertSpreadsheetToDTO(version.getSpreadsheet()), // Ensure version.getSpreadsheet() returns the correct spreadsheet
                            rangeDTOList // Add ranges to the VersionDTO
                    ));
                }

                // Return EngineDTO with versions, current version DTO, and last validation error
                return new EngineDTO(versionDTOMap, versionsManager.getCurrentVersion()); // Include spreadsheetDTO
            }
        }

        return new EngineDTO(Collections.emptyMap(),0); //fixme- check this
    }

    @Override
    public PermissionsManagerDTO getPermissionsData(String sheetName) {
        if (spreadsheetsMap.containsKey(sheetName)) {
            VersionsManager versionsManager = spreadsheetsMap.get(sheetName);
            PermissionsManager permissionsManager = versionsManager.getPermissionsManager();

            return new PermissionsManagerDTO(permissionsManager.getOwner(), permissionsManager.getWriters(), permissionsManager.getReaders());
        }

        return null;
    }

    public void removeUser(String userName) {
        // Remove the user from the user manager
        userManager.removeUser(userName);

        // Iterate through the spreadsheets map and remove spreadsheets owned by the user
        synchronized (spreadsheetsMap) {
            // Remove all entries where the owner is the given userName
            spreadsheetsMap.entrySet().removeIf(entry -> {
                VersionsManager versionsManager = entry.getValue();
                PermissionType usersPermission = versionsManager.getUserPermission(userName);

                return usersPermission.toString().equals("OWNER");
            });
        }
    }

    @Override
    // Method to convert spreadsheet to DTO
    public SpreadsheetDTO convertSpreadsheetToDTO(Spreadsheet spreadsheet) {
        if (spreadsheet == null) {
            return null;
        }

        return new SpreadsheetDTO(
                spreadsheet.getName(),
                spreadsheet.getRows(),
                spreadsheet.getColumns(),
                spreadsheet.getColumnWidth(),
                spreadsheet.getRowHeight(),
                spreadsheet.getVersionNumber(),
                convertCellsToDTO(spreadsheet.getCells())
        );
    }

    // Method to convert cells to DTO
    private Map<String, CellDTO> convertCellsToDTO(Map<String, Cell> cells) {
        // Create basic CellDTOs without dependencies
        Map<String, CellDTO> cellDTOMap = new HashMap<>();

        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
            Cell cell = entry.getValue();
            CellDTO cellDTO = new CellDTO(
                    cell.getOriginalValue(),
                    cell.getEffectiveValue(),
                    cell.getLastUpdatedVersion(),
                    new ArrayList<>(), // Placeholder for dependsOnThemIds
                    new ArrayList<>()  // Placeholder for dependsOnMeIds
            );
            cellDTOMap.put(entry.getKey(), cellDTO);
        }

        // Populate dependencies by adding cell IDs
        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
            String cellId = entry.getKey();
            Cell cell = entry.getValue();
            CellDTO cellDTO = cellDTOMap.get(cellId);

            // Populate dependsOnThemIds
            for (String dependsOnId : cell.getDependsOnThem().keySet()) {
                cellDTO.getDependsOnThemIds().add(dependsOnId);
            }

            // Populate dependsOnMeIds
            for (String dependsOnMeId : cell.getDependsOnMe().keySet()) {
                cellDTO.getDependsOnMeIds().add(dependsOnMeId);
            }
        }

        return cellDTOMap;
    }

    private RangeDTO convertRangeToDTO(Range range) {
        if (range == null) {
            return null; // Handle null case if necessary
        }

        return new RangeDTO(
                range.getName(), // Assuming Range has a getName() method
                range.getStartCell(),
                range.getEndCell(),
                range.getCells() // Assuming Range has a getCells() method returning List<String>
        );
    }

    @Override
    public int getCurrentVersion(String userName, String spreadsheetName) {
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager == null) {
            throw new IllegalArgumentException("File path not found for user: " + spreadsheetName);
        }

        return versionsManager.getCurrentVersion();
    }

    @Override
    public Spreadsheet getCurrentSpreadsheet(String userName, String spreadsheetName) {
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager == null) {
            throw new IllegalArgumentException("File path not found for user: " + spreadsheetName);
        }

        return versionsManager.getCurrentSpreadsheet();
    }

    @Override
    public void updateCellValue(String userName, String spreadsheetName, String cellId, String newValue)
            throws CircularReferenceException, CellUpdateException, SpreadsheetNotFoundException, UserNotFoundException {
        // Retrieve the VersionsManager for the specified filePath
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        // Check if the VersionsManager exists
        if (versionsManager != null) {
            // Update the cell value in the VersionsManager
            versionsManager.updateCellValue(cellId, newValue);
        } else {
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");
        }
    }

    @Override
    public void addRange(String userName, String spreadsheetName, String rangeName, String firstCell, String lastCell) throws Exception {
        // Retrieve the VersionsManager for the specified filePath
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        // Check if the VersionsManager exists
        if (versionsManager != null) {
            // Call the existing addRange method from VersionsManager
            versionsManager.addRange(rangeName, firstCell, lastCell);
        } else {
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");
        }
    }

    @Override
    public void removeRange(String userName, String spreadsheetName, String rangeName) throws Exception {
        // Retrieve the VersionsManager for the specified filePath
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        // Check if the VersionsManager exists
        if (versionsManager != null) {

            // Call the existing removeRange method from VersionsManager
            versionsManager.removeRange(rangeName.toUpperCase());
        } else {
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");
        }
    }

    @Override
    public Map<String, String> sortSpreadsheet(String userName, String spreadsheetName, Spreadsheet spreadsheet, String range, List<String> columnsToSortBy)
            throws InvalidColumnException, SpreadsheetNotFoundException, UserNotFoundException {
        // Retrieve the VersionsManager for the specified filePath
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        // Check if the VersionsManager exists
        if (versionsManager != null) {
            // Perform sorting on the provided spreadsheet
            return versionsManager.sortSpreadsheet(spreadsheet, range, columnsToSortBy);
        } else {
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");
        }
    }


//    @Override
//    public Map<String, Range> getAllRanges(String userName, String spreadsheetName) throws FileNotFoundException, UserNotFoundException {
//        // Retrieve the VersionsManager for the specified filePath
//        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);
//
//        // Check if the VersionsManager exists
//        if (versionsManager != null) {
//            // Call the getAllRanges method from the VersionsManager
//            return versionsManager.getAllRanges();
//        } else {
//            throw new FileNotFoundException("The specified file does not exist for this user.");
//        }
//    }

    // Method to check for circular references in the new expression
    public void checkForCircularReferences(String userName, String spreadsheetName, String cellId, Expression newExpression) throws CircularReferenceException, SpreadsheetNotFoundException, UserNotFoundException {
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager != null) {
            versionsManager.checkForCircularReferences(cellId, newExpression);
        } else {
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");

        }
    }

    @Override
    public String getColumnName(String userName, String spreadsheetName, int index) throws SpreadsheetNotFoundException, UserNotFoundException {
        // Retrieve the VersionsManager for the specified file path
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager != null) {
            // Call the getColumnName method from the VersionsManager
            return versionsManager.getColumnName(index);
        } else {
            // Throw an exception if the specified file does not exist for the user
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");
        }
    }

    @Override
    public List<String[][]> filterTableMultipleColumns(String userName, String spreadsheetName, String tableArea, Map<String, List<String>> selectedColumnValues)
            throws SpreadsheetNotFoundException, UserNotFoundException {
        // Retrieve the VersionsManager for the specified file path
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager != null) {
            // Call the filterTableMultipleColumns method from the VersionsManager
            return versionsManager.filterTableMultipleColumns(tableArea, selectedColumnValues);
        } else {
            // Throw an exception if the specified file does not exist for the user
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");
        }
    }

    @Override
    public int getColumnIndex(String userName, String spreadsheetName, String columnName)
            throws SpreadsheetNotFoundException, UserNotFoundException {
        // Retrieve the VersionsManager for the specified Spreadsheet Name
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager != null) {
            // Call the getColumnIndex method from the VersionsManager
            return versionsManager.getColumnIndex(columnName);
        } else {
            // Throw an exception if the specified file does not exist for the user
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");
        }
    }

    @Override
    public Expression parseExpression(String userName, String spreadsheetName, String input)
            throws InvalidExpressionException, SpreadsheetNotFoundException, UserNotFoundException {
        // Retrieve the VersionsManager for the specified file path
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager != null) {
            // Call the parseExpression method from the VersionsManager
            return versionsManager.parseExpression(input);
        } else {
            // Throw an exception if the specified file does not exist for the user
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");
        }
    }

    @Override
    public Spreadsheet getSpreadsheetByVersion(String userName, String spreadsheetName, int versionNumber)
            throws IndexOutOfBoundsException, SpreadsheetNotFoundException, UserNotFoundException {
        // Retrieve the VersionsManager for the specified file path
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager != null) {
            // Call the getSpreadsheetByVersion method from the VersionsManager
            return versionsManager.getSpreadsheetByVersion(versionNumber);
        } else {
            // Throw an exception if the specified file does not exist for the user
            throw new SpreadsheetNotFoundException("The specified file does not exist for this user.");
        }
    }

    @Override
    public void askForPermission(String username, String spreadsheetName, PermissionType permissionType) {
        // Retrieve the VersionsManager for the specified spreadsheet name
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager != null) {
            // Call the getSpreadsheetByVersion method from the VersionsManager
            versionsManager.askForPermission(username, permissionType);
        }
    }

    @Override
    public PermissionType getUserPermission(String username, String spreadsheetName) throws SpreadsheetNotFoundException {
        // Retrieve the VersionsManager for the specified spreadsheet name
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager != null) {
            // Call the getSpreadsheetByVersion method from the VersionsManager
            return versionsManager.getUserPermission(username);
        } else {
            // Throw an exception if the specified spreadsheet does not exist for the user
            throw new SpreadsheetNotFoundException("Couldn't find the spreadsheet in the system");
        }
    }

    public Map<String, VersionsManager> getSpreadsheetsMap() {
        return spreadsheetsMap;
    }

    public void handlePermissionRequest(String applicantName, String handlerName, String spreadsheetName, PermissionStatus permissionStatus, PermissionType permissionType) {
        // Retrieve the VersionsManager for the specified spreadsheet name
        VersionsManager versionsManager = spreadsheetsMap.get(spreadsheetName);

        if (versionsManager != null) {
            // Call the getSpreadsheetByVersion method from the VersionsManager
            versionsManager.handlePermissionRequest(applicantName, handlerName, permissionStatus, permissionType);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EngineImpl engine = (EngineImpl) o;
        return Objects.equals(spreadsheetsMap, engine.spreadsheetsMap) && Objects.equals(userManager, engine.userManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spreadsheetsMap, userManager);
    }
}