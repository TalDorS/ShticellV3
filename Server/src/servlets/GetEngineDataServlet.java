package servlets;

import api.Engine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.EngineDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "EngineDataServlet", urlPatterns = "/getEngineData")
public class GetEngineDataServlet extends HttpServlet {

    Gson gson = new GsonBuilder()
            .disableHtmlEscaping()  // Slightly improve speed
            .serializeNulls()       // Handle null values more explicitly
            .setPrettyPrinting()    // Optional: for readable output
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");

        // Retrieve the engine instance from the servlet context
        Engine engine = ServletUtils.getEngine(getServletContext());

        if (userName == null || spreadsheetName == null || userName.isEmpty() || spreadsheetName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing or empty parameters: userName or spreadsheetName.\"}");
            return;
        }

        try {
            // Get EngineDTO for the given user and file
            EngineDTO engineDTO = engine.getEngineData(userName, spreadsheetName);

            // Convert EngineDTO to JSON
            String jsonResponse = gson.toJson(engineDTO);
            System.out.println("EngineDataServlet: " + jsonResponse);

            // Set response status to OK and send the JSON response
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            // Handle any exception that occurred during the process
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
        }
    }
}
