package servlets.postservlets;

import api.Engine;
import com.google.gson.Gson;
import enums.PermissionType;
import exceptions.SpreadsheetNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "SubmitRequestForPermissionServlet", urlPatterns = "/requestPermission")
public class RequestPermissionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String spreadsheetName = request.getParameter("spreadsheetName");
        String permissionType = request.getParameter("permissionType");

        // Get the engine instace
        Engine engine = ServletUtils.getEngine(getServletContext());

        // Prepare GSON
        Gson gson = new Gson();
        Map<String, String> jsonResponse = new HashMap<>();

        // Ask for permission
        engine.askForPermission(username, spreadsheetName, PermissionType.valueOf(permissionType));
        jsonResponse.put("status", "PERMISSION_REQUESTED");

        // Set the content type and write the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}
