package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.User;
import okhttp3.Response;
import utils.ResponseUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Set<String> users = new HashSet<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Gson gson = new Gson();
        String body = req.getReader().lines().reduce("", String::concat);
        User user = gson.fromJson(body, User.class);

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            ResponseUtils.sendErrorResponse(resp, "Username is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (users.contains(user.getUsername())) {
            ResponseUtils.sendErrorResponse(resp, "Username already exists", HttpServletResponse.SC_CONFLICT);
        } else {
            users.add(user.getUsername());
            ResponseUtils.sendSuccessResponse(resp, "Login Successful");
        }
    }
}
