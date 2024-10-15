package gridwindow.top.interactiveformuladialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import gridwindow.GridWindowController;
import javafx.stage.Modality;
import javafx.stage.Stage;
import gridwindow.top.OptionsBarController;
import okhttp3.*;
import utils.ClientConstants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static utils.AlertUtils.showError;
import static utils.CommonResourcesPaths.INTERACTIVE_FORMULA_DIALOG_FXML;

public class InteractiveFormulaDialogController {
    @FXML
    private ComboBox<String> functionComboBox; // Dropdown to select function

    @FXML
    private VBox argumentContainer; // Container to hold arguments

    @FXML
    private TextField expressionTextField; // Text field that shows the current state of the expression

    @FXML
    private Button applyButton;

    @FXML
    private Button cancelButton;

    private GridWindowController mainController;
    private String currentFunctionName; // Store the selected function name
    private List<String> argumentValues = new ArrayList<>(); // Store argument values
    private boolean applied; // Indicates if "Apply" was pressed
    private OptionsBarController parentController;

    public void setMainController(GridWindowController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Populate the function dropdown with available functions
        ObservableList<String> functions = getFunctionNames();

        expressionTextField.setEditable(false);
        functionComboBox.setItems(functions);
        functionComboBox.setOnAction(event -> handleFunctionSelection());
        applyButton.setOnAction(event -> handleApplyButton());
        cancelButton.setOnAction(event -> handleCancelButton());
    }

    private void handleFunctionSelection() {
        // Clear previous arguments
        argumentContainer.getChildren().clear();
        argumentValues.clear();
        expressionTextField.clear();

        // Get selected function name from the ComboBox
        currentFunctionName = functionComboBox.getValue();

        if (currentFunctionName != null) {
            // Set the start of the function expression
            expressionTextField.setText("{" + currentFunctionName + ",");

            // Send request to server to get the number of arguments for the selected function
            String finalUrl = ClientConstants.GET_NUMBER_OF_ARGUMENTS;  // URL for fetching function details (number of arguments, etc.)
            RequestBody body = new FormBody.Builder()
                    .add("functionName", currentFunctionName)  // Send the selected function name to the server
                    .build();

            // Send async request to get function details (like argument count)
            HttpClientUtil.runAsyncPost(finalUrl, body, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> showError("Failed to fetch function details: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String jsonResponse = response.body().string();

                        // Assuming the server returns the number of arguments for the function as a simple integer
                        Gson gson = new Gson();
                        int argumentCount = gson.fromJson(jsonResponse, Integer.class);  // Parse the number of arguments

                        Platform.runLater(() -> {
                            // Dynamically create argument input fields
                            for (int i = 0; i < argumentCount; i++) {
                                addArgumentField();
                            }
                        });
                    } else {
                        Platform.runLater(() -> showError("Error: " + response.message()));
                    }
                }
            });
        }
    }


    private void addArgumentField() {
        HBox argumentBox = new HBox(5); // Horizontal box to contain the text field and button

        TextField argumentField = new TextField();
        argumentField.setPromptText("Enter value or expression");
        argumentField.textProperty().addListener((observable, oldValue, newValue) -> updateExpressionTextField());

        Button createFormulaButton = new Button("Create Formula");
        createFormulaButton.setOnAction(event -> openNestedFormulaDialog(argumentField));

        argumentBox.getChildren().addAll(argumentField, createFormulaButton); // Add text field and button to the box
        argumentContainer.getChildren().add(argumentBox); // Add the box to the container

        argumentValues.add(""); // Add an empty entry for the new argument
    }

    private void openNestedFormulaDialog(TextField targetField) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(INTERACTIVE_FORMULA_DIALOG_FXML));
            Parent root = loader.load();

            InteractiveFormulaDialogController nestedController = loader.getController();
            nestedController.setMainController(mainController);

            Stage stage = new Stage();
            stage.setTitle("Create Nested Formula");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Show the nested dialog and wait for it to be closed
            stage.showAndWait();

            // Check if the "Apply" button was pressed in the nested dialog
            if (nestedController.isApplied()) {
                String nestedExpression = nestedController.expressionTextField.getText();
                if (!nestedExpression.isEmpty()) {
                    targetField.setText(nestedExpression); // Set the nested expression to the target field
                    updateExpressionTextField(); // Update the main expression text field
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateExpressionTextField() {
        StringBuilder expressionBuilder = new StringBuilder();
        expressionBuilder.append("{").append(currentFunctionName);

        // Update argument values from text fields
        for (int i = 0; i < argumentContainer.getChildren().size(); i++) {
            HBox argumentBox = (HBox) argumentContainer.getChildren().get(i);
            TextField textField = (TextField) argumentBox.getChildren().get(0); // Get the text field from the HBox
            String argValue = textField.getText().trim();
            argumentValues.set(i, argValue); // Update the corresponding argument value

            if (!argValue.isEmpty()) {
                expressionBuilder.append(",").append(argValue);
            }
        }

        // Close the expression
        expressionBuilder.append("}");

        // Update the expression text field
        expressionTextField.setText(expressionBuilder.toString());
    }

    private void handleApplyButton() {
        this.applied = true; // Mark that the apply button was pressed

        // Check if this is a nested window
        if (getParentController() == null) {
            // Nested window, close without applying to action line
            ((Stage) applyButton.getScene().getWindow()).close();
        } else {
            // Not a nested window, apply to action line input
            parentController.applyExpressionToActionLine(expressionTextField.getText());
            ((Stage) applyButton.getScene().getWindow()).close(); // Close the dialog
        }
    }

    public void setParentController(OptionsBarController parentController) {
        this.parentController = parentController;
    }

    public OptionsBarController getParentController() {
        return parentController;
    }

    public boolean isApplied() {
        return applied;
    }

    private void handleCancelButton() {
        this.applied = false; // Ensure 'applied' is false if Cancel is pressed
        ((Stage) cancelButton.getScene().getWindow()).close(); // Close the dialog without applying any changes
    }

    private ObservableList<String> getFunctionNames() {
        ObservableList<String> functionNamesList = FXCollections.observableArrayList();
        String finalUrl = ClientConstants.GET_FUNCTION_NAMES;

        // Make an HTTP GET request to get the function names from the server
        HttpClientUtil.runAsyncGet(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showError("Failed to retrieve function data: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();

                    // Parse the JSON response to get a list of function names
                    Gson gson = new Gson();
                    List<String> functionNames = gson.fromJson(jsonResponse, new TypeToken<List<String>>() {}.getType());

                    // Update the ObservableList on the UI thread
                    Platform.runLater(() -> {
                        functionNamesList.clear();
                        functionNamesList.addAll(functionNames);
                    });
                } else {
                    Platform.runLater(() -> showError("Error: " + response.message()));
                }
            }
        });

        return functionNamesList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractiveFormulaDialogController that = (InteractiveFormulaDialogController) o;
        return applied == that.applied && Objects.equals(functionComboBox, that.functionComboBox) && Objects.equals(argumentContainer, that.argumentContainer) && Objects.equals(expressionTextField, that.expressionTextField) && Objects.equals(applyButton, that.applyButton) && Objects.equals(cancelButton, that.cancelButton) && Objects.equals(mainController, that.mainController) && Objects.equals(currentFunctionName, that.currentFunctionName) && Objects.equals(argumentValues, that.argumentValues) && Objects.equals(parentController, that.parentController);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionComboBox, argumentContainer, expressionTextField, applyButton, cancelButton, mainController, currentFunctionName, argumentValues, applied, parentController);
    }
}
