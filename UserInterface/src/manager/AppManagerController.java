package manager;

import javafx.fxml.FXML;
import javafx.scene.control.*;


public class AppManagerController {
    @FXML
    private ProgressBar progressBar;

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

}
