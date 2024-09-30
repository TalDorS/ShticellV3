package menuwindow;

import api.Engine;
import dto.EngineDTO;
import dto.SpreadsheetDTO;
import engineimpl.EngineImpl;
import exceptions.engineexceptions.*;
import gridwindow.GridWindowController;
import gridwindow.top.Skin;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import menuwindow.center.AvailableSheetTableController;
import menuwindow.rightside.RightSideController;
import menuwindow.top.HeaderLoadController;

import java.io.IOException;
import java.util.Objects;

import static utils.CommonResourcesPaths.GRID_WINDOW_FXML;

public class MenuWindowController {
    private Stage gridWindowStage;
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

        } catch (SpreadsheetLoadingException | CellUpdateException | InvalidExpressionException | CircularReferenceException | RangeProcessException e) {
            // Rethrow exceptions to be handled by the calling code or task
            throw e;
        }
    }

    public void showGridWindow(String filePath) {
        try {
            if (gridWindowStage == null) {  // Initialize the stage if it hasn't been created
                gridWindowStage = new Stage();
            }
            // Load the FXML for the Grid Window
            FXMLLoader appLoader = new FXMLLoader(getClass().getResource(GRID_WINDOW_FXML));
            Parent root = appLoader.load();

            // Get the GridWindowController and pass the file path
            GridWindowController gridWindowController = appLoader.getController();
            gridWindowController.setSpreadsheetData(filePath); // Assuming this method exists to set data

            // Set up the scene and stage for the new Grid Window
            Scene scene = new Scene(root);
            gridWindowStage.setTitle("Grid Scene");
            gridWindowStage.setScene(scene);
            gridWindowStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CellUpdateException e) {
            throw new RuntimeException(e);
        } catch (InvalidExpressionException e) {
            throw new RuntimeException(e);
        } catch (SpreadsheetLoadingException e) {
            throw new RuntimeException(e);
        } catch (RangeProcessException e) {
            throw new RuntimeException(e);
        } catch (CircularReferenceException e) {
            throw new RuntimeException(e);
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

    public AvailableSheetTableController getAvailableSheetTableController() {
        return availableSheetTableComponentController;
    }
}
