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
import java.util.List;

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

    private OkHttpClient client = new OkHttpClient();

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

    public void setMainController(MenuWindowController mainController) {
        this.mainController = mainController;
    }
}