package utils;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResponseUtils {
    private static final Gson gson = new Gson();

    public static void sendErrorResponse(HttpServletResponse resp, String message, int statusCode) throws IOException {
        resp.setStatus(statusCode);
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(response));
    }

    public static void sendSuccessResponse(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(response));
    }
}
