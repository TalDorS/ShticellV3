package gridwindow.top.interactiveformuladialog;

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
import enums.FunctionType;
import api.Expression;
import javafx.stage.Modality;
import javafx.stage.Stage;
import gridwindow.top.OptionsBarController;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InteractiveFormulaDialogController {
    @FXML
    private ComboBox<String> functionComboBox; // Dropdown to select function

    @FXML
    private VBox argumentContainer; // Container to hold arguments

    @FXML
    private TextField expressionTextField; // Text field that shows the current state of the expression

    @FXML
    private Button calculateButton;

    @FXML
    private Label resultPreview; // Label to display preview result

    @FXML
    private Button applyButton;

    @FXML
    private Button cancelButton;

    private GridWindowController mainController;
    private List<Expression> argumentExpressions = new ArrayList<>();
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
        ObservableList<String> functions = FXCollections.observableArrayList();

        for (FunctionType functionType : FunctionType.values()) {
            functions.add(functionType.name());
        }

        expressionTextField.setEditable(false);
        functionComboBox.setItems(functions);
        functionComboBox.setOnAction(event -> handleFunctionSelection());
        calculateButton.setOnAction(event -> calculateExpression());
        applyButton.setOnAction(event -> handleApplyButton());
        cancelButton.setOnAction(event -> handleCancelButton());
    }

    private void handleFunctionSelection() {
        // Clear previous arguments
        argumentContainer.getChildren().clear();
        argumentExpressions.clear();
        argumentValues.clear();
        expressionTextField.clear();

        // Get selected function and determine the number of required arguments
        currentFunctionName = functionComboBox.getValue();

        if (currentFunctionName != null) {
            expressionTextField.setText("{" + currentFunctionName + ","); // Set the start of the function expression
            FunctionType selectedFunction = FunctionType.valueOf(currentFunctionName);
            int argumentCount = selectedFunction.getFunction().getNumberOfArguments(); // Get expected arguments count for function

            for (int i = 0; i < argumentCount; i++) {
                addArgumentField();
            }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gridwindow/top/interactiveformuladialog/InteractiveFormulaDialog.fxml"));
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

    private void calculateExpression() {
        try {
            if (currentFunctionName == null || currentFunctionName.isEmpty()) {
                resultPreview.setText("Please select a function.");
                return;
            }

            Expression parseResult = mainController.parseExpression(expressionTextField.getText());
            Object result = parseResult.evaluate();

            if (result instanceof Number) {
                NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                numberFormat.setMinimumFractionDigits(0);  // No minimum decimal digits
                numberFormat.setMaximumFractionDigits(2);  // Maximum 2 decimal digits
                result = numberFormat.format(result);
            }

            // Check if this is not a nested window
            if (getParentController() != null) {
                mainController.checkForCircularReferences(parentController.getCurrentCellId(), parseResult);
            }
            resultPreview.setText("Result: " + result.toString());
        } catch (Exception e) {
            resultPreview.setText("Error: " + e.getMessage());
        }
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
}
