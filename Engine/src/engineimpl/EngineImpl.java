package engineimpl;
import api.Engine;
import api.Expression;
import api.Range;
import dto.*;
import dto.VersionDTO;
import exceptions.engineexceptions.*;
import expressionimpls.*;
import filter.SpreadsheetFilter;
import generatedschemafilesv2.*;
import cells.Cell;
import spreadsheet.Spreadsheet;
import user.User;
import user.UserManager;
import versions.Version;
import ranges.RangesManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Map;
import java.io.File;
import java.util.function.Supplier;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import versions.VersionsManager;

//This class implements the api.Engine interface and manages the spreadsheet
// First version of the spreadsheet will be index 1
public class EngineImpl implements Engine {

    private final Map<User, Map<String, VersionsManager>> clientFilesVersions; //String is the file path
    private final UserManager userManager;

    // private final VersionsManager versionsManager;
    //private final RangesManager rangesManager;
    //private final SpreadsheetFilter spreadsheetFilterer;
    //private static final int MAX_ROWS = 50;
    //private static final int MAX_COLS = 20;
    //private Supplier<Spreadsheet> spreadsheetSupplier = this::getCurrentSpreadsheet;

    public EngineImpl() {
       // this.versionsManager = new VersionsManager();
       // this.rangesManager = new RangesManager();
        //this.spreadsheetFilterer = new SpreadsheetFilter(this);
        this.clientFilesVersions = new HashMap<>();
        this.userManager = new UserManager();
    }

    public void addUser(String userName) {
        User user = new User(userName);
        clientFilesVersions.putIfAbsent(user, new HashMap<>());
    }

    public void loadSpreadsheet(String userName, String filePath) throws
            SpreadsheetLoadingException, CellUpdateException, CircularReferenceException, RangeProcessException {

        // Check if the file path already exists for any user in the system
        for (Map.Entry<User, Map<String, VersionsManager>> entry : clientFilesVersions.entrySet()) {
            Map<String, VersionsManager> userFiles = entry.getValue();
            if (userFiles.containsKey(filePath)) {
                // File already exists for some user, throw an exception
                throw new SpreadsheetLoadingException("The file path '" + filePath + "' already exists for another user.");
            }
        }

        User user = new User(userName);
        // Normalize the username to lower case for case-insensitive comparison
        String normalizedUserName = userName.toLowerCase();

        // Check if the user already exists (case-insensitive)
        Map<String, VersionsManager> userFiles = null;
        for (User existingUser : clientFilesVersions.keySet()) {
            if (existingUser.getUserName().equalsIgnoreCase(user.getUserName())) {
                userFiles = clientFilesVersions.get(existingUser); // Get the existing user map
                break;
            }
        }

        // If the user does not exist, create a new entry
        if (userFiles == null) {
            userFiles = new HashMap<>(); // Create a new map for the user
            clientFilesVersions.put(user, userFiles); // Add to the main map with the User object
        }

        // Create a new VersionsManager for the user and load the spreadsheet
        VersionsManager versionsManager = new VersionsManager();
        userFiles.put(filePath, versionsManager);

        // Load the spreadsheet using the versions manager
        versionsManager.loadSpreadsheet(filePath);
    }

//    @Override
//    public void clearVersions() {
//        this.versionsManager.clearVersions();
//    }

    @Override
    // Method to get the engine data using dto
    public EngineDTO getEngineData(String userName, String filePath) {
        if (!userManager.isUserExists(userName)) {
            // If user doesn't exist, remove them from clientFilesVersions
            removeUserFromClientFilesVersions(userName);  // Method to clean up invalid users
            return new EngineDTO(Collections.emptyMap(),  0); // Return empty DTO or handle appropriately
        }
        User user = new User(userName);

        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);
        Map<Integer, VersionDTO> versionDTOMap = new HashMap<>();

        if (userFiles != null) {
            VersionsManager versionsManager = userFiles.get(filePath);
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

    private void removeUserFromClientFilesVersions(String userName) {
        User user = new User(userName);
        clientFilesVersions.remove(user);
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
                    new HashMap<>(),  // Placeholder for dependsOnThem
                    new HashMap<>()   // Placeholder for dependsOnMe
            );
            cellDTOMap.put(entry.getKey(), cellDTO);
        }
        // Update the dependencies in the CellDTOs
        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
            String cellId = entry.getKey();
            Cell cell = entry.getValue();
            CellDTO cellDTO = cellDTOMap.get(cellId);

            // Deep copy for dependsOnThem
            for (Map.Entry<String, Cell> dependsOnThemEntry : cell.getDependsOnThem().entrySet()) {
                String dependsOnId = dependsOnThemEntry.getKey();
                cellDTO.getDependsOnThem().put(dependsOnId, cellDTOMap.get(dependsOnId));
            }

            // Deep copy for dependsOnMe
            for (Map.Entry<String, Cell> dependsOnMeEntry : cell.getDependsOnMe().entrySet()) {
                String dependsOnId = dependsOnMeEntry.getKey();
                cellDTO.getDependsOnMe().put(dependsOnId, cellDTOMap.get(dependsOnId));
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
    public int getCurrentVersion(String userName, String filePath) {
        User user = new User(userName);

        // Check if the user exists in the clientFilesVersions map
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);
        if (userFiles == null) {
            throw new IllegalArgumentException("User not found: " + userName);
        }
        // Retrieve the VersionsManager for the specific filePath
        VersionsManager versionsManager = userFiles.get(filePath);
        if (versionsManager == null) {
            throw new IllegalArgumentException("File path not found for user: " + filePath);
        }
        return versionsManager.getCurrentVersion();
    }

