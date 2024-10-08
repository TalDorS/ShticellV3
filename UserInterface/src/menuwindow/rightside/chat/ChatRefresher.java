package menuwindow.rightside.chat;

import javafx.application.Platform;

import java.util.TimerTask;

public class ChatRefresher extends TimerTask {
    private final ChatController chatController;

    // Constructor to pass the ChatController instance
    public ChatRefresher(ChatController chatController) {
        this.chatController = chatController;
    }

    @Override
    public void run() {
        // Ensure the UI update happens on the JavaFX Application Thread
        Platform.runLater(() -> chatController.updateMessagesTextField());
    }
}
