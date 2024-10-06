package menuwindow.center.sheettable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import menuwindow.MenuWindowController;
import okhttp3.*;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class AvailableSheetTableController {
    private MenuWindowController mainController;

    @FXML
    private TableView<SheetDetails> sheetTableView; // Update TableView to hold SheetDetails objects

    @FXML
    private TableColumn<SheetDetails, String> userColumn; // User who uploaded the sheet

    @FXML
    private TableColumn<SheetDetails, String> sheetNameColumn; // Sheet name

    @FXML
    private TableColumn<SheetDetails, String> sheetSizeColumn; // Sheet size

    @FXML
    private TableColumn<SheetDetails, String> permissionColumn; // User's permission for the sheet

    // ObservableList to hold the SheetDetails objects
    private ObservableList<SheetDetails> sheetDetailsList;

    private Timer timer;

    @FXML
    public void initialize() {
        // Initialize the ObservableList
        sheetDetailsList = FXCollections.observableArrayList();

        // Set cell value factories for each TableColumn
        userColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUploaderName()));
        sheetNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSheetName()));
        sheetSizeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSheetSize()));
        permissionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPermission()));

        // Bind the TableView to the ObservableList
        sheetTableView.setItems(sheetDetailsList);

        // Start refreshing every 2 seconds
        startSheetRefresher();
    }

    // Start the timer for the sheet refresher
    private void startSheetRefresher() {
        timer = new Timer();
        TimerTask refresherTask = new SheetRefresher(this);

        // Schedule the task to run every 2 seconds (2000 milliseconds)
        timer.scheduleAtFixedRate(refresherTask, 0, 2000);
    }

    // Method to update sheetDetails with an http request
    public void updateSheetDetails() {
        String finalUrl = "http://localhost:8080/Server_Web_exploded/sheet-details";

        HttpClientUtil.runAsyncGet(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> {
                    System.out.println("Failed to fetch sheet details: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Use Gson to deserialize the response into a List of SheetDetails
                    Gson gson = new Gson();
                    Type sheetListType = new TypeToken<List<SheetDetails>>() {}.getType();
                    List<SheetDetails> sheetDetails = gson.fromJson(responseBody, sheetListType);

                    // Update the table on the JavaFX Application thread
                    Platform.runLater(() -> {
                        sheetDetailsList.clear();
                        sheetDetailsList.addAll(sheetDetails); // Update the table with the new data
                    });
                } else {
                    Platform.runLater(() -> {
                        System.out.println("Failed to fetch sheet details: " + response.message());
                    });
                }
            }
        });
    }

    // Method to compare and update the sheet details while ignoring the order of elements
    public boolean isDataSame(List<SheetDetails> newSheetDetails) {
        // Convert both lists to sets for order-insensitive comparison
        Set<SheetDetails> currentDataSet = new HashSet<>(sheetDetailsList);
        Set<SheetDetails> newDataSet = new HashSet<>(newSheetDetails);

        return currentDataSet.equals(newDataSet);
    }

    // Update the table with new sheet details
    public void updateTableWithSheetDetails(List<SheetDetails> newSheetDetails) {
        sheetDetailsList.clear();
        sheetDetailsList.addAll(newSheetDetails);
    }

    public void setMainController(MenuWindowController mainController) {
        this.mainController = mainController;
    }

    // Return the sheet name of the currently selected row
    public String getSelectedSpreadsheetName() {
        SheetDetails selectedSheet = sheetTableView.getSelectionModel().getSelectedItem(); // Get the selected item

        if (selectedSheet != null) {
            return selectedSheet.getSheetName(); // Return the sheet name of the selected item
        }

        return null; // Return null if no row is selected
    }
}