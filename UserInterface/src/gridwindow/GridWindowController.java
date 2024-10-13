package gridwindow;

import api.Expression;
import api.Function;
import cells.Cell;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import dto.*;
import exceptions.*;
import expressionimpls.FunctionExpression;
import expressionimpls.LiteralExpression;
import expressionimpls.RangeExpression;
import expressionimpls.ReferenceExpression;
import functionsimpl.FunctionFactory;
import gridwindow.bottom.BackController;
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
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import ranges.RangeImpl;
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
import utils.SimpleCookieManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import static utils.AlertUtils.showAlert;
import static utils.AlertUtils.showError;
import static utils.CommonResourcesPaths.*;

public class GridWindowController {

    private List<FadeTransition> activeFadeTransitions = new ArrayList<>();  // List to store all active transitions
    private List<RotateTransition> activeRotateTransitions = new ArrayList<>();  // List to store all active transitions
    private String spreadsheetName; // Set by the menu window controller
    private String userName;        // Set by the menu window controller
    private OkHttpClient client;    // Set by the menu window controller
    private SimpleCookieManager cookieManager;  // Set by the menu window controller
    private Stage stage;                        // The main window (same stage for both views)
    private Parent menuRoot;                    // The root node of the main menu
    private boolean spreadsheetModified = false;// Track if the user has modified the spreadsheet
    private SpreadsheetVersionRefresher spreadsheetVersionRefresher;       // The version refresher fot the simultaneous updates

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
    private BackController backComponentController;

    @FXML
    public void initialize() {
        if(topGridWindowComponentController != null){
            topGridWindowComponentController.setMainController(this);
        }
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
        if (backComponentController != null) {
            backComponentController.setMainController(this);
        }
        if(spreadsheetVersionRefresher != null){
            spreadsheetVersionRefresher = new SpreadsheetVersionRefresher(this);
        }
    }

    public void setName(String name) {
        if (topGridWindowComponentController != null) {
            topGridWindowComponentController.setUsername(name);
        }
    }

    public String getSpreadsheetName() {
        return spreadsheetName;
    }

