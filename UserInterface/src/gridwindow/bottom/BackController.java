package gridwindow.bottom;

import gridwindow.GridWindowController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class BackController {


    private GridWindowController mainController;

    @FXML
    private Button backButton;

    private void initialize() {
        backButton.setOnAction(event -> handleBackButtonAction());
    }

    public void setMainController(GridWindowController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleBackButtonAction() {
        //todo- implement
    }
}
