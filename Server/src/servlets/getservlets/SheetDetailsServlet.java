package servlets.getservlets;

import api.Engine;
import com.google.gson.Gson;
import dto.SheetDetailsDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;
import versions.VersionsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SheetDetailsServlet", urlPatterns = "/getSheetDetails")
public class SheetDetailsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<SheetDetailsDTO> sheets = new ArrayList<>();
        response.setContentType("text/plain;charset=UTF-8");

        // Get user session and the engine
        String usernameFromSession = SessionUtils.getUsername(request);
        Engine engine = ServletUtils.getEngine(getServletContext());

        // Check if user is logged in
        if (usernameFromSession == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("You must be logged in to load a spreadsheet.}");
            return;
        }

        synchronized (this) {
            Map<String, VersionsManager> spreadsheetsMap = engine.getSpreadsheetsMap();

            // Iterate through the map entries (key-value pairs)
            for (Map.Entry<String, VersionsManager> entry : spreadsheetsMap.entrySet()) {
                String spreadsheetName = entry.getKey(); // Get the key
                VersionsManager versionManager = entry.getValue(); // Get the value (VersionsManager)

                // Get required details from the VersionsManager
                String sheetName = versionManager.getSpreadsheetName();
                String uploaderName = versionManager.getUploaderName();
                String sheetSize = versionManager.getRows() + "x" + versionManager.getCols();
                String permission = versionManager.getUserPermission(usernameFromSession).toString();

                // Add the sheet details to the list
                sheets.add(new SheetDetailsDTO(sheetName, uploaderName, sheetSize, permission));
            }

            // Convert the list of sheets to JSON and send it as the response
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(sheets);
            response.getWriter().write(jsonResponse);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
