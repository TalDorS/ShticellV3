package gridwindow;

import api.Engine;
import api.Expression;
import api.Range;
import cells.Cell;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.*;
import exceptions.engineexceptions.*;
import expressionimpls.LiteralExpression;
import gridwindow.top.*;
import gridwindow.top.Skin;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import okhttp3.*;
import utils.ClientConstants;

import javafx.util.Duration;
import spreadsheet.Spreadsheet;
import gridwindow.grid.MainGridAreaController;
import gridwindow.grid.dynamicanalysisdialog.DynamicAnalysisDialogController;
import gridwindow.leftside.LeftSideController;
import gridwindow.leftside.addrangedialog.AddRangeDialogController;
import gridwindow.leftside.sortdialog.SortDialogController;
import utils.AlertUtils;
import utils.HttpClientUtil;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

import java.util.List;

import static utils.AlertUtils.showAlert;
import static utils.AlertUtils.showError;
import static utils.CommonResourcesPaths.*;

public class GridWindowController {

    private Engine engine; //set by the menu window controller
    private List<FadeTransition> activeFadeTransitions = new ArrayList<>();  // List to store all active transitions
    private List<RotateTransition> activeRotateTransitions = new ArrayList<>();  // List to store all active transitions
    private String spreadsheetName;
    private String userName;//set by the menu window controller
    private OkHttpClient client;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TopGridWindowController topGridWindowComponentController;

    @FXML
    private OptionsBarController optionsBarComponentController;

    @FXML
    private LeftSideController leftSideComponentController;

    @FXML
    private MainGridAreaController mainGridAreaComponentController;

    @FXML
    private DynamicAnalysisDialogController dynamicAnalysisComponentController;

    @FXML
    private SortDialogController sortDialogController;

    @FXML
    private AddRangeDialogController addRangeDialogController;

    @FXML
    public void initialize() {

        if (optionsBarComponentController != null) {
            optionsBarComponentController.setMainController(this);
        }
        if (leftSideComponentController != null) {
            leftSideComponentController.setMainController(this);
        }
        if (mainGridAreaComponentController != null) {
            mainGridAreaComponentController.setMainController(this);
        }
        if(sortDialogController != null){
            sortDialogController.setMainController(this);
        }
        if(addRangeDialogController != null){
            addRangeDialogController.setMainController(this);
        }
        if(dynamicAnalysisComponentController != null){
            dynamicAnalysisComponentController.setMainController(this);
        }
        if(topGridWindowComponentController != null){
            topGridWindowComponentController.setMainController(this);
        }

    }

    public void setName(String name) {
        if (topGridWindowComponentController != null) {
            topGridWindowComponentController.setUsername(name);
        }
    }

    public void setSpreadsheetData(String spreadsheetName) throws CellUpdateException, InvalidExpressionException,
            SpreadsheetLoadingException, RangeProcessException, CircularReferenceException {
        this.spreadsheetName = spreadsheetName;

        // Build the URL for the GET request to retrieve engine data
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_ENGINE_DATA) // URL of the servlet you created
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", this.spreadsheetName)
                .build()
                .toString();

        // Send the GET request asynchronously
        HttpClientUtil.runAsyncGet(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Parse the JSON response into EngineDTO
                    Gson gson = new Gson();
                    EngineDTO engineDTO = gson.fromJson(responseBody, EngineDTO.class);

                    // Now, use the data as before
                    int currentVersionNumber = engineDTO.getCurrentVersionNumber();
                    SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();
                    List<RangeDTO> rangesDTO = engineDTO.getRanges();
                    // Update the table view and UI
                    Platform.runLater(() -> {
                        mainGridAreaComponentController.clearGrid();
                        optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber);
                        mainGridAreaComponentController.start(spreadsheetDTO, false);

                        // Refresh dependent UI elements
                        leftSideComponentController.refreshRanges(rangesDTO);  // Pass the rangesDTO to refreshRanges method
                    });
                } else {
                    Platform.runLater(() -> {
                        String errorMessage = String.valueOf(response);
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to load engine data: " + errorMessage);
                    });
                }
            }
        });
    }


