package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import users.UsersManager;
import utils.ServletUtils;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private UsersManager usersManager;

    @Override
    public void init() {
        // Initialize UsersManager inside init() where ServletConfig is available
        usersManager = ServletUtils.getUserManager(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");

        synchronized (usersManager) {
            // If username is already logged in, send an error response
            if (usersManager.isUserExist(username)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write(new Gson().toJson("User is already logged in."));
            } else {
                // Add user to the active users list
                try {
                    usersManager.addUser(username);

                    // Create session and store user information
                    HttpSession session = req.getSession(true);
                    session.setAttribute("username", username);

                    // Set a session cookie
                    Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
                    sessionCookie.setHttpOnly(true);
                    sessionCookie.setMaxAge(30 * 60); // TODO - check if needed TAL
                    resp.addCookie(sessionCookie);

                    // Success response
                    resp.setContentType("application/json");
                    resp.getWriter().write(new Gson().toJson("Login Successful"));
                } catch (Exception e) {
                    // Handle user addition failure
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(new Gson().toJson("Failed to log in user."));
                }
            }
        }
    }
}
