package servlets;

import api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet("/addRange") // Specify the URL pattern for this servlet
public class AddRangeServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Engine engine = ServletUtils.getEngine(getServletContext());

        // Retrieve parameters from the request
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");
        String rangeName = request.getParameter("rangeName");
        String firstCell = request.getParameter("firstCell");
        String lastCell = request.getParameter("lastCell");

        System.out.println("firstCell: " + firstCell + " lastCell: " + lastCell+ " rangeName: " + rangeName+ " spreadsheetName: " + spreadsheetName+ " userName: " + userName);
        // Check if parameters are valid (you can implement more validation)
        if (userName == null || spreadsheetName == null || rangeName == null || firstCell == null || lastCell == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid input parameters.\"}");
            return;
        }

        try {
            // Add the range using your engine/service class
            engine.addRange(userName, spreadsheetName, rangeName, firstCell, lastCell);
            // Respond with a success message
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\": \"Range created successfully.\"}");

        } catch (Exception e) {
            // Handle any exceptions that occur
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Error creating range: " + e.getMessage() + "\"}");
        }
    }
}