//    public void setSpreadsheetData(String spreadsheetName) throws CellUpdateException, InvalidExpressionException,
//            SpreadsheetLoadingException, RangeProcessException, CircularReferenceException {
//        try{
//            this.spreadsheetName = spreadsheetName;
//            EngineDTO engineDTO = engine.getEngineData(userName, spreadsheetName);
//            int currentVersionNumber = engineDTO.getCurrentVersionNumber();
//            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();
//
//            // Update the table view with the new data and open the grid window when the view sheet button is pressed
//            Platform.runLater(() -> {
//                // Clear the grid (if necessary)
//                mainGridAreaComponentController.clearGrid();
//
//                // Update the current version label in the options bar
//                optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber);
//
//                // Populate the grid with the new spreadsheet data
//                mainGridAreaComponentController.start(spreadsheetDTO, false);
//
//                // Refresh any dependent UI elements (ranges, etc.)
//                try {
//                    leftSideComponentController.refreshRanges();
//                } catch (UserNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    //todo- move to another controller for the sceond screen
//    public void loadSpreadsheet(String filePath) throws SpreadsheetLoadingException, CellUpdateException, InvalidExpressionException,
//            CircularReferenceException, RangeProcessException {
//        // Load the spreadsheet and update components on the JavaFX Application Thread
//        try {
//            engine.loadSpreadsheet(filePath);
//            EngineDTO engineDTO = engine.getEngineData();
//            int currentVersionNumber = engineDTO.getCurrentVersionNumber();
//            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();
//
//            // Ensure UI updates are executed on the JavaFX Application Thread
//            Platform.runLater(() -> {
//                mainGridAreaComponentController.clearGrid(); // Add a clearGrid() method to your controller if it doesn't exist
//                optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber); // Update the current version label
//                mainGridAreaComponentController.start(spreadsheetDTO, false);
//                leftSideComponentController.refreshRanges();
//            });
//
//        } catch (SpreadsheetLoadingException | CellUpdateException | InvalidExpressionException | CircularReferenceException | RangeProcessException e) {
//            // Rethrow exceptions to be handled by the calling code or task
//            throw e;
//        }
//    }

    public void highlightRange(String firstCell, String lastCell, boolean isHighlight) {
        // Delegate to the MainGridAreaController
        if (mainGridAreaComponentController != null) {
            mainGridAreaComponentController.highlightRange(firstCell, lastCell, isHighlight);
        }
    }

    // Method that occurs when a cell is clicked in Main Grid Area, the info is then delivered to
    // OptionsBarController to be printed out in the right textFields.
    public void updateSelectedCellInfo(String cellId, String OriginalValue, String lastUpdateVersion) {
        if (optionsBarComponentController != null) {
            optionsBarComponentController.updateCellInfo(cellId, OriginalValue, lastUpdateVersion);
            optionsBarComponentController.clearActionLineInput();
        }
    }

    public void updateCellValue(String cellId, String newValue) {
        String userName = this.userName ;
        String spreadsheetName = this.spreadsheetName;
        String finalUrl = ClientConstants.UPDATE_CELL_VALUE; // The endpoint for updating the cell

        // Create a form body to send the POST request with cell info
        RequestBody body = new FormBody.Builder()
                .add("userName", userName)
                .add("spreadsheetName", spreadsheetName)
                .add("cellId", cellId)
                .add("newValue", newValue)
                .build();


        // Execute the request asynchronously
        HttpClientUtil.runAsyncPost(finalUrl, body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("Received response code: " + response.code());

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // If the cell update is successful, now retrieve the updated spreadsheet data
                    Platform.runLater(() -> {
                        try {
                            setSpreadsheetData(spreadsheetName); // Retrieve the full spreadsheet data after the cell update
                            //showAlert(Alert.AlertType.INFORMATION, "Success", "Cell updated successfully and spreadsheet reloaded.");
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Error", "Error while reloading the spreadsheet: " + e.getMessage());
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        String errorMessage = String.valueOf(response);
                        showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
                    });
                }
            }
        });
    }


    // Method that occurs when we try to update a cell in Options Bar Controller.
