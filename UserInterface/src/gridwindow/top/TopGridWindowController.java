package gridwindow.top;

import exceptions.engineexceptions.*;
import gridwindow.GridWindowController;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Objects;

public class TopGridWindowController {
    @FXML
    private Label nameLabel;

    @FXML
    private MenuButton colorDisplay;

    @FXML
    private MenuButton animationDisplay;

    @FXML
    private Button updateNewVersionButton;

    @FXML
    private Label newVersionLabel;

    private GridWindowController mainController;
    private SpreadsheetVersionRefresher versionRefresher;

    @FXML
    public void initialize() {
        colorDisplay.setOnShowing(event -> handleColorDisplay());
        animationDisplay.setOnShowing(event -> handleAnimationDisplay());
    }

    public void startVersionRefresher() {
        if (versionRefresher == null) {
            versionRefresher = new SpreadsheetVersionRefresher(mainController);
            versionRefresher.startRefreshing();
        }
    }

    public void stopVersionRefresher() {
        if (versionRefresher != null) {
            versionRefresher.stopRefreshing();
            versionRefresher = null;  // Clear the reference
        }
    }

    public void setMainController(GridWindowController gridWindowController) {
        this.mainController = gridWindowController;
    }

    public void setUsername(String username) {
        nameLabel.setText(username);
    }

    public String getUsername() {
        return nameLabel.getText();
    }

    private void handleColorDisplay() {
        colorDisplay.getItems().clear();
        for (Skin skin : Skin.values()) {
            MenuItem menuItem = new MenuItem(skin.getDisplayName());
            menuItem.setOnAction(event -> handleSkinChange(skin));
            colorDisplay.getItems().add(menuItem);
        }
    }

    private void handleSkinChange(Skin skin) {
        mainController.setSkin(skin.name().toLowerCase());
        colorDisplay.setText(skin.getDisplayName());
    }

    private void handleAnimationDisplay() {
        animationDisplay.getItems().clear();
        for (Animation animation : Animation.values()) {
            MenuItem menuItem = new MenuItem(animation.getDisplayName());
            menuItem.setOnAction(event -> handleAnimationChange(animation));
            animationDisplay.getItems().add(menuItem);
        }
    }

    private void handleAnimationChange(Animation animation) {
        mainController.setAnimation(animation.getIdentifier());
        animationDisplay.setText(animation.getDisplayName());
    }

    public void setNewVersionVisiblity(boolean visible) {
        newVersionLabel.setVisible(visible);
        updateNewVersionButton.setVisible(visible);
    }

    public boolean isNewVersionVisible() {
        return newVersionLabel.isVisible();
    }

    public void handleUpdateNewVersionButton() throws CellUpdateException, InvalidExpressionException, SpreadsheetLoadingException,
            RangeProcessException, CircularReferenceException {
        // Load the new version or prompt the user
        mainController.setSpreadsheetData(mainController.getSpreadsheetName());
        setNewVersionVisiblity(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopGridWindowController that = (TopGridWindowController) o;
        return Objects.equals(nameLabel, that.nameLabel) && Objects.equals(colorDisplay, that.colorDisplay) && Objects.equals(animationDisplay, that.animationDisplay)
                && Objects.equals(updateNewVersionButton, that.updateNewVersionButton) && Objects.equals(newVersionLabel, that.newVersionLabel)
                && Objects.equals(mainController, that.mainController) && Objects.equals(versionRefresher, that.versionRefresher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameLabel, colorDisplay, animationDisplay, updateNewVersionButton, newVersionLabel, mainController, versionRefresher);
    }
}