    @Override
    public Spreadsheet getCurrentSpreadsheet(String userName, String filePath) {
        User user = new User(userName);
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);
        if (userFiles == null) {
            throw new IllegalArgumentException("User not found: " + userName);
        }
        VersionsManager versionsManager = userFiles.get(filePath);
        if (versionsManager == null) {
            throw new IllegalArgumentException("File path not found for user: " + filePath);
        }
        return versionsManager.getCurrentSpreadsheet();
    }

    @Override
    public void updateCellValue(String userName, String filePath, String cellId, String newValue)
            throws CircularReferenceException, CellUpdateException, SpreadsheetLoadingException {
        User user = new User(userName);
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);
        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(filePath);

            // Check if the VersionsManager exists
            if (versionsManager != null) {
                // Update the cell value in the VersionsManager
                versionsManager.updateCellValue(cellId, newValue);
            } else {
                throw new SpreadsheetLoadingException("The specified file does not exist for this user.");
            }
        } else {
            throw new SpreadsheetLoadingException("The user does not exist.");
        }
    }
    @Override
    public void addRange(String userName, String filePath, String rangeName, String firstCell, String lastCell) throws Exception {
        // Create a User object with the provided userName
        User user = new User(userName);

        // Retrieve the map of files for the specified user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(filePath);

            // Check if the VersionsManager exists
            if (versionsManager != null) {
                // Call the existing addRange method from VersionsManager
                versionsManager.addRange(rangeName, firstCell, lastCell);
            } else {
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            throw new UserNotFoundException("The user does not exist.");
        }
    }

    @Override
    public void removeRange(String userName, String filePath, String rangeName) throws Exception {
        // Create a User object with the provided userName
        User user = new User(userName);

        // Retrieve the map of files for the specified user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(filePath);

            // Check if the VersionsManager exists
            if (versionsManager != null) {

                // Call the existing removeRange method from VersionsManager
                versionsManager.removeRange(rangeName.toUpperCase());
            } else {
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            throw new UserNotFoundException("The user does not exist.");
        }
    }

    @Override
    public Map<String, String> sortSpreadsheet(String userName, String filePath, Spreadsheet spreadsheet, String range, List<String> columnsToSortBy)
            throws InvalidColumnException, FileNotFoundException, UserNotFoundException {
        // Create a User object with the provided userName
        User user = new User(userName);

        // Retrieve the map of files for the specified user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(filePath);

            // Check if the VersionsManager exists
            if (versionsManager != null) {
                // Perform sorting on the provided spreadsheet
                return versionsManager.sortSpreadsheet(spreadsheet, range, columnsToSortBy);
            } else {
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            throw new UserNotFoundException("The user does not exist.");
        }
    }

    @Override
    public Map<String, Range> getAllRanges(String userName, String filePath) throws FileNotFoundException, UserNotFoundException {
        // Create a User object with the provided userName
        User user = new User(userName);

        // Retrieve the map of files for the specified user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(filePath);

            // Check if the VersionsManager exists
            if (versionsManager != null) {
                // Call the getAllRanges method from the VersionsManager
                return versionsManager.getAllRanges();
            } else {
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            throw new UserNotFoundException("The user does not exist.");
        }
    }

        // Method to check for circular references in the new expression
    public void checkForCircularReferences(String userName, String filePath, String cellId, Expression newExpression) throws CircularReferenceException, FileNotFoundException, UserNotFoundException {

        User user = new User(userName);
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if(userFiles != null) {
            VersionsManager versionsManager = userFiles.get(filePath);
            if (versionsManager != null) {
                versionsManager.checkForCircularReferences(cellId, newExpression);
            } else {
                throw new FileNotFoundException("The specified file does not exist for this user.");

            }
        } else{
            throw new UserNotFoundException("The user does not exist.");
        }
    }
    @Override
    public String getColumnName(String userName, String filePath, int index) throws FileNotFoundException, UserNotFoundException {
        // Create a User object based on the provided username
        User user = new User(userName);

        // Retrieve the map of version managers associated with the user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified file path
            VersionsManager versionsManager = userFiles.get(filePath);

            if (versionsManager != null) {
                // Call the getColumnName method from the VersionsManager
                return versionsManager.getColumnName(index);
            } else {
                // Throw an exception if the specified file does not exist for the user
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            // Throw an exception if the user does not exist
            throw new UserNotFoundException("The user does not exist.");
        }
    }

    @Override
    public List<String[][]> filterTableMultipleColumns(String userName, String filePath, String tableArea, Map<String, List<String>> selectedColumnValues)
            throws FileNotFoundException, UserNotFoundException {

        // Create a User object based on the provided username
        User user = new User(userName);

        // Retrieve the map of version managers associated with the user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified file path
            VersionsManager versionsManager = userFiles.get(filePath);

            if (versionsManager != null) {
                // Call the filterTableMultipleColumns method from the VersionsManager
                return versionsManager.filterTableMultipleColumns(tableArea, selectedColumnValues);
            } else {
                // Throw an exception if the specified file does not exist for the user
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            // Throw an exception if the user does not exist
            throw new UserNotFoundException("The user does not exist.");
        }
    }

    @Override
    public int getColumnIndex(String userName, String filePath, String columnName)
            throws FileNotFoundException, UserNotFoundException {

        // Create a User object based on the provided username
        User user = new User(userName);

        // Retrieve the map of version managers associated with the user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified file path
            VersionsManager versionsManager = userFiles.get(filePath);

            if (versionsManager != null) {
                // Call the getColumnIndex method from the VersionsManager
                return versionsManager.getColumnIndex(columnName);
            } else {
                // Throw an exception if the specified file does not exist for the user
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            // Throw an exception if the user does not exist
            throw new UserNotFoundException("The user does not exist.");
        }
    }

    @Override
    public Expression parseExpression(String userName, String filePath, String input)
            throws InvalidExpressionException, FileNotFoundException, UserNotFoundException {

        // Create a User object based on the provided username
        User user = new User(userName);

        // Retrieve the map of version managers associated with the user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified file path
            VersionsManager versionsManager = userFiles.get(filePath);

            if (versionsManager != null) {
                // Call the parseExpression method from the VersionsManager
                return versionsManager.parseExpression(input);
            } else {
                // Throw an exception if the specified file does not exist for the user
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            // Throw an exception if the user does not exist
            throw new UserNotFoundException("The user does not exist.");
        }
    }

    @Override
    public Spreadsheet getSpreadsheetByVersion(String userName, String filePath, int versionNumber)
            throws IndexOutOfBoundsException, FileNotFoundException, UserNotFoundException {

        // Create a User object based on the provided username
        User user = new User(userName);

        // Retrieve the map of version managers associated with the user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified file path
            VersionsManager versionsManager = userFiles.get(filePath);

            if (versionsManager != null) {
                // Call the getSpreadsheetByVersion method from the VersionsManager
                return versionsManager.getSpreadsheetByVersion(versionNumber);
            } else {
                // Throw an exception if the specified file does not exist for the user
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            // Throw an exception if the user does not exist
            throw new UserNotFoundException("The user does not exist.");
        }
    }

