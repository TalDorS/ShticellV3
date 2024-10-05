package menuwindow.rightside;

import gridwindow.GridWindowController;
import gridwindow.top.Skin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import menuwindow.MenuWindowController;
import utils.AlertUtils;

import java.io.File;
import java.io.IOException;

import static utils.CommonResourcesPaths.GRID_WINDOW_FXML;

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
        //viewSheetButton.setOnAction(event -> handleViewSheetButtonAction()); // TODO get out of comment

        requestPermissionButton.setOnAction(event -> handleRequestPermissionButtonAction());

        ackOrDenyPermissionRequestButton.setOnAction(event -> handleAckOrDenyPermissionRequestButtonAction());
    }

    private void handleAckOrDenyPermissionRequestButtonAction() {
    }

    private void handleRequestPermissionButtonAction() {

    }

    // TODO get out of comment
//todo- change to file name instead of file path?? not sureee
//    private void handleViewSheetButtonAction() {
//        String selectedFileName = mainController.getAvailableSheetTableController().getSelectedFileName();
//        String userName = mainController.getUserName();
//
//        if (selectedFileName != null && userName != null) {
//            // Open the Grid Window and pass the selected file
//            mainController.showGridWindow(selectedFileName, userName);
//        } else {
//            // Handle the case where no file is selected, for example, show a warning dialog
//            AlertUtils.showAlert(Alert.AlertType.ERROR, "No File Selected", "Please select a file to view.");
//        }
//    }

    public void setMainController(MenuWindowController mainController) {
        this.mainController = mainController;
    }

}
