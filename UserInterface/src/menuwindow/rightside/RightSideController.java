package menuwindow.rightside;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import menuwindow.MenuWindowController;

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
    }

    private void handleRequestPermissionButtonAction() {

    }

    private void handleViewSheetButtonAction() {
        // todo- need here to send the sheet to the gridwindow and open it
    }

    public void setMainController(MenuWindowController mainController) {
        this.mainController = mainController;
    }

}
