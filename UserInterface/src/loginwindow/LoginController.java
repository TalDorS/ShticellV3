package loginwindow;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import manager.AppManager;
import menuwindow.MenuWindowController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {


    @FXML
    private TextField userNameField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;


    private CookieJar cookieJar = new CookieJar() {
        private List<Cookie> cookies = new ArrayList<>();

        @Override
        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> cookies) {
            this.cookies = cookies;
        }

        @NotNull
        @Override
        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
            return cookies != null ? cookies : new ArrayList<>();
        }
    };

    private OkHttpClient client = new OkHttpClient
            .Builder()
            .cookieJar(cookieJar)
            .build();

    @FXML
    private void handleLoginAction() {
        String userName = this.userNameField.getText();

        if (userName.isEmpty()) {
            this.messageLabel.setText("Please enter a username");
            return;
        }
        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> messageLabel.setText("Login failed"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.code() != 200) {
                    Platform.runLater(() ->
                            messageLabel.setText("Something went wrong:\n" + responseBody)
                    );
                } else {
                    Platform.runLater(() -> {
                        messageLabel.setText("Login successful");

                        PauseTransition pause = new PauseTransition(Duration.seconds(1)); // Set the delay to 1 seconds
                        pause.setOnFinished(event -> proceedToMenuWindow()); // Proceed to the menu window after the pause
                        pause.play(); // Start the pause transition
                    });
                }
            }
        });
    }


    private void proceedToMenuWindow() {
        try {
            // Load the Menu screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuwindow/MenuWindow.fxml"));
            Parent menuRoot = loader.load();

            // Set the username in the menu window
            MenuWindowController menuController = loader.getController();
            menuController.setUserName(userNameField.getText());

            // Set the scene to the menu
            Stage stage = (Stage) loginButton.getScene().getWindow();  // Get the current window
            stage.setTitle("Menu Window for " + userNameField.getText());
            Scene menuScene = new Scene(menuRoot);
            stage.setScene(menuScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Failed to load the menu.");
        }
    }

    public void setMessageLabel(String message) {
        this.messageLabel.setText(message);
    }
}
