package loginwindow;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import models.User;
import services.LoginService;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    private LoginService loginService;
    private Runnable onLoginSuccess;

    public LoginController() {
        this.loginService = new LoginService();
    }

    @FXML
    private void handleLoginAction() {
        String username = usernameField.getText();

        // Check for invalid username
        if (username == null || username.trim().isEmpty()) {
            messageLabel.setText("Please enter a username");
            return;
        }

        // Perform login action using login service
        try {
            String response = loginService.login(new User(username));
            if (response.contains("successful")) {
                messageLabel.setText("Login successful!");

                // Call the success callback if login is successful
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
            } else {
                messageLabel.setText(response);  // Display server error message
            }
        } catch (IOException e) {
            messageLabel.setText("Error communicating with the server.");
            e.printStackTrace();
        }
    }

    public void setOnLoginSuccess(Runnable onSuccess) {
        this.onLoginSuccess = onSuccess;
    }
}
