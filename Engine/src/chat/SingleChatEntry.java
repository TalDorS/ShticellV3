package chat;

import java.util.Objects;

public class SingleChatEntry {
    private final String chatString;
    private final String username;
    private final long time;

    public SingleChatEntry(String chatString, String username) {
        this.chatString = chatString;
        this.username = username;
        this.time = System.currentTimeMillis();
    }

    public String getChatString() {
        return chatString;
    }

    public long getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return (username != null ? username + ": " : "") + chatString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleChatEntry that = (SingleChatEntry) o;
        return time == that.time && Objects.equals(chatString, that.chatString) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatString, username, time);
    }
}
