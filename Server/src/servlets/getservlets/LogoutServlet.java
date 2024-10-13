package servlets.getservlets;

import engineimpl.EngineImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import user.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.Arrays;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        EngineImpl engine = (EngineImpl) ServletUtils.getEngine(getServletContext());

        if (usernameFromSession != null) {

            // Delete user and remove his uploaded spreadsheets
            engine.removeUser(usernameFromSession);
            SessionUtils.clearSession(request);
            response.setStatus(HttpServletResponse.SC_OK); // Set response to 200 OK
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Set response to 400 if no user found
            response.getWriter().write("No user session found for logout.");
        }
    }

}