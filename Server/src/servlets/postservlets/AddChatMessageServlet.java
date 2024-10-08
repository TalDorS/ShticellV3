package servlets.postservlets;

import chat.ChatManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "AddChatMessageServlet", urlPatterns = "/add-chat-message")
public class AddChatMessageServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the username from the session
        String username = SessionUtils.getUsername(request);

        // Get the chat message from the request
        String message = request.getParameter("message");

        // Get the ChatManager instance
        ChatManager chatManager = ServletUtils.getChatManager(getServletContext());

        if (username != null && message != null && !message.trim().isEmpty()) {
            // Thread-safe addition of the message
            synchronized (chatManager) {
                chatManager.addChatString(message, username);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid request: username or message is missing.");
        }
    }
}
