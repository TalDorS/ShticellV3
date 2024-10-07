package servlets;

import api.Engine;
import com.google.gson.Gson;
import enums.PermissionType;
import exceptions.engineexceptions.SpreadsheetNotFoundException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileNotFoundException;
import java.io.IOException;

@WebServlet(name = "GetUserPermissionServlet", urlPatterns = "/get-user-permission")
public class GetUserPermissionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String spreadsheetName = request.getParameter("spreadsheetName");

        // Get the engine instance (assuming it's available in the application context)
        Engine engine = (Engine) getServletContext().getAttribute("engine");

        // Thread-safe handling of user permission retrieval
        synchronized (engine) {
            PermissionType permissionType;
            try {
                permissionType = engine.getUserPermission(username, spreadsheetName);
            } catch (SpreadsheetNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Spreadsheet not found");
                return;
            }

            // Convert the PermissionType to JSON using Gson
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(permissionType);

            // Send the response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);
        }
    }
}