//    // It updates the relevant cell with its new info
//    public void updateCellValue(String cellId, String newValue) {
//        try {
//
//            // Update the value in the engine
//            engine.updateCellValue(userName,spreadsheetName,cellId, newValue);
//            EngineDTO engineDTO = engine.getEngineData(userName, spreadsheetName);
//            int currentVersionNumber = engineDTO.getCurrentVersionNumber();
//            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();
//            CellDTO currentCellDTO = spreadsheetDTO.getCellById(cellId);
//
//            // Update the StringProperty for the cell ID
//            StringProperty cellProperty = mainGridAreaComponentController.getCellProperty(cellId);
//
//            if (cellProperty != null) {
//                cellProperty.set(currentCellDTO.getEffectiveValue().toString());
//                updateSelectedCellInfo(cellId, currentCellDTO.getOriginalValue(), Integer.toString(currentCellDTO.getLastUpdatedVersion()));
//                optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber); // Update the current version label
//
//                Object effectiveValue = currentCellDTO.getEffectiveValue();
//                String effectiveValueString = String.valueOf(effectiveValue);
//
//                if (effectiveValue instanceof Boolean) {
//                    effectiveValueString = effectiveValueString.toUpperCase();
//                }
//
//                cellProperty.set(effectiveValueString);
//                optionsBarComponentController.updateCellInfo(cellId, currentCellDTO.getOriginalValue(), Integer.toString(currentCellDTO.getLastUpdatedVersion()));
//            }
//
//            // Update all dependent cells
//            updateDependentCells(cellId,false);
//
//        } catch (Exception e) { // Catch any exceptions thrown during the update
//            // Show an error alert with the exception message
//            showAlert(Alert.AlertType.ERROR, "Error Updating Cell", e.getMessage());
//        }
//    }

    // Method to update all dependent cells
    private void updateDependentCells(String cellId, Boolean isDynamicAnalysis) {
        try {
            // Retrieve the map of dependent cells from the current cell
            Spreadsheet currentSpreadsheet = engine.getCurrentSpreadsheet(userName, this.spreadsheetName);
            Map<String, Cell> dependentCellsMap = currentSpreadsheet.getCellById(cellId).getDependsOnMe();

            if (dependentCellsMap != null) { // Check if there are any dependent cells
                for (Map.Entry<String, Cell> entry : dependentCellsMap.entrySet()) {
                    String dependentCellId = entry.getKey();
                    Cell dependentCell = currentSpreadsheet.getCellById(dependentCellId);
                    StringProperty dependentCellProperty = mainGridAreaComponentController.getCellProperty(dependentCellId);

                    if (dependentCellProperty != null && dependentCell != null) {
                        // If it's part of a dynamic analysis, update effective value
                        if (isDynamicAnalysis) {
                            dependentCell.setEffectiveValue();
                        }

                        Object effectiveValue = dependentCell.getEffectiveValue();
                        String effectiveValueString = String.valueOf(effectiveValue);
                        if (effectiveValue instanceof Boolean) {
                            effectiveValueString = effectiveValueString.toUpperCase();
                        }

                        dependentCellProperty.set(effectiveValueString); // Update the StringProperty for the dependent cell
                    }

                    updateDependentCells(dependentCellId, false);
                }
            }

        } catch (Exception e) {
            // If there's an error in updating dependent cells, display an alert
            showAlert(Alert.AlertType.ERROR, "Error Updating Dependent Cells", "Failed to update dependent cells: " + e.getMessage());
        }
    }

    public void addNewRange(String name, String firstCell, String lastCell) {
        String finalUrl = ClientConstants.ADD_RANGE;

        // Convert cell references to uppercase
        firstCell = firstCell.toUpperCase();
        lastCell = lastCell.toUpperCase();

        // Create a form body to send the POST request with cell info
        RequestBody requestBody = new FormBody.Builder()
                .add("userName", userName)
                .add("spreadsheetName", spreadsheetName)
                .add("rangeName", name)
                .add("firstCell", firstCell)
                .add("lastCell", lastCell)
                .build();

        // Create a new HttpRequest
        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody) // Use POST method for adding a new range
                .build();

        try {
            // Send the request synchronously using the shared OkHttpClient
            Response response = client.newCall(request).execute();
            // Check if the response is successful
            if (response.isSuccessful()) {
                // Parse the JSON response to extract the message
                String responseBody = response.body().string();
                // Update the UI
                leftSideComponentController.addRangeToUI(name, firstCell, lastCell);
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Success", "Range created successfully.");
            } else {
                // Here we should also parse the JSON response if the call fails
                String errorMessage = response.body().string();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Creating Range", errorMessage);
            }
        } catch (IOException e) {
            // Handle the failure
            String errorMessage = "Failed to connect to the server: " + e.getMessage();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Creating Range", errorMessage);
        }

    }

