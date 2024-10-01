package menuwindow;

import api.Engine;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dto.EngineDTO;
import dto.SpreadsheetDTO;
import engineimpl.EngineImpl;
import exceptions.engineexceptions.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import menuwindow.center.AvailableSheetTableController;
import menuwindow.rightside.RightSideController;
import menuwindow.top.HeaderLoadController;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

public class MenuWindowController {
    private Engine engine;
    private OkHttpClient client = new OkHttpClient();

    @FXML
    private HeaderLoadController headerLoadComponentController;//fixme

    @FXML
    private RightSideController rightSideComponentController;

    @FXML
    private AvailableSheetTableController availableSheetTableComponentController;

    @FXML
    public void initialize() {
        if (headerLoadComponentController != null) { //fixme
            headerLoadComponentController.setMainController(this);
        }
        if( rightSideComponentController != null){
            rightSideComponentController.setMainController(this);
        }
        if(availableSheetTableComponentController != null){
            availableSheetTableComponentController.setMainController(this);
        }

        // Initialize Engine
        engine = new EngineImpl();

        // Load username from the server
        fetchUsernameFromServer();
    }

    //todo- fix that when pression view sheet button the sheet will be shown
    public void loadSpreadsheet(String filePath) throws SpreadsheetLoadingException, CellUpdateException, InvalidExpressionException,
            CircularReferenceException, RangeProcessException {
        // Load the spreadsheet and update components on the JavaFX Application Thread
        try {
            engine.loadSpreadsheet(filePath);
            if (availableSheetTableComponentController != null) {
                Platform.runLater(() -> availableSheetTableComponentController.addFilePathToTable(filePath));
            }
            EngineDTO engineDTO = engine.getEngineData();
            int currentVersionNumber = engineDTO.getCurrentVersionNumber();
            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();

            //todo- update the tableview with the new data, also when pressing the view sheet button the gridwindow will be opened
            // Ensure UI updates are executed on the JavaFX Application Thread
            Platform.runLater(() -> {
                //mainGridAreaComponentController.clearGrid(); // Add a clearGrid() method to your controller if it doesn't exist
               // optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber); // Update the current version label
                //mainGridAreaComponentController.start(spreadsheetDTO, false);
                //leftSideComponentController.refreshRanges();
            });

        } catch (SpreadsheetLoadingException | CellUpdateException | InvalidExpressionException | CircularReferenceException | RangeProcessException e) {
            // Rethrow exceptions to be handled by the calling code or task
            throw e;
        }
    }

    private void fetchUsernameFromServer() {
        Request request = new Request.Builder()
                .url("http://localhost:8080/Server_Web_exploded/user-details")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {
               Platform.runLater(() -> headerLoadComponentController.setUsername("Error fetching username"));
           }

           @Override
            public void onResponse(Call call, Response response) throws IOException {
               if (response.isSuccessful()) {
                   // Parse the response and get the username
                   String responseBody = response.body().string();
                   JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);

                   // Check if there's an error
                   if (jsonObject.has("error")) {
                       String error = jsonObject.get("error").getAsString();
                       Platform.runLater(() -> headerLoadComponentController.setUsername(error));
                   } else {
                       // Set the username from the server
                       String username = jsonObject.get("username").getAsString();
                       Platform.runLater(() -> headerLoadComponentController.setUsername(username));
                   }
               } else {
                   Platform.runLater(() -> headerLoadComponentController.setUsername("Failed to get username"));
               }
           }
        });
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
}
