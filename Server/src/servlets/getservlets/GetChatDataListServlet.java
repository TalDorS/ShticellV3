package servlets.getservlets;

import chat.ChatManager;
import chat.SingleChatEntry;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetChatDataListServlet", urlPatterns = "/getChatDataList")
public class GetChatDataListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the ChatManager instance (thread-safe)
        ChatManager chatManager = ServletUtils.getChatManager(getServletContext());

        // Fetch the list of chat messages
        List<SingleChatEntry> chatMessages;
        synchronized (chatManager) {
            chatMessages = chatManager.getChatEntries(); // Assume this method exists in ChatManager
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