//    public void addNewRange(String name, String firstCell, String lastCell) {
//        try {
//            firstCell = firstCell.toUpperCase();
//            lastCell = lastCell.toUpperCase();
//            engine.addRange(userName, spreadsheetName, name, firstCell, lastCell); // Add range to the backend engine
//            leftSideComponentController.addRangeToUI(name, firstCell, lastCell); // Update the UI
//            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Success", "Range created successfully.");
//        } catch (Exception e) {
//            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Creating Range", e.getMessage());
//        }
//    }

    public void removeRange(String rangeName) {

        String finalUrl = HttpUrl
                .parse(ClientConstants.REMOVE_RANGE) // Use your constant URL
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .addQueryParameter("rangeName", rangeName)
                .build()
                .toString();

        // Create a new HttpRequest
        Request request = new Request.Builder()
                .url(finalUrl)
                .delete() // Use DELETE method
                .build();

        try {
            // Send the request synchronously using the shared OkHttpClient
            Response response = client.newCall(request).execute();

            // Check if the response is successful
            if (response.isSuccessful()) {
                // Parse the response if necessary
                String responseBody = response.body().string();
                List<RangeDTO> rangesDTO = getRanges();

                // Refresh the UI
                Platform.runLater(() -> {
                    // Refresh dependent UI elements
                    leftSideComponentController.refreshRanges(rangesDTO);  // Pass the rangesDTO to refreshRanges method
                });
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Success", "Range deleted successfully.");
            } else {
                // Handle the error response
                String errorMessage = response.body().string();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Deleting Range", errorMessage);
            }
        } catch (IOException e) {
            // Handle the failure
            String errorMessage = "Failed to connect to the server: " + e.getMessage();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Deleting Range", errorMessage);
        }
    }


//    public void deleteRange(String rangeName) {
//        try {
//            // Delete range from the backend engine
//            engine.removeRange(userName, spreadsheetName, rangeName);
//            leftSideComponentController.refreshRanges(); // Refresh the ranges in UI
//        } catch (Exception e) {
//            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Deleting Range", e.getMessage());
//        }
//    }

    public void handleSortRequest(String range, List<String> columnsToSortBy) {
        try {
            // Assuming you have a method in the engine to sort the spreadsheet
            Spreadsheet sortedSpreadsheet = new Spreadsheet(engine.getCurrentSpreadsheet(userName, spreadsheetName));
            Map<String,String> idMapping = engine.sortSpreadsheet(userName, spreadsheetName, sortedSpreadsheet, range, columnsToSortBy);

            // Step 2: Convert the sorted spreadsheet (domain model) to a SpreadsheetDTO
            SpreadsheetDTO sortedSpreadsheetDTO = engine.convertSpreadsheetToDTO(sortedSpreadsheet);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(SORT_DIALOG_FXML));
            Parent root = loader.load();
            sortDialogController = loader.getController(); // Get the controller after loading the FXML
            sortDialogController.setMainController(this); // Set the main controller

            // Now create a popup window to display the sorted results
            sortDialogController.showSortedResultsPopup(sortedSpreadsheetDTO, idMapping);

        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Sorting Error", "Failed to sort the spreadsheet: " + e.getMessage());
        }
    }


    public List<RangeDTO> getRanges() throws IOException {
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_RANGES) // Replace with the actual URL to your servlet/API endpoint
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .build()
                .toString();

        // Create a GET request to the backend server
        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        // Execute the request synchronously (can be done asynchronously with enqueue if desired)
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // Parse the response body as a JSON string
                String responseBody = response.body().string();
                Gson gson = new Gson();
                List<RangeDTO> rangesDTO = gson.fromJson(responseBody, new TypeToken<List<RangeDTO>>(){}.getType());
                return rangesDTO;

            } else {
                throw new IOException("Failed to get ranges: " + response.body().string());
            }
        }
    }

    // Method to get the current ranges from the backend engine