//    @Override
//    // Method to get the engine data using dto
//    public EngineDTO getEngineData() {
//        // Create a map of version numbers to VersionDTOs
//        Map<Integer, VersionDTO> versionDTOMap = new HashMap<>();
//
//        // Get Versions
//        Map<Integer, Version> versions = getVersions();
//
//        // Retrieve ranges from RangesManager
//        Map<String, Range> ranges = rangesManager.getAllRanges();
//
//        // Convert each version to a VersionDTO and add it to the map
//        for (Map.Entry<Integer, Version> entry : versions.entrySet()) {
//            Version version = entry.getValue();
//
//            // Collecting ranges as DTOs
//            List<RangeDTO> rangeDTOList = ranges.values().stream()
//                    .map(this::convertRangeToDTO)
//                    .collect(Collectors.toList());
//
//            versionDTOMap.put(entry.getKey(), new VersionDTO(
//                    version.getVersionNumber(),
//                    version.getChangedCellsCount(),
//                    convertSpreadsheetToDTO(versions.get(entry.getKey()).getSpreadsheet()),
//                    rangeDTOList // Add ranges to the VersionDTO
//            ));
//        }
//
//        // Return EngineDTO with versions, current version DTO, and last validation error
//        return new EngineDTO(versionDTOMap, getCurrentVersion());
//    }
//
//    // Method to convert spreadsheet to DTO
//    public SpreadsheetDTO convertSpreadsheetToDTO(Spreadsheet spreadsheet) {
//        if (spreadsheet == null) {
//            return null;
//        }
//
//        return new SpreadsheetDTO(
//                spreadsheet.getName(),
//                spreadsheet.getRows(),
//                spreadsheet.getColumns(),
//                spreadsheet.getColumnWidth(),
//                spreadsheet.getRowHeight(),
//                spreadsheet.getVersionNumber(),
//                convertCellsToDTO(spreadsheet.getCells())
//        );
//    }
//
//    // Method to convert cells to DTO
//    private Map<String, CellDTO> convertCellsToDTO(Map<String, Cell> cells) {
//        // Create basic CellDTOs without dependencies
//        Map<String, CellDTO> cellDTOMap = new HashMap<>();
//        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
//            Cell cell = entry.getValue();
//            CellDTO cellDTO = new CellDTO(
//                    cell.getOriginalValue(),
//                    cell.getEffectiveValue(),
//                    cell.getLastUpdatedVersion(),
//                    new HashMap<>(),  // Placeholder for dependsOnThem
//                    new HashMap<>()   // Placeholder for dependsOnMe
//            );
//            cellDTOMap.put(entry.getKey(), cellDTO);
//        }
//        // Update the dependencies in the CellDTOs
//        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
//            String cellId = entry.getKey();
//            Cell cell = entry.getValue();
//            CellDTO cellDTO = cellDTOMap.get(cellId);
//
//            // Deep copy for dependsOnThem
//            for (Map.Entry<String, Cell> dependsOnThemEntry : cell.getDependsOnThem().entrySet()) {
//                String dependsOnId = dependsOnThemEntry.getKey();
//                cellDTO.getDependsOnThem().put(dependsOnId, cellDTOMap.get(dependsOnId));
//            }
//
//            // Deep copy for dependsOnMe
//            for (Map.Entry<String, Cell> dependsOnMeEntry : cell.getDependsOnMe().entrySet()) {
//                String dependsOnId = dependsOnMeEntry.getKey();
//                cellDTO.getDependsOnMe().put(dependsOnId, cellDTOMap.get(dependsOnId));
//            }
//        }
//
//        return cellDTOMap;
//    }
//
//    private RangeDTO convertRangeToDTO(Range range) {
//        if (range == null) {
//            return null; // Handle null case if necessary
//        }
//        return new RangeDTO(
//                range.getName(), // Assuming Range has a getName() method
//                range.getStartCell(),
//                range.getEndCell(),
//                range.getCells() // Assuming Range has a getCells() method returning List<String>
//        );
//    }






