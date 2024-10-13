package chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatManager that = (ChatManager) o;
        return Objects.equals(chatDataList, that.chatDataList);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chatDataList);
    }
}
