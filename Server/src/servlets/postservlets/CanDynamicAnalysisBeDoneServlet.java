package servlets.postservlets;

import api.Engine;
import api.Expression;
import cells.Cell;
import com.google.gson.Gson;
import expressionimpls.FunctionExpression;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/canDynamicAnalysisBeDone")
public class CanDynamicAnalysisBeDoneServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Get the paramters from the request
        String spreadsheetName = request.getParameter("spreadsheetName");
        String cellId = request.getParameter("cellId");

        // Get Engine Instace
        Engine engine = ServletUtils.getEngine(getServletContext());

        // Initialize a response map
        Map<String, String> resultMap = new HashMap<>();

        try {
            // Get the expression from the engine
            Cell cell = engine.getCell(spreadsheetName, cellId);

            // Check if the cell is null or not a number
            if (cell == null || !(cell.getEffectiveValue() instanceof Number)) {
                resultMap.put("status", "ERROR");
                resultMap.put("message", "Dynamic Analysis can only be performed on cells with numeric values.");
            } else {
                Expression cellExpression = cell.getExpression();

                // Check if the cell's value is a function expression
                if (cellExpression instanceof FunctionExpression) {
                    resultMap.put("status", "ERROR");
                    resultMap.put("message", "Dynamic Analysis cannot be performed on numbers made out of functions.");
                } else {
                    // If all checks pass, dynamic analysis can be done
                    resultMap.put("status", "SUCCESS");
                }
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during processing
            resultMap.put("status", "ERROR");
            resultMap.put("message", "An error occurred: " + e.getMessage());
        }

        // Send the response back as JSON
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(resultMap);
        response.getWriter().write(jsonResponse);
    }
}
