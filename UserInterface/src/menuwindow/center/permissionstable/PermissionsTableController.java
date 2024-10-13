package menuwindow.center.permissionstable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.PermissionsManagerDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import menuwindow.MenuWindowController;
import enums.PermissionStatus;
import enums.PermissionType;
import menuwindow.center.permissionstable.models.PermissionDetails;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.AlertUtils;
import utils.ClientConstants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PermissionsTableController {
    private MenuWindowController mainController;

    @FXML
    private TableView<PermissionDetails> permissionsTableView; // Table to hold the permissions

    @FXML
    private TableColumn<PermissionDetails, String> usernameColumn; // User's name

    @FXML
    private TableColumn<PermissionDetails, String> permissionTypeColumn; // Type of permission

    @FXML
    private TableColumn<PermissionDetails, String> permissionStatusColumn; // Status of the permission

    private ObservableList<PermissionDetails> permissionDetailsList;

    @FXML
    public void initialize() {
        // Initialize the ObservableList
        permissionDetailsList = FXCollections.observableArrayList();

        // Set cell value factories for each TableColumn
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        permissionTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPermissionType().toString()));
        permissionStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPermissionStatus().toString()));

        // Bind the TableView to the ObservableList
        permissionsTableView.setItems(permissionDetailsList);
    }

    public void setMainController(MenuWindowController mainController) {
        this.mainController = mainController;
    }

    // Method to request permission details from the server
    public void fetchPermissionsData(String spreadsheetName) {
        String url = HttpUrl.parse(ClientConstants.GET_PERMISSIONS)
                .newBuilder()
                .addQueryParameter("spreadsheetName", spreadsheetName)
                .build()
                .toString();

        HttpClientUtil.runAsyncGet(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call,@NotNull IOException e) {
                Platform.runLater(() -> {
                    AlertUtils.showAlert(Alert.AlertType.ERROR,"Failed to fetch permissions data: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Use Gson to deserialize the JSON response into a PermissionsManagerDTO
                    Gson gson = new Gson();
                    Type permissionsListType = new TypeToken<PermissionsManagerDTO>() {}.getType();
                    PermissionsManagerDTO permissionsData = gson.fromJson(responseBody, permissionsListType);

                    // Convert PermissionsManagerDTO to PermissionDetails list and update the table
                    Platform.runLater(() -> {
                        updatePermissionsTable(permissionsData);
                    });
                } else {
                    Platform.runLater(() -> {
                        AlertUtils.showAlert(Alert.AlertType.ERROR,"Failed to fetch permissions data: " + response.message());
                    });
                }
            }
        });
    }

    // Method to update the permissions table
    public void updatePermissionsTable(PermissionsManagerDTO permissionsData) {
        // Clear previous data
        permissionDetailsList.clear();

        // Add the owner
        permissionDetailsList.add(new PermissionDetails(permissionsData.getOwner(), PermissionType.OWNER, PermissionStatus.NONE));

        // Add the writers
        for (Map.Entry<String, PermissionStatus> entry : permissionsData.getWriters().entrySet()) {
            permissionDetailsList.add(new PermissionDetails(entry.getKey(), PermissionType.WRITER, entry.getValue()));
        }

        // Add the readers
        for (Map.Entry<String, PermissionStatus> entry : permissionsData.getReaders().entrySet()) {
            permissionDetailsList.add(new PermissionDetails(entry.getKey(), PermissionType.READER, entry.getValue()));
        }
    }

    // Method to get the selected request's permission status
    public PermissionStatus getSelectedRequestPermissionStatus() {
        // Get the selected item from the TableView
        PermissionDetails selectedRequest = permissionsTableView.getSelectionModel().getSelectedItem();

        // Check if an item is selected
        if (selectedRequest != null) {
            return selectedRequest.getPermissionStatus(); // Return the selected permission details
        } else {
            return null; // Return null if no item is selected
        }
    }

    // Method to get the selected request's permission type
    public PermissionType getSelectedRequestPermissionType() {
        // Get the selected item from the TableView
        PermissionDetails selectedRequest = permissionsTableView.getSelectionModel().getSelectedItem();

        // Check if an item is selected
        if (selectedRequest != null) {
            return selectedRequest.getPermissionType(); // Return the selected permission details
        } else {
            return null; // Return null if no item is selected
        }
    }

    // Method to get the username of the one who made the request
    public String getSelectedRequestUsername() {
        // Get the selected item from the TableView
        PermissionDetails selectedRequest = permissionsTableView.getSelectionModel().getSelectedItem();

        // Check if an item is selected
        if (selectedRequest != null) {
            return selectedRequest.getUsername(); // Return the selected permission details
        } else {
            return null; // Return null if no item is selected
        }
    }

    // Method to compare new data with current table data
    public boolean isDataSame(PermissionsManagerDTO newPermissionsData) {
        // Compare the current data in the permissions table with the new data (ignoring order)
        // You can convert both current table data and new data to sets for comparison
        Set<PermissionDetails> currentDataSet = new HashSet<>(permissionDetailsList);
        Set<PermissionDetails> newDataSet = new HashSet<>();

        // Add the owner
        newDataSet.add(new PermissionDetails(newPermissionsData.getOwner(), PermissionType.OWNER, PermissionStatus.NONE));

        // Add writers and readers
        for (Map.Entry<String, PermissionStatus> entry : newPermissionsData.getWriters().entrySet()) {
            newDataSet.add(new PermissionDetails(entry.getKey(), PermissionType.WRITER, entry.getValue()));
        }
        for (Map.Entry<String, PermissionStatus> entry : newPermissionsData.getReaders().entrySet()) {
            newDataSet.add(new PermissionDetails(entry.getKey(), PermissionType.READER, entry.getValue()));
        }

        return currentDataSet.equals(newDataSet);  // Return true if the data is the same
    }

    // Method to reset the permissions table to its default state
    public void resetPermissionsTableToDefault() {
        // Clear the current data in the table
        permissionDetailsList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionsTableController that = (PermissionsTableController) o;
        return Objects.equals(mainController, that.mainController) && Objects.equals(permissionsTableView, that.permissionsTableView)
                && Objects.equals(usernameColumn, that.usernameColumn) && Objects.equals(permissionTypeColumn, that.permissionTypeColumn)
                && Objects.equals(permissionStatusColumn, that.permissionStatusColumn) && Objects.equals(permissionDetailsList, that.permissionDetailsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, permissionsTableView, usernameColumn, permissionTypeColumn, permissionStatusColumn, permissionDetailsList);
    }
}
