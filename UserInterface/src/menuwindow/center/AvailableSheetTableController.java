package menuwindow.center;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import menuwindow.MenuWindowController;

public class AvailableSheetTableController {

    private MenuWindowController mainController;

    @FXML
    private TableView<String> fileTableView; // Declare TableView

    @FXML
    private TableColumn<String, String> fileColumn; // Declare TableColumn

    // ObservableList to hold the file paths
    private ObservableList<String> filePaths;

    @FXML
    public void initialize() {
        // Initialize the ObservableList
        filePaths = FXCollections.observableArrayList();

        // Set the cell value factory for the TableColumn
        fileColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));

        // Bind the TableView to the ObservableList
        fileTableView.setItems(filePaths);
    }

    @FXML
    public String getSelectedFilePath() {
        return fileTableView.getSelectionModel().getSelectedItem();
    }

    // Method to add file path to the table
    public void addFilePathToTable(String filePath) {
        // Add the file path to the ObservableList
        filePaths.add(filePath);
    }

    public void setMainController(MenuWindowController mainController) {
        this.mainController = mainController;
    }
}
