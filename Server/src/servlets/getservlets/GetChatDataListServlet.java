package servlets.getservlets;

import chat.ChatManager;
import chat.SingleChatEntry;
import com.google.gson.Gson;
import dto.ChatMessageDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "GetChatDataListServlet", urlPatterns = "/getChatDataList")
public class GetChatDataListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the ChatManager instance (thread-safe)
        ChatManager chatManager = ServletUtils.getChatManager(getServletContext());

        // Fetch the list of chat messages
        List<ChatMessageDTO> chatMessages;
        synchronized (chatManager) {
            chatMessages = chatManager.getChatEntries()
                    .stream()
                    .map(entry -> new ChatMessageDTO(entry.getChatString(), entry.getUsername())) // Map to ChatMessageDTO
                    .collect(Collectors.toList());
        }

        // Convert the list to JSON using Gson
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(chatMessages);

        // Send the response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
