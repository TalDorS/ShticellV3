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

@WebServlet("/getSpreadsheet")
public class GetSpreadsheetDTOServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Enable pretty printing

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Engine engine = ServletUtils.getEngine(getServletContext());

        // Retrieve parameters from the request
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");

        // Validate input parameters
        if (userName == null || spreadsheetName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid input parameters.");
            return;
        }

        try {
            // Retrieve the current spreadsheet
            EngineDTO engineDTO = engine.getEngineData(userName, spreadsheetName);
            SpreadsheetDTO spreadsheetDTO = engineDTO.getCurrentSpreadsheet();

            // Check if the spreadsheet exists
            if (spreadsheetDTO != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(spreadsheetDTO)); // Convert to JSON and send the response
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Spreadsheet not found.");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error retrieving spreadsheet: " + e.getMessage());
        }
    }
}
