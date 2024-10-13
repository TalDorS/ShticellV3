package menuwindow;

import com.google.gson.Gson;
import enums.PermissionType;
import gridwindow.GridWindowController;
import gridwindow.top.Skin;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import menuwindow.center.permissionstable.PermissionsTableController;
import menuwindow.center.permissionstable.PermissionsTableRefresher;
import menuwindow.center.sheettable.AvailableSheetTableController;
import menuwindow.rightside.RightSideController;
import menuwindow.top.HeaderLoadController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.ClientConstants;
import utils.HttpClientUtil;
import utils.SimpleCookieManager;

import java.io.IOException;
import java.util.*;

import static utils.AlertUtils.showAlert;
import static utils.AlertUtils.showError;
import static utils.CommonResourcesPaths.GRID_WINDOW_FXML;

public class MenuWindowController {
    private Timer permissionsTableTimer;
    private Stage stage; // To hold the stage reference
    private OkHttpClient client;
    private SimpleCookieManager cookieManager;
    private String lastSelectedSpreadsheetName;

    @FXML
    private HeaderLoadController headerLoadComponentController;

    @FXML
    private RightSideController rightSideComponentController;

    @FXML
    private AvailableSheetTableController availableSheetTableComponentController;

    @FXML
    private PermissionsTableController permissionsTableComponentController;

    GridWindowController gridWindowController;

    @FXML
    public void initialize() {

        if (headerLoadComponentController != null) {
            headerLoadComponentController.setMainController(this);
        }
        if( rightSideComponentController != null){
            rightSideComponentController.setMainController(this);
        }
        if(availableSheetTableComponentController != null){
            availableSheetTableComponentController.setMainController(this);
        }
        if (permissionsTableComponentController != null) {
            permissionsTableComponentController.setMainController(this);
        }

        // Method to refresh permissions table if a spreadsheet is selected
        startPermissionsTableRefresher();
    }

    public void setOkHttpClient(OkHttpClient client) {
        this.client = client;
    }

