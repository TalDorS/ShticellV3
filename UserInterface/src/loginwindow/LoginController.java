package loginwindow;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import menuwindow.MenuWindowController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.ClientConstants;
import utils.HttpClientUtil;
import utils.SimpleCookieManager;

import java.io.IOException;
import java.util.List;

import static utils.CommonResourcesPaths.MENU_WINDOW_FXML;

public class LoginController {

    @FXML
    private TextField userNameField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    // Create an instance of SimpleCookieManager
    private SimpleCookieManager cookieManager = new SimpleCookieManager();

    private OkHttpClient client = new OkHttpClient
            .Builder()
            .cookieJar(cookieManager)
            .build();

    @FXML
    private void handleLoginAction() {
        String userName = this.userNameField.getText();

        if (userName.isEmpty()) {
            this.messageLabel.setText("Please enter a username");
            return;
        }
        String finalUrl = HttpUrl
                .parse(ClientConstants.LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
                .build()
                .toString();

        HttpClientUtil.runAsyncGet(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> messageLabel.setText("Login failed, couldn't connect to server."));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();

                if (response.code() != 200) {
                    Platform.runLater(() ->
                            messageLabel.setText("Something went wrong:\n" + responseBody)
                    );
                } else {
                    // Extract cookies from response headers
                    List<Cookie> responseCookies = Cookie.parseAll(HttpUrl.parse(finalUrl), response.headers());

                    // Save cookies in the SimpleCookieManager
                    cookieManager.saveFromResponse(HttpUrl.parse(finalUrl), responseCookies);

                    Platform.runLater(() -> {
                        messageLabel.setText("Login successful");
                        messageLabel.setStyle("-fx-text-fill: green;"); // Set the text color to green
                        PauseTransition pause = new PauseTransition(Duration.seconds(0.5)); // Set the delay to 1 seconds
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MENU_WINDOW_FXML));
            Parent menuRoot = loader.load();

            // Set the username in the menu window
            MenuWindowController menuController = loader.getController();
            menuController.setUserName(userNameField.getText());

            // Pass the OkHttpClient instance
            menuController.setOkHttpClient(client);

            // Pass the SimpleCookieManager instance
            menuController.setCookieManager(cookieManager);

            // Set the scene to the menu
            Stage stage = (Stage) loginButton.getScene().getWindow();  // Get the current window
            stage.setTitle("Menu Window for " + userNameField.getText());
            stage.setResizable(true);
            // Pass the stage to the menu controller
            menuController.setStage(stage);
            // Set the new scene and show it
            Scene menuScene = new Scene(menuRoot);
            stage.setScene(menuScene);

            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Failed to load the menu.");
        }
    }

    public void setMessageLabel(String message) {
        this.messageLabel.setText(message);
    }

    // Method to handle the window close event and call the logout servlet
    private void handleWindowClose(WindowEvent event) {
        String finalUrl = HttpUrl
                .parse(ClientConstants.LOGOUT)
                .url()
                .toString();

        // Create the logout request
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        // Make the logout request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Optionally log or handle logout failure
                //System.out.println("Logout request failed: " + e.getMessage());
                Platform.exit(); // Exit after handling response
                System.exit(0);  // Forcefully terminate all threads
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    //System.out.println("Logout successful");
                } else {
                    //System.err.println("Logout failed with response code: " + response.code());
                }
                response.close();
                Platform.exit(); // Exit after handling response
                System.exit(0);  // Forcefully terminate all threads
            }
        });
    }
}
