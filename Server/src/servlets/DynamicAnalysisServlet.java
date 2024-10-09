package servlets;

import api.Engine;
import cells.Cell;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dto.EngineDTO;
import expressionimpls.LiteralExpression;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import spreadsheet.Spreadsheet;
import utils.ServletUtils;
import dto.common.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/dynamicAnalysis") // The URL mapping for this servlet
public class DynamicAnalysisServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DynamicAnalysisRequest dynamicAnalysisRequest;

        try {
            dynamicAnalysisRequest = gson.fromJson(new InputStreamReader(request.getInputStream()), DynamicAnalysisRequest.class);
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid JSON format.");
            return;
        }

        String cellId = dynamicAnalysisRequest.getCellId();
        double tempValue = dynamicAnalysisRequest.getTempValue();
        String userName = dynamicAnalysisRequest.getUserName();
        String spreadsheetName = dynamicAnalysisRequest.getSpreadsheetName();

        try {
            List<UpdatedCell> updatedCells = updateDependentCellsForDynamicAnalysis(userName, spreadsheetName, cellId, tempValue);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(updatedCells));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error Performing Dynamic Analysis: " + e.getMessage());
        }
    }

    private List<UpdatedCell> updateDependentCellsForDynamicAnalysis(String userName, String spreadsheetName, String cellId, double tempValue) throws Exception {
        // Get the current spreadsheet
        Engine engine = ServletUtils.getEngine(getServletContext());
        Spreadsheet currentSpreadsheet = engine.getCurrentSpreadsheet(userName, spreadsheetName);

        if (currentSpreadsheet == null) {
            throw new Exception("Current spreadsheet is null.");
        }

        // Temporarily update the value of the target cell in the spreadsheet's backend
        Cell cell = currentSpreadsheet.getCellById(cellId);

        if (cell != null) {
            cell.setExpression(new LiteralExpression(tempValue));
            cell.setEffectiveValue();
        }

        // Get the sorted list of cells to update using topological sort
        List<String> sortedCells = currentSpreadsheet.topologicalSort();
        List<UpdatedCell> updatedCells = new ArrayList<>();

        // Update each cell in the topological order
        for (String sortedCellId : sortedCells) {
            Cell dependentCell = currentSpreadsheet.getCellById(sortedCellId);

            if (dependentCell != null) {
                dependentCell.setEffectiveValue();
                Object effectiveValue = dependentCell.getEffectiveValue();

                // Create an updated cell response
                updatedCells.add(new UpdatedCell(sortedCellId, effectiveValue));
            }
        }
        return updatedCells; // Return the updated cell values to the UI
    }
}

