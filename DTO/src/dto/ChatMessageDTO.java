package dto;

import java.util.Objects;

public class ChatMessageDTO {
    private final String chatString;
    private final String username;

    public ChatMessageDTO(String chatString, String username) {
        this.chatString = chatString;
        this.username = username;
    }

    public String getChatString() {
        return chatString;
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
        ChatMessageDTO that = (ChatMessageDTO) o;
        return Objects.equals(chatString, that.chatString) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatString, username);
    }
}
