package servlets;

import api.Engine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exceptions.engineexceptions.SpreadsheetNotFoundException;
import exceptions.engineexceptions.UserNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet("/getColumnIndex") // Define the URL pattern for the servlet
public class GetColumnIndexServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // Retrieve parameters from the request
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");
        String columnName = request.getParameter("columnName");

        // Check if required parameters are missing
        if (userName == null || spreadsheetName == null || columnName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required parameters.");
            return;
        }

        // Retrieve the engine instance from the servlet context
        Engine engine = ServletUtils.getEngine(getServletContext());

        try {
            // Get the column index from the engine
            int columnIndex = engine.getColumnIndex(userName, spreadsheetName, columnName);

            // Send back the response with column index
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(Integer.toString(columnIndex)); // Write the integer as plain text

        } catch(UserNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("User not found.");
        }catch(SpreadsheetNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Spreadsheet not found.");
        }
        catch (Exception e) {
            // Handle other exceptions
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred: " + e.getMessage() );
            System.err.println("Error getting column index: " + e.getMessage());
        }
    }
}
