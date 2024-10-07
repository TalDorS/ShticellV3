package servlets.getservlets;

import api.Engine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.EngineDTO;
import dto.SpreadsheetDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet("/getSpreadsheetByVersion")
public class GetSpreadsheetByVersionServlet extends HttpServlet {

    Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");
        String versionNumberStr = request.getParameter("versionNumber");

        // Parse version number
        int versionNumber;
        try {
            versionNumber = Integer.parseInt(versionNumberStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            response.getWriter().write("Invalid version number.");
            return;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());

        try {
            EngineDTO engineDTO = engine.getEngineData(userName, spreadsheetName);
            // Fetch the spreadsheet by version number
            SpreadsheetDTO spreadsheetDTO = engineDTO.getSpreadsheetByVersion(versionNumber);

            if (spreadsheetDTO != null) {
                // Serialize the spreadsheet to JSON and send it in the response
                String jsonResponse = gson.toJson(spreadsheetDTO);
                response.setStatus(HttpServletResponse.SC_OK); // 200 OK
                response.getWriter().write(jsonResponse);
            } else {
                // If the spreadsheet is not found
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404 Not Found
                response.getWriter().write("Spreadsheet not found.");
            }
        } catch (Exception e) {
            // Handle exceptions (e.g., user or file not found)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write(e.getMessage());
        }
    }
}