//    @Override
//    // Method to load a spreadsheet from an XML file
//    public void loadSpreadsheet(String filePath) throws
//        SpreadsheetLoadingException, CellUpdateException, CircularReferenceException, RangeProcessException {
//        // Create a deep copy of the current versions map
//        Map<Integer, Version> originalVersions = new HashMap<>();
//
//        // Get Versions Map
//        Map<Integer, Version> versions = getVersions();
//
//        // Get current version
//        int prevCurrVersion = getCurrentVersion();
//
//        for (Map.Entry<Integer, Version> entry : versions.entrySet()) {
//            originalVersions.put(entry.getKey(), entry.getValue().deepCopy());
//        }
//
//        // Create a deep copy of the current ranges map
//        Map<String, Range> originalRanges = new HashMap<>(rangesManager.getAllRanges());
//
//        try {
//            // Unmarshal the STLSheet from the XML file
//            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
//            Unmarshaller unmarshaller = context.createUnmarshaller();
//            File file = new File(filePath);
//            STLSheet stlSheet = (STLSheet) unmarshaller.unmarshal(file);
//
//            // Clear the versions map to prepare for the new data
//            clearVersions();
//            rangesManager.clearRanges();
//            // Validate and load the spreadsheet into the cleared versions map
//            validateAndLoadSpreadsheet(stlSheet);
//
//        }  catch (JAXBException e) {
//            // Restore the original map if loading fails
//            versions.clear();
//            this.versionsManager.setCurrentVersionNumber(prevCurrVersion);
//            versions.putAll(originalVersions);
//            rangesManager.clearRanges();
//            rangesManager.getAllRanges().putAll(originalRanges);
//
//            throw new SpreadsheetLoadingException("Failed to parse XML file: " + e.getMessage(), e);
//        } catch (SpreadsheetLoadingException | CellUpdateException | CircularReferenceException | RangeProcessException e) {
//            // Restore the original map if validation fails
//            versions.clear();
//            this.versionsManager.setCurrentVersionNumber(prevCurrVersion);
//            versions.putAll(originalVersions);
//            rangesManager.clearRanges();
//            rangesManager.getAllRanges().putAll(originalRanges);
//
//            throw e; // Re-throw the original exception
//        }
//    }
//
//    private void validateAndLoadSpreadsheet(STLSheet stlSheet) throws
//            SpreadsheetLoadingException, CellUpdateException, CircularReferenceException, RangeProcessException {
//        // Validate the sheet dimensions
//        validateSheetDimensions(stlSheet);
//
//        // Initialize the Spreadsheet object
//        Spreadsheet spreadsheet = initializeSpreadsheet(stlSheet);
//
//        // Create a new version for the spreadsheet
//        createVersion(spreadsheet);
//
//        // Process ranges
//        processRanges(stlSheet);
//
//        // Validate and load cells
//        processCells(stlSheet);
//    }
//
//    private void validateSheetDimensions(STLSheet stlSheet) throws SpreadsheetLoadingException {
//        int rows = stlSheet.getSTLLayout().getRows();
//        int columns = stlSheet.getSTLLayout().getColumns();
//
//        if (rows < 1 || rows > MAX_ROWS) {
//            throw new SpreadsheetLoadingException("Row count must be between 1 and " + MAX_ROWS);
//        }
//        if (columns < 1 || columns > MAX_COLS) {
//            throw new SpreadsheetLoadingException("Column count must be between 1 and " + MAX_COLS);
//        }
//    }
//
//    private Spreadsheet initializeSpreadsheet(STLSheet stlSheet) {
//        int newVersionNumber = 0;
//        Spreadsheet spreadsheet = new Spreadsheet();
//        spreadsheet.setName(stlSheet.getName());
//        spreadsheet.setRows(stlSheet.getSTLLayout().getRows());
//        spreadsheet.setColumns(stlSheet.getSTLLayout().getColumns());
//        spreadsheet.setRowHeight(stlSheet.getSTLLayout().getSTLSize().getRowsHeightUnits());
//        spreadsheet.setColumnWidth(stlSheet.getSTLLayout().getSTLSize().getColumnWidthUnits());
//        spreadsheet.setVersionNumber(++newVersionNumber);
//        this.versionsManager.setCurrentVersionNumber(newVersionNumber);
//
//        return spreadsheet;
//    }
//
//    private void createVersion(Spreadsheet spreadsheet) {
//        Version newVersion = new Version(spreadsheet.getVersionNumber(), 0, spreadsheet);
//        getVersions().put(spreadsheet.getVersionNumber(), newVersion);
//    }
//
//    private void processCells(STLSheet stlSheet) throws
//            SpreadsheetLoadingException, CellUpdateException, CircularReferenceException {
//        List<STLCell> cellList = stlSheet.getSTLCells().getSTLCell();
//        int numOfCellsChanged = 0;
//        int currentVersion = getCurrentVersion();
//        Map<Integer, Version> versions = this.versionsManager.getVersions();
//
//        for (STLCell stlCell : cellList) {
//            String cellId = stlCell.getColumn() + stlCell.getRow();
//            String cellValue = stlCell.getSTLOriginalValue().strip();
//
//            try {
//                validateCellIdWhenLoading(cellId, stlSheet);
//                cellId = cellId.toUpperCase();
//                Cell cell = getOrCreateCell(cellId, versions.get(currentVersion).getSpreadsheet());
//                cell.setOriginalValue(cellValue);
//                cell.setLastUpdatedVersion(currentVersion);
//                parseAndApplyNewExpressionToCell(cellId, cellValue, cell, versions.get(currentVersion).getSpreadsheet());
//                numOfCellsChanged++;
//            } catch (InvalidCellIdFormatException | InvalidColumnException | InvalidRowException e) {
//                throw new SpreadsheetLoadingException(e.getMessage());
//            }
//        }
//        try {
//            versions.get(currentVersion).getSpreadsheet().recalculateEffectiveCellValues(1);
//        }catch (CircularReferenceException e) {
//            throw new CircularReferenceException("Circular reference detected: " + e.getMessage());
//        }
//
//
//        // Update the number of cells changed in the version
//        versions.get(currentVersion).setNumOfCellsChanged(numOfCellsChanged);
//    }
//
//    private void processRanges(STLSheet stlSheet) throws RangeProcessException {
//        // Assuming STLRanges is part of STLSheet
//        STLRanges stlRanges = stlSheet.getSTLRanges(); // Adjust according to actual method of accessing ranges
//
//        if (stlRanges != null) {
//            for (STLRange stlRange : stlRanges.getSTLRange()) {
//                String rangeName = stlRange.getName();
//                STLBoundaries boundaries = stlRange.getSTLBoundaries();
//
//                if (boundaries != null) {
//                    String from = boundaries.getFrom();
//                    String to = boundaries.getTo();
//
//                    // Add the range to the RangesManager
//                    try {
//                        // Leverage addRange for validation and addition
//                        addRange(rangeName, from, to);
//                    } catch (Exception e) {
//                        throw new RangeProcessException(e.getMessage());
//                    }
//                }
//            }
//        }
//    }
//
//
//    // Method to validate a cell ID based on spreadsheet size
//    private void validateCellIdWhenLoading(String cellId, STLSheet sheet) throws
//            InvalidColumnException, InvalidRowException, InvalidCellIdFormatException {
//
//        // Check if cellId is null
//        if (cellId == null) {
//
//            throw new InvalidCellIdFormatException(cellId);
//        }
//
//        // Convert the cell ID to uppercase to handle case insensitivity
//        cellId = cellId.toUpperCase();
//
//        // Validate the cell ID format using uppercase
//        if (!cellId.matches("^[A-Z]+[0-9]+$")) {
//            throw new InvalidCellIdFormatException(cellId);
//        }
//
//        // Extract column letters and row number from the cell ID
//        String columnPart = cellId.replaceAll("[0-9]", "");
//        String rowPart = cellId.replaceAll("[A-Z]", "");
//
//        // Check if there's at least one letter and one digit
//        if (columnPart.isEmpty() || rowPart.isEmpty()) {
//            throw new InvalidCellIdFormatException(cellId);
//        }
//
//        char column = columnPart.charAt(0);
//        int row = Integer.parseInt(rowPart);
//
//        // Determine the maximum valid column and row based on the spreadsheet size
//        int maxColumnIndex = sheet.getSTLLayout().getColumns() - 1; // 0-indexed
//        char maxColumn = (char) ('A' + maxColumnIndex);
//        int maxRow = sheet.getSTLLayout().getRows();
//
//        // Validate the column part
//        if (column < 'A' || column > maxColumn) {
//            throw new InvalidColumnException(cellId, column, maxColumn);
//        }
//
//        // Validate the row part
//        if (row < 1 || row > maxRow) {
//            throw new InvalidRowException(cellId, row, maxRow);
//        }
//    }
//
//    // test
//    @Override
//    public void updateCellValue(String cellId, String newValue)
//            throws CircularReferenceException, CellUpdateException {
//        // Get the current spreadsheet instance
//        Spreadsheet currentSpreadsheet = getCurrentSpreadsheet();
//
//        // Change cellId to be uppercased for case-sensitivity
//        cellId = cellId.toUpperCase();
//
//        // Strip new value of white spaces
//        newValue = newValue.strip();
//
//        // We want to create a new version, so create a new spreadsheet
//        currentSpreadsheet = new Spreadsheet(currentSpreadsheet);
//
//        // Ensure that a spreadsheet is loaded, otherwise throw an exception
//        validateSpreadsheetLoaded(currentSpreadsheet);
//
//        // Retrieve the cell by its ID, or create a new one if it doesn't exist
//        Cell cell = getOrCreateCell(cellId, currentSpreadsheet);
//
//        // Store the current original value
//        String currentOriginalValue = cell.getOriginalValue();
//
//        // Determine if the new value is different from the original value in the cell
//        boolean valueChanged = !newValue.equals(currentOriginalValue);
//
//        // Set the original value of the cell to the new value provided by the user
//        cell.setOriginalValue(newValue);
//
//        // If the new value is empty, clear the cell's content
//        if (isNewValueEmpty(newValue)) {
//            clearCell(cellId, currentSpreadsheet);
//        } else {
//            // Otherwise, update the cell with the new value, parsing and recalculating dependencies
//            parseAndApplyNewExpressionToCell(cellId, newValue, cell, currentSpreadsheet);
//        }
//
//        // If the cell was new or its value changed, create a new version of the spreadsheet
//        if (isNewCellOrValueChanged(cell, valueChanged)) {
//            // Update the cell's last updated version
//            cell.setLastUpdatedVersion(getCurrentVersion() + 1);
//            saveNewVersion(cellId, currentSpreadsheet);
//            try {
//                // Recalculate the entire spreadsheet to update the effective values of all dependent cells
//                getCurrentSpreadsheet().recalculateEffectiveCellValues(getCurrentVersion());
//            } catch (Exception e) {
//                this.versionsManager.deleteLatestVersion();
//                throw e;
//            }
//        }
//    }
//
//    // Validate that a spreadsheet is loaded
//    private void validateSpreadsheetLoaded(Spreadsheet spreadsheet) throws CellUpdateException {
//        if (spreadsheet == null) {
//            throw new CellUpdateException("No spreadsheet is currently loaded. Please load a spreadsheet and try again.");
//        }
//    }
//
//    // Get an existing cell or create a new one if it doesn't exist
//    protected Cell getOrCreateCell(String cellId, Spreadsheet spreadsheet) {
//        cellId = cellId.toUpperCase();
//        Cell cell = spreadsheet.getCellById(cellId);
//
//        if (cell == null) {
//            cell = new Cell();
//            spreadsheet.addCell(cellId, cell);
//        }
//
//        return cell;
//    }
//
//    // Check if the new value is empty
//    private boolean isNewValueEmpty(String newValue) {
//        return newValue == null || newValue.trim().isEmpty();
//    }
//
//    // Clear the value of a cell
//    private void clearCell(String cellId, Spreadsheet spreadsheet) {
//        spreadsheet.clearCellValue(cellId);
//    }
//
//    // Update the cell with the new value
//    private void parseAndApplyNewExpressionToCell(String cellId, String newValue, Cell cell, Spreadsheet spreadsheet)
//            throws CircularReferenceException, CellUpdateException {
//        try {
//            // Parse the new value into an Expression, which could be a function or a literal
//            //Supplier<Spreadsheet> spreadsheetSupplier = this::getCurrentSpreadsheet;
//            Expression newExpression = ExpressionParser.parse(newValue, spreadsheetSupplier, rangesManager);
//
//            // Check for any circular references that might be introduced by the new expression
//            checkForCircularReferences(cellId, newExpression);
//
//            // Update the cell's expression with the newly parsed expression
//            cell.setExpression(newExpression);
//
//            // Update the dependencies of the cell based on the new expression
//            // This ensures that the cell correctly tracks which other cells it depends on and which cells depend on it
//            updateDependencies(cellId, cell, newExpression, spreadsheet);
//        } catch (CircularReferenceException e) {
//            // If a circular reference is detected, rethrow the exception to indicate the error
//            throw e;
//        } catch (Exception e) {
//            throw new CellUpdateException("Failed to update cell: " + e.getMessage());
//        }
//    }
//
//    // Check if the cell is new or its value has changed
//    private boolean isNewCellOrValueChanged(Cell cell, boolean valueChanged) {
//        return cell.getOriginalValue().isEmpty() || valueChanged;
//    }
//
//    // Save a new version of the spreadsheet
//    private void saveNewVersion(String cellId, Spreadsheet spreadsheet) {
//        this.versionsManager.saveNewVersion(cellId, spreadsheet);
//    }