    public void setCookieManager(SimpleCookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        topGridWindowComponentController.setUsername(userName);
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public String getUserName() {return userName;}

    // Setter to pass the stage and menu root from the main controller
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setMenuRoot(Parent menuRoot) {
        this.menuRoot = menuRoot;
    }

    public boolean isSpreadsheetModified() {
        return spreadsheetModified;
    }

    public void setSpreadsheetModified(boolean modified) {
        this.spreadsheetModified = modified;
        if (modified) {
            stopVersionRefresher(); // Stop refreshing if the spreadsheet is modified
        }
    }
    public void stopVersionRefresher() {
        if (spreadsheetVersionRefresher != null) {
            spreadsheetVersionRefresher.stopRefreshing();
            spreadsheetVersionRefresher = null;  // Clear the reference
        }
    }

    public void showNewVersionAvailable() {
        // Show the label and button in the UI
        topGridWindowComponentController.setNewVersionVisiblity(true);
    }

    // Method to hide the grid and show the main menu in the same stage
    public void hideMainGridAndShowMenu() {
        // Switch back to the menu window
        Scene scene = stage.getScene();
        scene.setRoot(menuRoot);  // Change the root back to the menu view
        stage.setTitle("Menu Window for "+ userName);  // Reset the stage title
        stage.setWidth(1100);
        stage.setHeight(800);
        stage.show();
    }

    public void setSpreadsheetData(String spreadsheetName){
        this.spreadsheetName = spreadsheetName;

        // Build the URL for the GET request to retrieve engine data
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_ENGINE_DATA)
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .build()
                .toString();

        // Send the GET request asynchronously
        HttpClientUtil.runAsyncGet(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();

                        // Parse the JSON response into EngineDTO
                        Gson gson = new Gson();
                        EngineDTO engineDTO = gson.fromJson(responseBody, EngineDTO.class);

                        // Now, use the data as before
                        int currentVersionNumber = engineDTO.getCurrentVersionNumber();
                        SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();
                        List<RangeDTO> rangesDTO = engineDTO.getRanges();

                        // Update the UI
                        Platform.runLater(() -> {
                            topGridWindowComponentController.startVersionRefresher();
                            mainGridAreaComponentController.clearGrid();
                            optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber);
                            mainGridAreaComponentController.start(spreadsheetDTO, false);
                            topGridWindowComponentController.setNewVersionVisiblity(false); // Hide the new version label
                            leftSideComponentController.refreshRanges(rangesDTO);
                        });
                    } else {
                        Platform.runLater(() -> {
                            String errorMessage = String.valueOf(response);
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load engine data: " + errorMessage);
                        });
                    }
                } finally {
                    response.close(); // Ensure the response body is closed
                }
            }
        });
    }

    public void highlightRange(String firstCell, String lastCell, boolean isHighlight) {
        // Delegate to the MainGridAreaController
        if (mainGridAreaComponentController != null) {
            mainGridAreaComponentController.highlightRange(firstCell, lastCell, isHighlight);
        }
    }

    // Method that occurs when a cell is clicked in Main Grid Area, the info is then delivered to
    // OptionsBarController to be printed out in the right textFields.
    public void updateSelectedCellInfo(String cellId, String OriginalValue, String lastUpdateVersion, String lastUpdatedBy) {
        if (optionsBarComponentController != null) {
            optionsBarComponentController.updateCellInfo(cellId, OriginalValue, lastUpdateVersion, lastUpdatedBy);
            optionsBarComponentController.clearActionLineInput();
        }
    }

    public void updateCellValue(String cellId, String newValue, Boolean isDynamicAnalysis) {
        if (topGridWindowComponentController.isNewVersionVisible()) { // Check if a new version is available - can't update cell until new version is loaded
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot update cell value when a new version is available.");
            return;
        }
        String userName = this.userName ;
        String spreadsheetName = this.spreadsheetName;
        String finalUrl = ClientConstants.UPDATE_CELL_VALUE; // The endpoint for updating the cell

        // Create a form body to send the POST request with cell info
        RequestBody body = new FormBody.Builder()
                .add("userName", userName)
                .add("spreadsheetName", spreadsheetName)
                .add("cellId", cellId)
                .add("newValue", newValue)
                .add("isDynamicAnalysis", isDynamicAnalysis.toString())
                .build();

        // Execute the request asynchronously
        HttpClientUtil.runAsyncPost(finalUrl, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    System.out.println("Received response code: " + response.code());
                    String responseBody = response.body().string(); // Read the response body

                    if (response.isSuccessful()) {
                        // If the cell update is successful, now retrieve the updated spreadsheet data
                        Platform.runLater(() -> {
                            try {
                                setSpreadsheetModified(true);  // Mark the spreadsheet as modified by the user
                                setSpreadsheetData(spreadsheetName); // Retrieve the full spreadsheet data after the cell update
                                //showAlert(Alert.AlertType.INFORMATION, "Success", "Cell updated successfully and spreadsheet reloaded.");
                            } catch (Exception e) {
                                System.out.println("Error while reloading the spreadsheet: " + e.getMessage());
                                showAlert(Alert.AlertType.ERROR, "Error", "Error while reloading the spreadsheet: " + e.getMessage());
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            String errorMessage = responseBody;
                            System.out.println("Error message: " + errorMessage);
                            showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
                        });
                    }
                } finally {
                    response.close(); // Ensure the response body is closed
                }
            }
        });
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
                String errorMessage = response.body().string();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Creating Range", errorMessage);
            }
        } catch (IOException e) {
            // Handle the failure
            String errorMessage = "Failed to connect to the server: " + e.getMessage();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Creating Range", errorMessage);
        }

    }

    public void removeRange(String rangeName) {

        String finalUrl = HttpUrl
                .parse(ClientConstants.REMOVE_RANGE)
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .addQueryParameter("rangeName", rangeName)
                .build()
                .toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .delete() // Use DELETE method
                .build();

        try {
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

    public void handleSortRequest(String range, List<String> columnsToSortBy) {
        String finalUrl = ClientConstants.SORT_SPREADSHEET;
        Gson gson = new Gson();

        // Create a FormBody builder
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("userName", userName) // Ensure userName is initialized
                .add("spreadsheetName", spreadsheetName) // Ensure spreadsheetName is initialized
                .add("range", range);

        // Add each column to the form body as a separate parameter
        for (String column : columnsToSortBy) {
            formBuilder.add("columnsToSortBy", column); // Each column is added as a separate parameter
        }

        // Build the request body
        RequestBody requestBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody) // Use POST method
                .build();

        try {
            Response response = client.newCall(request).execute();

            // Check if the response is successful
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                // Parse the JSON response
                Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);
                SpreadsheetDTO sortedSpreadsheetDTO = gson.fromJson(gson.toJson(responseMap.get("sortedSpreadsheet")), SpreadsheetDTO.class);
                Map<String, String> idMapping = gson.fromJson(gson.toJson(responseMap.get("idMapping")), new TypeToken<Map<String, String>>(){}.getType());

                // Display the sorted results in the UI
                FXMLLoader loader = new FXMLLoader(getClass().getResource(SORT_DIALOG_FXML));
                Parent root = loader.load();
                sortDialogController = loader.getController(); // Get the controller after loading the FXML
                sortDialogController.setMainController(this); // Set the main controller
                // Show the sorted results popup
                sortDialogController.showSortedResultsPopup(sortedSpreadsheetDTO, idMapping); // Ensure you have idMapping available here
            } else {
                String errorMessage = response.body().string();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Sorting Error", errorMessage);
            }
        } catch (IOException e) {
            String errorMessage = "Failed to connect to the server: " + e.getMessage();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Sorting Error", errorMessage);
        } catch (JsonSyntaxException e) {
            String errorMessage = "Failed to parse the response: " + e.getMessage();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Parsing Error", errorMessage);
        }
    }

    public List<RangeDTO> getRanges() throws IOException {
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_RANGES)
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .build()
                .toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        try  {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                // Parse the response body as a JSON string
                String responseBody = response.body().string();
                Gson gson = new Gson();
                List<RangeDTO> rangesDTO = gson.fromJson(responseBody, new TypeToken<List<RangeDTO>>(){}.getType());
                return rangesDTO;

            } else {
                throw new IOException("Failed to get ranges: " + response.body().string());
            }
        } catch (IOException e){
            throw new IOException("Failed to connect to the server: " + e.getMessage());
        }
    }

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

        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        try {
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

    public void setSkin(String theme) {
        Scene scene = scrollPane.getScene();

        if (scene != null) {
            // Clear existing stylesheets
            scene.getStylesheets().clear();
            Skin skin;
            try {
                skin = Skin.valueOf(theme.toUpperCase());
            } catch (IllegalArgumentException e) {
                skin = Skin.DEFAULT; // Fallback to default skin if skin isn't selected
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

        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        try {
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

    public  SpreadsheetDTO getSpreadsheetByVersion(int versionNumber) {
        // Build the URL for the GET request to retrieve spreadsheet data
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_SPREADSHEET_BY_VERSION) // Use your constant URL
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .addQueryParameter("versionNumber", String.valueOf(versionNumber))
                .build()
                .toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .get() // GET request
                .build();

        try {
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

    public  List<String> getCurrentColumns() throws UserNotFoundException, SpreadsheetNotFoundException, IOException {
        SpreadsheetDTO currentSpreadsheet = getCurrentSpreadsheetDTO();

        if (currentSpreadsheet == null) {
            return new ArrayList<>(); // Return an empty list if no spreadsheet is loaded
        }

        int columnCount = currentSpreadsheet.getColumns();
        List<String> columnNames = new ArrayList<>();

        for (int i = 0; i < columnCount; i++) {
            columnNames.add(getColumnName(i)); // Convert index to column name
        }

        return columnNames;
    }

    public String getColumnName(int index) throws IOException, UserNotFoundException, SpreadsheetNotFoundException {
        // Build the URL with query parameters
        HttpUrl url = HttpUrl.parse(ClientConstants.GET_COLUMN_NAME)
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .addQueryParameter("index", Integer.toString(index))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get() // Use GET method
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                String errorMessage = response.body().string();
                if (response.code() == 404) {
                    throw new UserNotFoundException("User or Spreadsheet not found: " + errorMessage);
                }
                throw new SpreadsheetNotFoundException("Error fetching column name: " + errorMessage);
            }

            // If the response is successful, get the column name from the response body
            String responseBody = response.body().string();
            return responseBody.trim(); // Return the column name as a string

        } catch (IOException e) {
            throw new IOException("Error occurred while fetching the column name", e);
        }
    }

    public SpreadsheetDTO getCurrentSpreadsheetDTO() {
        String finalUrl = HttpUrl
                .parse(ClientConstants.GET_SPREADSHEET) // Use your constant URL
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
                String responseBody = response.body().string();

                // Use Gson to parse the JSON response to SpreadsheetDTO
                Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Enable pretty printing
                setSpreadsheetModified(false);
                return gson.fromJson(responseBody, SpreadsheetDTO.class);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load spreadsheet: " + response.message());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to parse the response: " + e.getMessage());
        }

        return null; // Return null or handle accordingly if there's an error
    }

    // Method to send a request to the server for filtering table with multiple columns
    public List<String[][]> filterTableMultipleColumns(String tableArea, Map<String, List<String>> selectedColumnValues) throws IOException, UserNotFoundException, SpreadsheetNotFoundException {
        String url = ClientConstants.FILTER_TABLE_MULTIPLE_COLUMNS;

        // Create the form body by iterating over the map
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("userName", userName)
                .add("spreadsheetName", spreadsheetName)
                .add("tableArea", tableArea);

        // Add each column and its selected values to the request body
        for (Map.Entry<String, List<String>> entry : selectedColumnValues.entrySet()) {
            String columnName = entry.getKey();
            for (String value : entry.getValue()) {
                formBuilder.add("selectedColumn_" + columnName, value); // Prefix the column name with a key to identify it
            }
        }

        RequestBody requestBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody) // POST request
                .build();

        try  {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorMessage = response.body().string();
                if (response.code() == 404) {
                    throw new UserNotFoundException("User or Spreadsheet not found: " + errorMessage);
                }
                throw new SpreadsheetNotFoundException("Error fetching filtered table: " + errorMessage);
            }

            // Parse the response body into the expected List<String[][]> format
            String responseBody = response.body().string();
            Type listType = new TypeToken<List<String[][]>>(){}.getType();  // Define the expected return type
            return new Gson().fromJson(responseBody, listType); // Parse response as needed
        }catch(IOException e){
            throw new IOException("Error occurred while fetching the filtered table", e);
        }
    }

    public int getColumnIndex(String columnName) throws IOException, UserNotFoundException, SpreadsheetNotFoundException {
        // Build the URL with query parameters
        HttpUrl url = HttpUrl.parse(ClientConstants.GET_COLUMN_INDEX)
                .newBuilder()
                .addQueryParameter("userName", userName)
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .addQueryParameter("columnName", columnName)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get() // Use GET request
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorMessage = response.body().string();
                if (response.code() == 404) {
                    throw new UserNotFoundException("User or Spreadsheet not found: " + errorMessage);
                }
                throw new SpreadsheetNotFoundException("Error fetching column index: " + errorMessage);
            }

            // If the response is successful, get the column index from the response body
            String responseBody = response.body().string();
            return Integer.parseInt(responseBody.trim()); // Parse and return the column index as an integer

        } catch (IOException e) {
            throw new IOException("Error occurred while fetching the column index", e);
        }
    }