    public void setCookieManager(SimpleCookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    // Method to set the stage
    public void setStage(Stage stage) {
        this.stage = stage;
        // Add a listener to the window close event
        stage.setOnCloseRequest(this::handleWindowClose);
    }

    // Method to handle the window close event and call the logout servlet
    private void handleWindowClose(WindowEvent event) {
        String finalUrl = HttpUrl
                .parse(ClientConstants.LOGOUT)
                .url()
                .toString();

        // Create the logout request without cookies
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        // Log the cookies being sent
        System.out.println("Cookies being sent with logout request: " + cookieManager.loadForRequest(HttpUrl.parse(finalUrl)));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
                Platform.exit(); // Exit after handling response
                System.exit(0);  // Forcefully terminate all threads
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("Logout successful");
                    cookieManager.removeCookiesOf(HttpUrl.parse(finalUrl).host());
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + response.message());
                    System.err.println("Logout failed with response code: " + response.code());
                }
                response.close();
                Platform.exit(); // Exit after handling response
                System.exit(0);  // Forcefully terminate all threads
            }
        });
    }

    public String getUserName() {
        return headerLoadComponentController.getUserName();
    }

    // Method to load the spreadsheet from the client side
    public void loadSpreadsheet(String filePath) {

        String finalUrl = ClientConstants.LOAD_SPREADSHEET;

        // Create a form body to send in the POST request
        RequestBody body = new FormBody.Builder()
                .add("filePath", filePath)
                .build();

        System.out.println("Sending request to: " + finalUrl);

        // Execute the request asynchronously
        HttpClientUtil.runAsyncPost(finalUrl, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call,@NotNull IOException e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println("Received response code: " + response.code());

                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        // Manually parse the response for success
                        if (responseBody != null) {
                            availableSheetTableComponentController.updateSheetDetails();
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Spreadsheet loaded successfully.");
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error", responseBody);
                    });
                }
            }
        });
    }


    public void showGridWindow(String spreadsheetName, String username) {
        try {
            // Load the FXML for the Grid Window
            FXMLLoader gridLoader = new FXMLLoader(getClass().getResource(GRID_WINDOW_FXML));
            Parent gridRoot = gridLoader.load();

             //Get the GridWindowController and pass the file path
            gridWindowController = gridLoader.getController();
            gridWindowController.setUserName(username);
            gridWindowController.setClient(client); //not sure yet
            gridWindowController.setSpreadsheetData(spreadsheetName); // set the spreadsheet data also sets spreadsheetName
            gridWindowController.setCookieManager(cookieManager);
            gridWindowController.setStage(stage);
            gridWindowController.setMenuRoot(stage.getScene().getRoot());

            // Switch the scene to the grid window
            Scene scene = stage.getScene();
            scene.setRoot(gridRoot);  // Change the scene root to the grid view
            stage.setWidth(1650);
            stage.setHeight(800);
            gridWindowController.setSkin(Skin.DEFAULT.getDirectoryName());
            stage.setTitle("Grid Window" + " - " + spreadsheetName);
            // Get the user's permission for this spreadsheet. If he has reader permission only, disable the editing buttons
            getUserPermissionAndLockGridIfNeedBe(spreadsheetName, username);
            stage.show();
        } catch (IOException e) {
        }
    }

    public void loadPermissionsForSheet(String sheetName) {
        if (permissionsTableComponentController != null) {
            permissionsTableComponentController.fetchPermissionsData(sheetName);
        }
    }

    public void getUserPermissionAndLockGridIfNeedBe(String spreadsheetName, String username) {
        // URL for the GET request
        String finalUrl = HttpUrl.parse(ClientConstants.GET_USER_PERMISSIONS)
                .newBuilder()
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .addQueryParameter("username", username)
                .build()
                .toString();

        // Create a request object
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        // Run the async GET request
        HttpClientUtil.runAsyncGet(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to fetch permission: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call,@NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parse the response to get the PermissionType using Gson
                    String jsonResponse = response.body().string();
                    Gson gson = new Gson();
                    PermissionType permissionType = gson.fromJson(jsonResponse, PermissionType.class);

                    // Check if the PermissionType is READER, then disable the edit buttons
                    Platform.runLater(() -> {
                        if (permissionType.equals(PermissionType.READER)) {
                            disableGridEditButtons();
                        }
                    });
                } else {
                    Platform.runLater(() -> showError("Error: " + response.message()));
                }
            }
        });
    }

    private void disableGridEditButtons() {
        if (gridWindowController != null) {
            this.gridWindowController.disableEditButtons();
        }
    }

    // Start the timer for the permissions table refresher
    public void startPermissionsTableRefresher() {
        if (permissionsTableTimer != null) {
            permissionsTableTimer.cancel(); // cancel previous timer
        }

        permissionsTableTimer = new Timer();
        TimerTask refresherTask = new PermissionsTableRefresher(this);

        // Schedule the task to run every 0.5 seconds
        permissionsTableTimer.scheduleAtFixedRate(refresherTask, 0, 2000);
    }

    public String getSelectedSpreadsheetName() {
        return availableSheetTableComponentController.getSelectedSpreadsheetName();
    }

    // Method to load permissions for the selected sheet
    public void loadPermissionsDataForSheet(String sheetName) {
        if (permissionsTableComponentController != null) {
            permissionsTableComponentController.fetchPermissionsData(sheetName);
        }
    }

    // Method to update the selected spreadsheet name
    public void setSelectedSpreadsheetName(String spreadsheetName) {
        this.lastSelectedSpreadsheetName = spreadsheetName;
    }

    // Getter for the selected spreadsheet name
    public String getLastSelectedSpreadsheetName() {
        return this.lastSelectedSpreadsheetName;
    }

    public AvailableSheetTableController getAvailableSheetTableController() {
        return availableSheetTableComponentController;
    }

    public PermissionsTableController getPermissionsTableComponentController() {
        return permissionsTableComponentController;
    }

    public void setUserName(String text) {
        headerLoadComponentController.setUserName(text);
    }

    @Override
    public boolean equals(Object o) {


        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuWindowController that = (MenuWindowController) o;
        return Objects.equals(permissionsTableTimer, that.permissionsTableTimer) && Objects.equals(stage, that.stage)
                && Objects.equals(client, that.client) && Objects.equals(cookieManager, that.cookieManager)
                && Objects.equals(lastSelectedSpreadsheetName, that.lastSelectedSpreadsheetName)
                && Objects.equals(headerLoadComponentController, that.headerLoadComponentController)
                && Objects.equals(rightSideComponentController, that.rightSideComponentController)
                && Objects.equals(availableSheetTableComponentController, that.availableSheetTableComponentController)
                && Objects.equals(permissionsTableComponentController, that.permissionsTableComponentController)
                && Objects.equals(gridWindowController, that.gridWindowController);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionsTableTimer, stage, client, cookieManager, lastSelectedSpreadsheetName,
                headerLoadComponentController, rightSideComponentController, availableSheetTableComponentController,
                permissionsTableComponentController, gridWindowController);
    }
}
