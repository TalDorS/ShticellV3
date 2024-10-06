package gridwindow.top;

import gridwindow.GridWindowController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

public class TopGridWindowController {
    @FXML
    private Label nameLabel;

    @FXML
    private MenuButton colorDisplay;

    @FXML
    private MenuButton animationDisplay;

    private GridWindowController mainController;

    @FXML
    public void initialize() {
        colorDisplay.setOnShowing(event -> handleColorDisplay());
        animationDisplay.setOnShowing(event -> handleAnimationDisplay());
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
}