//    public Map<String, String[]> getRanges() throws UserNotFoundException, FileNotFoundException {
//        // Fetch the ranges from the backend engine
//        Map<String, Range> ranges = engine.getAllRanges(userName, spreadsheetName);
//        Map<String, String[]> formattedRanges = new HashMap<>();
//
//        // Convert each Range object to a String[] format
//        for (Map.Entry<String, Range> entry : ranges.entrySet()) {
//            Range range = entry.getValue();
//            String[] cells = {range.getStartCell(), range.getEndCell()};
//            formattedRanges.put(entry.getKey(), cells);
//        }
//
//        return formattedRanges;
//    }

//    public List<VersionDTO> getVersionsForMenu() {
//
//        EngineDTO engineDTO = engine.getEngineData(userName, spreadsheetName);
//        Map<Integer, VersionDTO> versionMap = engineDTO.getVersions();
//
//        // Convert the map values (VersionDTO) to a list and return it
//        return versionMap.values().stream().collect(Collectors.toList());
//    }

    public List<VersionDTO> getVersions() {
        // Build the URL for the GET request to retrieve versions
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_VERSIONS) // Use your constant URL
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .build()
                .toString();

        System.out.println("Sending request to: " + finalUrl);

        // Create the request using OkHttp
        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        try {
            // Send the request synchronously
            Response response = client.newCall(request).execute();

            // Check if the response is successful
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                // Parse the JSON response into List<VersionDTO>
                Gson gson = new Gson();
                List<VersionDTO> versionsDTO = gson.fromJson(responseBody, new TypeToken<List<VersionDTO>>() {}.getType()); // Deserialize JSON
                return versionsDTO; // Return the list of VersionDTO
            } else {
                String errorMessage = "Failed to load versions: " + response.body().string();
                showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
                return null; // Return null on error
            }
        } catch (IOException e) {
            // Handle the failure case
            String errorMessage = "Failed to connect to the server: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
            return null; // Return null on failure
        }
    }

    public boolean isSpreadsheetLoaded() {

        String finalUrl = HttpUrl
                .parse(ClientConstants.IS_SPREADSHEET_LOADED) // Use your constant URL
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .build()
                .toString();

        // Create the request using OkHttp
        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        try {
            // Send the request synchronously using the shared OkHttpClient
            Response response = client.newCall(request).execute();

            // Check if the response is successful
            if (response.isSuccessful()) {
                String responseBody = response.body().string().trim();
                return Boolean.parseBoolean(responseBody); // Parse boolean from response string
            } else {
                String errorMessage = "Failed to load spreadsheet: " + response.body().string();
                showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
                return false;
            }
        } catch (IOException e) {
            String errorMessage = "Failed to load spreadsheet: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
            return false;
        }
    }


