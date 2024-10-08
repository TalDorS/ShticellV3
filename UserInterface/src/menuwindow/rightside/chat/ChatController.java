package menuwindow.rightside.chat;

import chat.SingleChatEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import menuwindow.MenuWindowController;
import menuwindow.rightside.RightSideController;
import okhttp3.*;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static utils.AlertUtils.showError;

public class ChatController {
    private RightSideController mainController;

    @FXML
    private TextField messageTextField;

    @FXML
    private VBox messageContainer;

    @FXML
    private void handleButtonClick() {
        String message = messageTextField.getText();

        // Check for empty message
        if (message.length() <= 0) {
            return;
        }

        // Prepare the POST request body
        String finalUrl = "http://localhost:8080/Server_Web_exploded/add-chat-message";
        RequestBody body = new FormBody.Builder()
                .add("message", message)
                .build();

        // Make the http request
        HttpClientUtil.runAsyncPost(finalUrl, body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showError("Failed to send message: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        messageTextField.clear();
                        updateMessagesTextField();
                    });
                } else {
                    Platform.runLater(() -> showError("Error: " + response.message()));
                }
            }
        });
    }

    private void updateMessagesTextField() {
        String finalUrl = "http://localhost:8080/Server_Web_exploded/get-chat-data-list";

        // Make GET request
        HttpClientUtil.runAsyncGet(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showError("Failed to fetch chat data: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();

                    // Parse the JSON response into a List
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<SingleChatEntry>>() {}.getType();
                    List<SingleChatEntry> chatMessages = gson.fromJson(jsonResponse, listType);

                    // Update the chat UI
                    Platform.runLater(() -> {
                        messageContainer.getChildren().clear();
                        for (SingleChatEntry chatMessage : chatMessages) {
                            Label messageLabel = new Label(chatMessage.toString());
                            messageLabel.setWrapText(true);
                            messageContainer.getChildren().add(messageLabel);
                        }
                    });
                } else {
                    Platform.runLater(() -> showError("Error: " + response.message()));
                }
            }
        });
    }

    public void setMainController(RightSideController mainController) {
        this.mainController = mainController;
    }
}
