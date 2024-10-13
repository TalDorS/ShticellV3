package menuwindow.rightside;

import com.google.gson.Gson;
import enums.PermissionStatus;
import enums.PermissionType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import menuwindow.MenuWindowController;
import menuwindow.rightside.chat.ChatController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.AlertUtils;
import utils.ClientConstants;
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
    private ChatController chatComponentController;

    @FXML
    private void initialize() {
        viewSheetButton.setOnAction(event -> handleViewSheetButtonAction());

        requestPermissionButton.setOnAction(event -> handleRequestPermissionButtonAction());

        ackOrDenyPermissionRequestButton.setOnAction(event -> handleAckOrDenyPermissionRequestButtonAction());

        if (chatComponentController != null) {
            chatComponentController.setMainController(this);
        }
    }

    private void handleAckOrDenyPermissionRequestButtonAction() {
        PermissionStatus permissionStatus = mainController.getPermissionsTableComponentController().getSelectedRequestPermissionStatus();
        PermissionType permissionType = mainController.getPermissionsTableComponentController().getSelectedRequestPermissionType();
        String spreadsheetName = mainController.getAvailableSheetTableController().getSelectedSpreadsheetName();
        String uploaderName = mainController.getAvailableSheetTableController().getSelectedSpreadsheetUploaderName();
        String currentUsername = mainController.getUserName();
        String applicantUsername = mainController.getPermissionsTableComponentController().getSelectedRequestUsername();

        // Check if a request was pressed
        if (applicantUsername == null) {
            showError("Please select a permission request first");
            return;
        }

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

        // Add the radio buttons horizontally using an HBox
        HBox hbox = new HBox(10, approveRadio, denyRadio); // Add spacing of 10 between buttons
        hbox.setPadding(new Insets(10)); // Optional padding for the HBox
        dialog.getDialogPane().setContent(hbox);

        // Add Submit and Cancel buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Handle the dialog result
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                PermissionStatus newStatus = approveRadio.isSelected() ? PermissionStatus.APPROVED : PermissionStatus.REJECTED;

                // Prepare the POST request to handle the permission request
                String finalUrl = ClientConstants.HANDLE_PERMISSION_REQUEST;
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
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Platform.runLater(() -> showError("Failed to process the request. Please try again."));
                    }

                    @Override
                    public void onResponse(@NotNull Call call,@NotNull Response response) {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Success", "Request handled successfully."));
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

        // Check if no file was pressed
        if (uploaderName == null || spreadsheetName == null || username == null) {
            showError("You must first select a file to request a permission");
            return;
        }
        // If the user is the uploader, no need to request permission
        if (uploaderName.equals(username)) {
            showError("The uploader of a file cannot request permission to his file.");
            return;
        }

        // Create Permission Request Dialog
        createAndHandlePermissionRequestDialog(username, spreadsheetName);
    }

    private void createAndHandlePermissionRequestDialog(String username, String spreadsheetName) {
        // Open a new dialog with radio buttons for selecting "Writer" or "Reader"
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Request Permission");

        // Create description text
        Label descriptionLabel = new Label(
                "Requesting permission for spreadsheet: " + spreadsheetName + "\n" +
                "Choose the type of permission you would like to request:\n\n" +
                        "• READER - View-Only Permission:\n" +
                        "This permission allows you to view the spreadsheet but not make any changes. You can sort, filter, and view older versions.\n\n" +
                        "• WRITER - Edit Permission:\n" +
                        "This permission allows you to fully edit the spreadsheet, including updating cells, modifying layout, and more."
        );
        descriptionLabel.setWrapText(true);

        // Create radio buttons for permission type selection
        RadioButton writerRadio = new RadioButton("Writer");
        RadioButton readerRadio = new RadioButton("Reader");
        ToggleGroup group = new ToggleGroup();
        writerRadio.setToggleGroup(group);
        readerRadio.setToggleGroup(group);

        // Select Writer by default
        writerRadio.setSelected(true);

        // Layout the components vertically with spacing
        VBox vbox = new VBox(10, descriptionLabel, new HBox(10, writerRadio, readerRadio));
        vbox.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(vbox);

        // Add submit and cancel buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Show the dialog and wait for the user's selection
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Determine selected permission type
                PermissionType selectedPermission = writerRadio.isSelected() ? PermissionType.WRITER : PermissionType.READER;

                // POST request body
                String finalUrl = ClientConstants.REQUEST_PERMISSION;
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
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // Parse the JSON response using Gson
                            Gson gson = new Gson();
                            String jsonResponse = response.body().string();
                            Map<String, String> result = gson.fromJson(jsonResponse, Map.class);

                            Platform.runLater(() -> {
                                if ("ALREADY_HAS_PERMISSION".equals(result.get("status"))) {
                                    showAlert(Alert.AlertType.ERROR, "Error", "You already have this permission.");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RightSideController that = (RightSideController) o;
        return Objects.equals(mainController, that.mainController) && Objects.equals(viewSheetButton, that.viewSheetButton)
                && Objects.equals(requestPermissionButton, that.requestPermissionButton) && Objects.equals(ackOrDenyPermissionRequestButton, that.ackOrDenyPermissionRequestButton)
                && Objects.equals(chatComponentController, that.chatComponentController);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, viewSheetButton, requestPermissionButton, ackOrDenyPermissionRequestButton, chatComponentController);
    }
}