//    public boolean isSpreadsheetLoaded() throws UserNotFoundException, FileNotFoundException {
//        return engine.getCurrentSpreadsheet(userName, spreadsheetName) != null;
//    }

    public void setSkin(String theme) {
        Scene scene = scrollPane.getScene();

        if (scene != null) {
            // Clear existing stylesheets
            scene.getStylesheets().clear();
            Skin skin;
            try {
                skin = Skin.valueOf(theme.toUpperCase());
            } catch (IllegalArgumentException e) {
                skin = Skin.DEFAULT; // Fallback to default skin if skin isnt selected
            }

            String[] components = {"TopGridWindow.css", "OptionsBar.css", "LeftSide.css", "MainGridArea.css", "GridWindow.css","Back.css"};
            for (String component : components) {
                String cssPath = String.format("/gridwindow/styles/%s/%s", skin.getDirectoryName(), component);
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
        }
    }

    public void setAnimation(String animation) {
        Animation animationEnum = Animation.valueOf(animation.toUpperCase());
        Scene scene = scrollPane.getScene();

        if (scene != null) {
            Pane rootPane = (Pane) scene.lookup("#borderPane");

            if (rootPane != null) {
                stopAnimations();
                if (animationEnum != Animation.NONE) {
                    Platform.runLater(() -> {
                        switch (animationEnum) {
                            case FADE:
                                applyFadeTransition(rootPane);
                                break;
                            case ROTATE:
                                applyRotateTransition(rootPane);
                                break;
                            default:
                                break;
                        }
                    });
                }
            }
        }
    }


    private void stopAnimations() {
        if(!activeFadeTransitions.isEmpty()) {
            for (FadeTransition fadeTransition : activeFadeTransitions) {
                fadeTransition.stop();  // Stop each active fade transition

                // Restore the node's opacity to its initial value
                Node node = fadeTransition.getNode();
                if (node != null) {
                    node.setOpacity(1.0);  // Reset to fully opaque
                }
            }
            activeFadeTransitions.clear();  // Clear the list after stopping
        }

        // Stop all rotate transitions
        if (!activeRotateTransitions.isEmpty()) {
            for (RotateTransition rotateTransition : activeRotateTransitions) {
                rotateTransition.stop();
                Node node = rotateTransition.getNode();
                if (node != null) {
                    node.setRotate(0);  // Reset rotation
                }
            }
            activeRotateTransitions.clear();
        }
    }

    // Method to apply fade transitions to nodes
    public void applyFadeTransition(Pane rootPane) {
        for (Node node : rootPane.getChildren()) {
            if (node instanceof Label || node instanceof Text || node instanceof Button || node instanceof MenuButton || node instanceof TextField) {
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), node);  // Create a new transition for each node
                fadeTransition.setFromValue(1.0);
                fadeTransition.setToValue(0.1);
                fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
                fadeTransition.setAutoReverse(true);
                fadeTransition.play();

                activeFadeTransitions.add(fadeTransition);  // Keep track of all transitions
            } else if (node instanceof Pane) { // Handle Pane and its children
                applyFadeTransition((Pane) node);  // Recursively apply transitions to child nodes
            } else if (node instanceof SplitPane) { // Handle SplitPane and its items
                SplitPane splitPane = (SplitPane) node;
                for (Node splitPaneItem : splitPane.getItems()) {
                    if (splitPaneItem instanceof Pane) {
                        applyFadeTransition((Pane) splitPaneItem);
                    }
                }
            }
        }
    }

    private void applyRotateTransition(Pane rootPane) {
        for (Node node : rootPane.getChildren()) {
            if (node instanceof Label || node instanceof Text || node instanceof Button || node instanceof MenuButton || node instanceof TextField) {
                RotateTransition rotateTransition = new RotateTransition(Duration.millis(5000), node);
                rotateTransition.setFromAngle(0);
                rotateTransition.setToAngle(360);
                rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
                rotateTransition.setInterpolator(javafx.animation.Interpolator.LINEAR);
                rotateTransition.play();

                activeRotateTransitions.add(rotateTransition);
            } else if (node instanceof Pane) {
                applyRotateTransition((Pane) node);
            } else if (node instanceof SplitPane) {
                SplitPane splitPane = (SplitPane) node;
                for (Node splitPaneItem : splitPane.getItems()) {
                    if (splitPaneItem instanceof Pane) {
                        applyRotateTransition((Pane) splitPaneItem);
                    } else {
                        applyRotateTransition((Pane)splitPaneItem);
                    }
                }
            }
        }
    }

    public Cell getCellById(String cellId) {
        // Build the URL for the GET request to retrieve cell data
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_CELL_BY_ID)
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .addQueryParameter("cellId", cellId)
                .build()
                .toString();


        // Create the request using OkHttp
        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        try {
            // Send the request synchronously using the shared OkHttpClient
            Response response = client.newCall(request).execute();

            // Check if the response is successful
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                // Parse the JSON response into Cell
                Gson gson = new Gson();
                Cell cell = gson.fromJson(responseBody, Cell.class); // Deserialize JSON to Cell
                return cell; // Return the Cell object
            } else {
                String errorMessage = "Failed to load cell data: " + response.body().string();
                showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
                return null; // Return null on error
            }
        } catch (IOException e) {
            // Handle the failure
            String errorMessage = "Failed to connect to the server: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
            return null; // Return null on failure
        }
    }


    public CellDTO getCellDTOById(String cellId) {
        // Build the URL for the GET request to retrieve cell data
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_CELLDTO_BY_ID)
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .addQueryParameter("cellId", cellId)
                .build()
                .toString();


        // Create the request using OkHttp
        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        try {
            // Send the request synchronously
            Response response = client.newCall(request).execute();

            // Check if the response is successful
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                // Parse the JSON response into CellDTO
                Gson gson = new Gson();
                CellDTO cell = gson.fromJson(responseBody, CellDTO.class); // Deserialize JSON to CellDTO
                return cell; // Return the CellDTO object
            } else {
                String errorMessage = "Failed to load cell data: " + response.body().string();
                showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
                return null; // Return null on error
            }
        } catch (IOException e) {
            // Handle the failure
            String errorMessage = "Failed to connect to the server: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
            return null; // Return null on failure
        }
    }

