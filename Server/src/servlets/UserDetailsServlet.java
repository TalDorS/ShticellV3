package servlets;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "UserDetailsServlet", urlPatterns = "/user-details")
public class UserDetailsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get the username from session
        String username = (String) req.getSession().getAttribute("username");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (username != null) {
            // Prepare the response JSON using Gson
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("username", username);

            // Write JSON response
            resp.getWriter().write(jsonResponse.toString());
        } else {
            // If no user is logged in, return an error message
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "No user is logged in.");

            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(errorResponse.toString());
        }
    }
}
