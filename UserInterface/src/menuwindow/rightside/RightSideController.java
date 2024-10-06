package menuwindow.rightside;

import com.google.gson.Gson;
import enums.PermissionStatus;
import enums.PermissionType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import menuwindow.MenuWindowController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.AlertUtils;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static utils.AlertUtils.showAlert;
import static utils.AlertUtils.showError;

public class RightSideController {
    private MenuWindowController mainController;

    @FXML
    private Button viewSheetButton;

    @FXML
    private Button requestPermissionButton;

    @FXML
    private Button ackOrDenyPermissionRequestButton;

    @FXML
    private void initialize() {
        viewSheetButton.setOnAction(event -> handleViewSheetButtonAction());

        requestPermissionButton.setOnAction(event -> handleRequestPermissionButtonAction());

        ackOrDenyPermissionRequestButton.setOnAction(event -> handleAckOrDenyPermissionRequestButtonAction());
    }

    private void handleAckOrDenyPermissionRequestButtonAction() {
        PermissionStatus permissionStatus = mainController.getPermissionsTableComponentController().getSelectedRequestPermissionStatus();
        PermissionType permissionType = mainController.getPermissionsTableComponentController().getSelectedRequestPermissionType();
        String spreadsheetName = mainController.getAvailableSheetTableController().getSelectedSpreadsheetName();
        String uploaderName = mainController.getAvailableSheetTableController().getSelectedSpreadsheetUploaderName();
        String currentUsername = mainController.getUserName();
        String applicantUsername = mainController.getPermissionsTableComponentController().getSelectedRequestUsername();

        // Check if the permission status is not PENDING
        if (permissionStatus != PermissionStatus.PENDING) {
            showError("This request doesn't need to be acknowledged or denied.");
            return;
        }

        // Check if the current user is not the owner
        if (!uploaderName.equals(currentUsername)) {
            showError("This request can only be approved or denied by the owner of the spreadsheet.");
            return;
        }

        // Create a dialog for approving or denying the request
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Handle Permission Request");

        // Create radio buttons for Approve and Deny, with Approve as the default option
        RadioButton approveRadio = new RadioButton("Approve Request");
        approveRadio.setSelected(true);
        RadioButton denyRadio = new RadioButton("Deny Request");
        ToggleGroup group = new ToggleGroup();
        approveRadio.setToggleGroup(group);
        denyRadio.setToggleGroup(group);

        // Add the radio buttons to the dialog
        VBox vbox = new VBox(approveRadio, denyRadio);
        dialog.getDialogPane().setContent(vbox);

        // Add Submit and Cancel buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Handle the dialog result
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                PermissionStatus newStatus = approveRadio.isSelected() ? PermissionStatus.APPROVED : PermissionStatus.REJECTED;

                // Prepare the POST request to handle the permission request
                String finalUrl = "http://localhost:8080/Server_Web_exploded/handle-permission-request";
                RequestBody body = new FormBody.Builder()
                        .add("applicantName", applicantUsername)
                        .add("handlerName", currentUsername)
                        .add("spreadsheetName", spreadsheetName)
                        .add("permissionStatus", newStatus.toString())
                        .add("permissionType", permissionType.toString())
                        .build();

                // Run the async POST request
                HttpClientUtil.runAsyncPost(finalUrl, body, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Platform.runLater(() -> showError("Failed to process the request. Please try again."));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION ,"Success", "Request handled successfully."));
                        } else {
                            Platform.runLater(() -> showError("Failed to process the request. " + response.message()));
                        }
                    }
                });
            }
        });
    }

    private void handleRequestPermissionButtonAction() {
        String uploaderName = mainController.getAvailableSheetTableController().getSelectedSpreadsheetUploaderName();
        String spreadsheetName = mainController.getAvailableSheetTableController().getSelectedSpreadsheetName();
        String username = mainController.getUserName();

        // If the user is the uploader, no need to request permission
        if (uploaderName.equals(username)) {
            return;
        }

        // Create Permission Request Dialog
        createAndHandlePermissionRequestDialog(username,spreadsheetName);
    }

    private void createAndHandlePermissionRequestDialog(String username, String spreadsheetName) {
        // Open a new dialog with radio buttons for selecting "Writer" or "Reader"
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Request Permission");

        // Create radio buttons
        RadioButton writerRadio = new RadioButton("Writer");
        RadioButton readerRadio = new RadioButton("Reader");
        ToggleGroup group = new ToggleGroup();
        writerRadio.setToggleGroup(group);
        readerRadio.setToggleGroup(group);

        // Add buttons to the dialog
        VBox vbox = new VBox(writerRadio, readerRadio);
        dialog.getDialogPane().setContent(vbox);

        // Add submit and cancel buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Show the dialog and wait for the user's selection
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Determine selected permission type
                PermissionType selectedPermission = writerRadio.isSelected() ? PermissionType.WRITER : PermissionType.READER;

                // POST request body
                String finalUrl = "http://localhost:8080/Server_Web_exploded/request-permission";
                RequestBody body = new FormBody.Builder()
                        .add("username", username)
                        .add("spreadsheetName", spreadsheetName)
                        .add("permissionType", selectedPermission.toString())
                        .build();

                // Run the async POST request
                HttpClientUtil.runAsyncPost(finalUrl, body, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Platform.runLater(() -> {
                            // Handle failure
                            showAlert(Alert.AlertType.ERROR, "Failed to request permission. Please try again.");
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // Parse the JSON response using Gson
                            Gson gson = new Gson();
                            String jsonResponse = response.body().string();
                            Map<String, String> result = gson.fromJson(jsonResponse, Map.class);

                            Platform.runLater(() -> {
                                if ("ALREADY_HAS_PERMISSION".equals(result.get("status"))) {
                                    showAlert(Alert.AlertType.ERROR, "Error","You already have this permission.");
                                } else if ("PERMISSION_REQUESTED".equals(result.get("status"))) {
                                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Success", "Permission request sent successfully.");
                                } else if ("ERROR".equals(result.get("status"))) {
                                    showAlert(Alert.AlertType.ERROR, "Error", result.get("message"));
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    //todo- change to file name instead of file path?? not sureee
    private void handleViewSheetButtonAction() {
        String selectedFileName = mainController.getAvailableSheetTableController().getSelectedSpreadsheetName();
        String permission = mainController.getAvailableSheetTableController().getSelectedSpreadsheetPermission();
        String userName = mainController.getUserName();

        if (selectedFileName == null || userName == null) {
            // Handle the case where no file is selected, for example, show a warning dialog
            showAlert(Alert.AlertType.ERROR, "No File Selected", "Please select a file to view.");
            return;
        }
        if (permission.equals("NONE")) {
            showError("You do not have permission to view this spreadsheet.");
            return;
        }

        // Open the Grid Window and pass the selected file
        mainController.showGridWindow(selectedFileName, userName);
    }

    public void setMainController(MenuWindowController mainController) {
        this.mainController = mainController;
    }

}
