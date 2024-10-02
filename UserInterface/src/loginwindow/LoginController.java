package loginwindow;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import manager.AppManager;
import manager.AppManagerController;
import menuwindow.MenuWindowController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    private AppManager mainController;

    public void setMainController(AppManager appManager) {
        this.mainController = appManager;
    }

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
        String username = this.usernameField.getText();

        if (username.isEmpty()) {
            this.messageLabel.setText("Please enter a username");
            return;
        }

        // Build request
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:8080/Server_Web_exploded/login")
                .post(formBody)
                .build();

        // Execute request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> messageLabel.setText("Login failed"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        messageLabel.setText("Login successful");
                        // Now the session cookie is stored in cookieJar for future requests
                        mainController.moveFromLoginToMenu();
                    });
                } else if (response.code() == 409) {
                    Platform.runLater(() -> messageLabel.setText("User is already logged in."));
                } else {
                    Platform.runLater(() -> messageLabel.setText("Login failed: " + response.message()));
                }
            }
        });
    }
}
