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
    private final Map<User, Map<String, VersionsManager>> clientFilesVersions; //String is the file name
    private final UserManager userManager;

    public EngineImpl() {
        this.clientFilesVersions = new HashMap<>();
        this.userManager = new UserManager();
    }

    @Override
    public synchronized void addUser(String userName) throws Exception {
        userManager.addUser(userName);
        clientFilesVersions.putIfAbsent(userManager.getUser(userName), new HashMap<>());
    }

    @Override
    public synchronized boolean isUserExist(String userName) {
        return userManager.isUserExists(userName);
    }

    @Override
    public String loadSpreadsheet(String userName, String filePath) throws Exception {
        User currentUser = userManager.getUser(userName);;
        String normalizedUserName = userName.toLowerCase();

        // Load spreadsheet first to get the file name
        VersionsManager versionsManager = new VersionsManager(userName);
        versionsManager.loadSpreadsheet(filePath);
        String fileName = versionsManager.getCurrentSpreadsheet().getName(); // Get the file name after loading

        // Check if the file path already exists for any user in the system
        for (Map.Entry<User, Map<String, VersionsManager>> entry : clientFilesVersions.entrySet()) {
            Map<String, VersionsManager> userFiles = entry.getValue();

            // Check if the file path exists for this user
            if (userFiles.containsKey(fileName)) {
                // File already exists for another user, throw exception
                throw new SpreadsheetLoadingException("The file name '" + fileName + "' already exists for another user.");
            }
        }

        // If the file does not exist for any user, add it for the current user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(currentUser);

        // If the user does not exist, create a new entry
        if (userFiles == null) {
            userFiles = new HashMap<>();
            clientFilesVersions.put(currentUser, userFiles);
            userManager.addUser(userName);
        }

        userFiles.put(fileName, versionsManager); //add the version manager and the file name to the user's map

        return fileName; // Return that the file was newly loaded
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
            removeUserFromClientFilesVersions(userName);  // Method to clean up invalid users
            return new EngineDTO(Collections.emptyMap(),  0); // Return empty DTO or handle appropriately
        }

        User user = userManager.getUser(userName);

        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);
        Map<Integer, VersionDTO> versionDTOMap = new HashMap<>();

        if (userFiles != null) {
            VersionsManager versionsManager = userFiles.get(fileName);

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
        clientFilesVersions.remove(userManager.getUser(userName));
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
    public int getCurrentVersion(String userName, String fileName) {
        User user = userManager.getUser(userName);

        // Check if the user exists in the clientFilesVersions map
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);
        if (userFiles == null) {
            throw new IllegalArgumentException("User not found: " + userName);
        }
        // Retrieve the VersionsManager for the specific filePath
        VersionsManager versionsManager = userFiles.get(fileName);
        if (versionsManager == null) {
            throw new IllegalArgumentException("File path not found for user: " + fileName);
        }
        return versionsManager.getCurrentVersion();
    }

    @Override
    public Spreadsheet getCurrentSpreadsheet(String userName, String fileName) {
        User user = userManager.getUser(userName);
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);
        if (userFiles == null) {
            throw new IllegalArgumentException("User not found: " + userName);
        }
        VersionsManager versionsManager = userFiles.get(fileName);
        if (versionsManager == null) {
            throw new IllegalArgumentException("File path not found for user: " + fileName);
        }
        return versionsManager.getCurrentSpreadsheet();
    }

    @Override
    public void updateCellValue(String userName, String fileName, String cellId, String newValue)
            throws CircularReferenceException, CellUpdateException, FileNotFoundException, UserNotFoundException {
        User user = userManager.getUser(userName);
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);
        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(fileName);

            // Check if the VersionsManager exists
            if (versionsManager != null) {
                // Update the cell value in the VersionsManager
                versionsManager.updateCellValue(cellId, newValue);
            } else {
                throw new FileNotFoundException("The specified file does not exist for this user.");
            }
        } else {
            throw new UserNotFoundException("The user does not exist.");
        }
    }
    @Override
    public void addRange(String userName, String fileName, String rangeName, String firstCell, String lastCell) throws Exception {
        User user = userManager.getUser(userName);

        // Retrieve the map of files for the specified user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(fileName);

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
    public void removeRange(String userName, String fileName, String rangeName) throws Exception {
        User user = userManager.getUser(userName);

        // Retrieve the map of files for the specified user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(fileName);

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
    public Map<String, String> sortSpreadsheet(String userName, String fileName, Spreadsheet spreadsheet, String range, List<String> columnsToSortBy)
            throws InvalidColumnException, FileNotFoundException, UserNotFoundException {
        User user = userManager.getUser(userName);

        // Retrieve the map of files for the specified user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(fileName);

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
    public Map<String, Range> getAllRanges(String userName, String fileName) throws FileNotFoundException, UserNotFoundException {
        User user = userManager.getUser(userName);

        // Retrieve the map of files for the specified user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        // Check if the userFiles map exists
        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified filePath
            VersionsManager versionsManager = userFiles.get(fileName);

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
    public void checkForCircularReferences(String userName, String fileName, String cellId, Expression newExpression) throws CircularReferenceException, FileNotFoundException, UserNotFoundException {
        User user = userManager.getUser(userName);
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if(userFiles != null) {
            VersionsManager versionsManager = userFiles.get(fileName);
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
    public String getColumnName(String userName, String fileName, int index) throws FileNotFoundException, UserNotFoundException {
        User user = userManager.getUser(userName);

        // Retrieve the map of version managers associated with the user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified file path
            VersionsManager versionsManager = userFiles.get(fileName);

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
    public List<String[][]> filterTableMultipleColumns(String userName, String fileName, String tableArea, Map<String, List<String>> selectedColumnValues)
            throws FileNotFoundException, UserNotFoundException {
        User user = userManager.getUser(userName);

        // Retrieve the map of version managers associated with the user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified file path
            VersionsManager versionsManager = userFiles.get(fileName);

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
        User user = userManager.getUser(userName);

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
    public Expression parseExpression(String userName, String fileName, String input)
            throws InvalidExpressionException, FileNotFoundException, UserNotFoundException {
        User user = userManager.getUser(userName);

        // Retrieve the map of version managers associated with the user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified file path
            VersionsManager versionsManager = userFiles.get(fileName);

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
    public Spreadsheet getSpreadsheetByVersion(String userName, String fileName, int versionNumber)
            throws IndexOutOfBoundsException, FileNotFoundException, UserNotFoundException {
        // Create a User object based on the provided username
        User user = userManager.getUser(userName);

        // Retrieve the map of version managers associated with the user
        Map<String, VersionsManager> userFiles = clientFilesVersions.get(user);

        if (userFiles != null) {
            // Retrieve the VersionsManager for the specified file path
            VersionsManager versionsManager = userFiles.get(fileName);

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

    public Map<String, VersionsManager> getClientFilesVersions(String username) {
        return clientFilesVersions.get(userManager.getUser(username));
    }

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