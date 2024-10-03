package menuwindow;

import api.Engine;
import engineimpl.EngineImpl;
import exceptions.engineexceptions.*;
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
import menuwindow.center.AvailableSheetTableController;
import menuwindow.rightside.RightSideController;
import menuwindow.top.HeaderLoadController;
import okhttp3.*;
import utils.ClientConstants;
import utils.SimpleCookieManager;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;

import static utils.AlertUtils.showAlert;
import static utils.CommonResourcesPaths.GRID_WINDOW_FXML;

public class MenuWindowController {
    private Stage stage; // To hold the stage reference
    private Map<String, Stage> gridWindowsStages = new HashMap<>();
    private Engine engine;

    private OkHttpClient client;
    private SimpleCookieManager cookieManager;

    @FXML
    private HeaderLoadController headerLoadComponentController;

    @FXML
    private RightSideController rightSideComponentController;

    @FXML
    private AvailableSheetTableController availableSheetTableComponentController;

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
        engine = new EngineImpl();
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
            public void onFailure(Call call, IOException e) {
                // Optionally log or handle logout failure
                System.out.println("Logout request failed: " + e.getMessage());
                Platform.exit(); // Exit after handling response
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("Logout successful");
                    cookieManager.removeCookiesOf(HttpUrl.parse(finalUrl).host());
                } else {
                    System.err.println("Logout failed with response code: " + response.code());
                }
                response.close();
                Platform.exit(); // Exit after handling response
                System.exit(0);  // Forcefully terminate all threads
            }
        });
    }

    public String getUserName() {
        return  headerLoadComponentController.getUserName();
    }

    //todo- fix that when premssion view sheet button the sheet will be shown
    public void loadSpreadsheet(String filePath) throws SpreadsheetLoadingException, CellUpdateException, InvalidExpressionException,
            CircularReferenceException, RangeProcessException {
        // Load the spreadsheet and update components on the JavaFX Application Thread
        try {
            String userName = headerLoadComponentController.getUserName();
            // Load the spreadsheet and get the result
            Pair<String, Boolean> result = engine.loadSpreadsheet(userName, filePath);
            String fileName = result.getKey();
            boolean isNewFile = result.getValue();
            if (!isNewFile) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "File Already Exists", "The file '" + fileName + "' already exists. Please use a different file.");
                });
                return; // Stop further processing if it's not a new file
            }
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Spreadsheet loaded successfully.");
            });

            // Add the file to the available sheet table only if it's a new file
            if (availableSheetTableComponentController != null) {
                Platform.runLater(() -> availableSheetTableComponentController.addFileNameToTable(fileName));
            }

        } catch (SpreadsheetLoadingException | CellUpdateException | InvalidExpressionException | CircularReferenceException | RangeProcessException e) {
            // Rethrow exceptions to be handled by the calling code or task
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void showGridWindow(String fileName, String userName) {
        try {
            if (!gridWindowsStages.containsKey(filePath)) {  // Initialize the stage if it hasn't been created
                // Insert the new filepath to grid maps
                gridWindowsStages.put(filePath, new Stage());

                // Get it
                Stage gridWindowStage = gridWindowsStages.get(filePath);

                // Load the FXML for the Grid Window
                FXMLLoader appLoader = new FXMLLoader(getClass().getResource(GRID_WINDOW_FXML));
                Parent root = appLoader.load();

                // Get the GridWindowController and pass the file path
                GridWindowController gridWindowController = appLoader.getController();
                //gridWindowController.setName(sheetName); //fixme do i need? maybe later we need the file name to be unique
                gridWindowController.setFilePath(filePath);
                gridWindowController.setUserName(userName);
                gridWindowController.setEngine(engine);
                gridWindowController.setSpreadsheetData(filePath); // Assuming this method exists to set data

                // Set up the scene and stage for the new Grid Window
                Scene scene = new Scene(root);
                gridWindowController.setSkin(Skin.DEFAULT.getDirectoryName());
                gridWindowStage.setTitle("Grid Window");
                gridWindowStage.setScene(scene);
            } else {
                gridWindowsStages.get(filePath).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CellUpdateException e) {
            throw new RuntimeException(e);
        } catch (InvalidExpressionException e) {
            throw new RuntimeException(e);
        } catch (SpreadsheetLoadingException e) {
            throw new RuntimeException(e);
        } catch (RangeProcessException e) {
            throw new RuntimeException(e);
        } catch (CircularReferenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuWindowController that = (MenuWindowController) o;
        return Objects.equals(engine, that.engine) && Objects.equals(headerLoadComponentController, that.headerLoadComponentController) && Objects.equals(rightSideComponentController, that.rightSideComponentController) && Objects.equals(availableSheetTableComponentController, that.availableSheetTableComponentController);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engine, headerLoadComponentController, rightSideComponentController, availableSheetTableComponentController);
    }

    public AvailableSheetTableController getAvailableSheetTableController() {
        return availableSheetTableComponentController;
    }

    public void setUserName(String text) {
        headerLoadComponentController.setUserName(text);
    }
}
