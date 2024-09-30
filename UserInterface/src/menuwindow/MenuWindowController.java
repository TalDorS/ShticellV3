package menuwindow;

import api.Engine;
import dto.EngineDTO;
import dto.SpreadsheetDTO;
import engineimpl.EngineImpl;
import exceptions.engineexceptions.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import menuwindow.center.AvailableSheetTableController;
import menuwindow.rightside.RightSideController;
import menuwindow.top.HeaderLoadController;

import java.util.Objects;

public class MenuWindowController {
    private Engine engine;
    @FXML
    private HeaderLoadController headerLoadComponentController;//fixme

    @FXML
    private RightSideController rightSideComponentController;

    @FXML
    private AvailableSheetTableController availableSheetTableComponentController;

    @FXML
    public void initialize() {

        if (headerLoadComponentController != null) { //fixme
            headerLoadComponentController.setMainController(this);
        }
        if( rightSideComponentController != null){
            rightSideComponentController.setMainController(this);
        }
        if(availableSheetTableComponentController != null){
            availableSheetTableComponentController.setMainController(this);
        }

        engine = new EngineImpl();

    }

    //todo- fix that when pression view sheet button the sheet will be shown
    public void loadSpreadsheet(String filePath) throws SpreadsheetLoadingException, CellUpdateException, InvalidExpressionException,
            CircularReferenceException, RangeProcessException {
        // Load the spreadsheet and update components on the JavaFX Application Thread
        try {
            engine.loadSpreadsheet(filePath);
            if (availableSheetTableComponentController != null) {
                Platform.runLater(() -> availableSheetTableComponentController.addFilePathToTable(filePath));
            }
            EngineDTO engineDTO = engine.getEngineData();
            int currentVersionNumber = engineDTO.getCurrentVersionNumber();
            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();

            //todo- update the tableview with the new data, also when pressing the view sheet button the gridwindow will be opened
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuWindowController that = (MenuWindowController) o;
        return Objects.equals(engine, that.engine) && Objects.equals(headerLoadComponentController, that.headerLoadComponentController) && Objects.equals(rightSideComponentController, that.rightSideComponentController) && Objects.equals(availableSheetTableComponentController, that.availableSheetTableComponentController);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engine, headerLoadComponentController, rightSideComponentController, availableSheetTableComponentController);
    }
}
