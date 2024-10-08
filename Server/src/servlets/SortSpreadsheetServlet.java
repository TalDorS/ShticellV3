package servlets;

import api.Engine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.SpreadsheetDTO;
import exceptions.engineexceptions.InvalidColumnException;
import exceptions.engineexceptions.SpreadsheetNotFoundException;
import exceptions.engineexceptions.UserNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import spreadsheet.Spreadsheet;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/sortSpreadsheet") // Specify the URL pattern for this servlet
public class SortSpreadsheetServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Enable pretty printing

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // Retrieve parameters from the request
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");
        String range = request.getParameter("range");
        String[] columnsToSortBy = request.getParameterValues("columnsToSortBy");

        // Ensure all parameters are present
        if (userName == null || spreadsheetName == null || range == null || columnsToSortBy == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid input parameters.");
            return;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());

        try {
            // Call the engine method to sort the spreadsheet
            Spreadsheet sortedSpreadsheet = new Spreadsheet(engine.getCurrentSpreadsheet(userName, spreadsheetName));
            Map<String, String> idMapping = engine.sortSpreadsheet(userName, spreadsheetName, sortedSpreadsheet, range, List.of(columnsToSortBy));

            // Convert the sorted spreadsheet (domain model) to a SpreadsheetDTO
            SpreadsheetDTO sortedSpreadsheetDTO = engine.convertSpreadsheetToDTO(sortedSpreadsheet);

            // Create a response JSON object
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(Map.of(
                    "sortedSpreadsheet", sortedSpreadsheetDTO,
                    "idMapping", idMapping
            )));
        }catch (UserNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("User not found.");
        }
        catch(SpreadsheetNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Spreadsheet not found.");
        }
        catch(InvalidColumnException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(e.getMessage());
        }
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error sorting spreadsheet: " + e.getMessage());
            System.err.println("Error sorting spreadsheet: " + e.getMessage());
        }
    }
}
