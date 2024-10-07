package servlets.getservlets;

import api.Engine;
import com.google.gson.Gson;
import dto.PermissionsManagerDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "GetPermissionsBySheetServlet", urlPatterns = "/getPermissions")
public class GetPermissionsBySheetServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String spreadsheetName = req.getParameter("spreadsheetName");

        // Retrieve the engine instance from the servlet context
        Engine engine = ServletUtils.getEngine(getServletContext());

        if (spreadsheetName == null || spreadsheetName.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing or empty parameters: Spreadsheet Name.\"}");
            return;
        }

        // Get the permissions data for the given spreadsheet
        PermissionsManagerDTO permissionsData = engine.getPermissionsData(spreadsheetName);

        if (permissionsData != null) {
            // Serialize the permissions into JSON
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(permissionsData);

            // Set response headers and send the JSON response
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Permissions not found for the spreadsheet.\"}");
        }
    }
}