//    // Method to check for circular references in the new expression
//    public void checkForCircularReferences(String cellId, Expression newExpression) throws CircularReferenceException {
//        if (newExpression instanceof FunctionExpression) {
//            List<Expression> arguments = ((FunctionExpression) newExpression).getArguments();
//
//            for (Expression argument : arguments) {
//                if (argument instanceof ReferenceExpression) {
//                    String referencedCellId = ((ReferenceExpression) argument).getCellId();
//
//                    // Check if the referenced cell is the same as the one being updated
//                    if (cellId.equals(referencedCellId) || isDependentOn(cellId, referencedCellId)) {
//                        throw new CircularReferenceException("Circular reference detected: " + referencedCellId + " depends on " + cellId);
//                    }
//                }
//            }
//        }
//    }
//
//    // Method to check if a cell depends on another cell (directly or indirectly)
//    private boolean isDependentOn(String cellId, String potentialDependencyId) {
//        Cell potentialDependency = getCurrentSpreadsheet().getCellById(potentialDependencyId);
//
//        if (potentialDependency == null) {
//            return false;
//        }
//
//        // Direct dependency check
//        if (potentialDependency.getDependsOnThem().containsKey(cellId)) {
//            return true;
//        }
//
//        // Indirect dependency check (recursive)
//        for (String dependentCellId : potentialDependency.getDependsOnThem().keySet()) {
//            if (isDependentOn(cellId, dependentCellId)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    private void updateDependencies(String cellId, Cell cell, Expression newExpression, Spreadsheet spreadsheet) {
//        // Clear current dependencies
//        for (String dependencyId : cell.getDependsOnThem().keySet()) {
//            Cell dependency = spreadsheet.getCellById(dependencyId);
//
//            if (dependency != null) {
//                dependency.removeDependsOnMe(cellId);
//            }
//        }
//        cell.getDependsOnThem().clear();
//
//        // Recursively add new dependencies
//        addDependencies(cellId, cell, newExpression, spreadsheet);
//    }
//
//    private void addDependencies(String cellId, Cell cell, Expression expression, Spreadsheet spreadsheet) {
//        if (expression instanceof ReferenceExpression) {
//            String dependencyId = ((ReferenceExpression) expression).getCellId();
//
//            // Validate the cell ID before creating a new cell or adding the dependency
//            try {
//                validateCellId(dependencyId, spreadsheet);
//            } catch (Exception e) {
//                throw new RuntimeException("Invalid cell ID: " + dependencyId + ". " + e.getMessage());
//            }
//
//            // Check if the dependency cell exists
//            Cell dependency = spreadsheet.getCellById(dependencyId);
//
//            // If the dependency doesn't exist, create it
//            if (dependency == null) {
//                dependency = spreadsheet.getOrCreateCell(dependencyId);
//            }
//
//            // Add the new dependency
//            cell.addDependsOnThem(dependencyId, dependency);
//            dependency.addDependsOnMe(cellId, cell);
//
//        } else if (expression instanceof FunctionExpression) {
//            List<Expression> args = ((FunctionExpression) expression).getArguments();
//            for (Expression arg : args) {
//                // Recursively add dependencies for each argument
//                addDependencies(cellId, cell, arg, spreadsheet);
//            }
//        } else if (expression instanceof RangeExpression) {
//            // Handle RangeExpression
//            RangeExpression rangeExpression = (RangeExpression) expression;
//            Range range = rangeExpression.getRange();
//
//            // Check if the range exists
//            if (range != null) {
//                for (String rangeCellId : range.getCells()) {
//                    // Validate the cell ID before creating a new cell or adding the dependency
//                    try {
//                        validateCellId(rangeCellId, spreadsheet);
//                    } catch (Exception e) {
//                        throw new RuntimeException("Invalid cell ID in range: " + rangeCellId + ". " + e.getMessage());
//                    }
//
//                    // Check if the range cell exists
//                    Cell rangeCell = spreadsheet.getCellById(rangeCellId);
//
//                    // If the range cell doesn't exist, create it
//                    if (rangeCell == null) {
//                        rangeCell = spreadsheet.getOrCreateCell(rangeCellId);
//                    }
//
//                    // Add the new dependency for each cell in the range
//                    cell.addDependsOnThem(rangeCellId, rangeCell);
//                    rangeCell.addDependsOnMe(cellId, cell);
//                }
//            }
//        }
//    }
//
//    // Ensure that the cell ID is valid and corresponds to an existing cell within the spreadsheet
//    private void validateCellId(String cellId, Spreadsheet spreadsheet) throws InvalidCellIdFormatException, InvalidRowException, InvalidColumnException {
//        if (cellId == null || !cellId.matches("^[A-Z]+[0-9]+$")) {
//            throw new InvalidCellIdFormatException(cellId);
//        }
//
//        int row = Integer.parseInt(cellId.replaceAll("[^0-9]", ""));
//        String col = cellId.replaceAll("[^A-Z]", "");
//
//        int colIndex = col.charAt(0) - 'A' + 1;
//
//        if (row < 1 || row > spreadsheet.getRows()) {
//            throw new InvalidRowException(cellId, row, spreadsheet.getRows());
//        }
//
//        if (colIndex < 1 || colIndex > spreadsheet.getColumns()) {
//            throw new InvalidColumnException(cellId, col.charAt(0), (char) ('A' + spreadsheet.getColumns() - 1));
//        }
//    }
//
////    @Override
////    public Spreadsheet getCurrentSpreadsheet() {
////        return this.versionsManager.getCurrentSpreadsheet();
////    }
////
////    @Override
////    public Spreadsheet getSpreadsheetByVersion(int versionNumber) throws IndexOutOfBoundsException {
////        return this.versionsManager.getSpreadsheetByVersion(versionNumber);
////    }
////
////    @Override
////    public Map<Integer, Version> getVersions() {
////        return this.versionsManager.getVersions();
////    }
//
//
//    public Supplier<Spreadsheet> getSpreadsheetSupplier() {
//        return spreadsheetSupplier;
//    }
//
//    public void setSpreadsheetSupplier(Supplier<Spreadsheet> spreadsheetSupplier) {
//        this.spreadsheetSupplier = spreadsheetSupplier;
//    }
//
////    @Override
////    public int getCurrentVersion() {
////        return this.versionsManager.getCurrentVersion();
////    }
//
//    @Override
//    public void addRange(String rangeName, String firstCell, String lastCell) throws Exception {
//        // Turn cell id's to uppercase
//        firstCell = firstCell.toUpperCase();
//        lastCell = lastCell.toUpperCase();
//
//        // Validate cell IDs
//        validateCellId(firstCell, getCurrentSpreadsheet());
//        validateCellId(lastCell, getCurrentSpreadsheet());
//
//        // Check if the first cell is greater than the last cell
//        if (isFirstCellGreater(firstCell, lastCell)) {
//            throw new Exception("Invalid Range: The first cell is greater than the last cell.");
//        }
//
//        // Add the range using the RangesManager
//        rangesManager.addRange(rangeName, firstCell, lastCell);
//    }
//
//
//    // Helper method to check if the first cell is greater than the last cell
//    private boolean isFirstCellGreater(String firstCell, String lastCell) {
//        // Extract column letters and row numbers
//        String firstColumn = firstCell.replaceAll("[^A-Z]", "");
//        String lastColumn = lastCell.replaceAll("[^A-Z]", "");
//        int firstRow = Integer.parseInt(firstCell.replaceAll("[^0-9]", ""));
//        int lastRow = Integer.parseInt(lastCell.replaceAll("[^0-9]", ""));
//
//        // Compare column letters
//        if (firstColumn.compareTo(lastColumn) > 0) {
//            return true; // First cell's column is greater
//        } else if (firstColumn.equals(lastColumn)) {
//            // If columns are the same, compare row numbers
//            return firstRow > lastRow;
//        }
//
//        return false; // First cell is not greater
//    }
//
//    public Range getRange(String rangeName) {
//        return this.rangesManager.getRange(rangeName);
//    }
//
//    @Override
//    public void removeRange(String rangeName) throws Exception {
//        // Check if the range is currently in use
//        if (isRangeInUse(rangeName.toUpperCase())) {
//            throw new Exception(
//                    "The range '" + rangeName + "' is currently in use by one or more cells. Please update or remove the reference before deleting.");
//        }
//
//        // Delete the range using the RangesManager
//        rangesManager.deleteRange(rangeName.toUpperCase());
//    }
//
//    @Override
//    public Map<String, Range> getAllRanges() {
//        // Retrieve all ranges from the RangesManager
//        return rangesManager.getAllRanges();
//    }
//
//    public List<String> getRangeValues(String rangeName) {
//        Range range = getRange(rangeName);
//        List<String> values = new ArrayList<>();
//
//        // Fetch all cells within the range
//        String startCell = range.getStartCell();
//        String endCell = range.getEndCell();
//
//        // Determine the starting and ending rows and columns
//        int startRow = Integer.parseInt(startCell.replaceAll("\\D", ""));
//        int endRow = Integer.parseInt(endCell.replaceAll("\\D", ""));
//        int startColumnIndex = getColumnIndex(startCell.replaceAll("\\d", ""));
//        int endColumnIndex = getColumnIndex(endCell.replaceAll("\\d", ""));
//
//        // Loop through each cell in the range and extract values
//        for (int row = startRow; row <= endRow; row++) {
//            for (int col = startColumnIndex; col <= endColumnIndex; col++) {
//                String cellId = getColumnName(col) + row;
//                Cell cell = getCurrentSpreadsheet().getCellById(cellId);
//
//                if (cell != null && cell.getEffectiveValue() != null) {
//                    // Add all values (numeric or non-numeric) to the list
//                    values.add(cell.getEffectiveValue().toString());
//                }
//            }
//        }
//
//        return values;
//    }
//
//    @Override
//    public boolean isRangeInUse(String rangeName) {
//        // Retrieve the current spreadsheet
//        Spreadsheet currentSpreadsheet = getCurrentSpreadsheet();
//
//        if (currentSpreadsheet == null) {
//            return false;
//        }
//
//        // Iterate through all cells in the spreadsheet
//        for (Cell cell : currentSpreadsheet.getCells().values()) {
//            // Check if the cell's expression uses the specified range
//            if (cell.getExpression() instanceof FunctionExpression) {
//                FunctionExpression functionExpression = (FunctionExpression) cell.getExpression();
//
//                for (Expression argument : functionExpression.getArguments()) {
//                    if (argument instanceof RangeExpression) {
//                        RangeExpression rangeExpression = (RangeExpression) argument;
//
//                        if (rangeName.equalsIgnoreCase(rangeExpression.getRangeName())) {
//                            return true; // Range is in use
//                        }
//                    }
//                }
//            }
//        }
//        return false; // Range is not in use
//    }
//
//    @Override
//    public  Map<String, String> sortSpreadsheet(Spreadsheet spreadsheet, String range, List<String> columnsToSortBy) throws InvalidColumnException {
//
//        if (spreadsheet == null) {
//            throw new IllegalStateException("No spreadsheet loaded to sort.");
//        }
//
//        // Perform sorting on the specified range using the columns to sort by
//        // Create a map of old cell IDs to new cell IDs after sorting
//        Map<String,String> idMapping = spreadsheet.sort(range, columnsToSortBy);
//
//        return idMapping; // Return the mapping of old cell IDs to new cell IDs
//    }
//
//    @Override
//    // Helper method to convert a column letter (e.g., "A") to a zero-based index
//    public int getColumnIndex(String columnName) {
//        int index = 0;
//        for (char c : columnName.toCharArray()) {
//            index = index * 26 + (c - 'A' + 1);
//        }
//        return index - 1; // Zero-based index
//    }
//
//    @Override
//    // Helper method to convert a zero-based column index to an Excel-style column name (A, B, C, ..., Z, AA, AB, ...)
//    public String getColumnName(int index) {
//        StringBuilder columnName = new StringBuilder();
//        while (index >= 0) {
//            columnName.insert(0, (char) ('A' + (index % 26)));
//            index = (index / 26) - 1;
//        }
//        return columnName.toString();
//    }
//
//    @Override
//    public List<String[][]> filterTableMultipleColumns(String tableArea, Map<String, List<String>> selectedColumnValues) {
//        return this.spreadsheetFilterer.filterTableMultipleColumns(tableArea, selectedColumnValues);
//    }
//
//    @Override
//    public Expression parseExpression (String input) throws InvalidExpressionException {
//        return ExpressionParser.parse(input, spreadsheetSupplier, rangesManager);
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EngineImpl engine = (EngineImpl) o;
        return Objects.equals(clientFilesVersions, engine.clientFilesVersions) && Objects.equals(userManager, engine.userManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientFilesVersions, userManager);
    }
}