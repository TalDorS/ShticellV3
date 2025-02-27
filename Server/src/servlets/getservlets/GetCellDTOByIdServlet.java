package servlets.getservlets;

import api.Engine;

import api.Expression;
import cells.Cell;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.CellDTO;
import dto.EngineDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import java.io.IOException;

@WebServlet("/getCellDTOById")
public class
GetCellDTOByIdServlet extends HttpServlet {


    Gson gson = new GsonBuilder()
            .disableHtmlEscaping()  // Slightly improve speed
            .serializeNulls()       // Handle null values more explicitly
            .setPrettyPrinting()    // for readable output
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // Get parameters from the request
        String userName = request.getParameter("userName");
        String spreadsheetName = request.getParameter("spreadsheetName");
        String cellId = request.getParameter("cellId");

        Engine engine = ServletUtils.getEngine(getServletContext());

        try {
            EngineDTO engineDTO = engine.getEngineData(userName, spreadsheetName);
            CellDTO cell = engineDTO.getCurrentSpreadsheet().getCellById(cellId);
            // If cell is found, convert it to JSON and send it back
            String jsonResponse = gson.toJson(cell);

            response.setStatus(HttpServletResponse.SC_OK); // 200 OK
            response.getWriter().write(jsonResponse);      // Write the JSON response to the body

        } catch (NullPointerException e) {
            // Handle case where spreadsheet might be null
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404 Not Found
            response.getWriter().write("Spreadsheet not found.");
        } catch (Exception e) {
            // Handle exceptions and send an error response
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write(e.getMessage());
        }
    }
}

