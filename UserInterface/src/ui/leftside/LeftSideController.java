package ui.leftside;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import app.AppController;
import ui.leftside.addrangedialog.AddRangeDialogController;
import ui.leftside.filterdialog.FilterDialogController;
import ui.leftside.graphdialog.GraphDialogController;
import ui.leftside.sortdialog.SortDialogController;
import utils.AlertUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static utils.CommonResourcesPaths.*;

public class LeftSideController {
    private AppController mainController;

    @FXML
    private Accordion rangesAccordion; // Container for range items

    @FXML
    public void initialize() {
        // Initialization logic here
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleAddRangeButton() {
        try {
            // Check if a spreadsheet is loaded
            if (!mainController.isSpreadsheetLoaded()) {
                throw new IllegalStateException("No spreadsheet loaded. Please load a spreadsheet first.");
            }

            // Load the Add Range Dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ADD_RANGE_DIALOG_FXML));
            Parent root = loader.load();

            // Get the controller of the dialog
            AddRangeDialogController dialogController = loader.getController();
            dialogController.setMainController(mainController); // Pass the main controller to the dialog

            // Create a new stage for the dialog
            Stage stage = new Stage();
            stage.setTitle("Add New Range");
            stage.setScene(new Scene(root));

            // Set the stage to be modal
            stage.initModality(Modality.APPLICATION_MODAL);

            // Show the stage and wait for it to be closed before returning to the main window
            stage.showAndWait();
        } catch (IllegalStateException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Adding New Range", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilterButton() {
        try {
            // Check if a spreadsheet is loaded
            if (!mainController.isSpreadsheetLoaded()) {
                throw new IllegalStateException("No spreadsheet loaded. Please load a spreadsheet first.");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(FILTER_DIALOG_FXML));
            Parent root = loader.load();

            FilterDialogController filterDialogController = loader.getController();
            filterDialogController.setMainController(mainController);

            Stage stage = new Stage();
            stage.setTitle("Filter Table");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Opening Filter Dialog", e.getMessage());
        } catch (IllegalStateException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Adding New Range", e.getMessage());
        }
    }

    public void addRangeToUI(String name, String firstCell, String lastCell) {
        // Create a new TitledPane for each range
        TitledPane rangePane = new TitledPane();
        rangePane.setText(name); // Set only the name as the title of the pane

        // Container for the content inside the TitledPane
        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT); // Align content vertically centered

        // Create a label to display the range details
        Label rangeDetails = new Label(firstCell + " to " + lastCell);

        // Create the delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> handleDeleteRange(name));

        // Add mouse hover events to change the background of relevant cells
        rangePane.setOnMouseEntered(event -> handleMouseHover(firstCell, lastCell, true));
        rangePane.setOnMouseExited(event -> handleMouseHover(firstCell, lastCell, false));

        // Add the label and delete button to the content
        content.getChildren().addAll(rangeDetails, deleteButton);

        // Set the content of the TitledPane
        rangePane.setContent(content);

        // Add the new TitledPane to the Accordion
        rangesAccordion.getPanes().add(rangePane);
    }


    // Method to handle mouse hover events
    private void handleMouseHover(String firstCell, String lastCell, boolean isHovering) {
        mainController.highlightRange(firstCell, lastCell, isHovering); // Delegate to AppController
    }

    private void handleDeleteRange(String rangeName) {
        mainController.deleteRange(rangeName); // Pass the delete request to the AppController
        refreshRanges(); // Refresh the ranges to update the UI
    }

    // Method to refresh the range list in the UI
    public void refreshRanges() {
        // Clear the current list of ranges
        rangesAccordion.getPanes().clear();

        // Fetch the current list of ranges from the backend engine
        Map<String, String[]> ranges = mainController.getRanges();

        // Rebuild the range list in the UI
        for (Map.Entry<String, String[]> entry : ranges.entrySet()) {
            String name = entry.getKey();
            String firstCell = entry.getValue()[0];
            String lastCell = entry.getValue()[1];

            addRangeToUI(name, firstCell, lastCell);
        }
    }

    @FXML
    private void handleSortButton() {
        try {
            // Check if a spreadsheet is loaded
            if (!mainController.isSpreadsheetLoaded()) {
                throw new IllegalStateException("No spreadsheet loaded. Please load a spreadsheet first.");
            }

            // Load the Sort Dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SORT_DIALOG_FXML));
            Parent root = loader.load();

            // Get the controller of the dialog
            SortDialogController dialogController = loader.getController();
            dialogController.setMainController(mainController); // Pass the main controller to the dialog

            // Create a new stage for the dialog
            Stage stage = new Stage();
            stage.setTitle("Sort Spreadsheet");
            stage.setScene(new Scene(root));

            // Set the stage to be modal
            stage.initModality(Modality.APPLICATION_MODAL);

            // Show the stage and wait for it to be closed before returning to the main window
            stage.showAndWait();

        } catch (IOException e) {
            // Handle IOException, which may occur during FXML loading
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Loading Sort Dialog", "An error occurred while loading the sort dialog: " + e.getMessage());
            e.printStackTrace(); // Optionally print stack trace for debugging
        } catch (IllegalStateException e) {
            // Handle IllegalStateException for cases where no spreadsheet is loaded
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Spreadsheet Not Loaded", e.getMessage());
        } catch (Exception e) {
            // Handle any other exceptions that might occur
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Optionally print stack trace for debugging
        }
    }

    @FXML
    public void handleGraphsButton() {
        try {
            // Check if a spreadsheet is loaded
            if (!mainController.isSpreadsheetLoaded()) {
                throw new IllegalStateException("No spreadsheet loaded. Please load a spreadsheet first.");
            }

            // Load the Graph Dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(GRAPH_DIALOG_FXML));
            Parent root = loader.load();

            // Get the controller of the dialog
            GraphDialogController graphDialogController = loader.getController();
            graphDialogController.setMainController(mainController); // Pass the main controller to the dialog

            // Get the available range names from the AppController
            List<String> rangeNames = mainController.getRangeNames();
            //graphDialogController.setAvailableRanges(rangeNames); // Set the available ranges in the graph dialog

            // Create a new stage for the dialog
            Stage stage = new Stage();
            stage.setTitle("Create Graph");
            stage.setScene(new Scene(root));

            // Set the stage to be modal
            stage.initModality(Modality.APPLICATION_MODAL);

            // Show the stage and wait for it to be closed before returning to the main window
            stage.showAndWait();
        } catch (IllegalStateException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Creating Graph", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error Opening Graph Dialog", e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeftSideController that = (LeftSideController) o;
        return Objects.equals(mainController, that.mainController) && Objects.equals(rangesAccordion, that.rangesAccordion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, rangesAccordion);
    }
}