//    public Cell getCellById(String cellId) {
//        return engine.getCurrentSpreadsheet(userName, spreadsheetName).getCellById(cellId);
//    }

    public SpreadsheetDTO getSpreadsheetByVersion(int versionNumber) {
        // Build the URL for the GET request to retrieve spreadsheet data
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_SPREADSHEET_BY_VERSION) // Use your constant URL
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .addQueryParameter("versionNumber", String.valueOf(versionNumber))
                .build()
                .toString();


        // Create the request using OkHttp
        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        try {
            // Send the request synchronously
            Response response = client.newCall(request).execute();

            // Check if the response is successful
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                // Parse the JSON response into SpreadsheetDTO
                Gson gson = new Gson();
                SpreadsheetDTO spreadsheetDTO = gson.fromJson(responseBody, SpreadsheetDTO.class); // Deserialize JSON
                return spreadsheetDTO; // Return the SpreadsheetDTO object
            } else {
                String errorMessage = "Failed to load spreadsheet: " + response.body().string();
                showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
                return null; // Return null on error
            }
        } catch (IOException e) {
            // Handle the failure case
            String errorMessage = "Failed to connect to the server: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
            return null; // Return null on failure
        }
    }


//    public Spreadsheet getSpreadsheetByVersion(int versionNumber) throws UserNotFoundException, FileNotFoundException {
//        return engine.getSpreadsheetByVersion(userName, spreadsheetName, versionNumber);
//    }


    public List<String> getCurrentColumns() throws UserNotFoundException, FileNotFoundException {
        Spreadsheet currentSpreadsheet = engine.getCurrentSpreadsheet(userName, spreadsheetName);
        if (currentSpreadsheet == null) {
            return new ArrayList<>(); // Return an empty list if no spreadsheet is loaded
        }

        int columnCount = currentSpreadsheet.getColumns(); // Assuming this returns the total number of columns
        List<String> columnNames = new ArrayList<>();

        for (int i = 0; i < columnCount; i++) {
            columnNames.add(getColumnName(i)); // Convert index to column name
        }

        return columnNames;
    }

    // Helper method to convert a zero-based column index to an Excel-style column name (A, B, C, ..., Z, AA, AB, ...)
    public String getColumnName(int index) throws UserNotFoundException, SpreadsheetNotFoundException {
        return engine.getColumnName(userName, fileName, index);
    }

    public Spreadsheet getCurrentSpreadsheet() {
        return engine.getCurrentSpreadsheet(userName, spreadsheetName);
    }

    public List<String[][]> filterTableMultipleColumns(String tableArea, Map<String, List<String>> selectedColumnValues) throws UserNotFoundException, SpreadsheetNotFoundException {
        return engine.filterTableMultipleColumns(userName, fileName, tableArea, selectedColumnValues);
    }

    // Helper method to convert a column letter (e.g., "A") to a zero-based index
    public int getColumnIndex(String columnName) throws UserNotFoundException, SpreadsheetNotFoundException {
        return engine.getColumnIndex(userName, fileName, columnName);
    }

    public void updateDependentCellsForDynamicAnalysis(String cellId, double tempValue) {
        try {
            // Get the current spreadsheet
            Spreadsheet currentSpreadsheet = getCurrentSpreadsheet();

            if (currentSpreadsheet == null) {
                return;
            }

            // Temporarily update the value of the target cell in the spreadsheet's backend
            Cell cell = currentSpreadsheet.getCellById(cellId);

            if (cell != null) {
                cell.setExpression(new LiteralExpression(tempValue));
                cell.setEffectiveValue();
            }

            // Get the sorted list of cells to update using topological sort
            List<String> sortedCells = currentSpreadsheet.topologicalSort();

            // Update each cell in the topological order
            for (String sortedCellId : sortedCells) {
                Cell dependentCell = currentSpreadsheet.getCellById(sortedCellId);
                StringProperty dependentCellProperty = mainGridAreaComponentController.getCellProperty(sortedCellId);

                if (dependentCellProperty != null && dependentCell != null) {
                    dependentCell.setEffectiveValue();
                    Object effectiveValue = dependentCell.getEffectiveValue();
                    String effectiveValueString;

                    // Format the number with thousands separators and up to two decimal places
                    if (effectiveValue instanceof Number) {
                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                        numberFormat.setMinimumFractionDigits(0);  // No minimum decimal digits
                        numberFormat.setMaximumFractionDigits(2);  // Maximum 2 decimal digits
                        effectiveValueString = numberFormat.format(effectiveValue);
                    } else {
                        effectiveValueString = String.valueOf(effectiveValue);
                    }

                    dependentCellProperty.set(effectiveValueString); // Update the StringProperty for the dependent cell
                }
            }

        } catch (Exception e) {
            // Handle any errors during dynamic analysis
            showAlert(Alert.AlertType.ERROR, "Error Performing Dynamic Analysis", e.getMessage());
        }
    }

    // Method to get the StringProperty of a cell by its ID
    public StringProperty getCellProperty(String cellId) {
        return mainGridAreaComponentController.getCellProperty(cellId);
    }

    public void showDynamicAnalysisDialog(String cellId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(DYNAMIC_ANALYSIS_FXML));
            Parent root = loader.load();

            // Get the controller from the FXML loader
            dynamicAnalysisComponentController = loader.getController();

            // Set the main controller
            dynamicAnalysisComponentController.setMainController(this);

            // Pass the cell ID to the dialog
            dynamicAnalysisComponentController.openDynamicAnalysisDialog(cellId);
        } catch (IOException e) {
            showError("Failed to open dynamic analysis dialog: " + e.getMessage());
        }
    }


