package secondWindow;

import api.Engine;
import dto.EngineDTO;
import dto.SpreadsheetDTO;
import engineimpl.EngineImpl;
import exceptions.engineexceptions.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import secondWindow.top.HeaderLoadController;

public class SecondWindowController {
    private Engine engine;
    @FXML
    private HeaderLoadController headerLoadComponentController;//fixme- remove

    @FXML
    public void initialize() {

        if (headerLoadComponentController != null) { //fixme- remove
            headerLoadComponentController.setMainController(this);
        }
        engine = new EngineImpl();

    }
    //todo- move to another controller for the sceond screen ??
    public void loadSpreadsheet(String filePath) throws SpreadsheetLoadingException, CellUpdateException, InvalidExpressionException,
            CircularReferenceException, RangeProcessException {
        // Load the spreadsheet and update components on the JavaFX Application Thread
        try {
            engine.loadSpreadsheet(filePath);
            EngineDTO engineDTO = engine.getEngineData();
            int currentVersionNumber = engineDTO.getCurrentVersionNumber();
            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();

            // Ensure UI updates are executed on the JavaFX Application Thread
            Platform.runLater(() -> {
                //mainGridAreaComponentController.clearGrid(); // Add a clearGrid() method to your controller if it doesn't exist
               // optionsBarComponentController.updateCurrentVersionLabel(currentVersionNumber); // Update the current version label
                //mainGridAreaComponentController.start(spreadsheetDTO, false);
                //leftSideComponentController.refreshRanges();
            });

        } catch (SpreadsheetLoadingException | CellUpdateException | InvalidExpressionException | CircularReferenceException | RangeProcessException e) {
            // Rethrow exceptions to be handled by the calling code or task
            throw e;
        }
    }
}
