package servlets.getservlets;

import api.Engine;
import exceptions.SpreadsheetNotFoundException;
import exceptions.UserNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet("/getColumnName") // Ensure this matches your routing
public class GetColumnNameServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // Retrieve parameters from the query string (GET parameters)
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");
        String indexString = request.getParameter("index");

        // Check if required parameters are missing
        if (userName == null || spreadsheetName == null || indexString == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required parameters.");
            return;
        }

        // Parse the index from the query (ensure it's an integer)
        int index;
        try {
            index = Integer.parseInt(indexString);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid column index format.");
            return;
        }

        // Retrieve the engine instance from the servlet context
        Engine engine = ServletUtils.getEngine(getServletContext());

        try {
            // Get the column name from the engine
            String columnName = engine.getColumnName(userName, spreadsheetName, index);

            // Send back the response with the column name
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(columnName); // Return column name as plain text

        } catch (SpreadsheetNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Spreadsheet not found.");
        } catch (UserNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("User not found.");
        } catch (Exception e) {
            // Handle any other exceptions
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred: " + e.getMessage());
        }
    }
}