//    public void checkForCircularReferences(String cellId, Expression newExpression) throws IOException {
//        String userName = this.userName;
//        String spreadsheetName = this.spreadsheetName;
//        String finalUrl = ClientConstants.CHECK_CIRCULAR_REFERENCES; // The endpoint for checking circular references
//
//        // Build the URL for the GET request
//        String urlWithParams = HttpUrl.parse(finalUrl)
//                .newBuilder()
//                .addQueryParameter("userName", userName)
//                .addQueryParameter("spreadsheetName", spreadsheetName)
//                .addQueryParameter("cellId", cellId)
//                .addQueryParameter("newExpression", newExpression.toString()) // Ensure to serialize this properly
//                .build()
//                .toString();
//
//        // Create the OkHttpClient instance
//        OkHttpClient client = new OkHttpClient();
//
//        // Create the request
//        Request request = new Request.Builder()
//                .url(urlWithParams)
//                .build();
//
//        // Execute the request synchronously
//        try {
//            Response response = client.newCall(request).execute();
//            if (response.isSuccessful()) {
//                // No circular references found
//                Platform.runLater(() -> {
//                    System.out.println("No circular references found.");
//                    //showAlert(Alert.AlertType.INFORMATION, "Success", "No circular references found.");
//                });
//            } else {
//                // Handle circular reference found
//                Platform.runLater(() -> {
//                    System.out.println("Circular reference detected: " + response.message());
//                    String errorMessage = response.message();
//                    showAlert(Alert.AlertType.ERROR, "Error", "Circular reference detected: " + errorMessage);
//                });
//            }
//        } catch (IOException e) {
//            // Handle exceptions
//            System.out.println("Failed to connect to the server: " + e.getMessage());
//            Platform.runLater(() -> {
//                showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
//            });
//        }
//    }

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
            dynamicAnalysisComponentController.openDynamicAnalysisDialog();
        } catch (IOException e) {
            showError("Failed to open dynamic analysis dialog: " + e.getMessage());
        }
    }

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

    public void disableEditButtons() {
        if (optionsBarComponentController != null) {
            optionsBarComponentController.disableEditButtons();
        }
        if (leftSideComponentController != null) {
            leftSideComponentController.disableEditButtons();
        }
    }

    // Helper method to check if dynamic analysis can be done on a cell
    public void canDynamicAnalysisBeDone(String cellId) throws IOException {
        String finalUrl = ClientConstants.CAN_DYNAMIC_ANALYSIS_BE_DONE; // Replace with your actual URL
        RequestBody body = new FormBody.Builder()
                .add("spreadsheetName", spreadsheetName)
                .add("cellId", cellId)
                .build();

        HttpClientUtil.runAsyncPost(finalUrl, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to connect to the server. Please try again."));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Gson gson = new Gson();
                    Map<String, String> result = gson.fromJson(jsonResponse, Map.class);

                    Platform.runLater(() -> {
                        if ("ERROR".equals(result.get("status"))) {
                            showError(result.get("message"));  // Show error from server
                        } else if ("SUCCESS".equals(result.get("status"))) {
                            return;
                        }
                    });
                } else {
                    Platform.runLater(() -> showError("Failed to retrieve analysis status: " + response.message()));
                }
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridWindowController that = (GridWindowController) o;
        return spreadsheetModified == that.spreadsheetModified && Objects.equals(activeFadeTransitions, that.activeFadeTransitions)
                && Objects.equals(activeRotateTransitions, that.activeRotateTransitions) && Objects.equals(spreadsheetName, that.spreadsheetName)
                && Objects.equals(userName, that.userName) && Objects.equals(client, that.client) && Objects.equals(cookieManager, that.cookieManager)
                && Objects.equals(stage, that.stage) && Objects.equals(menuRoot, that.menuRoot) && Objects.equals(spreadsheetVersionRefresher, that.spreadsheetVersionRefresher)
                && Objects.equals(scrollPane, that.scrollPane) && Objects.equals(topGridWindowComponentController, that.topGridWindowComponentController)
                && Objects.equals(optionsBarComponentController, that.optionsBarComponentController) && Objects.equals(leftSideComponentController, that.leftSideComponentController)
                && Objects.equals(mainGridAreaComponentController, that.mainGridAreaComponentController)
                && Objects.equals(dynamicAnalysisComponentController, that.dynamicAnalysisComponentController) && Objects.equals(sortDialogController, that.sortDialogController)
                && Objects.equals(addRangeDialogController, that.addRangeDialogController) && Objects.equals(backComponentController, that.backComponentController);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeFadeTransitions, activeRotateTransitions, spreadsheetName, userName, client, cookieManager, stage,
                menuRoot, spreadsheetModified, spreadsheetVersionRefresher, scrollPane, topGridWindowComponentController, optionsBarComponentController,
                leftSideComponentController, mainGridAreaComponentController, dynamicAnalysisComponentController, sortDialogController, addRangeDialogController, backComponentController);
    }
}