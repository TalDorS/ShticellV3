package servlets.getservlets;

import api.Engine;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

import java.util.Map;

@WebServlet("/isSpreadsheetLoaded")
public class IsSpreadsheetLoadedServlet extends HttpServlet {

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
            boolean isLoaded = engine.getCurrentSpreadsheet(userName, spreadsheetName) != null;
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(String.valueOf(isLoaded));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
        }
    }
}
