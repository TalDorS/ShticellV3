package gridwindow.bottom;

import gridwindow.GridWindowController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackController that = (BackController) o;
        return Objects.equals(mainController, that.mainController) && Objects.equals(backButton, that.backButton);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, backButton);
    }
}
