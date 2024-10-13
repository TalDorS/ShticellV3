package servlets;

import api.Engine;
import com.google.gson.Gson;
import exceptions.SpreadsheetNotFoundException;
import exceptions.UserNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.*;

@WebServlet("/filterTableMultipleColumns")
public class FilterTableMultipleColumnsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // Retrieve parameters from the request
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");
        String tableArea = request.getParameter("tableArea");

        // Ensure required parameters are present
        if (userName == null || spreadsheetName == null || tableArea == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required parameters.");
            return;
        }

        // Extract selected column values from the request parameters
        Map<String, List<String>> selectedColumnValues = new HashMap<>();

        // Assuming columns were prefixed with "selectedColumn_" in the client request
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            if (paramName.startsWith("selectedColumn_")) {
                String columnName = paramName.substring("selectedColumn_".length()); // Extract the actual column name
                String value = request.getParameter(paramName);

                // Add the value to the list of selected values for this column
                selectedColumnValues.computeIfAbsent(columnName, k -> new ArrayList<>()).add(value);
            }
        }

        // Retrieve the engine instance from the servlet context
        Engine engine = ServletUtils.getEngine(getServletContext());

        try {
            // Call the engine method to filter the table
            List<String[][]> filteredTable = engine.filterTableMultipleColumns(userName, spreadsheetName, tableArea, selectedColumnValues);

            // Send back the response with the filtered table
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(new Gson().toJson(filteredTable)); // Write the table as JSON

        }catch (SpreadsheetNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Spreadsheet not found.");
        }catch(UserNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("User not found.");
        } catch (Exception e) {
            // Handle any exceptions that occur during filtering
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred: " + e.getMessage());
        }
    }
}
