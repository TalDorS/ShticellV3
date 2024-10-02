package manager;

import api.Engine;
import engineimpl.EngineImpl;
import exceptions.engineexceptions.SpreadsheetLoadingException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class SpreadsheetManagerController {
    @FXML
    private ProgressBar progressBar;

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

}
