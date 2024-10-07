package servlets.getservlets;

import api.Engine;
import com.google.gson.Gson;
import dto.EngineDTO;
import dto.RangeDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/getRanges") // The URL to access this servlet
public class GetRangesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set content type to JSON
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Get Engine instance from Servlet Context
        Engine engine = ServletUtils.getEngine(getServletContext());

        // Extract parameters from the request
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");

        // Validate parameters
        if (userName == null || spreadsheetName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing userName or spreadsheetName parameter.\"}");
            return;
        }

        try {
            EngineDTO engineDTO = engine.getEngineData(userName, spreadsheetName);
            List<RangeDTO> rangesDTO = engineDTO.getRanges();
            response.setStatus(HttpServletResponse.SC_OK);
            // Convert the map to JSON
            Gson gson = new Gson();
            String rangesJson = gson.toJson(rangesDTO);
            response.getWriter().write(rangesJson);

        } catch (Exception e) {
            // Handle exceptions and return an error response
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error while fetching ranges: " + e.getMessage() + "\"}");
        }
    }
}
