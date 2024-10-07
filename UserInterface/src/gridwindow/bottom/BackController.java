package gridwindow.bottom;

import gridwindow.GridWindowController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class BackController {


    private GridWindowController mainController;

    @FXML
    private Button backButton;

    public void setMainController(GridWindowController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleBackButtonAction() {
        if (mainController != null) {
            mainController.hideMainGridAndShowMenu();  // Switch back to the main menu
        }
    }
}