=======
    public List<String> getRangeNames() throws IOException {
        // Call the getRanges() method to retrieve the list of RangeDTOs
        List<RangeDTO> rangesDTO = getRanges();
  
        // Extract range names and return them as a list
        List<String> rangeNames = new ArrayList<>();
        for (RangeDTO range : rangesDTO) {
            rangeNames.add(range.getName());
        }

        return rangeNames;
    }

//    public List<String> getRangeNames() throws UserNotFoundException, FileNotFoundException {
//        // Fetch all ranges from the backend engine
//        Map<String, Range> ranges = engine.getAllRanges(userName, spreadsheetName);
//
//        // Extract range names and return them as a list
//        return new ArrayList<>(ranges.keySet());
//    }

    public Map<String, String> getCellAlignments() {
        return mainGridAreaComponentController.getCellAlignments();
    }

    public Map<String, String> getCellBackgroundColors() {
        return mainGridAreaComponentController.getCellBackgroundColors();
    }
    public Map<String, String> getCellTextColors() {
        return mainGridAreaComponentController.getCellTextColors();
    }

    public Map<String, TextField> getTextFieldMap() {
        return mainGridAreaComponentController.getTextFieldMap();
    }

//    public EngineDTO getEngine() {
//        return engine.getEngineData(userName, spreadsheetName);
//    }



    public void checkForCircularReferences(String cellId, Expression newExpression) throws CircularReferenceException, UserNotFoundException, FileNotFoundException {
        engine.checkForCircularReferences(userName, spreadsheetName, cellId, newExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridWindowController that = (GridWindowController) o;
        return Objects.equals(engine, that.engine) && Objects.equals(scrollPane, that.scrollPane)
                && Objects.equals(optionsBarComponentController, that.optionsBarComponentController)
                && Objects.equals(leftSideComponentController, that.leftSideComponentController)
                && Objects.equals(mainGridAreaComponentController, that.mainGridAreaComponentController)
                && Objects.equals(sortDialogController, that.sortDialogController)
                && Objects.equals(addRangeDialogController, that.addRangeDialogController)
                && Objects.equals(dynamicAnalysisComponentController, that.dynamicAnalysisComponentController)
                && Objects.equals(topGridWindowComponentController, that.topGridWindowComponentController);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engine, scrollPane,
                optionsBarComponentController, leftSideComponentController,
                mainGridAreaComponentController, sortDialogController, addRangeDialogController,
                dynamicAnalysisComponentController, topGridWindowComponentController);
    }


    public Expression parseExpression (String input) throws InvalidExpressionException, UserNotFoundException, FileNotFoundException {
        return engine.parseExpression(userName, spreadsheetName, input);
    }


    public void setUserName(String userName) {
        this.userName = userName;
        topGridWindowComponentController.setUsername(userName);
    }
//
//    public void setEngine(Engine engine) {
//        this.engine = engine;
//    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

}