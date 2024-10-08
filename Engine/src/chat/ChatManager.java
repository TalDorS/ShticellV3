package chat;

import java.util.ArrayList;
import java.util.List;

public class ChatManager {
    private final List<SingleChatEntry> chatDataList;

    public ChatManager() {
        chatDataList = new ArrayList<>();
    }

    public synchronized void addChatString(String chatString, String username) {
        chatDataList.add(new SingleChatEntry(chatString, username));
    }

    public synchronized List<SingleChatEntry> getChatEntries() {
        return chatDataList;
    }

    public int getVersion() {
        return chatDataList.size();
    }
}
