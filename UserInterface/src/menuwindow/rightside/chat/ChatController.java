package menuwindow.rightside.chat;

import chat.SingleChatEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import menuwindow.rightside.RightSideController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.ClientConstants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Timer;

import static utils.AlertUtils.showError;

public class ChatController {
    private RightSideController mainController;
    private Timer chatTimer;

    @FXML
    private TextField messageTextField;

    @FXML
    private VBox messageContainer;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private void initialize() {
        startChatRefresher();
    }

    @FXML
    private void handleButtonClick() {
        String message = messageTextField.getText();

        // Check for empty message
        if (message.length() <= 0) {
            return;
        }

        // Prepare the POST request body
        String finalUrl = ClientConstants.ADD_CHAT_MESSAGE;
        RequestBody body = new FormBody.Builder()
                .add("message", message)
                .build();

        // Make the http request
        HttpClientUtil.runAsyncPost(finalUrl, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to send message: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        messageTextField.clear();
                        updateMessagesTextField();

                        // Scroll to the bottom after messages are added
                        chatScrollPane.layout();
                        chatScrollPane.setVvalue(1D); // Scroll to the bottom
                    });
                } else {
                    Platform.runLater(() -> showError("Error: " + response.message()));
                }
            }
        });
    }

    public void updateMessagesTextField() {
        String finalUrl = ClientConstants.GET_CHAT_DATA_LIST;

        // Make GET request
        HttpClientUtil.runAsyncGet(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call,@NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to fetch chat data: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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

    // Method to start the chat refresher
    private void startChatRefresher() {
        chatTimer = new Timer(true);  // Daemon timer, so it doesn't prevent app exit
        ChatRefresher chatRefresher = new ChatRefresher(this);
        chatTimer.schedule(chatRefresher, 0, 2000); // Schedule every 2 seconds
    }

    public void setMainController(RightSideController mainController) {
        this.mainController = mainController;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatController that = (ChatController) o;
        return Objects.equals(mainController, that.mainController) && Objects.equals(chatTimer, that.chatTimer)
                && Objects.equals(messageTextField, that.messageTextField) && Objects.equals(messageContainer, that.messageContainer)
                && Objects.equals(chatScrollPane, that.chatScrollPane);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainController, chatTimer, messageTextField, messageContainer, chatScrollPane);
    }
}
